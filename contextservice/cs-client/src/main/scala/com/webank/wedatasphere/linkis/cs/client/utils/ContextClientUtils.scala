package com.webank.wedatasphere.linkis.cs.client.utils

import java.lang
import java.lang.reflect.Type

import com.google.gson.{GsonBuilder, JsonElement, JsonPrimitive, JsonSerializationContext, JsonSerializer}

/**
 * created by cooperyang on 2020/2/23
 * Description:
 */
object ContextClientUtils {
  implicit val gson = new GsonBuilder().setPrettyPrinting().setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").serializeNulls
    .registerTypeAdapter(classOf[java.lang.Double], new JsonSerializer[java.lang.Double] {
      override def serialize(t: lang.Double, `type`: Type, jsonSerializationContext: JsonSerializationContext): JsonElement =
        if(t == t.longValue()) new JsonPrimitive(t.longValue()) else new JsonPrimitive(t)
    }).create
}
