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

package org.apache.linkis.monitor.scheduled;

import org.apache.linkis.monitor.config.MonitorConfig;
import org.apache.linkis.monitor.until.ThreadUtils;
import org.apache.linkis.monitor.utils.log.LogUtils;

import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;

/**
 * 诊断日志清理定时任务
 *
 * <p>功能：每日凌晨2点自动清理超过保留期的诊断日志文件
 *
 * <p>配置：使用MonitorConfig配置类管理所有配置参数
 *
 * <p>日志路径：${linkis.log.dir}/task/
 *
 * <p>日志目录结构：
 *
 * <pre>
 * ${linkis.log.dir}/task/
 * ├── {job_id}/              # 诊断任务ID目录
 * │   └── engineconn_{service}.log    # 引擎诊断日志
 * └── json/                 # JSON格式诊断结果
 *     └── {job_id}_detail.json       # 诊断明细JSON
 * </pre>
 *
 * <p>清理规则： - 清理 task/ 目录下所有纯数字命名的子目录及其内容（{job_id}/） - 清理 task/json/ 目录下 {job_id}_detail.json 文件 -
 * 按文件/目录修改时间判断是否过期 - 每次执行最多清理指定数量（避免性能影响）
 */
@Component
@PropertySource(value = "classpath:linkis-et-monitor.properties", encoding = "UTF-8")
public class DiagnosisLogClear {

  private static final Logger logger = LogUtils.stdOutLogger();

  /** JSON子目录名称 */
  private static final String JSON_SUBDIR = "json";

  /** job_id目录（纯数字）的正则表达式 */
  private static final String JOB_ID_DIR_PATTERN = "^\\d+$";

  /** JSON文件后缀 */
  private static final String JSON_FILE_SUFFIX = "_detail.json";

  /**
   * 定时清理诊断日志
   *
   * <p>Cron表达式：默认每日凌晨2点执行
   */
  @Scheduled(cron = "${linkis.monitor.diagnosis.log.clear.cron:0 0 2 * * ?}")
  public void clearDiagnosisLogs() {
    boolean diagnosisLogEnabled = MonitorConfig.DIAGNOSIS_LOG_ENABLED.getValue();
    String diagnosisLogPath = MonitorConfig.DIAGNOSIS_LOG_PATH.getValue();
    int retentionDays = MonitorConfig.DIAGNOSIS_LOG_RETENTION_DAYS.getValue();
    int maxDeletePerRun = MonitorConfig.DIAGNOSIS_LOG_MAX_DELETE_PER_RUN.getValue();

    if (!diagnosisLogEnabled) {
      logger.info("Diagnosis log cleanup is disabled by config, skip execution");
      return;
    }

    logger.info(
        "Start to clear diagnosis logs, path: {}, retention days: {}, max delete per run: {}",
        diagnosisLogPath,
        retentionDays,
        maxDeletePerRun);

    try {
      clearExpiredDiagnosisLogs(diagnosisLogPath, retentionDays, maxDeletePerRun);
      logger.info("Start to clear_history_task_diagnosis shell");
      List<String> cmdlist = new ArrayList<>();
      cmdlist.add("sh");
      cmdlist.add(MonitorConfig.shellPath + "clear_history_task_diagnosis.sh");
      cmdlist.add(String.valueOf(MonitorConfig.DIAGNOSIS_LOG_RETENTION_DAYS.getValue()));
      logger.info("clear_history_task_diagnosis  shell command {}", cmdlist);
      String exec = ThreadUtils.run(cmdlist, "clear_history_task_diagnosis.sh");
      logger.info("shell log  {}", exec);
      logger.info("End to clear_history_task_diagnosis shell ");

    } catch (Exception e) {
      logger.error("Error occurred while clearing diagnosis logs: {}", e.getMessage(), e);
    }
  }

  /**
   * 扫描并删除过期的诊断日志文件
   *
   * @param logPath 日志路径
   * @param retentionDays 保留天数
   * @param maxDeletePerRun 单次最大删除数量
   * @throws IOException 文件操作异常
   */
  private void clearExpiredDiagnosisLogs(String logPath, int retentionDays, int maxDeletePerRun)
      throws IOException {
    Path path = Paths.get(logPath);

    // 检查日志目录是否存在
    if (!Files.exists(path)) {
      logger.warn("Diagnosis log path does not exist: {}", logPath);
      return;
    }

    // 检查是否是目录
    if (!Files.isDirectory(path)) {
      logger.warn("Diagnosis log path is not a directory: {}", logPath);
      return;
    }

    // 计算过期时间点
    Instant cutoffTime = Instant.now().minus(retentionDays, ChronoUnit.DAYS);

    // 统计变量
    final int[] deletedCount = {0};
    final long[] freedSpace = {0};

    // 遍历task目录下的所有子目录和文件
    try (java.util.stream.Stream<Path> children = Files.list(path)) {
      for (Path child : children.toArray(Path[]::new)) {
        // 检查是否达到最大删除数量限制
        if (maxDeletePerRun > 0 && deletedCount[0] >= maxDeletePerRun) {
          logger.warn("Reached max delete limit: {}, stopping cleanup", maxDeletePerRun);
          break;
        }

        try {
          if (Files.isDirectory(child)) {
            // 处理子目录
            String dirName = child.getFileName().toString();
            if (isJobIdDirectory(dirName)) {
              // 处理job_id目录：整体删除
              deleteExpiredJobIdDirectory(
                  child, cutoffTime, deletedCount, freedSpace, maxDeletePerRun);
            } else if (JSON_SUBDIR.equals(dirName)) {
              // 处理json目录：清理 Detail JSON 文件
              deleteExpiredJsonFiles(child, cutoffTime, deletedCount, freedSpace, maxDeletePerRun);
            }
            // 其他目录跳过
          }
        } catch (Exception e) {
          logger.error("Failed to process {}: {}", child, e.getMessage());
        }
      }
    }

    logClearResult(deletedCount[0], freedSpace[0]);
  }

  /**
   * 判断目录名是否是job_id（纯数字）
   *
   * @param dirName 目录名
   * @return true if job_id directory
   */
  private boolean isJobIdDirectory(String dirName) {
    return dirName.matches(JOB_ID_DIR_PATTERN);
  }

  /**
   * 删除过期的job_id目录及其内容
   *
   * @param dirPath 目录路径
   * @param cutoffTime 过期时间点
   * @param deletedCount 删除计数
   * @param freedSpace 释放空间
   * @param maxDeletePerRun 单次最大删除数量
   * @throws IOException 文件操作异常
   */
  private void deleteExpiredJobIdDirectory(
      Path dirPath, Instant cutoffTime, int[] deletedCount, long[] freedSpace, int maxDeletePerRun)
      throws IOException {
    // 检查是否达到最大删除数量限制
    if (maxDeletePerRun > 0 && deletedCount[0] >= maxDeletePerRun) {
      return;
    }

    // 获取目录修改时间
    BasicFileAttributes attrs = Files.readAttributes(dirPath, BasicFileAttributes.class);
    if (attrs.lastModifiedTime().toInstant().isBefore(cutoffTime)) {
      // 计算目录大小（递归）
      long dirSize = calculateDirectorySize(dirPath);
      // 删除整个目录
      deleteDirectoryRecursively(dirPath);
      deletedCount[0]++;
      freedSpace[0] += dirSize;
      logger.debug("Deleted expired diagnosis directory: {}", dirPath);
    }
  }

  /**
   * 删除目录及其所有内容（递归）
   *
   * @param dirPath 目录路径
   * @throws IOException 文件操作异常
   */
  private void deleteDirectoryRecursively(Path dirPath) throws IOException {
    Files.walkFileTree(
        dirPath,
        new SimpleFileVisitor<Path>() {
          @Override
          public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
              throws IOException {
            Files.delete(file);
            return FileVisitResult.CONTINUE;
          }

          @Override
          public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
            if (exc == null) {
              Files.delete(dir);
              return FileVisitResult.CONTINUE;
            } else {
              throw exc;
            }
          }
        });
  }

  /**
   * 删除json目录下过期的detail JSON文件
   *
   * @param jsonDirPath json目录路径
   * @param cutoffTime 过期时间点
   * @param deletedCount 删除计数
   * @param freedSpace 释放空间
   * @param maxDeletePerRun 单次最大删除数量
   * @throws IOException 文件操作异常
   */
  private void deleteExpiredJsonFiles(
      Path jsonDirPath,
      Instant cutoffTime,
      int[] deletedCount,
      long[] freedSpace,
      int maxDeletePerRun)
      throws IOException {
    // 检查json目录是否存在
    if (!Files.exists(jsonDirPath)) {
      return;
    }

    // 遍历json目录下的所有文件
    try (java.util.stream.Stream<Path> files = Files.list(jsonDirPath)) {
      for (Path file : files.toArray(Path[]::new)) {
        // 检查是否达到最大删除数量限制
        if (maxDeletePerRun > 0 && deletedCount[0] >= maxDeletePerRun) {
          break;
        }

        try {
          if (!Files.isDirectory(file)) {
            String fileName = file.getFileName().toString();
            // 检查是否是detail JSON文件：{job_id}_detail.json
            if (isDetailJsonFile(fileName)) {
              BasicFileAttributes attrs = Files.readAttributes(file, BasicFileAttributes.class);
              if (attrs.lastModifiedTime().toInstant().isBefore(cutoffTime)) {
                long fileSize = Files.size(file);
                Files.delete(file);
                deletedCount[0]++;
                freedSpace[0] += fileSize;
                logger.debug("Deleted expired detail JSON: {}", file);
              }
            }
          }
        } catch (Exception e) {
          logger.error("Failed to process JSON file {}: {}", file, e.getMessage());
        }
      }
    }
  }

  /**
   * 判断文件是否是detail JSON文件
   *
   * <p>命名规则：{job_id}_detail.json，其中job_id是纯数字
   *
   * @param fileName 文件名
   * @return true if detail JSON file
   */
  private boolean isDetailJsonFile(String fileName) {
    if (!fileName.endsWith(JSON_FILE_SUFFIX)) {
      return false;
    }
    // 提取job_id部分（去掉后缀后的纯数字检查）
    String jobIdPart = fileName.substring(0, fileName.length() - JSON_FILE_SUFFIX.length());
    return jobIdPart.matches(JOB_ID_DIR_PATTERN);
  }

  /**
   * 计算目录大小（递归）
   *
   * @param dirPath 目录路径
   * @return 目录大小（字节）
   * @throws IOException 文件操作异常
   */
  private long calculateDirectorySize(Path dirPath) throws IOException {
    final long[] size = {0};
    Files.walkFileTree(
        dirPath,
        new SimpleFileVisitor<Path>() {
          @Override
          public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
            size[0] += attrs.size();
            return FileVisitResult.CONTINUE;
          }
        });
    return size[0];
  }

  /**
   * 记录清理结果
   *
   * @param deletedCount 删除的文件数量
   * @param freedSpace 释放的空间（字节）
   */
  private void logClearResult(int deletedCount, long freedSpace) {
    String freedSpaceSize = formatBytes(freedSpace);
    logger.info(
        "Diagnosis log cleanup completed. Deleted files: {}, Freed space: {}",
        deletedCount,
        freedSpaceSize);
  }

  /**
   * 格式化字节大小
   *
   * @param bytes 字节数
   * @return 格式化后的字符串
   */
  private String formatBytes(long bytes) {
    if (bytes < 1024) {
      return bytes + " B";
    } else if (bytes < 1024 * 1024) {
      return String.format("%.2f KB", bytes / 1024.0);
    } else if (bytes < 1024 * 1024 * 1024) {
      return String.format("%.2f MB", bytes / (1024.0 * 1024.0));
    } else {
      return String.format("%.2f GB", bytes / (1024.0 * 1024.0 * 1024.0));
    }
  }
}
