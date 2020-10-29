package com.rengu.operationsmanagementsuitev3.Entity;

import com.rengu.operationsmanagementsuitev3.Utils.FormatUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

@Data
@Slf4j
public class DeployPackaegEntity {

    // 当前包序号-4（序号为0都是检验的，从1开始发）
    private int serialNum;
    // 部署目标路径-128
    private String targetPath;
    // 文件MD5-34
    private String md5;
    // 当前正文长度-4
    private int dataSize;
    // 总文件大小-8
    private long totalSize;
    // 正文部分
    private byte[] data;

    public DeployPackaegEntity() {
        this.serialNum = -1;
        this.totalSize = 0;
        this.targetPath = "";
        this.md5 = "";
    }

    public DeployPackaegEntity(long totalSize, String targetPath, String md5) {
        this.serialNum = 0;
        this.totalSize = totalSize;
        this.targetPath = targetPath;
        this.md5 = md5;
    }

    public DeployPackaegEntity(int serialNum, long totalSize, byte[] data) {
        this.serialNum = serialNum;
        this.dataSize = data.length;
        this.totalSize = totalSize;
        this.data = data;
    }

    public byte[] getCheckBuffer() throws UnsupportedEncodingException {
        ByteBuffer byteBuffer = ByteBuffer.allocate(4 + 256 + 34 + 8);
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        byteBuffer.putInt(serialNum);
        byteBuffer.put(FormatUtils.getBytesFormString(targetPath, 256));
        byteBuffer.put(FormatUtils.getBytesFormString(md5, 34));
        byteBuffer.putLong(totalSize);
//        log.info("发送校验包数据：序号：" + serialNum + ",总大小:" + totalSize + ",Path:" + targetPath + ",MD5:" + md5 + ",校验包总长度：" + byteBuffer.capacity());
        return byteBuffer.array();
    }

    public byte[] getDataBuffer() {
        ByteBuffer byteBuffer = ByteBuffer.allocate(4 + 4 + 8 + data.length);
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        byteBuffer.putInt(serialNum);
        byteBuffer.putInt(dataSize);
        byteBuffer.putLong(totalSize);
        byteBuffer.put(data);
//        log.info("发送数据包数据：序号：" + serialNum + ",包大小:" + dataSize + ",总大小:" + totalSize + ",数据包总长度:" + byteBuffer.capacity());
        return byteBuffer.array();
    }

    public byte[] getFinishBuffer() throws UnsupportedEncodingException {
//        log.info("发送部署结束包数据");
        return getCheckBuffer();
    }
}