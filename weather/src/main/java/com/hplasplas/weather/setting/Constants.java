/*
 * Copyright © 2018. Dmitry Starkin Contacts: t0506803080@gmail.com
 *
 * This file is part of weather
 *
 *     weather is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *    weather is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with weather  If not, see <http://www.gnu.org/licenses/>.
 */
package com.hplasplas.weather.setting;

import com.hplasplas.weather.BuildConfig;

/**
 * Created by StarkinDG on 06.04.2017.
 */

public final class Constants {
    
    public static final boolean DEBUG = BuildConfig.DEBUG;
    
    public static final long MIL_PER_SEC = 1000;
    
    //wind directions determine
    public static final double WIND_DIRECTION_DIVIDER = 22.5;
    public static final String WIND_DIRECTION_PREFIX = "d_";
    
    //time stamps
    public static final String WEATHER_TIME_STAMP_PATTERN = "E, d MMM y, HH:mm";
    public static final String SUN_TIME_STAMP_PATTERN = "HH:mm";
    public static final String REFRESHING_TIME_STAMP_PATTERN = "mm 'min' ss 'sec'";
    public static final String WIDGET_TIME_STAMP_PATTERN = "E, d MMM, HH:mm";
    public static final String FORECAST_TIME_STAMP_PATTERN = "HH:mm";
    public static final String FORECAST_DATE_STAMP_PATTERN = "dd.MM.yy";
    
    //request values
    public static final long MIN_REQUEST_INTERVAL = 600000;
    
    //preferences values
    public static final String PREFERENCES_FILE = "myPref";
    public static final String PREF_FOR_CURRENT_CITY_ID = "currentCity";
    public static final String PREF_FOR_CURRENT_WEATHER_JSON_DATA = "currentWeather";
    public static final String PREF_FOR_FIFE_DAYS_FORECAST_JSON_DATA = "fifeDaysWeather";
    public static final String LAST_REQUEST_TIME = "lastRequestTime";
    
    //default request values
    public static final int DEFAULT_CITY_ID = 705812;
    
    //dialog values
    public static final String MESSAGE_DIALOG_TAG = "messageDialog";
    
    //Api setting
    public static final String API_KEY = "66f1e1075104ffa9d6e1a03796632b9e";
    public static final String BASE_URL = "http://api.openweathermap.org/data/2.5/";
    public static final String CURRENT_WEATHER_URL = "weather";
    public static final String FIFE_DAY_WEATHER_URL = "forecast";
    public static final String CITY_ID_PARAMETER = "id";
    public static final String API_KEY_PARAMETER = "APPID";
    public static final String UNITS_PARAMETER = "units";
    public static final String UNITS_PARAMETER_VALUE = "metric";
    public static final String ICON_DOWNLOAD_URL = "http://openweathermap.org/img/w/";
    public static final String ICON_FILE_SUFFIX = ".png";
    
    //other
    public static final String WEATHER_DRAWABLE_PREFIX = "w_";
    public static final int REFRESH_INDICATOR_START_OFFSET = 0;
    public static final int REFRESH_INDICATOR_END_OFFSET = 10;
    public static final int NUMBER_THREAD_FOR_QUERY = 1;
    public static final int SNACK_BAR_MESSAGE_DURATION = 5000;
    public static final String UPDATE_ALL_WIDGETS = "update_all_widgets";
}

