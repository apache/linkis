package org.apache.linkis.storage.script;

import org.apache.linkis.common.io.MetaData;

public class ScriptMetaData implements MetaData {
    private Variable[] variables;

    public ScriptMetaData(Variable[] variables) {
        this.variables = variables;
    }

    @Override
    public MetaData cloneMeta() {
        return new ScriptMetaData(variables.clone());
    }

    public Variable[] getMetaData() {
        return variables;
    }

    public void setMetaData(Variable[] variables) {
        this.variables = variables;
    }
}