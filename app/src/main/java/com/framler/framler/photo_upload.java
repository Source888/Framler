package com.framler.framler;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.kosalgeek.android.photoutil.CameraPhoto;
import com.kosalgeek.android.photoutil.GalleryPhoto;
import com.kosalgeek.android.photoutil.ImageBase64;
import com.kosalgeek.android.photoutil.ImageLoader;
import com.kosalgeek.genasync12.AsyncResponse;
import com.kosalgeek.genasync12.EachExceptionsHandler;
import com.kosalgeek.genasync12.PostResponseAsyncTask;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Photo Upload by Constantin Solovyov aka Source888
 */
public class photo_upload extends AppCompatActivity {
    private final String TAG = this.getClass().getName();
    ImageButton cam_load, fol_load, backtoreg;
    CameraPhoto cameraPhoto;
    String photopath, selectedPhoto;
    final int CAMERA_REQUEST = 13323;
    final int GALERY_REQUEST = 22131;
    CircleImageView user_photo;
    GalleryPhoto galleryPhoto;

    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    private View mContentView;
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };
    private View mControlsView;
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
            mControlsView.setVisibility(View.VISIBLE);
        }
    };
    private boolean mVisible;
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };
    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.photo_upload);

        mVisible = true;

        mContentView = findViewById(R.id.fullscreen_content);
        mControlsView = findViewById(R.id.fullscreen_content_controls);

       // Set up the user interaction to manually show or hide the system UI.
        mContentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle();
            }
        });

        findViewById(R.id.cam_load).setOnTouchListener(mDelayHideTouchListener);
        cameraPhoto = new CameraPhoto(getApplicationContext());
        galleryPhoto = new GalleryPhoto(getApplicationContext());
        // search buttons
        cam_load = (ImageButton)findViewById(R.id.cam_load);
        fol_load = (ImageButton)findViewById(R.id.fol_load);
        backtoreg = (ImageButton)findViewById(R.id.backtoreg);
        user_photo = (CircleImageView)findViewById(R.id.user_photo);
        // запускаем камеру

        cam_load.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    startActivityForResult(cameraPhoto.takePhotoIntent(), CAMERA_REQUEST);
                    cameraPhoto.addToGallery();
                } catch (IOException e) {
                    Toast.makeText(getApplicationContext(), "Что-то не так с камерой", Toast.LENGTH_SHORT).show();
                }
            }
        });
        // открываем галерею
        fol_load.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(galleryPhoto.openGalleryIntent(), GALERY_REQUEST);
            }
        });
        backtoreg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Bitmap bitmap = ImageLoader.init().from(selectedPhoto).requestSize(512, 512).getBitmap();
                    String encodedImage = ImageBase64.encode(bitmap);
                    HashMap<String, String> postData = new  HashMap<String, String>();
                    postData.put("image", encodedImage);
                    PostResponseAsyncTask task = new PostResponseAsyncTask(photo_upload.this, postData, new AsyncResponse() {
                        @Override
                        public void processFinish(String s) {
                            if(!s.contains("upload_filed")&&!s.contains("image_not_in")){
                                Toast.makeText(getApplicationContext(), "Image uploaded successfull", Toast.LENGTH_SHORT).show();
                                Intent reg = new Intent (photo_upload.this, regscr.class);
                                reg.putExtra("photopath", s);
                                startActivity(reg);
                            }
                            else {
                                Toast.makeText(getApplicationContext(), "Error during upload", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                    task.execute("http://framler.com/photoupload.php");
                    task.setEachExceptionsHandler(new EachExceptionsHandler() {
                        @Override
                        public void handleIOException(IOException e) {
                            Toast.makeText(getApplicationContext(), "Не могу соединиться с сервером", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void handleMalformedURLException(MalformedURLException e) {
                            Toast.makeText(getApplicationContext(), "Ошибка в адресе", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void handleProtocolException(ProtocolException e) {
                            Toast.makeText(getApplicationContext(), "Ошибка протокола", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void handleUnsupportedEncodingException(UnsupportedEncodingException e) {
                            Toast.makeText(getApplicationContext(), "Ошибка при декодировании", Toast.LENGTH_SHORT).show();
                        }
                    });
                } catch (FileNotFoundException e) {
                    Toast.makeText(getApplicationContext(), "Проблема при открытии галереи", Toast.LENGTH_SHORT).show();
                }
              //  Intent reg = new Intent (photo_upload.this, regscr.class);
              //  reg.putExtra("photopath", photopath);
              //  startActivity(reg);
            }
        });
    }
    //получаем фотку с камеры или из галереи
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK){
            if(requestCode == CAMERA_REQUEST){
                photopath = cameraPhoto.getPhotoPath();
                selectedPhoto = photopath;
                try {
                    Bitmap bitmap = ImageLoader.init().from(photopath).requestSize(512,512).getBitmap();
                    user_photo.setImageBitmap(bitmap);
                } catch (FileNotFoundException e) {
                    Toast.makeText(getApplicationContext(), "Проблема при загрузке фото", Toast.LENGTH_SHORT).show();
                }

            }
            else if (requestCode == GALERY_REQUEST){
                Uri uri = data.getData();
                galleryPhoto.setPhotoUri(uri);

                photopath = galleryPhoto.getPath();
                selectedPhoto = photopath;
                try {
                    Bitmap bitmap = ImageLoader.init().from(photopath).requestSize(512,512).getBitmap();
                    user_photo.setImageBitmap(bitmap);
                } catch (FileNotFoundException e) {
                    Toast.makeText(getApplicationContext(), "Проблема при открытии галереи", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }


    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }

    private void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mControlsView.setVisibility(View.GONE);
        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    @SuppressLint("InlinedApi")
    private void show() {
        // Show the system bar
        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mVisible = true;

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }
}
