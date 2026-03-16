# PriceMonitor Starter

PriceMonitor 公共依赖库，提供通用的日志、AOP等组件。

## 项目结构

```
pricemonitor-starter/
├── pricemonitor-starter-core/          # 核心公共依赖模块
│   ├── context/                         # 上下文相关
│   └── util/                            # 工具类
├── pricemonitor-starter-bizlog/         # 业务日志模块
│   ├── annotation/                      # 业务日志注解
│   ├── aspect/                          # 业务日志切面
│   ├── configuration/                   # 自动配置
│   ├── dto/                             # 数据传输对象
│   ├── event/                           # 事件
│   ├── listener/                        # 监听器
│   └── processor/                       # 处理器
└── pricemonitor-starter-syslog/         # 系统日志模块
    ├── annotation/                      # 系统日志注解
    ├── aspect/                          # 系统日志切面
    ├── configuration/                   # 自动配置
    ├── dto/                             # 数据传输对象
    ├── event/                           # 事件
    ├── listener/                        # 监听器
    └── processor/                       # 处理器
```

## 快速开始

### 业务日志模块

```xml
<dependency>
    <groupId>com.pricemonitor</groupId>
    <artifactId>pricemonitor-starter-bizlog</artifactId>
    <version>1.0.0</version>
</dependency>
```

```java
@BusinessLog(code = "USER_CREATE", description = "创建用户")
public void createUser(UserDTO userDTO) {
    // 业务逻辑
}
```

详细文档请参考：[pricemonitor-starter-bizlog/README.md](./pricemonitor-starter-bizlog/README.md)

### 系统日志模块

```xml
<dependency>
    <groupId>com.pricemonitor</groupId>
    <artifactId>pricemonitor-starter-syslog</artifactId>
    <version>1.0.0</version>
</dependency>
```

```java
@SystemLog(title = "用户登录", description = "用户登录系统", module = "用户管理")
public ResponseEntity<UserInfo> login(@RequestBody LoginRequest request) {
    // 业务逻辑
    return ResponseEntity.ok(userInfo);
}
```

详细文档请参考：[pricemonitor-starter-syslog/README.md](./pricemonitor-starter-syslog/README.md)

## 核心依赖

所有模块都依赖 `pricemonitor-starter-core`，该模块提供：

- **TokenInfo**: 用户上下文信息
- **TokenInfoContext**: 用户上下文持有者（线程本地变量）
- **JsonUtil**: JSON工具类
- **IpAddressUtil**: IP地址工具类
- **ExceptionUtil**: 异常工具类

## 模块对比

| 特性 | 业务日志 | 系统日志 |
|------|---------|---------|
| 注解 | @BusinessLog | @SystemLog |
| 关注点 | 业务操作记录 | 系统操作记录 |
| 同步支持 | 支持（sync=true） | 不支持（始终异步） |
| 参数记录 | 支持完整/简化 | 支持忽略 |
| 请求信息 | 不包含 | 包含（URL、IP等） |
| 异常记录 | 支持 | 支持 |
| 链路追踪 | 不支持 | 支持 |

## 版本说明

当前版本：1.0.0

- Spring Boot: 2.7.18
- Java: 1.8
- Hutool: 5.8.25
- Lombok: 1.18.30
- Jackson: 2.15.3
- Commons Lang3: 3.14.0

## 扩展性

所有模块都提供了良好的扩展性：

1. **自定义处理器**: 实现 `BusinessLogProcessor` 或 `SystemLogProcessor` 接口
2. **自定义监听器**: 继承 `AbstractBusinessLogListener` 或 `AbstractSystemLogListener`

## 最佳实践

1. **业务日志**：用于记录关键业务操作，如创建订单、审核通过等
2. **系统日志**：用于记录用户操作、API调用等，便于审计和排查问题
3. **用户上下文**：在认证拦截器中设置用户信息到 `TokenInfoContext`
4. **异步处理**：确保启用 `@EnableAsync` 以支持异步日志处理

## 注意事项

1. 需要启用 Spring 异步支持：`@EnableAsync`
2. 用户信息需要在使用前设置到 `TokenInfoContext` 中
3. 日志处理器按优先级顺序执行
4. 系统日志模块需要 Spring Web 环境

## 许可证

Copyright © 2024 PriceMonitor
