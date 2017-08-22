package com.weex.sample.extend.ble;

import android.bluetooth.BluetoothDevice;

/**
 * Created by Administrator on 2017/8/15.
 */

public interface Ble {
    /**
     * 设置蓝牙的服务号，读特征号，写特征号，通知号
     * @param serviceID
     * @param recID
     * @param sendID
     * @param notifyID
     */
    void setCharUUID(String serviceID,String recID,String sendID,String notifyID);
    /**
     * 检测手机蓝牙是否打开
     * @return true打开 false调用者自行开启
     */
    boolean checkBleOpen();

    /**
     * 开始扫描蓝牙设备
     * @param scanResultCallBack 扫描接口回调
     */
    void startScanBle(onBleScanResultCallBack scanResultCallBack);

    /**
     * 停止扫描
     */
    void stopScanBle();

    /**
     * 连接设备
     * @return
     */
    boolean connectBle(BluetoothDevice bluetoothDevice,onConnectResultCallBack onConnectResultCallBack);

    /**
     * 写数据
     * @param cmd 需要写入的消息号 详见BleCmdType类
     * @param responseCallBack 蓝牙结果回调
     * @return
     */
    boolean writeBle(String cmd, onBleResponseCallBack responseCallBack);

    /**
     * 断开蓝牙连接
     * @return true断开成功
     */
    boolean closeBleConnect();

    /**
     * 获取蓝牙连接状态
     * @return 1:连接
     */
    int getBleState();
}
