package com.why.project.mywebviewdemo.customwebview.mywebview;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebStorage;
import android.webkit.WebView;
import android.webkit.WebViewClient;


/**
 * @Created HaiyuKing
 * Used 主要处理解析，渲染网页等浏览器做的事情。帮助WebView处理各种通知、请求事件：比如页面加载的开始、结束、失败时的对话框提示
 */
public class MyWebViewClient extends WebViewClient {

	private static final String TAG = MyWebViewClient.class.getSimpleName();
	
	/**依赖的窗口*/
	private Context context;
	private MyWebView myWebView;

	/**进度加载对话框*/
	private MyWebViewProgressDialog progressDialog;

	private boolean blockLoadingNetworkImage=false;//WebView 图片延迟加载【暂时用不到】

	private boolean needClearHistory = false;//是否需要清除历史记录
	
	public MyWebViewClient(Context context, MyWebView myWebView) {
		this.context = context;
		this.myWebView = myWebView;
	}

	/**
	 * 重写此方法表明点击网页内的链接由自己处理，而不是新开Android的系统browser中响应该链接。
	 */
	@Override
	public boolean shouldOverrideUrlLoading(WebView view, String url) {
		Log.e(TAG,"{shouldOverrideUrlLoading}url="+url);
		if(url.startsWith("tel:")) {//拨打电话
            Intent intent = new Intent("android.intent.action.VIEW", Uri.parse(url));
            context.startActivity(intent);
            return true;
        }else{
			//view.loadUrl(url);//根据传入的参数再去加载新的网页【注意，需要注释掉】
			return false;//表示当前的webview可以处理打开新网页的请求，不用借助系统浏览器,【false 显示frameset, true 不显示Frameset】
		}
	}

	//是否在webview内加载页面【高版本写法】
	/**
	 * return true 表示当前url即使是重定向url也不会再执行（除了在return true之前使用webview.loadUrl(url)除外，因为这个会重新加载）
	 return false  表示由系统执行url，直到不再执行此方法，即加载完重定向的ur（即具体的url，不再有重定向）

	 * @param view
	 * @param request
	 * @return
	 */
//	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
//	@Override
	public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
		String url = "";
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			url = request.getUrl().toString();
		} else {
			url = request.toString();
		}
		Log.e(TAG,"{shouldOverrideUrlLoading}request.toString()="+url);

		if(url.startsWith("tel:")) {//拨打电话
			Intent intent = new Intent("android.intent.action.VIEW", Uri.parse(url));
			context.startActivity(intent);
			return true;
		}else{
			//view.loadUrl(url);//根据传入的参数再去加载新的网页【注意，需要注释掉】
			return false;//表示当前的webview可以处理打开新网页的请求，不用借助系统浏览器
		}
	}


	/**
	 * 网页加载开始时调用，显示加载提示旋转进度条
	 */
	@Override
	public void onPageStarted(WebView view, String url, Bitmap favicon) {
		// TODO Auto-generated method stub
		super.onPageStarted(view, url, favicon);
		Log.e(TAG,"{onPageStarted}url="+url);
		showProgressDialog();
	}
	
	
	/**
	 * 网页加载完成时调用，比如：隐藏加载提示旋转进度条*/
	@Override
	public void onPageFinished(WebView view, String url) {
		// TODO Auto-generated method stub
		super.onPageFinished(view, url);
		Log.e(TAG,"{onPageFinished}url="+url);
		dismissProgressDialog();
	}
	
	/**
	 * 网页加载失败时调用，隐藏加载提示旋转进度条
	 */
	@Override
	public void onReceivedError(WebView view, int errorCode,
								String description, String failingUrl) {
		Log.e(TAG,"{onReceivedError}failingUrl="+failingUrl);
		super.onReceivedError(view, errorCode, description, failingUrl);

		dismissProgressDialog();

		myWebView.setRefreshUrl(failingUrl);//保存网络异常时的URL地址，用于刷新
		myWebView.loadLocalUrl("404.html");
	}

	/**
	 * 直接捕获到404
	 */
	@Override
	public void onReceivedHttpError(WebView webView, WebResourceRequest webResourceRequest, WebResourceResponse webResourceResponse) {
		super.onReceivedHttpError(webView, webResourceRequest, webResourceResponse);
		String url = "";
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			url = webResourceRequest.getUrl().toString();
		} else {
			url = webResourceRequest.toString();
		}
		Log.e(TAG, "{onReceivedHttpError}url=" + url);
	}



	/**
     * 显示进度加载对话框
     * param msg 显示内容
     */
    public void showProgressDialog() {
    	try {
            if (progressDialog == null) {
                progressDialog = new MyWebViewProgressDialog(context);
            }
            progressDialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * 隐藏进度加载对话框
     */
    public void dismissProgressDialog() {
    	try {
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

	/*=================================根据需要清除历史记录=================================*/
	@Override
	public void doUpdateVisitedHistory(WebView view, String url, boolean isReload) {
		super.doUpdateVisitedHistory(view, url, isReload);
		Log.w(TAG, "{doUpdateVisitedHistory}");
		if(needClearHistory){
			view.clearHistory();//清除历史记录
			needClearHistory = false;
		}
	}

	/**一般在选项卡切换打开webview的时候调用*/
	public void setNeedClearHistory(boolean needClearHistory) {
		this.needClearHistory = needClearHistory;
	}
	
    /**扩充数据库的容量*/
    public void onExceededDatabaseQuota(String url, String databaseIdentifier, long currentQuota, long estimatedSize, long totalUsedQuota, WebStorage.QuotaUpdater quotaUpdater) {
    	quotaUpdater.updateQuota(estimatedSize * 2);  
    }

}
