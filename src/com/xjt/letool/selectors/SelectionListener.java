
package com.xjt.letool.selectors;

import com.xjt.letool.data.MediaPath;

public interface SelectionListener {
    public void onSelectionModeChange(int mode);

    public void onSelectionChange(MediaPath path, boolean selected);
}
