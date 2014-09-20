
package com.xjt.newpic.edit.controller;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.nineoldandroids.view.ViewHelper;
import com.xjt.newpic.R;
import com.xjt.newpic.edit.editors.Editor;

import java.util.Vector;

public class TextureChooser implements Control {

    private final String TAG = TextureChooser.class.getSimpleName();

    protected ParameterTexture mParameter;
    protected LinearLayout mLinearLayout;
    protected Editor mEditor;
    private View mTopView;
    private Vector<ImageView> mIconButton = new Vector<ImageView>();
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

    int mSelectedButton = 0;

    @Override
    public void setUp(ViewGroup container, Parameter parameter, Editor editor) {
        container.removeAllViews();
        mEditor = editor;
        mContext = container.getContext();
        mParameter = (ParameterTexture) parameter;

        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mTopView = inflater.inflate(mLayoutID, container, true);
        mLinearLayout = (LinearLayout) mTopView.findViewById(R.id.listStyles);
        mTopView.setVisibility(View.VISIBLE);

        mIconButton.clear();
        int[] palette = mParameter.getTexturePalette();
        for (int i = 0; i < mTexturessID.length; i++) {
            final ImageView button = (ImageView) mTopView.findViewById(mTexturessID[i]);
            mTextures[i] = button;
            button.setTag(palette[i]);
            button.setImageResource(palette[i]);
            button.getDrawable().setAlpha( mSelectedButton == i ? 255 : 80);
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
                //showColorPicker();
            }
        });

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
            button.getDrawable().setAlpha( mSelectedButton == i ? 255 : 80);
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
    //    public void changeSelectedColor(float[] hsvo) {
    //        int[] palette = mParameter.getTexturePalette();
    //        int c = Color.HSVToColor((int) (hsvo[3] * 255), hsvo);
    //        final ImageView button = mTextures[mSelectedButton];
    //        GradientDrawable sd = ((GradientDrawable) button.getBackground());
    //        sd.setColor(c);
    //        palette[mSelectedButton] = c;
    //        mParameter.setValue(Color.HSVToColor((int) (hsvo[OPACITY_OFFSET] * 255), hsvo));
    //        button.setTag(hsvo);
    //        mEditor.commitLocalRepresentation();
    //        button.invalidate();
    //    }
    //
    //    public void showColorPicker() {
    //        ColorListener cl = new ColorListener() {
    //
    //            @Override
    //            public void setColor(float[] hsvo) {
    //                changeSelectedColor(hsvo);
    //            }
    //
    //            @Override
    //            public void addColorListener(ColorListener l) {
    //            }
    //        };
    //        ColorPickerDialog cpd = new ColorPickerDialog(mContext, cl);
    //        float[] c = (float[]) mTextures[mSelectedButton].getTag();
    //        cpd.setColor(Arrays.copyOf(c, 4));
    //        cpd.setOrigColor(Arrays.copyOf(c, 4));
    //        cpd.show();
    //    }
}
