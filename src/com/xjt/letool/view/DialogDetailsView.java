
package com.xjt.letool.view;

import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.xjt.letool.LetoolContext;
import com.xjt.letool.R;
import com.xjt.letool.metadata.MediaDetails;
import com.xjt.letool.utils.Utils;
import com.xjt.letool.view.DetailsAddressResolver.AddressResolvingListener;
import com.xjt.letool.view.DetailsHelper.CloseListener;
import com.xjt.letool.view.DetailsHelper.DetailsSource;
import com.xjt.letool.view.DetailsHelper.DetailsViewContainer;
import com.xjt.letool.view.DetailsHelper.ResolutionResolvingListener;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Map.Entry;

public class DialogDetailsView implements DetailsViewContainer {

    @SuppressWarnings("unused")
    private static final String TAG = DialogDetailsView.class.getSimpleName();

    private final LetoolContext mLetoolContext;
    private DetailsAdapter mAdapter;
    private MediaDetails mDetails;
    private final DetailsSource mSource;
    private int mIndex;
    private LetoolDialog mDialog;
    private CloseListener mListener;

    public DialogDetailsView(LetoolContext context, DetailsSource source) {
        mLetoolContext = context;
        mSource = source;
    }

    @Override
    public void show() {
        reloadDetails();
        mDialog.show();
    }

    @Override
    public void hide() {
        mDialog.hide();
    }

    @Override
    public void reloadDetails() {
        int index = mSource.setIndex();
        if (index == -1)
            return;
        MediaDetails details = mSource.getDetails();
        if (details != null) {
            if (mIndex == index && mDetails == details)
                return;
            mIndex = index;
            mDetails = details;
            setDetails(details);
        }
    }

    private void setDetails(MediaDetails details) {
        mAdapter = new DetailsAdapter(details);
        String title = String.format(mLetoolContext.getActivityContext().getString(R.string.details_title), mIndex + 1, mSource.size());

        mDialog = new LetoolDialog(mLetoolContext.getActivityContext());
        mDialog.setListAdapter(mAdapter);
        mDialog.setTitle(title);
        mDialog.setOkBtn(R.string.common_close, new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mListener.onClose();
            }
        });
        mDialog.setOnDismissListener(new OnDismissListener() {

            @Override
            public void onDismiss(DialogInterface arg0) {
                mListener.onClose();
            }

        });
    }

    private class PropertyData {

        String title;
        String content;
    }

    private class DetailsAdapter extends BaseAdapter implements AddressResolvingListener, ResolutionResolvingListener {

        private final ArrayList<PropertyData> mItems;
        private int mLocationIndex;
        private final Locale mDefaultLocale = Locale.getDefault();
        private final DecimalFormat mDecimalFormat = new DecimalFormat(".####");
        private int mWidthIndex = -1;
        private int mHeightIndex = -1;

        public DetailsAdapter(MediaDetails details) {
            Context context = mLetoolContext.getActivityContext();
            mItems = new ArrayList<PropertyData>(details.size());
            mLocationIndex = -1;
            setDetails(context, details);
        }

        private void setDetails(Context context, MediaDetails details) {
            boolean resolutionIsValid = true;
            String path = null;
            for (Entry<Integer, Object> detail : details) {
                String value;
                switch (detail.getKey()) {
                    case MediaDetails.INDEX_LOCATION: {
                        double[] latlng = (double[]) detail.getValue();
                        mLocationIndex = mItems.size();
                        value = DetailsHelper.resolveAddress(mLetoolContext, latlng, this);
                        break;
                    }
                    case MediaDetails.INDEX_SIZE: {
                        value = Formatter.formatFileSize(
                                context, (Long) detail.getValue());
                        break;
                    }
                    case MediaDetails.INDEX_WHITE_BALANCE: {
                        value = "1".equals(detail.getValue())
                                ? context.getString(R.string.manual)
                                : context.getString(R.string.auto);
                        break;
                    }
                    case MediaDetails.INDEX_FLASH: {
                        MediaDetails.FlashState flash = (MediaDetails.FlashState) detail.getValue();
                        // TODO: camera doesn't fill in the complete values, show more information
                        // when it is fixed.
                        if (flash.isFlashFired()) {
                            value = context.getString(R.string.flash_on);
                        } else {
                            value = context.getString(R.string.flash_off);
                        }
                        break;
                    }
                    case MediaDetails.INDEX_EXPOSURE_TIME: {
                        value = (String) detail.getValue();
                        double time = Double.valueOf(value);
                        if (time < 1.0f) {
                            value = String.format(mDefaultLocale, "%d/%d", 1,
                                    (int) (0.5f + 1 / time));
                        } else {
                            int integer = (int) time;
                            time -= integer;
                            value = String.valueOf(integer) + "''";
                            if (time > 0.0001) {
                                value += String.format(mDefaultLocale, " %d/%d", 1,
                                        (int) (0.5f + 1 / time));
                            }
                        }
                        break;
                    }
                    case MediaDetails.INDEX_WIDTH:
                        mWidthIndex = mItems.size();
                        if (detail.getValue().toString().equalsIgnoreCase("0")) {
                            value = context.getString(R.string.unknown);
                            resolutionIsValid = false;
                        } else {
                            value = toLocalInteger(detail.getValue());
                        }
                        break;
                    case MediaDetails.INDEX_HEIGHT: {
                        mHeightIndex = mItems.size();
                        if (detail.getValue().toString().equalsIgnoreCase("0")) {
                            value = context.getString(R.string.unknown);
                            resolutionIsValid = false;
                        } else {
                            value = toLocalInteger(detail.getValue());
                        }
                        break;
                    }
                    case MediaDetails.INDEX_PATH:
                        // Prepend the new-line as a) paths are usually long, so
                        // the formatting is better and b) an RTL UI will see it
                        // as a separate section and interpret it for what it
                        // is, rather than trying to make it RTL (which messes
                        // up the path).
                        value = detail.getValue().toString();
                        path = detail.getValue().toString();
                        break;
                    case MediaDetails.INDEX_ISO:
                        value = toLocalNumber(Integer.parseInt((String) detail.getValue()));
                        break;
                    case MediaDetails.INDEX_FOCAL_LENGTH:
                        double focalLength = Double.parseDouble(detail.getValue().toString());
                        value = toLocalNumber(focalLength);
                        break;
                    case MediaDetails.INDEX_ORIENTATION:
                        value = toLocalInteger(detail.getValue());
                        break;
                    default: {
                        Object valueObj = detail.getValue();
                        // This shouldn't happen, log its key to help us diagnose the problem.
                        if (valueObj == null) {
                            Utils.fail("%s's value is Null", DetailsHelper.getDetailsName(context, detail.getKey()));
                        }
                        value = valueObj.toString();
                    }
                }
                int key = detail.getKey();
                PropertyData p = new PropertyData();
                p.title = DetailsHelper.getDetailsName(context, key) + ":";
                if (details.hasUnit(key)) {
                    p.content = String.format(" %s %s", value, context.getString(details.getUnit(key)));
                } else {
                    p.content = String.format(" %s", value);
                }
                mItems.add(p);
            }
            if (!resolutionIsValid) {
                DetailsHelper.resolveResolution(path, this);
            }
        }

        @Override
        public boolean areAllItemsEnabled() {
            return false;
        }

        @Override
        public boolean isEnabled(int position) {
            return false;
        }

        @Override
        public int getCount() {
            return mItems.size();
        }

        @Override
        public Object getItem(int position) {
            return mDetails.getDetail(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final View v;
            if (convertView == null) {
                v = LayoutInflater.from(mLetoolContext.getActivityContext()).inflate(R.layout.details_list_item, parent, false);
            } else {
                v = convertView;
            }
            TextView title = (TextView) v.findViewById(R.id.property_title);
            title.setText(mItems.get(position).title);
            TextView imageView = (TextView) v.findViewById(R.id.property_content);
            imageView.setText(mItems.get(position).content);

            return v;
        }

        @Override
        public void onAddressAvailable(String address) {
            mItems.get(mLocationIndex).content = address;
            notifyDataSetChanged();
        }

        @Override
        public void onResolutionAvailable(int width, int height) {
            if (width == 0 || height == 0)
                return;
            // Update the resolution with the new width and height
            Context context = mLetoolContext.getActivityContext();
            String widthString = String.format(mDefaultLocale, "%s: %d",
                    DetailsHelper.getDetailsName(
                            context, MediaDetails.INDEX_WIDTH), width);
            String heightString = String.format(mDefaultLocale, "%s: %d",
                    DetailsHelper.getDetailsName(
                            context, MediaDetails.INDEX_HEIGHT), height);
            mItems.get(mWidthIndex).content = String.valueOf(widthString);
            mItems.get(mHeightIndex).content = String.valueOf(heightString);
            notifyDataSetChanged();
        }

        /**
         * Converts the given integer (given as String or Integer object) to a
         * localized String version.
         */
        private String toLocalInteger(Object valueObj) {
            if (valueObj instanceof Integer) {
                return toLocalNumber((Integer) valueObj);
            } else {
                String value = valueObj.toString();
                try {
                    value = toLocalNumber(Integer.parseInt(value));
                } catch (NumberFormatException ex) {
                    // Just keep the current "value" if we cannot
                    // parse it as a fallback.
                }
                return value;
            }
        }

        /** Converts the given integer to a localized String version. */
        private String toLocalNumber(int n) {
            return String.format(mDefaultLocale, "%d", n);
        }

        /** Converts the given double to a localized String version. */
        private String toLocalNumber(double n) {
            return mDecimalFormat.format(n);
        }
    }

    @Override
    public void setCloseListener(CloseListener listener) {
        mListener = listener;
    }
}
