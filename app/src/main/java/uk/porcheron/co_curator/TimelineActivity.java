package uk.porcheron.co_curator;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.util.SparseArray;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import java.util.Timer;
import java.util.TimerTask;

import uk.porcheron.co_curator.collo.ColloCompass;
import uk.porcheron.co_curator.collo.ColloDict;
import uk.porcheron.co_curator.collo.ColloManager;
import uk.porcheron.co_curator.db.DbLoader;
import uk.porcheron.co_curator.item.ItemList;
import uk.porcheron.co_curator.item.ItemPhoto;
import uk.porcheron.co_curator.item.ItemScrollView;
import uk.porcheron.co_curator.item.ItemType;
import uk.porcheron.co_curator.item.ItemUrl;
import uk.porcheron.co_curator.item.dialog.DialogNote;
import uk.porcheron.co_curator.item.dialog.DialogUrl;
import uk.porcheron.co_curator.point.Pointer;
import uk.porcheron.co_curator.user.User;
import uk.porcheron.co_curator.user.UserList;
import uk.porcheron.co_curator.util.AnimationReactor;
import uk.porcheron.co_curator.util.CCLog;
import uk.porcheron.co_curator.util.Event;
import uk.porcheron.co_curator.util.Image;
import uk.porcheron.co_curator.util.Web;
import uk.porcheron.co_curator.val.Instance;
import uk.porcheron.co_curator.val.Phone;
import uk.porcheron.co_curator.val.Style;

public class TimelineActivity extends Activity implements View.OnLongClickListener,
        SurfaceHolder.Callback, ColloManager.ResponseHandler {
    private static final String TAG = "CC:TimelineActivity";

    private static boolean mCreated = false;
    private static TimelineActivity mInstance;

    private Timer mUpdateTimer;
    final Handler mUpdateHandler = new Handler();

    private View.OnTouchListener mGestureDetectorAbove;
    private View.OnTouchListener mGestureDetectorBelow;
    public ScaleGestureDetector mScaleDetector;

    private RelativeLayout mTimeline;
    private SurfaceHolder mSurfaceHolder;
    private ProgressDialog mProgressDialog;
    private FrameLayout mFrameLayout;

    private static final int FADE_TIME_ITEMS = 1000;
    private static final int FADE_DELAY = 500;

    public static final int PICK_PHOTO = 101;

    private boolean mUnbindAll = true;

    private float mLayoutTouchX = -1;
    private float mLayoutTouchY = -1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check the user has previously authenticated
        SharedPreferences sharedPrefs = getSharedPreferences(getString(R.string.prefFile), Context.MODE_PRIVATE);
        int globalUserId = sharedPrefs.getInt(getString(R.string.prefGlobalUserId), -1);
        int userId = sharedPrefs.getInt(getString(R.string.prefUserId), -1);
        int groupId = sharedPrefs.getInt(getString(R.string.prefGroupId), -1);
        String serverAddress = sharedPrefs.getString(getString(R.string.prefServerAddress), null);
        if(globalUserId >= 0 && userId >= 0 && groupId >= 0 && serverAddress != null && !serverAddress.isEmpty()) {
            Instance.globalUserId = globalUserId;
            Instance.userId = userId;
            Instance.groupId = groupId;
            Instance.serverAddress = serverAddress;
            Instance.addedUsers = 0;
            Instance.drawnUsers = 0;
            Log.d(TAG, "I am " + globalUserId + ":" + userId + ":" + groupId);
        } else {
            Intent intent = new Intent(this, ParticipantActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        // Setup the activity
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);

        setContentView(R.layout.activity_timelime);

        mInstance = this;

        CCLog.write(Event.APP_CREATE, Instance.asString());

        // Load various static values
        Phone.collectAttrs();
        Style.collectAttrs();

        // Begin preparation for drawing the UI
        mTimeline = (RelativeLayout) findViewById(R.id.timeline);
        SurfaceView surfaceView = (SurfaceView) findViewById(R.id.surface);
        mSurfaceHolder = surfaceView.getHolder();
        mSurfaceHolder.addCallback(this);

        ItemScrollView scrollView = (ItemScrollView) findViewById(R.id.horizontalScrollView);
        scrollView.setSmoothScrollingEnabled(true);

        mFrameLayout = (FrameLayout) findViewById(R.id.frameLayout);

        LinearLayout layoutAbove = (LinearLayout) findViewById(R.id.layoutAboveCentre);
        LinearLayout layoutBelow = (LinearLayout) findViewById(R.id.layoutBelowCentre);

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, Style.layoutHalfPadding, 0, 0);

        final int padRight = (int) (Phone.screenWidth * Style.autoscrollExtra);
        layoutAbove.setPadding(0, 0, padRight, Style.layoutHalfPadding);
        layoutBelow.setPadding(0, 0, padRight, 0);
        layoutBelow.setLayoutParams(params);

        // Gesture recognition
        float bottomOffset = Style.layoutHalfHeight - (2 * Style.layoutCentreHeight);
        mGestureDetectorAbove = new TimelineGestureDetector(0);
        mGestureDetectorBelow = new TimelineGestureDetector(bottomOffset);
        mScaleDetector = new ScaleGestureDetector(this, new OverviewDetector());
//        mFrameLayout.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(final View view, final MotionEvent event) {
//                return mFrameLayout.onTouch(event) || mScaleDetector.onTouchEvent(event);
//            }
//        });

        layoutAbove.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(final View view, final MotionEvent event) {
                mGestureDetectorAbove.onTouch(view, event);
                return mScaleDetector.onTouchEvent(event);
            }
        });
        layoutBelow.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(final View view, final MotionEvent event) {
                mGestureDetectorBelow.onTouch(view, event);
                return mScaleDetector.onTouchEvent(event);
            }
        });

        mUnbindAll = true;

//        if(mCreated) {
//            return;
//        }

        showLoadingDialog(R.string.dialogLoading);

        // Load items
        Instance.users = new UserList();
        Instance.items = new ItemList(scrollView, layoutAbove, layoutBelow);

        ColloManager.ResponseManager.registerHandler(ColloDict.ACTION_UNBIND, this);
        ColloManager.ResponseManager.registerHandler(ColloDict.ACTION_POINT, this);

        new DbLoader().execute();
    }

    public static TimelineActivity getInstance() {
        return mInstance;
    }

    @Override
    public void onResume() {
        CCLog.write(Event.APP_RESUME);

        Phone.collectAttrs();

        // Begin pitch tracking
        ColloCompass.getInstance().resumeListening();

        // Reschedule IP pinging
        try {
            mUpdateTimer = new Timer();
            mUpdateTimer.schedule(mUpdateUserTask, 1000, ColloManager.BEAT_EVERY);
        } catch(IllegalStateException e) {
        }

        // Fade in if not visible
        if(mFrameLayout.getAlpha() == 0f) {
            mFrameLayout.setVisibility(View.VISIBLE);
            mFrameLayout.setAlpha(1f);
        }

        super.onResume();
    }

    @Override
    public void onPause() {
        ColloCompass.getInstance().pauseListening();

        mUpdateUserTask.cancel();
        mUpdateTimer.cancel();
        mUpdateTimer.purge();

        super.onPause();
    }

    @Override
    public void onDestroy() {
        ColloManager.broadcast(ColloDict.ACTION_UNBIND);
        super.onDestroy();
    }

    @Override
    public boolean onLongClick(View v) {
        CCLog.write(Event.APP_LONG_CLICK);

        promptAdd(mLayoutTouchX);
        return true;
    }

    public void showLoadingDialog(int str) {
        mProgressDialog = ProgressDialog.show(this, "", getText(str), true);
    }

    public void fadeOut(final AnimationReactor listener) {
        mFrameLayout.animate()
                .alpha(0f)
                .setDuration(FADE_TIME_ITEMS)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mFrameLayout.setVisibility(View.INVISIBLE);
                        if (listener != null) {
                            listener.onAnimationEnd(animation);
                        }
                    }
                });
    }

    public void fadeIn(final AnimationReactor listener) {
        mFrameLayout.animate()
                .alpha(1f)
                .setDuration(FADE_TIME_ITEMS)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        if (listener != null) {
                            listener.onAnimationEnd(animation);
                        }
                    }

                    @Override
                    public void onAnimationStart(Animator animation) {
                        mFrameLayout.setVisibility(View.VISIBLE);
                    }
                });
    }

    public void hideLoadingDialog() {
        mProgressDialog.hide();
    }

    public void promptAdd(float x) {
        CCLog.write(Event.APP_PROMPT_ADD, "{x=" + mLayoutTouchX + "}");
        Log.v(TAG, "User long press at (" + x + ")");
        promptNewItem(Instance.items.isEmpty());

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.v(TAG, "Received ActivityResult (requestCode=" + requestCode + ",resultCode=" + resultCode + ")");

        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode != Activity.RESULT_OK) {
            return;
        }

        if (requestCode == PICK_PHOTO) {
            if (data == null) {
                Log.e(TAG, "No data retrieved...");
                return;
            }


            showLoadingDialog(R.string.dialogAddingImage);

            Uri selectedImage = data.getData();
            String[] filePathColumn = { MediaStore.Images.Media.DATA };

            Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String filename = cursor.getString(columnIndex);
            cursor.close();

            CCLog.write(Event.APP_PHOTO_ADD, "{filename=" + filename + "}");

            Log.v(TAG, "File selected by user: " + filename);

            int width = ItemPhoto.getThumbnailWidth();
            int height = ItemPhoto.getThumbnailHeight();

            synchronized (Instance.items) {
                final int itemId = Instance.items.size();
                final String destination = Instance.globalUserId + "-" + System.currentTimeMillis();
                Image.file2file(filename, destination, width, height, new Runnable() {
                    @Override
                    public void run() {
                        long dateTime = (System.currentTimeMillis() / 1000L);
                        if (!Instance.items.add(itemId, ItemType.PHOTO, Instance.user(), destination, dateTime, false, true, true)) {
                            Log.e(TAG, "Failed to save image");
                        }

                        TimelineActivity.getInstance().hideLoadingDialog();
                    }
                });
            }
        }
    }

    @Override
    public boolean respond(String action, int globalUserId, String... data) {
        switch(action) {
            case ColloDict.ACTION_UNBIND:
                ColloManager.unBindFromUser(globalUserId);
                return true;

            case ColloDict.ACTION_POINT:
                if(!ColloManager.isBoundTo(globalUserId)) {
                    Log.v(TAG, "Ignore pointer, not bound to user");
                    return false;
                }

                final User u = Instance.users.getByGlobalUserId(globalUserId);
                if(u == null) {
                    Log.e(TAG, "Badly formed globalUserId");
                    return true;
                }

                try {
                    final float x = Float.parseFloat(data[0]);
                    final float y = Float.parseFloat(data[1]);
                    TimelineActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showPointer(u, x, y);
                        }
                    });
                    return true;
                } catch(NumberFormatException e) {
                    Log.e(TAG, "Badly formed ACTION_POINT");
                }

                return false;
        }

        return false;
    }


    public void promptNewItem(final boolean forceAdd) {
        CCLog.write(Event.TL_NEW_ITEM, "{forceAdd=" + forceAdd + "}");

        AlertDialog.Builder builder = new AlertDialog.Builder(TimelineActivity.this);
        builder.setTitle(R.string.dialog_add_message);

        if(forceAdd) {
            builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    promptNewItem(true);
                }
            });
            builder.setCancelable(false);
        }

        final ItemType[] types = ItemType.values();
        CharSequence[] typeLabels = new CharSequence[types.length - 1];
        for (int i = 0; i < typeLabels.length; i++) {
            typeLabels[i] = getString(types[i+1].getLabel()); // first item is the UNKNOWN type
        }

        builder.setItems(typeLabels, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                ItemType type = types[which + 1]; // first item is the UNKNOWN type
                switch (type) {
                    case PHOTO:
                        addNewPhoto();
                        break;

                    case NOTE:
                        addNewNote(forceAdd);
                        break;

                    case URL:
                        addNewUrl(forceAdd);
                        break;
                }
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void addNewNote(final boolean promptOnCancel) {
        CCLog.write(Event.TL_NEW_NOTE, "{promptOnCancel=" + promptOnCancel + "}");

        new DialogNote(this)
                .setAutoEdit(true)
                .setOnSubmitListener(new DialogNote.OnSubmitListener() {
                    @Override
                    public void onSubmit(DialogInterface dialog, String text) {
                        Log.e(TAG, "Note Submitted");

                        if (promptOnCancel && text.isEmpty()) {
                            promptNewItem(true);
                        }

                        CCLog.write(Event.TL_NEW_SAVE, "{" + text + "}");

                        synchronized (Instance.items) {
                            final int itemId = Instance.items.size();

                            long dateTime = Instance.items.getDateTimeClosestTo(mLayoutTouchX);

                            boolean create = Instance.items.add(itemId, ItemType.NOTE, Instance.user(), text, dateTime, false, true, true);
                            if (promptOnCancel && !create) {
                                promptNewItem(true);
                            }
                        }
                    }
                })
                .setOnCancelListener(new DialogNote.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        CCLog.write(Event.TL_NEW_CANCEL);
                        if (promptOnCancel) {
                            promptNewItem(true);
                        }
                    }
                })
                .isDeletable(true)
                .isEditable(true)
                .create()
                .show();
    }

    private void addNewUrl(final boolean promptOnCancel) {
        CCLog.write(Event.TL_NEW_URL, "{promptOnCancel=" + promptOnCancel + "}");

        new DialogUrl(this)
                .setAutoEdit(true)
                .setOnSubmitListener(new DialogNote.OnSubmitListener() {
                    @Override
                    public void onSubmit(DialogInterface dialog, String text) {
                        Log.e(TAG, "URL Submitted");

                        if (promptOnCancel && text.isEmpty()) {
                            promptNewItem(true);
                        }

                        CCLog.write(Event.TL_NEW_SAVE, "{" + text + "}");

                        showLoadingDialog(R.string.dialogAddingUrl);

                        if (!text.startsWith("http://") && !text.startsWith("https://")) {
                            text = "http://" + text;
                        }

                        synchronized (Instance.items) {
                            final int itemId = Instance.items.size();
                            final String url = text;
                            final String b64Url = Web.b64encode(text);
                            final String filename = itemId + "-" + b64Url;
                            String fetchFrom = Web.GET_URL_SCREENSHOT + b64Url;

                            boolean isVideo = ItemUrl.isVideo(url);
                            int width = ItemUrl.getThumbnailWidth(isVideo);
                            int height = ItemUrl.getThumbnailHeight(isVideo);

                            Image.url2File(fetchFrom, filename, width, height, new Runnable() {
                                @Override
                                public void run() {
                                    long dateTime = (System.currentTimeMillis() / 1000L);
                                    if (!Instance.items.add(itemId, ItemType.URL, Instance.user(), url, dateTime, false, true, true)) {
                                        Log.e(TAG, "Failed to save URL + screenshot");
                                        if (promptOnCancel) {
                                            promptNewItem(true);
                                            return;
                                        }
                                    }

                                    TimelineActivity.getInstance().hideLoadingDialog();
                                }
                            });
                        }
                    }
                })
                .setOnCancelListener(new DialogNote.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        CCLog.write(Event.TL_NEW_CANCEL);
                        if (promptOnCancel) {
                            promptNewItem(true);
                        }
                    }
                })
                .isDeletable(true)
                .isEditable(true)
                .create()
                .show();
    }

    private void addNewPhoto() {
        CCLog.write(Event.TL_NEW_PHOTO);

        Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickIntent.setType("image/*");

        Intent chooserIntent = Intent.createChooser(pickIntent, "Select Image");
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{pickIntent});

        startActivityForResult(pickIntent, TimelineActivity.PICK_PHOTO);
    }

    public void redrawCentrelines() {
        Log.v(TAG, "Redraw Centrelines");
        updateCanvas(mSurfaceHolder);
    }

    private void updateCanvas(SurfaceHolder holder) {
        Log.v(TAG, "Redraw canvas");

        Canvas canvas = holder.lockCanvas();

        try {
            canvas.drawColor(Style.backgroundColor);

            int w = canvas.getWidth();
            int h = canvas.getHeight();

            for(User user : Instance.users) {
                if (user.draw()) {
                    int y1 = (int) (((h - Style.layoutCentreHeight) / 2) + user.centrelineOffset);
                    int y2 = (int) (y1 + Style.lineWidth);

                    canvas.drawRect(0, y1, w, y2, user.bgPaint);
                }
            }
        } catch(NullPointerException e) {
            Log.e(TAG, "NullPointer in canvas update: " + e.getMessage());
        } finally {
            try {
                holder.unlockCanvasAndPost(canvas);
            } catch(IllegalStateException e) {
                Log.e(TAG, "Error unlocking and posting canvas: " + e.getMessage());
            }
        }
    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        updateCanvas(holder);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        updateCanvas(holder);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
    }

    private static final long DISMISS_LOADING_IN = 3000L;
    private Handler mLoadedHandler = new Handler();
    private Runnable mLoadedRunner = new Runnable() {
        @Override
        public void run() {
            TimelineActivity.this.hideLoadingDialog();
        }
    };

    private TimerTask mUpdateUserTask = new TimerTask() {
        @Override
        public void run() {
            mUpdateHandler.post(new Runnable() {
                public void run() {
                    try {
                        ColloManager.beat(mUnbindAll);
                        if (mUnbindAll) {
                            mLoadedHandler.postDelayed(mLoadedRunner, DISMISS_LOADING_IN);
                            mUnbindAll = false;
                        }
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                    }
                }
            });
        }
    };

    private static long LONG_PRESS_DELAY = 500;
    private static float DOUBLE_TAP_POINT_LEEWAY = 50f;
    private static float SCALE_LEEWAY = 150f;
    private static long DOUBLE_TAP_TIME_LEEWAY = 450L;
    private static long mLastTap = -1;

    final SparseArray<Runnable> mPointerHandlerRunners = new SparseArray<>();
    final SparseArray<Handler> mPointerHandlers = new SparseArray<>();
    private SparseArray<Pointer> mPointers = new SparseArray<>();

    private class TimelineGestureDetector extends GestureDetector.SimpleOnGestureListener implements View.OnTouchListener {

        private final float mYOffset;

        public TimelineGestureDetector(float yOffset) {
            mYOffset = yOffset;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            CCLog.write(Event.COLLO_POINT, "{x=" + e.getX() + ",y=" + mYOffset + e.getY() + "}");
            Log.v(TAG, "broadcast point");
            ColloManager.broadcast(ColloDict.ACTION_POINT, e.getX(), mYOffset + e.getY());

            showPointer(Instance.user(), e.getX(), mYOffset + e.getY());

            return true;
        }

        public boolean onDown(MotionEvent e) {
            super.onDown(e);
            return true;
        }

        @Override
        public boolean onTouch(View v, MotionEvent e) {
            long now = System.currentTimeMillis();

            if(e.getAction() == MotionEvent.ACTION_CANCEL
                    || e.getAction() == MotionEvent.ACTION_UP
                    || e.getAction() == MotionEvent.ACTION_SCROLL) {
                mLongPressHandler.removeCallbacks(mLongPressed);
            }

            if(e.getAction() == MotionEvent.ACTION_UP) {
                if(Math.abs(mLayoutTouchX - e.getX()) < DOUBLE_TAP_POINT_LEEWAY) {
                    if (now - mLastTap < DOUBLE_TAP_TIME_LEEWAY) {
                        float diffY = Math.abs(mLayoutTouchY - (e.getY() + mYOffset));
                        if (diffY < DOUBLE_TAP_POINT_LEEWAY) {
                            return onDoubleTap(e);
                        } else if (diffY > SCALE_LEEWAY) {
                            Intent i = new Intent(TimelineActivity.this, OverviewActivity.class);
                            startActivity(i);
                        }
                    }
                }

                mLastTap = now;
                mLayoutTouchY = e.getY() + mYOffset;
            }

            if(e.getAction() == MotionEvent.ACTION_DOWN) {
                mLongPressHandler.postDelayed(mLongPressed, LONG_PRESS_DELAY);
            }

            mLayoutTouchX = e.getX();

            return false;
        }
    }

    final Handler mLongPressHandler = new Handler();
    Runnable mLongPressed = new Runnable() {
        public void run() {
            promptAdd(mLayoutTouchX);
        }
    };

    public float getLayoutTouchX() {
        return mLayoutTouchX;
    }

    class OverviewDetector extends ScaleGestureDetector.SimpleOnScaleGestureListener {

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
            TimelineActivity.this.fadeOut(new AnimationReactor() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    Intent i = new Intent(TimelineActivity.this, OverviewActivity.class);
                    startActivity(i);
                }
            });
        }

    }

    public void showPointer(View b, ViewGroup bg) {

    }

    private void showPointer(User user, float x, float y) {
        int userId = user.userId;
        Pointer existing = mPointers.get(userId);
        if(existing != null) {
            mFrameLayout.removeView(existing);

            Handler h = mPointerHandlers.get(userId);
            if(h != null) {
                h.removeCallbacks(mPointerHandlerRunners.get(userId));
            }
        }

        final Pointer p = new Pointer(user);
        p.setTranslationX(x);
        p.setTranslationY(y);
        mFrameLayout.addView(p);
        p.bringToFront();
        mPointers.put(userId, p);

        if(Style.pointerLength > 0) {
            mPointerHandlerRunners.put(userId, new Runnable() {
                @Override
                public void run() {
                    mFrameLayout.removeView(p);
                }
            });

            mPointerHandlers.get(userId, new Handler()).postDelayed(mPointerHandlerRunners.get(userId), Style.pointerLength);
        }
    }
}

