package com.alibaba.datax.plugin.unstructuredstorage;

import java.util.Objects;

/**
 * @author davidhua
 * 2019/6/13
 */
public class PathMeta {
    private String absolute;

    private String relative;

    public PathMeta(){

    }

    public PathMeta(String absolute, String relative){
        this.absolute = absolute;
        this.relative = relative;
    }

    public String getAbsolute(){
        return this.absolute;
    }

    public String getRelative(){
        return this.relative;
    }

    public void setAbsolute(String absolute) {
        this.absolute = absolute;
    }

    public void setRelative(String relative) {
        this.relative = relative;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o){
            return true;
        }
        if (o == null || getClass() != o.getClass()){
            return false;
        }
        PathMeta pathMeta = (PathMeta) o;
        return Objects.equals(absolute, pathMeta.absolute);
    }

    @Override
    public int hashCode() {

        return Objects.hash(absolute);
    }
}
