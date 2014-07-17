package com.xjt.letool.activities;

import com.xjt.letool.R;
import com.xjt.letool.movieplayer.MoviePlayer;

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
 * This activity plays a video from a specified URI.
 * The client of this activity can pass a logo bitmap in the intent (KEY_LOGO_BITMAP)
 * to set the action bar logo so the playback process looks more seamlessly integrated with
 * the original activity.
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
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.movie_view);
        View rootView = findViewById(R.id.movie_view_root);
        Intent intent = getIntent();
        initializeActionBar(intent);
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
            int orientation = intent.getIntExtra(MediaStore.EXTRA_SCREEN_ORIENTATION,ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
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
    
    private void initializeActionBar(Intent intent) {
/*        mUri = intent.getData();
        final ActionBar actionBar = getActionBar();
        Bitmap logo = intent.getParcelableExtra(KEY_LOGO_BITMAP);
        if (logo != null) {
            actionBar.setLogo(new BitmapDrawable(getResources(), logo));
        }
        actionBar.setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP,ActionBar.DISPLAY_HOME_AS_UP);

        String title = intent.getStringExtra(Intent.EXTRA_TITLE);
        if (title != null) {
            actionBar.setTitle(title);
        } else {
            // Displays the filename as title, reading the filename from the
            // interface: {@link android.provider.OpenableColumns#DISPLAY_NAME}.
            AsyncQueryHandler queryHandler =
                    new AsyncQueryHandler(getContentResolver()) {
                @Override
                protected void onQueryComplete(int token, Object cookie,
                        Cursor cursor) {
                    try {
                        if ((cursor != null) && cursor.moveToFirst()) {
                            String displayName = cursor.getString(0);

                            // Just show empty title if other apps don't set
                            // DISPLAY_NAME
                            actionBar.setTitle((displayName == null) ? "" :
                                    displayName);
                        }
                    } finally {
                        Utils.closeSilently(cursor);
                    }
                }
            };
            queryHandler.startQuery(0, null, mUri,
                    new String[] {OpenableColumns.DISPLAY_NAME}, null, null,
                    null);
        }*/
    }

    @Override
    public void onStart() {
        ((AudioManager) getSystemService(AUDIO_SERVICE)).requestAudioFocus(null, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
        super.onStart();
    }
    
    @Override
    public void onResume() {
        mMoviePlayer.onResume();
        super.onResume();
    }
    
    @Override
    public void onPause() {
        mMoviePlayer.onPause();
        super.onPause();
    }
    
    @Override
    protected void onStop() {
        ((AudioManager) getSystemService(AUDIO_SERVICE)).abandonAudioFocus(null);
        super.onStop();
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
