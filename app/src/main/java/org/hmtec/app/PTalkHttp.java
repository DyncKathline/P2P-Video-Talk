package org.hmtec.app;

import android.content.Context;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.apache.http.entity.StringEntity;

import java.io.UnsupportedEncodingException;

/**
 * Created by Eric on 2016/4/4.
 */
public class PTalkHttp {
    private static final String kPostUrl = "http://123.59.68.21:8688";

    static public void SignIn(Context ctx, String userId, String pwd, AsyncHttpResponseHandler respHandler) {
        //* Send http request
        AsyncHttpClient httpClient = new AsyncHttpClient();
        httpClient.setTimeout(30 * 1000);
        httpClient.addHeader("Content-type", "application/x-www-form-urlencoded; charset=UTF-8");
        String content = "cellphone=" + userId + "&password=" + pwd;
        try {
            httpClient.post(ctx, kPostUrl + "/users/signin", new StringEntity(content), "text/plain", respHandler);
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            respHandler.onFailure(-1, null, null, null);
        }
    }
}
