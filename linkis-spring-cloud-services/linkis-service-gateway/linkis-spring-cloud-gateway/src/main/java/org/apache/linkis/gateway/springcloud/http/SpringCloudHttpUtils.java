/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
package org.apache.linkis.gateway.springcloud.http;

import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.NettyDataBufferFactory;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.socket.WebSocketMessage;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.http.websocket.WebsocketOutbound;

import javax.servlet.http.Cookie;
import java.util.HashMap;
import java.util.Map;

public class SpringCloudHttpUtils {

    public static Mono<Void> sendWebSocket(WebsocketOutbound out, DataBuffer dataBuffer) {
        WebSocketMessage webSocketMessage = new WebSocketMessage(WebSocketMessage.Type.TEXT, dataBuffer).retain();
        TextWebSocketFrame textWebSocketFrame = new TextWebSocketFrame(NettyDataBufferFactory.toByteBuf(webSocketMessage.getPayload()));
        Flux<WebSocketFrame> frames = Flux.just(textWebSocketFrame);
        //todo
        return out.sendObject(frames).then();
        //return out.options(NettyPipeline.SendOptions::flushOnEach).sendObject(frames).then();
    }

    public static void addIgnoreTimeoutSignal(HttpHeaders httpHeaders) {
        Cookie cookie = org.apache.linkis.server.security.SecurityFilter.ignoreTimeoutSignal();
        Map<String, Cookie[]> cookies = new HashMap<>();
        cookies.put(cookie.getName(), new Cookie[]{cookie});
        addCookies(httpHeaders, cookies);
    }

    public static void addCookies(HttpHeaders httpHeaders, Map<String, Cookie[]> cookies) {
        if(cookies == null || cookies.isEmpty()) {
            return;
        }
        StringBuilder cookieStr = new StringBuilder();
        for (String cookieName: cookies.keySet()) {
            Cookie[] cookie = cookies.get(cookieName);
            if(cookie == null || cookie.length == 0) continue;
            HttpCookie httpCookie = new HttpCookie(cookie[0].getName(), cookie[0].getValue());
            cookieStr.append(httpCookie.toString()).append(";");
        }
        if(cookieStr.length() > 1) {
            cookieStr.setLength(cookieStr.length() - 1);
            if(!httpHeaders.containsKey("Cookie")) {
                httpHeaders.set("Cookie", cookieStr.toString());
            } else {
                httpHeaders.set("Cookie", httpHeaders.getFirst("Cookie") + ";" + cookieStr.toString());
            }
        }
    }
}
