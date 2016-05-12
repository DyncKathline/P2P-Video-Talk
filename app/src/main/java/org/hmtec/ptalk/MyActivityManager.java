package org.hmtec.ptalk;

import android.app.Activity;

import java.lang.ref.WeakReference;

/**
 * Created by Eric on 2016/4/4.
 */
public class MyActivityManager {
    private static MyActivityManager sInstance = new MyActivityManager();
    private WeakReference<Activity> sCurrentActivityWeakRef;


    private MyActivityManager() {

    }

    public static MyActivityManager getInstance() {
        return sInstance;
    }

    public Activity getCurrentActivity() {
        Activity currentActivity = null;
        if (sCurrentActivityWeakRef != null) {
            currentActivity = sCurrentActivityWeakRef.get();
        }
        return currentActivity;
    }

    public void setCurrentActivity(Activity activity) {
        if(sCurrentActivityWeakRef != null) {
            sCurrentActivityWeakRef.clear();
            sCurrentActivityWeakRef = null;
        }

        sCurrentActivityWeakRef = new WeakReference<Activity>(activity);
    }

    public void unsetCurrentActivity(Activity activity) {
        if(sCurrentActivityWeakRef.get() == activity) {
            sCurrentActivityWeakRef.clear();
            sCurrentActivityWeakRef = null;
        }
    }
}
