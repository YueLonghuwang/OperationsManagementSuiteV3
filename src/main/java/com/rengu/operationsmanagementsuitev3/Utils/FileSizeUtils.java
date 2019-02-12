package com.rengu.operationsmanagementsuitev3.Utils;

import org.apache.commons.io.FileUtils;

import java.text.DecimalFormat;

public class FileSizeUtils {

    public static String getFileSize(long size) {
        StringBuffer stringBuffer = new StringBuffer();
        DecimalFormat format = new DecimalFormat("###.0");
        if (size >= FileUtils.ONE_GB) {
            double i = (size / (1024.0 * 1024.0 * 1024.0));
            stringBuffer.append(format.format(i)).append("GB");
        } else if (size >= FileUtils.ONE_MB) {
            double i = (size / (1024.0 * 1024.0));
            stringBuffer.append(format.format(i)).append("MB");
        } else if (size >= FileUtils.ONE_KB) {
            double i = (size / (1024.0));
            stringBuffer.append(format.format(i)).append("KB");
        } else if (size < 1024) {
            if (size <= 0) {
                stringBuffer.append("0B");
            } else {
                stringBuffer.append((int) size).append("B");
            }
        }
        return stringBuffer.toString();
    }
}