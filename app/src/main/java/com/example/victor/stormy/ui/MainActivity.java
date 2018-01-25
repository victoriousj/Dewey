package com.example.victor.stormy.ui;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.victor.stormy.R;
import com.example.victor.stormy.weather.Current;
import com.example.victor.stormy.weather.Day;
import com.example.victor.stormy.weather.Forecast;
import com.example.victor.stormy.weather.Hour;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = MainActivity.class.getSimpleName();
    public static final String DAILY_FORECAST = "DAILY_FORECAST";
    public static final String HOURLY_FORECAST = "HOURLY FORECAST";
    public static final String LOCATION = "LOCATION";
    private static final int PERMISSIONS_REQUEST_CODE = 11;

    //region Member Variables
    final String apiKey = "9b0a1d40d2d860cd50c1419fe73287ee";
    double latitude;
    double longitude;
    String city;
    private Forecast mForecast;
    private final LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
//            requestUserLocation();
//            if (latitude != 0.0 && longitude != 0.0) {
//                getForecast(latitude, longitude);
//            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };
    //endregion

    //region Views
    public TextView mTimeLabel;
    public TextView mPrecipValue;
    public TextView mSummaryLabel;
    public TextView mLocationLabel;
    public TextView mHumidityValue;
    public TextView mTemperatureLabel;
    public ProgressBar mProgressBar;
    public ImageView mIconImageView;
    public ImageView mRefreshImageView;
    public Button mHourlyButton;
    public Button mDailyButton;
    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //region Properties
        mTimeLabel = findViewById(R.id.timeLabel);
        mLocationLabel = findViewById(R.id.locationLabel);
        mTemperatureLabel = findViewById(R.id.temperatureLabel);
        mHumidityValue = findViewById(R.id.humidityValue);
        mPrecipValue = findViewById(R.id.precipValue);
        mSummaryLabel = findViewById(R.id.summaryLabel);
        mIconImageView = findViewById(R.id.iconImageView);
        mRefreshImageView = findViewById(R.id.refreshImageView);
        mProgressBar = findViewById(R.id.progressBar);
        mHourlyButton = findViewById(R.id.hourlyButton);
        mDailyButton = findViewById(R.id.dailyButton);
        //endregion

        mProgressBar.setVisibility(View.INVISIBLE);

        mRefreshImageView.setOnClickListener((View v) -> getForecast(latitude, longitude));
        mHourlyButton.setOnClickListener((View v) -> startHourlyActivity(v));
        mDailyButton.setOnClickListener((View v) -> startDailyActivity(v));

        handleLocationPermission();

        getForecast(latitude, longitude);
    }

    private void requestUserLocation() {
        LocationManager mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        try {
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1, 1, mLocationListener);
            Location loc = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (loc != null) {
                latitude = loc.getLatitude();
                longitude = loc.getLongitude();
            }
        } catch (SecurityException | NullPointerException e) {
            Log.e(TAG, "Exception: ", e);
        }
    }

    private String getCityName() {
        if (latitude == 0.0 || longitude == 0.0) return null;

        String location = "";
        Geocoder geo = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addressList = geo.getFromLocation(latitude, longitude, 1);
            if (addressList.size() > 0) {
                Address address = addressList.get(0);
                String city = address.getLocality();
                String state = getUSStateCode(address);

                if (state == null) { state = address.getAdminArea(); }

                if (city != null && state != null) { location = String.format("%s, %s", city, state); }
                else if (city != null) { location = city; }
                else { location = address.toString(); }

            } else {
                String truncLat = String.format(Locale.getDefault(), "%.2f", latitude);
                String truncLon = String.format(Locale.getDefault(), "%.2f", longitude);
                location = String.format("Lat: %s, Lon: %s", truncLat, truncLon);
            }
        } catch (IOException e) {
            Log.e(TAG, "IOException", e);
        }
        return location;
    }

    private String getUSStateCode(Address USAddress){
        String fullAddress = "";
        for(int j = 0; j <= USAddress.getMaxAddressLineIndex(); j++)
            if (USAddress.getAddressLine(j) != null)
                fullAddress = fullAddress + " " + USAddress.getAddressLine(j);

        String stateCode = null;
        Pattern pattern = Pattern.compile(" [A-Z]{2} ");
        String helper = fullAddress.toUpperCase().substring(0, fullAddress.toUpperCase().indexOf("USA"));
        Matcher matcher = pattern.matcher(helper);
        while (matcher.find())
            stateCode = matcher.group().trim();

        return stateCode;
    }

    private void getForecast(double latitude, double longitude) {

        if (latitude != 0.0 && longitude != 0.0) {
            String weatherString = "https://api.forecast.io/forecast/" + apiKey + "/" + latitude + "," + longitude;

            // Set the color of the spinner loader to white
            mProgressBar.getIndeterminateDrawable().setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);


            if (isNetworkAvailable()) {
                toggleRefresh();
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder()
                        .url(weatherString)
                        .build();

                Call call = client.newCall(request);
                call.enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        runOnUiThread(() -> toggleRefresh());
                        alertUserAboutError();
                        Log.e(TAG, "Exception caught:", e);
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        runOnUiThread(() -> toggleRefresh());
                        try {
                            String jsonDate = response.body().string();
                            Log.v(TAG, jsonDate);
                            if (response.isSuccessful()) {
                                mForecast = parseForecastDetails(jsonDate);
                                runOnUiThread(() -> updateDisplay());
                            } else {
                                alertUserAboutError();
                            }
                        } catch (IOException | JSONException e) {
                            Log.e(TAG, "Exception caught: " + e);
                        }
                    }

                });
            } else {
                alertUserAboutConnectivityIssue();
            }
        }
    }

    private void handleLocationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && latitude == 0.0 && longitude == 0.0) {
                if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle("Location Permission")
                            .setMessage("Hi there! We can't tell you what the weather is without knowing your location, could you please grant it?")
                            .setPositiveButton("Yep", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_CODE);
                                }
                            })
                            .setNegativeButton("No thanks", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    Toast.makeText(MainActivity.this, ":(", Toast.LENGTH_SHORT).show();
                                    mSummaryLabel.setText(R.string.location_permission_denied);
                                    mRefreshImageView.setVisibility(View.INVISIBLE);
                                }
                            }).show();
                } else {
                    requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_CODE);
                }
            } else {
                requestUserLocation();
            }
        }
    }

    private void toggleRefresh() {
        if (mProgressBar.getVisibility() == View.INVISIBLE) {
            mProgressBar.setVisibility(View.VISIBLE);
            mRefreshImageView.setVisibility(View.INVISIBLE);
        } else {
            mProgressBar.setVisibility(View.INVISIBLE);
            mRefreshImageView.setVisibility(View.VISIBLE);
        }
    }

    private void updateDisplay() {
        Current current = mForecast.getCurrent();

        mTemperatureLabel.setText(String.format(Locale.getDefault(),
                "%d", Math.round(current.getTemperture())));

        mTimeLabel.setText(String.format(Locale.getDefault(),
                "At %s it will be", current.getFormattedTime()));

        mHumidityValue.setText((current.getHumidity() + ""));

        mPrecipValue.setText(String.format(Locale.getDefault(),
                "%1$,.2f%%", current.getPrecipe()));

        mSummaryLabel.setText(current.getSummary());

        mLocationLabel.setText(current.getCity());

        Drawable drawable = getResources().getDrawable(current.getIconId());
        mIconImageView.setImageDrawable(drawable);

    }

    private Forecast parseForecastDetails(String jsonData) throws JSONException {
        Forecast forecast = new Forecast();

        forecast.setCurrent(getCurrentDetails(jsonData));

        forecast.setHourlyForecast(getHourlyForecast(jsonData));

        forecast.setDayForecast(getDailyForecast(jsonData));

        return forecast;
    }

    private Day[] getDailyForecast(String jsonData) throws JSONException{
        JSONObject jsonObject = new JSONObject(jsonData);
        String timeZone = jsonObject.getString("timezone");
        JSONObject daily = jsonObject.getJSONObject("daily");
        JSONArray data = daily.getJSONArray("data");

        int dataLength = data.length();
        Day[] days = new Day[dataLength];

        for (int i = 0; i < dataLength; i++) {
            JSONObject jsonDay = data.getJSONObject(i);

            Day day = new Day();
            day.setSummary(jsonDay.getString("summary"));
            day.setTemperatureMax(jsonDay.getDouble("temperatureMax"));
            day.setIcon(jsonDay.getString("icon"));
            day.setTimeZone(timeZone);
            day.setTime(jsonDay.getLong("time"));
            if (city != null) {
                day.setCity(city);
            } else {
                day.setCity("In Your  Area");
            }

            days[i] = day;
        }

        return days;
    }

    private Hour[] getHourlyForecast(String jsonData) throws JSONException{
        JSONObject jsonObject = new JSONObject(jsonData);
        String timeZone = jsonObject.getString("timezone");
        JSONObject hourly = jsonObject.getJSONObject("hourly");
        JSONArray data = hourly.getJSONArray("data");

        int dataLength = data.length();
        Hour[] hours = new Hour[dataLength];

        for (int i = 0; i < dataLength; i++) {
            JSONObject jsonHour = data.getJSONObject(i);

            Hour hour = new Hour();
            hour.setSummary(jsonHour.getString("summary"));
            hour.setTemperature(jsonHour.getDouble("temperature"));
            hour.setIcon(jsonHour.getString("icon"));
            hour.setTime(jsonHour.getLong("time"));
            hour.setTimeZone(timeZone);

            hours[i] = hour;
        }

        return hours;
    }

    private Current getCurrentDetails(String jsonDate) throws JSONException{
        JSONObject jsonObject = new JSONObject(jsonDate);
        JSONObject currently = jsonObject.getJSONObject("currently");

        Current weather = new Current();
        String timeZone = jsonObject.getString("timezone");
        weather.setTimeZone(timeZone);
        weather.setHumidity(currently.getDouble("humidity"));
        weather.setTime(currently.getLong("time"));
        weather.setIcon(currently.getString("icon"));
        weather.setPrecipe(currently.getDouble("precipProbability"));
        weather.setSummary(currently.getString("summary"));
        weather.setTemperture(currently.getDouble("temperature"));
        weather.setCity(getCityName());
        if (weather.getCity() != null) {
            city = weather.getCity();
        }

        Log.d(TAG, weather.getFormattedTime());
        return weather;
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager manager = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();

        boolean isAvailable = false;
        if (networkInfo != null && networkInfo.isConnected()) {
            isAvailable = true;
        }
        return  isAvailable;
    }

    private void alertUserAboutConnectivityIssue() {
        NetworkUnavailableDialogFragment dialogFragment = new NetworkUnavailableDialogFragment();
        dialogFragment.show(getFragmentManager(), "");
    }

    private void alertUserAboutError() {
        AlertDialogFragment dialogFragment = new AlertDialogFragment();
        dialogFragment.show(getFragmentManager(), "");
    }

    public void startDailyActivity(View view) {
        if (latitude != 0.0 && longitude != 0.0) {
            Intent intent = new Intent(this, DailyForecastActivity.class);
            intent.putExtra(DAILY_FORECAST, mForecast.getDayForecast());
            intent.putExtra(LOCATION, city);
            startActivity(intent);
        } else {
            Toast.makeText(this, "Sorry, we do have any weather data to show you :(", Toast.LENGTH_LONG).show();
        }
    }

    private void startHourlyActivity(View v) {
        if (latitude != 0.0 && longitude != 0.0) {
            Intent intent = new Intent(this, HourlyForecastActivity.class);
            intent.putExtra(HOURLY_FORECAST, mForecast.getHourlyForecast());
            startActivity(intent);
        } else {
            Toast.makeText(this, "Sorry, we do have any weather data to show you :(", Toast.LENGTH_LONG).show();
        }
    }
}

