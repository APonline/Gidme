package ca.gidme.gidme;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class Webs extends AppCompatActivity {

    private WebView mWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.web_view);

        // Get the Intent that started this activity and extract the string
        Intent intent = getIntent();
        String uri = intent.getStringExtra(MainActivity.WEB_URI);


        setContentView(R.layout.web_view);
        mWebView = (WebView)findViewById(R.id.webview);
        mWebView.setWebViewClient(new WebViewClient());
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.loadUrl(uri);
    }


    /** Called when the user taps the Clear button */
    public void goBackToView() {
        Intent intent = new Intent(this, MainActivity.class);

        startActivity(intent);
    }
}
