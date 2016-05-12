package org.ptalk.jni;

/**
 * Created by Eric on 2016/3/31.
 */
public class PTalkJni {
    /**
     * 构造访问jni底层库的对象
     */
    private final long fNativeAppId;

    public PTalkJni(JTalkHelper helper) {
        fNativeAppId = Create(helper);
    }

    private static native long Create(JTalkHelper helper);

    public native int ConnectionStatus();
    public native void Connect(String strSvrAddr, int nSvrPort,
                               String strUserId);

    public native void	Message(int cmd, String strTo, String strContent);
    public native void Disconnect();

    /**
     * 销毁APP
     */
    public native void Destroy();
}
