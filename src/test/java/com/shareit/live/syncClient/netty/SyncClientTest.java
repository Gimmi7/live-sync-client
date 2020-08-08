package com.shareit.live.syncClient.netty;

import com.google.protobuf.ByteString;
import com.shareit.live.framework.pojo.constants.RedisConstants;
import com.shareit.live.proto.*;
import io.lettuce.core.cluster.api.sync.RedisAdvancedClusterCommands;
import lombok.SneakyThrows;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@SpringBootTest
class SyncClientTest {
    @Autowired
    private SyncClient syncClient;
    @Autowired
    private RedisAdvancedClusterCommands<String, String> commands;

    private static final String local = "ws://localhost:2021/live/bf784e9fa0cb34eaa8140cbcf8373989/K6to/1";
    private static final String dev = "wss://imentry-dev.slivee.com:2021/live/bf784e9fa0cb34eaa8140cbcf8373989/K6to/1";
    private static final String test = "wss://imentry-test.slivee.com:2021/live/4cbd43168ec536bfbaa35eb365a92c9f/6VbMF/1";
    private static final String prod = "wss://imentry.slivee.com:2021/live/";

    private static final String devUserId = "K6to";
    private static final String testUserId = "6VbMF";
    private static final String prodUserId = "K6to";

    private static final String userId = devUserId;
    private static final String roomId = "test_room";
    private static final String streamId = "wangcy_stream";

    @BeforeEach
    private void initClient() throws URISyntaxException {
        syncClient.connect(dev);
    }

    @SneakyThrows
    @Test
    void clientTest() {
        MsgWrapper enterRoomRsp = syncClient.sendMsg(this.enterRoom());
        System.out.println(enterRoomRsp);

        this.addSteamToOnlineSet();//todo

//        for (int i = 0; i < 10; i++) {
//            MsgWrapper sendCommentRsp = syncClient.sendMsg(this.sendComment());
//            System.out.println(sendCommentRsp);
//        }
        TimeUnit.MINUTES.sleep(5);

    }

    private MsgWrapper sendComment() {
        String content = RandomStringUtils.randomAlphabetic(10);
        LiveCommentMsg commentMsg = LiveCommentMsg.newBuilder()
                .setUser(this.buildUser())
                .setRoomId(roomId)
                .setStreamId(streamId)
                .setTimestamp(System.currentTimeMillis())
                .setContent(content)
                .setCountry("IN")
                .build();
        return this.buildReqWrapper(ApiKey.API_KEY_SEND_COMMENT, commentMsg.toByteString(), 2);
    }

    private MsgWrapper enterRoom() {
        EnterRoomMsg enterRoomMsg = EnterRoomMsg.newBuilder()
                .setRoomId(roomId)
                .setStreamId(streamId)
                .setUser(this.buildUser())
                .build();
        MsgWrapper wrapper = MsgWrapper.newBuilder()
                .setClientVersion(4050603)
                .setSeq(0)
                .setTimestamp(System.currentTimeMillis())
                .setAction(MsgAction.ENTER_ROOM)
                .setUserId(userId)
                .setAppId(AppId.SHAREIT)
                .setBody(enterRoomMsg.toByteString())
                .build();
        return wrapper;
    }

    private User buildUser() {
        return User.newBuilder()
                .setUid(userId)
                .setNickName("Jimmy")
                .setUserType(UserType.USER_TYPE_PHONE)
                .build();
    }

    private MsgWrapper buildReqWrapper(ApiKey apiKey, ByteString reqBody, int seq) {
        ReqMsg reqMsg = ReqMsg.newBuilder()
                .setApiKey(apiKey)
                .setBody(reqBody)
                .build();
        MsgWrapper wrapper = MsgWrapper.newBuilder()
                .setClientVersion(4050603)
                .setSeq(seq)
                .setTimestamp(System.currentTimeMillis())
                .setAction(MsgAction.REQ)
                .setUserId(userId)
                .setAppId(AppId.SHAREIT)
                .setBody(reqMsg.toByteString())
                .build();
        return wrapper;
    }


    @Test
    void clearTestInfoDev() {
        List<String> list = new ArrayList<>();
        list.add(RedisConstants.STREAM_AUDIENCE_KEY_PREFIX + streamId);
        list.add(RedisConstants.STREAM_AUDIENCE_BIT_PREFIX + streamId);
        list.add(RedisConstants.STREAM_LIKE_KEY_PREFIX + streamId);

        list.add(RedisConstants.STREAM_AUDIENCE_KEY_PREFIX_FAKE + streamId);
        list.add(RedisConstants.STREAM_AUDIENCE_BIT_PREFIX_FAKE + streamId);
        list.add(RedisConstants.STREAM_LIKE_KEY_PREFIX_FAKE + streamId);

        commands.del(list.toArray(new String[0]));
        String member = roomId + "/" + streamId;
        commands.srem(RedisConstants.ONLINE_STREAM_SET, member);
    }

    private void addSteamToOnlineSet() {
        String member = roomId + "/" + streamId;
        commands.sadd(RedisConstants.ONLINE_STREAM_SET, member);
    }
}