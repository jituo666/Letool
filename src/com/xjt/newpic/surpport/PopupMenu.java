
package com.xjt.newpic.surpport;

import java.util.ArrayList;
import java.util.List;

import com.xjt.newpic.R;
import com.xjt.newpic.common.LLog;

import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

public class PopupMenu implements PopupWindow.OnDismissListener {

    private static final String TAG = PopupMenu.class.getSimpleName();

    private Context mContext;
    private LayoutInflater mInflater;
    private View mAnchor;
    private WindowManager mWindowManager;
    private PopupWindow mPopupWindow;
    private ListView mItemsView;
    private OnMenuItemClickListener mListener;
    private OnDismissListener mDismissListener;

    private List<PopupMenuItem> mItems;
    private int mWidth = 160; //dp
    private float mScale;
    private int mDensity;

    public interface OnDismissListener {

        public void onDismiss(PopupMenu menu);
    }

    public interface OnMenuItemClickListener {

        public boolean onMenuItemClick(PopupMenuItem item);
    }

    public PopupMenu(Context context) {
        mContext = context;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        mWindowManager.getDefaultDisplay().getMetrics(metrics);
        mScale = metrics.scaledDensity;
        mDensity = Math.round(metrics.density);

        mItems = new ArrayList<PopupMenuItem>();

        mPopupWindow = new PopupWindow(context);
        mPopupWindow.setTouchInterceptor(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
                    mPopupWindow.dismiss();
                    return true;
                }
                return false;
            }
        });
        mPopupWindow.setOnDismissListener(this);

        setContentView(mInflater.inflate(R.layout.popup_menu, null));
    }

    public PopupMenu(Context context, View anchor) {
        this(context);
        mAnchor = anchor;
    }

    /**
     * Add menu item.
     *
     * @param itemId
     * @param titleRes
     * @param iconRes
     *
     * @return item
     */
    public PopupMenuItem add(int itemId, int titleRes) {
        PopupMenuItem item = new PopupMenuItem();
        item.setItemId(itemId);
        item.setTitle(mContext.getString(titleRes));
        item.setVisible(true);
        mItems.add(item);
        return item;
    }

    public int size() {
        return mItems.size();
    }

    public PopupMenuItem getItem(int index) {
        if (index >= 0 && index < mItems.size()) {
            return mItems.get(index);
        }
        return null;
    }

    public void show() {
        if (mItems.size() == 0) {
            throw new IllegalStateException("PopupMenu#add was not called with a menu item to display.");
        }
        preShow();
        MenuItemAdapter adapter = new MenuItemAdapter(mContext, mItems);
        mItemsView.setAdapter(adapter);
        mItemsView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (mListener != null) {
                    mListener.onMenuItemClick(mItems.get(position));
                }
                mPopupWindow.dismiss();
            }
        });
        if (mAnchor == null) {
            View parent = ((Activity) mContext).getWindow().getDecorView();
            mPopupWindow.showAtLocation(parent, Gravity.CENTER, 0, 0);
            return;
        }
        int[] location = new int[2];
        mAnchor.getLocationOnScreen(location);
        mPopupWindow.showAtLocation(mAnchor, Gravity.NO_GRAVITY, location[0], location[1] - mItemsView.getCount() * mAnchor.getHeight()
                + (mItemsView.getCount() * 2) * mDensity);
    }

    public boolean isShowing() {
        return mPopupWindow.isShowing();
    }

    public void dismiss() {
        if (mPopupWindow != null && mPopupWindow.isShowing()) {
            mPopupWindow.dismiss();
        }
    }

    public void setWidth(int width) {
        mWidth = width;
    }

    public void setOnMenuItemClickListener(OnMenuItemClickListener listener) {
        mListener = listener;
    }

    public void setOnDismissListener(OnDismissListener listener) {
        mDismissListener = listener;
    }

    private void setContentView(View contentView) {
        mItemsView = (ListView) contentView.findViewById(R.id.items);
        mPopupWindow.setContentView(contentView);
    }

    private void preShow() {
        int width = (int) (mWidth * mScale);
        mPopupWindow.setWidth(width);
        mPopupWindow.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        mPopupWindow.setTouchable(true);
        mPopupWindow.setFocusable(true);
        mPopupWindow.setOutsideTouchable(true);
        mPopupWindow.setAnimationStyle(android.R.style.Animation_Dialog);
    }

    static class ViewHolder {

        ImageView icon;
        TextView title;
    }

    private class MenuItemAdapter extends ArrayAdapter<PopupMenuItem> {

        public MenuItemAdapter(Context context, List<PopupMenuItem> objects) {
            super(context, 0, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            ViewHolder holder;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.popup_menu_item, null);
                holder = new ViewHolder();
                holder.icon = (ImageView) convertView.findViewById(R.id.icon);
                holder.title = (TextView) convertView.findViewById(R.id.title);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            PopupMenuItem item = getItem(position);
            if (item.getIcon() != null) {
                holder.icon.setImageDrawable(item.getIcon());
                holder.icon.setVisibility(View.VISIBLE);
            } else {
                holder.icon.setVisibility(View.GONE);
            }
            holder.title.setText(item.getTitle());
            convertView.setVisibility(item.isVisible() ? View.VISIBLE : View.GONE);
            return convertView;
        }
    }

    @Override
    public void onDismiss() {
        if (mDismissListener != null) {
            mDismissListener.onDismiss(this);
        }
    }
}
