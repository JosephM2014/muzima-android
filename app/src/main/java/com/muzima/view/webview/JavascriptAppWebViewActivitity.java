package com.muzima.view.webview;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import com.muzima.MuzimaApplication;
import com.muzima.R;
import com.muzima.api.model.FormTemplate;
import com.muzima.controller.FormController;
import com.muzima.model.BaseForm;
import com.muzima.utils.javascriptinterface.SharedHealthRecordViewerJavascriptInterface;
import com.muzima.utils.smartcard.SmartCardIntentIntegrator;
import com.muzima.utils.smartcard.SmartCardIntentResult;
import com.muzima.view.BroadcastListenerActivity;
import com.muzima.view.progressdialog.MuzimaProgressDialog;

import static android.webkit.ConsoleMessage.MessageLevel.ERROR;
import static java.text.MessageFormat.format;

public class JavascriptAppWebViewActivitity extends BroadcastListenerActivity {
    private static final String TAG = JavascriptAppWebViewActivitity.class.getSimpleName();
    public static final String PATIENT = "patient";
    public static final String APP_INTERFACE = "shrInterface";
    public static final String APP_TITLE = "appTitle";
    public static final String APP_SOURCE_FORM = "appSourceForm";

    private WebView webView;
    private MuzimaProgressDialog progressDialog;

    private SharedHealthRecordViewerJavascriptInterface webViewJavascriptInterface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar( );
        String appTitle = getIntent().getStringExtra(APP_TITLE);

        webViewJavascriptInterface = createWebViewJavascriptInterface();
        actionBar.setTitle(appTitle);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.activity_form_webview);
        progressDialog = new MuzimaProgressDialog(this);

        showProgressBar(getString(R.string.hint_loading_progress));
        try {
            setupWebView( );
        } catch (Throwable t) {
            Log.e(TAG, t.getMessage( ), t);
        }
        super.onStart( );
    }

    protected SharedHealthRecordViewerJavascriptInterface createWebViewJavascriptInterface(){
        return new SharedHealthRecordViewerJavascriptInterface(this);
    }


    protected void setupWebView() throws FormController.FormFetchException{
        FormController formController = ((MuzimaApplication)getApplicationContext()).getFormController();
        BaseForm baseForm = (BaseForm)getIntent().getSerializableExtra(APP_SOURCE_FORM);
        FormTemplate formTemplate = formController.getFormTemplateByUuid(baseForm.getFormUuid());

        webView = (WebView) findViewById(R.id.webView);
        webView.setWebChromeClient(createWebChromeClient( ));

        getSettings( ).setRenderPriority(WebSettings.RenderPriority.HIGH);
        getSettings( ).setJavaScriptEnabled(true);
        getSettings( ).setDatabaseEnabled(true);
        getSettings( ).setDomStorageEnabled(true);
        getSettings( ).setBuiltInZoomControls(true);
        webView.addJavascriptInterface(webViewJavascriptInterface, APP_INTERFACE);
        webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        webView.loadDataWithBaseURL("file:///android_asset/www/forms/", formTemplate.getHtml(),
                "text/html", "UTF-8", "");
    }

    private WebChromeClient createWebChromeClient() {
        return new WebChromeClient( ) {
            @Override
            public void onProgressChanged(WebView view, int progress) {
                JavascriptAppWebViewActivitity.this.setProgress(progress * 1000);
                if (progress == 100) {
                    progressDialog.dismiss( );
                }
            }

            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                String message = format("Javascript Log. Message: {0}, lineNumber: {1}, sourceId, {2}", consoleMessage.message( ),
                        consoleMessage.lineNumber( ), consoleMessage.sourceId( ));
                if (consoleMessage.messageLevel( ) == ERROR) {
                    Log.e(TAG, message);
                } else {
                    Log.d(TAG, message);
                }
                return true;
            }
        };
    }

    private WebSettings getSettings() {
        return webView.getSettings( );
    }

    public void showProgressBar(final String message) {
        runOnUiThread(new Runnable( ) {
            public void run() {
                progressDialog.show(message);
            }
        });
    }

    public void loadUrl(String url){
        webView.loadUrl(url);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent dataIntent) {
        try {
            SmartCardIntentResult intentResult = SmartCardIntentIntegrator.parseActivityResult(requestCode, resultCode, dataIntent);
            if(intentResult.isSuccessResult()) {
                Log.e(TAG,"REQUEST SUCCESSFUL: ");
                if (SmartCardIntentIntegrator.isReadRequest(requestCode)) {
                    Log.e(TAG,"READ REQUEST SUCCESSFUL: ");
                    webViewJavascriptInterface.onReadSharedHealthRecordFromCardActivityResultSuccess(intentResult.getSHRModel().getPlainSHRPayload());
                } else if (SmartCardIntentIntegrator.isWriteRequest(requestCode)) {
                    Log.e(TAG,"WRITE REQUEST SUCCESSFUL: ");

                    webViewJavascriptInterface.onWriteSharedHealthRecordFromCardActivityResultSuccess(intentResult.getSHRModel().getEncryptedSHRPayload());
                }
            } else {
                if(SmartCardIntentIntegrator.isReadRequest(requestCode)){
                    Log.e(TAG,"READ REQUEST NOT SUCCESSFUL: ");
                    webViewJavascriptInterface.onReadSharedHealthRecordFromCardActivityResultError(intentResult.getErrors());
                }
                if(SmartCardIntentIntegrator.isWriteRequest(requestCode)){
                    Log.e(TAG,"WRITE REQUEST NOT SUCCESSFUL: ");
                    webViewJavascriptInterface.onWriteSharedHealthRecordFromCardActivityResultError(intentResult.getErrors());
                }
            }
        } catch (Exception e){
            Log.e(TAG,"Could not retrieve result: ",e);
            if(SmartCardIntentIntegrator.isReadRequest(requestCode)){
                webViewJavascriptInterface.onReadSharedHealthRecordFromCardActivityResultError("Could not read SHR record: "+e.getMessage());
            }
            if(SmartCardIntentIntegrator.isWriteRequest(requestCode)){
                webViewJavascriptInterface.onWriteSharedHealthRecordFromCardActivityResultError("Could not write SHR record: "+e.getMessage());
            }
        }
    }
}