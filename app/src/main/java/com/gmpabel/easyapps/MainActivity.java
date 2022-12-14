package com.gmpabel.easyapps;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.PermissionRequest;
import android.webkit.URLUtil;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    //private Button button;
     WebView webView;
    ProgressBar progressbar;
    SwipeRefreshLayout swipeRefreshHotelDetails;


  //  WebView webView;
    private static final String TAG = MainActivity.class.getSimpleName();
    private String mCM;
    private ValueCallback mUM;
    private ValueCallback<Uri[]> mUMA;
    private final static int FCR=1;
    private TextView mProgressbarText;
    private RelativeLayout mLoadFailedPage;




    AlertDialog.Builder builder;
    String test = "";
    Button closeButton;
    private int mDisplayHight;
    private int mDisplayWidth;




    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent){


        super.onActivityResult(requestCode, resultCode, intent);
        if(Build.VERSION.SDK_INT >= 21){
            Uri[] results = null;
            //Check if response is positive
            if(resultCode== Activity.RESULT_OK){
                if(requestCode == FCR){
                    if(null == mUMA){
                        return;
                    }
                    if(intent == null){
                        //camera selected
                        //Capture Photo if no image available
                        if(mCM != null){
                            results = new Uri[]{Uri.parse(mCM)};
                        }
                    }else{
                        //gallery selected
                        String dataString = intent.getDataString();
                        if(dataString != null){
                            results = new Uri[]{Uri.parse(dataString)};
                        }
                    }
                }
            }
            mUMA.onReceiveValue(results);
            mUMA = null;
        }else{
            if(requestCode == FCR){
                if(null == mUM) return;
                Uri result = intent == null || resultCode != RESULT_OK ? null : intent.getData();
                mUM.onReceiveValue(result);
                mUM = null;
            }
        }
    }



    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_PROGRESS);
        // gettin display with and height in pixel
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        mDisplayHight = displayMetrics.heightPixels;
        mDisplayWidth = displayMetrics.widthPixels;

        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            setColor();
        }
        getSupportActionBar().hide();
        if(Build.VERSION.SDK_INT >=23 && (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, 1);
        }

        getWindow().setFeatureInt(Window.FEATURE_PROGRESS, Window.PROGRESS_VISIBILITY_ON);

        webView = findViewById(R.id.webView1);
        progressbar = findViewById(R.id.progressbarId);
        swipeRefreshHotelDetails=findViewById(R.id.swipeRefreshHotelDetails);


        webView = findViewById(R.id.webView1);
        mLoadFailedPage = findViewById(R.id.web_view_load_error_page);
        progressbar = findViewById(R.id.progressbarId);
        mProgressbarText = findViewById(R.id.tx_progressbar);
       // swipeRefreshHotelDetails=findViewById(R.id.swipeRefreshHotelDetails);



        registerForContextMenu(webView);

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setAllowFileAccess(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setJavaScriptEnabled(true);
        webSettings.setAllowFileAccess(true);
        webSettings.setAppCacheEnabled(true);

        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            webSettings.setMediaPlaybackRequiresUserGesture(false);
        }


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            progressbar.setProgressTintList(ColorStateList.valueOf(Color.parseColor("#FF004E")));
        }
         progressbar.setMax(100);
        webView.loadUrl("https://easyshop64.com/");

        //progress bar
        progressbar.setProgress(0);
        webView.setWebViewClient(new Callback());
        webView.setWebViewClient(new WebViewClient());
        webView.setWebChromeClient(new MyChrome());
        webView.setDownloadListener(new DownloadListener()
        {
            @Override
            public void onDownloadStart(String url, String userAgent,
                                        String contentDisposition, String mimeType,
                                        long contentLength) {
                DownloadManager.Request request = new DownloadManager.Request(
                        Uri.parse(url));
                request.setMimeType(mimeType);
                String cookies = CookieManager.getInstance().getCookie(url);
                request.addRequestHeader("cookie", cookies);
                request.addRequestHeader("User-Agent", userAgent);
                request.setDescription("Downloading File...");
                request.setTitle(URLUtil.guessFileName(url, contentDisposition, mimeType));
                request.allowScanningByMediaScanner();
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                request.setDestinationInExternalPublicDir(
                        Environment.DIRECTORY_DOWNLOADS, URLUtil.guessFileName(
                                url, contentDisposition, mimeType));
                DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                dm.enqueue(request);
                Toast.makeText(getApplicationContext(), "Downloading File", Toast.LENGTH_LONG).show();
            }});


        //refresh layout
        swipeRefreshHotelDetails.setOnRefreshListener(() -> {
            // Log.e("ref","ref: 1");
            webView.reload();
            swipeRefreshHotelDetails.setRefreshing(false);

        });


        final boolean[] loadState = {true, false, true}; // loadingFinished, redirect,
        // successfully reached web page
        webView.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String urlNewString) {
                if (!loadState[0]) { // loadingFinished
                    loadState[1] = true; // redirect
                }

                loadState[0] = false; // loadingFinished
                view.loadUrl(urlNewString);
                return true;
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap facIcon) {
                loadState[0] = false; // loadingFinished
                //SHOW LOADING IF IT ISNT ALREADY VISIBLE
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                if(!loadState[1]){ // redirect
                    loadState[0] = true; // loadingFinished
                }

                if(loadState[0] && !loadState[1]) { // loadingFinished, redirect
                    if (loadState[2]) { // successfully reached web page
                       webView.setVisibility(View.VISIBLE);
                        //swipeRefreshHotelDetails.setVisibility(View.VISIBLE);
                        mLoadFailedPage.setVisibility(View.GONE);
                    }
                    else {
                        // occur some error to reach web page
                        webView.setVisibility(View.GONE);
                        //swipeRefreshHotelDetails.setVisibility(View.GONE);
                        mLoadFailedPage.setVisibility(View.VISIBLE);

                        TextView retry = findViewById(R.id.cnn_lost_retry_btn);
                        retry.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                webView.reload();
                            }
                        });
                    }
                    loadState[2] = true;

                } else{
                    loadState[1] = false; // redirect
                }

            }
            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                loadState[2] = false; // successfully reached web page
                webView.setVisibility(View.GONE);
                mLoadFailedPage.setVisibility(View.VISIBLE);
              //  Toast.makeText(MainActivity.this, "failed", Toast.LENGTH_SHORT).show();
            }
        });




    }


    private class MyChrome extends WebChromeClient {

        private View mCustomView;
        private WebChromeClient.CustomViewCallback mCustomViewCallback;
        protected FrameLayout mFullscreenContainer;
        private int mOriginalOrientation;
        private int mOriginalSystemUiVisibility;


        MyChrome() {}

        //image upload file chooser
        //For Android 3.0+
        public void openFileChooser(ValueCallback<Uri> uploadMsg){
            mUM = uploadMsg;
            Intent i = new Intent(Intent.ACTION_GET_CONTENT);
            i.addCategory(Intent.CATEGORY_OPENABLE);
            i.setType("*/*");
            MainActivity.this.startActivityForResult(Intent.createChooser(i,"File Chooser"), FCR);
        }
        // For Android 3.0+, above method not supported in some android 3+ versions, in such case we use this
        public void openFileChooser(ValueCallback uploadMsg, String acceptType){
            mUM = uploadMsg;
            Intent i = new Intent(Intent.ACTION_GET_CONTENT);
            i.addCategory(Intent.CATEGORY_OPENABLE);
            i.setType("*/*");
            MainActivity.this.startActivityForResult(
                    Intent.createChooser(i, "File Browser"),
                    FCR);
        }
        //For Android 4.1+
        public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture){
            mUM = uploadMsg;
            Intent i = new Intent(Intent.ACTION_GET_CONTENT);
            i.addCategory(Intent.CATEGORY_OPENABLE);
            i.setType("*/*");
            MainActivity.this.startActivityForResult(Intent.createChooser(i, "File Chooser"), MainActivity.FCR);
        }
        //For Android 5.0+
        @SuppressLint("QueryPermissionsNeeded")
        public boolean onShowFileChooser(
                WebView webView, ValueCallback<Uri[]> filePathCallback,
                FileChooserParams fileChooserParams){
            if(mUMA != null){
                mUMA.onReceiveValue(null);
            }
            mUMA = filePathCallback;
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if(takePictureIntent.resolveActivity(MainActivity.this.getPackageManager()) != null){
                File photoFile = null;
                try{
                    photoFile = createImageFile();

                    takePictureIntent.putExtra("PhotoPath", mCM);
                }catch(IOException ex){
                    Log.e(TAG, "Failed create image", ex);
                }
                if(photoFile != null){
                    mCM = "file:" + photoFile.getAbsolutePath();
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                }else{
                    takePictureIntent = null;
                }
            }
            Intent contentSelectionIntent = new Intent(Intent.ACTION_GET_CONTENT);
            contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE);
            contentSelectionIntent.setType("*/*");
            Intent[] intentArray;
            if(takePictureIntent != null){
                intentArray = new Intent[]{takePictureIntent};
            }else{
                intentArray = new Intent[0];
            }

            Intent chooserIntent = new Intent(Intent.ACTION_CHOOSER);
            chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent);
            chooserIntent.putExtra(Intent.EXTRA_TITLE, "Select an action");
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray);
            startActivityForResult(chooserIntent, FCR);
            return true;
        }


        @SuppressLint("LongLogTag")
        @Override
        public void onPermissionRequest(PermissionRequest request) {
            super.onPermissionRequest(request);

//                Environment.getExternalStorageDirectory(), "filename.wav";

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                String permissions[] = {Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.MODIFY_AUDIO_SETTINGS,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE};

                for (String r : request.getResources()) {
                    if (r.equals("android.webkit.resource.AUDIO_CAPTURE")){
                        int code = ActivityCompat.checkSelfPermission(MainActivity.this, permissions[0]);

                        ActivityCompat.requestPermissions(MainActivity.this, permissions, 1);
                        request.grant(request.getResources());

//                            if (code == PackageManager.PERMISSION_DENIED) {
//
//                            }
                    }
                }
            }
        }


        public void onProgressChanged(WebView view, int progress) {
            progressbar.setProgress(progress);
            mProgressbarText.setText(progress + "%");

            // calculating padding based on percentage
            int paddingL = ((int)Math.ceil(mDisplayWidth / 100.00)) * progress;
            mProgressbarText.setPadding(paddingL, 0, 0, 0);

            if (progress == 100) {
                mProgressbarText.setPadding(0,0,0,0);
                progressbar.setVisibility(View.GONE);
                mProgressbarText.setVisibility(View.GONE);
            }else{
                progressbar.setVisibility(View.VISIBLE);
                mProgressbarText.setVisibility(View.VISIBLE);
            }
            super.onProgressChanged(view,progress);

        }

        public Bitmap getDefaultVideoPoster()
        {
            if (mCustomView == null) {
                return null;
            }
            return BitmapFactory.decodeResource(getApplicationContext().getResources(), 2130837573);
        }

        public void onHideCustomView()
        {
            ((FrameLayout)getWindow().getDecorView()).removeView(this.mCustomView);
            this.mCustomView = null;
            getWindow().getDecorView().setSystemUiVisibility(this.mOriginalSystemUiVisibility);
            setRequestedOrientation(this.mOriginalOrientation);
            this.mCustomViewCallback.onCustomViewHidden();
            this.mCustomViewCallback = null;
        }

        public void onShowCustomView(View paramView, WebChromeClient.CustomViewCallback paramCustomViewCallback)
        {
            if (this.mCustomView != null)
            {
                onHideCustomView();
                return;
            }
            this.mCustomView = paramView;
            this.mOriginalSystemUiVisibility = getWindow().getDecorView().getSystemUiVisibility();
            this.mOriginalOrientation = getRequestedOrientation();
            this.mCustomViewCallback = paramCustomViewCallback;
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            ((FrameLayout)getWindow().getDecorView()).addView(this.mCustomView, new FrameLayout.LayoutParams(-1, -1));
            getWindow().getDecorView().setSystemUiVisibility(3846 | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        }



    }


    // Create an image file
    private File createImageFile() throws IOException {
        @SuppressLint("SimpleDateFormat") String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "img_"+timeStamp+"_";
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(imageFileName,".jpg",storageDir);
    }

    ///////////////////////////
    public class Callback extends WebViewClient{
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl){
            Toast.makeText(getApplicationContext(), "Failed loading app!", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig){
        super.onConfigurationChanged(newConfig);
    }
    @Override
    public void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenu.ContextMenuInfo contextMenuInfo){
        super.onCreateContextMenu(contextMenu, view, contextMenuInfo);

        final WebView.HitTestResult webViewHitTestResult = webView.getHitTestResult();

        if (webViewHitTestResult.getType() == WebView.HitTestResult.IMAGE_TYPE ||
                webViewHitTestResult.getType() == WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE) {

            contextMenu.setHeaderTitle("Download file From below");

            contextMenu.add(0, 1, 0, "Save - Download")
                    .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem menuItem) {

                            String DownloadImageURL = webViewHitTestResult.getExtra();


                            if(URLUtil.isValidUrl(DownloadImageURL)){

                                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(DownloadImageURL));
                                request.allowScanningByMediaScanner();
                                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                                DownloadManager downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                                downloadManager.enqueue(request);

                                String downloadPath= Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();


                                Toast.makeText(MainActivity.this," Downloaded Successfully.",Toast.LENGTH_LONG).show();


                            }
                            else {
                                Toast.makeText(MainActivity.this,"Sorry.. Something Went Wrong!",Toast.LENGTH_LONG).show();
                            }
                            return false;
                        }
                    });
        }}
////////////////////////////
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        webView.saveState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        webView.restoreState(savedInstanceState);
    }




    //set Color
    @RequiresApi(api = Build.VERSION_CODES.M)
    void setColor(){
        getWindow().setStatusBarColor(getResources().getColor(R.color.status_bar_color, this.getTheme()));
        //  getWindow().setNavigationBarColor(getResources().getColor(R.color.status_bar_color, this.getTheme()));
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        }
        else {


            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Exit app");
            builder.setMessage("Are you sure?");
            builder.setPositiveButton("No", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                }
            });
            builder.setNegativeButton("Yes", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    MainActivity.super.onBackPressed();
                }
            });
            builder.show();

            //super.onBackPressed();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, @NonNull KeyEvent event){
        if(event.getAction() == KeyEvent.ACTION_DOWN){
            switch(keyCode){
                case KeyEvent.KEYCODE_BACK:
                    if(webView.canGoBack()){
                        webView.goBack();
                    }else{

                        AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        builder.setTitle("Exit");
                        builder.setMessage("Do you want to exit now?");
                        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                MainActivity.super.onBackPressed();
                            }
                        });
                        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                            }
                        });
                        builder.show();
                        // finish();
                    }
                    return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }


}