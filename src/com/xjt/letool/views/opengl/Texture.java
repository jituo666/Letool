
package com.xjt.letool.views.opengl;

/**
 * @Author Jituo.Xuan
 * @Date 11:38:11 AM Mar 20, 2014
 * @Comments:Texture is a rectangular image which can be drawn on GLCanvas.
 */

//Texture is a rectangular image which can be drawn on GLCanvas.
//The isOpaque() function gives a hint about whether the texture is opaque,
//so the drawing can be done faster. This is the current texture hierarchy:
//
//Texture
//-- ColorTexture
//-- FadeInTexture
//-- FadeOutTexture
//-- BasicTexture
//  -- ExtTexture
//  -- UploadedTexture
//      -- BitmapTexture
//      -- TileTexture
//      -- ResourceTexture
//          -- NinePatchTexture
//      -- CanvasTexture
//          -- StringTexture
//          -- MultiLineTexture
//       -  - StringTexture
//
public interface Texture {

    public int getWidth();

    public int getHeight();

    public void draw(GLESCanvas canvas, int x, int y);

    public void draw(GLESCanvas canvas, int x, int y, int w, int h);

    public boolean isOpaque();
}
