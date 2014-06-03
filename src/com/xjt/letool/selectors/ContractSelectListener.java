
package com.xjt.letool.selectors;

import com.xjt.letool.metadata.MediaPath;

public interface ContractSelectListener {

    public void onSelectionModeChange(int mode);

    public void onSelectionChange(MediaPath path, boolean selected);

}
