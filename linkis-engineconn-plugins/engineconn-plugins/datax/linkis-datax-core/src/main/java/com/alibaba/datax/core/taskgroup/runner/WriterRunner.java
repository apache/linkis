package com.alibaba.datax.core.taskgroup.runner;

import com.alibaba.datax.common.element.*;
import com.alibaba.datax.common.exception.DataXException;
import com.alibaba.datax.common.plugin.AbstractTaskPlugin;
import com.alibaba.datax.common.plugin.BasicDataReceiver;
import com.alibaba.datax.common.plugin.RecordReceiver;
import com.alibaba.datax.common.spi.Writer;
import com.alibaba.datax.common.statistics.PerfRecord;
import com.alibaba.datax.core.statistics.communication.CommunicationTool;
import com.alibaba.datax.core.util.ClassUtil;
import com.alibaba.datax.core.util.FrameworkErrorCode;
import com.alibaba.datax.enums.State;
import org.apache.linkis.datax.core.ThreadLocalSecurityManager;
import org.apache.linkis.datax.core.processor.Processor;
import org.apache.linkis.datax.core.processor.ProcessorSecurityManager;
import org.apache.linkis.datax.core.transport.stream.ChannelInput;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InterruptedIOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Created by jingxing on 14-9-1.
 * <p/>
 * 单个slice的writer执行调用
 */
public class WriterRunner extends AbstractRunner implements Runnable {

    private static final Logger LOG = LoggerFactory
            .getLogger(WriterRunner.class);


    private boolean shutdown;

    private RecordReceiver recordReceiver;

    private ChannelInput channelInput;

    private String processor;

    public void setRecordReceiver(RecordReceiver receiver) {
        this.recordReceiver = receiver;
    }

    public void setChannelInput(ChannelInput channelInput){
        this.channelInput = channelInput;
    }

    public void setProcessor(String processor){
        this.processor = processor;
    }

    public WriterRunner(AbstractTaskPlugin abstractTaskPlugin) {
        super(abstractTaskPlugin);
    }

    @Override
    public void run() {
        Writer.Task taskWriter = (Writer.Task) this.getPlugin();
        //统计waitReadTime，并且在finally end
        PerfRecord channelWaitRead = new PerfRecord(getTaskGroupId(), getTaskId(), PerfRecord.PHASE.WAIT_READ_TIME);
        try {
            channelWaitRead.start();
            LOG.debug("task writer starts to do init ...");
            PerfRecord initPerfRecord = new PerfRecord(getTaskGroupId(), getTaskId(), PerfRecord.PHASE.WRITE_TASK_INIT);
            initPerfRecord.start();
            taskWriter.init();
            initPerfRecord.end();
            LOG.debug("task writer starts to do prepare ...");
            PerfRecord preparePerfRecord = new PerfRecord(getTaskGroupId(), getTaskId(), PerfRecord.PHASE.WRITE_TASK_PREPARE);
            preparePerfRecord.start();
            taskWriter.prepare();
            preparePerfRecord.end();
            LOG.debug("task writer starts to write ...");
            PerfRecord dataPerfRecord = new PerfRecord(getTaskGroupId(), getTaskId(), PerfRecord.PHASE.WRITE_TASK_DATA);
            dataPerfRecord.start();
            if(null != recordReceiver){
                if(StringUtils.isNotBlank(processor)){
                    ClassLoader currentClassLoader = Thread.currentThread().getContextClassLoader();
                    Processor<?> processIns = ClassUtil.instantiate(processor, Processor.class,
                            currentClassLoader);
                    startToWrite(taskWriter, recordReceiver, processIns, currentClassLoader);
                }else {
                    taskWriter.startWrite(recordReceiver);
                }
                dataPerfRecord.addCount(CommunicationTool.getTotalReadRecords(super.getRunnerCommunication()));
            }else if(null != channelInput){
                taskWriter.startWrite(channelInput);
            }
            dataPerfRecord.addSize(CommunicationTool.getTotalReadBytes(super.getRunnerCommunication()));
            dataPerfRecord.end();

            LOG.debug("task writer starts to do post ...");
            PerfRecord postPerfRecord = new PerfRecord(getTaskGroupId(), getTaskId(), PerfRecord.PHASE.WRITE_TASK_POST);
            postPerfRecord.start();
            taskWriter.post();
            postPerfRecord.end();
            super.markSuccess();
        } catch(Throwable e) {
            if(shutdown){
                //have been shutdown by task group container
                return;
            }
            Throwable cause = e;
            while(null != cause){
                if(cause instanceof InterruptedException || cause instanceof InterruptedIOException){
                    this.getRunnerCommunication().setState(State.KILLED);
                    return;
                }
                cause = cause.getCause();
            }
            LOG.error("Writer Runner Received Exceptions:", e);
            super.markFail(e);
        } finally {
            LOG.debug("task writer starts to do destroy ...");
            PerfRecord desPerfRecord = new PerfRecord(getTaskGroupId(), getTaskId(), PerfRecord.PHASE.WRITE_TASK_DESTROY);
            desPerfRecord.start();
            super.destroy();
            desPerfRecord.end();
            channelWaitRead.end(super.getRunnerCommunication().getLongCounter(CommunicationTool.WAIT_READER_TIME));
        }
    }

    public boolean supportFailOver() {
        Writer.Task taskWriter = (Writer.Task) this.getPlugin();
        return taskWriter.supportFailOver();
    }

    @Override
    public void shutdown() {
        shutdown = true;
        if(null != recordReceiver){
            recordReceiver.shutdown();
        }
        if(null != channelInput){
            channelInput.shutdown();
        }
    }

    private void startToWrite(Writer.Task taskWriter, RecordReceiver recordReceiver, Processor<?> processor
                                , ClassLoader runtimeClassLoader){
        Class<?> clazz = Object.class;
        Type[] types = processor.getClass().getGenericInterfaces();
        for(Type type : types){
            if(type instanceof ParameterizedType){
                ParameterizedType parameterizedType = (ParameterizedType)type;
                if(parameterizedType.getRawType().getTypeName().equals(
                        Processor.class.getTypeName()
                )){
                    clazz = (Class)parameterizedType.getActualTypeArguments()[0];
                    break;
                }
            }
        }
        ThreadLocalSecurityManager rootSecurityManager = null;
        if(System.getSecurityManager() instanceof ThreadLocalSecurityManager){
            rootSecurityManager = (ThreadLocalSecurityManager)System.getSecurityManager();
        }else{
            rootSecurityManager = new ThreadLocalSecurityManager();
            System.setSecurityManager(rootSecurityManager);
        }
        ThreadLocalSecurityManager finalRootSecurityManager = rootSecurityManager;
        ProcessorSecurityManager processorSecurityManager = new ProcessorSecurityManager(System.getProperty("user.dir"));
        if(clazz.equals(Record.class)){
            taskWriter.startWrite(new RecordReceiver() {
                @Override
                public Record getFromReader() {
                    return doInSecurity(finalRootSecurityManager, processorSecurityManager, ()->
                    {
                        try {
                            Record record ;
                            if(!Thread.currentThread().getContextClassLoader().equals(runtimeClassLoader)){
                                Thread.currentThread().setContextClassLoader(runtimeClassLoader);
                            }
                            while(null != (record = recordReceiver.getFromReader())){
                                Record result = (Record) processor.process(transformColumns(record.getColumns()));
                                if (null != result){
                                    return result;
                                }
                            }
                            return null;
                        } catch (Exception e) {
                            throw DataXException.asDataXException(FrameworkErrorCode.PROCESSOR_RUN_ERROR, e);
                        }
                    });
                }

                @Override
                public void shutdown() {
                    recordReceiver.shutdown();
                }
            });
        }else{
            taskWriter.startWrite(new BasicDataReceiver<Object>() {
                @Override
                public Object getFromReader() {
                    return doInSecurity(finalRootSecurityManager, processorSecurityManager, ()->
                    {
                        try {
                            Record record;
                            while(null != (record = recordReceiver.getFromReader())){
                                Object result = processor.process(transformColumns(record.getColumns()));
                                if (null != result){
                                    return result;
                                }
                            }
                            return null;
                        } catch (Exception e) {
                            throw DataXException.asDataXException(FrameworkErrorCode.PROCESSOR_RUN_ERROR, e);
                        }
                    });
                }

                @Override
                public void shutdown() {
                    recordReceiver.shutdown();
                }
            }, clazz);
        }
    }

    private <T>T doInSecurity(ThreadLocalSecurityManager rootSecurityManager,
                              ProcessorSecurityManager processorSecurityManager, Supplier<T> supplier){
        rootSecurityManager.setThreadSecurityManager(this, processorSecurityManager);
        try {
            return supplier.get();
        }finally {
            rootSecurityManager.removeThreadSecurityManager(this);
        }
    }

    private List<Object> transformColumns(List<Column> columns){
        List<Object> columnData = new ArrayList<>();
        columns.forEach(column -> {
            if(column instanceof StringColumn){
                columnData.add(column.asString());
            }else if(column instanceof BytesColumn){
                columnData.add(column.asBytes());
            }else if(column instanceof BoolColumn){
                columnData.add(column.asBoolean());
            }else if(column instanceof DateColumn){
                columnData.add(column.asDate());
            }else if(column instanceof DoubleColumn){
                columnData.add(column.asDouble());
            }else if(column instanceof LongColumn){
                columnData.add(column.asLong());
            }else{
                columnData.add(column.asString());
            }
        });
        return columnData;
    }
}
