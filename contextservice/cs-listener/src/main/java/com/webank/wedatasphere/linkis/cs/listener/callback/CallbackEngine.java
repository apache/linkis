package com.webank.wedatasphere.linkis.cs.listener.callback;

import com.webank.wedatasphere.linkis.cs.listener.callback.imp.ContextKeyValueBean;

import java.util.ArrayList;
import java.util.Set;

/**
 * @Author: chaogefeng
 * @Date: 2020/2/20
 */
public interface CallbackEngine {

    ArrayList<ContextKeyValueBean> getListenerCallback(String source);

}
