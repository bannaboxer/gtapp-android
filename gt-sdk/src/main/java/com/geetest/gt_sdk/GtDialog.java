package com.geetest.gt_sdk;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

/**
 * 验证对话框
 * 
 * @author dreamzsm@gmail.com
 * 
 */
public class GtDialog extends Dialog {

    private String baseURL = "http://static.geetest.com/static/appweb/android-index.html";

    private String captcha;
    private String challenge;
    private Boolean offline;
    private String product = "embed";
    private Boolean debug = false;

    public GtDialog (Context context, String captcha, String challenge, Boolean success) {

        super(context);
        this.captcha = captcha;
        this.challenge = challenge;
        this.offline = !success;
    }

    public void setBaseURL(String url) {
        this.baseURL = url;
    }

    public void setDebug(Boolean debug) {
        this.debug = debug;
    }

    public void setProduct(String product) {
        this.product = product;
    }
    
    @Override
    public void onDetachedFromWindow() {

        super.onDetachedFromWindow();
    }

    private WebView webView;

    public interface GtListener {
        void closeGt();
        void gtResult(boolean success, String result);
    }

    private GtListener gtListener;

    public void setGtListener(GtListener listener) {
        gtListener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        webView = new WebView(getContext());

        setContentView(webView);

        LayoutParams layoutParams = webView.getLayoutParams();

        layoutParams.height = LayoutParams.MATCH_PARENT;
        layoutParams.width = LayoutParams.MATCH_PARENT;
        webView.setLayoutParams(layoutParams);
        
        WebSettings settings = webView.getSettings();
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        settings.setJavaScriptEnabled(true);
        webView.addJavascriptInterface(new JSInterface(), "JSInterface");

        ClientInfo clientInfo = ClientInfo.build(getContext());

        String mobile_info = clientInfo.toJsonString();

        String gt_mobile_req_url = baseURL
                + "?gt=" + this.captcha
                + "&challenge=" + this.challenge
                + "&offline=" + this.offline
                + "&product=" + this.product
                + "&debug=" + this.debug
                + "&mobileInfo=" + mobile_info;

        GeetestLib.log_v(gt_mobile_req_url);

        webView.loadUrl(gt_mobile_req_url);
        GeetestLib.log_v(gt_mobile_req_url);

        webView.setWebChromeClient(new WebChromeClient() {

            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                // TODO Auto-generated method stub
                super.onProgressChanged(view, newProgress);
                if (newProgress == 100) {
                    webView.getLayoutParams().height = LayoutParams.MATCH_PARENT;
                    webView.getLayoutParams().width = LayoutParams.MATCH_PARENT;
                    webView.setLayoutParams(webView.getLayoutParams());
                }
            }
        });

        webView.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageFinished(WebView view, String url) {
                // TODO Auto-generated method stub
                super.onPageFinished(view, url);
            }

        });
    }

    public class JSInterface {

        @JavascriptInterface
        public void gtCallBack(String code, String result, String message) {
            GeetestLib.log_v("gtCallBack");
            GeetestLib.log_v("code:" + code);
            GeetestLib.log_v("result:" + result);
            GeetestLib.log_v("message:client result" + message);
            int codeInt;
            try {
                codeInt = Integer.parseInt(code);
                if (codeInt == 1) {
                    dismiss();

                    if (gtListener != null) {
                        gtListener.gtResult(true, result);
                    }

                } else {
                    if (gtListener != null) {
                        gtListener.gtResult(false, result);
                    }
                    Toast.makeText(getContext(), "message:" + message,
                            Toast.LENGTH_LONG).show();
                }
            } catch (NumberFormatException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }

        @JavascriptInterface
        public void gtCloseWindow() {
            GeetestLib.log_v("gtCloseWindow");
            dismiss();
            if (gtListener != null) {
                gtListener.closeGt();
            }
        }

    }

}