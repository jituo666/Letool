
package com.xjt.newpic.edit.controller;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.xjt.newpic.R;
import com.xjt.newpic.edit.colorpicker.ColorListener;
import com.xjt.newpic.edit.editors.Editor;

public class ChoseBorderTexture implements Control {

    private final String TAG = ChoseBorderTexture.class.getSimpleName();

    private final static int SELECTED_BITMAP_SIZE = 64;
    private final static int SELECTED_BORD_SIZE = 2; // dp
    protected ParameterTexture mParameter;
    protected LinearLayout mLinearLayout;
    protected Editor mEditor;
    private View mTopView;
    protected int mLayoutID = R.layout.np_edit_control_texture_chooser;
    private Context mContext;
    private int[] mTexturessID = {
            R.id.draw_texture01,
            R.id.draw_texture02,
            R.id.draw_texture03,
            R.id.draw_texture04,
            R.id.draw_texture05,
    };
    private ImageView[] mTextures = new ImageView[mTexturessID.length];
    private int mSelectedButton = 0;
    private Resources mRes;

    @Override
    public void setUp(ViewGroup container, Parameter parameter, Editor editor) {
        container.removeAllViews();
        mEditor = editor;
        mContext = container.getContext();
        mParameter = (ParameterTexture) parameter;
        mRes = container.getContext().getResources();
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mTopView = inflater.inflate(mLayoutID, container, true);
        mLinearLayout = (LinearLayout) mTopView.findViewById(R.id.listTextures);
        mTopView.setVisibility(View.VISIBLE);

        int[] palette = mParameter.getTexturePalette();
        for (int i = 0; i < mTexturessID.length; i++) {
            final ImageView button = (ImageView) mTopView.findViewById(mTexturessID[i]);
            mTextures[i] = button;
            button.setTag(palette[i]);
            if (mSelectedButton == i) {
                button.setImageBitmap(createSelectedBitmap(palette[i]));
            } else {
                button.setImageResource(palette[i]);
            }
            final int textureNo = i;
            button.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    selectTexture(v, textureNo);
                }
            });
        }
        ImageView button = (ImageView) mTopView.findViewById(R.id.draw_texture_popupbutton);
        button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                showTexturePicker();
            }
        });

    }

    private Bitmap createSelectedBitmap(int resid) {
        Bitmap org = BitmapFactory.decodeResource(mRes, resid);
        int padding = Math.round(mRes.getDisplayMetrics().density * SELECTED_BORD_SIZE);
        int size = SELECTED_BITMAP_SIZE + padding;
        Bitmap result = Bitmap.createBitmap(size, size, Config.ARGB_8888);
        Canvas c = new Canvas(result);
        Drawable d = mRes.getDrawable(R.drawable.grid_pressed);
        d.setBounds(new Rect(0, 0, size, size));
        d.draw(c);
        c.drawBitmap(org, new Rect(0, 0, org.getWidth(), org.getHeight()), new Rect(padding, padding,
                result.getWidth() - padding, result.getHeight() - padding), null);
        return result;
    }

    public void setTextureSet(int[] basTextures) {
        int[] palette = mParameter.getTexturePalette();
        for (int i = 0; i < palette.length; i++) {
            palette[i] = basTextures[i];
            mTextures[i].setTag(palette[i]);
            mTextures[i].setImageResource(palette[i]);
        }

    }

    public int[] getTextureSet() {
        return mParameter.getTexturePalette();
    }

    private void resetBorders() {
        int[] palette = mParameter.getTexturePalette();
        for (int i = 0; i < mTexturessID.length; i++) {
            final ImageView button = mTextures[i];
            button.setImageResource(palette[i]);
            if (mSelectedButton == i) {
                button.setImageBitmap(createSelectedBitmap(palette[i]));
            } else {
                button.setImageResource(palette[i]);
            }
        }
    }

    public void selectTexture(View button, int textureNo) {
        mSelectedButton = textureNo;
        int textureId = (Integer) button.getTag();
        mParameter.setValue(textureId);
        resetBorders();
        mEditor.commitLocalRepresentation();
    }

    @Override
    public View getTopView() {
        return mTopView;
    }

    @Override
    public void setPrameter(Parameter parameter) {
        mParameter = (ParameterTexture) parameter;
        updateUI();
    }

    @Override
    public void updateUI() {
        if (mParameter == null) {
            return;
        }
    }

    //
    public void changeSelectedTexture(int texture) {
        int[] palette = mParameter.getTexturePalette();
        final ImageView button = mTextures[mSelectedButton];
        button.setImageBitmap(createSelectedBitmap(texture));
        palette[mSelectedButton] = texture;
        mParameter.setValue(texture);
        button.setTag(texture);
        mEditor.commitLocalRepresentation();
        button.invalidate();
    }

    //
    public void showTexturePicker() {
        TextureListener cl = new TextureListener() {

            @Override
            public void setTexture(int t) {
                changeSelectedTexture(t);
            }

            @Override
            public void addTextureListener(ColorListener l) {
            }
        };
        PickTextureDialog tpd = new PickTextureDialog(mContext, cl);
        int texture = (Integer) mTextures[mSelectedButton].getTag();
        tpd.setTexture(texture, cl);
        tpd.show();
    }

    public static interface TextureListener {

        void setTexture(int t);

        public void addTextureListener(ColorListener l);
    }

}
