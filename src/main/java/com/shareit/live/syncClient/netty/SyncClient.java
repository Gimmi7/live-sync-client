package com.shareit.live.syncClient.netty;

import com.shareit.live.proto.MsgWrapper;
import com.shareit.live.syncClient.handler.ChildChannelHandler;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URISyntaxException;

@Service
@Slf4j
public class SyncClient {
    @Autowired
    private ChildChannelHandler childChannelHandler;

    private Channel channel;

    public MsgWrapper syncWrapper;

    public boolean loginSuccess;

    public void connect(String url) throws URISyntaxException {
        NettyClient nettyClient = new NettyClient();
        URI uri = new URI(url);
        Channel channel = nettyClient.runClient(childChannelHandler, uri);
        this.channel = channel;
    }

    public MsgWrapper sendMsg(MsgWrapper wrapper) {
        ByteBuf byteBuf = channel.alloc().buffer();
        byteBuf.writeBytes(wrapper.toByteArray());
        BinaryWebSocketFrame bwf = new BinaryWebSocketFrame(byteBuf);
        this.channel.writeAndFlush(bwf);
        synchronized (this) {
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return this.syncWrapper;
    }

}
