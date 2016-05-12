package org.hmtec.app;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import org.ptalk.jni.JTalkHelper;
import org.ptalk.jni.JTalkType;
import org.ptalk.jni.NativeContextRegistry;
import org.ptalk.jni.PTalkJni;

/**
 * Created by Eric on 2016/3/31.
 */
public class PTalkApp implements JTalkHelper {
    public static class CallSession {
        public static final int CALLIDLE = 0;
        public static final int CALLING = 1;
        public static final int SETUP = 2;
        protected int callStatus = CALLIDLE;
        protected String strCallId = "";
        protected String strPeerId = "";
        protected void Clear() {
            callStatus = CALLIDLE;
            strCallId = "";
            strPeerId = "";
        }
    }

    private static NativeContextRegistry sNativeContext;
    private PTalkJni mTalkJni;
    private Context mContext;
    private Boolean mConnected;
    private PTalkEvents.PTalkEventsImpl mEvents = null;
    private CallSession mCallSession;

    public PTalkApp(Context context) {
        mContext = context;
        {// * New all value.
            if (sNativeContext == null) {
                sNativeContext = new NativeContextRegistry();
                sNativeContext.register(context);
            }

            mEvents = new PTalkEvents.PTalkEventsImpl();
            mCallSession = new CallSession();
            mTalkJni = new PTalkJni(this);
            mConnected = false;
        }
    }

    public void AttachEvent(String tag, Activity event) {
        mEvents.AttachEvent(tag, event);
    }

    public void DetachEvent(String tag) {
        mEvents.DetachEvent(tag);
    }

    public void Destroy() {
        if(mTalkJni != null) {
            mTalkJni.Destroy();
            mTalkJni = null;
        }
    }

    public boolean IsConnected() {
        return mConnected;
    }

    public void Connect(final String userId) {
        if(mConnected) {
            return;
        }
        if(userId == null || userId.length() == 0) {
            return;
        }
        if (mTalkJni.ConnectionStatus() == JTalkType.NOT_CONNECTED) {
            mTalkJni.Connect("123.59.68.21", 8899, userId);
        }
    }

    public void Disconnect() {
        if(mConnected) {
            mTalkJni.Disconnect();
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //* Http

            mConnected = false;
        }
    }

    public boolean MakeCall(String peerId, String strJsep){
        if(!mConnected) {
            return false;
        }
        if(mCallSession.callStatus == CallSession.CALLIDLE) {
            mCallSession.callStatus = CallSession.CALLING;
            mCallSession.strPeerId = peerId;
            mCallSession.strCallId = peerId;
            mTalkJni.Message(JTalkType.MAKE_CALL, peerId, strJsep);
        } else {
            return false;
        }
        return true;
    }

    public void AcceptCall(String peerId, String strJsep) {
        if(mCallSession.callStatus == CallSession.CALLING) {
            mCallSession.callStatus = CallSession.SETUP;
            mTalkJni.Message(JTalkType.ACCEPT_CALL, peerId, strJsep);
        }
    }

    public void RejectCall(String peerId) {
        if(mCallSession.callStatus != CallSession.CALLIDLE) {
            mCallSession.callStatus = CallSession.CALLIDLE;
            mCallSession.Clear();
            mTalkJni.Message(JTalkType.REJECT_CALL, peerId, "RejectCall");
        }
    }

    public void EndCall(String peerId) {
        if(mCallSession.callStatus != CallSession.CALLIDLE) {
            mCallSession.callStatus = CallSession.CALLIDLE;
            mCallSession.Clear();
            mTalkJni.Message(JTalkType.END_CALL, peerId, "EndCall");
        }
    }

    public void SendCallInfo(String peerId, String strJsep) {
        if(mCallSession.callStatus == CallSession.SETUP) {
            mTalkJni.Message(JTalkType.CALL_INFO, peerId, strJsep);
        }
    }

    /**
     * internal function
     */
    private void ProcessMakeCall(String fromId, String strJsep) {
        if(mCallSession.callStatus == CallSession.CALLIDLE) {
            mCallSession.callStatus = CallSession.CALLING;
            mCallSession.strPeerId = fromId;
            mCallSession.strCallId = fromId;
            mEvents.OnCallRingIn(fromId, strJsep);
        } else {
            mTalkJni.Message(JTalkType.REJECT_CALL, fromId, "");
        }
    }
    private void ProcessEndCall(String fromId, String strJsep) {
        if(mCallSession.callStatus != CallSession.CALLIDLE) {
            mCallSession.callStatus = CallSession.CALLIDLE;
            mCallSession.Clear();
            mEvents.OnCallEnd(fromId);
        }
    }
    private void ProcessAccept(String fromId, String strJsep) {
        if(mCallSession.callStatus != CallSession.CALLIDLE) {
            mCallSession.callStatus = CallSession.SETUP;
            mEvents.OnCallAccept(fromId, strJsep);
        }
    }
    private void ProcessReject(String fromId, String strJsep) {
        if(mCallSession.callStatus != CallSession.CALLIDLE) {
            mCallSession.callStatus = CallSession.CALLIDLE;
            mCallSession.Clear();
            mEvents.OnCallReject(fromId, 0);
        }
    }
    private void ProcessCallInfo(String fromId, String strJsep) {
        mEvents.OnCallInfo(fromId, strJsep);
    }

    /**
     * implements for JTalkHelper
    */
    @Override
    public void OnRtcConnect(int code, String strSysConf) {
        if(code == 200) {
            mConnected = true;
            mEvents.OnConnected(PTalkEvents.ConnCode.LC_OK);
        } else {
            mConnected = false;
            mEvents.OnConnected(PTalkEvents.ConnCode.LC_MSG_ERROR);
        }
    }

    @Override
    public void OnRtcMessage(int cmd, String fromId, String strJsep) {
        Log.e("TAG", "OnRtcMessage: cmd = " + cmd + ",fromId = " + fromId + "strJsep = " + strJsep );
        switch(cmd) {
            case JTalkType.MAKE_CALL: {
                ProcessMakeCall(fromId, strJsep);
            }
            break;
            case JTalkType.END_CALL: {
                ProcessEndCall(fromId, strJsep);
            }
            break;
            case JTalkType.ACCEPT_CALL: {
                ProcessAccept(fromId, strJsep);
            }
            break;
            case JTalkType.REJECT_CALL: {
                ProcessReject(fromId, strJsep);
            }
            break;
            case JTalkType.CALL_INFO: {
                ProcessCallInfo(fromId, strJsep);
            }
            break;
        }
    }

    @Override
    public void OnRtcDisconnect() {
        if(mConnected) {
            mConnected = false;

            if(mCallSession.callStatus != CallSession.CALLIDLE) {
                mCallSession.callStatus = CallSession.CALLIDLE;
                mEvents.OnCallEnd(mCallSession.strCallId);
                mCallSession.Clear();
            }

            mEvents.OnDisconnect(-1);
        } else {
            mEvents.OnConnected(PTalkEvents.ConnCode.LC_MSG_ERROR);
        }
    }

    @Override
    public void OnRtcConnectFailed() {
        mEvents.OnConnected(PTalkEvents.ConnCode.LC_MSG_ERROR);
    }
}
