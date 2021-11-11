/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
package org.apache.linkis.instance.label.async;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;


public class GenericAsyncConsumerQueue<T> implements AsyncConsumerQueue<T>{

    private static final Logger LOG = LoggerFactory.getLogger(GenericAsyncConsumerQueue.class);
    private ArrayBlockingQueue<T> innerQueue;

    private ReentrantLock consumeLock = new ReentrantLock();

    private ExecutorService consumeService;

    private Condition consumeCondition = consumeLock.newCondition();

    private AtomicInteger consumeBatchSize = new AtomicInteger(-1);

    private boolean exit = true;

    public GenericAsyncConsumerQueue(int maxQueueSize){
        this.innerQueue = new ArrayBlockingQueue<T>(maxQueueSize);
        this.consumeService = Executors.newSingleThreadExecutor();
    }

    @Override
    public int size() {
        return this.innerQueue.size();
    }

    @Override
    public boolean offer(T entity) {
        if(innerQueue.offer(entity)){
            tryToNotifyConsumer();
            return true;
        }
        return false;
    }

    @Override
    public boolean offer(T entity, long timeout, TimeUnit timeUnit) throws InterruptedException {
        if(innerQueue.offer(entity, timeout, timeUnit)){
            tryToNotifyConsumer();
            return true;
        }
        return false;
    }

    @Override
    public synchronized void consumer(int batchSize, long interval, TimeUnit timeUnit, Consumer<List<T>> consumer) {
        if(!exit){
            throw new IllegalStateException("The consumer has been defined, please don't define repeatedly");
        }
        this.consumeBatchSize.set(batchSize);
        this.consumeService.submit(()->{
            long triggerTime = System.currentTimeMillis();
            long intervalInMillis = TimeUnit.MILLISECONDS.convert(interval, timeUnit);
            while(!exit){
                long nowMillsTime = System.currentTimeMillis();
                if(triggerTime >= nowMillsTime){
                    LOG.trace("wait until the next time: " + triggerTime);
                    consumeLock.lock();
                    try {
                        consumeCondition.await(triggerTime - nowMillsTime , TimeUnit.MILLISECONDS);
                    } catch (InterruptedException e) {
                        LOG.error("Interrupt in awaiting action, message: [" + e.getMessage() +"]", e);
                        continue;
                    }finally{
                        if(consumeLock.isLocked()){
                            consumeLock.unlock();
                        }
                    }
                    try {
                        List<T> elementsWaitForDeal = new ArrayList<>();
                        //Do not block
                        innerQueue.drainTo(elementsWaitForDeal, innerQueue.size());
                        if (!elementsWaitForDeal.isEmpty()) {
                            consumer.accept(elementsWaitForDeal);
                        }
                    }catch(Throwable e){
                        LOG.error("Exception in consuming queue, message: [" + e.getMessage() +"]", e);
                    }
                }
                triggerTime = System.currentTimeMillis() + intervalInMillis;
            }
        });
        this.exit = false;
    }

    /**
     * Notify consumer
     */
    private void tryToNotifyConsumer(){
        if(innerQueue.size() >= consumeBatchSize.get()){
            consumeLock.lock();
            try{
                if(!innerQueue.isEmpty()){
                    consumeCondition.signalAll();
                }
            }finally{
                consumeLock.unlock();
            }
        }
    }
}
