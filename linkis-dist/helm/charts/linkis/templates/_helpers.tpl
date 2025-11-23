# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements.  See the NOTICE file distributed with
# this work for additional information regarding copyright ownership.
# The ASF licenses this file to You under the Apache License, Version 2.0
# (the "License"); you may not use this file except in compliance with
# the License.  You may obtain a copy of the License at
#
#   http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

{{/*
Expand the name of the chart.
*/}}
{{- define "linkis.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Create a default fully qualified app name.
We truncate at 63 chars because some Kubernetes name fields are limited to this (by the DNS naming spec).
If release name contains chart name it will be used as a full name.
*/}}
{{- define "linkis.fullname" -}}
{{- if .Values.fullnameOverride }}
{{- .Values.fullnameOverride | trunc 63 | trimSuffix "-" }}
{{- else }}
{{- $name := default .Chart.Name .Values.nameOverride }}
{{- if contains $name .Release.Name }}
{{- .Release.Name | trunc 63 | trimSuffix "-" }}
{{- else }}
{{- printf "%s-%s" .Release.Name $name | trunc 63 | trimSuffix "-" }}
{{- end }}
{{- end }}
{{- end }}

{{/*
Create chart name and version as used by the chart label.
*/}}
{{- define "linkis.chart" -}}
{{- printf "%s-%s" .Chart.Name .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Common labels
*/}}
{{- define "linkis.labels" -}}
helm.sh/chart: {{ include "linkis.chart" . }}
{{ include "linkis.selectorLabels" . }}
{{- if .Chart.AppVersion }}
app.kubernetes.io/version: {{ .Chart.AppVersion | quote }}
{{- end }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- end }}


{{/**************************************************************************
Global template for linkis
***************************************************************************/}}
{{- define "linkis.datasource.url" -}}
jdbc:mysql://{{ .Values.linkis.datasource.host }}:{{ .Values.linkis.datasource.port }}/{{ .Values.linkis.datasource.database }}?characterEncoding=UTF-8
{{- end }}
{{- define "linkis.registration.url" -}}
http://{{ include "linkis.fullname" . }}-mg-eureka-0.{{ include "linkis.fullname" . }}-mg-eureka-headless.{{ .Release.Namespace }}.svc.cluster.local:{{ .Values.mgEureka.port }}/eureka/
{{- end }}
{{- define "linkis.gateway.url" -}}
http://{{ include "linkis.fullname" . }}-mg-gateway.{{ .Release.Namespace }}.svc.cluster.local:{{ .Values.mgGateway.port }}
{{- end }}


{{/**************************************************************************
Labels for mg-eureka
***************************************************************************/}}
{{/*
Common labels
*/}}
{{- define "linkis.mgEureka.labels" -}}
helm.sh/chart: {{ include "linkis.chart" . }}
{{ include "linkis.mgEureka.selectorLabels" . }}
{{- if .Chart.AppVersion }}
app.kubernetes.io/version: {{ .Chart.AppVersion | quote }}
{{- end }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- end }}

{{/*
Selector labels
*/}}
{{- define "linkis.mgEureka.selectorLabels" -}}
app.kubernetes.io/part-of: linkis
app.kubernetes.io/name: {{ include "linkis.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}-mg-eureka
{{- end }}


{{/**************************************************************************
Labels for mg-gateway
***************************************************************************/}}
{{/*
Common labels
*/}}
{{- define "linkis.mgGateway.labels" -}}
helm.sh/chart: {{ include "linkis.chart" . }}
{{ include "linkis.mgGateway.selectorLabels" . }}
{{- if .Chart.AppVersion }}
app.kubernetes.io/version: {{ .Chart.AppVersion | quote }}
{{- end }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- end }}

{{/*
Selector labels
*/}}
{{- define "linkis.mgGateway.selectorLabels" -}}
app.kubernetes.io/part-of: linkis
app.kubernetes.io/name: {{ include "linkis.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}-mg-gateway
{{- end }}


{{/**************************************************************************
Labels for cg-linkismanager
***************************************************************************/}}
{{/*
Common labels
*/}}
{{- define "linkis.cgLinkisManager.labels" -}}
helm.sh/chart: {{ include "linkis.chart" . }}
{{ include "linkis.cgLinkisManager.selectorLabels" . }}
{{- if .Chart.AppVersion }}
app.kubernetes.io/version: {{ .Chart.AppVersion | quote }}
{{- end }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- end }}

{{/*
Selector labels
*/}}
{{- define "linkis.cgLinkisManager.selectorLabels" -}}
app.kubernetes.io/part-of: linkis
app.kubernetes.io/name: {{ include "linkis.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}-cg-linkismanager
{{- end }}


{{/**************************************************************************
Labels for cg-engineconnmanager
***************************************************************************/}}
{{/*
Common labels
*/}}
{{- define "linkis.cgEngineConnManager.labels" -}}
helm.sh/chart: {{ include "linkis.chart" . }}
{{ include "linkis.cgEngineConnManager.selectorLabels" . }}
{{- if .Chart.AppVersion }}
app.kubernetes.io/version: {{ .Chart.AppVersion | quote }}
{{- end }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- end }}

{{/*
Selector labels
*/}}
{{- define "linkis.cgEngineConnManager.selectorLabels" -}}
app.kubernetes.io/part-of: linkis
app.kubernetes.io/name: {{ include "linkis.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}-cg-engineconnmanager
{{- end }}


{{/**************************************************************************
Labels for cg-engineplugin
***************************************************************************/}}
{{/*
Common labels
*/}}
{{- define "linkis.cgEnginePlugin.labels" -}}
helm.sh/chart: {{ include "linkis.chart" . }}
{{ include "linkis.cgEnginePlugin.selectorLabels" . }}
{{- if .Chart.AppVersion }}
app.kubernetes.io/version: {{ .Chart.AppVersion | quote }}
{{- end }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- end }}

{{/*
Selector labels
*/}}
{{- define "linkis.cgEnginePlugin.selectorLabels" -}}
app.kubernetes.io/part-of: linkis
app.kubernetes.io/name: {{ include "linkis.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}-cg-engineplugin
{{- end }}


{{/**************************************************************************
Labels for cg-entrance
***************************************************************************/}}
{{/*
Common labels
*/}}
{{- define "linkis.cgEntrance.labels" -}}
helm.sh/chart: {{ include "linkis.chart" . }}
{{ include "linkis.cgEntrance.selectorLabels" . }}
{{- if .Chart.AppVersion }}
app.kubernetes.io/version: {{ .Chart.AppVersion | quote }}
{{- end }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- end }}

{{/*
Selector labels
*/}}
{{- define "linkis.cgEntrance.selectorLabels" -}}
app.kubernetes.io/part-of: linkis
app.kubernetes.io/name: {{ include "linkis.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}-cg-entrance
{{- end }}


{{/**************************************************************************
Labels for ps-cs
***************************************************************************/}}
{{/*
Common labels
*/}}
{{- define "linkis.psCs.labels" -}}
helm.sh/chart: {{ include "linkis.chart" . }}
{{ include "linkis.psCs.selectorLabels" . }}
{{- if .Chart.AppVersion }}
app.kubernetes.io/version: {{ .Chart.AppVersion | quote }}
{{- end }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- end }}

{{/*
Selector labels
*/}}
{{- define "linkis.psCs.selectorLabels" -}}
app.kubernetes.io/part-of: linkis
app.kubernetes.io/name: {{ include "linkis.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}-ps-cs
{{- end }}


{{/**************************************************************************
Labels for ps-data-source-manager
***************************************************************************/}}
{{/*
Common labels
*/}}
{{- define "linkis.psDataSourceManager.labels" -}}
helm.sh/chart: {{ include "linkis.chart" . }}
{{ include "linkis.psDataSourceManager.selectorLabels" . }}
{{- if .Chart.AppVersion }}
app.kubernetes.io/version: {{ .Chart.AppVersion | quote }}
{{- end }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- end }}

{{/*
Selector labels
*/}}
{{- define "linkis.psDataSourceManager.selectorLabels" -}}
app.kubernetes.io/part-of: linkis
app.kubernetes.io/name: {{ include "linkis.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}-ps-data-source-manager
{{- end }}


{{/**************************************************************************
Labels for ps-metadataquery
***************************************************************************/}}
{{/*
Common labels
*/}}
{{- define "linkis.psMetadataQuery.labels" -}}
helm.sh/chart: {{ include "linkis.chart" . }}
{{ include "linkis.psMetadataQuery.selectorLabels" . }}
{{- if .Chart.AppVersion }}
app.kubernetes.io/version: {{ .Chart.AppVersion | quote }}
{{- end }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- end }}

{{/*
Selector labels
*/}}
{{- define "linkis.psMetadataQuery.selectorLabels" -}}
app.kubernetes.io/part-of: linkis
app.kubernetes.io/name: {{ include "linkis.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}-ps-metadataquery
{{- end }}


{{/**************************************************************************
Labels for ps-publicservice
***************************************************************************/}}
{{/*
Common labels
*/}}
{{- define "linkis.psPublicService.labels" -}}
helm.sh/chart: {{ include "linkis.chart" . }}
{{ include "linkis.psPublicService.selectorLabels" . }}
{{- if .Chart.AppVersion }}
app.kubernetes.io/version: {{ .Chart.AppVersion | quote }}
{{- end }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- end }}

{{/*
Selector labels
*/}}
{{- define "linkis.psPublicService.selectorLabels" -}}
app.kubernetes.io/part-of: linkis
app.kubernetes.io/name: {{ include "linkis.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}-ps-publicservice
{{- end }}


{{/**************************************************************************
Labels for web
***************************************************************************/}}
{{/*
Common labels
*/}}
{{- define "linkis.Web.labels" -}}
helm.sh/chart: {{ include "linkis.chart" . }}
{{ include "linkis.Web.selectorLabels" . }}
{{- if .Chart.AppVersion }}
app.kubernetes.io/version: {{ .Chart.AppVersion | quote }}
{{- end }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- end }}

{{/*
Selector labels
*/}}
{{- define "linkis.Web.selectorLabels" -}}
app.kubernetes.io/part-of: linkis
app.kubernetes.io/name: {{ include "linkis.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}-web
{{- end }}


{{/**************************************************************************
Common selector labels
***************************************************************************/}}
{{/*
Selector labels
*/}}
{{- define "linkis.selectorLabels" -}}
app.kubernetes.io/part-of: linkis
app.kubernetes.io/name: {{ include "linkis.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end }}

{{/*
Create the name of the service account to use
*/}}
{{- define "linkis.serviceAccountName" -}}
{{- if .Values.serviceAccount.create }}
{{- default (include "linkis.fullname" .) .Values.serviceAccount.name }}
{{- else }}
{{- default "default" .Values.serviceAccount.name }}
{{- end }}
{{- end }}

{{/*
Create the name of the role to use
*/}}
{{- define "linkis.roleName" -}}
{{- if .Values.role.create }}
{{- default (include "linkis.fullname" .) .Values.role.name }}
{{- else }}
{{- default "default" .Values.role.name }}
{{- end }}
{{- end }}

{{/*
Create the name of the role to use
*/}}
{{- define "linkis.roleBindingName" -}}
{{- if .Values.roleBinding.create }}
{{- default (include "linkis.fullname" .) .Values.roleBinding.name }}
{{- else }}
{{- default "default" .Values.roleBinding.name }}
{{- end }}
{{- end }}
