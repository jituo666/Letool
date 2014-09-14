
package com.xjt.newpic.edit.category;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.widget.ArrayAdapter;

import com.xjt.newpic.R;
import com.xjt.newpic.common.LLog;
import com.xjt.newpic.edit.NpEditActivity;
import com.xjt.newpic.edit.filters.FilterRepresentation;
import com.xjt.newpic.edit.imageshow.MasterImage;

public class CategoryAction {

    private static final String TAG = CategoryAction.class.getSimpleName();

    private FilterRepresentation mRepresentation;
    private String mName;
    private Rect mImageFrame;
    private Bitmap mImage;
    public static final int FULL_VIEW = 0;
    public static final int CROP_VIEW = 1;
    public static final int ADD_ACTION = 2;
    public static final int SPACER = 3;
    private int mType = CROP_VIEW;
    private NpEditActivity mContext;
    private ArrayAdapter mAdapter;
    private boolean mCanBeRemoved = false;
    private boolean mIsDoubleAction = false;

    public CategoryAction(NpEditActivity context, FilterRepresentation representation, int type, boolean canBeRemoved) {
        this(context, representation, type);
        mCanBeRemoved = canBeRemoved;
    }

    public CategoryAction(NpEditActivity context, FilterRepresentation representation, int type) {
        mContext = context;
        setType(type);
        mContext.registerAction(this);
        setRepresentation(representation);
        if (representation.getSampleResource() > 0) {
            mImage = BitmapFactory.decodeResource(context.getResources(), representation.getSampleResource());
        } else {
            mImage = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_launcher);
        }
    }

    public CategoryAction(NpEditActivity context, FilterRepresentation representation) {
        this(context, representation, CROP_VIEW);
    }

    public boolean isDoubleAction() {
        return mIsDoubleAction;
    }

    public void setIsDoubleAction(boolean value) {
        mIsDoubleAction = value;
    }

    public boolean canBeRemoved() {
        return mCanBeRemoved;
    }

    public int getType() {
        return mType;
    }

    public FilterRepresentation getRepresentation() {
        return mRepresentation;
    }

    public void setRepresentation(FilterRepresentation representation) {
        mRepresentation = representation;
        mName = representation.getName();
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public void setAdapter(ArrayAdapter adapter) {
        mAdapter = adapter;
    }

    public void setImageFrame(Rect imageFrame, int orientation) {
        if (mImageFrame != null && mImageFrame.equals(imageFrame)) {
            return;
        }
        if (getType() == CategoryAction.ADD_ACTION) {
            return;
        }
//        clearBitmap();
        mImageFrame = imageFrame;
//        Bitmap temp = MasterImage.getImage().getOriginalBitmapSmall();
//        if (temp != null) {
//            mImage = temp;
//        }
//        mAdapter.notifyDataSetChanged();
    }

    public Bitmap getImage() {
        return mImage;
    }

    public void setImage(Bitmap image) {
        mImage = image;
    }

    public void setType(int type) {
        mType = type;
    }

    public void clearBitmap() {
        if (mImage != null && mImage != MasterImage.getImage().getOriginalBitmapSmall()) {
            MasterImage.getImage().getBitmapCache().cache(mImage);
        }
        mImage = null;
    }
}
