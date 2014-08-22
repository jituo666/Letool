package com.xjt.newpic.filtershow.pipeline;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.v4.util.LongSparseArray;
import android.util.Log;

public class ProcessingTaskController implements Handler.Callback {
    private static final String LOGTAG = "ProcessingTaskController";

    private Context mContext;
    private HandlerThread mHandlerThread = null;
    private Handler mProcessingHandler = null;
    private int mCurrentType;
    private LongSparseArray<ProcessingTask> mTasks = new LongSparseArray<ProcessingTask>();

    public final static int RESULT = 1;
    public final static int UPDATE = 2;

    private final Handler mResultHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            ProcessingTask task = mTasks.get(msg.what);
            if (task != null) {
                if (msg.arg1 == RESULT) {
                    task.onResult((ProcessingTask.Result) msg.obj);
                } else if (msg.arg1 == UPDATE) {
                    task.onUpdate((ProcessingTask.Update) msg.obj);
                } else {
                    Log.w(LOGTAG, "received unknown message! " + msg.arg1);
                }
            }
        }
    };

    @Override
    public boolean handleMessage(Message msg) {
        ProcessingTask task = mTasks.get(msg.what);
        if (task != null) {
            task.processRequest((ProcessingTask.Request) msg.obj);
            return true;
        }
        return false;
    }

    public ProcessingTaskController(Context context) {
        mContext = context;
        mHandlerThread = new HandlerThread("ProcessingTaskController",android.os.Process.THREAD_PRIORITY_FOREGROUND);
        mHandlerThread.start();
        mProcessingHandler = new Handler(mHandlerThread.getLooper(), this);
    }

    public Handler getProcessingHandler() {
        return mProcessingHandler;
    }

    public Handler getResultHandler() {
        return mResultHandler;
    }

    public int getReservedType() {
        return mCurrentType++;
    }

    public Context getContext() {
        return mContext;
    }

    public void add(ProcessingTask task) {
        task.added(this);
        mTasks.put(task.getType(), task);
    }

    public void quit() {
        mHandlerThread.quit();
    }
}
