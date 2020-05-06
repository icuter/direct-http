package cn.icuter.directhttp.data;

import cn.icuter.directhttp.utils.StringUtils;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * name: value *(; params) <br />
 * params: key=val
 *
 * <br />
 *
 * Different to the Cookie {@link java.net.HttpCookie}
 *
 * @author edward
 * @since 2020-05-01
 */
public class Header {
    private String name;
    private String value;
    private Map<String, String> params;

    public Header(String name, String value) {
        if (StringUtils.isBlank(name)) {
            throw new IllegalArgumentException("Header name MUST not be empty or null");
        }
        this.name = name;
        this.value = value;
    }

    public String getParam(String key) {
        return getParam(key, null);
    }

    public String getParam(String key, String defValue) {
        if (params != null) {
            return params.getOrDefault(key, defValue);
        }
        return defValue;
    }

    public Header addParam(String key, String value) {
        if (params == null) {
            params = new LinkedHashMap<>();
        }
        params.put(key, value);

        return this;
    }

    public void setParams(Map<String, String> params) {
        this.params = params;
    }

    public Map<String, String> getParams() {
        return params;
    }

    @Override
    public String toString() {
        return name + ": " + value + paramsToString(params);
    }

    private String paramsToString(Map<String, String> params) {
        if (params == null || params.isEmpty()) {
            return "";
        }
        StringBuilder result = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            result.append("; ").append(entry.getKey()).append("=").append(entry.getValue());
        }
        return result.toString();
    }
}
