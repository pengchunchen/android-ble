package com.android.livedemo.ble;

import android.bluetooth.BluetoothDevice;

/**
 * Created by Administrator on 2017/8/15.
 */

public interface onBleScanResultCallBack {
    /**
     * @return 扫描到的蓝牙设备  null--没有扫描到设备，扫描超时
     */
    void scanResult(BluetoothDevice device);
}
