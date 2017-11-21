package com.android.livedemo.ble;

/**
 * Created by Administrator on 2017/8/16.
 */

public class BleCmdType {

    public static final int BleCmd_GetDeviceBattery = 1;//获取设备电量
    public static final int BleCmd_GetDeviceTime = 2;//获取设备时间
    public static final int BleCmd_GetDeviceSensorCount = 3;//获取设备传感器个数
    public static final int BleCmd_GetDeviceSaveWord = 4;//获取设备保留字
    public static final int BleCmd_GetDeviceTimeInterval = 5;//获取设备读取数据时间间隔

    public static final int BleCmd_SetDeviceTimeInterval = 1000;//设置设备读取数据时间间隔
    public static final int BleCmd_SetDeviceSensorCount = 1001;//设置设备传感器个数
    public static final int BleCmd_SetDeviceTime = 1002;//设置设备时间
    public static final int BleCmd_SetDeviceCleanFlush = 1003;//清空设备flush
    public static final int BleCmd_SetDeviceSaveWord = 1004;//设置设备保留字

    public static final int BleCmd_ReadHistoryData = 2000;//读取设备历史数据
    public static final int BleCmd_ReadCurrentData = 2001;//读取设备实时数据

}
