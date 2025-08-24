# 📌 Linkis MCP Server

## 📖 项目简介
**Linkis MCP Server** 是一个基于 **Model Context Protocol (MCP)** 的服务端实现，  
它将 **Linkis** 的元数据管理、数据源管理、任务调度与监控等核心能力，统一封装为标准化接口，并以 **MCP 工具**形式对外提供服务。  
这样，AI Agent、自动化脚本或其他客户端都能通过统一协议便捷调用、集成和扩展 Linkis 功能。

---

## 🎯 设计目标
- **统一 API 接口层**：降低直接调用 Linkis REST API 的复杂度
- **模块化结构**：便于扩展新功能模块
- **健壮的错误处理机制**：保障生产环境的稳定性
- **对 AI 友好**：天然适配 AI Agent 等智能系统

---

## 🛠 功能特点
- **认证管理**：封装登录/登出等认证流程
- **元数据 API**：查询库、表、字段、分区信息
- **数据源 API**：管理、创建、修改、删除数据源
- **任务管理 API**：任务提交、状态查询、进度监控、杀任务等
- **标准化 MCP 工具**：便于跨应用调用
- **统一错误处理**：所有接口返回格式统一，易于调试与接入

---

## 🏗 架构概览
```
linkis-mcp-server/
├── mcp_server.py                # MCP Server 主入口，注册所有工具
├── linkis_client.py             # 底层 HTTP 客户端
├── auth.py                      # 认证/登录模块
├── metadata.py                  # 元数据 API
├── knowledge.py                 # 知识问答
├── datasource.py                # 数据源集成 API
├── tasks.py                     # 任务和计算治理 API 
├── config.py                    # 配置管理（环境变量、常量）
├── requirements.txt             # Python 依赖
├── README.md                    # 项目文档
└──tests/                        # 单元测试
        ├── test_auth_api.py
        ├── test_metadata_api.py
        ├── test_datasource_api.py
        ├── test_tasks_api.py
        ├── test_knowledge_api.py
        └── test_mcp_tools.py

```
---

## 📦 安装与运行

```bash
# 1. 克隆代码仓库
git clone https://your-repo-url.git
cd linkis-mcp-server

# 2. 创建并启用虚拟环境
python -m venv venv
source venv/bin/activate       # macOS / Linux
venv\Scripts\activate          # Windows

# 3. 安装依赖
pip install --upgrade pip
pip install -r requirements.txt

# 4. 配置环境
# 在 config.json 中设置 Linkis 地址、端口、Token 等信息
nano config.json

# 5. 启动 MCP 服务
python mcp_server.py

