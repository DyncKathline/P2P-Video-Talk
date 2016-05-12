package org.hmtec.ptalk;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import org.hmtec.app.AppRTCAudioManager;
import org.hmtec.app.PTalkApp;
import org.hmtec.app.PTalkEvents;
import org.hmtec.app.PeerConnectionClient;
import org.hmtec.demo.PTalkApplication;
import org.hmtec.view.CallFragment;
import org.hmtec.view.PercentFrameLayout;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.webrtc.EglBase;
import org.webrtc.IceCandidate;
import org.webrtc.PeerConnection;
import org.webrtc.RendererCommon;
import org.webrtc.SessionDescription;
import org.webrtc.StatsReport;
import org.webrtc.SurfaceViewRenderer;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Eric on 2016/4/2.
 */
public class CallActivity extends Activity implements PeerConnectionClient.PeerConnectionEvents, CallFragment.OnCallEvents , PTalkEvents.PTalkCallEvents {
    private static final String mEventTag = "call_activity";
    private static final String TAG = "CallRTCClient";
    public static final String EXTRA_CALLID =
            "org.hmtec.ptalk.CALLID";
    public static final String EXTRA_VIDEO_CALL =
            "org.hmtec.ptalk.VIDEO_CALL";
    public static final String EXTRA_VIDEO_WIDTH =
            "org.hmtec.ptalk.VIDEO_WIDTH";
    public static final String EXTRA_VIDEO_HEIGHT =
            "org.hmtec.ptalk.VIDEO_HEIGHT";
    public static final String EXTRA_VIDEO_FPS =
            "org.hmtec.ptalk.VIDEO_FPS";
    public static final String EXTRA_VIDEO_CAPTUREQUALITYSLIDER_ENABLED =
            "org.appsopt.apprtc.VIDEO_CAPTUREQUALITYSLIDER";
    public static final String EXTRA_CALL_MODE =
            "org.hmtec.ptalk.CALL_MODE";
    public static final String EXTRA_CALL_INFO =
            "org.hmtec.ptalk.CALL_INFO";


    public static final int CALL_MODE_OUT = 0;
    public static final int CALL_MODE_IN = 1;
    // Local preview screen position before call is connected.
    private static final int LOCAL_X_CONNECTING = 0;
    private static final int LOCAL_Y_CONNECTING = 0;
    private static final int LOCAL_WIDTH_CONNECTING = 100;
    private static final int LOCAL_HEIGHT_CONNECTING = 100;
    // Local preview screen position after call is connected.
    private static final int LOCAL_X_CONNECTED = 72;
    private static final int LOCAL_Y_CONNECTED = 72;
    private static final int LOCAL_WIDTH_CONNECTED = 25;
    private static final int LOCAL_HEIGHT_CONNECTED = 25;
    // Remote video screen position
    private static final int REMOTE_X = 0;
    private static final int REMOTE_Y = 0;
    private static final int REMOTE_WIDTH = 100;
    private static final int REMOTE_HEIGHT = 100;

    // List of mandatory application permissions.
    private static final String[] MANDATORY_PERMISSIONS = {
            "android.permission.MODIFY_AUDIO_SETTINGS",
            "android.permission.RECORD_AUDIO",
            "android.permission.INTERNET"
    };

    private PTalkApp mPTalkApp;
    private PeerConnectionClient peerConnectionClient = null;
    private AppRTCAudioManager audioManager = null;
    private EglBase rootEglBase;
    private SurfaceViewRenderer localRender;
    private SurfaceViewRenderer remoteRender;
    private PercentFrameLayout localRenderLayout;//显示本地像
    private PercentFrameLayout remoteRenderLayout;//显示对方像
    private RendererCommon.ScalingType scalingType;
    private Toast logToast;
    private boolean iceConnected;
    private boolean callControlFragmentVisible = true;
    private PeerConnectionClient.PeerConnectionParameters peerConnectionParameters;
    private String mPeerId;
    private boolean mInitiator = false;
    private boolean mActivityRunning = false;
    private String mCallInfo;
    private LinkedList<IceCandidate> queuedCandidates;
    private MediaPlayer mediaPlayer;

    // Controls，接受、挂断、切换像的按钮等
    CallFragment callFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set window styles for fullscreen-window size. Needs to be done before
        // adding content.
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN
                        | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                        | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                        | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                        | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

        setContentView(R.layout.activity_call);

        mPTalkApp = PTalkApplication.the().PTalkApp();

        iceConnected = false;
        scalingType = RendererCommon.ScalingType.SCALE_ASPECT_FILL;

        // Create UI controls.
        localRender = (SurfaceViewRenderer) findViewById(R.id.local_video_view);
        remoteRender = (SurfaceViewRenderer) findViewById(R.id.remote_video_view);
        localRenderLayout = (PercentFrameLayout) findViewById(R.id.local_video_layout);
        remoteRenderLayout = (PercentFrameLayout) findViewById(R.id.remote_video_layout);
        callFragment = new CallFragment();

        // Show/hide call control fragment on view click.
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleCallControlFragmentVisibility();
            }
        };



        localRender.setOnClickListener(listener);
        remoteRender.setOnClickListener(listener);

        // Create video renderers.
        rootEglBase = EglBase.create();
        localRender.init(rootEglBase.getEglBaseContext(), null);
        remoteRender.init(rootEglBase.getEglBaseContext(), null);
        localRender.setZOrderMediaOverlay(true);
        updateVideoView();

        // Check for mandatory permissions.
        for (String permission : MANDATORY_PERMISSIONS) {
            if (checkCallingOrSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                logAndToast("Permission " + permission + " is not granted");
                setResult(RESULT_CANCELED);
                finish();
                return;
            }
        }
        // Get Intent parameters.
        final Intent intent = getIntent();
        // Send intent arguments to fragments.
        callFragment.setArguments(intent.getExtras());
        // Activate call and HUD fragments and start the call.
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.add(R.id.call_fragment_container, callFragment);
        ft.commit();

        mPeerId = intent.getExtras().getString(CallActivity.EXTRA_CALLID);
        mInitiator = intent.getExtras().getInt(CallActivity.EXTRA_CALL_MODE, 0) == CallActivity.CALL_MODE_OUT;
        mCallInfo = intent.getExtras().getString(CallActivity.EXTRA_CALL_INFO);
        this.setResult(101, intent);
        {
            peerConnectionParameters = new PeerConnectionClient.PeerConnectionParameters(
                    intent.getBooleanExtra(EXTRA_VIDEO_CALL, true),
                    false, false, 1280, 720, 30, 800, "H264", true, false, 32, "opus", false, false, false);
            peerConnectionClient = PeerConnectionClient.getInstance();
            peerConnectionClient.createPeerConnectionFactory(
                    CallActivity.this, peerConnectionParameters, CallActivity.this);
        }
        logAndToast("EXTRA_VIDEO_CALL = "+intent.getBooleanExtra(EXTRA_VIDEO_CALL, true));
        if(mInitiator) {
            queuedCandidates = new LinkedList<IceCandidate>();
            CreateAudioMgr();
            MakeCall();
        } else {
            StartRing();
        }
        mPTalkApp.AttachEvent(mEventTag, this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        peerConnectionClient.switchSurfaceViewRenderer(remoteRender);
    }

    // Activity interfaces
    @Override
    public void onPause() {
        super.onPause();
        mActivityRunning = false;
        if(mediaPlayer != null) {
            mediaPlayer.pause();
        }
        if (peerConnectionClient != null) {
//            peerConnectionClient.stopVideoSource();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mActivityRunning = true;
        if(mediaPlayer != null) {
            mediaPlayer.start();
        }
        if (peerConnectionClient != null) {
            peerConnectionClient.startVideoSource();
        }
    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        mPTalkApp.DetachEvent(mEventTag);
        StopRing();
        if (peerConnectionClient != null) {
            peerConnectionClient.close();
            peerConnectionClient = null;
        }
        if (audioManager != null) {
            audioManager.close();
            audioManager = null;
        }
        if(queuedCandidates != null) {
            queuedCandidates.clear();
            queuedCandidates = null;
        }
        MyWindowManager.removeBigWindow(this);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }

    // Log |msg| and Toast about it.
    private void logAndToast(String msg) {
        Log.d(TAG, msg);
        if (logToast != null) {
            logToast.cancel();
        }
        logToast = Toast.makeText(this, msg, Toast.LENGTH_SHORT);
        logToast.show();
    }

    // Helper functions.
    private void toggleCallControlFragmentVisibility() {
        if (!iceConnected || !callFragment.isAdded()) {
            return;
        }
        // Show/hide call control fragment
        callControlFragmentVisible = !callControlFragmentVisible;
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        if (callControlFragmentVisible) {
            ft.show(callFragment);
        } else {
            ft.hide(callFragment);
        }
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        ft.commit();
    }

    private void updateVideoView() {
        remoteRenderLayout.setPosition(REMOTE_X, REMOTE_Y, REMOTE_WIDTH, REMOTE_HEIGHT);
        remoteRender.setScalingType(scalingType);
        remoteRender.setMirror(false);

        if (iceConnected) {
            localRenderLayout.setPosition(
                    LOCAL_X_CONNECTED, LOCAL_Y_CONNECTED, LOCAL_WIDTH_CONNECTED, LOCAL_HEIGHT_CONNECTED);
            localRender.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT);
        } else {
            localRenderLayout.setPosition(
                    LOCAL_X_CONNECTING, LOCAL_Y_CONNECTING, LOCAL_WIDTH_CONNECTING, LOCAL_HEIGHT_CONNECTING);
            localRender.setScalingType(scalingType);
        }
        localRender.setMirror(false);

        localRender.requestLayout();
        remoteRender.requestLayout();
    }

    private void CreateAudioMgr() {
        if(audioManager == null) {
            // Create and audio manager that will take care of audio routing,
            // audio modes, audio device enumeration etc.
            audioManager = AppRTCAudioManager.create(this, new Runnable() {
                        // This method will be called each time the audio state (number and
                        // type of devices) has been changed.
                        @Override
                        public void run() {
                            onAudioManagerChangedState();
                        }
                    }
            );
            // Store existing audio settings and change audio mode to
            // MODE_IN_COMMUNICATION for best possible VoIP performance.
            Log.d(TAG, "Initializing the audio manager...");
            audioManager.init();
        }
    }

    private void onAudioManagerChangedState() {
        // TODO(henrika): disable video if AppRTCAudioManager.AudioDevice.EARPIECE
        // is active.
    }

    private void StartRing(){
        /**
         * 创建音频文件的方法：
         * 1、播放资源目录的文件：MediaPlayer.create(MainActivity.this,R.raw.ring);//播放res/raw 资源目录下的MP3文件
         * 2:播放sdcard卡的文件：mediaPlayer=new MediaPlayer();
         *   mediaPlayer.setDataSource("/sdcard/ring.mp3");//前提是sdcard卡要先导入音频文件
         */
        if(mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(this,R.raw.phonering);
            mediaPlayer.setLooping(true);
            mediaPlayer.start();
        }
    }

    private void StopRing() {
        if(mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    private void MakeCall() {
        List<PeerConnection.IceServer> iceServers = new ArrayList<PeerConnection.IceServer>();
        iceServers.add(new PeerConnection.IceServer("stun:123.59.68.21"));
        iceServers.add(new PeerConnection.IceServer("turn:123.59.68.21", "rtk007", "007pass"));
        peerConnectionClient.createPeerConnection(rootEglBase.getEglBaseContext(),
                localRender, remoteRender, iceServers);
        // Create offer. Offer SDP will be sent to answering client in
        // PeerConnectionEvents.onLocalDescription event.
        peerConnectionClient.createOffer();
    }

    private void AcceptCall() {
        String strJsep = mCallInfo;
        List<PeerConnection.IceServer> iceServers = new ArrayList<PeerConnection.IceServer>();
        iceServers.add(new PeerConnection.IceServer("stun:123.59.68.21"));
        iceServers.add(new PeerConnection.IceServer("turn:123.59.68.21", "rtk007", "007pass"));
        peerConnectionClient.createPeerConnection(rootEglBase.getEglBaseContext(),
                localRender, remoteRender, iceServers);
        if (strJsep != null && strJsep.length() > 0) {
            JSONTokener jsonParser = new JSONTokener(strJsep);
            final JSONObject json;
            try {
                json = (JSONObject) jsonParser.nextValue();
                final String type = json.has("type") ? json.getString("type") : "";
                final String sdp = json.has("sdp") ? json.getString("sdp") : "";
                final SessionDescription.Type jtype = SessionDescription.Type
                        .fromCanonicalForm(type);
                peerConnectionClient.setRemoteDescription(new SessionDescription(jtype, sdp));
                // Create answer. Answer SDP will be sent to offering client in
                // PeerConnectionEvents.onLocalDescription event.
                peerConnectionClient.createAnswer();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void EndCall() {
        if(mActivityRunning) {
            MediaPlayer player = MediaPlayer.create(this,R.raw.playend);
            player.start();
            player = null;
        }
        this.finish();
    }

    // Put a |key|->|value| mapping in |json|.
    private static void jsonPut(JSONObject json, String key, Object value) {
        try {
            json.put(key, value);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Implements for CallFragment.OnCallEvents
     */
    @Override
    public void onCallAccept() {
        StopRing();
        CreateAudioMgr();
        AcceptCall();
        callFragment.CallOK();
    }
    @Override
    public void onCallHangUp() {
        StopRing();
        if(mInitiator || iceConnected) {
            mPTalkApp.EndCall(mPeerId);
        } else {
            mPTalkApp.RejectCall(mPeerId);
        }

        EndCall();
    }

    @Override
    public void onCameraSwitch() {
        if (peerConnectionClient != null) {
            peerConnectionClient.switchCamera();
        }
    }

    @Override
    public void onVideoScalingSwitch(RendererCommon.ScalingType scalingType) {
        this.scalingType = scalingType;
        updateVideoView();
    }

    @Override
    public void onCaptureFormatChange(int width, int height, int framerate) {
        if (peerConnectionClient != null) {
            peerConnectionClient.changeCaptureFormat(width, height, framerate);
        }
    }

    @Override
    public void onSwitchRemoteView() {
        Log.e(TAG, "onSwitchRemoteView: ");
        MyWindowManager.createBigWindow(PTalkApplication.the().getContext(),peerConnectionClient,rootEglBase);
    }

    /**
     * Implements for PeerConnectionClient.PeerConnectionEvents
     */
    @Override
    public void onLocalDescription(final SessionDescription sdp) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                JSONObject json = new JSONObject();
                jsonPut(json, "sdp", sdp.description);
                if(mInitiator) {
                    jsonPut(json, "type", "offer");
                    Log.e(TAG, "offer");
                    mPTalkApp.MakeCall(mPeerId, json.toString());
                } else {
                    jsonPut(json, "type", "answer");
                    Log.e(TAG, "answer");
                    mPTalkApp.AcceptCall(mPeerId, json.toString());
                }
            }
        });
    }

    @Override
    public void onIceCandidate(final IceCandidate candidate) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(queuedCandidates != null) {
                    queuedCandidates.add(candidate);
                } else {
                    JSONObject json = new JSONObject();
                    jsonPut(json, "type", "candidate");
                    jsonPut(json, "sdpMLineIndex", candidate.sdpMLineIndex);
                    jsonPut(json, "sdpMid", candidate.sdpMid);
                    jsonPut(json, "candidate", candidate.sdp);
                    mPTalkApp.SendCallInfo(mPeerId, json.toString());
                }
            }
        });
    }

    @Override
    public void onIceConnected() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                iceConnected = true;
                // Update video view.
                updateVideoView();
            }
        });
    }

    @Override
    public void onIceDisconnected() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                iceConnected = false;
                // Update video view.
                updateVideoView();
            }
        });
    }

    @Override
    public void onPeerConnectionClosed() {

    }

    @Override
    public void onPeerConnectionStatsReady(StatsReport[] reports) {

    }

    @Override
    public void onPeerConnectionError(String description) {

    }

    /**
     * Implements for PTalkCallEvents.
     */
    @Override
    public void OnCallAccept(String peerId, String strJsep) {
        if (strJsep != null && strJsep.length() > 0) {
            JSONTokener jsonParser = new JSONTokener(strJsep);
            final JSONObject json;
            try {
                json = (JSONObject) jsonParser.nextValue();
                String type = json.has("type") ? json.getString("type") : "";
                String sdp = json.has("sdp") ? json.getString("sdp") : "";
                SessionDescription.Type jtype = SessionDescription.Type
                        .fromCanonicalForm(type);
                Log.e(TAG, sdp);
                peerConnectionClient.setRemoteDescription(new SessionDescription(jtype, sdp));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        if (queuedCandidates != null) {
            Log.d(TAG, "Add " + queuedCandidates.size() + " remote candidates");
            for (IceCandidate candidate : queuedCandidates) {
                JSONObject json = new JSONObject();
                jsonPut(json, "type", "candidate");
                jsonPut(json, "sdpMLineIndex", candidate.sdpMLineIndex);
                jsonPut(json, "sdpMid", candidate.sdpMid);
                jsonPut(json, "candidate", candidate.sdp);
                mPTalkApp.SendCallInfo(peerId, json.toString());
            }
            queuedCandidates.clear();
            queuedCandidates = null;
        }
        callFragment.CallOK();
    }

    @Override
    public void OnCallReject(String peerId, int reason) {
        logAndToast("对方拒绝啦!");
        EndCall();
    }

    @Override
    public void OnCallInfo(final String peerId, final String strJsep) {
        if(peerConnectionClient != null) {
            JSONTokener jsonParser = new JSONTokener(strJsep);
            try {
                final JSONObject json = (JSONObject) jsonParser.nextValue();
                IceCandidate candidate = new IceCandidate(
                        json.getString("sdpMid"),
                        json.getInt("sdpMLineIndex"),
                        json.getString("candidate"));
                peerConnectionClient.addRemoteIceCandidate(candidate);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void OnCallEnd(String peerId) {
        logAndToast("对方挂断喽!");
        EndCall();
    }
}
