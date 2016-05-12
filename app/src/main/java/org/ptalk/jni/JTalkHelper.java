package org.ptalk.jni;

public interface JTalkHelper {
	public void OnRtcConnect(int code, String strSysConf);

	public void OnRtcMessage(int cmd, String fromId, String strJsep);

	public void OnRtcDisconnect();

	public void OnRtcConnectFailed();
}
