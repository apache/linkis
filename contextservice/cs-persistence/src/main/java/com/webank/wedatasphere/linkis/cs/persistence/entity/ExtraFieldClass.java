package com.webank.wedatasphere.linkis.cs.persistence.entity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by patinousward on 2020/2/14.
 */
public class ExtraFieldClass {

    private String className;

    private List<ExtraFieldClass> subs = new ArrayList<>();

    private List<String> fieldNames = new ArrayList<>();

    private List<Object> fieldValues = new ArrayList<>();

    private List<String> fieldTypes = new ArrayList<>();

    public void addSub(ExtraFieldClass sub) {
        subs.add(sub);
    }

    public void addFieldName(String fieldName) {
        fieldNames.add(fieldName);
    }

    public void addFieldValue(Object fieldValue) {
        fieldValues.add(fieldValue);
    }

    public void addFieldType(String fieldtype) {
        fieldTypes.add(fieldtype);
    }

    public ExtraFieldClass getOneSub(int index) {
        return subs.get(index);
    }

    public String getOneFieldName(int index) {
        return fieldNames.get(index);
    }

    public String getOneFieldType(int index) {
        return fieldTypes.get(index);
    }

    public Object getOneFieldValue(int index) {
        return fieldValues.get(index);
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public List<ExtraFieldClass> getSubs() {
        return subs;
    }

    public void setSubs(List<ExtraFieldClass> subs) {
        this.subs = subs;
    }

    public List<String> getFieldNames() {
        return fieldNames;
    }

    public void setFieldNames(List<String> fieldNames) {
        this.fieldNames = fieldNames;
    }

    public List<Object> getFieldValues() {
        return fieldValues;
    }

    public void setFieldValues(List<Object> fieldValues) {
        this.fieldValues = fieldValues;
    }

    public List<String> getFieldTypes() {
        return fieldTypes;
    }

    public void setFieldTypes(List<String> fieldTypes) {
        this.fieldTypes = fieldTypes;
    }
}
