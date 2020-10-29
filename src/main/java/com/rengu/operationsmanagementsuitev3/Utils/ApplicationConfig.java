package com.rengu.operationsmanagementsuitev3.Utils;

import org.apache.commons.io.FileUtils;

import java.io.File;

/**
 * @program: OperationsManagementSuiteV3
 * @author: hanchangming
 * @create: 2018-08-22 16:59
 **/
public class ApplicationConfig {

    // 默认角色
    public static final String DEFAULT_ADMIN_ROLE_NAME = "admin";
    public static final String DEFAULT_USER_ROLE_NAME = "user";
    // 默认管理员用户
    public static final String DEFAULT_ADMIN_USERNAME = "admin";
    public static final String DEFAULT_ADMIN_PASSWORD = "admin";

    // 服务器连接地址、端口
    public static final int TCP_RECEIVE_PORT = 6005;
    public static final int UDP_RECEIVE_PORT = 6004;
    public static final int UDP_SEND_PORT = 3087;
    public static final int TCP_DEPLOY_PORT = 3088;
    public static final String SERVER_CAST_ADDRESS = "224.10.10.15";
    public static final int SERVER_BROAD_CAST_PORT = 3086;
    public static final int SERVER_MULTI_CAST_PORT = 3086;

    // 文件块保存路径
    public static final String CHUNKS_SAVE_PATH = FormatUtils.formatPath(FileUtils.getTempDirectoryPath() + File.separator + "OMS" + File.separator + "CHUNKS");
    // 文件保存路径
    public static final String FILES_SAVE_PATH = FormatUtils.formatPath(FileUtils.getUserDirectoryPath() + File.separator + "OMS" + File.separator + "FILES");

    // 扫描超时时间
    public static final long SCAN_TIME_OUT = 1000 * 5;
    // 部署回复超时时间
    public static final long REPLY_TIME_OUT = 1000 * 8;
    // 设备在线心跳检测间隔
    public static final long HEART_BEAT_CHECK_TIME = 1000 * 5;
    // 文件读取缓冲区大小
    public static final int FILE_READ_BUFFER_SIZE = 10240;
}