package com.rengu.operationsmanagementsuitev3.Service;

import com.google.protobuf.InvalidProtocolBufferException;
import com.rengu.operationsmanagementsuitev3.Entity.SimData;
import com.rengu.operationsmanagementsuitev3.Utils.ApplicationConfig;
import io.nats.client.Connection;
import io.nats.client.Dispatcher;
import io.nats.client.Message;
import io.nats.client.Nats;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * @Author YJH
 * @Date 2019/3/19 17:39
 */

@Slf4j
@Service
public class SimEngineService {

    public static final String ENTITY_LIST_TOPIC = "ENTITY_LIST_TOPIC";
    public static final String EVENT_LIST_TOPIC = "EVENT_LIST_TOPIC";

    private Connection connectNats(String natsIp) throws IOException, InterruptedException {
        return Nats.connect("nats://" + natsIp + ":4222");
    }

    @Async
    public void subscribeEntityMessage() {
        try {
            log.info("OMS服务器-引擎实体信息监听线程：" + ENTITY_LIST_TOPIC + "@" + ApplicationConfig.NATS_SERVER_IP);
            Connection connection = connectNats(ApplicationConfig.NATS_SERVER_IP);
            Dispatcher dispatcher = connection.createDispatcher(this::entityMessageHandler);
            dispatcher.subscribe(ENTITY_LIST_TOPIC);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void entityMessageHandler(Message message) {
        try {
            SimData.DSERECEntityRecord dserecEntityRecord = SimData.DSERECEntityRecord.parseFrom(message.getData());
            for (SimData.DSERECEntity dserecEntity : dserecEntityRecord.getEntityListList()) {
                // todo 解析实体信息
                log.info(dserecEntity.getName());
            }
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    @Async
    public void subscribeEventMessage() {
        try {
            log.info("OMS服务器-引擎事件信息监听线程：" + EVENT_LIST_TOPIC + "@" + ApplicationConfig.NATS_SERVER_IP);
            Connection connection = connectNats(ApplicationConfig.NATS_SERVER_IP);
            Dispatcher dispatcher = connection.createDispatcher(this::eventMessageHandler);
            dispatcher.subscribe(EVENT_LIST_TOPIC);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void eventMessageHandler(Message message) {
        try {
            SimData.DSERECEventRecord dserecEventRecord = SimData.DSERECEventRecord.parseFrom(message.getData());
            for (SimData.DSERECEvent dserecEvent : dserecEventRecord.getEventListList()) {
                // todo 解析事件信息
                log.info(dserecEvent.getEventName());
            }
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }
}
