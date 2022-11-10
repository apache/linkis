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

package org.apache.sqoop.mapreduce;

import org.apache.linkis.engineconnplugin.sqoop.client.Sqoop;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.InputFormat;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.OutputFormat;
import org.apache.hadoop.util.StringUtils;
import org.apache.sqoop.config.ConfigurationConstants;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.cloudera.sqoop.SqoopOptions;
import com.cloudera.sqoop.config.ConfigurationHelper;
import com.cloudera.sqoop.manager.ConnManager;
import com.cloudera.sqoop.tool.SqoopTool;
import com.cloudera.sqoop.util.ClassLoaderStack;
import com.cloudera.sqoop.util.Jars;

/**
 * Base class for configuring and running a MapReduce job. Allows dependency injection, etc, for
 * easy customization of import job types.
 */
public class JobBase {

  public static final Log LOG = LogFactory.getLog(JobBase.class.getName());

  public static final String SERIALIZE_SQOOPOPTIONS = "sqoop.jobbase.serialize.sqoopoptions";
  public static final boolean SERIALIZE_SQOOPOPTIONS_DEFAULT = false;
  public static final String HADOOP_MAP_TASK_MAX_ATTEMTPS = "mapreduce.map.maxattempts";
  public static final String HADOOP_REDUCE_TASK_MAX_ATTEMTPS = "mapreduce.reduce.maxattempts";

  protected SqoopOptions options;
  protected Class<? extends Mapper> mapperClass;
  protected Class<? extends InputFormat> inputFormatClass;
  protected Class<? extends OutputFormat> outputFormatClass;

  private Job mrJob;

  private ClassLoader prevClassLoader = null;
  protected final boolean isHCatJob;

  public static final String PROPERTY_VERBOSE = "sqoop.verbose";

  public JobBase() {
    this(null);
  }

  public JobBase(final SqoopOptions opts) {
    this(opts, null, null, null);
  }

  public JobBase(
      final SqoopOptions opts,
      final Class<? extends Mapper> mapperClass,
      final Class<? extends InputFormat> inputFormatClass,
      final Class<? extends OutputFormat> outputFormatClass) {
    System.out.println(SqoopOptions.class.getClassLoader());
    this.options = opts;
    this.mapperClass = mapperClass;
    this.inputFormatClass = inputFormatClass;
    this.outputFormatClass = outputFormatClass;
    isHCatJob = options.getHCatTableName() != null;
  }

  /** @return the mapper class to use for the job. */
  protected Class<? extends Mapper> getMapperClass() throws ClassNotFoundException {
    return this.mapperClass;
  }

  /** @return the inputformat class to use for the job. */
  protected Class<? extends InputFormat> getInputFormatClass() throws ClassNotFoundException {
    return this.inputFormatClass;
  }

  /** @return the outputformat class to use for the job. */
  protected Class<? extends OutputFormat> getOutputFormatClass() throws ClassNotFoundException {
    return this.outputFormatClass;
  }

  /** Set the OutputFormat class to use for this job. */
  public void setOutputFormatClass(Class<? extends OutputFormat> cls) {
    this.outputFormatClass = cls;
  }

  /** Set the InputFormat class to use for this job. */
  public void setInputFormatClass(Class<? extends InputFormat> cls) {
    this.inputFormatClass = cls;
  }

  /** Set the Mapper class to use for this job. */
  public void setMapperClass(Class<? extends Mapper> cls) {
    this.mapperClass = cls;
  }

  /** Set the SqoopOptions configuring this job. */
  public void setOptions(SqoopOptions opts) {
    this.options = opts;
  }

  /**
   * Put jar files required by Sqoop into the DistributedCache.
   *
   * @param job the Job being submitted.
   * @param mgr the ConnManager to use.
   */
  protected void cacheJars(Job job, ConnManager mgr) throws IOException {
    if (options.isSkipDistCache()) {
      LOG.info("Not adding sqoop jars to distributed cache as requested");
      return;
    }

    Configuration conf = job.getConfiguration();
    FileSystem fs = FileSystem.getLocal(conf);
    Set<String> localUrls = new HashSet<String>();

    addToCache(Jars.getSqoopJarPath(), fs, localUrls);
    if (null != mgr) {
      addToCache(Jars.getDriverClassJar(mgr), fs, localUrls);
      addToCache(Jars.getJarPathForClass(mgr.getClass()), fs, localUrls);
    }

    SqoopTool tool = this.options.getActiveSqoopTool();
    if (null != tool) {
      // Make sure the jar for the tool itself is on the classpath. (In case
      // this is a third-party plugin tool.)
      addToCache(Jars.getJarPathForClass(tool.getClass()), fs, localUrls);
      List<String> toolDeps = tool.getDependencyJars();
      if (null != toolDeps) {
        for (String depFile : toolDeps) {
          addToCache(depFile, fs, localUrls);
        }
      }
    }

    // If the user specified a particular jar file name,

    // Add anything in $SQOOP_HOME/lib, if this is set.
    String sqoopHome = System.getenv("SQOOP_HOME");
    if (null != sqoopHome) {
      File sqoopHomeFile = new File(sqoopHome);
      File sqoopLibFile = new File(sqoopHomeFile, "lib");
      if (sqoopLibFile.exists()) {
        addDirToCache(sqoopLibFile, fs, localUrls);
      }
    } else {
      LOG.warn("SQOOP_HOME is unset. May not be able to find " + "all job dependencies.");
    }

    // If the user run import into hive as Parquet file,
    // Add anything in $HIVE_HOME/lib.
    if (options.doHiveImport()
        && (options.getFileLayout() == SqoopOptions.FileLayout.ParquetFile)) {
      String hiveHome = options.getHiveHome();
      if (null != hiveHome) {
        File hiveHomeFile = new File(hiveHome);
        File hiveLibFile = new File(hiveHomeFile, "lib");
        if (hiveLibFile.exists()) {
          addDirToCache(hiveLibFile, fs, localUrls);
        }
      } else {
        LOG.warn("HIVE_HOME is unset. Cannot add hive libs as dependencies.");
      }
    }

    String tmpjars = conf.get(ConfigurationConstants.MAPRED_DISTCACHE_CONF_PARAM);
    StringBuilder sb = new StringBuilder();

    // If we didn't put anything in our set, then there's nothing to cache.
    if (localUrls.isEmpty() && (org.apache.commons.lang3.StringUtils.isEmpty(tmpjars))) {
      return;
    }

    if (null != tmpjars) {
      String[] tmpjarsElements = tmpjars.split(",");
      for (String jarElement : tmpjarsElements) {
        if (jarElement.isEmpty()) {
          warn("Empty input is invalid and was removed from tmpjars.");
        } else {
          sb.append(jarElement);
          sb.append(",");
        }
      }
    }

    int lastComma = sb.lastIndexOf(",");
    if (localUrls.isEmpty() && lastComma >= 0) {
      sb.deleteCharAt(lastComma);
    }

    // Add these to the 'tmpjars' array, which the MR JobSubmitter
    // will upload to HDFS and put in the DistributedCache libjars.
    sb.append(StringUtils.arrayToString(localUrls.toArray(new String[0])));
    conf.set(ConfigurationConstants.MAPRED_DISTCACHE_CONF_PARAM, sb.toString());
  }

  protected void warn(String message) {
    LOG.warn(message);
  }

  private void addToCache(String file, FileSystem fs, Set<String> localUrls) {
    if (null == file) {
      return;
    }

    Path p = new Path(file);
    String qualified = p.makeQualified(fs).toString();
    LOG.debug("Adding to job classpath: " + qualified);
    localUrls.add(qualified);
  }

  /** Add the .jar elements of a directory to the DCache classpath, nonrecursively. */
  private void addDirToCache(File dir, FileSystem fs, Set<String> localUrls) {
    if (null == dir) {
      return;
    }

    for (File libfile : dir.listFiles()) {
      if (libfile.exists() && !libfile.isDirectory() && libfile.getName().endsWith("jar")) {
        addToCache(libfile.toString(), fs, localUrls);
      }
    }
  }

  /** If jars must be loaded into the local environment, do so here. */
  protected void loadJars(Configuration conf, String ormJarFile, String tableClassName)
      throws IOException {

    boolean isLocal =
        "local".equals(conf.get("mapreduce.jobtracker.address"))
            || "local".equals(conf.get("mapred.job.tracker"));
    if (isLocal) {
      // If we're using the LocalJobRunner, then instead of using the compiled
      // jar file as the job source, we're running in the current thread. Push
      // on another classloader that loads from that jar in addition to
      // everything currently on the classpath.
      this.prevClassLoader = ClassLoaderStack.addJarFile(ormJarFile, tableClassName);
    }
  }

  /** If any classloader was invoked by loadJars, free it here. */
  protected void unloadJars() {
    if (null != this.prevClassLoader) {
      // unload the special classloader for this jar.
      ClassLoaderStack.setCurrentClassLoader(this.prevClassLoader);
    }
  }

  /** Configure the inputformat to use for the job. */
  protected void configureInputFormat(
      Job job, String tableName, String tableClassName, String splitByCol)
      throws ClassNotFoundException, IOException {
    // TODO: 'splitByCol' is import-job specific; lift it out of this API.
    Class<? extends InputFormat> ifClass = getInputFormatClass();
    LOG.debug("Using InputFormat: " + ifClass);
    job.setInputFormatClass(ifClass);
  }

  /** Configure the output format to use for the job. */
  protected void configureOutputFormat(Job job, String tableName, String tableClassName)
      throws ClassNotFoundException, IOException {
    Class<? extends OutputFormat> ofClass = getOutputFormatClass();
    LOG.debug("Using OutputFormat: " + ofClass);
    job.setOutputFormatClass(ofClass);
  }

  /**
   * Set the mapper class implementation to use in the job, as well as any related configuration
   * (e.g., map output types).
   */
  protected void configureMapper(Job job, String tableName, String tableClassName)
      throws ClassNotFoundException, IOException {
    job.setMapperClass(getMapperClass());
  }

  /**
   * Configure the number of map/reduce tasks to use in the job, returning the number of map tasks
   * for backward compatibility.
   */
  protected int configureNumTasks(Job job) throws IOException {
    int numMapTasks = configureNumMapTasks(job);
    configureNumReduceTasks(job);
    return numMapTasks;
  }

  /** Configure the number of map tasks to use in the job. */
  protected int configureNumMapTasks(Job job) throws IOException {
    int numMapTasks = options.getNumMappers();
    if (numMapTasks < 1) {
      numMapTasks = SqoopOptions.DEFAULT_NUM_MAPPERS;
      LOG.warn("Invalid mapper count; using " + numMapTasks + " mappers.");
    }
    ConfigurationHelper.setJobNumMaps(job, numMapTasks);
    return numMapTasks;
  }

  /** Configure the number of reduce tasks to use in the job. */
  protected int configureNumReduceTasks(Job job) throws IOException {
    job.setNumReduceTasks(0);
    return 0;
  }

  /** Set the main job that will be run. */
  protected void setJob(Job job) {
    LOG.info("Customize JobBase Set The Job");
    mrJob = job;
    Sqoop.job.set(job);
  }

  /** @return the main MapReduce job that is being run, or null if no job has started. */
  public Job getJob() {
    return mrJob;
  }

  /**
   * Create new Job object in unified way for all types of jobs.
   *
   * @param configuration Hadoop configuration that should be used
   * @return New job object, created object won't be persisted in the instance
   */
  public Job createJob(Configuration configuration) throws IOException {
    // Put the SqoopOptions into job if requested
    if (configuration.getBoolean(SERIALIZE_SQOOPOPTIONS, SERIALIZE_SQOOPOPTIONS_DEFAULT)) {
      putSqoopOptionsToConfiguration(options, configuration);
    }

    return new Job(configuration);
  }

  /**
   * Iterates over serialized form of SqoopOptions and put them into Configuration object.
   *
   * @param opts SqoopOptions that should be serialized
   * @param configuration Target configuration object
   */
  public void putSqoopOptionsToConfiguration(SqoopOptions opts, Configuration configuration) {
    for (Map.Entry<Object, Object> e : opts.writeProperties().entrySet()) {
      String key = (String) e.getKey();
      String value = (String) e.getValue();

      // We don't need to do if(value is empty) because that is already done
      // for us by the SqoopOptions.writeProperties() method.
      configuration.set("sqoop.opt." + key, value);
    }
  }

  /** Actually run the MapReduce job. */
  protected boolean runJob(Job job)
      throws ClassNotFoundException, IOException, InterruptedException {
    return job.waitForCompletion(true);
  }

  /**
   * Display a notice on the log that the current MapReduce job has been retired, and thus Counters
   * are unavailable.
   *
   * @param log the Log to display the info to.
   */
  protected void displayRetiredJobNotice(Log log) {
    log.info("The MapReduce job has already been retired. Performance");
    log.info("counters are unavailable. To get this information, ");
    log.info("you will need to enable the completed job store on ");
    log.info("the jobtracker with:");
    log.info("mapreduce.jobtracker.persist.jobstatus.active = true");
    log.info("mapreduce.jobtracker.persist.jobstatus.hours = 1");
    log.info("A jobtracker restart is required for these settings");
    log.info("to take effect.");
  }

  /**
   * Save interesting options to constructed job. Goal here is to propagate some of them to the job
   * itself, so that they can be easily accessed. We're propagating only interesting global options
   * (like verbose flag).
   *
   * @param job Destination job to save options
   */
  protected void propagateOptionsToJob(Job job) {
    Configuration configuration = job.getConfiguration();

    // So far, propagate only verbose flag
    configuration.setBoolean(PROPERTY_VERBOSE, options.getVerbose());
  }
}
