package com.weex.sample.extend.ble;

/**
 * Created by Administrator on 2017/8/15.
 */

public interface onBleScanResultCallBack {
    /**
     *1成功,0失败
     * @return
     */
    boolean scanResult(int state);
}
