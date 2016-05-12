/*
 *  Copyright 2015 The WebRTC Project Authors. All rights reserved.
 *
 *  Use of this source code is governed by a BSD-style license
 *  that can be found in the LICENSE file in the root of the source
 *  tree. An additional intellectual property rights grant can be found
 *  in the file PATENTS.  All contributing project authors may
 *  be found in the AUTHORS file in the root of the source tree.
 */

package org.hmtec.view;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import org.hmtec.demo.MainActivity;
import org.hmtec.ptalk.CallActivity;
import org.hmtec.ptalk.R;
import org.webrtc.RendererCommon.ScalingType;

/**
 * Fragment for call control.
 */
public class CallFragment extends Fragment {
    private View controlView;
    private ImageView contactHead;
    private TextView contactView;
    private ImageButton connectButton;
    private ImageButton disconnectButton;
    private ImageButton cameraSwitchButton;
    private ImageButton videoScalingButton;
    private TextView captureFormatText;
    private SeekBar captureFormatSlider;
    private OnCallEvents callEvents;
    private ScalingType scalingType;
    private boolean videoCallEnabled = true;
    private int callMode = CallActivity.CALL_MODE_IN;

    /**
     * Call control interface for container activity.
     */
    public interface OnCallEvents {
        public void onCallAccept();

        public void onCallHangUp();

        public void onCameraSwitch();

        public void onVideoScalingSwitch(ScalingType scalingType);

        public void onCaptureFormatChange(int width, int height, int framerate);

        public void onSwitchRemoteView();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        controlView =
                inflater.inflate(R.layout.fragment_call, container, false);

        // Create UI controls.
        contactHead = (ImageView) controlView.findViewById(R.id.contact_head);
        contactView =
                (TextView) controlView.findViewById(R.id.contact_name_call);
        captureFormatText =
                (TextView) controlView.findViewById(R.id.capture_format_text_call);
        captureFormatSlider =
                (SeekBar) controlView.findViewById(R.id.capture_format_slider_call);

        scalingType = ScalingType.SCALE_ASPECT_FILL;

        return controlView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        connectButton =
                (ImageButton) controlView.findViewById(R.id.button_call_connect);
        disconnectButton =
                (ImageButton) controlView.findViewById(R.id.button_call_disconnect);
        cameraSwitchButton =
                (ImageButton) controlView.findViewById(R.id.button_call_switch_camera);
        videoScalingButton =
                (ImageButton) controlView.findViewById(R.id.button_call_scaling_mode);

        // Add buttons click events.
        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callEvents.onCallAccept();
                connectButton.setVisibility(View.GONE);
            }
        });
        disconnectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callEvents.onCallHangUp();
            }
        });

        cameraSwitchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callEvents.onCameraSwitch();
            }
        });

        videoScalingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), MainActivity.class);
                getActivity().startActivity(intent);
                callEvents.onSwitchRemoteView();
//                if (scalingType == ScalingType.SCALE_ASPECT_FILL) {
//                    videoScalingButton.setBackgroundResource(
//                            R.drawable.ic_action_full_screen);
//                    scalingType = ScalingType.SCALE_ASPECT_FIT;
//                } else {
//                    videoScalingButton.setBackgroundResource(
//                            R.drawable.ic_action_return_from_full_screen);
//                    scalingType = ScalingType.SCALE_ASPECT_FILL;
//                }
//                callEvents.onVideoScalingSwitch(scalingType);
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();

        boolean captureSliderEnabled = false;
        Bundle args = getArguments();
        if (args != null) {
            String contactName = args.getString(CallActivity.EXTRA_CALLID);
            contactView.setText(contactName);
            videoCallEnabled = args.getBoolean(CallActivity.EXTRA_VIDEO_CALL, true);
            callMode = args.getInt(CallActivity.EXTRA_CALL_MODE, CallActivity.CALL_MODE_IN);
            captureSliderEnabled = videoCallEnabled
                    && args.getBoolean(CallActivity.EXTRA_VIDEO_CAPTUREQUALITYSLIDER_ENABLED, false);
        }
        if (!videoCallEnabled) {
            cameraSwitchButton.setVisibility(View.INVISIBLE);
        }
        if (callMode == CallActivity.CALL_MODE_OUT) {
            connectButton.setVisibility(View.GONE);
        }
        if (captureSliderEnabled) {
            captureFormatSlider.setOnSeekBarChangeListener(
                    new CaptureQualityController(captureFormatText, callEvents));
        } else {
            captureFormatText.setVisibility(View.GONE);
            captureFormatSlider.setVisibility(View.GONE);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        callEvents = (OnCallEvents) activity;
    }

    public void CallOK() {
        contactHead.setVisibility(View.GONE);
        contactView.setVisibility(View.GONE);
        cameraSwitchButton.setVisibility(View.VISIBLE);
        videoScalingButton.setVisibility(View.VISIBLE);
    }

}
