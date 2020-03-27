package cn.icuter.directhttp.utils;

public abstract class HeaderUtils {
    public static String findHeaderParamValue(String value, String paramName, String defParamValue) {
        String valueLowercase = value.toLowerCase();
        int paramIndex = valueLowercase.indexOf(paramName);
        if (paramIndex >= 0) {
            int start = paramIndex + paramName.length() + 1;
            int semicolonIndex = valueLowercase.indexOf(';', start);
            return value.substring(start, semicolonIndex < 0 ? value.length() : semicolonIndex).trim();
        }
        return defParamValue;
    }
}
