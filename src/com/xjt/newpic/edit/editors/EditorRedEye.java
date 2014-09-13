
package com.xjt.newpic.edit.editors;

import android.content.Context;
import android.widget.FrameLayout;

import com.xjt.newpic.R;
import com.xjt.newpic.edit.filters.FilterRedEyeRepresentation;
import com.xjt.newpic.edit.filters.FilterRepresentation;
import com.xjt.newpic.edit.imageshow.ImageRedEye;

/**
 * The editor with no slider for filters without UI
 */
public class EditorRedEye extends Editor {

    private final String TAG = EditorRedEye.class.getSimpleName();

    public static int ID = R.id.editorRedEye;

    ImageRedEye mImageRedEyes;

    public EditorRedEye() {
        super(ID);
    }

    protected EditorRedEye(int id) {
        super(id);
    }

    @Override
    public void createEditor(Context context, FrameLayout frameLayout) {
        super.createEditor(context, frameLayout);
        mView = mImageShow = mImageRedEyes = new ImageRedEye(context);
        mImageRedEyes.setEditor(this);
    }

    @Override
    public void reflectCurrentFilter() {
        super.reflectCurrentFilter();
        FilterRepresentation rep = getLocalRepresentation();
        if (rep != null && getLocalRepresentation() instanceof FilterRedEyeRepresentation) {
            FilterRedEyeRepresentation redEyeRep = (FilterRedEyeRepresentation) rep;

            mImageRedEyes.setRepresentation(redEyeRep);
        }
    }

    @Override
    public boolean showsSeekBar() {
        return false;
    }
}
