package com.xjt.letool.movieplayer;

import android.view.View;

public interface ControllerOverlay {

  interface Listener {
    void onPlayPause();
    void onSeekStart();
    void onSeekMove(int time);
    void onSeekEnd(int time);
    void onShown();
    void onHidden();
    void onReplay();
  }

  void setListener(Listener listener);

  void setCanReplay(boolean canReplay);

  /**
   * @return The overlay view that should be added to the player.
   */
  View getView();

  void show();

  void showPlaying();

  void showPaused();

  void showEnded();

  void showLoading();

  void showErrorMessage(String message);

  void setTimes(int currentTime, int totalTime);
}
