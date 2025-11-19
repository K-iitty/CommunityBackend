package com.community.admin.common.exception;

/**
 * 业务异常类
 * 
 * 用于封装业务逻辑中的异常情况
 * 区别于系统异常，业务异常通常是可以预见的、由业务规则触发的异常
 * 
 * 关键点:
 * 1. 继承自RuntimeException，属于非受检异常，无需显式捕获处理
 * 2. 包含错误码和错误信息，便于前端根据错误码进行不同的处理
 * 3. 提供两种构造函数，支持仅传入错误信息或同时传入错误码和错误信息
 */
public class BusinessException extends RuntimeException {
    
    /**
     * 错误码
     */
    private int code;
    
    /**
     * 错误信息
     */
    private String message;

    /**
     * 构造函数 - 仅包含错误信息
     * @param message 错误信息
     */
    public BusinessException(String message) {
        super(message);
        this.message = message;
    }

    /**
     * 构造函数 - 包含错误码和错误信息
     * @param code 错误码
     * @param message 错误信息
     */
    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    /**
     * 获取错误码
     * @return 错误码
     */
    public int getCode() {
        return code;
    }

    /**
     * 获取错误信息
     * @return 错误信息
     */
    @Override
    public String getMessage() {
        return message;
    }
}