package org.hmtec.ptalk;

import android.content.Context;
import android.graphics.PixelFormat;
import android.view.Gravity;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;

import org.hmtec.app.PeerConnectionClient;
import org.webrtc.EglBase;

public class MyWindowManager {

	/**
	 * 大悬浮窗View的实例
	 */
	private static FloatWindowBigView bigWindow;

	/**
	 * 大悬浮窗View的参数
	 */
	private static LayoutParams bigWindowParams;

	/**
	 * 用于控制在屏幕上添加或移除悬浮窗
	 */
	private static WindowManager mWindowManager;

	/**
	 * 创建一个大悬浮窗。位置为屏幕正中间。
	 *
	 * @param context
	 *            必须为应用程序的Context.
	 * @param rootEglBase
	 */
	public static void createBigWindow(Context context, PeerConnectionClient peerConnectionClient, EglBase rootEglBase) {
		WindowManager windowManager = getWindowManager(context);
		int screenWidth = windowManager.getDefaultDisplay().getWidth();
		int screenHeight = windowManager.getDefaultDisplay().getHeight();
		if (bigWindow == null) {
			bigWindow = new FloatWindowBigView(context, peerConnectionClient,rootEglBase);
			if (bigWindowParams == null) {
				bigWindowParams = new LayoutParams();
				bigWindowParams.type = LayoutParams.TYPE_PHONE;
				bigWindowParams.format = PixelFormat.RGBA_8888;
				bigWindowParams.flags = LayoutParams.FLAG_NOT_FOCUSABLE;
				bigWindowParams.gravity = Gravity.LEFT | Gravity.TOP;
				//小窗口摆放的位置，手机屏幕中央
				bigWindowParams.x = screenWidth / 2 - FloatWindowBigView.viewWidth / 2;
				bigWindowParams.y = screenHeight / 2 - FloatWindowBigView.viewHeight / 2;
				bigWindowParams.width = FloatWindowBigView.viewWidth;
				bigWindowParams.height = FloatWindowBigView.viewHeight;
			}
			bigWindow.setParams(bigWindowParams);
			windowManager.addView(bigWindow, bigWindowParams);
		}
	}

	/**
	 * 将大悬浮窗从屏幕上移除。
	 * 
	 * @param context
	 *            必须为应用程序的Context.
	 */
	public static void removeBigWindow(Context context) {
		if (bigWindow != null) {
			WindowManager windowManager = getWindowManager(context);
			windowManager.removeView(bigWindow);
			bigWindow = null;
		}
	}

	/**
	 * 如果WindowManager还未创建，则创建一个新的WindowManager返回。否则返回当前已创建的WindowManager。
	 * 
	 * @param context
	 *            必须为应用程序的Context.
	 * @return WindowManager的实例，用于控制在屏幕上添加或移除悬浮窗。
	 */
	private static WindowManager getWindowManager(Context context) {
		if (mWindowManager == null) {
			mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		}
		return mWindowManager;
	}

}
