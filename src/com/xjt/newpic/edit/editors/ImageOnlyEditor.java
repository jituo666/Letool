package com.xjt.newpic.edit.editors;

import android.content.Context;
import android.widget.FrameLayout;

import com.xjt.newpic.R;
import com.xjt.newpic.edit.imageshow.ImageShow;

/**
 * The editor with no slider for filters without UI
 */
public class ImageOnlyEditor extends Editor {
    public final static int ID = R.id.imageOnlyEditor;
    private final String LOGTAG = "ImageOnlyEditor";

    public ImageOnlyEditor() {
        super(ID);
    }

    protected ImageOnlyEditor(int id) {
        super(id);
    }

    public boolean useUtilityPanel() {
        return false;
    }

    @Override
    public void createEditor(Context context, FrameLayout frameLayout) {
        super.createEditor(context, frameLayout);
        mView = mImageShow = new ImageShow(context);
    }

}
