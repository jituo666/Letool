
package com.xjt.letool.views.utils;

import android.content.Intent;
import android.graphics.drawable.Drawable;

/**
 * @Author Jituo.Xuan
 * @Date 8:16:51 PM Jul 24, 2014
 * @Comments:null
 */
public class ActionItem {

    private int itemId;
    private String title;
    private Drawable icon;
    private Intent intent;

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    public int getItemId() {
        return itemId;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public void setIcon(Drawable icon) {
        this.icon = icon;
    }

    public Drawable getIcon() {
        return icon;
    }

    public void setIntent(Intent intent) {
        this.intent = intent;
    }

    public Intent getIntent() {
        return intent;
    }
}
