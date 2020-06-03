package com.webank.wedatasphere.linkis.cs.server.enumeration;

import com.webank.wedatasphere.linkis.cs.server.protocol.*;

/**
 * Created by patinousward on 2020/2/19.
 */
public enum ServiceType {
    /**
     *
     */
    CONTEXT_ID {
        @Override
        public HttpRequestProtocol getRequestProtocol() {
            return new ContextIDProtocol();
        }
    },
    CONTEXT {
        @Override
        public HttpRequestProtocol getRequestProtocol() {
            return new ContextProtocol();
        }
    },

    CONTEXT_LISTENER {
        @Override
        public HttpRequestProtocol getRequestProtocol() {
            return new ContextListenerProtocol();
        }
    };

    public HttpRequestProtocol getRequestProtocol() {
        return null;
    }
}
