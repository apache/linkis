/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.linkis.engineconnplugin.sqoop.client;

import org.apache.linkis.engineconnplugin.sqoop.client.config.ParamsMapping;
import org.apache.linkis.engineconnplugin.sqoop.client.exception.JobClosableException;
import org.apache.linkis.engineconnplugin.sqoop.context.SqoopEnvConfiguration;
import org.apache.linkis.engineconnplugin.sqoop.context.SqoopParamsConfiguration;
import org.apache.linkis.protocol.engine.JobProgressInfo;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.mapred.TIPStatus;
import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.apache.sqoop.manager.SqlManager;
import org.apache.sqoop.manager.oracle.OraOopManagerFactory;
import org.apache.sqoop.util.LoggingUtils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import com.cloudera.sqoop.SqoopOptions;
import com.cloudera.sqoop.manager.DefaultManagerFactory;
import com.cloudera.sqoop.tool.SqoopTool;
import com.cloudera.sqoop.util.OptionsFileUtil;

import static org.apache.linkis.engineconnplugin.sqoop.client.errorcode.SqoopErrorCodeSummary.ERROR_IN_CLOSING;
import static org.apache.linkis.engineconnplugin.sqoop.client.errorcode.SqoopErrorCodeSummary.UNABLE_TO_CLOSE;

/**
 * Main entry-point for Sqoop Usage: hadoop jar (this_jar_name) com.cloudera.sqoop.Sqoop (options)
 * See the SqoopOptions class for options.
 */
public class Sqoop extends Configured implements Tool {

  public static final Log LOG = LogFactory.getLog(Sqoop.class.getName());
  public static volatile AtomicReference<Job> job = new AtomicReference<>();
  public static SqlManager sqlManager;
  public static final String[] DEFAULT_FACTORY_CLASS_NAMES_ARR = {
    OraOopManagerFactory.class.getName(), DefaultManagerFactory.class.getName(),
  };
  public static final String FACTORY_CLASS_NAMES_KEY = "sqoop.connection.factories";
  public static final String METRICS_RUN_TIME = "MetricsRunTime";
  private static Float progress = 0.0f;

  /** If this System property is set, always throw an exception, do not just exit with status 1. */
  public static final String SQOOP_RETHROW_PROPERTY = "sqoop.throwOnError";

  /** The option to specify an options file from which other options to the tool are read. */
  public static final String SQOOP_OPTIONS_FILE_SPECIFIER = "--options-file";

  static {
    Configuration.addDefaultResource("sqoop-site.xml");
  }

  private SqoopTool tool;
  private SqoopOptions options;
  private String[] childPrgmArgs;

  /**
   * Creates a new instance of Sqoop set to run the supplied SqoopTool with the default
   * configuration.
   *
   * @param tool the SqoopTool to run in the main body of Sqoop.
   */
  public Sqoop(SqoopTool tool) {
    this(tool, (Configuration) null);
  }

  /**
   * Creates a new instance of Sqoop set to run the supplied SqoopTool with the provided
   * configuration.
   *
   * @param tool the SqoopTool to run in the main body of Sqoop.
   * @param conf the Configuration to use (e.g., from ToolRunner).
   */
  public Sqoop(SqoopTool tool, Configuration conf) {
    this(tool, conf, new SqoopOptions());
  }

  /**
   * Creates a new instance of Sqoop set to run the supplied SqoopTool with the provided
   * configuration and SqoopOptions.
   *
   * @param tool the SqoopTool to run in the main body of Sqoop.
   * @param conf the Configuration to use (e.g., from ToolRunner).
   * @param opts the SqoopOptions which control the tool's parameters.
   */
  public Sqoop(SqoopTool tool, Configuration conf, SqoopOptions opts) {
    /*LOG.info("Running Sqoop version: " + new SqoopVersion().VERSION);*/

    if (null != conf) {
      setConf(conf);
    }

    this.options = opts;
    this.options.setConf(getConf());

    this.tool = tool;
  }

  /** @return the SqoopOptions used in this Sqoop instance. */
  public SqoopOptions getOptions() {
    return this.options;
  }

  /** @return the SqoopTool used in this Sqoop instance. */
  public SqoopTool getTool() {
    return this.tool;
  }

  @Override
  /** Actual main entry-point for the program */
  public int run(String[] args) {
    if (options.getConf() == null) {
      options.setConf(getConf());
    }
    options.getConf().setStrings(FACTORY_CLASS_NAMES_KEY, DEFAULT_FACTORY_CLASS_NAMES_ARR);
    try {
      options = tool.parseArguments(args, null, options, false);
      tool.appendArgs(this.childPrgmArgs);
      tool.validateOptions(options);
      if (options.getVerbose()) {
        LoggingUtils.setDebugLevel();
      }
    } catch (Exception e) {
      LOG.error(e.getMessage(), e);
      System.err.println(e.getMessage());
      return 1;
    }
    return tool.run(options);
  }

  /**
   * SqoopTools sometimes pass arguments to a child program (e.g., mysqldump). Users can specify
   * additional args to these programs by preceeding the additional arguments with a standalone
   * '--'; but ToolRunner/GenericOptionsParser will cull out this argument. We remove the
   * child-program arguments in advance, and store them to be readded later.
   *
   * @param argv the argv in to the SqoopTool
   * @return the argv with a "--" and any subsequent arguments removed.
   */
  private String[] stashChildPrgmArgs(String[] argv) {
    for (int i = 0; i < argv.length; i++) {
      if ("--".equals(argv[i])) {
        this.childPrgmArgs = Arrays.copyOfRange(argv, i, argv.length);
        return Arrays.copyOfRange(argv, 0, i);
      }
    }

    // Didn't find child-program arguments.
    return argv;
  }

  /**
   * Given a Sqoop object and a set of arguments to deliver to its embedded SqoopTool, run the tool,
   * wrapping the call to ToolRunner. This entry-point is preferred to ToolRunner.run() because it
   * has a chance to stash child program arguments before GenericOptionsParser would remove them.
   */
  public static int runSqoop(Sqoop sqoop, String[] args) {
    String[] toolArgs = sqoop.stashChildPrgmArgs(args);
    try {
      return ToolRunner.run(sqoop.getConf(), sqoop, toolArgs);
    } catch (Exception e) {
      LOG.error("Got exception running Sqoop: " + e.toString());
      e.printStackTrace();
      rethrowIfRequired(toolArgs, e);
      return 1;
    }
  }

  public static void rethrowIfRequired(String[] toolArgs, Exception ex) {
    final RuntimeException exceptionToThrow;
    if (ex instanceof RuntimeException) {
      exceptionToThrow = (RuntimeException) ex;
    } else {
      exceptionToThrow = new RuntimeException(ex);
    }

    throw exceptionToThrow;
  }

  /**
   * Entry-point that parses the correct SqoopTool to use from the args, but does not call
   * System.exit() as main() will.
   */
  public static int runTool(Map<String, String> argsMap, Configuration conf) {

    // Expand the options
    String[] expandedArgs = null;
    try {
      String[] flatArgs = convertParamsMapToAarray(argsMap, conf);
      expandedArgs = OptionsFileUtil.expandArguments(flatArgs);
    } catch (Exception ex) {
      LOG.error("Error while expanding arguments", ex);
      System.err.println(ex.getMessage());
      System.err.println("Try 'sqoop help' for usage.");
      return 1;
    }

    String toolName = expandedArgs[0];
    Configuration pluginConf = SqoopTool.loadPlugins(conf);
    SqoopTool tool = SqoopTool.getTool(toolName);
    if (null == tool) {
      System.err.println("No such sqoop tool: " + toolName + ". See 'sqoop help'.");
      return 1;
    }

    Sqoop sqoop = new Sqoop(tool, pluginConf);
    return runSqoop(sqoop, Arrays.copyOfRange(expandedArgs, 1, expandedArgs.length));
  }

  private static String[] convertParamsMapToAarray(
      Map<String, String> paramsMap, Configuration conf) throws Exception {
    List<String> paramsList = new ArrayList<>();

    for (Map.Entry<String, String> entry : paramsMap.entrySet()) {
      if (StringUtils.isNotBlank(entry.getKey())) {
        String key = entry.getKey().toLowerCase();
        if (key.equals(SqoopParamsConfiguration.SQOOP_PARAM_MODE().getValue())) {
          paramsList.add(0, entry.getValue());
          continue;
        }
        if (key.startsWith(SqoopParamsConfiguration.SQOOP_PARAM_ENV_PREFIX().getValue())) {
          key =
              key.substring(SqoopParamsConfiguration.SQOOP_PARAM_ENV_PREFIX().getValue().length());
          conf.set(key, entry.getValue());
          continue;
        }
        String conKey = ParamsMapping.mapping.get(key);
        if (conKey != null) {
          if (entry.getValue() != null && entry.getValue().length() != 0) {
            paramsList.add(conKey);
            paramsList.add(entry.getValue());
          } else {
            paramsList.add(conKey);
          }
        } else {
          // Ignore the unrecognized params
          LOG.warn("The Key " + entry.getKey() + " Is Not Supported");
        }
      }
    }
    return paramsList.toArray(new String[0]);
  }

  /**
   * Entry-point that parses the correct SqoopTool to use from the args, but does not call
   * System.exit() as main() will.
   */
  public static int runTool(Map<String, String> params) {
    Configuration conf = new Configuration();
    try {
      for (String fileName : SqoopEnvConfiguration.SQOOP_HADOOP_SITE_FILE().getValue().split(";")) {
        File resourceFile = Paths.get(fileName).toFile();
        if (resourceFile.exists()) {
          LOG.info("Append resource: [" + resourceFile.getPath() + "] to configuration");
          conf.addResource(resourceFile.toURI().toURL());
        }
      }

    } catch (MalformedURLException e) {
      e.printStackTrace();
      System.exit(1);
    }
    return runTool(params, conf);
  }

  public static int main(Map<String, String> code) {
    return runTool(code);
  }

  /**
   * Close method
   *
   * @throws JobClosableException
   */
  public static void close() throws JobClosableException {
    Job runnableJob = job.get();
    try {
      if (Objects.nonNull(runnableJob)) {
        runnableJob.killJob();
      }
      if (sqlManager != null && sqlManager.getConnection() != null) {
        sqlManager.getConnection().close();
      }
    } catch (IllegalStateException se) {
      if (isJobReady(runnableJob)) {
        LOG.warn(
            "Unable to close the mapReduce job, it seems that the job isn't connected to the cluster");
      } else if (Objects.nonNull(runnableJob)) {
        String cluster = "UNKNOWN";
        try {
          cluster = runnableJob.getCluster().getFileSystem().getCanonicalServiceName();
        } catch (Exception e) {
          // Ignore
        }
        throw new JobClosableException(UNABLE_TO_CLOSE.getErrorDesc() + "[" + cluster + "]", se);
      }
    } catch (IOException | SQLException e) {
      throw new JobClosableException(ERROR_IN_CLOSING.getErrorDesc(), e);
    }
  }

  /**
   * Get application id
   *
   * @return string value
   */
  public static String getApplicationId() {
    String applicationId = "";
    try {
      Job runnableJob = job.get();
      if (Objects.nonNull(runnableJob)) {
        JobID jobId = runnableJob.getJobID();
        if (Objects.nonNull(jobId)) {
          applicationId = jobId.toString();
        }
      }
    } catch (Exception e) {
      // Not throw exception
      LOG.error("GetApplicationId in sqoop Error", e);
    }
    return applicationId;
  }

  /**
   * Get application url
   *
   * @return url
   */
  public static String getApplicationURL() {
    String applicationUrl = "";
    Job runnableJob = job.get();
    try {
      if (Objects.nonNull(runnableJob)) {
        return runnableJob.getTrackingURL();
      }
    } catch (Exception e) {
      if (e instanceof IllegalStateException && !isJobReady(runnableJob)) {
        LOG.trace("The mapReduce job is not ready, wait for the job status to be Running");
      } else {
        LOG.error("GetApplicationURL in sqoop Error", e);
      }
    }
    return applicationUrl;
  }

  /**
   * Get progress value
   *
   * @return float value
   */
  public static Float progress() {
    Job runnableJob = job.get();
    try {
      if (Objects.nonNull(runnableJob)) {
        // Count by two paragraphs
        progress = (runnableJob.mapProgress() + runnableJob.reduceProgress()) / 2.0f;
      }
    } catch (Exception e) {
      if (e instanceof IllegalStateException && !isJobReady(runnableJob)) {
        LOG.trace("The mapReduce job is not ready, the value of progress is 0.0 always");
      } else {
        LOG.error("Get progress in sqoop Error", e);
      }
    }
    return progress;
  }

  /**
   * Get progress info
   *
   * @return info
   */
  public static JobProgressInfo getProgressInfo() {
    Job runnableJob = job.get();
    try {
      if (Objects.nonNull(runnableJob)) {
        AtomicInteger totalTasks = new AtomicInteger();
        AtomicInteger failedTasks = new AtomicInteger();
        AtomicInteger runTasks = new AtomicInteger();
        AtomicInteger successTasks = new AtomicInteger();
        TaskType[] analyzeTypes = new TaskType[] {TaskType.MAP, TaskType.REDUCE};
        for (TaskType taskType : analyzeTypes) {
          TaskReport[] taskReports = runnableJob.getTaskReports(taskType);
          Optional.ofNullable(taskReports)
              .ifPresent(
                  reports -> {
                    totalTasks.addAndGet(reports.length);
                    for (TaskReport report : reports) {
                      TIPStatus tipStatus = report.getCurrentStatus();
                      switch (tipStatus) {
                        case FAILED:
                        case KILLED:
                          failedTasks.getAndIncrement();
                          break;
                        case PENDING:
                        case RUNNING:
                          runTasks.getAndIncrement();
                          break;
                        case COMPLETE:
                          successTasks.getAndIncrement();
                          break;
                        default:
                      }
                    }
                  });
        }
        return new JobProgressInfo(
            getApplicationId(),
            totalTasks.get(),
            runTasks.get(),
            failedTasks.get(),
            successTasks.get());
      }
    } catch (Exception e) {
      if (e instanceof IllegalStateException && !isJobReady(runnableJob)) {
        LOG.trace("The mapReduce job is not ready, the value of progressInfo is always empty");
      } else {
        LOG.error("Get progress info in sqoop Error", e);
      }
    }
    return new JobProgressInfo(getApplicationId(), 0, 0, 0, 0);
  }

  /**
   * Get metrics
   *
   * @return metrics map
   */
  public static Map<String, Object> getMetrics() {
    Job runnableJob = job.get();
    // Actual the counter map
    Map<String, Object> metricsMap = new HashMap<>();
    try {
      if (Objects.nonNull(runnableJob)) {
        Counters counters = runnableJob.getCounters();
        counters.forEach(
            group ->
                metricsMap.computeIfAbsent(
                    group.getName(),
                    (groupName) -> {
                      Map<String, Object> map = new HashMap<>();
                      group.forEach(counter -> map.put(counter.getName(), counter.getValue()));
                      return map;
                    }));
        long startTime = runnableJob.getStartTime();
        long endTime =
            runnableJob.getFinishTime() > 0
                ? runnableJob.getFinishTime()
                : System.currentTimeMillis();
        // Analyze the run time
        metricsMap.put(METRICS_RUN_TIME, startTime > 0 ? endTime - startTime : 0);
      }
    } catch (Exception e) {
      if (e instanceof IllegalStateException && !isJobReady(runnableJob)) {
        LOG.trace("The mapReduce job is not ready, the value of metricsMap is always empty");
      } else {
        LOG.error("Get metrics info in sqoop Error", e);
      }
    }
    return metricsMap;
  }

  /**
   * Get diagnosis
   *
   * @return
   */
  public static Map<String, Object> getDiagnosis() {
    Job runnableJob = job.get();
    Map<String, Object> diagnosis = new HashMap<>();
    try {
      if (Objects.nonNull(runnableJob)) {
        TaskType[] analyzeTypes = new TaskType[] {TaskType.MAP, TaskType.REDUCE};
        List<TaskReport> listReports = new ArrayList<>();
        for (TaskType taskType : analyzeTypes) {
          listReports.addAll(Arrays.asList(runnableJob.getTaskReports(taskType)));
        }
        listReports.forEach(report -> diagnosis.put(report.getTaskId(), report.getDiagnostics()));
      }
    } catch (Exception e) {
      if (e instanceof IllegalStateException && !isJobReady(runnableJob)) {
        LOG.trace("The mapReduce job is not ready, the value of diagnosis is always empty");
      } else {
        LOG.error("Get diagnosis info in sqoop Error", e);
      }
    }
    return diagnosis;
  }

  /**
   * If the job is ready
   *
   * @param runnableJob job
   * @return
   */
  private static boolean isJobReady(Job runnableJob) {
    boolean ready = false;
    try {
      Field stateField = Job.class.getDeclaredField("state");
      stateField.setAccessible(true);
      Job.JobState state = (Job.JobState) stateField.get(runnableJob);
      ready = state.equals(Job.JobState.RUNNING);
    } catch (NoSuchFieldException | IllegalAccessException e) {
      // Ignore
    }
    return ready;
  }
}
