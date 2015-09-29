package uk.porcheron.co_curator.dialog;

import android.os.Handler;

/**
 * Class for stating whether dialogs are visible or not.
 */
public class DialogManager {

    private static boolean mDialogShown = false;

    private static final Handler mHandler = new Handler();
    private static final long DELAY_SHOW_CHANGE = 1500;

    static synchronized void isShown(final boolean shown) {
        mDialogShown = shown;
    }

    static void delayShown(final boolean shown) {
        mHandler.postDelayed(new Runnable() {
            public void run() {
                    mDialogShown = shown;
            }
        }, DELAY_SHOW_CHANGE);

    }

    public synchronized static boolean dialogShown() {
        return mDialogShown;
    }

}
