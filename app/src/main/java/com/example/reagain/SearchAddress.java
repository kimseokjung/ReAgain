package com.example.reagain;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class SearchAddress extends AppCompatActivity {

    private WebView webView;

    class SearchAddressInterface {
        @JavascriptInterface
        @SuppressWarnings("unused")
        public void processDATA(String data) {

            Bundle extra = new Bundle();
            Intent intent = new Intent();
            extra.putString("data", data);
            intent.putExtras(extra);
            setResult(RESULT_OK, intent);
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_address);

        webView = findViewById(R.id.webView_address);

        webView.getSettings().setJavaScriptEnabled(true);
        webView.addJavascriptInterface(new SearchAddressInterface(), "Android");

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {

                webView.loadUrl("javascript:sample2_execDaumPostcode();");
            }
        });

        webView.loadUrl("http://www.inspond.com/daum.html");

    }
}
//
//        // WebView 초기화
//        init_webView();
//
//        // 핸들러를 통한 JavaScript 이벤트 반응
//        handler = new Handler();
//    }
//    public void init_webView() {
//        // WebView 설정
//        webView = findViewById(R.id.webView_address);
//
//        // JavaScript 허용
//        webView.getSettings().setJavaScriptEnabled(true);
//
//        // JavaScript의 window.open 허용
//        webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
//
//
//        // JavaScript이벤트에 대응할 함수를 정의 한 클래스를 붙여줌
//        webView.addJavascriptInterface(new AndroidBridge(), "TestApp");
//
//        // web client 를 chrome 으로 설정
//        webView.setWebChromeClient(new WebChromeClient());
//
//        // webview url load. php 파일 주소
//        webView.loadUrl("file:///android_asset/daum.html");
//
//    }
//
//
//    private class AndroidBridge {
//        @JavascriptInterface
//        public void setAddress(final String arg1, final String arg2, final String arg3) {
//            handler.post(new Runnable() {
//                @Override
//                public void run() {
//                    txt_address.setText(String.format("(%s) %s %s", arg1, arg2, arg3));
//
//                    // WebView를 초기화 하지않으면 재사용할 수 없음
//                    init_webView();
//                }
//            });
//        }
//    }
//
//}
