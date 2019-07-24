/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.webank.wedatasphere.linkis.gateway.springcloud.http;

import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.NettyDataBufferFactory;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.socket.WebSocketMessage;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.ipc.netty.NettyPipeline;
import reactor.ipc.netty.http.websocket.WebsocketOutbound;

import javax.servlet.http.Cookie;

/**
 * created by cooperyang on 2019/1/9.
 */
public class SpringCloudHttpUtils {

    public static Mono<Void> sendWebSocket(WebsocketOutbound out, DataBuffer dataBuffer) {
        WebSocketMessage webSocketMessage = new WebSocketMessage(WebSocketMessage.Type.TEXT, dataBuffer).retain();
        TextWebSocketFrame textWebSocketFrame = new TextWebSocketFrame(NettyDataBufferFactory.toByteBuf(webSocketMessage.getPayload()));
        Flux<WebSocketFrame> frames = Flux.just(textWebSocketFrame);
        return out.options(NettyPipeline.SendOptions::flushOnEach).sendObject(frames).then();
    }

    public static void addIgnoreTimeoutSignal(HttpHeaders httpHeaders) {
        Cookie cookie = com.webank.wedatasphere.linkis.server.security.SecurityFilter.ignoreTimeoutSignal();
        HttpCookie httpCookie = new HttpCookie(cookie.getName(), cookie.getValue());
        if(!httpHeaders.containsKey("Cookie")) {
            httpHeaders.set("Cookie", httpCookie.toString());
        } else {
            httpHeaders.set("Cookie", httpHeaders.getFirst("Cookie") + ";" + httpCookie.toString());
        }
    }
}
