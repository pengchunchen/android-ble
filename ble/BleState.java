package com.android.livedemo.ble;

/**
 * Created by Administrator on 2017/11/21.
 */

public enum BleState {
    UNKNOWN,//未知状态
    IDLE,//不可用状态
    SCANNING,//扫描中
    BLUETOOTH_OFF,//手机蓝牙关闭
    CONNECTING,//连接中
    CONNECTED,//连接上
    DISCONNECTING,//正在断开
    DISCONNECTED//已断开
}
