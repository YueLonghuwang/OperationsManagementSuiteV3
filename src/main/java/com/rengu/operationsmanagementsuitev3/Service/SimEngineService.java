package com.rengu.operationsmanagementsuitev3.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.protobuf.InvalidProtocolBufferException;
import com.rengu.operationsmanagementsuitev3.Entity.SimData;
import com.rengu.operationsmanagementsuitev3.Entity.SimEntity;
import com.rengu.operationsmanagementsuitev3.Utils.ApplicationConfig;
import com.rengu.operationsmanagementsuitev3.Utils.JsonUtils;
import io.nats.client.Connection;
import io.nats.client.Dispatcher;
import io.nats.client.Message;
import io.nats.client.Nats;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.rengu.operationsmanagementsuitev3.Enums.SimCmd.*;

/**
 * @Author YJH
 * @Date 2019/3/19 17:39
 */

@Slf4j
@Service
public class SimEngineService {
    public static final String CMD_SUBJECT = "CMD_TOPIC1";
    public static final String ENTITY_LIST_TOPIC = "ENTITY_LIST_TOPIC";
    public static final String EVENT_LIST_TOPIC = "EVENT_LIST_TOPIC";

    private Connection connectNats(String natsIp) throws IOException, InterruptedException {
        return Nats.connect("nats://" + natsIp + ":4222");
    }


    public void getSimEngineCmd(String simCmd) throws IOException, InterruptedException {
        SimData.DSERECCommand.Builder builder = SimData.DSERECCommand.newBuilder();
        switch (simCmd) {
            case "start":
                builder.setCmdType(CMD_ENGINE_START.ordinal());
                break;
            case "suspend":
                builder.setCmdType(CMD_ENGINE_SUSPEND.ordinal());
                break;
            case "recover":
                builder.setCmdType(CMD_ENGINE_RECOVER.ordinal());
                break;
            case "stop":
                builder.setCmdType(CMD_ENGINE_STOP.ordinal());
                break;
            default:
                throw new RuntimeException(simCmd + CMD_TYPE_ERROR);
        }
        SimData.DSERECCommand dserecCommand = builder.build();
        sendSimCmd(CMD_SUBJECT, dserecCommand);
    }

    //  发送消息给引擎
    private void sendSimCmd(String subject, SimData.DSERECCommand dserecCommand) throws IOException, InterruptedException {
        Connection connection = connectNats(ApplicationConfig.NATS_SERVER_IP);
        connection.publish(subject, dserecCommand.toByteArray());
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

    //  订阅实体类信息
    private void entityMessageHandler(Message message) {
        try {
            SimData.DSERECEntityRecord dserecEntityRecord = SimData.DSERECEntityRecord.parseFrom(message.getData());
            List<SimEntity> simEntityList = new ArrayList<>();

            for (SimData.DSERECEntity dserecEntity : dserecEntityRecord.getEntityListList()) {
                SimEntity simEntity = new SimEntity();
                simEntity.setEntityID(dserecEntity.getEntityID());
                simEntity.setName(dserecEntity.getName());
                simEntity.setItemClass(dserecEntity.getItemClass());
                simEntity.setEntityType(dserecEntity.getEntityType());
                simEntity.setEquipmentType(dserecEntity.getEquipmentType());
                simEntity.setAtt(dserecEntity.getAtt());
                simEntity.setLLAPositionLon(dserecEntity.getLLAPositionLon());
                simEntity.setLLAPositionLat(dserecEntity.getLLAPositionLat());
                simEntity.setLLAPositionAlt(dserecEntity.getLLAPositionAlt());
                simEntity.setVelocityX(dserecEntity.getVelocityX());
                simEntity.setVelocityY(dserecEntity.getVelocityY());
                simEntity.setVelocityZ(dserecEntity.getVelocityZ());
                simEntity.setPitch(dserecEntity.getPitch());
                simEntity.setYaw(dserecEntity.getYaw());
                simEntity.setRoll(dserecEntity.getRoll());
                simEntity.setLive(dserecEntity.getIsLive());
                simEntity.setHealthPoint(dserecEntity.getHealthPoint());
                simEntity.setEntityParam(dserecEntity.getEntityParam());
                simEntity.setCommanderID(dserecEntity.getCommanderID());
                simEntity.setCommander(dserecEntity.getCommander());
                simEntityList.add(simEntity);
            }
            log.info(JsonUtils.toJson(simEntityList));
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        } catch (JsonProcessingException e) {
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

    //  订阅事件信息
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
