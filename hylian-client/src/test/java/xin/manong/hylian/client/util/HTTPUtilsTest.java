package xin.manong.hylian.client.util;

import org.junit.Assert;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

/**
 * @author frankcl
 * @date 2023-09-04 17:06:58
 */
public class HTTPUtilsTest {

    @Test
    public void testRemoveQueries() {
        {
            String url = "https://www.sina.com.cn/a/b/c?a=1&b=2&c=abc";
            Set<String> queryKeys = new HashSet<>();
            queryKeys.add("a");
            queryKeys.add("c");
            Assert.assertEquals("https://www.sina.com.cn/a/b/c?b=2", HTTPUtils.removeQueries(url, queryKeys));
        }
        {
            String url = "https://www.sina.com.cn:123/a/b/c?a=1&b=2&c=abc";
            Set<String> queryKeys = new HashSet<>();
            queryKeys.add("a");
            Assert.assertEquals("https://www.sina.com.cn:123/a/b/c?b=2&c=abc", HTTPUtils.removeQueries(url, queryKeys));
        }
        {
            String url = "https://www.sina.com.cn/a/b/c?a=1&b=2&c=abc";
            Set<String> queryKeys = new HashSet<>();
            queryKeys.add("b");
            Assert.assertEquals("https://www.sina.com.cn/a/b/c?a=1&c=abc", HTTPUtils.removeQueries(url, queryKeys));
        }
        {
            String url = "https://www.sina.com.cn:8080/a/b/c?a=1&b=2&c=abc";
            Set<String> queryKeys = new HashSet<>();
            queryKeys.add("a");
            queryKeys.add("b");
            queryKeys.add("c");
            Assert.assertEquals("https://www.sina.com.cn:8080/a/b/c", HTTPUtils.removeQueries(url, queryKeys));
        }
    }
}
