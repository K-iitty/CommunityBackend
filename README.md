# 社区客户端后端系统 (Community Client Backend)

![Version](https://img.shields.io/badge/version-1.0.0-blue)
![Java](https://img.shields.io/badge/Java-17-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.4-brightgreen)
![License](https://img.shields.io/badge/license-MIT-green)

## 📋 项目简介

Community Client Backend 是一个基于 Spring Boot 3.x 的微服务后端系统，为社区管理应用提供完整的业务支持。该系统采用模块化架构设计，分离业主端和物业端的业务逻辑，支持多租户场景，提供高效的数据处理和安全的用户认证机制。

## ✨ 主要特性

- 🏗️ **模块化架构** - 业主端（Owner Module）和物业端（Property Module）独立部署
- 🔐 **JWT 认证** - 基于 JSON Web Token 的安全认证机制
- 🗄️ **数据库优化** - 使用 MyBatis Plus ORM 框架，支持高效的数据库操作
- 📚 **API 文档** - 集成 Knife4j 和 SpringDoc OpenAPI，自动生成和展示 API 文档
- ☁️ **云存储集成** - 支持阿里云 OSS 对象存储
- 🤖 **AI 能力** - 集成阿里云通义千问大模型，提供智能问答功能
- 📄 **文档处理** - 支持 PDF、Word 等多种文档格式处理
- 💾 **缓存服务** - 集成 Redis，提高系统性能
- 🔄 **异步处理** - 支持 WebFlux 异步流式处理

## 🏛️ 项目架构

```
CommunityClient-Backend/
├── owner-module/              # 业主端模块
│   ├── src/
│   │   ├── main/java/com/community/owner/
│   │   │   ├── config/        # 配置类（JWT、OSS、安全配置）
│   │   │   ├── controller/    # 控制器层
│   │   │   ├── service/       # 业务逻辑层
│   │   │   ├── mapper/        # 数据映射层（MyBatis）
│   │   │   ├── entity/        # 数据实体
│   │   │   ├── dto/           # 数据传输对象
│   │   │   ├── filter/        # 过滤器（JWT认证）
│   │   │   ├── utils/         # 工具类
│   │   │   └── OwnerApplication.java
│   │   └── resources/
│   │       └── application.yml # 配置文件
│   └── pom.xml
├── property-module/           # 物业端模块
│   ├── src/
│   │   ├── main/java/com/community/property/
│   │   │   ├── config/        # 配置类
│   │   │   ├── controller/    # 控制器层
│   │   │   ├── service/       # 业务逻辑层
│   │   │   ├── mapper/        # 数据映射层
│   │   │   ├── entity/        # 数据实体
│   │   │   ├── dto/           # 数据传输对象
│   │   │   ├── utils/         # 工具类
│   │   │   └── PropertyApplication.java
│   │   └── resources/
│   │       └── application.yml
│   └── pom.xml
├── docs/                      # 文档目录
├── pom.xml                    # 父项目配置
└── README.md
```

## 🛠️ 核心模块说明

### Owner Module（业主端模块）

业主端模块主要包含业主用户的相关功能：

| 功能模块 | 说明 | 相关类 |
|---------|------|-------|
| **认证管理** | 用户登录、注册、登出 | OwnerAuthController, AuthService |
| **账户管理** | 个人信息管理、密码修改 | OwnerController, OwnerService |
| **房产信息** | 我的房产、房产详情管理 | OwnerHouseController, HouseService |
| **车位管理** | 停车位申请、查询 | OwnerParkingController, ParkingService |
| **车辆管理** | 车辆注册、备案 | OwnerVehicleController, VehicleService |
| **水电气表** | 抄表记录、使用情况 | OwnerMeterController, MeterService |
| **物业账单** | 费用查询、缴费记录 | OwnerBillingController, BillingService |
| **社区公告** | 公告查看、搜索、评价 | CommunityNoticeController, NoticeService |
| **报修服务** | 报修申请、进度跟踪 | OwnerIssueController, IssueService |
| **文件上传** | 文件上传、处理 | FileUploadController, FileService |
| **知识库** | 社区知识库查询 | KnowledgeBaseController, KnowledgeBaseService |
| **智能问答** | AI 驱动的智能客服 | SmartQaController, QaService |
| **部门联系** | 部门及联系方式 | DepartmentContactController, DepartmentService |

### Property Module（物业端模块）

物业端模块包含物业管理相关功能：

| 功能模块 | 说明 | 相关类 |
|---------|------|-------|
| **认证管理** | 物业员工登录认证 | PropertyAuthController |
| **社区管理** | 社区基础信息、房产管理 | CommunityService |
| **业主管理** | 业主档案、信息修改 | OwnerService |
| **收费管理** | 费用设置、催缴管理 | BillingService |
| **报修处理** | 工单处理、派工、验收 | IssueService |
| **投诉反馈** | 投诉处理、回复 | ComplaintService |
| **公告发布** | 公告创建、发布、统计 | NoticeService |
| **文档管理** | 知识库维护、文档管理 | DocumentService |

## 🚀 快速开始

### 前置要求

- **Java 17** 或更高版本
- **Maven 3.6+**
- **MySQL 8.0+** - 数据库
- **Redis 6.0+** - 缓存服务
- **阿里云账号** - 可选，用于 OSS 和 AI 服务

### 环境配置

1. **克隆项目**
   ```bash
   git clone <repository-url>
   cd CommunityClient-Backend
   ```

2. **配置数据库**
   
   创建 MySQL 数据库：
   ```sql
   CREATE DATABASE community DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```
   
   修改 `application.yml` 中的数据库连接信息：
   ```yaml
   spring:
     datasource:
       url: jdbc:mysql://localhost:3306/community
       username: root
       password: your_password
   ```

3. **配置 Redis**
   
   确保 Redis 服务启动：
   ```bash
   redis-server
   ```
   
   如需修改连接信息，编辑 `application.yml`：
   ```yaml
   spring:
     data:
       redis:
         host: localhost
         port: 6379
   ```

4. **配置阿里云服务（可选）**
   
   设置环境变量：
   ```bash
   # OSS 配置
   export OSS_ACCESS_KEY_ID=your_access_key
   export OSS_ACCESS_KEY_SECRET=your_secret_key
   
   # AI 服务配置
   export ALIBABA_BAILIAN_API_KEY=your_api_key
   ```

### 构建和运行

**编译项目**
```bash
mvn clean package
```

**运行业主端模块**
```bash
mvn -pl owner-module spring-boot:run
```

业主端服务将运行在 `http://localhost:8081`

**运行物业端模块**
```bash
mvn -pl property-module spring-boot:run
```

物业端服务将运行在对应的配置端口

## 📖 API 文档

项目使用 Knife4j 和 SpringDoc OpenAPI 自动生成 API 文档。

**访问 API 文档**：
- **Knife4j 文档**: `http://localhost:8081/doc.html`
- **Swagger UI**: `http://localhost:8081/swagger-ui.html`
- **OpenAPI JSON**: `http://localhost:8081/v3/api-docs`

## 🔐 安全机制

### JWT 认证流程

1. **用户登录** - 提供用户名和密码
2. **生成 Token** - 系统生成 JWT Token
3. **Token 存储** - 客户端存储 Token
4. **请求验证** - 每个请求都在 Header 中携带 Token
5. **Token 验证** - `JwtAuthenticationFilter` 验证 Token 的有效性

### 密码安全

- 使用密码加密算法进行密码存储
- 支持密码修改和重置功能

### CORS 配置

根据实际需求配置跨域资源共享策略。

## 📦 核心依赖

| 依赖 | 版本 | 用途 |
|------|------|------|
| Spring Boot | 3.3.4 | 核心框架 |
| Spring Security | Latest | 安全认证 |
| MyBatis Plus | 3.5.6 | ORM 框架 |
| MySQL | 8.0.32 | 关系型数据库 |
| Redis | Latest | 缓存服务 |
| JWT (JJWT) | 0.11.5 | Token 管理 |
| Knife4j | 4.4.0 | API 文档 |
| SpringDoc OpenAPI | 2.6.0 | OpenAPI 规范 |
| Aliyun OSS | 3.17.4 | 对象存储 |
| Spring AI Alibaba | 1.0.0-M6.1 | 大模型集成 |
| Apache PDFBox | 2.0.28 | PDF 处理 |
| Apache POI | 5.2.3 | Word/Excel 处理 |

## 🔧 配置说明

### application.yml 主要配置项

```yaml
server:
  port: 8081                    # 服务端口

spring:
  application:
    name: community-client-backend
  datasource:                   # 数据库配置
    url: jdbc:mysql://localhost:3306/community
  data:
    redis:                       # Redis 配置
      host: localhost
      port: 6379
  ai:
    dashscope:                   # 阿里云 AI 配置
      api-key: ${ALIBABA_BAILIAN_API_KEY}

mybatis-plus:                   # MyBatis Plus 配置
  global-config:
    db-config:
      id-type: auto

jwt:
  secret: mySecretKeyForCommunitySystemWhichIsVeryLongAndSecure
  expiration: 86400000          # Token 过期时间（毫秒）

aliyun:
  oss:                          # 阿里云 OSS 配置
    endpoint: oss-cn-beijing.aliyuncs.com
    bucket-name: smart-community-system
```

## 📝 常见操作

### 1. 添加新的 API 端点

```java
@RestController
@RequestMapping("/api/owner")
public class NewFeatureController {
    
    @PostMapping("/feature")
    public ResponseEntity<?> createFeature(@RequestBody FeatureDTO dto) {
        // 业务逻辑
        return ResponseEntity.ok("success");
    }
}
```

### 2. 添加新的数据实体

```java
@Data
@TableName("your_table")
public class YourEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String name;
    // ... 其他字段
}
```

### 3. 创建新的服务层

```java
@Service
public class YourService {
    @Autowired
    private YourMapper mapper;
    
    public List<YourEntity> findAll() {
        return mapper.selectList(null);
    }
}
```

## 🐛 故障排除

| 问题 | 解决方案 |
|------|--------|
| 连接数据库失败 | 检查 MySQL 是否启动，验证用户名密码 |
| Redis 连接失败 | 确保 Redis 服务启动，检查 host 和 port |
| OSS 上传失败 | 验证阿里云密钥和 Bucket 配置 |
| AI 服务调用失败 | 检查 API Key 是否正确设置 |
| JWT Token 过期 | 重新登录获取新的 Token |

## 📚 项目文档

更多详细文档请查看 `docs/` 目录：
- 数据库设计文档
- API 接口详细说明
- 业务流程图
- 部署指南

## 🤝 开发规范

### 代码风格

- 遵循 Java 编码规范
- 使用 Lombok 注解简化代码
- 方法命名：小驼峰命名法
- 类命名：大驼峰命名法

### 提交规范

```
[类型]: 简短描述

具体描述（可选）

- 详细改动 1
- 详细改动 2
```

类型包括：
- `feat`: 新功能
- `fix`: Bug 修复
- `docs`: 文档更新
- `style`: 代码风格
- `refactor`: 代码重构
- `test`: 添加测试

## 📄 许可证

该项目采用 MIT 许可证。

## 📞 联系方式

如有任何问题或建议，欢迎提交 Issue 或联系开发团队。

---

**最后更新**: 2025年11月  
**版本**: 1.0.0  
**维护者**: Community Development Team
