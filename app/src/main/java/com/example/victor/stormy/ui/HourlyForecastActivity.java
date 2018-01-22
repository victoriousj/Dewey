package com.example.victor.stormy.ui;

import android.content.Intent;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.ThemedSpinnerAdapter;

import com.example.victor.stormy.R;
import com.example.victor.stormy.adapters.HourAdapter;
import com.example.victor.stormy.weather.Hour;

import java.util.Arrays;

public class HourlyForecastActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hourly_forecast);
        Hour[] mHours;
        RecyclerView mRecyclerView;

        HelperMethods.changeStatusBarColor(this);
        mRecyclerView = findViewById(R.id.recyclerView);

        Intent intent = getIntent();
        Parcelable[] parcelabes = intent.getParcelableArrayExtra(MainActivity.HOURLY_FORECAST);
        mHours = Arrays.copyOf(parcelabes, parcelabes.length, Hour[].class);

        HourAdapter adapter = new HourAdapter(mHours, this);
        mRecyclerView.setAdapter(adapter);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(layoutManager);

        mRecyclerView.setHasFixedSize(true);
    }
}
