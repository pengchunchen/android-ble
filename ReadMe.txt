Ble:蓝牙基本操作接口
调用顺序：
1.setCharUUID：设置需要连接的蓝牙参数，在各自的蓝牙模块中初始化
2.checkBleOpen：检测手机蓝牙是否打开，打开中可以进行下一步操作，否则自行调用申请打开蓝牙
		同时设置蓝牙状态回调
3.startScanBle：开始扫描蓝牙设备，这边能得到的是扫描到的蓝牙名称，筛选需要连接的设备
4.stopScanBle：找到目标设备，应该立即停止蓝牙扫描
5.connectBle：连接第3步选择的蓝牙设备
6.writeBle：向蓝牙发送指令，回调回收指令
7.closeBleConnect：不再使用蓝牙，记得断开

异常处理：
1.蓝牙中途断开，可调用reConnectBle，重连蓝牙操作