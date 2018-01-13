package com.example.victor.stormy;

import android.content.Context;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = MainActivity.class.getSimpleName();

    String apiKey = "9b0a1d40d2d860cd50c1419fe73287ee";
    double latitude = 37.8267;
    double longitude = -122.423;
    private String weatherString = "https://api.forecast.io/forecast/"
            + apiKey + "/" + latitude + "," + longitude;
    private CurrentWeather mCurrentWeather;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //set the status bar color to the same color as the background
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(Color.parseColor("#FFFC970B"));


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.v(TAG, "app started");

        if (isNetworkAvailable()) {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(weatherString)
                    .build();

            Call call = client.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e(TAG, "Exception caught:", e);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try {
                        String jsonDate = response.body().string();
                        Log.v(TAG, jsonDate);
                        if (response.isSuccessful()) {
                            mCurrentWeather = getCurrentDetails(jsonDate);
                        } else {
                            alertUserAboutError();
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "Exception caught: " + e);
                    }
                    catch ( JSONException e) {
                        Log.e(TAG, "Exception caught: " + e);
                    }
                }
            });
        } else {
//            Toast.makeText(this, R.string.network_unavailable_message, Toast.LENGTH_LONG)
//                    .show();
            AlertUserAboutConnectivityIssue();
        }
        Log.d(TAG, "Main UI Code is running");
    }

    private CurrentWeather getCurrentDetails(String jsonDate) throws JSONException{
        JSONObject jsonObject = new JSONObject(jsonDate);

        String timeZone = jsonObject.getString("timezone");

        Log.i(TAG, timeZone);

        JSONObject currently = jsonObject.getJSONObject("currently");

        CurrentWeather weather = new CurrentWeather();
        weather.setTimeZone(timeZone);
        weather.setHumidity(currently.getDouble("humidity"));
        weather.setTime(currently.getLong("time"));
        weather.setIcon(currently.getString("icon"));
        weather.setPrecipe(currently.getDouble("precipProbability"));
        weather.setSummary(currently.getString("summary"));
        weather.setTemperture(currently.getDouble("temperature"));

        Log.d(TAG, weather.getFormattedTime());

        return weather;
    }

    private void AlertUserAboutConnectivityIssue() {
        NetworkUnavailableDialogFragment dialogFragment = new NetworkUnavailableDialogFragment();
        dialogFragment.show(getFragmentManager(), "");
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


    private void alertUserAboutError() {
        AlertDialogFragment dialogFragment = new AlertDialogFragment();
        dialogFragment.show(getFragmentManager(), "");
    }
}

