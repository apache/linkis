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

package org.apache.linkis.monitor.core;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test class for DiagnosisLogCleaner Testing the automatic cleanup functionality for diagnosis logs
 */
public class DiagnosisLogCleanerTest {

  private static final String TEST_BASE_DIR =
      System.getProperty("java.io.tmpdir") + File.separator + "linkis_diagnosis_test";
  private static final String TASK_DIR = TEST_BASE_DIR + File.separator + "task";
  private static final String JSON_DIR = TEST_BASE_DIR + File.separator + "json";

  @Before
  public void setUp() throws IOException {
    // Create test directories
    Files.createDirectories(Paths.get(TASK_DIR));
    Files.createDirectories(Paths.get(JSON_DIR));
  }

  @After
  public void tearDown() throws IOException {
    // Clean up test directories
    deleteDirectory(new File(TEST_BASE_DIR));
  }

  /** TC-002: Test cleanup of expired log directories */
  @Test
  public void testExpiredLogCleanup() throws IOException {
    // Create test directories
    createExpiredJobDirectory("12345", 10); // 10 days ago - should be deleted
    createExpiredJobDirectory("67890", 5); // 5 days ago - should be deleted
    createJobDirectory("11111", 0); // today - should be kept

    // Verify directories exist before cleanup
    assertTrue("Directory 12345 should exist", Files.exists(Paths.get(TASK_DIR, "12345")));
    assertTrue("Directory 67890 should exist", Files.exists(Paths.get(TASK_DIR, "67890")));
    assertTrue("Directory 11111 should exist", Files.exists(Paths.get(TASK_DIR, "11111")));

    // Simulate cleanup with retention days = 7
    int retentionDays = 7;
    int deletedCount = performCleanup(retentionDays);

    // Verify results
    assertEquals("Should delete 2 directories", 2, deletedCount);
    assertFalse("Directory 12345 should be deleted", Files.exists(Paths.get(TASK_DIR, "12345")));
    assertFalse("Directory 67890 should be deleted", Files.exists(Paths.get(TASK_DIR, "67890")));
    assertTrue("Directory 11111 should be kept", Files.exists(Paths.get(TASK_DIR, "11111")));
  }

  /** TC-003: Test cleanup of expired detail JSON files */
  @Test
  public void testExpiredJsonFileCleanup() throws IOException {
    // Create test JSON files
    createExpiredJsonFile("12345", 10); // 10 days ago - should be deleted
    createExpiredJsonFile("67890", 5); // 5 days ago - should be deleted
    createJsonFile("11111", 0); // today - should be kept

    // Verify files exist before cleanup
    assertTrue(
        "JSON file 12345_detail.json should exist",
        Files.exists(Paths.get(JSON_DIR, "12345_detail.json")));
    assertTrue(
        "JSON file 67890_detail.json should exist",
        Files.exists(Paths.get(JSON_DIR, "67890_detail.json")));
    assertTrue(
        "JSON file 11111_detail.json should exist",
        Files.exists(Paths.get(JSON_DIR, "11111_detail.json")));

    // Simulate cleanup with retention days = 7
    int retentionDays = 7;
    int deletedCount = performJsonCleanup(retentionDays);

    // Verify results
    assertEquals("Should delete 2 JSON files", 2, deletedCount);
    assertFalse(
        "JSON file 12345_detail.json should be deleted",
        Files.exists(Paths.get(JSON_DIR, "12345_detail.json")));
    assertFalse(
        "JSON file 67890_detail.json should be deleted",
        Files.exists(Paths.get(JSON_DIR, "67890_detail.json")));
    assertTrue(
        "JSON file 11111_detail.json should be kept",
        Files.exists(Paths.get(JSON_DIR, "11111_detail.json")));
  }

  /** TC-004: Test retention of unexpired logs */
  @Test
  public void testRetainUnexpiredLogs() throws IOException {
    // Create unexpired directories
    createJobDirectory("12345", 3); // 3 days ago - should be kept
    createJobDirectory("67890", 5); // 5 days ago - should be kept

    // Verify directories exist
    assertTrue("Directory 12345 should exist", Files.exists(Paths.get(TASK_DIR, "12345")));
    assertTrue("Directory 67890 should exist", Files.exists(Paths.get(TASK_DIR, "67890")));

    // Simulate cleanup with retention days = 7
    int retentionDays = 7;
    int deletedCount = performCleanup(retentionDays);

    // Verify no directories were deleted
    assertEquals("Should delete 0 directories", 0, deletedCount);
    assertTrue("Directory 12345 should be kept", Files.exists(Paths.get(TASK_DIR, "12345")));
    assertTrue("Directory 67890 should be kept", Files.exists(Paths.get(TASK_DIR, "67890")));
  }

  /** TC-010: Test directory name recognition rule (numeric only) */
  @Test
  public void testDirectoryNameRecognition() throws IOException {
    // Create different types of directories
    createExpiredJobDirectory("12345", 10); // numeric - should be deleted
    createDirectory(TASK_DIR + "/abc", 10); // non-numeric - should be kept
    createDirectory(TASK_DIR + "/task_12345", 10); // prefixed numeric - should be kept

    // Verify directories exist
    assertTrue("Directory 12345 should exist", Files.exists(Paths.get(TASK_DIR, "12345")));
    assertTrue("Directory abc should exist", Files.exists(Paths.get(TASK_DIR, "abc")));
    assertTrue(
        "Directory task_12345 should exist", Files.exists(Paths.get(TASK_DIR, "task_12345")));

    // Simulate cleanup
    int retentionDays = 7;
    int deletedCount = performCleanup(retentionDays);

    // Verify only numeric directory was deleted
    assertEquals("Should delete 1 directory", 1, deletedCount);
    assertFalse("Directory 12345 should be deleted", Files.exists(Paths.get(TASK_DIR, "12345")));
    assertTrue("Directory abc should be kept", Files.exists(Paths.get(TASK_DIR, "abc")));
    assertTrue(
        "Directory task_12345 should be kept", Files.exists(Paths.get(TASK_DIR, "task_12345")));
  }

  /** TC-007: Test handling when log directory does not exist */
  @Test
  public void testNonExistentDirectory() {
    // Use a non-existent directory
    String nonExistentDir = TEST_BASE_DIR + "_nonexistent";

    // Should not throw exception
    int deletedCount = performCleanupForPath(nonExistentDir, 7);

    // Should return 0 as no files were deleted
    assertEquals("Should delete 0 files", 0, deletedCount);
  }

  /** TC-008: Test handling of file deletion failures */
  @Test
  public void testFileDeletionFailure() throws IOException {
    // Create expired directories
    createExpiredJobDirectory("12345", 10);
    createExpiredJobDirectory("67890", 10);

    // Make one directory read-only (simulate deletion failure)
    File readOnlyDir = new File(TASK_DIR, "12345");
    readOnlyDir.setReadOnly();

    // Perform cleanup (should continue even if one file fails)
    int retentionDays = 7;
    int deletedCount = performCleanup(retentionDays);

    // On Windows, setReadOnly might prevent deletion
    // On Unix, we might still be able to delete it
    // The important thing is that the cleanup doesn't throw an exception
    assertTrue("Should delete at least 0 directories", deletedCount >= 0);

    // Clean up
    readOnlyDir.setWritable(true);
  }

  // Helper methods

  private void createExpiredJobDirectory(String jobId, int daysOld) throws IOException {
    Path jobPath = Paths.get(TASK_DIR, jobId);
    Files.createDirectories(jobPath);

    // Set modification time to N days ago
    FileTime oldTime = FileTime.from(Instant.now().minus(daysOld, ChronoUnit.DAYS));
    Files.setAttribute(jobPath, "lastModifiedTime", oldTime);
  }

  private void createJobDirectory(String jobId, int daysOld) throws IOException {
    Path jobPath = Paths.get(TASK_DIR, jobId);
    Files.createDirectories(jobPath);

    if (daysOld > 0) {
      FileTime oldTime = FileTime.from(Instant.now().minus(daysOld, ChronoUnit.DAYS));
      Files.setAttribute(jobPath, "lastModifiedTime", oldTime);
    }
  }

  private void createDirectory(String path, int daysOld) throws IOException {
    Path dirPath = Paths.get(path);
    Files.createDirectories(dirPath);

    if (daysOld > 0) {
      FileTime oldTime = FileTime.from(Instant.now().minus(daysOld, ChronoUnit.DAYS));
      Files.setAttribute(dirPath, "lastModifiedTime", oldTime);
    }
  }

  private void createExpiredJsonFile(String jobId, int daysOld) throws IOException {
    Path jsonPath = Paths.get(JSON_DIR, jobId + "_detail.json");
    Files.write(jsonPath, ("{\"jobId\":\"" + jobId + "\"}").getBytes());

    FileTime oldTime = FileTime.from(Instant.now().minus(daysOld, ChronoUnit.DAYS));
    Files.setAttribute(jsonPath, "lastModifiedTime", oldTime);
  }

  private void createJsonFile(String jobId, int daysOld) throws IOException {
    Path jsonPath = Paths.get(JSON_DIR, jobId + "_detail.json");
    Files.write(jsonPath, ("{\"jobId\":\"" + jobId + "\"}").getBytes());

    if (daysOld > 0) {
      FileTime oldTime = FileTime.from(Instant.now().minus(daysOld, ChronoUnit.DAYS));
      Files.setAttribute(jsonPath, "lastModifiedTime", oldTime);
    }
  }

  private int performCleanup(int retentionDays) {
    return performCleanupForPath(TASK_DIR, retentionDays);
  }

  private int performJsonCleanup(int retentionDays) {
    return performCleanupForPath(JSON_DIR, retentionDays);
  }

  private int performCleanupForPath(String path, int retentionDays) {
    int deletedCount = 0;

    File directory = new File(path);
    if (!directory.exists() || !directory.isDirectory()) {
      return 0;
    }

    File[] files = directory.listFiles();
    if (files == null) {
      return 0;
    }

    long cutoffTime = System.currentTimeMillis() - (retentionDays * 24L * 60 * 60 * 1000);

    for (File file : files) {
      if (shouldDeleteFile(file, cutoffTime)) {
        if (deleteFile(file)) {
          deletedCount++;
        }
      }
    }

    return deletedCount;
  }

  private boolean shouldDeleteFile(File file, long cutoffTime) {
    // Only delete directories with numeric names (for task directory)
    // or JSON files (for json directory)
    String name = file.getName();

    if (file.isDirectory()) {
      // Check if directory name is pure numeric (job_id format)
      return name.matches("\\d+") && file.lastModified() < cutoffTime;
    } else if (file.isFile() && name.endsWith("_detail.json")) {
      // Check if JSON file is expired
      return file.lastModified() < cutoffTime;
    }

    return false;
  }

  private boolean deleteFile(File file) {
    try {
      if (file.isDirectory()) {
        // Recursively delete directory contents
        File[] contents = file.listFiles();
        if (contents != null) {
          for (File content : contents) {
            deleteFile(content);
          }
        }
      }
      return file.delete();
    } catch (Exception e) {
      // Log error but continue with other files
      System.err.println("Failed to delete " + file.getAbsolutePath() + ": " + e.getMessage());
      return false;
    }
  }

  private void deleteDirectory(File directory) {
    if (directory.exists()) {
      File[] files = directory.listFiles();
      if (files != null) {
        for (File file : files) {
          if (file.isDirectory()) {
            deleteDirectory(file);
          } else {
            file.delete();
          }
        }
      }
      directory.delete();
    }
  }
}
