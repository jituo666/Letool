
package com.xjt.letool.activities;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.ViewGroup;

import com.xjt.letool.LetoolApp;
import com.xjt.letool.LetoolContext;
import com.xjt.letool.R;
import com.xjt.letool.common.OrientationManager;
import com.xjt.letool.common.ThreadPool;
import com.xjt.letool.fragment.PhotoFragment;
import com.xjt.letool.metadata.DataManager;
import com.xjt.letool.view.GLBaseView;
import com.xjt.letool.view.GLController;
import com.xjt.letool.view.GLRootView;
import com.xjt.letool.view.LetoolBottomBar;
import com.xjt.letool.view.LetoolTopBar;

import java.util.List;

public class LocalImageBrowseActivity extends FragmentActivity implements
        LetoolContext {

    private LetoolTopBar mTopBar;
    private LetoolBottomBar mBottomBar;
    private ViewGroup mMainView;
    private GLRootView mGLESView;
    private OrientationManager mOrientationManager;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.local_browse_image);
        mTopBar = new LetoolTopBar(this, (ViewGroup) findViewById(R.id.local_image_browse_top_bar));
        mBottomBar = new LetoolBottomBar(this, (ViewGroup) findViewById(R.id.local_image_browse_bottom_bar));
        mMainView = (ViewGroup) findViewById(R.id.main_view);
        mGLESView = (GLRootView) mMainView.findViewById(R.id.gl_root_view);
        mOrientationManager = new OrientationManager(this);
        Fragment f = new PhotoFragment();
        Bundle data = new Bundle();
        data.putString(ThumbnailActivity.KEY_MEDIA_PATH, getDataManager()
                .getTopSetPath(DataManager.INCLUDE_LOCAL_IMAGE_ONLY));
        data.putBoolean(ThumbnailActivity.KEY_IS_PHOTO_ALBUM, true);
        f.setArguments(data);
        pushContentFragment(f, false);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mGLESView.getVisibility() == View.VISIBLE)
            mGLESView.onPause();
    }

    @Override
    protected void onResume() {
        if (mGLESView.getVisibility() == View.VISIBLE)
            mGLESView.onResume();
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    //
    public void setMainView(GLBaseView view) {
        mGLESView.setContentPane(view);
        mGLESView.setVisibility(View.VISIBLE);
        ViewGroup normalView = (ViewGroup) mMainView
                .findViewById(R.id.normal_root_view);
        normalView.removeAllViews();
        normalView.setVisibility(View.GONE);
    }

    @Override
    public void setMainView(View view) {
        mGLESView.setVisibility(View.GONE);
        ViewGroup normalView = (ViewGroup) mMainView
                .findViewById(R.id.normal_root_view);
        normalView.removeAllViews();
        normalView.addView(view);
        normalView.setVisibility(View.VISIBLE);
    }

    public void pushContentFragment(Fragment newFragment, boolean backToStack) {

        FragmentManager fm = getSupportFragmentManager();
        List<Fragment> fragments = fm.getFragments();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        if (fragments != null && fragments.size() > 0) {
            for (Fragment oldFragment : fragments) {
                if (oldFragment != null) {
                    ft.remove(oldFragment);
                    if (backToStack)
                        ft.addToBackStack(null);
                }
            }
        }
        ft.add(newFragment, null);
        ft.commit();
    }

    public void popContentFragment() {
        getSupportFragmentManager().popBackStack();
    }

    //

    @Override
    public LetoolTopBar getLetoolTopBar() {
        return mTopBar;
    }

    @Override
    public LetoolBottomBar getLetoolBottomBar() {
        return mBottomBar;
    }

    @Override
    public DataManager getDataManager() {
        return ((LetoolApp) getApplication()).getDataManager();
    }

    @Override
    public Context getAppContext() {
        return getApplicationContext();
    }

    @Override
    public ThreadPool getThreadPool() {
        return ((LetoolApp) getApplication()).getThreadPool();
    }

    @Override
    public OrientationManager getOrientationManager() {
        return mOrientationManager;
    }

    public GLController getGLController() {
        return (GLRootView) mMainView.findViewById(R.id.gl_root_view);
    }

}
