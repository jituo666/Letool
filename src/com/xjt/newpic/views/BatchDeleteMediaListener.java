
package com.xjt.newpic.views;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;

import android.view.View;
import android.view.View.OnClickListener;
import android.os.AsyncTask;

import com.xjt.newpic.R;
import com.xjt.newpic.common.LLog;
import com.xjt.newpic.metadata.DataManager;
import com.xjt.newpic.metadata.MediaPath;

import java.util.ArrayList;

/**
 * @Author Jituo.Xuan
 * @Date 4:44:26 PM May 18, 2014
 * @Comments:null
 */
public class BatchDeleteMediaListener implements OnClickListener {

    private static final String TAG = BatchDeleteMediaListener.class.getSimpleName();
    DeleteMediaProgressListener progressListener;
    DataManager manager;
    Context context;
    ArrayList<MediaPath> deleteItems;

    public interface DeleteMediaProgressListener {

        public void onConfirmDialogDismissed(boolean confirmed);

        public ArrayList<MediaPath> onGetDeleteItem();

    }

    public BatchDeleteMediaListener(Activity c, DataManager m, DeleteMediaProgressListener l) {
        progressListener = l;
        context = c;
        manager = m;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.ok_btn) {
            deleteItems = progressListener.onGetDeleteItem();
            if (deleteItems != null && deleteItems.size() > 0) {
                new DeleteMeidaTask().execute();
            }
        } else if (v.getId() == R.id.cancel_btn) {
            if (progressListener != null) {
                progressListener.onConfirmDialogDismissed(false);
            }
        }
    }

    private class DeleteMeidaTask extends AsyncTask<Void, Void, Void> {

        ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(context);
            dialog.setTitle(context.getString(R.string.common_video));
            dialog.setMessage(context.getString(R.string.common_delete_waiting));
            dialog.setIndeterminate(true);
            dialog.setCancelable(true);
            dialog.show();
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            for (MediaPath p : deleteItems) {
                manager.delete(p);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
            if (progressListener != null) {
                progressListener.onConfirmDialogDismissed(true);
            }
        }

    }
}
