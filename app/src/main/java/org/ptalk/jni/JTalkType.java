package org.ptalk.jni;

/**
 * Created by Eric on 2016/3/31.
 */
public class JTalkType {
    //* Connection Status
    public static final int NOT_CONNECTED = 0;
    public static final int RESOLVING = 1;
    public static final int CONNECTTING = 2;
    public static final int CONNECTED = 3;

    //* Call type
    public static final int MAKE_CALL = 0;
    public static final int END_CALL = 1;
    public static final int ACCEPT_CALL = 2;
    public static final int REJECT_CALL = 3;
    public static final int CALL_INFO = 4;
}
