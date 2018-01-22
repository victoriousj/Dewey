package com.example.victor.stormy.ui;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.victor.stormy.R;
import com.example.victor.stormy.adapters.DayAdapter;
import com.example.victor.stormy.ui.HelperMethods;
import com.example.victor.stormy.ui.MainActivity;
import com.example.victor.stormy.weather.Day;

import org.w3c.dom.Text;

import java.lang.reflect.Array;
import java.util.Arrays;

public class DailyForecastActivity extends Activity {

    private Day[] mDays;
    private ListView mListView;
    private TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_forecast);
        mListView = findViewById(android.R.id.list);
        mTextView = findViewById(android.R.id.empty);

        // Set the status bar color
        HelperMethods.changeStatusBarColor(this);

        Intent intent = getIntent();
        String city = intent.getStringExtra(MainActivity.LOCATION);
        TextView mCity = findViewById(R.id.locationLabel);
        mCity.setText(city);

        Parcelable[] parcelables = intent.getParcelableArrayExtra(MainActivity.DAILY_FORECAST);
        mDays = Arrays.copyOf(parcelables, parcelables.length, Day[].class);
        DayAdapter dayAdapter = new DayAdapter(this, mDays);
        mListView.setAdapter(dayAdapter);
        mListView.setEmptyView(mTextView);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String dayOfTheWeek = mDays[position].getDayOfTheWeek();
                String conditions = mDays[position].getSummary();
                String highTemp = mDays[position].getTemperatureMax() + "";
                String message = String.format("On %s the high will be %s and it will be %s",
                        dayOfTheWeek,
                        highTemp,
                        conditions);

                Toast.makeText(DailyForecastActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }
}
