package com.why.project.mywebviewdemo;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.tbruyelle.rxpermissions2.RxPermissions;

import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;

public class MainActivity extends AppCompatActivity {
	private static final String TAG = MainActivity.class.getSimpleName();

	private EditText mUrlEdt;
	private Button mWebviewBtn;
	private Button mLocationBtn;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		onePermission();//申请运行时权限

		initViews();
		initEvents();
	}

	private void initViews() {
		mUrlEdt = findViewById(R.id.url_edt);
		mWebviewBtn = findViewById(R.id.webview_btn);
		mLocationBtn = findViewById(R.id.location_btn);
	}
	private void initEvents() {
		mWebviewBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String openUrl = "";
				if (!TextUtils.isEmpty(mUrlEdt.getText().toString())) {
					openUrl = mUrlEdt.getText().toString();
				}
				Intent intent = new Intent(MainActivity.this, MyWebviewActivity.class);
				Bundle bundle = new Bundle();
				bundle.putString("urlKey", openUrl);
				intent.putExtras(bundle);
				MainActivity.this.startActivity(intent);
			}
		});

		mLocationBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String openUrl = "";
				Intent intent = new Intent(MainActivity.this, MyWebviewActivity.class);
				Bundle bundle = new Bundle();
				bundle.putString("urlKey", openUrl);
				intent.putExtras(bundle);
				MainActivity.this.startActivity(intent);
			}
		});
	}


	/**只有一个运行时权限申请的情况*/
	private void onePermission(){
		RxPermissions rxPermissions = new RxPermissions(MainActivity.this); // where this is an Activity instance
		rxPermissions.request(Manifest.permission.CAMERA,Manifest.permission.RECORD_AUDIO,Manifest.permission.READ_EXTERNAL_STORAGE) //权限名称，多个权限之间逗号分隔开
				.subscribe(new Consumer<Boolean>() {
					@Override
					public void accept(Boolean granted) throws Exception {
						Log.e(TAG, "{accept}granted=" + granted);//执行顺序——1【多个权限的情况，只有所有的权限均允许的情况下granted==true】
						if (granted) { // 在android 6.0之前会默认返回true
							// 已经获取权限
							Toast.makeText(MainActivity.this, "已经获取权限", Toast.LENGTH_SHORT).show();
						} else {
							// 未获取权限
							Toast.makeText(MainActivity.this, "您没有授权该权限，请在设置中打开授权", Toast.LENGTH_SHORT).show();
						}
					}
				}, new Consumer<Throwable>() {
					@Override
					public void accept(Throwable throwable) throws Exception {
						Log.e(TAG,"{accept}");//可能是授权异常的情况下的处理
					}
				}, new Action() {
					@Override
					public void run() throws Exception {
						Log.e(TAG,"{run}");//执行顺序——2
					}
				});
	}

}
