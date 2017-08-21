package com.weex.sample.extend.ble;

import android.bluetooth.BluetoothDevice;
import android.content.Context;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by Administrator on 2017/8/15.
 */

public class Jf_BleUtil implements onBleResponseCallBack {
    private Context mContext;
    private Ble mBle;
    private onResponseCallBack callback;//调用者设置回调
    private int mSetBleCmd;//当前设置命令
    private boolean isReadHistory = false;//是否在读历史数据
    private int readHistoryCount;//读取历史数据请求的传感器个数
    private String historyResultString;//历史数据

    private static Jf_BleUtil mJf_BleUtil = null;
    public static Jf_BleUtil getInstance(Context context){
     synchronized(Jf_BleUtil.class){
        if(mJf_BleUtil == null)
        {
            mJf_BleUtil = new Jf_BleUtil(context);
        }
        return mJf_BleUtil;
     }
    }

    public Jf_BleUtil(Context context) {
        this.mContext = context;
        mBle = new BleModule(this.mContext);
        mBle.setCharUUID("FFF0", "FFF1", "FFF1", "2902");
    }

    /**
     * 判断蓝牙打开状态
     *
     * @return
     */
    public boolean checkBleOpen() {
        return mBle.checkBleOpen();
    }

    /**
     * 开始扫描
     */
    public void startScan(onBleScanResultCallBack scanResultCallBack) {
        mBle.startScanBle(scanResultCallBack);
    }

    /**
     * 停止扫描
     */
    public void stopScan() {
        mBle.stopScanBle();
    }

    /**
     * 开始连接
     */
    public boolean connectBle(BluetoothDevice bluetoothDevice) {
        return mBle.connectBle(bluetoothDevice);
    }

    /**
     * 写数据,不带参数,用于请求蓝牙数据
     */
    public boolean writeBle(int cmd, onResponseCallBack callback) {
        this.callback = callback;
        String content = null;
        switch (cmd) {
            case BleCmdType.BleCmd_GetDeviceBattery:
                content = "FE0008040000";
                break;
            case BleCmdType.BleCmd_GetDeviceSaveWord:
                content = "FE0008070000";
                break;
            case BleCmdType.BleCmd_GetDeviceSensorCount:
                content = "FE0008020000";
                break;
            case BleCmdType.BleCmd_GetDeviceTime:
                content = "FE0008030000";
                break;
            case BleCmdType.BleCmd_GetDeviceTimeInterval:
                content = "FE0008010000";
                break;
            case BleCmdType.BleCmd_SetDeviceCleanFlush:
                content = "FE0006060000";
                break;
        }
        if (content != null) {
            return mBle.writeBle(content, this);
        }
        return false;
    }

    /**
     * 写数据,带参数,用于设置蓝牙数据
     * @param cmd 请求的协议号
     * @param param BleCmd_SetDeviceTimeInterval：时间间隔/整数
     *              BleCmd_SetDeviceSaveWord：保留字/字符串 如"BC000001"
     *              BleCmd_SetDeviceSensorCount:传感器数量/整数
     *              BleCmd_SetDeviceTime：时间/整数 若要设置为当前时间，传System.currentTimeMillis()/1000
     *              BleCmd_ReadHistoryData:传那个传感器，这边是用16进制传值 如当前请求第四个传感器即1000，传值08
     *              BleCmd_ReadCurrentData:当前请求第二和四个传感器即1010，传值0A
     * @param callback
     * @return
     */
    public boolean writeBle(int cmd, String param, onResponseCallBack callback) {
        this.callback = callback;
        String content = null;
        mSetBleCmd = cmd;
        switch (cmd) {
            case BleCmdType.BleCmd_SetDeviceTimeInterval:
                content = "FE00060104" + intToHexString(param) + "00";
                break;
            case BleCmdType.BleCmd_SetDeviceSaveWord:
                while (param.length() < 8) {
                    param = "0" + param;
                }
                content = "FE00060704" + param + "00";
                break;
            case BleCmdType.BleCmd_SetDeviceSensorCount:
                content = "FE00060204" + intToHexString(param) + "00";
                break;
            case BleCmdType.BleCmd_SetDeviceTime:
                content = "FE00060304" + intToHexString(param) + "00";
                break;
            case BleCmdType.BleCmd_ReadHistoryData:
                content = historyCmd(param);
                break;
            case BleCmdType.BleCmd_ReadCurrentData:
                content = "FE0004" + param + "00";
        }
        if (content != null) {
            return mBle.writeBle(content, this);
        }
        return false;
    }

    /**
     * 拼接历史数据请求
     *
     * @param param 传感器编号  16进制
     * @return
     */
    private String historyCmd(String param) {
        isReadHistory = true;
        String value = "FE0002" + param + "00";
        String binaryValue = Integer.toBinaryString(Integer.parseInt(param, 16));
        int count = 0;
        for (int i = 0; i < binaryValue.length(); i++) {
            char cv = binaryValue.charAt(i);
            if (cv == '1') {
                count++;
            }
        }
        readHistoryCount = count;
        historyResultString = "";
        return value;
    }

    /**
     * 断开蓝牙连接
     */
    public void closeBleConnect() {
        mBle.closeBleConnect();
    }

    /**
     * 设备蓝牙数据回调
     *
     * @param code 2进制字符串
     */
    @Override
    public void respose(String code) {
        if (!isReadHistory) {
            String header = code.substring(0, 8);
            if (header.equals("11111110")) {
                String funcCodeString = code.substring(16, 24);
                if (funcCodeString.equals("00001001")) {
                    String type = code.substring(24, 32);
                    if (type.equals("00000001")) {
                        callback.response(BleCmdType.BleCmd_GetDeviceTimeInterval, bitStringToInt(code.substring(40, 72)));
                    } else if (type.equals("00000010")) {
                        callback.response(BleCmdType.BleCmd_GetDeviceSensorCount, bitStringToInt(code.substring(40, 72)));
                    } else if (type.equals("00000011")) {
                        callback.response(BleCmdType.BleCmd_GetDeviceTime, bitStringToInt(code.substring(40, 72)));
                    } else if (type.equals("00000100")) {
                        callback.response(BleCmdType.BleCmd_GetDeviceBattery, bitStringToInt(code.substring(40, 72)));
                    } else if (type.equals("00000111")) {
                        callback.response(BleCmdType.BleCmd_GetDeviceSaveWord, binaryToHex(code.substring(40, 72)));
                    }
                } else if (funcCodeString.equals("00000111")) {
                    callback.response(mSetBleCmd, 0);
                } else if (funcCodeString.equals("00000101")) {
                    int dataLength = bitStringToInt(code.substring(32, 40));
                    int dataCount = (dataLength - 4) / 5;
                    List<DeviceData> deviceDataLists = new ArrayList<>();
                    DeviceData deviceData = new DeviceData();
                    deviceData.historyDatas = parseData(code, dataCount);
                    deviceData.time = bitStringToInt(code.substring(code.length() - 40, code.length() - 8));
                    deviceDataLists.add(deviceData);
                    if (callback != null) {
                        callback.responseHistoryData(deviceDataLists);
                    }
                }
            }
        } else {
            historyResultString += code;
            int resultLength = historyResultString.length();
            if (historyResultString.substring(resultLength - 6 * 8, resultLength - 8).equals("1111111000000000000000110000000000000000")) {
                int eachDataLength = (10 + readHistoryCount * 5) * 8;
                int dataCount = (resultLength - 48) / eachDataLength;
                DeviceData deviceData;
                List<DeviceData> deviceDataLists = new ArrayList<>();
                for (int i = 0; i < dataCount; i++) {
                    String eachData = historyResultString.substring(i * eachDataLength, (i + 1) * eachDataLength);
                    String headString = eachData.substring(0, 8);
                    if (headString.equals("11111110")) {
                        String funcCodeString = eachData.substring(16, 24);
                        if (funcCodeString.equals("00000011")) {
                            int dataLength = bitStringToInt(eachData.substring(32, 40));
                            int eachdataCount = (dataLength - 4) / 5;
                            deviceData = new DeviceData();
                            deviceData.historyDatas = parseData(eachData, eachdataCount);
                            deviceData.time = bitStringToInt(eachData.substring(eachData.length() - 40, eachData.length() - 8));
                            deviceDataLists.add(deviceData);
                        }
                    }
                }
                if (callback != null) {
                    callback.responseHistoryData(deviceDataLists);
                    isReadHistory = false;
                    historyResultString = "";
                }
            }
        }

    }

    /**
     * 解析历史数据字符串
     *
     * @param data      字符串
     * @param dataCount 数据个数
     * @return
     */
    private List<HistoryData> parseData(String data, int dataCount) {
        List<HistoryData> lists = new ArrayList<>();
        HistoryData historyData;
        for (int i = 1; i <= dataCount; i++) {
            int channelState = bitStringToInt(data.substring(i * 40, i * 40 + 4));
            if (channelState == 0) {
                historyData = new HistoryData();
                historyData.channelNumber = bitStringToInt(data.substring(i * 40 + 4, i * 40 + 8));
                historyData.pressure = bitStringToInt(data.substring(i * 40 + 8, i * 40 + 32));
                historyData.pressureState = bitStringToInt(data.substring(i * 40 + 32, i * 40 + 40));
                lists.add(historyData);
            }
        }
        return lists;
    }

    /**
     * 获取蓝牙状态
     *
     * @return 1：已经连接
     */
    public int getBleState() {
        return mBle.getBleState();
    }

    /**
     * 2进制转换成10进制值
     *
     * @param bitString
     * @return
     */
    private int bitStringToInt(String bitString) {
        int result = 0;
        int length = bitString.length();
        for (int i = 0; i < bitString.length(); i++) {
            char code = bitString.charAt(i);
            if (code == '1') {
                result = result + (int) Math.pow(2, length - i - 1);
            }
        }
        return result;
    }

    /**
     * int类型值转化成8位16进制
     */
    private String intToHexString(String value) {
        String hexString = Integer.toHexString(Integer.valueOf(value));
        int diffCount = 8 - hexString.length();
        for (int i = 0; i < diffCount; i++) {
            hexString = "0" + hexString;
        }
        return hexString;
    }

    /**
     * 2进制转16进制
     * @param value
     * @return
     */
    private String binaryToHex(String value) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            sb.append(Integer.toHexString(Integer.parseInt(value.substring(4 * i, 4 * (i + 1)), 2)));
        }
        return sb.toString();
    }
}
