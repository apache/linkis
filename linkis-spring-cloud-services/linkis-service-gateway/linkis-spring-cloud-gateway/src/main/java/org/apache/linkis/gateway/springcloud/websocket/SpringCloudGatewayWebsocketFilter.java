/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.linkis.gateway.springcloud.websocket;

import org.apache.linkis.common.ServiceInstance;
import org.apache.linkis.gateway.http.GatewayContext;
import org.apache.linkis.gateway.parser.GatewayParser;
import org.apache.linkis.gateway.route.GatewayRouter;
import org.apache.linkis.gateway.security.GatewaySSOUtils;
import org.apache.linkis.gateway.springcloud.http.SpringCloudGatewayHttpRequest;
import org.apache.linkis.gateway.springcloud.http.SpringCloudHttpUtils;
import org.apache.linkis.server.Message;
import org.apache.linkis.server.socket.controller.ServerEvent;
import org.apache.linkis.server.socket.controller.SocketServerEvent;

import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.WebsocketRoutingFilter;
import org.springframework.cloud.gateway.filter.headers.HttpHeadersFilter;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import org.springframework.web.reactive.socket.client.WebSocketClient;
import org.springframework.web.reactive.socket.server.WebSocketService;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;

import static org.apache.linkis.gateway.springcloud.websocket.SpringCloudGatewayWebsocketUtils.*;

public class SpringCloudGatewayWebsocketFilter implements GlobalFilter, Ordered {
  private WebsocketRoutingFilter websocketRoutingFilter;
  private WebSocketClient webSocketClient;
  private WebSocketService webSocketService;
  private LoadBalancerClient loadBalancer;
  private GatewayParser parser;
  private GatewayRouter router;

  public SpringCloudGatewayWebsocketFilter(
      WebsocketRoutingFilter websocketRoutingFilter,
      WebSocketClient webSocketClient,
      WebSocketService webSocketService,
      LoadBalancerClient loadBalancer,
      GatewayParser parser,
      GatewayRouter router) {
    this.websocketRoutingFilter = websocketRoutingFilter;
    this.webSocketClient = webSocketClient;
    this.webSocketService = webSocketService;
    this.loadBalancer = loadBalancer;
    this.parser = parser;
    this.router = router;
  }

  public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
    changeSchemeIfIsWebSocketUpgrade(websocketRoutingFilter, exchange);
    URI requestUrl = exchange.getRequiredAttribute(ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR);
    String scheme = requestUrl.getScheme();
    if (!ServerWebExchangeUtils.isAlreadyRouted(exchange)
        && ("ws".equals(scheme) || "wss".equals(scheme))) {
      ServerWebExchangeUtils.setAlreadyRouted(exchange);
      HttpHeaders headers = exchange.getRequest().getHeaders();
      List<String> protocols = headers.get("Sec-WebSocket-Protocol");
      if (protocols != null) {
        protocols =
            (List<String>)
                protocols.stream()
                    .flatMap(
                        (header) -> {
                          return Arrays.stream(StringUtils.commaDelimitedListToStringArray(header));
                        })
                    .map(String::trim)
                    .collect(Collectors.toList());
      }
      List<String> collectedProtocols = protocols;
      GatewayContext gatewayContext = getGatewayContext(exchange);
      return this.webSocketService.handleRequest(
          exchange,
          new WebSocketHandler() {
            public Mono<Void> handle(WebSocketSession webClientSocketSession) {
              GatewayWebSocketSessionConnection gatewayWebSocketSession =
                  getGatewayWebSocketSessionConnection(
                      GatewaySSOUtils.getLoginUsername(gatewayContext), webClientSocketSession);
              FluxSinkListener fluxSinkListener =
                  new FluxSinkListener<WebSocketMessage>() {
                    private FluxSink<WebSocketMessage> fluxSink = null;

                    @Override
                    public void setFluxSink(FluxSink<WebSocketMessage> fluxSink) {
                      this.fluxSink = fluxSink;
                    }

                    @Override
                    public void next(WebSocketMessage webSocketMessage) {
                      if (fluxSink != null) fluxSink.next(webSocketMessage);
                      GatewaySSOUtils.updateLastAccessTime(gatewayContext);
                    }

                    @Override
                    public void complete() {
                      if (fluxSink != null) fluxSink.complete();
                    }
                  };
              Flux<WebSocketMessage> receives =
                  Flux.create(
                      sink -> {
                        fluxSinkListener.setFluxSink(sink);
                      });
              gatewayWebSocketSession
                  .receive()
                  .doOnNext(WebSocketMessage::retain)
                  .map(
                      t -> {
                        String user;
                        try {
                          user = GatewaySSOUtils.getLoginUsername(gatewayContext);
                        } catch (Throwable e) {
                          if (gatewayWebSocketSession.isAlive()) {
                            String message =
                                Message.response(
                                    Message.noLogin(e.getMessage())
                                        .$less$less(gatewayContext.getRequest().getRequestURI()));
                            ;
                            fluxSinkListener.next(
                                getWebSocketMessage(
                                    gatewayWebSocketSession.bufferFactory(), message));
                          }
                          return gatewayWebSocketSession.close();
                        }
                        if (t.getType() == WebSocketMessage.Type.PING
                            || t.getType() == WebSocketMessage.Type.PONG) {
                          WebSocketMessage pingMsg =
                              new WebSocketMessage(WebSocketMessage.Type.PING, t.getPayload());
                          gatewayWebSocketSession.heartbeat(pingMsg);
                          return sendMsg(exchange, gatewayWebSocketSession, pingMsg);
                        }
                        String json = t.getPayloadAsText();
                        t.release();
                        ServerEvent serverEvent = SocketServerEvent.getServerEvent(json);
                        ((SpringCloudGatewayHttpRequest) gatewayContext.getRequest())
                            .setRequestBody(SocketServerEvent.getMessageData(serverEvent));
                        ((SpringCloudGatewayHttpRequest) gatewayContext.getRequest())
                            .setRequestURI(serverEvent.getMethod());
                        parser.parse(gatewayContext);
                        if (gatewayContext.getResponse().isCommitted()) {
                          return sendMsg(
                              exchange,
                              gatewayWebSocketSession,
                              ((WebsocketGatewayHttpResponse) gatewayContext.getResponse())
                                  .getWebSocketMsg());
                        }
                        ServiceInstance serviceInstance = router.route(gatewayContext);
                        if (gatewayContext.getResponse().isCommitted()) {
                          return sendMsg(
                              exchange,
                              gatewayWebSocketSession,
                              ((WebsocketGatewayHttpResponse) gatewayContext.getResponse())
                                  .getWebSocketMsg());
                        }
                        WebSocketSession webSocketProxySession =
                            getProxyWebSocketSession(gatewayWebSocketSession, serviceInstance);
                        if (webSocketProxySession != null) {
                          return sendMsg(exchange, webSocketProxySession, json);
                        } else {
                          URI uri = exchange.getRequest().getURI();
                          Boolean encoded = ServerWebExchangeUtils.containsEncodedParts(uri);
                          String host;
                          int port;
                          if (StringUtils.isEmpty(serviceInstance.getInstance())) {
                            org.springframework.cloud.client.ServiceInstance service =
                                loadBalancer.choose(serviceInstance.getApplicationName());
                            host = service.getHost();
                            port = service.getPort();
                          } else {
                            String[] instanceInfo = serviceInstance.getInstance().split(":");
                            host = instanceInfo[0];
                            port = Integer.parseInt(instanceInfo[1]);
                          }
                          URI requestURI =
                              UriComponentsBuilder.fromUri(requestUrl)
                                  .host(host)
                                  .port(port)
                                  .build(encoded)
                                  .toUri();
                          HttpHeaders filtered =
                              HttpHeadersFilter.filterRequest(
                                  getHeadersFilters(websocketRoutingFilter), exchange);
                          SpringCloudHttpUtils.addIgnoreTimeoutSignal(filtered);
                          return webSocketClient.execute(
                              requestURI,
                              filtered,
                              new WebSocketHandler() {
                                public Mono<Void> handle(WebSocketSession proxySession) {
                                  setProxyWebSocketSession(
                                      user, serviceInstance, gatewayWebSocketSession, proxySession);
                                  Mono<Void> proxySessionSend =
                                      sendMsg(exchange, proxySession, json);
                                  proxySessionSend.subscribe();
                                  return getProxyWebSocketSession(
                                          gatewayWebSocketSession, serviceInstance)
                                      .receive()
                                      .doOnNext(WebSocketMessage::retain)
                                      .doOnNext(fluxSinkListener::next)
                                      .then();
                                }

                                public List<String> getSubProtocols() {
                                  return collectedProtocols;
                                }
                              });
                        }
                      })
                  .doOnComplete(fluxSinkListener::complete)
                  .doOnNext(Mono::subscribe)
                  .subscribe();
              return gatewayWebSocketSession.send(receives);
            }

            public List<String> getSubProtocols() {
              return collectedProtocols;
            }
          });
    } else {
      return chain.filter(exchange);
    }
  }

  public int getOrder() {
    return websocketRoutingFilter.getOrder() - 1;
  }

  interface FluxSinkListener<T> {
    void setFluxSink(FluxSink<T> fluxSink);

    void next(T t);

    void complete();
  }
}
