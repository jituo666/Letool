
package com.xjt.newpic.surpport;

import android.content.Intent;
import android.graphics.drawable.Drawable;

public class PopupMenuItem {

    private int itemId;
    private String title;
    private Drawable icon;
    private Intent intent;
    private boolean visible;

    public PopupMenuItem() {

    }

    public PopupMenuItem(int id, String t, Drawable ic, Intent it, boolean v) {
        itemId = id;
        title = t;
        icon = ic;
        intent = it;
        visible = v;
    }

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

    public void setVisible(boolean v) {
        visible = v;
    }

    public boolean isVisible() {
        return visible;
    }
}
