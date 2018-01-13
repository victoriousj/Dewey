package com.example.victor.stormy;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = MainActivity.class.getSimpleName();

    private CurrentWeather mCurrentWeather;

//    @BindView(R.id.timeLabel) TextView mTimeLabel;
//    @BindView(R.id.temperatureLabel) TextView mTemperatureLabel;
//    @BindView(R.id.humidityValue) TextView mHumidityValue;
//    @BindView(R.id.precipValue) TextView mPrecipValue;
//    @BindView(R.id.summaryLabel) TextView mSummaryLabel;
//    @BindView(R.id.iconImageView)

    public TextView mTimeLabel;
    public TextView mTemperatureLabel;
    public TextView mHumidityValue;
    public TextView mPrecipValue;
    public TextView mSummaryLabel;
    public ImageView mIconImageView;
    public ImageView mRefreshImageView;
    public ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTimeLabel = (TextView) findViewById(R.id.timeLabel);
        mTemperatureLabel = findViewById(R.id.temperatureLabel);
        mHumidityValue = findViewById(R.id.humidityValue);
        mPrecipValue = findViewById(R.id.precipValue);
        mSummaryLabel = findViewById(R.id.summaryLabel);
        mIconImageView = findViewById(R.id.iconImageView);
        mRefreshImageView = findViewById(R.id.refreshImageView);
        mProgressBar = findViewById(R.id.progressBar);

        mProgressBar.setVisibility(View.INVISIBLE);

        ButterKnife.bind(this);

        mRefreshImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getForecast();
            }
        });

        getForecast();
    }

    private void getForecast() {
        String apiKey = "9b0a1d40d2d860cd50c1419fe73287ee";
        double latitude = 44.963323;
        double longitude = -93.268284;
        String weatherString = "https://api.forecast.io/forecast/"+apiKey+"/"+latitude+","+longitude;

        mProgressBar.getIndeterminateDrawable().setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);

        //set the status bar color to the same color as the background
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(Color.parseColor("#FFFC970B"));

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
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            toggleRefresh();
                        }
                    });
                    alertUserAboutError();
                    Log.e(TAG, "Exception caught:", e);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            toggleRefresh();
                        }
                    });
                    try {
                        String jsonDate = response.body().string();
                        Log.v(TAG, jsonDate);
                        if (response.isSuccessful()) {
                            mCurrentWeather = getCurrentDetails(jsonDate);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {updateDisplay();}
                            });
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
        mTemperatureLabel.setText(Math.round(mCurrentWeather.getTemperture())+"");
        mTimeLabel.setText(("At " + mCurrentWeather.getFormattedTime() + " it will be"));
        mHumidityValue.setText((mCurrentWeather.getHumidity() + ""));
        mPrecipValue.setText(mCurrentWeather.getPrecipe() + "%");
        mSummaryLabel.setText(mCurrentWeather.getSummary());


        Drawable drawable = getResources().getDrawable(mCurrentWeather.getIconId());
        mIconImageView.setImageDrawable(drawable);

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

