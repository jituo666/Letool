
package com.xjt.letool.share;

import android.app.Activity;
import android.os.Bundle;

public interface ShareListener {

    public void onShareTo(Activity activity, int shareTo, Bundle shareData);
}
