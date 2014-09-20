
package com.xjt.newpic.edit.controller;

import android.app.Dialog;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;

import com.xjt.newpic.R;
import com.xjt.newpic.edit.controller.TextureChooser.TextureListener;

public class TexturePickerDialog extends Dialog {

    private GridView mTextureGrid;
    private Button mCancel;
    private TextureListener mTextureListener;

    private static final int[] ALL_TEXTURES = new int[] {
            R.drawable.edit_border_tile1,
            R.drawable.edit_border_tile2,
            R.drawable.edit_border_tile3,
            R.drawable.edit_border_tile4,
            R.drawable.edit_border_tile5,
            R.drawable.edit_border_tile6,
            R.drawable.edit_border_tile7,
            R.drawable.edit_border_tile11,
            R.drawable.edit_border_tile12,
            R.drawable.edit_border_tile13,
            R.drawable.edit_border_tile14,
            R.drawable.edit_border_tile15,
            R.drawable.edit_border_tile16,
            R.drawable.edit_border_tile17,
            R.drawable.edit_border_tile18,
            R.drawable.edit_border_tile19,
            R.drawable.edit_border_tile110,
            R.drawable.edit_border_tile111,
            R.drawable.edit_border_tile112,
            R.drawable.edit_border_tile113,
            R.drawable.edit_border_tile114,
            R.drawable.edit_border_tile115,
            R.drawable.edit_border_tile117,
            R.drawable.edit_border_tile118,
            R.drawable.edit_border_tile119,
            R.drawable.edit_border_tile120
    };

    public TexturePickerDialog(Context context, final TextureListener cl) {
        super(context);
        DisplayMetrics metrics = new DisplayMetrics();
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(metrics);
        int height = metrics.heightPixels * 8 / 10;
        int width = metrics.widthPixels * 8 / 10;
        getWindow().setLayout(width, height);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.np_edit_texture_picker);
        mCancel = (Button) findViewById(R.id.cancelTexturePick);
        mCancel.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                TexturePickerDialog.this.dismiss();
            }
        });
        mTextureGrid = (GridView) findViewById(R.id.texture_grid);
        mTextureGrid.setAdapter(new TextureAdapter());
        mTextureGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> a, View v, int pos, long id) {
                if (mTextureListener != null) {
                    mTextureListener.setTexture(ALL_TEXTURES[pos]);
                }
                TexturePickerDialog.this.dismiss();
            }

        });
    }

    public void setTexture(int texture, TextureListener l) {
        mTextureListener = l;
    }

    public class TextureAdapter extends BaseAdapter {

        public TextureAdapter() {
        }

        @Override
        public int getCount() {
            return ALL_TEXTURES.length;
        }

        @Override
        public Integer getItem(int position) {
            return ALL_TEXTURES[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final View v;
            if (convertView == null) {
                v = getLayoutInflater().inflate(R.layout.np_edit_texture_list_item, parent, false);
            } else {
                v = convertView;
            }
            ImageView texture = (ImageView) v.findViewById(R.id.texture_sample);
            texture.setImageResource(ALL_TEXTURES[position]);

            return v;
        }
    }
}
