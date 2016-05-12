package org.hmtec.demo;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;

import org.hmtec.app.PTalkApp;

import java.lang.ref.WeakReference;

import cn.jpush.android.api.JPushInterface;

/**
 * Created by Eric on 2016/4/4.
 */
public class PTalkApplication extends Application {
    private static PTalkApplication sApp = null;
    public static PTalkApplication the() {
        return sApp;
    }
    private PTalkApp mPTalkApp = null;
    private WeakReference<Activity> currentActivityWeakRef;

    public Context getContext() {
        return context;
    }

    private Context context;
    @Override public void onCreate() {
        super.onCreate();
        sApp = this;
        JPushInterface.setDebugMode(true); 	// 设置开启日志,发布时请关闭日志
        JPushInterface.init(this);     		// 初始化 JPush
        mPTalkApp = new PTalkApp(getApplicationContext());
        context = getApplicationContext();
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

            }

            @Override
            public void onActivityStarted(Activity activity) {

            }

            @Override
            public void onActivityResumed(Activity activity) {
                if(currentActivityWeakRef != null) {
                    currentActivityWeakRef.clear();
                    currentActivityWeakRef = null;
                }

                currentActivityWeakRef = new WeakReference<Activity>(activity);
            }

            @Override
            public void onActivityPaused(Activity activity) {
                if(currentActivityWeakRef == null)
                    return;
                if(currentActivityWeakRef.get() == activity) {
                    currentActivityWeakRef.clear();
                    currentActivityWeakRef = null;
                }
            }

            @Override
            public void onActivityStopped(Activity activity) {

            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

            }

            @Override
            public void onActivityDestroyed(Activity activity) {

            }
        });
    }

    public PTalkApp PTalkApp() {
        return mPTalkApp;
    }

    public void EixtApp() {
        {//* Release RtcClient
            mPTalkApp.Disconnect();
            mPTalkApp.Destroy();
            mPTalkApp = null;
        }
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.exit(0);
    }

    public Activity getCurrentActivity() {
        Activity currentActivity = null;
        if (currentActivityWeakRef != null) {
            currentActivity = currentActivityWeakRef.get();
        }
        return currentActivity;
    }
}
