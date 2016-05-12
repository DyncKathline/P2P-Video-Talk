package org.hmtec.demo;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpResponseHandler;

import org.apache.http.Header;
import org.hmtec.app.PTalkApp;
import org.hmtec.app.PTalkEvents;
import org.hmtec.app.PTalkHttp;
import org.hmtec.ptalk.CallActivity;
import org.hmtec.ptalk.R;
import org.hmtec.util.AppRTCUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.Set;

import cn.jpush.android.api.JPushInterface;
import cn.jpush.android.api.TagAliasCallback;
import cn.pedant.SweetAlert.SweetAlertDialog;

public class MainActivity extends AppCompatActivity implements LoginFragment.OnLoginEvents, MainFragment.OnMainEvents, PTalkEvents.PTalkCommonEvents {
    private static final String mEventTag = "main_activity";
    private static final String TAG = "PTalk";
    private static final int RE_CONNECT = 0;
    private static final int DO_EXIT = 1;
    private PTalkApp mPTalkApp = null;
    private SharedPreferences mSharedPref;
    private Toast mLogToast;
    private boolean mActivityRunning = false;
    private boolean mDoExit = false;

    // Controls
    LoginFragment mLoginFragment;
    MainFragment mMainFragment;

    // Handler
    Handler myHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case RE_CONNECT: {
                    DoConnect(mSharedPref.getString(getString(R.string.pref_account), ""));
                }
                break;
                case DO_EXIT: {
                    mDoExit = false;
                }
                break;
            }
            super.handleMessage(msg);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mPTalkApp = PTalkApplication.the().PTalkApp();

        stateCheck(savedInstanceState);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        // Get setting keys.
        mSharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        boolean isLogined = mSharedPref.getBoolean(getString(R.string.pref_logined), false);

        // Activate call and HUD fragments and start the call.
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        if (isLogined) {
            ft.hide(mLoginFragment).show(mMainFragment);
            ft.commit();
            DoConnect(mSharedPref.getString(getString(R.string.pref_account), ""));
        } else {
            ft.hide(mMainFragment).show(mLoginFragment);
            ft.commit();
        }

        mPTalkApp.AttachEvent(mEventTag, this);

    }

    @Override
    public void onPause() {
        super.onPause();
        JPushInterface.onPause(this);
        mActivityRunning = false;
    }

    @Override
    public void onResume() {
        super.onResume();
        JPushInterface.onResume(this);
        mActivityRunning = true;
    }


    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        mPTalkApp.DetachEvent(mEventTag);
    }

    private String[] tags = new String[2];

    protected void stateCheck(Bundle savedInstanceState) {
        FragmentManager fm = getFragmentManager();
        if (savedInstanceState == null) {
            FragmentTransaction ft = fm.beginTransaction();
            mLoginFragment = new LoginFragment();
            mMainFragment = new MainFragment();
            tags[0] = "0";
            ft.add(R.id.fragment_container, mLoginFragment, tags[0]);
            tags[1] = "1";
            ft.add(R.id.fragment_container, mMainFragment, tags[1]).hide(mLoginFragment);
            ft.commit();
        } else {
            mLoginFragment = (LoginFragment) fm.findFragmentByTag(tags[0]);
            mMainFragment = (MainFragment) fm.findFragmentByTag(tags[1]);
            fm.beginTransaction().show(mMainFragment).hide(mLoginFragment).commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // Log |msg| and Toast about it.
    private void logAndToast(String msg) {
        Log.d(TAG, msg);
        if (mLogToast != null) {
            mLogToast.cancel();
        }
        mLogToast = Toast.makeText(this, msg, Toast.LENGTH_SHORT);
        mLogToast.show();
    }

    /**
     * Private Function
     */
    private void ReloadUI() {
        if (!mPTalkApp.IsConnected()) {

        }
    }

    private void DoHttpAuth(final String userId, final String pwd) {
        final SweetAlertDialog sDlg = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        sDlg.setTitleText("");
        sDlg.setContentText("登录中...");
        sDlg.setCancelable(false);
        sDlg.show();

        //* Send http request
        PTalkHttp.SignIn(getApplicationContext(), userId, pwd, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int i, Header[] headers, byte[] response) {
                sDlg.dismiss();
                if (i == 200) {
                    String content = new String(response);
                    try {
                        JSONObject respJson = new JSONObject(content);
                        int code = respJson.getInt("code");
                        if (code == 200) {
                            String strAuthorization = respJson.getString("authorization");
                            {
                                FragmentTransaction ft = getFragmentManager().beginTransaction();
                                mLoginFragment.SetLoginOK(strAuthorization);
                                ft.hide(mLoginFragment);
                                ft.show(mMainFragment);
                                ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                                ft.commit();
                            }
                            DoConnect(userId);
                        } else {
                            switch (code) {
                                case 202:
                                    ShowAlertDlg("登录失败", "用户名密码错误!");
                                    break;
                                case 204:
                                    ShowAlertDlg("登录失败", "用户不存在");
                                    break;
                            }
                        }
                    } catch (JSONException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                        AppRTCUtils.assertIsTrue(false);
                    }
                } else {
                    ShowAlertDlg("登录失败", "认证服务器异常");
                }
            }

            @Override
            public void onFailure(int i, Header[] headers, byte[] bytes, Throwable throwable) {
                sDlg.dismiss();
                ShowAlertDlg("认证失败", "无法连接认证服务器!");
            }
        });
    }

    private void DoExit() {
        if (!mDoExit) {
            mDoExit = true;
            logAndToast("再按一次退出程序");
            // 利用handler延迟发送更改状态信息
            myHandler.sendEmptyMessageDelayed(DO_EXIT, 1800);
        } else {
            mPTalkApp = null;
            finish();
            PTalkApplication.the().EixtApp();
        }
    }

    private void DoConnect(String userId) {
        //1, 判断当前手机网络是否正常
        //2, 判断是否已经Http认证过期
        if (!mPTalkApp.IsConnected()) {
            this.setTitle("连接中...");
            mPTalkApp.Connect(userId);
        }

        Set<String> users = new HashSet<>();
        users.add(userId);
        JPushInterface.setAliasAndTags(this, userId, users, TagsCallback);
    }

    private void OnConnectFailed(int code) {
        if (code != 0) {
            this.setTitle("未连接");
            boolean isLogined = mSharedPref.getBoolean(getString(R.string.pref_logined), false);
            if (isLogined) {
                myHandler.sendEmptyMessageDelayed(RE_CONNECT, 5000);
            }
        }
    }

    private void ShowAlertDlg(String title, String msg) {
        SweetAlertDialog sDlg = new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE);
        sDlg.setTitleText(title);
        sDlg.setContentText(msg);
        sDlg.setCancelable(false);
        sDlg.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                sweetAlertDialog.cancel();
            }
        });
        sDlg.show();
    }

    private final TagAliasCallback TagsCallback = new TagAliasCallback() {

        @Override
        public void gotResult(int code, String alias, Set<String> tags) {
            String logs;
            switch (code) {
                case 0:
                    logs = "Set tag and alias success";
                    Log.i(TAG, logs);
                    break;

                case 6002:
                    logs = "Failed to set alias and tags due to timeout. Try again after 60s.";
                    Log.e(TAG, logs);
                    break;

                default:
                    logs = "Failed with errorCode = " + code;
                    Log.e(TAG, logs);
            }
        }
    };

    /**
     * Implements for LoginFragment.OnLoginEvents
     */
    @Override
    public void onDoLogin(String account, String password) {
        //* 1, Http请求认证
        //* 2, Http请求认证成功后，连接消息服务器
        //* 3, 连接消息服务器成功后，回调OnLogined(0)成功
        DoHttpAuth(account, password);
    }

    /**
     * Implements for LoginFragment.OnMainEvents
     * 呼叫别人
     */
    @Override
    public void onCallPeer(String account) {
        if (!mPTalkApp.IsConnected()) {
            logAndToast("正在努力连接中...");
        } else {
//            Log.e(TAG, "onCallPeer: 正在呼叫 = " + account);
            Intent it = new Intent(this, CallActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString(CallActivity.EXTRA_CALLID, account);
            bundle.putInt(CallActivity.EXTRA_CALL_MODE, CallActivity.CALL_MODE_OUT);
            it.putExtras(bundle);
            startActivityForResult(it, 101);
        }
    }

    /**
     * Implements for PTalkEvents.
     */
    @Override
    public void OnConnected(PTalkEvents.ConnCode code) {
        if (code == PTalkEvents.ConnCode.LC_OK) {
            this.setTitle("PTalk");
        } else {
            //String errorInfo = "连接失败";
            //ShowAlertDlg("", errorInfo);
            logAndToast("连接服务器失败,稍后将重新连接!");
            OnConnectFailed(-1);
        }
    }

    @Override
    public void OnDisconnect(int code) {
        OnConnectFailed(code);
    }

    /**
     * 被呼叫方调用
     *
     * @param peerId
     * @param briefMsg
     */
    @Override
    public void OnCallRingIn(String peerId, String briefMsg) {
        Intent it = new Intent(this, CallActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString(CallActivity.EXTRA_CALLID, peerId);
        bundle.putInt(CallActivity.EXTRA_CALL_MODE, CallActivity.CALL_MODE_IN);
        bundle.putString(CallActivity.EXTRA_CALL_INFO, briefMsg);
        it.putExtras(bundle);
        this.startActivityForResult(it, 101);
    }

    @Override
    public void OnCallEnd(String peerId) {

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            DoExit();
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        ReloadUI();
    }
}
