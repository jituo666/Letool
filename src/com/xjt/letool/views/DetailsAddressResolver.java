package com.xjt.letool.views;

import android.content.Context;
import android.location.Address;
import android.os.Handler;
import android.os.Looper;

import com.xjt.letool.Future;
import com.xjt.letool.FutureListener;
import com.xjt.letool.ThreadPool.Job;
import com.xjt.letool.ThreadPool.JobContext;
import com.xjt.letool.activities.LetoolBaseActivity;
import com.xjt.letool.data.MediaDetails;
import com.xjt.letool.utils.LetoolUtils;

public class DetailsAddressResolver {
    private AddressResolvingListener mListener;
    private final LetoolBaseActivity mContext;
    private Future<Address> mAddressLookupJob;
    private final Handler mHandler;

    private class AddressLookupJob implements Job<Address> {
        private double[] mLatlng;

        protected AddressLookupJob(double[] latlng) {
            mLatlng = latlng;
        }

        @Override
        public Address run(JobContext jc) {
//            ReverseGeocoder geocoder = new ReverseGeocoder(mContext.getAndroidContext());
//            return geocoder.lookupAddress(mLatlng[0], mLatlng[1], true);
            return null;
        }
    }

    public interface AddressResolvingListener {
        public void onAddressAvailable(String address);
    }

    public DetailsAddressResolver(LetoolBaseActivity context) {
        mContext = context;
        mHandler = new Handler(Looper.getMainLooper());
    }

    public String resolveAddress(double[] latlng, AddressResolvingListener listener) {
        mListener = listener;
        mAddressLookupJob = mContext.getThreadPool().submit(
                new AddressLookupJob(latlng),
                new FutureListener<Address>() {
                    @Override
                    public void onFutureDone(final Future<Address> future) {
                        mAddressLookupJob = null;
                        if (!future.isCancelled()) {
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    updateLocation(future.get());
                                }
                            });
                        }
                    }
                });
        return LetoolUtils.formatLatitudeLongitude("(%f,%f)", latlng[0], latlng[1]);
    }

    private void updateLocation(Address address) {
        if (address != null) {
            Context context = mContext.getAndroidContext();
            String parts[] = {
                address.getAdminArea(),
                address.getSubAdminArea(),
                address.getLocality(),
                address.getSubLocality(),
                address.getThoroughfare(),
                address.getSubThoroughfare(),
                address.getPremises(),
                address.getPostalCode(),
                address.getCountryName()
            };

            String addressText = "";
            for (int i = 0; i < parts.length; i++) {
                if (parts[i] == null || parts[i].isEmpty()) continue;
                if (!addressText.isEmpty()) {
                    addressText += ", ";
                }
                addressText += parts[i];
            }
            String text = String.format("%s : %s", DetailsHelper.getDetailsName(
                    context, MediaDetails.INDEX_LOCATION), addressText);
            mListener.onAddressAvailable(text);
        }
    }

    public void cancel() {
        if (mAddressLookupJob != null) {
            mAddressLookupJob.cancel();
            mAddressLookupJob = null;
        }
    }
}
