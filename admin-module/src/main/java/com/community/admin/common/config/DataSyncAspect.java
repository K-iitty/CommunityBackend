package com.community.admin.common.config;

import com.community.admin.service.RedisMessageService;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;

/**
 * 数据同步切面
 * 自动在增删改操作后发布Redis消息，实现实时数据同步
 */
@Aspect
@Component
@Slf4j
public class DataSyncAspect {

    @Autowired
    private RedisMessageService redisMessageService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 拦截Service层的增删改操作
     * 包括MyBatis Plus的标准方法和自定义方法
     */
    @AfterReturning(pointcut = "execution(* com.community.admin.service..*Service*.save*(..)) || " +
                              "execution(* com.community.admin.service..*Service*.update*(..)) || " +
                              "execution(* com.community.admin.service..*Service*.delete*(..)) || " +
                              "execution(* com.community.admin.service..*Service*.remove*(..)) || " +
                              "execution(* com.community.admin.service..*Service*.create*(..)) || " +
                              "execution(* com.community.admin.service..*Service*.add*(..)) || " +
                              "execution(* com.community.admin.service..*ServiceImpl.save*(..)) || " +
                              "execution(* com.community.admin.service..*ServiceImpl.update*(..)) || " +
                              "execution(* com.community.admin.service..*ServiceImpl.delete*(..)) || " +
                              "execution(* com.community.admin.service..*ServiceImpl.remove*(..)) || " +
                              "execution(* com.community.admin.service..*ServiceImpl.create*(..)) || " +
                              "execution(* com.community.admin.service..*ServiceImpl.add*(..))", 
                    returning = "result")
    public void afterDataModification(JoinPoint joinPoint, Object result) {
        try {
            String methodName = joinPoint.getSignature().getName();
            String className = joinPoint.getTarget().getClass().getSimpleName();
            Object[] args = joinPoint.getArgs();

            // 确定操作类型
            String action = determineAction(methodName);
            
            // 确定实体类型
            String entityType = determineEntityType(className);
            
            // 获取实体ID
            Object entityId = extractEntityId(args, result);
            
            if (action != null && entityType != null) {
                // 发布数据变更消息
                redisMessageService.publishAdminChange(action, entityType, entityId, result);
                
                // 更新最后更新时间
                String key = "community:last_update:" + entityType;
                redisTemplate.opsForValue().set(key, String.valueOf(System.currentTimeMillis()));
                
                log.info("Published data sync message: action={}, entityType={}, entityId={}", 
                        action, entityType, entityId);
            }
            
        } catch (Exception e) {
            log.error("Error in data sync aspect", e);
        }
    }

    /**
     * 根据方法名确定操作类型
     */
    private String determineAction(String methodName) {
        if (methodName.startsWith("save") || methodName.startsWith("create") || methodName.startsWith("add")) {
            return "CREATE";
        } else if (methodName.startsWith("update")) {
            return "UPDATE";
        } else if (methodName.startsWith("delete") || methodName.startsWith("remove")) {
            return "DELETE";
        }
        return null;
    }

    /**
     * 根据Service类名确定实体类型
     */
    private String determineEntityType(String className) {
        if (className.contains("Owner")) {
            return "Owner";
        } else if (className.contains("House")) {
            return "House";
        } else if (className.contains("Notice")) {
            return "CommunityNotice";
        } else if (className.contains("Community")) {
            return "CommunityInfo";
        } else if (className.contains("Issue")) {
            return "OwnerIssue";
        } else if (className.contains("Vehicle")) {
            return "Vehicle";
        } else if (className.contains("Parking")) {
            return "ParkingSpace";
        } else if (className.contains("Building")) {
            return "Building";
        } else if (className.contains("Staff")) {
            return "Staff";
        } else if (className.contains("Department")) {
            return "Department";
        } else if (className.contains("SystemAdmin")) {
            return "SystemAdmin";
        }
        return className.replace("Service", "");
    }

    /**
     * 提取实体ID
     */
    private Object extractEntityId(Object[] args, Object result) {
        // 尝试从参数中获取ID
        if (args != null && args.length > 0) {
            Object firstArg = args[0];
            if (firstArg instanceof Number) {
                return firstArg;
            }
            // 尝试通过反射获取ID字段
            try {
                Method getIdMethod = firstArg.getClass().getMethod("getId");
                return getIdMethod.invoke(firstArg);
            } catch (Exception e) {
                // 忽略异常，继续尝试其他方式
            }
        }
        
        // 尝试从返回结果中获取ID
        if (result != null) {
            try {
                Method getIdMethod = result.getClass().getMethod("getId");
                return getIdMethod.invoke(result);
            } catch (Exception e) {
                // 忽略异常
            }
        }
        
        return null;
    }
}
