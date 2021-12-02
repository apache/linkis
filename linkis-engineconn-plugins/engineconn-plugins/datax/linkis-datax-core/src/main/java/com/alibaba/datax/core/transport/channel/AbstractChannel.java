package com.alibaba.datax.core.transport.channel;

import com.alibaba.datax.common.util.Configuration;
import com.alibaba.datax.core.statistics.communication.Communication;
import com.alibaba.datax.core.statistics.communication.CommunicationTool;
import com.alibaba.datax.core.util.container.CoreConstant;
import com.google.common.util.concurrent.RateLimiter;
import org.apache.linkis.datax.core.transport.channel.ChannelElement;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

/**
 * Created by jingxing on 14-8-25.
 * Abstracted and enhanced by davidhua@webank.com
 */
public abstract class AbstractChannel<T extends ChannelElement> {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractChannel.class);

    protected int taskGroupId;

    private int capacity;

    protected int byteCapacity;

    /**
     * bps: bytes/s
     */
    private long byteSpeed;
    private RateLimiter rateLimiterByte;

    /**
     * tps: domain/s
     */
    protected long dataSpeed;

    RateLimiter rateLimiterData;

    private long flowControlInterval;

    private volatile boolean isClosed = false;

    protected Configuration configuration = null;

    protected AtomicLong waitReaderTime = new AtomicLong(0);

    protected AtomicLong waitWriterTime = new AtomicLong(0);

    private Boolean isFirstPrint = true;

    private Communication currentCommunication;

    private Communication lastCommunication = new Communication();

    /**
     * Consume lock
     */
    private ReentrantLock consumeLock = new ReentrantLock();

    /**
     * Number of consumers
     */
    protected int consumers = 0;

    /**
     * Cache from pulling
     */
    private List<T> cachePulled = new ArrayList<>();
    /**
     * If set consumeIsolated = true, channel will use consume cache to distinct different consuming
     */
    protected boolean consumeIsolated = false;
    /**
     * Semaphore for consumers
     */
    private AtomicInteger consumeSem = new AtomicInteger(consumers - 1);
    /**
     * Counters of consumers for consuming from cache
     */
    private ConcurrentHashMap<String, AtomicInteger> consumeCache = new ConcurrentHashMap<>();

    private Condition notConsumed = consumeLock.newCondition();

    /**
     *
     * @param configuration task configuration
     */
    public AbstractChannel(final Configuration configuration){
        this.configuration = configuration;
        int capacity = configuration.getInt(
                CoreConstant.DATAX_CORE_TRANSPORT_CHANNEL_CAPACITY, 2048);
        long byteSpeed = configuration.getLong(
                CoreConstant.DATAX_CORE_TRANSPORT_CHANNEL_SPEED_BYTE, 1024L * 1024L);

        if(capacity <= 0){
            throw new IllegalArgumentException(String.format(
                    "通道容量[%d]必须大于0.", capacity));
        }
        if(isFirstPrint){
            firstPrint();
            isFirstPrint = false;
        }
        this.taskGroupId = configuration.getInt(
                CoreConstant.DATAX_CORE_CONTAINER_TASKGROUP_ID);
        this.capacity = capacity;
        this.byteSpeed = byteSpeed;
        this.rateLimiterByte = RateLimiter.create(this.byteSpeed);
        this.flowControlInterval = configuration.getLong(
                CoreConstant.DATAX_CORE_TRANSPORT_CHANNEL_FLOWCONTROLINTERVAL, 1000);
        this.byteCapacity = configuration.getInt(
                CoreConstant.DATAX_CORE_TRANSPORT_CHANNEL_CAPACITY_BYTE, 8 * 1024 * 1024);
    }

    public void close(){
        this.isClosed = true;
    }

    public void open(){
        this.isClosed = false;
    }

    public boolean isClosed(){
        return isClosed;
    }

    public int getTaskGroupId(){
        return this.taskGroupId;
    }

    public int getCapacity(){
        return capacity;
    }

    public long getByteSpeed(){
        return byteSpeed;
    }

    public Configuration getConfiguration(){
        return this.configuration;
    }

    public void setCommunication(final Communication communication){
        this.currentCommunication = communication;
        this.lastCommunication.reset();
    }

    public void push(final T t){
        Validate.notNull(t, "push domain cannot be empty in channel");
        this.doPush(t);
        this.statPush(1L, t.getByteSize());
    }

    public void pushTerminate(final T t){
        Validate.notNull(t, "push domain cannot be empty in channel");
        this.doPush(t);
    }

    public void pushAll(final Collection<T> collection){
        Validate.notNull(collection);
        Validate.noNullElements(collection);
        for(T t : collection){
            push(t);
        }
    }

    public T pull(){
        List<T> pulled = (List<T>) doPullInSync(1, new ArrayList<>(), collection -> collection.add(this.doPull()));
        T data = pulled.get(0);
        this.statPull(1L, data.getByteSize());
        return data;
    }

    public void pullAll(final Collection<T> collection){
        Validate.notNull(collection);
        doPullInSync(Integer.MAX_VALUE, collection, this::doPullAll);
        this.statPull(collection.size(),  this.getByteSize(collection));
    }

    private Collection<T> doPullInSync(int maxPullSize, Collection<T> pulled, Consumer<Collection<T>> pullFunction){
        String hashCode = String.valueOf(Thread.currentThread().hashCode());
        pulled.clear();
        consumeLock.lock();
        try {
            while(!cachePulled.isEmpty()){
                AtomicInteger counter = consumeCache.computeIfAbsent(hashCode, key -> new AtomicInteger(0));
                //Only the different consuming threads can consume the cache
                int pos = counter.get();
                if(consumeSem.get() > 0 && pos >= cachePulled.size()){
                    try {
                        //Await other consumers
                        notConsumed.await();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new IllegalStateException(e);
                    }
                }else {
                    int count = 0;
                    for(int i = pos; count < maxPullSize && i < cachePulled.size(); i++){
                        //Consume cache
                        pulled.add((T) cachePulled.get(i).copyElement());
                        count ++;
                    }
                    if(counter.addAndGet(count) >= cachePulled.size() && consumeSem.decrementAndGet() <= 0){
                        //Empty the cache and reset the semaphore
                        cachePulled.clear();
                        consumeSem.set(consumers - 1);
                        consumeCache.forEach((key, value) -> value.set(0));
                        notConsumed.signalAll();
                    }
                    return pulled;
                }
            }
            //Fill pulled collection
            pullFunction.accept(pulled);
            if(consumers > 1 && consumeIsolated){
                //In the situation of multiply consumers, use the cache
                cachePulled.addAll(pulled);
                AtomicInteger counter = consumeCache.computeIfAbsent(hashCode, key -> new AtomicInteger(0));
                //Mark the current thread that has consumed the cache
                counter.set(pulled.size());
            }
            return pulled;
        }finally{
            consumeLock.unlock();
        }
    }
    private void statPull(long dataSize, long byteSize){

        statPull(currentCommunication, dataSize);
        currentCommunication.increaseCounter(CommunicationTool.WRITE_RECEIVED_BYTES, byteSize);
    }

    public void statPush(long dataSize,long byteSize){
        boolean isChannelByteSpeedLimit = (this.byteSpeed > 0);
        boolean isChannelDataSpeedLimit = (this.dataSpeed > 0);

        if(!isChannelByteSpeedLimit && !isChannelDataSpeedLimit){
            return;
        }
        if(byteSize > 0){
            rateLimiterByte.acquire((int)byteSize);
        }
        statPush(currentCommunication, dataSize);
        if(rateLimiterData != null && dataSize > 0){
            rateLimiterData.acquire((int)dataSize);
        }
        currentCommunication.increaseCounter(CommunicationTool.READ_SUCCEED_BYTES, byteSize);

        currentCommunication.setLongCounter(CommunicationTool.WAIT_READER_TIME, waitReaderTime.get());
        currentCommunication.setLongCounter(CommunicationTool.WAIT_WRITER_TIME, waitWriterTime.get());
    }

    public synchronized void adjustRateLimit(long byteSpeed, long dataSpeed){
        if(byteSpeed > 0 && null != this.rateLimiterByte &&
                byteSpeed != this.rateLimiterByte.getRate()) {
            this.rateLimiterByte.setRate(byteSpeed);
        }
        if(dataSpeed > 0 && null != this.rateLimiterData &&
                dataSpeed != this.rateLimiterData.getRate()) {
            this.rateLimiterData.setRate(dataSpeed);
        }
    }

    private long getByteSize(final Collection<T> rs){
        final long[] size = {0};
        rs.forEach(t -> size[0] += t.getByteSize());
        return size[0];
    }

    /**
     * do push
     * @param t
     */
    protected abstract void doPush(T t);

    /**
     * do push all
     * @param collection
     */
    protected void doPushAll(Collection<T> collection){
        //default not support
    }

    /**
     * do pull
     */
    protected abstract T doPull();

    /**
     * do pull all
     * @param collection
     */
    protected void doPullAll(Collection<T> collection){
        //default not support
    }

    /**
     * Increase consumer
     */
    public void incConsumer(){
        consumeSem.compareAndSet(consumers - 1, consumers);
        consumers ++;
    }

    /**
     * Do checkpoint
     * @param checkPointId
     */
    public void doCheckPoint(String checkPointId){
    }

    public abstract int size();

    public  abstract boolean isEmpty();

    public abstract void clear();

    /**
     * stat push
     * @param dataSize
     */
    protected abstract void statPush(Communication currentCommunication, long dataSize);

    /**
     * stat pull
     * @param currentCommunication
     * @param dataSize
     */
    protected abstract void statPull(Communication currentCommunication, long dataSize);
    /**
     * current domain speed
     * @return
     */
    protected abstract long currentDataSpeed(Communication currentCommunication, Communication lastCommunication, long interval);

    /**
     * update counter
     * @param currentCommunication
     * @param lastCommunication
     */
    protected abstract void updateCounter(Communication currentCommunication, Communication lastCommunication);
    /**
     *  the log printed in the first time
     */
    protected abstract void firstPrint();


}
