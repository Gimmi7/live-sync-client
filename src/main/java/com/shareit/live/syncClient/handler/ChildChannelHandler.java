package com.shareit.live.syncClient.handler;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.EmptyHttpHeaders;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.WebSocketClientProtocolHandler;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.concurrent.TimeUnit;

@Component
public class ChildChannelHandler extends ChannelInitializer<SocketChannel> {

    @Autowired
    private DataChannelHandler dataChannelHandler;

    private WebSocketClientProtocolHandler wsHandler;

    private URI uri;

    private boolean wss = false;

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        if (wss) {
            final SslContext sslCtx = SslContextBuilder.forClient().trustManager(InsecureTrustManagerFactory.INSTANCE).build();
            ch.pipeline().addLast(sslCtx.newHandler(ch.alloc(), uri.getHost(), uri.getPort()));
        }
        //webSocket基于http协议，需要使用http编解码器
        ch.pipeline().addLast(new HttpClientCodec());
        //分块写数据，防止发送大文件时导致内存溢出，channel.write(new ChunkedFile(new File("bigFile.mkv")))
        ch.pipeline().addLast(new ChunkedWriteHandler());
        //将HttpMessage和HttpContent聚合到一个完整的FullHttpRequest或FullHttpResponse中，需要放到HttpServerCodec后面
        ch.pipeline().addLast(new HttpObjectAggregator(10240));
        //webSocket数据压缩拓展，当添加这个处理器时WebSocketClientProtocolHandler的allowExtension需要设置true
//        ch.pipeline().addLast(WebSocketClientCompressionHandler.INSTANCE);
        //WebSocketServerProtocolHandler处理了握手以及 Close,Ping,Pong控制帧的处理
        ch.pipeline().addLast(wsHandler);
        //自定义二进制消息处理器
        ch.pipeline().addLast(dataChannelHandler);


        //中断空闲连接
        ch.pipeline().addLast(new ReadTimeoutHandler(1, TimeUnit.HOURS));
        ch.pipeline().addLast(new WriteTimeoutHandler(1, TimeUnit.HOURS));
    }

    public void setWsUri(URI uri) {
        this.uri = uri;
        if (uri.getScheme().equals("wss")) {
            wss = true;
        }
        WebSocketClientProtocolHandler handler = new WebSocketClientProtocolHandler(uri, WebSocketVersion.V13, null, true, EmptyHttpHeaders.INSTANCE, 10485760);
        this.wsHandler = handler;
    }

    public WebSocketClientProtocolHandler getWsHandler() {
        return wsHandler;
    }
}
