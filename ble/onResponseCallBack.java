package com.android.livedemo.ble;

import java.util.List;

/**
 * Created by Administrator on 2017/8/16.
 * 调用者回调，需要实现
 */

public interface onResponseCallBack {

    void response(int type, Object data);

    void responseHistoryData(List<DeviceData> lists);
}
