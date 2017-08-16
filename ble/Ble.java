package com.weex.sample.extend.ble;

/**
 * Created by Administrator on 2017/8/15.
 */

public interface Ble {
    /**
     * 检测手机蓝牙是否打开
     * @return true打开 false调用者自行开启
     */
    boolean checkBleOpen();

    /**
     * 开始扫描蓝牙设备
     * @param bleName 设备名称
     * @param scanResultCallBack 扫描接口回调
     */
    void startScanBle(String bleName, onBleScanResultCallBack scanResultCallBack);

    /**
     * 停止扫描
     */
    void stopScanBle();

    /**
     * 连接设备
     * @return
     */
    boolean connectBle();

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
