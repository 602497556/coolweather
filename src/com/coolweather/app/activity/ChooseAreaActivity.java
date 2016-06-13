package com.coolweather.app.activity;

import java.util.ArrayList;
import java.util.List;

import com.coolweather.app.R;
import com.coolweather.app.db.CoolWeatherDB;
import com.coolweather.app.model.City;
import com.coolweather.app.model.County;
import com.coolweather.app.model.Province;
import com.coolweather.app.util.HttpCallbackListener;
import com.coolweather.app.util.HttpUtil;
import com.coolweather.app.util.Utility;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ChooseAreaActivity extends Activity {
	
	public static final int LEVEL_PROVINCE = 0;
	public static final int LEVEL_CITY = 1;
	public static final int LEVEL_COUNTY = 2;
	
	private int currentLevel;
	
	private TextView titleText;
	private ListView listView;
	
	private CoolWeatherDB coolWeatherDB;
	
	private List<Province> provinceList;
	private List<City> cityList;
	private List<County> countyList;
	
	private Province selectedProvince;
	private City selectedCity;
	
	private ArrayAdapter<String> adapter;
	
	private List<String> dataList = new ArrayList<String>();
	
	private ProgressDialog progressDialog;
	
	boolean isFromWeatherActivity;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		isFromWeatherActivity = getIntent().getBooleanExtra("from_weather_activity", false);
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		if ( prefs.getBoolean("city_selected", false) && !isFromWeatherActivity ){
			Intent i = new Intent(ChooseAreaActivity.this,WeatherActivity.class);
			startActivity(i);
			finish();
			return;
		}
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.choose_area);
		coolWeatherDB = CoolWeatherDB.getInstance(this);
		titleText = (TextView) findViewById(R.id.title_text);
		listView = (ListView) findViewById(R.id.list_view);
		adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,dataList);
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id){
				if (currentLevel == LEVEL_PROVINCE){
					selectedProvince = provinceList.get(position);
					queryCities();
				} else if (currentLevel == LEVEL_CITY){
					selectedCity = cityList.get(position);
					queryCounties();
				} else if (currentLevel == LEVEL_COUNTY){
					String countyCode = countyList.get(position).getCountyCode();
					Intent intent = new Intent(ChooseAreaActivity.this,WeatherActivity.class);
					intent.putExtra("county_code",countyCode);
					startActivity(intent);
					finish();
				}
			}
		});
		queryProvinces();
	}
	
	/*
	 *查询全国所有的省份，先从本地查询，没有再从服务器获取 
	 */
	private void queryProvinces() {
		provinceList = coolWeatherDB.loadProvinces();
		if ( provinceList.size()>0 ){
			dataList.clear();
			for (Province p : provinceList){
				dataList.add(p.getProvinceName());
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			titleText.setText("中国");
			currentLevel = LEVEL_PROVINCE;
		} else {
			queryFromServer(null,"province");
		}
	}
	
	/*
	 *查询某省下所有的市，先从本地查询，没有再从服务器获取 
	 */
	private void queryCities(){
		cityList = coolWeatherDB.loadCities(selectedProvince.getId());
		if ( cityList.size() >0 ){
			dataList.clear();
			for (City c: cityList){
				dataList.add(c.getCityName());
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			titleText.setText(selectedProvince.getProvinceName());
			currentLevel = LEVEL_CITY;
		} else {
			queryFromServer(selectedProvince.getProvinceCode(), "city");
		}
	}
	
	/*
	 *查询某市下所有的县，先从本地查询，没有再从服务器获取 
	 */
	private void queryCounties(){
		countyList = coolWeatherDB.loadCounties(selectedCity.getId());
		if ( countyList.size() >0 ){
			dataList.clear();
			for (County c: countyList){
				dataList.add(c.getCountyName());
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			titleText.setText(selectedCity.getCityName());
			currentLevel = LEVEL_COUNTY;
		} else {
			queryFromServer(selectedCity.getCityCode(), "county");
		}
	}
	
	/*
	 * 根据传入的代号和类型从服务器上查询省市县数据
	 */
	private void queryFromServer(String code,final String type) {
		String address;
		if (!TextUtils.isEmpty(code)){
			address = "http://www.weather.com.cn/data/list3/city"+code+".xml";
		} else {
			address = "http://www.weather.com.cn/data/list3/city.xml";
		}
		showProgressDialog();
		HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
			@Override
			public void onFinish(String response) {
				boolean result = false;
				if ("province".equals(type)){
					result = Utility.handleProvincesResponse(coolWeatherDB, response);
				} else if ("city".equals(type)){
					result = Utility.handleCitiesResponse(coolWeatherDB, response, selectedProvince.getId());
				} else if ("county".equals(type)){
					result = Utility.handleCountiesResponse(coolWeatherDB, response, selectedCity.getId());
				}
				if (result){
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							closeProgressDialog();
							if ("province".equals(type)){
								queryProvinces();
							} else if ("city".equals(type)){
								queryCities();
							} else if ("county".equals(type)){
								queryCounties();
							}
						}
					});
				}
			}
			@Override
			public void onError(Exception e) {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						closeProgressDialog();
						Toast.makeText(ChooseAreaActivity.this, "加载出错", Toast.LENGTH_SHORT).show();
					}
				});
			}
		});	
	}
	
	/*
	 * 显示进度条对话框
	 */
	private void showProgressDialog(){
		if (progressDialog == null){
			progressDialog = new ProgressDialog(this);
			progressDialog.setMessage("正在加载");
			progressDialog.setCanceledOnTouchOutside(false);
		}
		progressDialog.show();
	}
	
	/*
	 * 关闭进度条
	 */
	private void closeProgressDialog(){
		if( progressDialog != null){
			progressDialog.dismiss();
		}
	}
	
	/*
	 * 捕获Back按键
	 */
	@Override
	public void onBackPressed() {
		if (currentLevel == LEVEL_COUNTY){
			queryCities();
		} else if (currentLevel == LEVEL_CITY){
			queryProvinces();
		} else if (currentLevel == LEVEL_PROVINCE ){
			if (isFromWeatherActivity ){
				Intent intent = new Intent(ChooseAreaActivity.this,WeatherActivity.class);
				startActivity(intent);
				}
			finish();
		}
	}
	
	
}
