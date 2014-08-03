
package com.xjt.newpic.activities;

import com.umeng.analytics.MobclickAgent;
import com.xjt.newpic.R;
import com.xjt.newpic.movieplayer.MoviePlayer;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.media.AudioManager;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

/**
 * @Author Jituo.Xuan
 * @Date 8:16:26 PM Jul 24, 2014
 * @Comments:null
 */
public class MoviePlayActivity extends Activity {

    @SuppressWarnings("unused")
    private static final String TAG = MoviePlayActivity.class.getSimpleName();
    public static final String KEY_LOGO_BITMAP = "logo-bitmap";
    private MoviePlayer mMoviePlayer;
    private boolean mFinishOnCompletion;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.movie_play_view);
        View rootView = findViewById(R.id.movie_view_root);
        Intent intent = getIntent();
        mFinishOnCompletion = intent.getBooleanExtra(MediaStore.EXTRA_FINISH_ON_COMPLETION, true);
        mMoviePlayer = new MoviePlayer(rootView, this, intent.getData(), savedInstanceState, !mFinishOnCompletion) {

            @Override
            public void onCompletion() {
                if (mFinishOnCompletion) {
                    finish();
                }
            }
        };
        if (intent.hasExtra(MediaStore.EXTRA_SCREEN_ORIENTATION)) {
            int orientation = intent.getIntExtra(MediaStore.EXTRA_SCREEN_ORIENTATION, ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
            if (orientation != getRequestedOrientation()) {
                setRequestedOrientation(orientation);
            }
        }
        Window win = getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();
        winParams.buttonBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_OFF;
        winParams.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
        win.setAttributes(winParams);
        // We set the background in the theme to have the launching animation. But for the performance (and battery), we remove the background here.
        win.setBackgroundDrawable(null);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mMoviePlayer.onSaveInstanceState(outState);
    }

    @Override
    public void onStart() {
        ((AudioManager) getSystemService(AUDIO_SERVICE)).requestAudioFocus(null, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
        super.onStart();
    }

    @Override
    public void onResume() {
        MobclickAgent.onResume(this);
        mMoviePlayer.onResume();
        super.onResume();
    }

    @Override
    public void onPause() {
        MobclickAgent.onPause(this);
        mMoviePlayer.onPause();
        super.onPause();
    }

    @Override
    protected void onStop() {
        ((AudioManager) getSystemService(AUDIO_SERVICE)).abandonAudioFocus(null);
        super.onStop();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_left_in, R.anim.slide_right_out);
    }

    @Override
    public void onDestroy() {
        mMoviePlayer.onDestroy();
        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return mMoviePlayer.onKeyDown(keyCode, event) || super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        return mMoviePlayer.onKeyUp(keyCode, event) || super.onKeyUp(keyCode, event);
    }
}
