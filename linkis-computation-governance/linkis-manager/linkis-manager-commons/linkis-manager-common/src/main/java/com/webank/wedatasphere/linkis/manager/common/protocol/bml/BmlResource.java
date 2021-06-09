package com.webank.wedatasphere.linkis.manager.common.protocol.bml;



import java.io.Serializable;

 public class BmlResource implements Serializable {
    private String fileName;
    private String resourceId;
    private String version;
    private BmlResourceVisibility visibility;
    private String visibleLabels;
    private String owner;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

     public BmlResourceVisibility getVisibility() {
         return visibility;
     }

     public void setVisibility(BmlResourceVisibility visibility) {
         this.visibility = visibility;
     }

     public String getVisibleLabels() {
         return visibleLabels;
     }

     public void setVisibleLabels(String visibleLabels) {
         this.visibleLabels = visibleLabels;
     }

     public String getOwner() {
         return owner;
     }

     public void setOwner(String owner) {
         this.owner = owner;
     }

     public static enum BmlResourceVisibility {
        Public, Private, Label
    }

/*    @Override
    public String toString() {
        return "{" +
                "fileName='" + fileName + '\'' +
                ", resourceId='" + resourceId + '\'' +
                ", version='" + version + '\'' +
                '}';
    }*/
}
