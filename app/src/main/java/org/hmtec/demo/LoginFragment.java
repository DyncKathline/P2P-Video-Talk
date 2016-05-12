package org.hmtec.demo;

import android.app.Activity;
import android.app.Fragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import org.hmtec.ptalk.R;

/**
 * Created by Eric on 2016/4/4.
 */
public class LoginFragment extends Fragment {
    /**
     * Call control interface for container activity.
     */
    public interface OnLoginEvents {
        public void onDoLogin(String account, String password);
    }

    private SharedPreferences mSharedPref;
    private OnLoginEvents mLoginEvents;
    private View control_view;
    private EditText edit_account;
    private EditText edit_password;
    private Button btn_login;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Get setting keys.
        mSharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        // UI
        control_view =
                inflater.inflate(R.layout.fragment_login, container, false);
        {
            edit_account = (EditText) control_view.findViewById(R.id.edit_account);
            edit_password = (EditText) control_view.findViewById(R.id.edit_password);
        }
        return control_view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        btn_login = (Button) control_view.findViewById(R.id.btn_login);

        addViewListener();
    }

    @Override
    public void onStart() {
        super.onStart();
    }
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mLoginEvents = (OnLoginEvents) activity;
    }

    public void SetLoginOK(String authorization) {
        SharedPreferences.Editor editor = mSharedPref.edit();
        editor.putString(getString(R.string.pref_account), edit_account.getEditableText().toString());
        editor.putString(getString(R.string.pref_password), edit_password.getEditableText().toString());
        editor.putString(getString(R.string.pref_authorization), authorization);
        editor.putBoolean(getString(R.string.pref_logined), true);
        editor.commit();
    }

    private void addViewListener() {
        // Add buttons click events.
        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String strAccount = edit_account.getEditableText().toString();
                if(strAccount.length() == 0) {
                    edit_account.setError("Input!");
                    return;
                }
                String strPassword = edit_password.getEditableText().toString();
                if(strPassword.length() == 0) {
                    edit_password.setError("Input!");
                    return;
                }

                mLoginEvents.onDoLogin(strAccount, "7eb47883a09b362d8f54a0a032fc0608");
            }
        });
    }
}
