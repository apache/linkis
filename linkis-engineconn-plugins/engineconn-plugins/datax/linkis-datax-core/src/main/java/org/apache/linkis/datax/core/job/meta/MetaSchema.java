/*
 *
 *  Copyright 2020 WeBank
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.linkis.datax.core.job.meta;

import org.apache.linkis.datax.common.GsonUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * store the job related schema information
 * @author davidhua
 * 2019/9/25
 */
public class MetaSchema {
    private List<FieldSchema> fieldSchemas = new ArrayList<>();

    private Map<String, SchemaInfoContext> schemaInfo = new HashMap<>();


    public MetaSchema(){

    }

    public MetaSchema(List<FieldSchema> fieldSchemas){
        this.fieldSchemas = fieldSchemas;
    }

    private static class SchemaInfoContext{
        enum StoreType{
            /**
             * json type
             */
            JSON,
            /**
             * object
             */
            SOURCE_OBJECT
        }
        private Object v;
        private StoreType storeType;
        SchemaInfoContext(Object v, StoreType storeType){
            this.v = v;
            this.storeType = storeType;
        }

    }
    /**
     * field schema
     */
    public static class FieldSchema{
        private String name;
        private String type;
        private String comment;
        private Map<String, Object> props;

        public FieldSchema(String name, String type, String comment){
            this(name, type, comment,  new HashMap<>());
        }
        public FieldSchema(String name, String type,
                           String comment, Map<String, Object> props){
           this.name = name;
           this.type = type;
           this.comment = comment;
           this.props = props;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getComment() {
            return comment;
        }

        public void setComment(String comment) {
            this.comment = comment;
        }

        public Map<String, Object> getProps() {
            return props;
        }

        public void setProps(Map<String, Object> props) {
            this.props = props;
        }
    }

    public void addSchemaInfo(String key, Object v){
        if(v instanceof String || v.getClass().isPrimitive() || isWrapClass(v.getClass())){
            this.schemaInfo.put(key, new SchemaInfoContext(v, SchemaInfoContext.StoreType.SOURCE_OBJECT));
        }else{
            this.schemaInfo.put(key, new SchemaInfoContext(GsonUtil.toJson(v), SchemaInfoContext.StoreType.JSON));
        }
    }

    public <T>T getSchemaInfo(String key, Class<?> clazz){
        SchemaInfoContext schemaInfoContext = this.schemaInfo.get(key);
        if(schemaInfoContext != null){
            if(schemaInfoContext.storeType == SchemaInfoContext.StoreType.JSON){
                return GsonUtil.fromJson(String.valueOf(schemaInfoContext.v), clazz);
            }
            if(schemaInfoContext.v.getClass().equals(clazz)){
                return (T) schemaInfoContext.v;
            }
        }
        return null;
    }

    public <T>List<T> getSchemaInfoList(String key, Class<T> elementClazz){
        SchemaInfoContext schemaInfoContext = this.schemaInfo.get(key);
        if(schemaInfoContext != null){
            return GsonUtil.fromJson(String.valueOf(schemaInfoContext.v), List.class, elementClazz);
        }
        return null;
    }

    public <K,V>Map<K,V> getSchemaInfoMap(String key, Class<K> kClass, Class<V> vClass){
        SchemaInfoContext schemaInfoContext = this.schemaInfo.get(key);
        if(schemaInfoContext != null){
            return GsonUtil.fromJson(String.valueOf(schemaInfoContext.v), Map.class, kClass, vClass);
        }
        return null;
    }

    public List<FieldSchema> getFieldSchemas() {
        return fieldSchemas;
    }

    public void setFieldSchemas(List<FieldSchema> fieldSchemas) {
        this.fieldSchemas = fieldSchemas;
    }

    private static boolean isWrapClass(Class clz){
        try{
            return ((Class)clz.getField("TYPE").get(null)).isPrimitive();
        }catch(Exception e){
            return false;
        }
    }
}
