package com.community.admin.common;

import java.util.HashMap;
import java.util.Map;

/**
 * 统一API响应结果封装类
 * 
 * 该类用于封装所有API接口的返回结果，提供统一的响应格式
 * 包含状态码(code)、消息(msg)和数据(data)三个基本字段
 * 
 * 响应格式示例:
 * {
 *   "code": 200,
 *   "msg": "操作成功",
 *   "data": {...}
 * }
 */
public class Result extends HashMap<String, Object> {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 默认构造函数，初始化成功响应
     * code: 200 (成功)
     * msg: "操作成功"
     */
    public Result() {
        put("code", 200);
        put("msg", "操作成功");
    }
    
    /**
     * 创建成功响应对象
     * @return 成功响应Result实例
     */
    public static Result ok() {
        return new Result();
    }
    
    /**
     * 创建带自定义消息的成功响应对象
     * @param msg 自定义成功消息
     * @return 成功响应Result实例
     */
    public static Result ok(String msg) {
        Result result = new Result();
        result.put("msg", msg);
        return result;
    }
    
    /**
     * 创建带数据的成功响应对象
     * @param data 响应数据
     * @return 成功响应Result实例
     */
    public static Result ok(Map<String, Object> data) {
        Result result = new Result();
        result.put("data", data);
        return result;
    }
    
    /**
     * 创建默认错误响应对象
     * @return 错误响应Result实例 (code: 500, msg: "操作失败")
     */
    public static Result error() {
        return error(500, "操作失败");
    }
    
    /**
     * 创建带自定义消息的错误响应对象
     * @param msg 错误消息
     * @return 错误响应Result实例 (code: 500)
     */
    public static Result error(String msg) {
        return error(500, msg);
    }
    
    /**
     * 创建带状态码和消息的错误响应对象
     * @param code 错误状态码
     * @param msg 错误消息
     * @return 错误响应Result实例
     */
    public static Result error(int code, String msg) {
        Result result = new Result();
        result.put("code", code);
        result.put("msg", msg);
        return result;
    }
    
    /**
     * 创建带消息和数据的错误响应对象
     * @param msg 错误消息
     * @param data 错误相关数据
     * @return 错误响应Result实例
     */
    public static Result error(String msg, Map<String, Object> data) {
        Result result = new Result();
        result.put("msg", msg);
        result.put("data", data);
        return result;
    }
    
    /**
     * 重写put方法，支持链式调用
     * @param key 键
     * @param value 值
     * @return 当前Result实例
     */
    @Override
    public Result put(String key, Object value) {
        super.put(key, value);
        return this;
    }
}