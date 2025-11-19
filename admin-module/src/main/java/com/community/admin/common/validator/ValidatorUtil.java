package com.community.admin.common.validator;

import org.apache.commons.lang3.StringUtils;

import java.util.regex.Pattern;

/**
 * 数据验证工具类
 * 
 * 提供常用的业务数据格式验证功能
 * 包括手机号、身份证号、邮箱和车牌号等常见格式验证
 * 
 * 关键点:
 * 1. 使用正则表达式进行格式验证
 * 2. 使用Apache Commons Lang3的StringUtils处理空值情况
 * 3. 提供静态方法，便于直接调用
 * 
 * 难点说明:
 * 1. 正则表达式的准确性: 需要符合最新的标准规范
 * 2. 边界条件处理: 正确处理null、空字符串等边界情况
 * 3. 性能考虑: 正则表达式应尽量简洁高效
 */
public class ValidatorUtil {
    
    /**
     * 手机号验证
     * 
     * 验证规则:
     * 1. 以1开头
     * 2. 第二位为3-9的数字
     * 3. 总共11位数字
     * 
     * @param mobile 手机号字符串
     * @return true表示格式正确，false表示格式错误
     */
    public static boolean isMobile(String mobile) {
        if (StringUtils.isBlank(mobile)) {
            return false;
        }
        String regex = "^1[3-9]\\d{9}$";
        return Pattern.matches(regex, mobile);
    }
    
    /**
     * 身份证号验证
     * 
     * 验证规则(18位身份证):
     * 1. 6位地区码
     * 2. 8位出生日期码 (YYYYMMDD格式)
     * 3. 3位顺序码
     * 4. 1位校验码(数字或X)
     * 
     * @param idCard 身份证号字符串
     * @return true表示格式正确，false表示格式错误
     */
    public static boolean isIdCard(String idCard) {
        if (StringUtils.isBlank(idCard)) {
            return false;
        }
        String regex = "^[1-9]\\d{5}(18|19|20)\\d{2}((0[1-9])|(1[0-2]))(([0-2][1-9])|10|20|30|31)\\d{3}[0-9Xx]$";
        return Pattern.matches(regex, idCard);
    }
    
    /**
     * 邮箱验证
     * 
     * 验证规则:
     * 1. 包含@符号
     * 2. @符号前后都有内容
     * 3. 包含有效的域名格式
     * 
     * @param email 邮箱字符串
     * @return true表示格式正确，false表示格式错误
     */
    public static boolean isEmail(String email) {
        if (StringUtils.isBlank(email)) {
            return false;
        }
        String regex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        return Pattern.matches(regex, email);
    }
    
    /**
     * 车牌号验证
     * 
     * 验证规则:
     * 1. 包含汉字车牌前缀(省份简称+地区代码)
     * 2. 后跟5位字母或数字
     * 3. 最后一位可能是特殊字符(挂、学、警、港、澳等)
     * 
     * @param licensePlate 车牌号字符串
     * @return true表示格式正确，false表示格式错误
     */
    public static boolean isLicensePlate(String licensePlate) {
        if (StringUtils.isBlank(licensePlate)) {
            return false;
        }
        String regex = "^[京津沪渝冀豫云辽黑湘皖鲁新苏浙赣鄂桂甘晋蒙陕吉闽贵粤青藏川宁琼使领A-Z]{1}[A-Z]{1}[A-Z0-9]{4}[A-Z0-9挂学警港澳]{1}$";
        return Pattern.matches(regex, licensePlate);
    }
}