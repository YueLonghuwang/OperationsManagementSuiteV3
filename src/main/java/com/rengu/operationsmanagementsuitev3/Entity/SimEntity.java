package com.rengu.operationsmanagementsuitev3.Entity;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author YJH
 * @Date 2019/3/20 10:57
 */

@Data
public class SimEntity implements Serializable {

    private String entityID;
    private String name;
    private String itemClass;
    private int entityType;
    private int equipmentType;
    private int att;
    private double lLAPositionLon;
    private double lLAPositionLat;
    private double lLAPositionAlt;
    private double velocityX;
    private double velocityY;
    private double velocityZ;
    private double pitch;
    private double yaw;
    private double roll;
    private boolean isLive;
    private double healthPoint;
    private String entityParam;
    private int commanderID;
    private String commander;
}
