package com.weex.sample;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;

import com.weex.sample.extend.ble.BleCmdType;
import com.weex.sample.extend.ble.DeviceData;
import com.weex.sample.extend.ble.Jf_BleUtil;
import com.weex.sample.extend.ble.onBleScanResultCallBack;
import com.weex.sample.extend.ble.onResponseCallBack;

import java.util.List;


/**
 * Created by Administrator on 2017/7/31.
 * 蓝牙命令等前一条收到后再发下一条，否则可能写入失败
 */

public class BActivity extends Activity implements onBleScanResultCallBack, onResponseCallBack {

    Jf_BleUtil jf_bleUtil;
    public static int REQUEST_CODE = 11111;
    private static String TAG = BActivity.class.getSimpleName();
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.abactivity);
        jf_bleUtil = new Jf_BleUtil(this);
    }

    public void textClick(View view)
    {
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();

        Log.i(TAG, "onStart: 判断蓝牙是否打开");
        if(jf_bleUtil.checkBleOpen())
        {
            jf_bleUtil.startScan("LanQian",this);
            Log.i(TAG, "onStart: 开始扫描蓝牙");
        }else{
            Intent mIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(mIntent, REQUEST_CODE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_CODE)
        {
            if(requestCode == RESULT_OK)
                jf_bleUtil.startScan("LanQian",this);
        }
    }

    @Override
    public boolean scanResult(int state) {
        if(state == 1)
        {
            jf_bleUtil.stopScan();//停止扫描
            if(jf_bleUtil.connectBle())//停止扫描
            {
                Log.i(TAG, "scanResult: 连接成功");
                return true;
            }
        }
        Log.i(TAG, "scanResult: 连接失败");
        return false;
    }
    
    private void sendMsg()
    {
        Log.i(TAG, "sendMsg: 在连接成功的基础上去发送数据");
        boolean result = jf_bleUtil.writeBle(BleCmdType.BleCmd_GetDeviceBattery,this);
        Log.i(TAG, "sendMsg: 写入结果 如果是true说明写操作成功 等待response" + result);
    }
    

    @Override
    public void response(int type, int data) {
        switch (type)
        {
            case BleCmdType.BleCmd_GetDeviceBattery:
                Log.i(TAG, "response: 当前设备蓝牙电量" + data);
                break;
        }
    }

    @Override
    public void responseHistoryData(List<DeviceData> lists) {
        //这边处理历史压力和实时压力
    }
}
