package com.xjt.letool.view;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.os.AsyncTask;

import com.xjt.letool.R;
import com.xjt.letool.metadata.DataManager;
import com.xjt.letool.metadata.MediaPath;

/**
 * @Author Jituo.Xuan
 * @Date 4:44:26 PM May 18, 2014
 * @Comments:null
 */
public class SingleDeleteMediaListener implements OnClickListener, OnCancelListener {

    DeleteMediaProgressListener progressListener;
    DataManager manager;
    Context context;
    MediaPath mPath;

    public interface DeleteMediaProgressListener {

        public void onConfirmDialogDismissed(boolean confirmed);
    }

    public SingleDeleteMediaListener(Activity c, MediaPath p, DataManager m, DeleteMediaProgressListener l) {
        context = c;
        manager = m;
        mPath = p;
        progressListener = l;
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (which == DialogInterface.BUTTON_POSITIVE) {
            if (mPath != null) {
                new DeleteMeidaTask().execute();
                dialog.dismiss();
            }
        } else {
            if (progressListener != null) {
                progressListener.onConfirmDialogDismissed(false);
            }
        }
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        if (progressListener != null) {
            progressListener.onConfirmDialogDismissed(false);
        }
    }

    private class DeleteMeidaTask extends AsyncTask<Void, Void, Void> {

        ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(context);
            dialog.setTitle(context.getString(R.string.common_delete));
            dialog.setMessage(context.getString(R.string.common_delete_waiting));
            dialog.setIndeterminate(true);
            dialog.setCancelable(true);
            dialog.show();
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            manager.delete(mPath);
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
            if (progressListener != null) {
                progressListener.onConfirmDialogDismissed(false);
            }
        }

    }
}
