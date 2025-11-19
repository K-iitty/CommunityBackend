# 实时同步功能快速集成指南

## 问题解决方案

您的项目运行成功但实时更新不生效的原因是：
1. AOP切面可能没有正确拦截到MyBatis Plus的方法
2. 现有Controller中没有手动发布Redis消息
3. 前端没有实现轮询或SSE连接

## 快速解决步骤

### 1. 在Controller中添加实时同步

在您现有的Controller中，按以下步骤添加实时同步功能：

#### 步骤1：添加依赖注入

```java
// 在Controller类中添加
@Autowired
private RedisMessageService redisMessageService;

// 或者使用工具类（推荐）
@Autowired
private RealtimeSyncUtil realtimeSyncUtil;
```

#### 步骤2：在增删改操作后添加消息发布

**示例1：业主问题提交（已完成）**
```java
// 在 OwnerIssueController.submitIssue 方法中
OwnerIssue issue = ownerIssueService.submitIssue(owner.getId(), request);

// 发布实时同步消息
realtimeSyncUtil.publishCreate("OwnerIssue", issue.getId(), issue);
realtimeSyncUtil.publishIssueNotification("CREATE", owner.getName(), issue.getTitle());
```

**示例2：公告发布**
```java
// 在公告Controller中
@PostMapping("/create")
public Result createNotice(@RequestBody NoticeRequest request) {
    CommunityNotice notice = noticeService.createNotice(request);
    
    // 发布实时同步消息
    realtimeSyncUtil.publishCreate("CommunityNotice", notice.getId(), notice);
    realtimeSyncUtil.publishNoticeNotification("CREATE", notice.getTitle());
    
    return Result.ok(notice);
}

@PutMapping("/update/{id}")
public Result updateNotice(@PathVariable Long id, @RequestBody NoticeRequest request) {
    CommunityNotice notice = noticeService.updateNotice(id, request);
    
    // 发布实时同步消息
    realtimeSyncUtil.publishUpdate("CommunityNotice", notice.getId(), notice);
    realtimeSyncUtil.publishNoticeNotification("UPDATE", notice.getTitle());
    
    return Result.ok(notice);
}

@DeleteMapping("/delete/{id}")
public Result deleteNotice(@PathVariable Long id) {
    CommunityNotice notice = noticeService.getById(id);
    noticeService.deleteNotice(id);
    
    // 发布实时同步消息
    realtimeSyncUtil.publishDelete("CommunityNotice", id);
    realtimeSyncUtil.publishNoticeNotification("DELETE", notice.getTitle());
    
    return Result.ok();
}
```

**示例3：房屋信息管理**
```java
// 在房屋Controller中
@PostMapping("/create")
public Result createHouse(@RequestBody HouseRequest request) {
    House house = houseService.createHouse(request);
    
    // 发布实时同步消息
    realtimeSyncUtil.publishCreate("House", house.getId(), house);
    realtimeSyncUtil.publishHouseNotification("CREATE", house.getAddress());
    
    return Result.ok(house);
}
```

### 2. 批量添加实时同步的脚本方法

如果您有很多Controller需要修改，可以使用以下模板：

```java
// 在每个需要实时同步的Controller方法中添加：

// 创建操作后
try {
    realtimeSyncUtil.publishCreate("EntityType", entity.getId(), entity);
} catch (Exception e) {
    log.error("发布创建消息失败", e);
}

// 更新操作后
try {
    realtimeSyncUtil.publishUpdate("EntityType", entity.getId(), entity);
} catch (Exception e) {
    log.error("发布更新消息失败", e);
}

// 删除操作后
try {
    realtimeSyncUtil.publishDelete("EntityType", entityId);
} catch (Exception e) {
    log.error("发布删除消息失败", e);
}
```

### 3. 微信小程序集成

将 `MINIPROGRAM_REALTIME_EXAMPLE.js` 中的代码添加到您的微信小程序页面：

1. 复制轮询检查方法到您的页面JS文件
2. 在 `onShow()` 中调用 `startRealtimeSync()`
3. 在 `onHide()` 中调用 `stopRealtimeSync()`
4. 根据您的页面调整 `refreshPageData()` 方法

### 4. Web管理端集成

1. 将 `WEB_REALTIME_EXAMPLE.html` 作为参考
2. 在您的管理端页面中添加SSE连接代码
3. 根据数据变更类型更新相应的页面元素

## 测试步骤

### 1. 启动所有服务
```bash
# 启动Redis
redis-server

# 启动三个模块
cd owner-module && mvn spring-boot:run
cd property-module && mvn spring-boot:run  
cd admin-module && mvn spring-boot:run
```

### 2. 测试实时同步

**方法1：使用测试接口**
```bash
# 测试发送消息
curl -X POST http://localhost:8081/api/realtime/notify \
  -H "Content-Type: application/json" \
  -d '{
    "module": "owner",
    "action": "CREATE",
    "entityType": "OwnerIssue",
    "entityId": 123,
    "data": {"title": "测试问题", "description": "这是一个测试问题"}
  }'
```

**方法2：通过业务接口测试**
1. 在微信小程序中提交一个问题
2. 观察其他端是否收到实时更新
3. 在Web管理端发布公告
4. 观察微信小程序是否收到更新

### 3. 检查日志

查看控制台日志，应该能看到：
```
INFO - Published CREATE message for OwnerIssue: 123
INFO - Received Redis message from channel: community:data:change
INFO - Processing data sync: module=owner, action=CREATE, entityType=OwnerIssue
```

## 常见问题解决

### 1. AOP切面不生效
- 确保添加了 `spring-boot-starter-aop` 依赖
- 检查 `@EnableAspectJAutoProxy` 注解（Spring Boot默认启用）
- 确认Service类上有 `@Service` 注解

### 2. Redis消息未发送
- 检查Redis连接配置
- 确认RedisMessageService被正确注入
- 查看是否有异常日志

### 3. 前端未收到更新
- 检查轮询接口是否正常调用
- 确认SSE连接是否建立成功
- 检查跨域配置

### 4. 消息重复或丢失
- 检查是否有多个地方发布同一消息
- 确认Redis连接稳定性
- 添加消息去重逻辑

## 性能优化建议

1. **批量操作优化**：对于批量操作，可以合并消息或延迟发送
2. **消息过滤**：根据用户权限过滤不相关的消息
3. **连接管理**：定期清理无效的SSE连接
4. **缓存策略**：使用Redis缓存减少数据库查询

## 监控和维护

1. **添加监控指标**：
   - Redis消息发布数量
   - SSE连接数量
   - 消息处理延迟

2. **日志记录**：
   - 记录所有数据变更操作
   - 记录消息发布失败情况
   - 记录连接异常情况

3. **告警机制**：
   - Redis连接异常告警
   - 消息积压告警
   - 连接数异常告警

按照以上步骤操作后，您的实时同步功能应该就能正常工作了！
