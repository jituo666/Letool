package com.xjt.newpic.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View.MeasureSpec;

import com.xjt.newpic.NpContext;
import com.xjt.newpic.R;
import com.xjt.newpic.metadata.MediaDetails;
import com.xjt.newpic.views.DetailsAddressResolver.AddressResolvingListener;

public class DetailsHelper {
    private static DetailsAddressResolver sAddressResolver;
    private DetailsViewContainer mContainer;

    public interface DetailsSource {
        public int size();
        public int setIndex();
        public MediaDetails getDetails();
    }

    public interface CloseListener {
        public void onClose();
    }

    public interface DetailsViewContainer {
        public void reloadDetails();
        public void setCloseListener(CloseListener listener);
        public void show();
        public void hide();
    }

    public interface ResolutionResolvingListener {
        public void onResolutionAvailable(int width, int height);
    }

    public DetailsHelper(NpContext activity, GLView rootPane, DetailsSource source) {
        mContainer = new DialogDetailsView(activity, source);
    }

    public void layout(int left, int top, int right, int bottom) {
        if (mContainer instanceof GLView) {
            GLView view = (GLView) mContainer;
            view.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.makeMeasureSpec(bottom - top, MeasureSpec.AT_MOST));
            view.layout(0, top, view.getMeasuredWidth(), top + view.getMeasuredHeight());
        }
    }

    public void reloadDetails() {
        mContainer.reloadDetails();
    }

    public void setCloseListener(CloseListener listener) {
        mContainer.setCloseListener(listener);
    }

    public static String resolveAddress(NpContext activity, double[] latlng, AddressResolvingListener listener) {
        if (sAddressResolver == null) {
            sAddressResolver = new DetailsAddressResolver(activity);
        } else {
            sAddressResolver.cancel();
        }
        return sAddressResolver.resolveAddress(latlng, listener);
    }

    public static void resolveResolution(String path, ResolutionResolvingListener listener) {
        Bitmap bitmap = BitmapFactory.decodeFile(path);
        if (bitmap == null) return;
        listener.onResolutionAvailable(bitmap.getWidth(), bitmap.getHeight());
    }

    public static void pause() {
        if (sAddressResolver != null) sAddressResolver.cancel();
    }

    public void show() {
        mContainer.show();
    }

    public void hide() {
        mContainer.hide();
    }

    public static String getDetailsName(Context context, int key) {
        switch (key) {
            case MediaDetails.INDEX_DESCRIPTION:
                return context.getString(R.string.description);
            case MediaDetails.INDEX_DATETIME:
                return context.getString(R.string.time);
            case MediaDetails.INDEX_LOCATION:
                return context.getString(R.string.location);
            case MediaDetails.INDEX_PATH:
                return context.getString(R.string.path);
            case MediaDetails.INDEX_WIDTH:
                return context.getString(R.string.width);
            case MediaDetails.INDEX_HEIGHT:
                return context.getString(R.string.height);
            case MediaDetails.INDEX_ORIENTATION:
                return context.getString(R.string.orientation);
            case MediaDetails.INDEX_DURATION:
                return context.getString(R.string.duration);
            case MediaDetails.INDEX_MIMETYPE:
                return context.getString(R.string.mimetype);
            case MediaDetails.INDEX_SIZE:
                return context.getString(R.string.file_size);
            case MediaDetails.INDEX_MAKE:
                return context.getString(R.string.maker);
            case MediaDetails.INDEX_MODEL:
                return context.getString(R.string.model);
            case MediaDetails.INDEX_FLASH:
                return context.getString(R.string.flash);
            case MediaDetails.INDEX_APERTURE:
                return context.getString(R.string.aperture);
            case MediaDetails.INDEX_FOCAL_LENGTH:
                return context.getString(R.string.focal_length);
            case MediaDetails.INDEX_WHITE_BALANCE:
                return context.getString(R.string.white_balance);
            case MediaDetails.INDEX_EXPOSURE_TIME:
                return context.getString(R.string.exposure_time);
            case MediaDetails.INDEX_ISO:
                return context.getString(R.string.iso);
            default:
                return "Unknown key" + key;
        }
    }
}


