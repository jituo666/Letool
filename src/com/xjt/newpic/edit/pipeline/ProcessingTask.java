
package com.xjt.newpic.edit.pipeline;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;

public abstract class ProcessingTask {

    private ProcessingTaskController mTaskController;
    private Handler mProcessingHandler;
    private Handler mResultHandler;
    private int mType;
    private static final int DELAY = 300;

    static interface Request {
    }

    static interface Update {
    }

    static interface Result {
    }

    public boolean postRequest(Request message) {
        Message msg = mProcessingHandler.obtainMessage(mType);
        msg.obj = message;
        if (isPriorityTask()) {
            if (mProcessingHandler.hasMessages(getType())) {
                return false;
            }
            mProcessingHandler.sendMessageAtFrontOfQueue(msg);
        } else if (isDelayedTask()) {
            if (mProcessingHandler.hasMessages(getType())) {
                mProcessingHandler.removeMessages(getType());
            }
            mProcessingHandler.sendMessageDelayed(msg, DELAY);
        } else {
            mProcessingHandler.sendMessage(msg);
        }
        return true;
    }

    public void postUpdate(Update message) {
        Message msg = mResultHandler.obtainMessage(mType);
        msg.obj = message;
        msg.arg1 = ProcessingTaskController.UPDATE;
        mResultHandler.sendMessage(msg);
    }

    public void processRequest(Request message) {
        Object result = doInBackground(message);
        Message msg = mResultHandler.obtainMessage(mType);
        msg.obj = result;
        msg.arg1 = ProcessingTaskController.RESULT;
        mResultHandler.sendMessage(msg);
    }

    public void added(ProcessingTaskController taskController) {
        mTaskController = taskController;
        mResultHandler = taskController.getResultHandler(); // 主线程
        mProcessingHandler = taskController.getProcessingHandler(); // 渲染线程
        mType = taskController.getReservedType();
    }

    public int getType() {
        return mType;
    }

    public Context getContext() {
        return mTaskController.getContext();
    }

    public void setOriginal(Bitmap bitmap) {

    };

    public abstract Result doInBackground(Request message);

    public abstract void onResult(Result message);

    public void onUpdate(Update message) {

    }

    public boolean isPriorityTask() {
        return false;
    }

    public boolean isDelayedTask() {
        return false;
    }
}
