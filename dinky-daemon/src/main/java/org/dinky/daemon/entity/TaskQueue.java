/*
 *
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.dinky.daemon.entity;

import org.dinky.daemon.task.DaemonTask;
import org.dinky.daemon.task.DaemonTaskConfig;

import java.util.LinkedList;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Slf4j
public class TaskQueue<T extends DaemonTask> {

    private final LinkedList<T> tasks = new LinkedList<>();

    private final Object lock = new Object();

    public void enqueue(T task) {
        synchronized (lock) {
            lock.notifyAll();
            // prevent duplicate additions
            dequeueByTask(task.getConfig());
            tasks.addLast(task);
        }
    }

    public T dequeue() {
        synchronized (lock) {
            while (tasks.isEmpty()) {
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                    log.error(e.getMessage(), e);
                }
            }
            return tasks.removeFirst();
        }
    }

    public T dequeueByTask(DaemonTaskConfig task) {
        synchronized (lock) {
            T find = null;
            for (T t : tasks) {
                if (t.getConfig().equals(task)) {
                    find = t;
                }
            }
            if (find != null) {
                tasks.remove(find);
            }
            return find;
        }
    }

    public int getTaskSize() {
        synchronized (lock) {
            return tasks.size();
        }
    }
}