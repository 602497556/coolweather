package com.coolweather.app.activity;

import com.coolweather.app.R;
import com.coolweather.app.util.HttpCallbackListener;
import com.coolweather.app.util.HttpUtil;
import com.coolweather.app.util.Utility;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;

public class WeatherActivity extends Activity {
	
	private LinearLayout weatherInfoLayout;
	
	private TextView cityNameText;
	private TextView publishText;
	private TextView weatherDespText;
	private TextView temp1Text;
	private TextView temp2Text;
	private TextView currentDateText;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.weather_layout);
		
		cityNameText = (TextView) findViewById(R.id.city_name);
		publishText = (TextView) findViewById(R.id.publish_text);
		weatherInfoLayout = (LinearLayout) findViewById(R.id.weather_info_layout);
		currentDateText = (TextView) findViewById(R.id.current_date);
		weatherDespText = (TextView) findViewById(R.id.weather_desp);
		temp1Text = (TextView) findViewById(R.id.temp1);
		temp2Text = (TextView) findViewById(R.id.temp2);
		
		String countyCode = getIntent().getStringExtra("county_code");
		
		if ( !TextUtils.isEmpty(countyCode)){
			publishText.setText("正在同步中...");
			cityNameText.setVisibility(View.INVISIBLE);
			weatherInfoLayout.setVisibility(View.INVISIBLE);
			queryWeatherCode(countyCode);
		} else {
			showWeather();
		}
	}

	/*
	 * 查询县所对应的天气代号
	 */
	private void queryWeatherCode(String countyCode) {
		String address = "http://www.weather.com.cn/data/list3/city"+countyCode+".xml";
		queryFromServer(address,"countyCode");
	}
	
	/*
	 * 查询天气代号对应的天气
	 */
	protected void queryWeatherInfo(String weatherCode) {
		String address = "http://www.weather.com.cn/data/cityinfo/"+weatherCode+".html";
		queryFromServer(address, "weatherCode");
	}

	/*
	 * 向服务器查询天气代号或者天气信息
	 */
	private void queryFromServer(String address,final String type) {
		
		Log.e("", "***************"+ address +"***************");
		
		HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
			@Override
			public void onFinish(String response) {
				if ("countyCode".equals(type)){
					if ( ! TextUtils.isEmpty(response)){
						String[] array = response.split("\\|");
						if ( array != null && array.length == 2){
							String weatherCode = array[1];
							Log.e("", "**********"+ weatherCode +"**********");
							queryWeatherInfo(weatherCode);
						}
					}
				} else if ("weatherCode".equals(type)){
					Log.e("", "**********"+ response +"**********");
					Utility.handleWeatherResponse(WeatherActivity.this, response);
					// 通过runOnUiThread()方法回到UI线程进行UI操作
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							showWeather();
						}
					});
				}
			}
			
			@Override
			public void onError(Exception e) {
				// 通过runOnUiThread()方法回到UI线程进行UI操作
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						publishText.setText("同步失败");
					}
				});
			}
		});
		
	}
	
	/*
	 * 显示存储在SharedPreferences中的天气信息
	 */
	private void showWeather() {
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
		cityNameText.setText(sp.getString("city_name", ""));
		publishText.setText("今天"+sp.getString("publish_time", "")+"发布");
		currentDateText.setText(sp.getString("current_date", ""));
		weatherDespText.setText(sp.getString("weather_desp", ""));
		temp1Text.setText(sp.getString("temp1", ""));
		temp2Text.setText(sp.getString("temp2", ""));
		cityNameText.setVisibility(View.VISIBLE);
		weatherInfoLayout.setVisibility(View.VISIBLE);
	}
	
	
	

}
