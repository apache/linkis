package com.alibaba.datax.core.statistics.communication;

import com.alibaba.datax.common.base.BaseObject;
import com.alibaba.datax.enums.State;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

/**
 * DataX所有的状态及统计信息交互类，job、taskGroup、task等的消息汇报都走该类
 */
public class Communication extends BaseObject implements Cloneable {
    /**
     * 所有的数值key-value对 *
     */
    private Map<String, Number> counter;

    /**
     * 运行状态 *
     */
    private State state;

    /**
     * 异常记录 *
     */
    private Throwable throwable;

    /**
     * 记录的timestamp *
     */
    private long timestamp;

    /**
     * task给job的信息 *
     */
    Map<String, List<String>> message;

    /**
     * Task's parameter
     */
    Map<String, Object> params;
    public Communication() {
        this.init();
    }

    public synchronized void reset() {
        this.init();
    }

    private void init() {
        this.counter = new ConcurrentHashMap<>();
        this.state = State.RUNNING;
        this.throwable = null;
        this.message = new ConcurrentHashMap<>();
        this.timestamp = System.currentTimeMillis();
        this.params = new ConcurrentHashMap<>();
    }

    public Map<String, Number> getCounter() {
        return this.counter;
    }

    public synchronized State getState() {
        return this.state;
    }

    public synchronized void setState(State state, boolean isForce) {
        if (!isForce && this.state.equals(State.FAILED)) {
            return;
        }

        this.state = state;
    }

    public synchronized void setState(State state) {
        setState(state, false);
    }

    public Throwable getThrowable() {
        return this.throwable;
    }

    public synchronized String getThrowableMessage() {
        return this.throwable == null ? "" : this.throwable.getMessage();
    }

    public void setThrowable(Throwable throwable) {
        setThrowable(throwable, false);
    }

    public synchronized void setThrowable(Throwable throwable, boolean isForce) {
        if (isForce) {
            this.throwable = throwable;
        } else {
            this.throwable = this.throwable == null ? throwable : this.throwable;
        }
    }

    public long getTimestamp() {
        return this.timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public Map<String, List<String>> getMessage() {
        return this.message;
    }

    public List<String> getMessage(final String key) {
        return message.get(key);
    }

    public synchronized void addMessage(final String key, final String value) {
        Validate.isTrue(StringUtils.isNotBlank(key), "增加message的key不能为空");
        List<String> valueList = this.message.computeIfAbsent(key, k -> new ArrayList<>());
        valueList.add(value);
    }

    /**
     * Add parameter
     * @param key key
     * @param value value
     */
    public synchronized void addParameter(final String key, final String value, boolean isUnique){
        Validate.isTrue(StringUtils.isNotBlank(key), "Parameter key cannot be empty(参数的key不能为空)");
        if(isUnique){
            this.params.put(key, value);
        }else {
            Object current = this.params.computeIfAbsent(key, k -> value);
            if (current instanceof List) {
                ((List<String>) current).add(value);
            } else if(!current.equals(value)){
                List<String> valueList = new ArrayList<>();
                valueList.add(value);
                this.params.put(key, valueList);
            }
        }
    }

    /**
     * Get parameter
     * @param key key
     * @return value object
     */
    public Object getParameter(final String key){
        return this.params.get(key);
    }

    /**
     * Get parameter map
     * @return value map
     */
    public Map<String, Object> getParameters(){
        return params;
    }
    public synchronized Long getLongCounter(final String key) {
        Number value = this.counter.get(key);

        return value == null ? 0 : value.longValue();
    }

    public synchronized void setLongCounter(final String key, final long value) {
        Validate.isTrue(StringUtils.isNotBlank(key), "设置counter的key不能为空");
        this.counter.put(key, value);
    }

    public synchronized Double getDoubleCounter(final String key) {
        Number value = this.counter.get(key);

        return value == null ? 0.0d : value.doubleValue();
    }

    public synchronized void setDoubleCounter(final String key, final double value) {
        Validate.isTrue(StringUtils.isNotBlank(key), "设置counter的key不能为空");
        this.counter.put(key, value);
    }

    public synchronized void increaseCounter(final String key, final long deltaValue) {
        Validate.isTrue(StringUtils.isNotBlank(key), "增加counter的key不能为空");

        long value = this.getLongCounter(key);

        this.counter.put(key, value + deltaValue);
    }

    @Override
    public Communication clone() {
        Communication communication = new Communication();

        /**
         * clone counter
         */
        if (this.counter != null) {
            for (Entry<String, Number> entry : this.counter.entrySet()) {
                String key = entry.getKey();
                Number value = entry.getValue();
                if (value instanceof Long) {
                    communication.setLongCounter(key, (Long) value);
                } else if (value instanceof Double) {
                    communication.setDoubleCounter(key, (Double) value);
                }
            }
        }

        communication.setState(this.state, true);
        communication.setThrowable(this.throwable, true);
        communication.setTimestamp(this.timestamp);

        /**
         * clone message
         */
        if (this.message != null) {
            for (final Entry<String, List<String>> entry : this.message.entrySet()) {
                String key = entry.getKey();
                List<String> value = new ArrayList<>(entry.getValue());
                communication.getMessage().put(key, value);
            }
        }

        return communication;
    }

    public synchronized Communication mergeFrom(final Communication otherComm) {
        if (otherComm == null) {
            return this;
        }

        /**
         * counter的合并，将otherComm的值累加到this中，不存在的则创建
         * 同为long
         */
        for (Entry<String, Number> entry : otherComm.getCounter().entrySet()) {
            String key = entry.getKey();
            Number otherValue = entry.getValue();
            if (otherValue == null) {
                continue;
            }

            Number value = this.counter.get(key);
            if (value == null) {
                value = otherValue;
            } else {
                if (value instanceof Long && otherValue instanceof Long) {
                    value = value.longValue() + otherValue.longValue();
                } else {
                    value = value.doubleValue() + value.doubleValue();
                }
            }

            this.counter.put(key, value);
        }

        // 合并state
        mergeStateFrom(otherComm);

        /**
         * 合并throwable，当this的throwable为空时，
         * 才将otherComm的throwable合并进来
         */
        this.throwable = this.throwable == null ? otherComm.getThrowable() : this.throwable;

        /**
         * timestamp是整个一次合并的时间戳，单独两两communication不作合并
         */

        /**
         * message的合并采取求并的方式，即全部累计在一起
         */
        for (Entry<String, List<String>> entry : otherComm.getMessage().entrySet()) {
            String key = entry.getKey();
            List<String> valueList = this.message.computeIfAbsent(key, k -> new ArrayList<>());
            valueList.addAll(entry.getValue());
        }
        //Merge parameters
        otherComm.getParameters().forEach((key, otherValue) ->{
            Object currentValue = this.params.computeIfAbsent(key, k -> new ArrayList<>());
            List<String> valueList;
            if(!(currentValue instanceof List)){
                valueList = new ArrayList<>();
                this.params.put(key, valueList);
                valueList.add(String.valueOf(currentValue));
            }else{
                valueList = (List<String>)currentValue;
            }
            if(otherValue instanceof List){
                valueList.addAll((List<String>)otherValue);
            }else{
                valueList.add(String.valueOf(otherValue));
            }
        });
        return this;
    }

    /**
     * 合并state，优先级： (Failed | Killed) > Running > Success
     * 这里不会出现 Killing 状态，killing 状态只在 Job 自身状态上才有.
     */
    public synchronized State mergeStateFrom(final Communication otherComm) {
        State retState = this.getState();
        if (otherComm == null) {
            return retState;
        }

        if (this.state == State.FAILED || otherComm.getState() == State.FAILED
                || this.state == State.KILLED || otherComm.getState() == State.KILLED) {
            retState = State.FAILED;
        } else if (this.state.isRunning() || otherComm.state.isRunning()) {
            retState = State.RUNNING;
        }

        this.setState(retState);
        return retState;
    }

    public synchronized boolean isFinished() {
        return this.state == State.SUCCEEDED || this.state == State.FAILED
                || this.state == State.KILLED;
    }

}
