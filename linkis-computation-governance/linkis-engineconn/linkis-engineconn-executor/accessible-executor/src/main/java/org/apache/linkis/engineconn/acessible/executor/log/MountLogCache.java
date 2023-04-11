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

package org.apache.linkis.engineconn.acessible.executor.log;

import org.apache.linkis.engineconn.acessible.executor.conf.AccessibleExecutorConfiguration;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MountLogCache implements LogCache {

  private static final Logger logger = LoggerFactory.getLogger(MountLogCache.class);

  class CircularQueue {

    private int max;
    private String[] elements;
    private int front, rear, count;

    CircularQueue() {
      this((Integer) AccessibleExecutorConfiguration.ENGINECONN_LOG_CACHE_NUM().getValue());
    }

    CircularQueue(int max) {
      this.max = max;
      this.elements = new String[max];
    }

    public boolean isEmpty() {
      return count == 0;
    }

    public synchronized void enqueue(String value) {
      if (count == max) {
        logger.debug("Queue is full, log: {} needs to be dropped", value);
      } else {
        rear = (rear + 1) % max;
        elements[rear] = value;
        count++;
      }
    }

    public String dequeue() {
      if (count == 0) {
        logger.debug("Queue is empty, nothing to get");
        return null;
      } else {
        front = (front + 1) % max;
        count--;
        return elements[front];
      }
    }

    public List<String> dequeue(int num) {
      List<String> list = new ArrayList<>();
      int index = 0;
      while (index < num) {
        String tempLog = dequeue();
        if (StringUtils.isNotEmpty(tempLog)) {
          list.add(tempLog);
        } else if (tempLog == null) {
          break;
        }
        index++;
      }
      return list;
    }

    public synchronized List<String> getRemain() {
      List<String> list = new ArrayList<>();
      while (!isEmpty()) {
        list.add(dequeue());
      }
      return list;
    }

    public int size() {
      return count;
    }
  }

  private CircularQueue logs;

  public MountLogCache(int loopMax) {
    this.logs = new CircularQueue(loopMax);
  }

  @Override
  public void cacheLog(String log) {
    logs.enqueue(log);
  }

  @Override
  public List<String> getLog(int num) {
    return logs.dequeue(num);
  }

  @Override
  public synchronized List<String> getRemain() {
    return logs.getRemain();
  }

  @Override
  public int size() {
    return logs.size();
  }
}
