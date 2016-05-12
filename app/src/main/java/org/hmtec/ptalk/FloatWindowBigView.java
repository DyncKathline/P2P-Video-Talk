package org.hmtec.ptalk;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;

import org.hmtec.app.PeerConnectionClient;
import org.hmtec.view.PercentFrameLayout;
import org.webrtc.EglBase;
import org.webrtc.RendererCommon;
import org.webrtc.SurfaceViewRenderer;

import java.lang.reflect.Field;

public class FloatWindowBigView extends LinearLayout {

    Context context;
    private long startTime;
    /**
     * 记录大悬浮窗的宽度
     */
    public static int viewWidth;

    /**
     * 记录大悬浮窗的高度
     */
    public static int viewHeight;
    /**
     * 记录系统状态栏的高度
     */
    private static int statusBarHeight;

    /**
     * 用于更新小悬浮窗的位置
     */
    private WindowManager windowManager;

    /**
     * 小悬浮窗的参数
     */
    private WindowManager.LayoutParams mParams;

    /**
     * 记录当前手指位置在屏幕上的横坐标值
     */
    private float xInScreen;

    /**
     * 记录当前手指位置在屏幕上的纵坐标值
     */
    private float yInScreen;

    /**
     * 记录手指按下时在屏幕上的横坐标的值
     */
    private float xDownInScreen;

    /**
     * 记录手指按下时在屏幕上的纵坐标的值
     */
    private float yDownInScreen;

    /**
     * 记录手指按下时在小悬浮窗的View上的横坐标的值
     */
    private float xInView;

    /**
     * 记录手指按下时在小悬浮窗的View上的纵坐标的值
     */
    private float yInView;
    private SurfaceViewRenderer remoteRender;
    private PercentFrameLayout remoteRenderLayout;//显示对方像
    private EglBase rootEglBase;

    public FloatWindowBigView(final Context context, PeerConnectionClient peerConnectionClient, EglBase rootEglBase) {
        super(context);
        this.context = context;
        windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        LayoutInflater.from(context).inflate(R.layout.float_window_big, this);
        View view = findViewById(R.id.big_window_layout);

        remoteRender = (SurfaceViewRenderer) view.findViewById(R.id.remote_video_view);
        remoteRenderLayout = (PercentFrameLayout) view.findViewById(R.id.remote_video_layout);

        // Create video renderers.
        this.rootEglBase = rootEglBase;
        remoteRender.init(this.rootEglBase.getEglBaseContext(), null);

        peerConnectionClient.switchSurfaceViewRenderer(remoteRender);
        remoteRenderLayout.setPosition(0,0,100,100);
        remoteRender.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL);
        remoteRender.setMirror(false);
        remoteRender.requestLayout();

        viewWidth = view.getLayoutParams().width;
        viewHeight = view.getLayoutParams().height;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // 手指按下时记录必要数据,纵坐标的值都需要减去状态栏高度
                xInView = event.getX();
                yInView = event.getY();
                xDownInScreen = event.getRawX();
                yDownInScreen = event.getRawY() - getStatusBarHeight();
                xInScreen = event.getRawX();
                yInScreen = event.getRawY() - getStatusBarHeight();
                break;
            case MotionEvent.ACTION_MOVE:
                xInScreen = event.getRawX();
                yInScreen = event.getRawY() - getStatusBarHeight();
                // 手指移动的时候更新小悬浮窗的位置
                updateViewPosition();
                break;
            case MotionEvent.ACTION_UP:
                // 如果手指离开屏幕时，xDownInScreen和xInScreen相等，且yDownInScreen和yInScreen相等，则视为触发了单击事件。
                if (Math.abs(xDownInScreen - xInScreen) < 5 && Math.abs(yDownInScreen - yInScreen) < 5) {
                    long end = System.currentTimeMillis() - startTime;
                    // 双击的间隔在 300ms以下
                    if (end < 300) {
                        //TODO
                        Intent intent = new Intent("org.dync.ACTION_CALL_ACTIVITY");
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(intent);
                        MyWindowManager.removeBigWindow(context);
                    }
                    startTime = System.currentTimeMillis();
                }
                break;
            default:
                break;
        }
        return true;
    }

    /**
     * 将小悬浮窗的参数传入，用于更新小悬浮窗的位置。
     *
     * @param params 小悬浮窗的参数
     */
    public void setParams(WindowManager.LayoutParams params) {
        mParams = params;
    }

    /**
     * 更新小悬浮窗在屏幕中的位置。
     */
    private void updateViewPosition() {
        mParams.x = (int) (xInScreen - xInView);
        mParams.y = (int) (yInScreen - yInView);
        windowManager.updateViewLayout(this, mParams);
    }

    /**
     * 用于获取状态栏的高度。
     *
     * @return 返回状态栏高度的像素值。
     */
    private int getStatusBarHeight() {
        if (statusBarHeight == 0) {
            try {
                Class<?> c = Class.forName("com.android.internal.R$dimen");
                Object o = c.newInstance();
                Field field = c.getField("status_bar_height");
                int x = (Integer) field.get(o);
                statusBarHeight = getResources().getDimensionPixelSize(x);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return statusBarHeight;
    }

}
