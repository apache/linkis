package com.webank.wedatasphere.linkis.engine.flink.executor;

import com.webank.wedatasphere.linkis.common.io.resultset.ResultSetWriter;
import com.webank.wedatasphere.linkis.engine.execute.EngineExecutorContext;
import com.webank.wedatasphere.linkis.engine.flink.client.sql.operation.result.ResultSet;
import com.webank.wedatasphere.linkis.engine.flink.client.sql.session.Session;
import com.webank.wedatasphere.linkis.engine.flink.common.ResultListener;
import com.webank.wedatasphere.linkis.storage.domain.Column;
import com.webank.wedatasphere.linkis.storage.resultset.ResultSetFactory$;
import com.webank.wedatasphere.linkis.storage.resultset.table.TableMetaData;
import com.webank.wedatasphere.linkis.storage.resultset.table.TableRecord;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.flink.api.common.JobID;
import org.mortbay.util.ajax.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FlinkResultListener implements ResultListener {

    private Logger LOG = LoggerFactory.getLogger(getClass());

    public EngineExecutorContext engineExecutorContext;

    public String jobid;

    public Session session;

    private boolean finished = false;

    private ResultSetWriter resultSetWriter;

    public FlinkResultListener(EngineExecutorContext engineExecutorContext){
        this.engineExecutorContext = engineExecutorContext;
        this.resultSetWriter = engineExecutorContext.createResultSetWriter(ResultSetFactory$.MODULE$.TABLE_TYPE());

    }


    @Override
    public void resultNotify()  {
        Optional<ResultSet> resultSetSelectOptional = session.getJobResult(JobID.fromHexString(jobid));
        ResultSet resultSetSelect = resultSetSelectOptional.get();
        LOG.info("resultSetSelect：" + JSON.toString(resultSetSelect));
        List<Column> columns = new ArrayList<>();
        resultSetSelect.getColumns().forEach(columnInfo -> {
            columns.add(new Column(columnInfo.getName(), null, null));
        });
        TableMetaData metaData = new TableMetaData(columns.toArray(new Column[columns.size()]));
        try {
            this.resultSetWriter.addMetaData(metaData);
        } catch (IOException e) {
            e.printStackTrace();
        }
        resultSetSelect.getData().forEach(row -> {
            List<String> rowList = new ArrayList<>();
            for (int i = 0; i < row.getArity(); ++i) {
                rowList.add(row.getField(i).toString());
            }
            try {
                resultSetWriter.addRecord(new TableRecord(rowList.toArray(new String[rowList.size()])));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        this.engineExecutorContext.sendResultSet(this.resultSetWriter);
        //IOUtils.closeQuietly(this.resultSetWriter);
        this.finished = true;
    }

    public String getJobid() {
        return jobid;
    }

    public void setJobid(String jobid) {
        this.jobid = jobid;
    }

    public Session getSession() {
        return session;
    }

    public void setSession(Session session) {
        this.session = session;
    }

    @Override
    public void error(int status) {

    }

    @Override
    public boolean isFinished() {
        return this.finished;
    }

    @Override
    public void progress(float progress) {

    }

    @Override
    public void message(List<String> message) {

    }

    @Override
    public void cancel() {
            this.finished =true;
        if(StringUtils.isNotBlank(this.jobid)) {
            this.session.cancelJob(JobID.fromHexString(this.jobid));
        }
    }
}
