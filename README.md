# Java 静态分析工具项目

本项目是一个用于分析 Java 项目结构和代码质量的静态分析工具，具备类与接口汇总、方法统计、类依赖关系图生成、代码度量，并支持通过本地部署的大语言模型生成代码摘要。

---

##  项目结构

```
project_analyzer/
│
├── src/main/java/analyzer/
│   ├── Class_Interface.java         # 汇总所有类与接口
│   ├── Method.java                  # 收集方法数量、参数个数、代码行数等信息
│   ├── Dependency.java              # 构建类依赖图，生成 .dot 文件
│   ├── Metric.java                  # 计算类的圈复杂度与注释率
│   ├── TargetMethod.java            # 筛选高复杂度/长方法并导出源码至 JSON
│   ├── RunAll.java                  # 一键运行分析任务并记录日志
│
├── generate_summary.py             # 本地调用 Qwen3 生成方法功能摘要
├── selected_methods.json            # 高复杂度/长方法及其源码（由 TargetMethod 生成）
├── method_summaries.json            # 方法功能摘要结果（由 summary_generator.py 生成）
├── output.log                       # RunAll 执行生成的日志文件
├── dependency.dot                   # 生成的类依赖图 DOT 文件（由 Dependency 生成）
├── dependency.png                   # 依赖图可视化图像
├── Assessment.py					 # 根据json文件生成评估excel表
```

---

##  快速开始

###  1. 环境准备

- Java 8+
- Maven
- Python 3.x
- 已通过 Ollama 部署 Qwen3 模型

###  2. 构建并运行

```bash
mvn clean install
cd project_analyzer
```

编译所有文件

```bash
mvn compile  
```

运行所有分析模块，并将日志写入 `output.log`：

```bash
mvn exec:java -Prun-all
```

单独运行某个模块，并将结果显示在终端

```bash
mvn exec:java
```

##  分析功能介绍

| 模块类名                | 功能说明                                                     |
| ----------------------- | ------------------------------------------------------------ |
| `Class_Interface`       | 汇总项目中所有类和接口的完整限定名。                         |
| `Method`                | 统计每个类中方法数量、参数个数、代码行数等基础指标。         |
| `Dependency`            | 构建类之间的依赖关系，并输出 dot 图文件，可用于可视化。      |
| `Metric`                | 计算每个类的圈复杂度（Cyclomatic Complexity）与注释率（Comment Density）。 |
| `TargetMethodCollector` | 筛选出圈复杂度 > 10 或代码行数 > 50 的方法，提取源码并写入 `selected_methods.json`。 |
| `RunAll`                | 将以上四个分析任务整合执行，并将所有输出写入日志文件 `output.log`。 |

##  方法摘要生成（基于本地大模型）

###  步骤说明：

1. 确保 `selected_methods.json` 已由 `TargetMethod` 生成；

```bash
mvn exec:java -Prun-target 
```

2. 本地部署好大语言模型（如通过 Ollama 安装 `qwen3:4b`）：

```bash
ollama run qwen3:4b
```

这步需要提前在网上下载Ollama可执行程序。

3. 执行摘要生成脚本：

```bash
python generate_summary.py
```

运行后生成 `method_summaries.json`，包含每个方法的一句话功能摘要。

##  输出文件说明

| 文件名                  | 描述                                  |
| ----------------------- | ------------------------------------- |
| `output.log`            | 所有分析模块的执行日志。              |
| `selected_methods.json` | 所有复杂/冗长方法的源代码与结构信息。 |
| `method_summaries.json` | 方法的功能摘要（通过大模型生成）。    |
| `dependency.dot`        | 类之间的依赖图（dot）。               |
| `dependency.png`        | 类之间的依赖图（png）。               |

##  示例：快速分析并生成摘要

```bash
mvn compile 
mvn exec:java -Prun-all           # 分析结构 & 输出日志
mvn exec:java -Prun-target    	  #筛选出圈复杂度 > 10 或代码行数 > 50 & 输出json文件
python generate_summary.py       # 使用 Qwen3 本地模型生成方法摘要
```
