package com.webank.wedatasphere.linkis.metadata.utils

import java.lang.reflect.Type
import java.util.Date

import com.google.gson._


object MdqUtils {


  val gson:Gson= {
    val gsonBuilder = new GsonBuilder()
    gsonBuilder.registerTypeAdapter(classOf[Date], new JsonDeserializer[Date](){
      override def deserialize(json: JsonElement, _type: Type, context: JsonDeserializationContext): Date = {
        new Date(json.getAsJsonPrimitive.getAsLong)
      }
    })
    gsonBuilder.create()
  }



  def ruleString(code:String):String = {
    if (code.length <= 1000) code else code.substring(0, 1000)
  }




}
