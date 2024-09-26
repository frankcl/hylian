package xin.manong.hylian.client.common;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.util.regex.Pattern;

/**
 * URL模式
 *
 * @author frankcl
 * @date 2023-09-14 15:07:05
 */
@Data
public class URLPattern {

    /**
     * 是否为正则表达式
     */
    public Boolean regex;
    /**
     * 模式字符串
     */
    public String pattern;
    /**
     * 匹配模式
     */
    @JSONField(serialize = false, deserialize = false)
    public Pattern matcher;

    /**
     * 构建文本匹配模式
     *
     * @param pattern 模式字符串
     * @return URL模式
     */
    public static URLPattern buildNormal(String pattern) {
        URLPattern urlPattern = new URLPattern();
        urlPattern.setRegex(false);
        urlPattern.setPattern(pattern);
        return urlPattern;
    }

    /**
     * 构建正则匹配模式
     *
     * @param pattern 模式字符串
     * @return URL模式
     */
    public static URLPattern buildRegex(String pattern) {
        URLPattern urlPattern = new URLPattern();
        urlPattern.setRegex(true);
        urlPattern.setPattern(pattern);
        return urlPattern;
    }
}
