package com.weex.sample;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;

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

    public static int REQUEST_CODE = 11111;
    private static String TAG = BActivity.class.getSimpleName();
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.abactivity);
    }

    public void textClick(View view)
    {
        sendMsg();
    }

    public void textClick2(View view)
    {
        sendMsg2();
    }

    @Override
    protected void onStart() {
        super.onStart();

        Log.i(TAG, "onStart: 判断蓝牙是否打开");
        if(Jf_BleUtil.getInstance(this).checkBleOpen())
        {
            Jf_BleUtil.getInstance(this).startScan(this);
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
            if(resultCode == RESULT_OK)
                Jf_BleUtil.getInstance(this).startScan(this);
        }
    }


    
    private void sendMsg()
    {
        Log.i(TAG, "sendMsg: 在连接成功的基础上去发送数据");
        boolean result = Jf_BleUtil.getInstance(this).writeBle(BleCmdType.BleCmd_SetDeviceTime, System.currentTimeMillis()/1000+"",this);
        Log.i(TAG, "sendMsg: 写入结果 如果是true说明写操作成功 等待response" + result);
    }

    private void sendMsg2()
    {
        Log.i(TAG, "sendMsg: 在连接成功的基础上去发送数据");
        boolean result = Jf_BleUtil.getInstance(this).writeBle(BleCmdType.BleCmd_GetDeviceTime,this);
        Log.i(TAG, "sendMsg: 写入结果 如果是true说明写操作成功 等待response" + result);
    }
    

    @Override
    public void response(int type, Object data) {
        switch (type)
        {
            case BleCmdType.BleCmd_SetDeviceSaveWord:
                Log.i(TAG, "response: 当前设置保留字" + data);
                break;
            case BleCmdType.BleCmd_GetDeviceTime:
                Log.i(TAG, "response: 当前读取保留字" + data);
                break;
        }
    }

    @Override
    public void responseHistoryData(List<DeviceData> lists) {
        //这边处理历史压力和实时压力
    }

    @Override
    public void scanResult(BluetoothDevice device) {
        if(device != null)
        {
            if(device.getName().contains("LanQian"))
            {
                Jf_BleUtil.getInstance(this).stopScan();//停止扫描
                boolean b = Jf_BleUtil.getInstance(this).connectBle(device);//连接设备
                Log.i(TAG, "scanResult: " + b);
            }
        }
    }
}
