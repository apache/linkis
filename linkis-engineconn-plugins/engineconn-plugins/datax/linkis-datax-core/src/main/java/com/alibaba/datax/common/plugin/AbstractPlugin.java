package com.alibaba.datax.common.plugin;

import com.alibaba.datax.common.base.BaseObject;
import com.alibaba.datax.common.util.Configuration;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractPlugin extends BaseObject implements Pluginable {

    //Job configuration
    private Configuration pluginJobConf;

    //Plugin config
    private Configuration pluginConf;

    //Support multiply peers
    private List<Configuration> peerPluginJobConfList = new ArrayList<>();

    private List<String> peerPluginNames = new ArrayList<>();

    @Override
    public String getPluginName() {
        assert null != this.pluginConf;
        return this.pluginConf.getString("name");
    }

    @Override
    public String getDeveloper() {
        assert null != this.pluginConf;
        return this.pluginConf.getString("developer");
    }

    @Override
    public String getDescription() {
        assert null != this.pluginConf;
        return this.pluginConf.getString("description");
    }

    @Override
    public Configuration getPluginJobConf() {
        return pluginJobConf;
    }

    @Override
    public void setPluginJobConf(Configuration pluginJobConf) {
        this.pluginJobConf = pluginJobConf;
    }

    @Override
    public void setPluginConf(Configuration pluginConf) {
        this.pluginConf = pluginConf;
    }

    @Override
    public List<Configuration> getPeerPluginJobConfList() {
        return peerPluginJobConfList;
    }

    @Override
    public void addPeerPluginJobConf(Configuration peerPluginJobConf) {
        this.peerPluginJobConfList.add(peerPluginJobConf);
    }

    @Override
    public List<String> getPeerPluginNameList() {
        return peerPluginNames;
    }

    @Override
    public void addPeerPluginName(String peerPluginName) {
        this.peerPluginNames.add(peerPluginName);
    }

    public void preCheck() {
    }

    public void prepare() {
    }

    public void post() {
    }

    public void preHandler(Configuration jobConfiguration) {

    }

    public void postHandler(Configuration jobConfiguration) {

    }

}
