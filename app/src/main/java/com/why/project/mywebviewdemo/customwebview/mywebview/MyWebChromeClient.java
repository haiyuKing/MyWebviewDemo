package com.why.project.mywebviewdemo.customwebview.mywebview;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;


/**
 * @Created HaiyuKing
 * Used 处理解析，渲染网页等浏览器做的事情。辅助WebView处理Javascript的对话框，网站图标，网站title，加载进度等
 */
public class MyWebChromeClient extends WebChromeClient {

	private static final String TAG = MyWebChromeClient.class.getSimpleName();

	/**依赖的窗口*/
	private Context context;
	private MyWebView myWebView;

	public MyWebChromeClient(Context context, MyWebView myWebView) {
		this.context = context;
		this.myWebView = myWebView;
	}

	//更改加载进度值
	@Override
	public void onProgressChanged(WebView view, int newProgress) {
		// TODO Auto-generated method stub
		super.onProgressChanged(view, newProgress);
	}

	/*=========================================实现webview打开文件管理器功能==============================================*/
	/**
	 * HTML界面：
	 * <input accept="image/*" capture="camera" id="imgFile" name="imgFile" type="file">
	 * <input type="file" capture="camera" accept="* /*" name="image">
	 * <input type="file" accept="image/*" onchange="previewImage(this)" style="display: none;" class="previewImg">
	 *  */

	/**
	 * 重写WebChromeClient 的openFileChooser方法
	 * 这里有个漏洞，4.4.x的由于系统内核发生了改变，没法调用以上方法，现在仍然找不到解决办法，唯一的方法就是4.4直接使用手机浏览器打开，这个是可以的。
	 * */

	private ValueCallback<Uri> mUploadMessage;//5.0--版本用到的
	private ValueCallback<Uri[]> mUploadCallbackAboveL;//5.0++版本用到的

	//3.0--
	public void openFileChooser(ValueCallback<Uri> uploadMsg) {
		openFileChooserImpl(uploadMsg);
	}
	//3.0++
	public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType) {
		openFileChooserImpl(uploadMsg);
	}
	//4.4--(4.4.2特殊，不执行该方法)
	public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
		openFileChooserImpl(uploadMsg);
	}
	//5.0++
	@Override
	public boolean onShowFileChooser(WebView webView,
									 ValueCallback<Uri[]> filePathCallback,
									 FileChooserParams fileChooserParams) {
		openFileChooserImplForAndroid5(filePathCallback);
		return true;
	}
	//5.0--的调用
	private void openFileChooserImpl(ValueCallback<Uri> uploadMsg) {
		mUploadMessage = uploadMsg;
		dispatchTakePictureIntent();
	}
	//5.0++的调用
	private void openFileChooserImplForAndroid5(ValueCallback<Uri[]> uploadMsg) {
		mUploadCallbackAboveL = uploadMsg;
		dispatchTakePictureIntent();
	}

	//拍照或者打开文件管理器
	private void dispatchTakePictureIntent() {
		if(mUploadMessage!=null){
			Log.w(TAG,"mUploadMessage.toString()="+mUploadMessage.toString());
		}
		if(mUploadCallbackAboveL!=null) {
			Log.w(TAG, "mUploadCallbackAboveL.toString()=" + mUploadCallbackAboveL.toString());
		}
		WebViewJSInterface.getInstance(context,myWebView).chooseFile();
	}

	public ValueCallback<Uri> getmUploadMessage() {
		return mUploadMessage;
	}

	public ValueCallback<Uri[]> getmUploadCallbackAboveL() {
		return mUploadCallbackAboveL;
	}

	public void setmUploadMessage(ValueCallback<Uri> mUploadMessage) {
		this.mUploadMessage = mUploadMessage;
	}

	public void setmUploadCallbackAboveL(ValueCallback<Uri[]> mUploadCallbackAboveL) {
		this.mUploadCallbackAboveL = mUploadCallbackAboveL;
	}
	
}
