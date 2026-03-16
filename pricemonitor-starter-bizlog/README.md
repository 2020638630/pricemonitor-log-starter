# PriceMonitor Starter - BusinessLog

业务日志模块，基于AOP记录业务操作日志。

## 功能特性

- 基于 AOP 的日志记录
- 支持同步/异步两种处理方式
- 支持自定义日志处理器
- 自动获取用户上下文信息
- 支持参数灵活配置

## 快速开始

### 1. 添加依赖

```xml
<dependency>
    <groupId>com.pricemonitor</groupId>
    <artifactId>pricemonitor-starter-bizlog</artifactId>
    <version>1.0.0</version>
</dependency>
```

### 2. 使用注解

```java
@Service
public class UserService {

    @BusinessLog(code = "USER_CREATE", description = "创建用户")
    public void createUser(UserDTO userDTO) {
        // 业务逻辑
    }

    @BusinessLog(code = "ORDER_UPDATE", description = "更新订单", sync = true)
    public void updateOrder(OrderDTO orderDTO) {
        // 业务逻辑
    }
}
```

### 3. ⚠️ 重要：实现日志处理器

**必须实现 `BusinessLogProcessor` 接口，否则日志将使用默认处理器输出到日志文件。**

在生产环境中，强烈建议实现自定义的日志处理器来持久化日志：

```java
@Component
public class DatabaseBusinessLogProcessor implements BusinessLogProcessor {

    @Autowired
    private BusinessLogMapper businessLogMapper;

    @Override
    public void process(BusinessLogDTO logDTO) {
        BusinessLogEntity entity = convertToEntity(logDTO);
        businessLogMapper.insert(entity);
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

### @BusinessLog

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| code | String | 是 | - | 业务编码 |
| description | String | 否 | "" | 业务描述 |
| fullArgs | boolean | 否 | false | 是否打印完整参数 |
| sync | boolean | 否 | false | 是否同步处理 |

## 扩展性

### 自定义日志处理器

实现 `BusinessLogProcessor` 接口即可自定义日志处理逻辑：

```java
public interface BusinessLogProcessor {
    void process(BusinessLogDTO logDTO);
    boolean support(BusinessLogDTO logDTO);
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

## 注意事项

1. **必须实现 `BusinessLogProcessor`**
   - 如果未实现自定义处理器，日志将使用默认处理器输出到日志文件
   - 建议生产环境实现自定义处理器以持久化日志到数据库
   - 应用启动时会自动检查并给出提示（可通过 `pricemonitor.bizlog.startup-check=false` 关闭）

2. 异步日志处理需要启用 Spring 异步支持：`@EnableAsync`

3. 用户信息需要在使用前设置到 `UserContextHolder` 中

4. 日志处理器会按照优先级顺序执行（数值越小优先级越高）

## 启动检查

应用启动时，会自动检查是否配置了自定义的 `BusinessLogProcessor`：

- **有自定义处理器**：输出日志 "业务日志处理器配置正常，共找到 X 个自定义处理器"
- **无自定义处理器**：输出警告日志，提示日志将输出到日志文件，并建议实现自定义处理器

可以通过配置禁用启动检查：

```yaml
pricemonitor:
  bizlog:
    startup-check: false  # 禁用启动检查
```
