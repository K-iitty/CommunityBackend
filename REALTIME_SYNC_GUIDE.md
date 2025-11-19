# 实时数据同步系统使用指南

## 概述

本系统实现了基于Redis的跨模块实时数据同步功能，支持业主端、物业端和管理端之间的数据实时更新。

## 系统架构

### 核心组件

1. **RedisMessageService**: Redis消息发布服务
2. **RedisMessageListener**: Redis消息监听器
3. **DataSyncAspect**: AOP切面，自动拦截数据变更操作
4. **RealtimeSyncController**: 实时同步API控制器

### 消息主题

- `community:data:change`: 通用数据变更主题
- `community:owner:change`: 业主端数据变更
- `community:property:change`: 物业端数据变更
- `community:admin:change`: 管理端数据变更
- `community:{module}:notification`: 模块特定通知

## 使用方法

### 1. 自动数据同步

系统会自动拦截Service层的以下方法：
- `save*()`, `create*()`, `add*()` - 创建操作
- `update*()` - 更新操作
- `delete*()`, `remove*()` - 删除操作

当这些方法执行后，会自动发布Redis消息通知其他模块。

### 2. 手动发布消息

```java
@Autowired
private RedisMessageService redisMessageService;

// 发布业主数据变更
redisMessageService.publishOwnerChange("CREATE", "Owner", ownerId, ownerData);

// 发布物业数据变更
redisMessageService.publishPropertyChange("UPDATE", "House", houseId, houseData);

// 发布管理端数据变更
redisMessageService.publishAdminChange("DELETE", "Building", buildingId, null);

// 发布通知消息
redisMessageService.publishNotification("owner", "NOTICE", "新公告", "有新的社区公告发布", null);
```

### 3. 微信小程序实时更新

#### 方式一：轮询检查（推荐）

```javascript
// 微信小程序中的实现
const checkForUpdates = async () => {
  const lastUpdateTime = wx.getStorageSync('lastUpdateTime') || '0';
  
  try {
    const response = await wx.request({
      url: 'http://localhost:8081/api/realtime/check-updates',
      method: 'GET',
      data: {
        lastUpdateTime: lastUpdateTime,
        entityTypes: 'Owner,House,CommunityNotice' // 可选，指定关注的实体类型
      }
    });
    
    if (response.data.hasUpdates) {
      console.log('有新的数据更新，需要刷新页面');
      // 刷新页面数据
      await refreshPageData();
      // 更新最后检查时间
      wx.setStorageSync('lastUpdateTime', response.data.currentTime.toString());
    }
  } catch (error) {
    console.error('检查更新失败:', error);
  }
};

// 定期检查更新（每30秒）
setInterval(checkForUpdates, 30000);

// 页面显示时检查更新
onShow(() => {
  checkForUpdates();
});
```

#### 方式二：SSE长连接（适用于支持的环境）

```javascript
// 注意：微信小程序不直接支持SSE，此方式适用于H5页面
const connectSSE = (userId) => {
  const eventSource = new EventSource(`http://localhost:8081/api/realtime/subscribe/${userId}?clientType=miniprogram`);
  
  eventSource.onmessage = (event) => {
    const data = JSON.parse(event.data);
    console.log('收到实时更新:', data);
    // 处理数据更新
    handleDataUpdate(data);
  };
  
  eventSource.addEventListener('dataChange', (event) => {
    const changeData = JSON.parse(event.data);
    console.log('数据变更:', changeData);
    // 根据变更类型刷新对应数据
    refreshSpecificData(changeData.entityType, changeData.action);
  });
  
  eventSource.onerror = (error) => {
    console.error('SSE连接错误:', error);
  };
};
```

### 4. Web管理端实时更新

```javascript
// Web端JavaScript实现
class RealtimeSync {
  constructor(userId) {
    this.userId = userId;
    this.eventSource = null;
    this.connect();
  }
  
  connect() {
    this.eventSource = new EventSource(`http://localhost:8082/api/realtime/subscribe/${this.userId}?clientType=web`);
    
    this.eventSource.onopen = () => {
      console.log('实时连接已建立');
    };
    
    this.eventSource.onmessage = (event) => {
      console.log('收到消息:', event.data);
    };
    
    this.eventSource.addEventListener('dataChange', (event) => {
      const data = JSON.parse(event.data);
      this.handleDataChange(data);
    });
    
    this.eventSource.onerror = (error) => {
      console.error('连接错误:', error);
      // 重连逻辑
      setTimeout(() => this.connect(), 5000);
    };
  }
  
  handleDataChange(data) {
    const { module, action, entityType, entityId } = data;
    
    // 根据数据变更类型执行相应操作
    switch (action) {
      case 'CREATE':
        this.handleCreate(entityType, data.data);
        break;
      case 'UPDATE':
        this.handleUpdate(entityType, entityId, data.data);
        break;
      case 'DELETE':
        this.handleDelete(entityType, entityId);
        break;
    }
  }
  
  handleCreate(entityType, data) {
    // 处理新增数据
    console.log(`新增${entityType}:`, data);
    // 刷新列表或添加新项
  }
  
  handleUpdate(entityType, entityId, data) {
    // 处理更新数据
    console.log(`更新${entityType} ID:${entityId}:`, data);
    // 更新对应的UI元素
  }
  
  handleDelete(entityType, entityId) {
    // 处理删除数据
    console.log(`删除${entityType} ID:${entityId}`);
    // 从UI中移除对应元素
  }
  
  disconnect() {
    if (this.eventSource) {
      this.eventSource.close();
    }
  }
}

// 使用示例
const realtimeSync = new RealtimeSync('admin_user_123');
```

### 5. 测试实时同步

```bash
# 测试发送数据变更通知
curl -X POST http://localhost:8081/api/realtime/notify \
  -H "Content-Type: application/json" \
  -d '{
    "module": "owner",
    "action": "CREATE",
    "entityType": "Owner",
    "entityId": 123,
    "data": {"name": "张三", "phone": "13800138000"}
  }'

# 检查连接数
curl http://localhost:8081/api/realtime/connections/count
```

## 配置说明

### Redis配置

确保所有模块的Redis配置一致：

```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379
      password: sheep14
      database: 0
      timeout: 2000ms
```

### 端口配置

- Owner Module: 8081
- Property Module: 8082  
- Admin Module: 8082 (注意端口冲突，建议改为8083)

## 注意事项

1. **性能考虑**: 频繁的数据变更可能产生大量Redis消息，建议根据实际需求调整发布频率
2. **网络稳定性**: SSE连接依赖网络稳定性，建议实现重连机制
3. **消息去重**: 可能收到重复消息，客户端应实现去重逻辑
4. **权限控制**: 实际部署时应添加用户权限验证
5. **监控告警**: 建议添加Redis连接和消息处理的监控

## 故障排除

1. **Redis连接失败**: 检查Redis服务状态和配置
2. **消息未收到**: 检查Redis主题订阅和网络连接
3. **SSE连接断开**: 检查网络稳定性和超时设置
4. **数据不同步**: 检查AOP切面是否正确拦截方法调用

## 扩展功能

1. **消息持久化**: 可将重要消息存储到数据库
2. **批量操作优化**: 对批量操作进行消息合并
3. **分布式部署**: 支持多实例部署的消息同步
4. **消息过滤**: 根据用户权限过滤消息内容
