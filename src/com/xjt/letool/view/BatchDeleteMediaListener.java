
package com.xjt.letool.view;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;

import android.view.View;
import android.view.View.OnClickListener;
import android.os.AsyncTask;

import com.xjt.letool.R;
import com.xjt.letool.metadata.DataManager;
import com.xjt.letool.metadata.MediaPath;
import com.xjt.letool.selectors.SelectionManager;

/**
 * @Author Jituo.Xuan
 * @Date 4:44:26 PM May 18, 2014
 * @Comments:null
 */
public class BatchDeleteMediaListener implements OnClickListener {

    private static final String TAG = BatchDeleteMediaListener.class.getSimpleName();
    DeleteMediaProgressListener progressListener;
    SelectionManager selectionManager;
    DataManager manager;
    Context context;

    public interface DeleteMediaProgressListener {

        //        public void onConfirmDialogShown();

        public void onConfirmDialogDismissed(boolean confirmed);
        //        public void onProgressStart();
        //        public void onProgressUpdate(int index);
        //        public void onProgressComplete(int result);
    }

    public BatchDeleteMediaListener(Activity c, SelectionManager s, DataManager m, DeleteMediaProgressListener l) {
        progressListener = l;
        context = c;
        selectionManager = s;
        manager = m;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.ok_btn) {
            if (selectionManager != null && selectionManager.getSelectedCount() > 0) {
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
            dialog.setTitle(context.getString(R.string.common_delete));
            dialog.setMessage(context.getString(R.string.common_delete_waiting));
            dialog.setIndeterminate(true);
            dialog.setCancelable(true);
            dialog.show();
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            for (MediaPath p : selectionManager.getSelected(false)) {
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
                progressListener.onConfirmDialogDismissed(false);
            }
        }

    }
}
