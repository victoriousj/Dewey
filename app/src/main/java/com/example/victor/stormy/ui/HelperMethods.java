package com.example.victor.stormy.ui;

import android.app.Activity;
import android.view.Window;
import android.view.WindowManager;

import com.example.victor.stormy.R;

public class HelperMethods {
    public static void changeStatusBarColor(Activity activity) {
        //set the status bar color to the same color as the background
        Window window = activity.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

        int accentColor = activity.getResources().getColor(R.color.colorAccent);

        window.setStatusBarColor(accentColor);
    }
}
