package com.rengu.operationsmanagementsuitev3.Entity;

import com.rengu.operationsmanagementsuitev3.Utils.FormatUtils;
import lombok.Data;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

@Data
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

    public DeployPackaegEntity(String targetPath, String md5) {
        this.serialNum = 0;
        this.targetPath = targetPath;
        this.md5 = md5;
    }

    public DeployPackaegEntity(int serialNum, long totalSize, byte[] data) {
        this.serialNum = serialNum;
        this.dataSize = data.length;
        this.totalSize = totalSize;
        this.data = data;
    }

    public byte[] getCheckBuffer() {
        ByteBuffer byteBuffer = ByteBuffer.allocate(4 + 128 + 34);
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        byteBuffer.putInt(serialNum);
        byteBuffer.put(FormatUtils.getBytesFormString(targetPath, 128));
        byteBuffer.put(FormatUtils.getBytesFormString(md5, 34));
        return byteBuffer.array();
    }

    public byte[] getDataBuffer() {
        ByteBuffer byteBuffer = ByteBuffer.allocate(4 + 4 + 8 + data.length);
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        byteBuffer.putInt(serialNum);
        byteBuffer.putInt(dataSize);
        byteBuffer.putLong(totalSize);
        byteBuffer.put(data);
        return byteBuffer.array();
    }
}