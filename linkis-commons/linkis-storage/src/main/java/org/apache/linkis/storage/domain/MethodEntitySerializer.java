package org.apache.linkis.storage.domain;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.module.scala.ScalaObjectMapper$;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.json4s.DefaultFormats;
import org.json4s.DefaultFormats$;
import org.json4s.JsonAST;
import org.json4s.jackson.JsonMethods;
import org.json4s.jackson.JsonMethods$;
import org.json4s.jackson.Serialization;
import java.lang.reflect.Type;
import scala.collection.JavaConverters;

/**
 * Serialize MethodEntity to code 序列化MethodEntity为code
 * <p>
 * Serialized to code as a MethodEntity object 序列化为code为MethodEntity对象
 * <p>
 * Serialize a java object as a string 序列化java对象为字符串
 * <p>
 * Deserialize a string into a java object 将字符串解序列化为java对象
 */
public class MethodEntitySerializer {

    private static final Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").create();
    private static final DefaultFormats formats = DefaultFormats$.MODULE$;

    /**

     Serialized to code as a MethodEntity object 序列化为code为MethodEntity对象
     @param code
     @return
     */
    public static MethodEntity deserializer(String code) {
        //TODO
//        JavaConverters.asScalaBufferConverter(code.getBytes()).mkString(""));
//        JsonAST.JValue parse = JsonMethods$.MODULE$.parse(code, true, true);
//        return.extract(MethodEntity.class, formats, ScalaObjectMapper$.MODULE$);
        return gson.fromJson(code, MethodEntity.class);
    }
    /**

     Serialize MethodEntity to code 序列化MethodEntity为code
     @param methodEntity
     @return
     */
    public static String serializer(MethodEntity methodEntity) {
        return gson.toJson(methodEntity);
    }
    /**

     Serialize a java object as a string 序列化java对象为字符串
     @param value
     @return
     */
    public static String serializerJavaObject(Object value) {
        return gson.toJson(value);
    }
    /**

     Deserialize a string into a java object 将字符串解序列化为java对象
     @param json
     @param classType
     @param <T>
     @return
     */
    public static <T> T deserializerToJavaObject(String json, Class<T> classType) {
        return gson.fromJson(json, classType);
    }
    public static <T> T deserializerToJavaObject(String json, Type oType) {
        return gson.fromJson(json, oType);
    }
}