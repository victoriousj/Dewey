package com.example.victor.stormy.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.victor.stormy.R;
import com.example.victor.stormy.adapters.DayAdapter;
import com.example.victor.stormy.weather.Day;

import java.util.Arrays;

public class DailyForecastActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_forecast);

        ListView mListView = findViewById(android.R.id.list);
        TextView mTextView = findViewById(android.R.id.empty);

        Intent intent = getIntent();
        String city = intent.getStringExtra(MainActivity.LOCATION);
        TextView mCity = findViewById(R.id.locationLabel);
        mCity.setText(city);

        Parcelable[] parcelables = intent.getParcelableArrayExtra(MainActivity.DAILY_FORECAST);
        Day[] mDays = Arrays.copyOf(parcelables, parcelables.length, Day[].class);
        DayAdapter dayAdapter = new DayAdapter(this, mDays);
        mListView.setAdapter(dayAdapter);
        mListView.setEmptyView(mTextView);

        mListView.setOnItemClickListener((parent, view, position, id) -> {
            String dayOfTheWeek = mDays[position].getDayOfTheWeek();
            String conditions = mDays[position].getSummary();
            String highTemp = mDays[position].getTemperatureMax() + "";
            String message = String.format("On %s the high will be %s and it will be %s",
                    dayOfTheWeek,
                    highTemp,
                    conditions);

            Toast.makeText(DailyForecastActivity.this, message, Toast.LENGTH_LONG).show();
        });
    }
}
