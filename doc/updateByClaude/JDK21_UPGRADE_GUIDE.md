# JDK 21 升级指南

## 当前状态

项目已经完成了从 Java 8 到 Java 21 的代码适配：
- ✅ Spring Boot 升级到 3.3.5
- ✅ 所有 `javax.*` 包已替换为 `jakarta.*`
- ✅ 实体类使用最新的 JPA 3.x 语法
- ✅ Security 配置更新为 Spring Security 6.x
- ✅ 添加了 Spring AI 依赖（OpenAI + PgVector）
- ✅ pom.xml 配置为 Java 21

## 需要安装 JDK 21

当前系统使用 JDK 1.8，需要升级到 JDK 21 才能运行项目。

### 下载 JDK 21

**推荐选项：**

1. **Oracle JDK 21** (官方版本)
   - 下载地址: https://www.oracle.com/java/technologies/downloads/#java21
   - 选择 Windows x64 Installer

2. **OpenJDK 21** (开源版本)
   - Adoptium (Eclipse Temurin): https://adoptium.net/temurin/releases/?version=21
   - Microsoft OpenJDK: https://learn.microsoft.com/zh-cn/java/openjdk/download#openjdk-21
   - Amazon Corretto 21: https://aws.amazon.com/cn/corretto/

### 安装步骤

#### Windows 安装

1. **下载安装包**
   - 下载 `.msi` 或 `.exe` 安装文件

2. **运行安装程序**
   - 双击安装文件
   - 建议安装路径: `C:\Program Files\Java\jdk-21`

3. **配置环境变量**

   **方式一：通过系统设置**
   ```
   1. 右键"此电脑" → 属性 → 高级系统设置
   2. 环境变量
   3. 系统变量 → 新建:
      - 变量名: JAVA_HOME
      - 变量值: C:\Program Files\Java\jdk-21
   4. 编辑 Path，添加:
      - %JAVA_HOME%\bin
   5. 确定保存
   ```

   **方式二：使用 PowerShell**
   ```powershell
   # 以管理员身份运行
   [System.Environment]::SetEnvironmentVariable("JAVA_HOME", "C:\Program Files\Java\jdk-21", "Machine")
   $path = [System.Environment]::GetEnvironmentVariable("Path", "Machine")
   [System.Environment]::SetEnvironmentVariable("Path", "$path;%JAVA_HOME%\bin", "Machine")
   ```

4. **验证安装**
   ```cmd
   # 重新打开命令行窗口
   java -version
   javac -version
   ```

   应该看到类似输出:
   ```
   openjdk version "21.0.x" 2024-xx-xx
   OpenJDK Runtime Environment (build 21.0.x+...)
   OpenJDK 64-Bit Server VM (build 21.0.x+..., mixed mode, sharing)
   ```

### 使用 SDKMAN 管理多版本 Java (推荐)

如果需要同时管理多个 Java 版本，推荐使用 SDKMAN：

#### 在 Windows 上使用 WSL 或 Git Bash

```bash
# 安装 SDKMAN
curl -s "https://get.sdkman.io" | bash
source "$HOME/.sdkman/bin/sdkman-init.sh"

# 安装 Java 21
sdk install java 21.0.1-tem

# 切换版本
sdk use java 21.0.1-tem

# 设置默认版本
sdk default java 21.0.1-tem

# 查看所有已安装版本
sdk list java
```

## 验证项目

安装 JDK 21 后，执行以下命令验证项目：

### 1. 清理并编译

```bash
mvnw.cmd clean compile
```

### 2. 运行测试（可选）

```bash
mvnw.cmd test
```

### 3. 打包项目

```bash
mvnw.cmd clean package
```

### 4. 运行应用

```bash
mvnw.cmd spring-boot:run
```

## Java 21 新特性（项目可用）

升级到 Java 21 后，你可以使用以下新特性：

### 1. Record Patterns (Records 模式匹配)

```java
public record Point(int x, int y) {}

// 在 switch 中使用
Object obj = new Point(1, 2);
switch (obj) {
    case Point(int x, int y) -> System.out.println("Point: " + x + ", " + y);
    default -> System.out.println("Other");
}
```

### 2. Pattern Matching for switch

```java
public String formatValue(Object obj) {
    return switch (obj) {
        case Integer i -> String.format("int %d", i);
        case Long l -> String.format("long %d", l);
        case Double d -> String.format("double %f", d);
        case String s -> String.format("String %s", s);
        default -> obj.toString();
    };
}
```

### 3. Virtual Threads (虚拟线程)

```java
// 非常适合 I/O 密集型操作
try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
    IntStream.range(0, 10_000).forEach(i -> {
        executor.submit(() -> {
            // 执行任务
            return i;
        });
    });
}
```

### 4. String Templates (预览功能)

需要在编译时启用：`--enable-preview`

```java
String name = "World";
String message = STR."Hello, \{name}!";
```

### 5. Sequenced Collections

```java
List<String> list = new ArrayList<>();
list.addFirst("first");
list.addLast("last");
String first = list.getFirst();
String last = list.getLast();
```

## Spring Boot 3.x + Java 21 优势

1. **性能提升**
   - 虚拟线程支持（Project Loom）
   - 更好的 GC 性能
   - 向量 API 改进

2. **Spring AI 集成**
   - 原生支持 OpenAI
   - Vector Store (PgVector)
   - 提示词模板管理

3. **现代化特性**
   - AOT (Ahead-of-Time) 编译
   - GraalVM Native Image 支持
   - Observability 增强

## 故障排除

### 问题 1: `JAVA_HOME` 未生效

**解决方案:**
- 重启命令行窗口
- 检查环境变量拼写
- 确保路径没有引号

### 问题 2: Maven 仍使用旧版本 Java

**解决方案:**
```cmd
# 检查 Maven 使用的 Java 版本
mvnw.cmd -version

# 如果不是 Java 21，清理环境
set JAVA_HOME=C:\Program Files\Java\jdk-21
```

### 问题 3: 编译错误

**解决方案:**
```bash
# 清理 Maven 缓存
mvnw.cmd clean
rm -rf ~/.m2/repository/org/springframework

# 重新编译
mvnw.cmd clean compile
```

## 下一步

安装 JDK 21 后：

1. ✅ 验证 Java 版本: `java -version`
2. ✅ 编译项目: `mvnw.cmd clean compile`
3. ✅ 运行项目: `mvnw.cmd spring-boot:run`
4. 🚀 开始使用 Spring AI 功能
5. 🚀 探索 Java 21 新特性

## 参考资料

- [Java 21 Release Notes](https://www.oracle.com/java/technologies/javase/21-relnote-issues.html)
- [Spring Boot 3.3 Reference](https://docs.spring.io/spring-boot/docs/3.3.x/reference/html/)
- [Spring AI Documentation](https://docs.spring.io/spring-ai/reference/)
- [Jakarta EE 10 Specification](https://jakarta.ee/specifications/platform/10/)

---

**当前项目状态**: 代码已完全适配 Java 21，等待 JDK 升级后即可运行 ✅
