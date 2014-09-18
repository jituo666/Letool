package com.xjt.newpic.edit.editors;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.xjt.newpic.R;
import com.xjt.newpic.common.LLog;
import com.xjt.newpic.edit.NpEditActivity;
import com.xjt.newpic.edit.history.HistoryManager;
import com.xjt.newpic.edit.imageshow.ImageManager;

public class EditorPanel extends Fragment {

    private static final String TAG = EditorPanel.class.getSimpleName();

    private LinearLayout mMainView;
    private Editor mEditor;
    private int mEditorID;

    public void setEditor(int editor) {
        mEditorID = editor;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        NpEditActivity filterShowActivity = (NpEditActivity) activity;
        mEditor = filterShowActivity.getEditor(mEditorID);
    }

    public void cancelCurrentFilter() {
        ImageManager masterImage = ImageManager.getImage();
        HistoryManager adapter = masterImage.getHistory();

        int position = adapter.undo();
        masterImage.onHistoryItemClick(position);
        ((NpEditActivity)getActivity()).invalidateViews();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        NpEditActivity activity = (NpEditActivity) getActivity();
        if (mMainView != null) {
            if (mMainView.getParent() != null) {
                ViewGroup parent = (ViewGroup) mMainView.getParent();
                parent.removeView(mMainView);
            }
            return mMainView;
        }
        mMainView = (LinearLayout) inflater.inflate(R.layout.np_edit_editor_panel, null);

        View actionControl = mMainView.findViewById(R.id.panelAccessoryViewList);
        View editControl = mMainView.findViewById(R.id.controlArea);
        ImageButton cancelButton = (ImageButton) mMainView.findViewById(R.id.cancelFilter);
        ImageButton applyButton = (ImageButton) mMainView.findViewById(R.id.applyFilter);
        Button editTitle = (Button) mMainView.findViewById(R.id.applyEffect);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelCurrentFilter();
                NpEditActivity activity = (NpEditActivity) getActivity();
                activity.backToMain();
            }
        });
        mEditor = activity.getEditor(mEditorID);
        if (mEditor != null) {
            mEditor.setUpEditorUI(actionControl, editControl, editTitle, null);
            mEditor.reflectCurrentFilter();
            if (mEditor.useUtilityPanel()) {
                mEditor.openUtilityPanel((LinearLayout) actionControl);
            }
        }
        applyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NpEditActivity activity = (NpEditActivity) getActivity();
                mEditor.finalApplyCalled();
                activity.backToMain();
            }
        });
        return mMainView;
    }

    @Override
    public void onDetach() {
        if (mEditor != null) {
            mEditor.detach();
        }
        super.onDetach();
    }

}
