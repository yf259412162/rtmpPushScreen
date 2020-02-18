package com.example.rtmp;

public class RtmpClient {
    static {
        System.loadLibrary("rtmp-lib");
    }

    /**
     * 打开RTMP流
     * @param url  推流地址
     * @param isPublishMode
     * @return
     */
    public static native long open(String url, boolean isPublishMode);

    /**
     * 写入data数据
     * @param rtmpPointer
     * @param data
     * @param size
     * @param type
     * @param ts
     * @return
     */
    public static native int write(long rtmpPointer, byte[] data, int size, int type, int ts);

    /**
     * 关闭RTMP流
     * @param rtmpPointer
     * @return
     */
    public static native int close(long rtmpPointer);

    /**
     * 获得推流地址
     * @param rtmpPointer
     * @return
     */
    public static native String getIpAddr(long rtmpPointer);
}
