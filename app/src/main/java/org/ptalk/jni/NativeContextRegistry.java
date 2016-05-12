package org.ptalk.jni;

import android.content.Context;

/**
 * Copyright (c) 2015 DYNC. All Rights Reserved.
 * 
 * 向jni层注册和注销Java虚拟机
 * 
 * @author DYNC
 *
 */
public class NativeContextRegistry {

	/**
	 * 加载api所需要的动态库
	 */
	static {
		System.loadLibrary("ptalk-jni");
	}

	/**
	 * 向Jni层注册Java虚拟机
	 * 
	 * @param context
	 *            ：应用的上下文
	 */
	public native void register(Context context);

	/**
	 * 解除Java虚拟机与Jni层的绑定
	 */
	public native void unRegister();
}
