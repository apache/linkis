/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

const DEVPROCESS = {
  DEVELOPMENTCENTER: 'dev',// Develop center(开发中心)
  PRODUCTCENTER: 'prod'// Production center(生产中心)
}

const ORCHESTRATORMODES = {
  WORKFLOW: 'pom_work_flow',// Workflow Orchestration(工作流编排)
  SINGLETASK: 'pom_single_task',// single task scheduling(单任务编排)
  CONSTSTORCHESTRATOR: 'pom_consist_orchestrator'// Combination arrangement(组合编排)
}
export { DEVPROCESS, ORCHESTRATORMODES }
