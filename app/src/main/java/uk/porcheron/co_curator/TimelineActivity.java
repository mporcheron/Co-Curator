package uk.porcheron.co_curator;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;

import java.util.Timer;
import java.util.TimerTask;

import uk.porcheron.co_curator.collo.ColloDict;
import uk.porcheron.co_curator.collo.ColloManager;
import uk.porcheron.co_curator.collo.ColloCompass;
import uk.porcheron.co_curator.collo.ServerManager;
import uk.porcheron.co_curator.db.DbLoader;
import uk.porcheron.co_curator.db.WebLoader;
import uk.porcheron.co_curator.item.ItemType;
import uk.porcheron.co_curator.item.ItemList;
import uk.porcheron.co_curator.item.NoteDialog;
import uk.porcheron.co_curator.user.User;
import uk.porcheron.co_curator.user.UserList;
import uk.porcheron.co_curator.util.Image;
import uk.porcheron.co_curator.val.Phone;
import uk.porcheron.co_curator.val.Style;
import uk.porcheron.co_curator.val.Instance;

public class TimelineActivity extends Activity implements View.OnLongClickListener, SurfaceHolder.Callback, ColloManager.ResponseHandler {
    private static final String TAG = "CC:TimelineActivity";

    private static boolean mCreated = false;
    private static TimelineActivity mInstance;

    private Timer mUpdateTimer;
    final Handler mUpdateHandler = new Handler();

    private SurfaceHolder mSurfaceHolder;
    private ProgressDialog mProgressDialog;
    private FrameLayout mFrameLayout;

    public static final int PICK_IMAGE = 101;

    private boolean mUnbindAll = true;


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

        // Load various static values
        Phone.collectAttrs();
        Style.collectAttrs();

        // Begin preparation for drawing the UI
        SurfaceView surfaceView = (SurfaceView) findViewById(R.id.surface);
        mSurfaceHolder = surfaceView.getHolder();
        mSurfaceHolder.addCallback(this);

        HorizontalScrollView scrollView = (HorizontalScrollView) findViewById(R.id.horizontalScrollView);
        scrollView.setSmoothScrollingEnabled(true);

        mFrameLayout = (FrameLayout) findViewById(R.id.frameLayout);

        LinearLayout layoutAbove = (LinearLayout) findViewById(R.id.layoutAboveCentre);
        LinearLayout layoutBelow = (LinearLayout) findViewById(R.id.layoutBelowCentre);

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, Style.layoutHalfPadding, 0, 0);

        layoutAbove.setPadding(0, 0, 0, Style.layoutHalfPadding);
        layoutBelow.setLayoutParams(params);

        mFrameLayout.setOnLongClickListener(this);
        layoutAbove.setOnLongClickListener(this);
        layoutBelow.setOnLongClickListener(this);

//        final GestureDetector gD  = new GestureDetector(this, ColloGesture.getInstance());
//        layoutAbove.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(final View view, final MotionEvent event) {
//                return gD.onTouchEvent(event);
//            }
//        });
//        layoutBelow.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(final View view, final MotionEvent event) {
//                return gD.onTouchEvent(event);
//            }
//        });

        mUnbindAll = true;

        if(mCreated) {
            return;
        }

        showLoadingDialog(R.string.dialogLoading);

        // Load items
        Instance.users = new UserList();
        Instance.items = new ItemList(scrollView, layoutAbove, layoutBelow);

        ColloManager.ResponseManager.registerHandler(ColloDict.ACTION_UNBIND, this);

        new DbLoader().execute();
    }

    public static TimelineActivity getInstance() {
        return mInstance;
    }

    @Override
    public void onResume() {
        Phone.collectAttrs();

        // Begin pitch tracking
        ColloCompass.getInstance().resumeListening();

        // Reschedule IP pinging
        try {
            mUpdateTimer = new Timer();
            mUpdateTimer.schedule(mUpdateUserTask, 1000, ColloManager.BEAT_EVERY);
        } catch(IllegalStateException e) {

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
        promptNewItem(v, Instance.items.isEmpty());
        return true;
    }

    public void showLoadingDialog(int str) {
        mProgressDialog = ProgressDialog.show(this, "", getText(str), true);
    }

    public void hideLoadingDialog() {
        mProgressDialog.hide();
    }

    public void promptAdd() {
        onLongClick(mFrameLayout);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.v(TAG, "Received ActivityResult (requestCode=" + requestCode + ",resultCode=" + resultCode + ")");

        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode != Activity.RESULT_OK) {
            return;
        }

        if (requestCode == PICK_IMAGE) {
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
            String filePath = cursor.getString(columnIndex);
            cursor.close();

            Log.v(TAG, "File selected by user: " + filePath);

            new ImportImage().execute(filePath);

        }
    }

    @Override
    public boolean respond(String action, int globalUserId, String... data) {
        switch(action) {
            case ColloDict.ACTION_UNBIND:
                Instance.users.unDrawUser(globalUserId);
                TimelineActivity.getInstance().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Instance.items.retestDrawing();
                        TimelineActivity.getInstance().redrawCentrelines();
                    }
                });
                return true;
        }

        return false;
    }

    private class ImportImage extends AsyncTask<String,Void,String> {

        @Override
        protected String doInBackground(String... params) {
            Bitmap bitmap = BitmapFactory.decodeFile(params[0]);

            try {
                return Image.save(TimelineActivity.this, bitmap);
            } catch (Exception e) {
                Log.e(TAG, "Failed to save image");
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            if(!Instance.items.add(ItemType.PHOTO, Instance.user(), result, false, true, true)) {
                Log.e(TAG, "Failed to save photo");
            }

            hideLoadingDialog();

        }
    }

    public void promptNewItem(final View view, final boolean forceAdd) {
        AlertDialog.Builder builder = new AlertDialog.Builder(TimelineActivity.this);
        builder.setTitle(R.string.dialog_add_message);

        if(forceAdd) {
            builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    promptNewItem(view, true);
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
                        new NoteDialog()
                                .setAutoEdit(false)
                                .setOnSubmitListener(new NoteDialog.OnSubmitListener() {
                                    @Override
                                    public void onSubmit(DialogInterface dialog, String text) {
                                        Log.e(TAG, "Note Submitted");
                                        if (forceAdd && text.isEmpty()) {
                                            promptNewItem(view, true);
                                        }
                                        boolean create = Instance.items.add(ItemType.NOTE, Instance.user(), text, false, true, true);
                                        if (forceAdd && !create) {
                                            promptNewItem(view, true);
                                        }
                                    }
                                })
                                .setOnCancelListener(new NoteDialog.OnCancelListener() {
                                    @Override
                                    public void onCancel(DialogInterface dialog) {
                                        if (forceAdd) {
                                            promptNewItem(view, true);
                                        }
                                    }
                                })
                                .create()
                                .show();
                        break;

                    case URL:
                        addNewUrl(view, forceAdd);
                        break;
                }
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void addNewNote(final View view, final boolean promptOnCancel) {
        final EditText editText = new EditText(TimelineActivity.this);
        editText.setSingleLine(false);

        AlertDialog dialog = new AlertDialog.Builder(TimelineActivity.this)
                .setTitle(getString(R.string.dialog_note_title))
                .setView(editText)
                .setPositiveButton(getString(R.string.dialog_note_positive), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String text = editText.getText().toString();
                        if (!Instance.items.add(ItemType.NOTE, Instance.user(), text, false, true, true)) {
                            promptNewItem(view, promptOnCancel);
                        }
                    }
                })
                .setNegativeButton(getString(R.string.dialog_note_negative), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        if (promptOnCancel) {
                            promptNewItem(view, true);
                        }
                    }
                })
                .create();

        dialog.show();
        editText.requestFocus();

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }

    private void addNewUrl(final View view, final boolean promptOnCancel) {
        final EditText editText = new EditText(TimelineActivity.this);
        editText.setSingleLine(true);
        editText.setInputType(EditorInfo.TYPE_TEXT_VARIATION_URI);

        AlertDialog dialog = new AlertDialog.Builder(TimelineActivity.this)
                .setTitle(getString(R.string.dialog_url_title))
                .setView(editText)
                .setPositiveButton(getString(R.string.dialog_url_positive), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String text = editText.getText().toString();

                        String insertUrl = text;
                        if(!text.startsWith("http")) {
                            insertUrl = "http://" + text;
                        }

                        if(!Instance.items.add(ItemType.URL, Instance.user(), insertUrl, false, true, true)) {
                            promptNewItem(view, promptOnCancel);
                        }
                    }
                })
                .setNegativeButton(getString(R.string.dialog_url_negative), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        if(promptOnCancel) {
                            promptNewItem(view, true);
                        }
                    }
                })
                .create();

        dialog.show();
        editText.requestFocus();

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }

    private void addNewPhoto() {
        Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickIntent.setType("image/*");

        Intent chooserIntent = Intent.createChooser(pickIntent, "Select Image");
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{pickIntent});

        startActivityForResult(pickIntent, TimelineActivity.PICK_IMAGE);
    }

    public void redrawCentrelines() {
        Log.v(TAG, "Redraw Centrelines");
        updateCanvas(mSurfaceHolder);
    }

    private void updateCanvas(SurfaceHolder holder) {
        Log.v(TAG, "Redaw canvas");

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
                return;
        } finally {
            try {
                holder.unlockCanvasAndPost(canvas);
            } catch(IllegalStateException e) {

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

    TimerTask mUpdateUserTask = new TimerTask() {
        @Override
        public void run() {
            mUpdateHandler.post(new Runnable() {
                public void run() {
                    try {
                        ColloManager.beat(mUnbindAll);
                        mUnbindAll = false;
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                    }
                }
            });
        }
    };

}
