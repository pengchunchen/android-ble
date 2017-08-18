package com.weex.sample.extend.ble;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.Handler;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by Administrator on 2017/8/15.
 */

public class BleModule implements Ble {

    private static String TAG = BleModule.class.getSimpleName();
    private boolean isScan = false;//扫描标记

    private Context mContext;
    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothDevice mBluetoothDevice;
    private BluetoothGatt mBluetoothGatt;

    private final int STOP_LESCAN = 0x1;
    public static BluetoothState mState = BluetoothState.UNKNOWN;

    public static String DATA_SERVICE_UUID;     //血压计提供的SERVICE
    public static String REC_PKG_CHAR_UUID;     //接受数据要使用的characteristic的UUID
    public static String SEND_PKG_CHAR_UUID;    //SEND_PKG_CHAR_UUID
    public static String CLIENT_CHARACTERISTIC_CONFIG;   //设置蓝牙通知的UUID

    //回调
    //扫描结果
    private onBleScanResultCallBack scanResultCallBack;
    //返回回调
    private onBleResponseCallBack bleResponseCallBack;

    public BleModule(Context context) {
        this.mContext = context;
        mBluetoothManager = (BluetoothManager) mContext.getSystemService(Context.BLUETOOTH_SERVICE);
        if (mBluetoothManager != null) {
            mBluetoothAdapter = mBluetoothManager.getAdapter();
        }
    }

    @Override
    public void setCharUUID(String serviceID, String recID, String sendID, String notifyID) {
        DATA_SERVICE_UUID = "0000" + serviceID + "-0000-1000-8000-00805F9B34FB";
        REC_PKG_CHAR_UUID = "0000" + recID + "-0000-1000-8000-00805F9B34FB";
        SEND_PKG_CHAR_UUID = "0000" + sendID + "-0000-1000-8000-00805F9B34FB";
        CLIENT_CHARACTERISTIC_CONFIG = "0000" + notifyID + "-0000-1000-8000-00805f9b34fb";
    }

    @Override
    public boolean checkBleOpen() {
        if (mBluetoothAdapter != null) {
            if (!mBluetoothAdapter.isEnabled()) {
                mState = BluetoothState.BLUETOOTH_OFF;
                return false;
            }
        }
        return true;
    }

    @Override
    public void startScanBle(onBleScanResultCallBack scanResultCallBack) {
        isScan = true;
        this.scanResultCallBack = scanResultCallBack;
        mBluetoothAdapter.startLeScan(mLeScanCallback);
        mState = BluetoothState.SCANNING;
        mHandler.sendEmptyMessageDelayed(STOP_LESCAN, 15 * 1000);  //这个搜索10秒，如果搜索不到则停止搜索
    }

    private Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case STOP_LESCAN: {
                    isScan = false;
                    scanResultCallBack.scanResult(null);
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    Log.i(TAG, "Scan time is up");
                }
                break;
            }
        }
    };

    @Override
    public void stopScanBle() {
        if (isScan) {
            isScan = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
            mHandler.removeMessages(STOP_LESCAN);
        }
    }

    /**
     * 搜索结果回调
     */
    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice device, int arg1, byte[] arg2) {
            if (device.getName() == null) {
                return;
            }
            scanResultCallBack.scanResult(device);
        }
    };

    @Override
    public boolean connectBle(BluetoothDevice bluetoothDevice) {
        this.mBluetoothDevice = bluetoothDevice;
        if (mBluetoothDevice == null) {
            return false;
        }
        mBluetoothGatt = mBluetoothDevice.connectGatt(mContext, false, mGattCallback);  //mGattCallback为回调接口
        if (mBluetoothGatt != null) {
            return mBluetoothGatt.connect();
        } else {
            return false;
        }
    }

    @Override
    public boolean writeBle(String cmd, onBleResponseCallBack responseCallBack) {
        this.bleResponseCallBack = responseCallBack;
        if (cmd != null) {
            return write(hexStringToBytes(cmd));
        }
        return false;
    }

    /**
     * 连接结果回调
     */
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED && status == BluetoothGatt.GATT_SUCCESS) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                mState = BluetoothState.CONNECTED;
                gatt.discoverServices(); //执行到这里其实蓝牙已经连接成功了
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                mState = BluetoothState.DISCONNECTED;
                closeBleConnect();
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.i(TAG, "onServicesDiscovered");
                openBLEDataIn(); //打开BLE设备的 notify 通道
            } else {
                Log.i(TAG, "onServicesDiscovered status------>" + status);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            Log.d(TAG, "onCharacteristicRead------>" + bytesToHexString(characteristic.getValue()));
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            byte[] value = characteristic.getValue();
            Log.d(TAG, "onCharacteristicChanged------>" + bytesToHexString(characteristic.getValue()));
            if (bleResponseCallBack != null) {
                bleResponseCallBack.respose(bytesToBinary(characteristic.getValue()));
            }
        }

        //接受Characteristic被写的通知,收到蓝牙模块的数据后会触发onCharacteristicWrite
        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            Log.d(TAG, "status = " + status);
            byte[] value = characteristic.getValue();
            Log.d(TAG, "onCharacteristicWrite------>" + bytesToHexString(characteristic.getValue()));
        }
    };

    /**
     * 打开BLE设备的 notify 通道
     */
    public void openBLEDataIn() {
        if (mBluetoothGatt != null) {
            BluetoothGattService service = mBluetoothGatt.getService(UUID.fromString(DATA_SERVICE_UUID));

            if (service != null) {
                BluetoothGattCharacteristic characteristic = service.getCharacteristic(UUID.fromString(REC_PKG_CHAR_UUID));
                if (characteristic != null) {
                    BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID.fromString(CLIENT_CHARACTERISTIC_CONFIG));
                    descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                    mBluetoothGatt.writeDescriptor(descriptor);
                    mBluetoothGatt.setCharacteristicNotification(characteristic, true);
                    Log.i(TAG, "openBLEDataIn() 蓝牙设备的nofity通知发出， 蓝牙连接完成，准备接收命令");
                }
            }
        }
    }

    private BluetoothGattCharacteristic getCharcteristic(String serviceUUID, String characteristicUUID) {
        //得到服务对象
        BluetoothGattService service = getService(UUID.fromString(serviceUUID));  //调用上面获取服务的方法

        if (service == null) {
            Log.e(TAG, "Can not find 'BluetoothGattService'");
            return null;
        }

        //得到此服务结点下Characteristic对象
        final BluetoothGattCharacteristic gattCharacteristic = service.getCharacteristic(UUID.fromString(characteristicUUID));
        if (gattCharacteristic != null) {
            return gattCharacteristic;
        } else {
            Log.e(TAG, "Can not find 'BluetoothGattCharacteristic'");
            return null;
        }
    }

    public BluetoothGattService getService(UUID uuid) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.e(TAG, "BluetoothAdapter not initialized");
            return null;
        }
        return mBluetoothGatt.getService(uuid);
    }

    /**
     * 向蓝牙设备写入数据
     *
     * @param data
     */
    public boolean write(byte[] data) {   //一般都是传byte
        //得到可写入的characteristic Utils.isAIRPLANE(mContext) &&
        if (!mBluetoothAdapter.isEnabled()) {
            Log.e(TAG, "writeCharacteristic 开启飞行模式");
            return false;
        }
        BluetoothGattCharacteristic writeCharacteristic = getCharcteristic(DATA_SERVICE_UUID,
                SEND_PKG_CHAR_UUID);  //这个UUID都是根据协议号的UUID
        if (writeCharacteristic == null) {
            Log.e(TAG, "Write failed. GattCharacteristic is null.");
            return false;
        }
        writeCharacteristic.setValue(data); //为characteristic赋值
        return writeCharacteristicWrite(writeCharacteristic);

    }

    public boolean writeCharacteristicWrite(BluetoothGattCharacteristic characteristic) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.e(TAG, "BluetoothAdapter not initialized");
            return false;
        }
        Log.e(TAG, "BluetoothAdapter 写入数据");
        boolean isBoolean = false;
        isBoolean = mBluetoothGatt.writeCharacteristic(characteristic);
        Log.e(TAG, "BluetoothAdapter_writeCharacteristic = " + isBoolean);  //如果isBoolean返回的是true则写入成功
        return isBoolean;
    }

    @Override
    public boolean closeBleConnect() {
        if (mBluetoothGatt == null) {
            return false;
        }

        mBluetoothGatt.disconnect();
        mBluetoothGatt.close();
        mBluetoothGatt = null;
        return true;
    }

    @Override
    public int getBleState() {
        return mState == BluetoothState.CONNECTED ? 1 : 0;
    }

    public enum BluetoothState {
        UNKNOWN,
        IDLE,
        SCANNING,
        BLUETOOTH_OFF,
        CONNECTING,
        CONNECTED,
        DISCONNECTING,
        DISCONNECTED
    }

    private byte charToByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }

    public byte[] hexStringToBytes(String hexString) {
        if (hexString == null || hexString.equals("")) {
            return null;
        }
        hexString = hexString.toUpperCase();
        int length = hexString.length() / 2;
        char[] hexChars = hexString.toCharArray();
        byte[] d = new byte[length];
        for (int i = 0; i < length; i++) {
            int pos = i * 2;
            d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
        }
        Log.i(TAG, "hexStringToBytes: " + d);
        return d;
    }

    public String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
                System.out.println(stringBuilder);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }

    /**
     * Convert byte[] to Binary
     *
     * @param src
     * @return
     */
    public String bytesToBinary(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String tString = Integer.toBinaryString((v + 0x100)).substring(1);
            stringBuilder.append(tString);
        }
        return stringBuilder.toString();
    }
}
