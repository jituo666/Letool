package com.xjt.newpic.edit.colorpicker;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.xjt.newpic.R;

import java.util.ArrayList;

public class ColorGridDialog extends Dialog {
    RGBListener mCallback;
    private static final String LOGTAG = "ColorGridDialog";

    public ColorGridDialog(Context context, final RGBListener cl) {
        super(context);
        mCallback = cl;
        setTitle(R.string.color_pick_title);
        setContentView(R.layout.np_edit_color_gird);
        Button sel = (Button) findViewById(R.id.filtershow_cp_custom);
        ArrayList<Button> b = getButtons((ViewGroup) getWindow().getDecorView());
        int k = 0;
        float[] hsv = new float[3];

        for (Button button : b) {
            if (!button.equals(sel)){
                hsv[0] = (k % 5) * 360 / 5;
                hsv[1] = (k / 5) / 3.0f;
                hsv[2] = (k < 5) ? (k / 4f) : 1;
                final int c = (Color.HSVToColor(hsv) & 0x00FFFFFF) | 0xAA000000;
                GradientDrawable sd = ((GradientDrawable) button.getBackground());
                button.setOnClickListener(new View.OnClickListener() {
                        @Override
                    public void onClick(View v) {
                        mCallback.setColor(c);
                        dismiss();
                    }
                });
                sd.setColor(c);
                k++;
            }

        }
        sel.setOnClickListener(new View.OnClickListener() {
                @Override
            public void onClick(View v) {
                showColorPicker();
                ColorGridDialog.this.dismiss();
            }
        });
    }

    private ArrayList<Button> getButtons(ViewGroup vg) {
        ArrayList<Button> list = new ArrayList<Button>();
        for (int i = 0; i < vg.getChildCount(); i++) {
            View v = vg.getChildAt(i);
            if (v instanceof Button) {
                list.add((Button) v);
            } else if (v instanceof ViewGroup) {
                list.addAll(getButtons((ViewGroup) v));
            }
        }
        return list;
    }

    public void showColorPicker() {
        ColorListener cl = new ColorListener() {
                @Override
            public void setColor(float[] hsvo) {
                int c = Color.HSVToColor(hsvo) & 0xFFFFFF;
                int alpha = (int) (hsvo[3] * 255);
                c |= alpha << 24;
                mCallback.setColor(c);
            }
            @Override
            public void addColorListener(ColorListener l) {
            }
        };
        ColorPickerDialog cpd = new ColorPickerDialog(this.getContext(), cl);
        cpd.show();
    }

}
