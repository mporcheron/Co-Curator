package uk.porcheron.co_curator.util;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Calendar;

import uk.porcheron.co_curator.TimelineActivity;

/**
 * Event logging
 */
public class CCLog {
    private static final String TAG = "CC:CCLog";

    private static final String FILENAME = "events.log";

    private static TimelineActivity mActivity = null;
    private static File mFile = null;

    public static void write(final Event event) {
        write(event, "");
    }

    public static void write(final Event event, final String data) {
        if(!isExternalStorageWritable()) {
            Log.e(TAG, "Cannot write to log file, no external storage");
            return;
        }

        if(mActivity == null) {
            mActivity = TimelineActivity.getInstance();
        }

        if(mFile == null) {
            File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
            if (!dir.exists() && !dir.mkdirs()) {
                Log.e(TAG, "Directory not created");
                return;
            }

            mFile = new File(dir, FILENAME);
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                try (FileOutputStream oS = new FileOutputStream(mFile, true)) {
                    Calendar c = Calendar.getInstance();

                    String line =
                            c.getTime().toString() + "\t" +
                            event.toString() + "\t" +
                            data + "\n";

                    oS.write(line.getBytes());
                } catch (Exception e) {
                    Log.e(TAG, "Could not append log file");
                    e.printStackTrace();
                }
            }
        }).start();
    }

    protected static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

}
