
package com.xjt.letool.selectors;

import com.xjt.letool.metadata.MediaPath;

/**
 * @Author Jituo.Xuan
 * @Date 8:18:33 PM Jul 24, 2014
 * @Comments:null
 */
public interface ContractSelectListener {

    public void onSelectionModeChange(int mode);

    public void onSelectionChange(MediaPath path, boolean selected);

}
