package org.hmtec.app;

import android.app.Activity;

import org.hmtec.demo.PTalkApplication;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by Eric on 2016/3/31.
 */
public interface PTalkEvents {
    public static enum ConnCode {
        LC_OK, LC_MSG_ERROR, LC_ONLINED
    }
    public void OnConnected(ConnCode code);

    public void OnDisconnect(int code);

    //* 呼出
    public void OnCallAccept(String peerId, String strJsep);

    public void OnCallReject(String peerId, int reason);

    //* 呼入
    public void OnCallRingIn(String peerId, String strJsep);

    //* 通用
    public void OnCallInfo(String peerId, String strJsep);
    public void OnCallEnd(String peerId);

    public interface PTalkCommonEvents {
        public void OnConnected(ConnCode code);

        public void OnDisconnect(int code);

        public void OnCallRingIn(String peerId, String strJsep);

        public void OnCallEnd(String peerId);
    }

    public interface PTalkCallEvents {
        public void OnCallAccept(String peerId, String strJsep);

        public void OnCallReject(String peerId, int reason);

        public void OnCallInfo(String peerId, String strJsep);

        public void OnCallEnd(String peerId);

    }

    public static class PTalkEventsImpl implements PTalkEvents {
        private HashMap<String , Activity> mEventMap = null;
        public PTalkEventsImpl() {
            mEventMap = new HashMap<>();
        }
        public void AttachEvent(String tag, Activity event) {
            mEventMap.put(tag, event);
        }

        public void DetachEvent(String tag) {
            mEventMap.remove(tag);
        }
        @Override
        public void OnConnected(final ConnCode code) {
            Activity curActivity = PTalkApplication.the().getCurrentActivity();
            if(curActivity == null) {
                //* 后台模式
            }
            Iterator iter = mEventMap.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry entry = (Map.Entry) iter.next();
                final Activity activity = (Activity)entry.getValue();
                if(activity instanceof PTalkCommonEvents) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            PTalkCommonEvents events = (PTalkCommonEvents)activity;
                            events.OnConnected(code);
                        }
                    });
                }
            }
        }

        @Override
        public void OnDisconnect(final int code) {
            Activity curActivity = PTalkApplication.the().getCurrentActivity();
            if(curActivity == null) {
                //* 后台模式
            }
            Iterator iter = mEventMap.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry entry = (Map.Entry) iter.next();
                final Activity activity = (Activity) entry.getValue();
                if (curActivity instanceof PTalkCommonEvents) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            PTalkCommonEvents events = (PTalkCommonEvents) activity;
                            events.OnDisconnect(code);
                        }
                    });
                }
            }
        }

        @Override
        public void OnCallAccept(final String peerId, final String strJsep) {
            Activity curActivity = PTalkApplication.the().getCurrentActivity();
            if(curActivity == null) {
                //* 后台模式
            }
            Iterator iter = mEventMap.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry entry = (Map.Entry) iter.next();
                final Activity activity = (Activity) entry.getValue();
                if (activity instanceof PTalkCallEvents) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            PTalkCallEvents events = (PTalkCallEvents) activity;
                            events.OnCallAccept(peerId, strJsep);
                        }
                    });
                }
            }
        }

        @Override
        public void OnCallReject(final String peerId, final int reason) {
            Activity curActivity = PTalkApplication.the().getCurrentActivity();
            if(curActivity == null) {
                //* 后台模式
            }
            Iterator iter = mEventMap.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry entry = (Map.Entry) iter.next();
                final Activity activity = (Activity) entry.getValue();
                if (activity instanceof PTalkCallEvents) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            PTalkCallEvents events = (PTalkCallEvents) activity;
                            events.OnCallReject(peerId, reason);
                        }
                    });
                }
            }
        }

        @Override
        public void OnCallRingIn(final String peerId, final String strJsep) {
            Activity curActivity = PTalkApplication.the().getCurrentActivity();
            if(curActivity == null) {
                //* 后台模式
            }
            Iterator iter = mEventMap.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry entry = (Map.Entry) iter.next();
                final Activity activity = (Activity) entry.getValue();
                if (activity instanceof PTalkCommonEvents) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            PTalkCommonEvents events = (PTalkCommonEvents) activity;
                            events.OnCallRingIn(peerId, strJsep);
                        }
                    });
                }
            }
        }

        @Override
        public void OnCallInfo(final String peerId, final String strJsep) {
            Activity curActivity = PTalkApplication.the().getCurrentActivity();
            if(curActivity == null) {
                //* 后台模式
            }
            Iterator iter = mEventMap.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry entry = (Map.Entry) iter.next();
                final Activity activity = (Activity) entry.getValue();
                if (activity instanceof PTalkCallEvents) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            PTalkCallEvents events = (PTalkCallEvents) activity;
                            events.OnCallInfo(peerId, strJsep);
                        }
                    });
                }
            }
        }

        @Override
        public void OnCallEnd(final String peerId) {
            Activity curActivity = PTalkApplication.the().getCurrentActivity();
            if(curActivity == null) {
                //* 后台模式
            }
            Iterator iter = mEventMap.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry entry = (Map.Entry) iter.next();
                final Activity activity = (Activity) entry.getValue();
                if (activity instanceof PTalkCommonEvents) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            PTalkCommonEvents events = (PTalkCommonEvents) activity;
                            events.OnCallEnd(peerId);
                        }
                    });
                } else if (activity instanceof PTalkCallEvents) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            PTalkCallEvents events = (PTalkCallEvents) activity;
                            events.OnCallEnd(peerId);
                        }
                    });
                }
            }
        }
    }
}
