package cn.icuter.directhttp.data;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author edward
 * @since 2020-05-02
 */
public class HeaderTest {

    @Test
    public void testNoParams() {
        Header header = new Header("header-name", "header-value");
        Assert.assertEquals("header-name: header-value", header.toString());
    }

    @Test
    public void testWithParams() {
        Header header = new Header("header-name", "header-value");
        header.addParam("charset", "UTF-8");

        Assert.assertNull(header.getParam("NONE"));
        Assert.assertEquals("DEF", header.getParam("NONE", "DEF"));
        Assert.assertEquals("header-name: header-value; charset=UTF-8", header.toString());

        header.addParam("lang", "zh-CN");
        Assert.assertEquals("UTF-8", header.getParam("charset"));
        Assert.assertEquals("zh-CN", header.getParam("lang"));
        Assert.assertEquals("header-name: header-value; charset=UTF-8; lang=zh-CN", header.toString());
    }
}
