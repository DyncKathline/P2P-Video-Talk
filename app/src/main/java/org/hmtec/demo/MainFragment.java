package org.hmtec.demo;

import android.app.Activity;
import android.app.Fragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.hmtec.ptalk.R;

import java.util.ArrayList;

/**
 * Created by Eric on 2016/4/4.
 */
public class MainFragment extends Fragment {
    /**
     * Call control interface for container activity.
     */
    public interface OnMainEvents {
        public void onCallPeer(String account);
    }

    private SharedPreferences mSharedPref;
    private OnMainEvents mMainEvents;
    private View control_view;
    private ListView lstv_users;


    private ArrayList<String> mUserList;
    private ArrayAdapter<String> mAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        control_view =
                inflater.inflate(R.layout.fragment_main, container, false);
        {
            lstv_users = (ListView) control_view.findViewById(R.id.list_users);
        }
        return control_view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Get setting keys.
        mSharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mUserList = new ArrayList<String>();
        mUserList.add("555555555");
        mUserList.add("666666666");
        mUserList.add("777777777");
        mUserList.add("888888888");
        mUserList.add("999999999");
//        String roomListJson = sharedPref.getString(getString(R.string.pref_room_list_key), null);
//        if (roomListJson != null) {
//            try {
//                JSONArray jsonArray = new JSONArray(roomListJson);
//                for (int i = 0; i < jsonArray.length(); i++) {
//                    mUserList.add(jsonArray.get(i).toString());
//                }
//            } catch (JSONException e) {
//                Log.e("", "Failed to load room list: " + e.toString());
//            }
//        }
        mAdapter = new ArrayAdapter<String>(
                getActivity(), android.R.layout.simple_list_item_1, mUserList);
        lstv_users.setAdapter(mAdapter);

        lstv_users.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //如果呼叫的是自己则不执行以下代码
                if (!mUserList.get(position).equals(mSharedPref.getString(getString(R.string.pref_account),""))) {
                    mMainEvents.onCallPeer(mUserList.get(position));
                }else {
                    Toast.makeText(getActivity(),"不能呼叫自己",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mMainEvents = (OnMainEvents) activity;
    }
}
