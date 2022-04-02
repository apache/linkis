package org.apache.linkis.entrance.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.lang3.StringUtils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Map;

/**
 *
 * 支持任意时间格式化和加减运算
 * 例如:${yyyyMMdd%-1d}/${yyyy-MM-01%-2M}等等
 *
 * Author: duhanmin
 * Description: 占位符操作
 * Date: 2021/5/7 11:10
 */
public class PlaceHolder {

    private static final String DOLLAR = "$";
    private static final String PLACEHOLDER_SPLIT = "%";
    private static final String PLACEHOLDER_LEFT = "{";
    private static final String PLACEHOLDER_RIGHT = "}";
    private static final String CYCLE_YEAR = "y";
    private static final String CYCLE_MONTH = "M";
    private static final String CYCLE_DAY = "d";
    private static final String CYCLE_HOUR = "H";
    private static final String CYCLE_MINUTE = "m";
    private static final String CYCLE_SECOND = "s";
    private static final String[] CYCLES  = new String[]{CYCLE_YEAR, CYCLE_MONTH, CYCLE_DAY, CYCLE_HOUR, CYCLE_MINUTE, CYCLE_SECOND};

    public static void main(String[] args) {
        String str = "abc${yyyyMMdd}def";
        System.out.println(str);
        System.out.println(replaces(ZonedDateTime.now(),str));
        String json = "{\"a\":\"${yyyyMMdd}\"}";
        System.out.println(json);
        System.out.println(replaces(ZonedDateTime.now(),json));
    }

    /**
     * 处理时间 yyyy-MM-dd HH:mm:ss
     * @param date
     * @return
     */
    public static ZonedDateTime toZonedDateTime(Date date) {
        Instant instant = date.toInstant();
        ZoneId zoneId = ZoneId.systemDefault();
        LocalDateTime localDateTime = instant.atZone(zoneId).toLocalDateTime();
        return ZonedDateTime.of(localDateTime,zoneId);
    }

    /**
     * json中时间占位符的替换
     * @param dateTime
     * @param str
     * @return
     */
    public static String replaces(ZonedDateTime dateTime,String str){
        return replaces(dateTime,str,true);
    }


    /**
     * json中时间占位符的替换
     * @param dateTime
     * @param str
     * @param format
     * @return
     */
    public static String replaces(ZonedDateTime dateTime,String str,boolean format){
        try {
            final JsonElement parse = new JsonParser().parse(str);
            if (parse.isJsonArray() || parse.isJsonObject()){
                replaceJson(dateTime,parse);
                return parse.toString();
            }
        }catch (Exception e){}
        return replace(dateTime,str);
    }

    /**
     * 具体的替换方法
     * @param dateTime
     * @param str
     * @return
     */
    private static String replace(ZonedDateTime dateTime, String str){
        StringBuilder buffer = new StringBuilder(str);
        int startIndex = str.indexOf(PLACEHOLDER_LEFT);

        while (startIndex != -1) {
            int endIndex = buffer.indexOf(PLACEHOLDER_RIGHT, startIndex);
            if (endIndex != -1) {
                String placeHolder = buffer.substring(startIndex, endIndex + 1);
                String content = placeHolder.replace(PLACEHOLDER_LEFT, "").replace(PLACEHOLDER_RIGHT, "").trim();
                String[] parts = content.split(PLACEHOLDER_SPLIT);
                try{
                    ZonedDateTime ndt = dateTime;
                    for(int i = 1;i< parts.length;i++){
                        ndt = changeDateTime(ndt,parts[i]);
                    }

                    String newContent = ndt.format(DateTimeFormatter.ofPattern(parts[0]));
                    if (buffer.substring(startIndex -1 ,endIndex + 1).contains(DOLLAR)){
                        buffer.replace(startIndex - 1, endIndex + 1, newContent);
                    }else {
                        buffer.replace(startIndex, endIndex + 1, newContent);
                    }
                    startIndex = buffer.indexOf(PLACEHOLDER_LEFT, startIndex + newContent.length());
                }catch (IllegalArgumentException e1){
                    startIndex = buffer.indexOf(PLACEHOLDER_LEFT, endIndex);

                } catch (Exception e2){
                    throw new RuntimeException(e2);
                }
            } else{
                startIndex = -1; //leave while
            }
        }
        return buffer.toString();
    }

    /**
     * 替换参数
     * @param dateTime
     * @param str
     * @return
     */
    private static ZonedDateTime changeDateTime(ZonedDateTime dateTime ,String str){
        if(str == null || str.isEmpty()){
            return dateTime;
        }

        for (String cycle : CYCLES) {
            if (str.contains(cycle)) {
                switch (cycle) {
                    case CYCLE_DAY:
                        return dateTime.plusDays(Integer.parseInt(str.replace(CYCLE_DAY, "")));
                    case CYCLE_HOUR:
                        return dateTime.plusHours(Integer.parseInt(str.replace(CYCLE_HOUR, "")));
                    case CYCLE_MINUTE:
                        return dateTime.plusMinutes(Integer.parseInt(str.replace(CYCLE_MINUTE, "")));
                    case CYCLE_MONTH:
                        return dateTime.plusMonths(Integer.parseInt(str.replace(CYCLE_MONTH, "")));
                    case CYCLE_SECOND:
                        return dateTime.plusSeconds(Integer.parseInt(str.replace(CYCLE_SECOND, "")));
                    case CYCLE_YEAR:
                        return dateTime.plusYears(Integer.parseInt(str.replace(CYCLE_YEAR, "")));
                    default:
                        break;
                }
            }
        }

        return dateTime;
    }

    /**
     * 替换参数
     * @param keyValue
     * @param str
     * @return
     */
    private static String replace(Map<String,String> keyValue, String str){
        StringBuilder buffer = new StringBuilder(str);
        int startIndex = str.indexOf(PLACEHOLDER_LEFT);

        while (startIndex != -1) {
            int endIndex = buffer.indexOf(PLACEHOLDER_RIGHT, startIndex);
            if (endIndex != -1) {
                String placeHolder = buffer.substring(startIndex, endIndex + 1);
                String content = placeHolder.replace(PLACEHOLDER_LEFT, "").replace(PLACEHOLDER_RIGHT, "").trim();
                try{
                    String newContent = keyValue.get(content);
                    if(newContent != null){
                        if (buffer.substring(startIndex -1 ,endIndex + 1).contains(DOLLAR)){
                            buffer.replace(startIndex - 1, endIndex + 1, newContent);
                        }else {
                            buffer.replace(startIndex, endIndex + 1, newContent);
                        }
                        startIndex = buffer.indexOf(PLACEHOLDER_LEFT, startIndex + newContent.length());
                    }else{
                        startIndex = buffer.indexOf(PLACEHOLDER_LEFT, endIndex);
                    }
                }catch (Exception e2){
                    throw new RuntimeException(e2);
                }
            } else{
                startIndex = -1; //leave while
            }
        }
        return buffer.toString();
    }

    /**
     * 替换resource目录下的json文件中的占位符
     * @param dateTime
     * @param object
     */
    @SuppressWarnings("DuplicatedCode")
    private static void replaceJson(ZonedDateTime dateTime, JsonElement object){
        if(object.isJsonArray()){
            JsonArray array = object.getAsJsonArray();
            for(int i = 0;i <array.size();i++){
                JsonElement temp = array.get(i);
                if(temp.isJsonArray()){
                    replaceJson(dateTime,temp);
                }else if(temp.isJsonObject()){
                    replaceJson(dateTime,temp);
                }else{
                    array.add(replace(dateTime,temp.toString()));
                }
            }
        }else if(object.isJsonObject()) {
            JsonObject jsonObject = object.getAsJsonObject();
            for(Map.Entry<String,JsonElement> entry : jsonObject.entrySet()){
                JsonElement temp = entry.getValue();
                if(temp.isJsonArray()){
                    replaceJson(dateTime,temp);
                }else if(temp.isJsonObject()){
                    replaceJson(dateTime,temp);
                }else{
                    jsonObject.addProperty(entry.getKey(), replace(dateTime, temp.toString()));
                }
            }
        }
    }

    /**
     * 使用环境变量替换值
     * @param template
     * @param map
     * @return
     */
    public static String format(CharSequence template, Map<?, ?> map) {
        return PlaceHolder.format(template,map,"${","}",true);
    }

    /**
     * 格式化文本，使用 {varName} 占位<br>
     * map = {a: "aValue", b: "bValue"} format("{a} and {b}", map) ---=》 aValue and bValue
     *
     * @param template   文本模板，被替换的部分用 {key} 表示
     * @param map        参数值对
     * @param leftStr 左边占位符
     * @param rightStr 右边占位符
     * @param ignoreNull 是否忽略 {@code null} 值，忽略则 {@code null} 值对应的变量不被替换，否则替换为""
     * @return
     */
    public static String format(CharSequence template, Map<?, ?> map, CharSequence leftStr, CharSequence rightStr, boolean ignoreNull) {
        if (null == template) {
            return null;
        }
        if (null == map || map.isEmpty()) {
            return template.toString();
        }

        if (StringUtils.isBlank(leftStr)) {
            leftStr = "";
        }

        if (StringUtils.isBlank(rightStr)) {
            rightStr = "";
        }

        String template2 = template.toString();
        String value;
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            value = entry.getValue().toString();
            if (null == value && ignoreNull) {
                continue;
            }
            template2 = StringUtils.replace(template2, leftStr.toString() + entry.getKey() + rightStr, value);
        }
        return template2;
    }
}
