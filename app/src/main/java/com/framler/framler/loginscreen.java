package com.framler.framler;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import android.content.Context;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;

import com.google.android.gms.common.api.GoogleApiClient;
import com.vk.sdk.VKSdk;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.SecureRandom;
import java.util.Arrays;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class loginscreen extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener{
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    String login;
    Context ctx=this;
    Context context=this;
    String applicationContext=this;
    String NAME=null;
    String PASSWORD=null;
    String TOKEN=null;
    String FAMILY;
    String NIK;
    String nameFamily;
    String EMAIL;
    String PHOTO_PATH;
    String sToken;
    String password;
    private ImageButton fblogin;
    private ImageButton gsignin;
    private ImageButton vklogin;
    private GoogleApiClient googleApiClient;
    private GoogleSignInOptions signInOptions;
    public static final int REQUEST_CODE = 100;
    private static final boolean AUTO_HIDE = true;
    CallbackManager callbackManager;
    private CallbackManager mCallbackManager;
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

    private View mContentView;
    private View mControlsView;
    private boolean mVisible;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(this.getApplicationContext());

        VKSdk.initialize(Context applicationContext);
        mCallbackManager = CallbackManager.Factory.create();

        LoginManager.getInstance().registerCallback(mCallbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        Log.d("Success", "Login");

                        Profile profile = Profile.getCurrentProfile();
                        NIK = profile.getFirstName();
                        PASSWORD = profile.getId();
                        EMAIL = profile.getId();
                        NAME = profile.getFirstName();
                        if (profile.getProfilePictureUri(400, 400) != null) {
                            Uri user_photo = profile.getProfilePictureUri(400, 400);
                            PHOTO_PATH = user_photo.toString();
                        }

                        FAMILY  = profile.getLastName();

                        SecureRandom random = new SecureRandom();
                        byte bytes[] = new byte[80];
                        random.nextBytes(bytes);
                        sToken = bytes.toString();

                        File fileName = null;
                        String sdState = android.os.Environment.getExternalStorageState();
                        if (sdState.equals(android.os.Environment.MEDIA_MOUNTED)) {
                            File sdDir = android.os.Environment.getExternalStorageDirectory();
                            fileName = new File(sdDir, "framlertest/token");
                        } else {
                            fileName = context.getCacheDir();
                        }
                        if (!fileName.exists())
                            fileName.mkdirs();
                        File token = new File(fileName, "token.txt");
                        try {
                            FileWriter f = new FileWriter(token);
                            f.write(sToken);
                            f.flush();
                            f.close();
                        } catch (Exception e) {

                        }
                        GoogSendData c = new GoogSendData();
                        if (PHOTO_PATH != null){
                            c.execute(NAME, PASSWORD, EMAIL, FAMILY, NIK, sToken, PHOTO_PATH);
                        }else {
                            c.execute(NAME, PASSWORD, EMAIL, FAMILY, NIK, sToken);
                        }
                        nameFamily = NAME + " " + FAMILY;
                        Intent i = new Intent(loginscreen.this, start.class);
                        i.putExtra("nik", NAME);
                        if (PHOTO_PATH != null){
                            i.putExtra("photopath", PHOTO_PATH);
                        }

                        i.putExtra("namef",  NAME);
                        startActivity(i);


                    }

                    @Override
                    public void onCancel() {
                        Toast.makeText(loginscreen.this, "Login Cancel", Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        Toast.makeText(loginscreen.this, exception.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });

        setContentView(R.layout.loginscreen);

        mVisible = true;
        mControlsView = findViewById(R.id.fullscreen_content_controls);
        mContentView = findViewById(R.id.fullscreen_content);
        signInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build();

        googleApiClient = new GoogleApiClient.Builder(this).enableAutoManage(this, this).addApi(Auth.GOOGLE_SIGN_IN_API, signInOptions).build();
        gsignin = (ImageButton)findViewById(R.id.gglsignbt);
        fblogin = (ImageButton) findViewById(R.id.fblogbtn);
        //get login fb

        fblogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LoginManager.getInstance().logInWithReadPermissions(loginscreen.this, Arrays.asList("public_profile", "email"));
            }
        });
        gsignin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
                startActivityForResult(signInIntent, REQUEST_CODE);

            }
        });

        // Set up the user interaction to manually show or hide the system UI.
        mContentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle();
            }
        });

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        findViewById(R.id.login_button).setOnTouchListener(mDelayHideTouchListener);
        findViewById(R.id.textView3).setOnTouchListener(mDelayHideTouchListener);
    }
    public void startScreen(View v) {

        EditText loginfield = (EditText) findViewById(R.id.login_field);
        EditText passfield = (EditText) findViewById(R.id.passwordfield);

        //Получение введенных данных
        login = loginfield.getText().toString();
        password = passfield.getText().toString();
        if((login.length()!=0)&&(password.length()!=0)){
        BackGround b = new BackGround();
        b.execute(login, password);
         } else {
            Toast toast = Toast.makeText(getApplicationContext(),
                    "Необходимо ввести логин и пароль", Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    public void regScreen(View v) {

        startActivity(new Intent(this, regscr.class));
        overridePendingTransition(R.anim.vertanim,R.anim.alpha);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if(result.isSuccess()) {
                GoogleSignInAccount account = result.getSignInAccount();
                if (account != null) {
                    if (account.getPhotoUrl() != null){
                        Uri photo_uri = account.getPhotoUrl();
                        PHOTO_PATH = photo_uri.toString();
                    }
                    NAME = account.getGivenName();
                    FAMILY = account.getFamilyName();
                    EMAIL = account.getEmail();
                    NIK = account.getGivenName();
                    PASSWORD = account.getId();

                    SecureRandom random = new SecureRandom();
                    byte bytes[] = new byte[80];
                    random.nextBytes(bytes);
                    sToken = bytes.toString();

                    File fileName = null;
                    String sdState = android.os.Environment.getExternalStorageState();
                    if (sdState.equals(android.os.Environment.MEDIA_MOUNTED)) {
                        File sdDir = android.os.Environment.getExternalStorageDirectory();
                        fileName = new File(sdDir, "framlertest/token");
                    } else {
                        fileName = context.getCacheDir();
                    }
                    if (!fileName.exists())
                        fileName.mkdirs();
                    File token = new File(fileName, "token.txt");
                    try {
                        FileWriter f = new FileWriter(token);
                        f.write(sToken);
                        f.flush();
                        f.close();
                    } catch (Exception e) {

                    }
                    GoogSendData c = new GoogSendData();
                    if (PHOTO_PATH != null){
                        c.execute(NAME, PASSWORD, EMAIL, FAMILY, NIK, sToken, PHOTO_PATH);
                    }else {
                        c.execute(NAME, PASSWORD, EMAIL, FAMILY, NIK, sToken);
                    }
                    nameFamily = NAME + " " + FAMILY;
                    Intent i = new Intent(loginscreen.this, start.class);
                    i.putExtra("nik", NIK);
                    if (PHOTO_PATH != null){
                        i.putExtra("photopath", PHOTO_PATH);
                    }

                    i.putExtra("namef", nameFamily);
                    startActivity(i);
                    overridePendingTransition(R.anim.left, R.anim.lalpha);

                }
            }else {
                Toast.makeText(getApplicationContext(), "account is null", Toast.LENGTH_SHORT).show();
            }
        } else if (mCallbackManager.onActivityResult(requestCode, resultCode, data)) {
            return;
        }
    }


    class BackGround extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {
            String name = params[0];
            String password = params[1];
            String data="";
            int tmp;

            try {
                URL url = new URL("http://framler.com/apiscripts/login.php");
                String urlParams = "email="+name+"&password="+password;

                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setDoOutput(true);
                OutputStream os = httpURLConnection.getOutputStream();
                os.write(urlParams.getBytes());
                os.flush();
                os.close();

                InputStream is = httpURLConnection.getInputStream();
                while((tmp=is.read())!=-1){
                    data+= (char)tmp;
                }

                is.close();
                httpURLConnection.disconnect();

                return data;
            } catch (MalformedURLException e) {
                e.printStackTrace();
                return "Exception: "+e.getMessage();
            } catch (IOException e) {
                e.printStackTrace();
                return "Exception: "+e.getMessage();
            }
        }

        @Override
        protected void onPostExecute(String s) {
            String err = null;
            final String g = s;
            //проверяем ответ сервера, если он нулевой, значит логин или пароль не найдены в базе.
            if (g.equals("{\"user_data\":[]}")) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Вы ввели неверный E-mail или пароль", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                try {
                    JSONObject root = new JSONObject(s);
                    JSONObject user_data = root.getJSONObject("user_data");
                    EMAIL = user_data.getString("email");
                    NAME = user_data.getString("name");
                    PASSWORD = user_data.getString("password");
                    TOKEN = user_data.getString("token");
                    FAMILY = user_data.getString("family");
                    NIK = user_data.getString("nik");
                    nameFamily = NAME + " " + FAMILY;


                        Intent i = new Intent(loginscreen.this, start.class);
                        i.putExtra("nik", NIK);
                        i.putExtra("namef", nameFamily);
                        startActivity(i);
                        overridePendingTransition(R.anim.left, R.anim.lalpha);
                    //проверка наличия файла токена на телефоне. Если его нет получаем из базы.
                        File fileName;
                        String sdState = android.os.Environment.getExternalStorageState();
                        if (sdState.equals(android.os.Environment.MEDIA_MOUNTED)) {
                            File sdDir = android.os.Environment.getExternalStorageDirectory();
                            fileName = new File(sdDir, "framlertest/token");
                        } else {
                            fileName = context.getCacheDir();
                        }
                        if (!fileName.exists())
                            fileName.mkdirs();
                        File token = new File(fileName, "token.txt");
                        try {
                            FileWriter f = new FileWriter(token);
                            f.write(TOKEN);
                            f.flush();
                            f.close();
                        } catch (Exception e) {
                        }

                }
            catch(JSONException e){
                e.printStackTrace();
                err = "Exception: " + e.getMessage();
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

    private final Handler mHideHandler = new Handler();
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }
}
