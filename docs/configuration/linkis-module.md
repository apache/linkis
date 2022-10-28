## linkis-module configure


| Module Name (Service Name) | Parameter Name | Default Value | Description |Used|
| -------- | -------- | ----- |----- |  -----   |
|linkis-module|wds.linkis.server.component.exclude.packages| | exclude.packages |true|
|linkis-module|wds.linkis.server.component.exclude.classes| |exclude.classes|true|
|linkis-module|wds.linkis.server.component.exclude.annotation| |exclude.annotation|true|
|linkis-module|wds.linkis.server.spring.application.listeners| | application.listeners  |true|
|linkis-module|wds.linkis.server.version| |version|true|
|linkis-module|wds.linkis.crypt.key| bdp-for-server | crypt.key  |true|
|linkis-module|wds.linkis.ticket.header| bfs_ | ticket.header  |true|
|linkis-module|wds.linkis.test.user| |test.user|true|
|linkis-module|wds.linkis.server.home|   | server.home  |true|
|linkis-module|wds.linkis.server.distinct.mode|  |distinct.mode|true|
|linkis-module|wds.linkis.server.socket.mode| |socket.mode|true|
|linkis-module|wds.linkis.server.ident.string|true| server.ident.string |false|
|linkis-module|wds.linkis.server.jetty.name| |jetty.name |false|
|linkis-module|wds.linkis.server.address|   |server.address  |true|
|linkis-module|wds.linkis.server.port| 20303|server.port |false|
|linkis-module|wds.linkis.server.security.filter | |security.filter  |true|
|linkis-module|wds.linkis.server.security.referer.validate|  SSLv2,SSLv3 | security.referer.validate  |false|
|linkis-module|wds.linkis.server.security.ssl.keystore.path| |keystore.path|false|
|linkis-module|wds.linkis.server.security.ssl.keystore.type| JKS  | keystore.type  |false|
|linkis-module|wds.linkis.server.security.ssl.cipher.suites|  |cipher.suites|false|
|linkis-module|wds.linkis.server.context.path| /|context.path|true|
|linkis-module|wds.linkis.server.restful.uri|/api/rest_j/+ BDP_SERVER_VERSION| restful.uri|true|
|linkis-module|wds.linkis.server.user.restful.uri|/api/rest_j/" + BDP_SERVER_VERSION + "/user |user.restful.uri |true|
|linkis-module|wds.linkis.server.user.restful.uri.pass.auth|   |restful.uri.pass.auth |true|
|linkis-module|wds.linkis.server.user.security.ssl.uri| |security.ssl.uri|true|
|linkis-module|wds.linkis.server.socket.uri |/ws |server.socket.uri  |true|
|linkis-module|wds.linkis.server.socket.login.uri|/ws/user/login |socket.login.uri |true|
|linkis-module|wds.linkis.server.war|   |server.war |true|
|linkis-module|wds.linkis.server.war.tempdir|  |server.war.tempdir|true|
|linkis-module|wds.linkis.server.default.dir.allowed |false | default.dir.allowed  |true|
|linkis-module|wds.linkis.server.web.session.timeout|    | session.timeout |true|
|linkis-module|wds.linkis.server.event.queue.size| 5000 |queue.size|true|
|linkis-module|wds.linkis.server.event.consumer.thread| 10 | event.consumer.thread |true|
|linkis-module|wds.linkis.server.event.consumer.thread.max.free|  |thread.max.free|true|
|linkis-module|wds.linkis.server.socket.text.message.size.max| 1024000 |message.size.max|true|
|linkis-module| wds.linkis.server.restful.scan.packages |  | restful.scan.packages|false|
|linkis-module|wds.linkis.server.restful.register.classes|  |restful.register.classes |true|
|linkis-module|wds.linkis.is.gateway|  false |gateway |true|
|linkis-module|wds.linkis.server.web.allowmethod| POST,GET,OPTIONS,PUT,HEAD,DELETE |web.allowmethod|true|
|linkis-module|linkis.username.suffix.name |_c |username.suffix.name |true|
|linkis-module| wds.linkis.session.ticket.key |linkis_user_session_ticket_id_v1 | ticket.key |true|
|linkis-module|wds.linkis.session.proxy.user.ticket.key|linkis_user_session_proxy_ticket_id_v1  |ticket.key |true|
|linkis-module|wds.linkis.proxy.ticket.header.crypt.key|  linkis-trust-key |crypt.key |true|
|linkis-module|wds.linkis.proxy.ticket.header.crypt.key| bfs_ | crypt.key|true|
