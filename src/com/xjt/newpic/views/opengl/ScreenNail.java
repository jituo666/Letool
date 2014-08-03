
package com.xjt.newpic.views.opengl;

import android.graphics.RectF;

public interface ScreenNail {

    public int getWidth();

    public int getHeight();

    public void draw(GLESCanvas canvas, int x, int y, int width, int height);

    public void noDraw();// We do not need to draw this ScreenNail in this frame.

    public void recycle();// This ScreenNail will not be used anymore. Release related resources.

    public void draw(GLESCanvas canvas, RectF source, RectF dest);// This is only used by TileImageView to back up the tiles not yet loaded.
}
