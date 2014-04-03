package com.xjt.letool.fragments;

import com.xjt.letool.R;
import com.xjt.letool.TransitionStore;
import com.xjt.letool.common.LLog;
import com.xjt.letool.views.GLController;
import com.xjt.letool.views.GLRootView;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class PhotoFragment extends LetoolFragment {
    private static final String TAG = "PhotoFragment";
    private GLRootView mGLRootView;
    private TransitionStore mTransitionStore = new TransitionStore();

    public PhotoFragment() {
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.gl_root_group, container, false);
        mGLRootView = (GLRootView)rootView.findViewById(R.id.gl_root_view);
        LLog.i(TAG, "onCreateView:" + mGLRootView);
        return rootView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onDestroyView() {

        super.onDestroyView();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public GLController getGLController() {
        return mGLRootView;
    }
}
