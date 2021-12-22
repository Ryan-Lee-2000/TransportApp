package com.example.transportapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.view.View;

import com.example.transportapp.databinding.ActivityMainBinding;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.*;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private String bus_stop = "";

    private MutableLiveData<String> grabBusStop = new MutableLiveData<>("");
    private LiveData<String> thisBusStop = grabBusStop;

    private NavController navController;
    private AppBarConfiguration mAppBarConfiguration;

    private TabLayout tabLayout;
    private TabLayout.Tab checkBus;
    private TabLayout.Tab busStops;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        resetFavStop();
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.fragment);
        navController = navHostFragment.getNavController();
        tabLayout = binding.tabbar;
        checkBus = tabLayout.newTab().setText(R.string.menu_bus_check);
        busStops = tabLayout.newTab().setText(R.string.menu_bus_stop);
        tabLayout.addTab(checkBus);
        tabLayout.addTab(busStops);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {

            @SuppressLint("RestrictedApi")
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                System.out.println("Hello, tab pressed " + tab.getText() + " " + getResources().getString(R.string.menu_bus_stop));
                System.out.println(navController.getCurrentDestination().getDisplayName().split("/")[1]);
                if(tab.getText().equals(getResources().getString(R.string.menu_bus_stop))){
                    //favBusStops
                    if(navController.getCurrentDestination().getDisplayName().split("/")[1].equals("favBusStops")){
                        System.out.println("You are currently in that fragment.");
                        return;
                    }
                    System.out.println("This is bus stop");
                    navController.navigate(R.id.action_show_favBusStop);
                    return;
                }
                if(tab.getText().equals(getResources().getString(R.string.menu_bus_check))){
                    System.out.println("This is check bus");
                    if(navController.getCurrentDestination().getDisplayName().split("/")[1].equals("checkBusStop")){
                        System.out.println("You are currently in that fragment.");
                        return;
                    }
                    navController.navigate(R.id.action_show_checkBusStop);
                    return;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });


        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(getString(R.string.preference_file_key), "67489,55031");
        editor.apply();
            //navController.navigate(R.id.action_show_checkBusStop);
//        binding.button.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Editable text = binding.busStopNumber.getText();
//                String busStop = text.toString();
//                if(busStop != null && busStop != ""){
//                    System.out.println("Hey, button pressed " + busStop);
//                    grabBusStop.postValue(busStop);
//                }
//            }
//        });


    }



    public void navigate(int id){
        navController.navigate(id);
    }

    public void setTab(int id){
        switch(id){
            case 1:
                tabLayout.selectTab(checkBus, true);
                break;
            case 2:
                tabLayout.selectTab(busStops, true);
                break;
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        resetFavStop();
    }

    public void resetFavStop(){
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(getString(R.string.chosen_fav_stop), null);
        editor.apply();
    }



}