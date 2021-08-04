package com.webank.wedatasphere.linkis.engineconnplugin.flink.context

import java.net.URL
import java.util
import java.util.Objects

import com.google.common.collect.Lists
import com.webank.wedatasphere.linkis.engineconnplugin.flink.client.config.Environment
import com.webank.wedatasphere.linkis.engineconnplugin.flink.client.factory.LinkisYarnClusterClientFactory
import org.apache.commons.lang.StringUtils
import org.apache.flink.configuration.{Configuration, DeploymentOptionsInternal, GlobalConfiguration}
import org.apache.flink.yarn.configuration.{YarnConfigOptions, YarnDeploymentTarget}

/**
  * Created by enjoyyin on 2021/4/14.
  */
class EnvironmentContext(defaultEnv: Environment,
                         yarnConfDir: String,
                         flinkConfDir: String,
                         flinkHome: String,
                         distJarPath: String,
                         flinkLibRemotePath: String,
                         dependencies: util.List[URL]) {

  private var providedLibDirs: util.List[String] = _
  private var shipDirs: util.List[String] = _

  private var flinkConfig: Configuration = _

  private var deploymentTarget: YarnDeploymentTarget = YarnDeploymentTarget.PER_JOB

  def this(defaultEnv: Environment, systemConfiguration: Configuration, yarnConfDir: String, flinkConfDir: String,
           flinkHome: String, distJarPath: String, flinkLibRemotePath: String, providedLibDirsArray: Array[String],
           shipDirsArray: Array[String], dependencies: util.List[URL]) {
    this(defaultEnv, yarnConfDir, flinkConfDir, flinkHome, distJarPath, flinkLibRemotePath, dependencies)
    //远程资源目录
    this.providedLibDirs = Lists.newArrayList(providedLibDirsArray.filter(StringUtils.isNotBlank): _*)
    //本地资源目录
    this.shipDirs = Lists.newArrayList(shipDirsArray.filter(StringUtils.isNotBlank): _*)
    //加载系统级别配置
    this.flinkConfig = GlobalConfiguration.loadConfiguration(this.flinkConfDir)
    if (null != systemConfiguration) this.flinkConfig.addAll(systemConfiguration)
    //设置 flink conf目录
    this.flinkConfig.set(DeploymentOptionsInternal.CONF_DIR, this.flinkConfDir)
    //设置 yarn conf目录
    this.flinkConfig.set(LinkisYarnClusterClientFactory.YARN_CONFIG_DIR, this.yarnConfDir)
    //设置 flink dist jar
    this.flinkConfig.set(YarnConfigOptions.FLINK_DIST_JAR, distJarPath)
  }

  def setDeploymentTarget(deploymentTarget: YarnDeploymentTarget): Unit = this.deploymentTarget = deploymentTarget

  def getDeploymentTarget: YarnDeploymentTarget = deploymentTarget

  def getProvidedLibDirs: util.List[String] = providedLibDirs

  def getShipDirs: util.List[String] = shipDirs

  def getYarnConfDir: String = yarnConfDir

  def getFlinkConfDir: String = flinkConfDir

  def getFlinkHome: String = flinkHome

  def getFlinkLibRemotePath: String = flinkLibRemotePath

  def getFlinkConfig: Configuration = flinkConfig

  def getDefaultEnv: Environment = defaultEnv

  def getDependencies: util.List[URL] = dependencies

  override def equals(o: Any): Boolean = o match {
    case context: EnvironmentContext =>
      if(this eq context) return true
      Objects.equals(defaultEnv, context.getDefaultEnv) &&
        Objects.equals(dependencies, context.getDependencies) &&
        Objects.equals(flinkConfig, context.flinkConfig)
    case _ => false
  }

  override def hashCode: Int = Objects.hash(defaultEnv, dependencies, flinkConfig)
}
