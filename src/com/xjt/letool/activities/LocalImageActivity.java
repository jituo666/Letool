
package com.xjt.letool.activities;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import com.xjt.letool.LetoolApp;
import com.xjt.letool.R;
import com.xjt.letool.fragment.GalleryFragment;
import com.xjt.letool.fragment.PhotoFragment;
import com.xjt.letool.metadata.DataManager;
import com.xjt.letool.surpport.TabPageIndicator;

public class LocalImageActivity extends FragmentActivity {

    private static final int IMAGE_FRAGMENT_PHOTO = 0;
    private static final int IMAGE_FRAGMENT_GALLERY = 1;
    private DataManager mDataManager;

    private static final Class<?>[] IMAGE_FRAGMENT_CLASSES = new Class<?>[] {
            PhotoFragment.class,
            GalleryFragment.class
    };

    private static final int[] IMAGE_FRAGMENT_TITLE_RES_IDS = new int[] {
            R.string.common_photo,
            R.string.common_gallery
    };

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.local_images);

        FragmentPagerAdapter adapter = new ImageAdapter(getSupportFragmentManager());

        ViewPager pager = (ViewPager) findViewById(R.id.pager);
        pager.setAdapter(adapter);

        TabPageIndicator indicator = (TabPageIndicator) findViewById(R.id.indicator);
        indicator.setViewPager(pager);
        //
        LetoolApp app = (LetoolApp)getApplication();
        mDataManager = app.getDataManager();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    class ImageAdapter extends FragmentPagerAdapter {

        public ImageAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            try {
                Fragment f = (Fragment) IMAGE_FRAGMENT_CLASSES[position].newInstance();
                if (position == IMAGE_FRAGMENT_PHOTO) {
                    Bundle data = new Bundle();
                    data.putString(ThumbnailActivity.KEY_MEDIA_PATH, mDataManager.getTopSetPath(DataManager.INCLUDE_LOCAL_IMAGE_ONLY));
                    data.putBoolean(ThumbnailActivity.KEY_IS_PHOTO_ALBUM, true);
                    f.setArguments(data);
                } else if (position == IMAGE_FRAGMENT_GALLERY) {
                    Bundle data = new Bundle();
                    data.putString(ThumbnailActivity.KEY_MEDIA_PATH, mDataManager.getTopSetPath(DataManager.INCLUDE_LOCAL_IMAGE_SET_ONLY));
                    f.setArguments(data);
                }
                return f;
            } catch (InstantiationException e) {
                e.printStackTrace();
                return null;
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return getString(IMAGE_FRAGMENT_TITLE_RES_IDS[position]);
        }

        @Override
        public int getCount() {
            return IMAGE_FRAGMENT_CLASSES.length;
        }
    }
}
