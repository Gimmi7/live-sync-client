package com.shareit.live.syncClient.handler;

import com.shareit.live.proto.MsgAction;
import com.shareit.live.proto.MsgWrapper;
import com.shareit.live.syncClient.netty.SyncClient;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DataChannelHandler extends ChannelHandlerBase {
    @Autowired
    private SyncClient syncClient;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, BinaryWebSocketFrame msg) throws Exception {
        ByteBuf byteBuf = msg.content();
        MsgWrapper wrapper = MsgWrapper.parseFrom(byteBuf.nioBuffer());
        if (wrapper.getAction().equals(MsgAction.ENTER_ROOM)
                || wrapper.getAction().equals(MsgAction.EXIT_ROOM)
                || wrapper.getAction().equals(MsgAction.RSP)) {
            syncClient.syncWrapper = wrapper;
            synchronized (syncClient) {
                syncClient.notify();
            }
        } else {
            String out = "[[------------------------------\n" +
                    wrapper.toString() +
                    "\n------------------------------]]";
            System.out.println(out);
        }
    }
}
