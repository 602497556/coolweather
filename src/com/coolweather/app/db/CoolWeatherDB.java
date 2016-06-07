package com.coolweather.app.db;

import java.util.ArrayList;
import java.util.List;

import com.coolweather.app.model.City;
import com.coolweather.app.model.County;
import com.coolweather.app.model.Province;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class CoolWeatherDB {
	
	/*
	 * 数据库名
	 */
	public static final String DB_NAME = "cool_weather";
	
	/*
	 * 数据库版本
	 */
	public static final int VERSION = 1;
	
	private SQLiteDatabase db;
	
	private static CoolWeatherDB coolWeatherDB;
	
	/*
	 * 构造方法私有化
	 */
	private CoolWeatherDB( Context context ){
		CoolWeatherOpenHelper helper = new CoolWeatherOpenHelper(context, DB_NAME, null, VERSION);
		db = helper.getWritableDatabase();
	}
	
	public synchronized static CoolWeatherDB getInstance(Context context){
		if( coolWeatherDB == null ) {
			coolWeatherDB = new CoolWeatherDB(context);
		}
		return coolWeatherDB;
	}
	
	/*
	 * 将Province实列存储到数据库
	 */
	public void saveProvince(Province province){
		if( province != null ) {
			ContentValues values = new ContentValues();
			values.put("province_name", province.getProvinceName());
			values.put("province_code", province.getProvinceCode());
			db.insert("Province", null, values);
		}
	}
	
	/*
	 * 从数据库读取全国所有省份信息
	 */
	public List<Province> loadProvinces() {
		List<Province> provinces = new ArrayList<Province>();
		Cursor cursor = db.query("Province", null, null, null, null, null, null);
		if( cursor.moveToFirst() ) {
			do {
				Province p = new Province();
				p.setId(cursor.getInt(cursor.getColumnIndex("id")));
				p.setProvinceName(cursor.getString(cursor.getColumnIndex("province_name")));
				p.setProvinceCode(cursor.getString(cursor.getColumnIndex("province_code")));
				provinces.add(p);
			} while (cursor.moveToNext());
		}
		return provinces;
	}
	
	/*
	 * 将City实列存储到数据库
	 */
	public void saveCity(City city) {
		if( city != null ) {
			ContentValues values = new ContentValues();
			values.put("city_name", city.getCityName());
			values.put("city_code", city.getCityCode());
			values.put("province_id", city.getProvinceId());
			db.insert("City", null, values);
		}	
	}
	
	/*
	 * 从数据库读取某一省份下所有市
	 */
	public List<City> loadCities( int provinceId ) {
		List<City> cities = new ArrayList<City>();
		Cursor cursor = db.query("City", null, "province_id = ?", new String[]{String.valueOf(provinceId)}, null, null, null);
		if( cursor.moveToFirst() ) {
			do {
				City city = new City();
				city.setId(cursor.getInt(cursor.getColumnIndex("id")));
				city.setCityName(cursor.getString(cursor.getColumnIndex("city_name")));
				city.setCityCode(cursor.getString(cursor.getColumnIndex("city_code")));
				city.setProvinceId(provinceId);
				cities.add(city);
			} while (cursor.moveToNext());
		}
		return cities;
	}
	
	/*
	 * 将County实列存储到数据库
	 */
	public void saveCounty(County county) {
		if( county != null ) {
			ContentValues values = new ContentValues();
			values.put("county_name", county.getCountyName());
			values.put("county_code", county.getCountyCode());
			values.put("city_id", county.getCityId());
			db.insert("County", null, values);
		}	
	}
	
	/*
	 * 从数据库读取某个市下所有的县
	 */
	public List<County> loadCounties( int cityId ) {
		List<County> counties = new ArrayList<County>();
		Cursor cursor = db.query("County", null, "city_id = ?", new String[]{String.valueOf(cityId)}, null, null, null);
		if( cursor.moveToFirst() ) {
			do {
				County county = new County();
				county.setId(cursor.getInt(cursor.getColumnIndex("id")));
				county.setCountyName(cursor.getString(cursor.getColumnIndex("county_name")));
				county.setCountyCode(cursor.getString(cursor.getColumnIndex("county_code")));
				county.setCityId(cityId);
				counties.add(county);
			} while (cursor.moveToNext());	
		}
		return counties;
	}
	
	
	
	

}
