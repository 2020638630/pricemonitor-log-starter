# PriceMonitor Starter - SystemLog

系统日志模块，基于AOP记录系统操作日志。

## 功能特性

- 基于 AOP 的日志记录
- 自动记录请求信息（URL、IP、参数等）
- 支持异常信息记录
- 支持链路追踪集成
- 支持自定义日志处理器
- 异步处理，不影响业务性能

## 快速开始

### 1. 添加依赖

```xml
<dependency>
    <groupId>com.pricemonitor</groupId>
    <artifactId>pricemonitor-starter-syslog</artifactId>
    <version>1.0.0</version>
</dependency>
```

### 2. 使用注解

```java
@RestController
@RequestMapping("/api/user")
public class UserController {

    @SystemLog(title = "用户登录", description = "用户使用账号密码登录系统", module = "用户管理")
    public ResponseEntity<UserInfo> login(@RequestBody LoginRequest request) {
        // 业务逻辑
        return ResponseEntity.ok(userInfo);
    }

    @SystemLog(title = "数据导出", containsFile = true, module = "数据管理")
    public ResponseEntity<Resource> exportData() {
        // 业务逻辑
        return ResponseEntity.ok(resource);
    }
}
```

### 3. ⚠️ 重要：实现日志处理器

**必须实现 `SystemLogProcessor` 接口，否则日志将使用默认处理器输出到日志文件。**

在生产环境中，强烈建议实现自定义的日志处理器来持久化日志：

```java
@Component
public class DatabaseSystemLogProcessor implements SystemLogProcessor {

    @Autowired
    private SystemLogMapper systemLogMapper;

    @Override
    public void process(SystemLogDTO logDTO) {
        SystemLogEntity entity = convertToEntity(logDTO);
        systemLogMapper.insert(entity);
    }

    @Override
    public int getOrder() {
        return 10;
    }
}
```

### 4. 配置用户上下文（可选）

如果需要记录用户信息，可以通过实现 `UserContextProvider` 接口来自定义用户信息获取方式：

```java
@Component
public class CustomUserContextProvider implements UserContextProvider {

    @Override
    public UserContext getUserContext() {
        // 从认证框架中获取用户信息
        UserContext context = new UserContext();
        context.setUserId(SecurityUtils.getCurrentUserId());
        context.setUsername(SecurityUtils.getCurrentUsername());
        context.setTenantId(SecurityUtils.getCurrentTenantId());
        return context;
    }
}
```

## 注解说明

### @SystemLog

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| title | String | 否 | "" | 日志标题 |
| description | String | 否 | "" | 操作描述 |
| containsFile | boolean | 否 | false | 是否包含文件参数 |
| ignoreParams | boolean | 否 | false | 是否忽略参数记录 |
| module | String | 否 | "" | 业务模块 |

## 扩展性

### 自定义日志处理器

实现 `SystemLogProcessor` 接口即可自定义日志处理逻辑：

```java
public interface SystemLogProcessor {
    void process(SystemLogDTO logDTO);
    boolean support(SystemLogDTO logDTO);
    int getOrder();
}
```

### 自定义用户信息获取

实现 `UserContextProvider` 接口即可自定义用户信息获取方式：

```java
public interface UserContextProvider {
    UserContext getUserContext();
    boolean isSupported();
}
```

## 链路追踪集成

系统日志自动支持 MDC 中的 traceId，确保日志与链路追踪工具（如 SkyWalking、Zipkin 等）的集成。

## 注意事项

1. **必须实现 `SystemLogProcessor`**
   - 如果未实现自定义处理器，日志将使用默认处理器输出到日志文件
   - 建议生产环境实现自定义处理器以持久化日志到数据库
   - 应用启动时会自动检查并给出提示（可通过 `pricemonitor.syslog.startup-check=false` 关闭）

2. 异步日志处理需要启用 Spring 异步支持：`@EnableAsync`

3. 用户信息需要在使用前设置到 `UserContextHolder` 中

4. 日志处理器会按照优先级顺序执行（数值越小优先级越高）

5. 包含文件参数时，文件内容不会被记录

## 启动检查

应用启动时，会自动检查是否配置了自定义的 `SystemLogProcessor`：

- **有自定义处理器**：输出日志 "系统日志处理器配置正常，共找到 X 个自定义处理器"
- **无自定义处理器**：输出警告日志，提示日志将输出到日志文件，并建议实现自定义处理器

可以通过配置禁用启动检查：

```yaml
pricemonitor:
  syslog:
    startup-check: false  # 禁用启动检查
```
