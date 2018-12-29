package com.why.project.mywebviewdemo.customwebview.mywebview;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.webkit.WebSettings;
import android.webkit.WebView;

/**
 * @Created HaiyuKing
 * Used 封装webview
 * http://www.cnblogs.com/bluestorm/archive/2013/04/15/3021996.html
 */
public class MyWebView extends WebView {

	private static final String TAG = MyWebView.class.getSimpleName();
	
	/**网页编码*/
	private static final String ENCODENAME = "utf-8";
	/**缩放等级 ：这个值很重要，决定了展现效果【可缩放状态下 100 代表不缩放】*/
	private static final int SCALEVALUE_CANSCALE = 100;
	/**缩放等级 ：【某些个别情况用到了，比如该HTML界面不是专门适用于手机展现的情况】【暂时留作记录，应该用不到】*/
	//private static final int SCALEVALUE_NOTCANSCALE = 300;
	
	/**上下文*/
	private Context mContext;

	private MyWebChromeClient myWebChromeClient;
	private MyWebViewClient myWebViewClient;

	/**是否可以返回到上一页(true:可以，默认；false：不可以)*/
	private boolean canBackPreviousPage = false;
	/**当前Webview所处的上下文（默认大家使用的是DialogFragment）【用于点击底部返回键时执行onKeyDown方法时关闭上下文】*/
	private DialogFragment mDialog;
	private Activity mActivity;
	private Fragment mFragment;

	private String refreshUrl;//网络异常时保存的上一个URL地址【MyWebViewClient中赋值，然后在404.html执行刷新的js方法的时候执行WebViewJSInterface中的refresh方法中使用】

	/*
     * 在Code中new实例化一个View会调用第一个构造函数
     * 如果在xml中定义会调用第二个构造函数
     * 而第三个函数系统是不调用的，要由View（我们自定义的或系统预定义的View）显式调用，一般用于自定义属性的相关操作
     * */
	/**在Java代码中new实例化的时候调用*/
	public MyWebView(Context context) {
		super(context);
		mContext = context;
		initWebViewSet(false);
	}
	/**在xml布局文件中定义的时候调用*/
	public MyWebView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		initWebViewSet(false);
	}
	
	/**
	 * 初始化WebView的常规设置*/
	@SuppressLint({ "SetJavaScriptEnabled", "NewApi" })
	public void initWebViewSet(boolean canZoom){
		
		this.setFocusable(true);//设置可触摸的
		this.requestFocus();//获取焦点。解决输入法不出来的问题
		this.requestFocusFromTouch();//获取焦点，补充requestFocus不能获取焦点的情况
		
		WebSettings webSettings = this.getSettings();
		
		/*=============================JS的相关设置===========================================*/
		
		webSettings.setJavaScriptEnabled(true);//设置WebView是否允许执行JavaScript脚本，默认false，不允许。
		webSettings.setJavaScriptCanOpenWindowsAutomatically(true);//让JavaScript自动打开窗口，默认false。适用于JavaScript方法window.open()。

		/*=============================缓存机制的相关设置===========================================*/
		webSettings.setAllowFileAccess(true);//是否允许访问文件，默认允许。注意，这里只是允许或禁止对文件系统的访问，Assets 和 resources 文件使用file:///android_asset和file:///android_res仍是可访问的。  
		webSettings.setDomStorageEnabled(true);// 使用localStorage则必须打开, 支持文件存储

//		webSetting.setDatabaseEnabled(true);//数据库存储API是否可用，默认值false。
//		webSetting.setDatabasePath(mContext.getApplicationContext().getDir("databases", 0).getPath());

		// 启动应用缓存
		webSettings.setAppCacheEnabled(true);//应用缓存API是否可用，默认值false, 结合setAppCachePath(String)使用。
		webSettings.setAppCacheMaxSize(Long.MAX_VALUE);//设置app缓存容量
		String appCacheDir = mContext.getApplicationContext().getDir("appcache", Context.MODE_PRIVATE).getPath();   //缓存存储路径
		webSettings.setAppCachePath(appCacheDir);//设置缓存文件的路径

		webSettings.setGeolocationEnabled(true);
		webSettings.setGeolocationDatabasePath(mContext.getApplicationContext().getDir("geolocation", 0)
				.getPath());
		
		// 设置缓存模式【我们可以在有网的情况下将缓存模式改为websetting.setCacheMode(WebSettings.LOAD_DEFAULT);当没有网络时则设置为 LOAD_CACHE_ELSE_NETWORK】
		webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);// 设置不使用缓存
		//webSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);//第一次加载之后便在本地缓存，如果没网络就加载缓存，

		webSettings.setBlockNetworkImage(false);//WebView是否下载图片资源，默认为true。【此处需要为false。否则图片不展现】
		
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN){
			webSettings.setAllowUniversalAccessFromFileURLs(true);
		}

		if(canZoom){//(如果要支持缩放，html页面本身也要支持缩放：不能加user-scalable=no)  
			/*=================================webview页面支持放大缩小（二选一）===================================*/
			webSettings.setSupportZoom(true);//是否应该支持使用其屏幕缩放控件和手势缩放，默认值true
			webSettings.setBuiltInZoomControls(true);//设置触摸可缩放  ，默认值为false。
			webSettings.setDisplayZoomControls(false);//放大缩小的图标不显示，true或者不设置则为显示
			// 设置此属性，可任意比例缩放。
			webSettings.setUseWideViewPort(true); 
			//是否允许WebView度超出以概览的方式载入页面，默认false。即缩小内容以适应屏幕宽度
			webSettings.setLoadWithOverviewMode(true);
			this.setInitialScale(SCALEVALUE_CANSCALE);//缩放等级 ，可以在这里根据需求来设置缩放比例。
			
		}else{
			/*=================================webview页面自适应屏幕（二选一）===================================*/
			webSettings.setSupportZoom(false);//是否应该支持使用其屏幕缩放控件和手势缩放,默认值true
			webSettings.setBuiltInZoomControls(false);//设置触摸可缩放  ，默认值为false。
			//设置加载进来的页面自适应手机屏幕【方案一】
			webSettings.setDisplayZoomControls(false); //隐藏webview缩放按钮
			// 设置此属性，可任意比例缩放。
			webSettings.setUseWideViewPort(true); 
			//是否允许WebView度超出以概览的方式载入页面，默认false。即缩小内容以适应屏幕宽度
			webSettings.setLoadWithOverviewMode(true);
		}
		// 排版适应屏幕 设置布局，会引起WebView的重新布局（relayout）,默认值NARROW_COLUMNS
		webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NARROW_COLUMNS);
		
		webSettings.setDefaultTextEncodingName(ENCODENAME);//设置网页默认编码

		//解决低版本机型下提示“是否希望浏览器记住此密码”的提示框
		webSettings.setSavePassword(false);
		webSettings.setSaveFormData(false);//WebView是否保存表单数据，默认值true。【可以控制是否保存输入框的历史记录】
		
		webSettings.setSupportMultipleWindows(false);//设置WebView是否支持多窗口。如果设置为true，主程序要实现onCreateWindow(WebView, boolean, boolean, Message)，默认false。
		// webview从5.0开始默认不允许混合模式,https中不能加载http资源,需要设置开启。
		//当一个安全的来源（origin）试图从一个不安全的来源加载资源时配置WebView的行为。
		//默认情况下，KITKAT及更低版本默认值为MIXED_CONTENT_ALWAYS_ALLOW，
		//LOLLIPOP版本默认值MIXED_CONTENT_NEVER_ALLOW，
		//WebView首选的最安全的操作模式为MIXED_CONTENT_NEVER_ALLOW ，不鼓励使用MIXED_CONTENT_ALWAYS_ALLOW。
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        	webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }
        
        webSettings.setTextZoom(100);//设置页面上的文本缩放百分比，默认100。
        //webSettings.setRenderPriority(WebSettings.RenderPriority.HIGH);//在API18以上已废弃。不建议调整线程优先级，未来版本不会支持这样做。
        
        /*=================================滚动条设置===================================*/
		this.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);//设置滚动条样式--取消滚动条
		this.setHorizontalScrollBarEnabled(false);//设置水平方向滚动条不可用
		this.setHorizontalScrollbarOverlay(false);//指定水平滚动条无叠加样式
		this.setVerticalScrollBarEnabled(false);//设置竖直方向滚动条不可用
		
		//使用WebChormClient的特性处理html页面
		myWebChromeClient = new MyWebChromeClient(getContext(),this);
		this.setWebChromeClient(myWebChromeClient);
		//使用WebViewClient的特性处理html页面
		myWebViewClient = new MyWebViewClient(getContext(),this);
		this.setWebViewClient(myWebViewClient);

		//实现html文件中可以调用java方法
		addJavascriptInterface(WebViewJSInterface.getInstance(mContext,this), "androidMethod");
	}
	
	/**加载远程网页
	 * @param webUrl - 远程网页链接地址：比如http://m.baidu.com/*/
	public void loadWebUrl(String webUrl){
		this.loadUrl(webUrl);
	}
	
	/**加载本地assets目录下的网页
	 * @param localUrl - assets目录下的路径：比如www/login.html*/
	public void loadLocalUrl(String localUrl){
		this.loadUrl("file:///android_asset/"+localUrl);
	}


	/**设置是否直接退出还是返回上一页面【根据实际情况，可以再返回当前webview的载体（activity或者DialogFragment）进行处理】*/
	public void setCanBackPreviousPage(boolean canBackPreviousPage,DialogFragment dialog) {
		this.canBackPreviousPage = canBackPreviousPage;
		this.mDialog = dialog;
	}

	public void setCanBackPreviousPage(boolean canBackPreviousPage,Activity activtiy) {
		this.canBackPreviousPage = canBackPreviousPage;
		this.mActivity = activtiy;
	}

	public void setCanBackPreviousPage(boolean canBackPreviousPage,Fragment fragment) {
		this.canBackPreviousPage = canBackPreviousPage;
		this.mFragment = fragment;
	}

	/**按返回键时， 是不退出当前界面而是返回上一浏览页面还是直接退出当前界面*/
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		Log.e(TAG,"{onKeyDown}canBackPreviousPage="+canBackPreviousPage);
		if(canBackPreviousPage){
			if ((keyCode == KeyEvent.KEYCODE_BACK) && canGoBack()) {
				goBack();
				return true;
			}else if((keyCode == KeyEvent.KEYCODE_BACK) && !canGoBack()){
				//当没有上一页可返回的时候
				//此处执行DialogFragment关闭或者Activity关闭.....
				if(mDialog != null){
					mDialog.dismiss();
				}
				if(mActivity != null || mFragment != null){
					return  false;//将操作返回到activity处理
				}
				return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	public MyWebViewClient getMyWebViewClient() {
		return myWebViewClient;
	}

	public MyWebChromeClient getMyWebChromeClient() {
		return myWebChromeClient;
	}

	public String getRefreshUrl() {
		return refreshUrl;
	}

	public void setRefreshUrl(String refreshUrl) {
		this.refreshUrl = refreshUrl;
	}
}
