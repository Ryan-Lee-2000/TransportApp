package com.example.transportapp.fragment;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.transportapp.MainActivity;
import com.example.transportapp.R;
import com.example.transportapp.databinding.CheckBusStopFragmentBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class CheckBusStop extends Fragment {

    private CheckBusStopFragmentBinding binding;

    private MutableLiveData<String> grabBusStop = new MutableLiveData<>("");
    private MutableLiveData<String> displayBusStop = new MutableLiveData<>("");
    private boolean start = false;

    public static CheckBusStop newInstance() {
        return new CheckBusStop();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = CheckBusStopFragmentBinding.inflate(inflater, container, false);

        grabBusStop.observe(getViewLifecycleOwner(), busStop -> {
            System.out.println("Hello, button was indeed pressed");
            if(start){
                if(busStop != null && busStop != ""){
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try  {
                                //Your code goes here
                                System.out.println("Hello!");
                                getBusArrival(busStop);

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                }
            } else{
                start = true;
            }
        });
        displayBusStop.observe(getViewLifecycleOwner(), display -> {
            //binding.description.setText(display);
        });


        //startGetBusArrival.start();

        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        String stop = sharedPref.getString(getString(R.string.chosen_fav_stop), null);

        if(stop != null && stop != ""){
            binding.busStopNumber.setText(stop);
            grabBusStop.postValue(stop);

        }


        return binding.getRoot();
    }

    private void getBusArrival(String busStop){
        String sURL = "http://datamall2.mytransport.sg/ltaodataservice/BusArrivalv2?BusStopCode=" + busStop;


        try {
            URL url = new URL(sURL);
            HttpURLConnection myURLConnection = (HttpURLConnection)url.openConnection();
            myURLConnection.setRequestProperty("AccountKey", "H36hA0/7Rn2n2n3hZ3rKIQ==");
            myURLConnection.setRequestProperty("accept", "application/json");
            myURLConnection.setConnectTimeout(5000);
            myURLConnection.setReadTimeout(5000);

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(myURLConnection.getInputStream()));
            String inputLine;
            StringBuffer content = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();
            myURLConnection.disconnect();
            try {
                JSONObject jsonObject = new JSONObject(content.toString());
                JSONArray array = jsonObject.getJSONArray("Services");
                String display = "";
                ArrayList<BusTiming> busTimingArrayList = new ArrayList<>();
                for(int i = 0; i < array.length(); i++) {
                    BusTiming addBusTiming = new BusTiming();
                    addBusTiming.busNumber = array.getJSONObject(i).getString("ServiceNo");
                    addBusTiming.bus1time = estimatedTime(array.getJSONObject(i).getJSONObject("NextBus").getString("EstimatedArrival"));
                    addBusTiming.bus2time = estimatedTime(array.getJSONObject(i).getJSONObject("NextBus2").getString("EstimatedArrival"));
                    addBusTiming.bus3time = estimatedTime(array.getJSONObject(i).getJSONObject("NextBus3").getString("EstimatedArrival"));

                    addBusTiming.loadCapacity1 = array.getJSONObject(i).getJSONObject("NextBus").getString("Load");
                    addBusTiming.loadCapacity2 = array.getJSONObject(i).getJSONObject("NextBus2").getString("Load");
                    addBusTiming.loadCapacity3 = array.getJSONObject(i).getJSONObject("NextBus3").getString("Load");
                    addBusTiming.bustype1 = array.getJSONObject(i).getJSONObject("NextBus").getString("Type");
                    addBusTiming.bustype2 = array.getJSONObject(i).getJSONObject("NextBus2").getString("Type");
                    addBusTiming.bustype3 = array.getJSONObject(i).getJSONObject("NextBus3").getString("Type");
//                    display += "Bus: " + array.getJSONObject(i).getString("ServiceNo");
//                    display += "\n";
//                    display += "Arrival: " + estimatedTime(array.getJSONObject(i).getJSONObject("NextBus").getString("EstimatedArrival"));
//                    display += " " + estimatedTime(array.getJSONObject(i).getJSONObject("NextBus2").getString("EstimatedArrival"));
//                    display += " " + estimatedTime(array.getJSONObject(i).getJSONObject("NextBus3").getString("EstimatedArrival"));
//                    //System.out.println(array.getJSONObject(i).getJSONObject("NextBus3").getString("EstimatedArrival"));
//                    display += "\n";
//                    display += "\n";
                    busTimingArrayList.add(addBusTiming);
                }
                busTimingArrayList = sortList(busTimingArrayList);
                update(busTimingArrayList);
                //setText(binding.description, display);
                System.out.println(jsonObject.toString(2));
            }catch (JSONException err){
                System.out.println(err.toString());
            }


        } catch(MalformedURLException e){
            System.out.println("Hey there was an error, MalformedURLException");
            e.printStackTrace();
        } catch(IOException e){
            System.out.println("Hey there was an error, IOException");
            e.printStackTrace();
        }

    }

    private ArrayList<BusTiming> sortList(ArrayList<BusTiming> busTimings){
        int sortBusTimings[] = new int[busTimings.size()];
        ArrayList<BusTiming> newBusTiming = new ArrayList<>();
        for(int i = 0; i < busTimings.size(); i++){
            sortBusTimings[i] = Integer.parseInt(busTimings.get(i).busNumber);
        }
        for(int i = 0; i < sortBusTimings.length - 1; i++){
            int temp;
            if(sortBusTimings[i] > sortBusTimings[i+1]){
                temp = sortBusTimings[i];
                sortBusTimings[i] = sortBusTimings[i+1];
                sortBusTimings[i+1] = temp;
                i=-1;
            }
        }

        for (int bus:
             sortBusTimings) {
            BusTiming busToAdd = new BusTiming();
            for (BusTiming swapBus:
                 busTimings) {
                if(swapBus.busNumber.equals(Integer.toString(bus))){
                    busToAdd = swapBus;
                    break;
                }
            }
            newBusTiming.add(busToAdd);
        }


        return newBusTiming;
    }


    private String estimatedTime(String time){
        if(time.isEmpty() || time.equals("")){
            return "";
        }
        String getTime = time.substring(11, 19);
        String currentTime = Calendar.getInstance().getTime().toString().substring(11,19);
        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
        try {
            Date date1 = format.parse(getTime);
            Date date2 = format.parse(currentTime);
            long difference = date2.getTime() - date1.getTime();

            int seconds = (int) (difference / 1000 % 60);
            int mins = (int) ((difference / (1000 * 60)) % 60);

            System.out.println("hello, seconds " + seconds);
            System.out.println("Hello, mins " + mins);

            if(date2.compareTo(date1) == -1){
                if(Math.abs(seconds) < 10 && Math.abs(mins) < 10){
                    return "0" + Math.abs(mins) + ":0" + Math.abs(seconds);
                } else if (Math.abs(seconds) < 10){
                    return Math.abs(mins) + ":0" + Math.abs(seconds);
                } else if(Math.abs(mins) < 10){
                    return "0" + Math.abs(mins) + ":" + Math.abs(seconds);
                } else {
                    return Math.abs(mins) + ":" + Math.abs(seconds);
                }
            } else {
                return "LATE";
            }



        } catch(Exception e){
            e.printStackTrace();
            return "Failed to get time";
        }
    }

//    private void setText(TextView description, String display){
//        getActivity().runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                binding.description.setText(display);
//            }
//        });
//    }

    @SuppressLint("ResourceType")
    private void update(ArrayList<BusTiming> busTimings){
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                binding.listOfBusTimings.removeAllViews();
                LinearLayout listOfBusTimings = binding.listOfBusTimings;
                for (BusTiming busTiming: busTimings) {
                    View view = LayoutInflater.from(getContext()).inflate(R.layout.bus_stop_layout, null);
                    TextView busNumber = (TextView) view.findViewById(R.id.bus_number);
                    TextView firstBusTime = (TextView) view.findViewById(R.id.first_bus_time);
                    TextView firstBusType = (TextView) view.findViewById(R.id.first_bus_type);
                    ImageView firstBusSeats = (ImageView) view.findViewById(R.id.first_bus_seats);
                    TextView secondBusTime = (TextView) view.findViewById(R.id.second_bus_time);
                    TextView secondBusType = (TextView) view.findViewById(R.id.second_bus_type);
                    ImageView secondBusSeats = (ImageView) view.findViewById(R.id.second_bus_seats);
                    TextView thirdBusTime = (TextView) view.findViewById(R.id.third_bus_time);
                    TextView thirdBusType = (TextView) view.findViewById(R.id.third_bus_type);
                    ImageView thirdBusSeats = (ImageView) view.findViewById(R.id.third_bus_seats);

                    busNumber.setText(busTiming.busNumber);

                    firstBusTime.setText(busTiming.bus1time);
                    secondBusTime.setText(busTiming.bus2time);
                    thirdBusTime.setText(busTiming.bus3time);

                    for(int i = 0; i < 3; i++){
                        switch(i){
                            case 0:
                                if(busTiming.bustype1.equals("SD")){
                                    firstBusType.setText("SINGLE");
                                    break;
                                }
                                if(busTiming.bustype1.equals("DD")){
                                    firstBusType.setText("DOUBLE");
                                    break;
                                }
                                if(busTiming.bustype1.equals("BD")){
                                    firstBusType.setText("BENDY");
                                    break;
                                }
                                firstBusType.setText("");
                                break;
                            case 1:
                                if(busTiming.bustype2.equals("SD")){
                                    secondBusType.setText("SINGLE");
                                    break;
                                }
                                if(busTiming.bustype2.equals("DD")){
                                    secondBusType.setText("DOUBLE");
                                    break;
                                }
                                if(busTiming.bustype2.equals("BD")){
                                    secondBusType.setText("BENDY");
                                    break;
                                }
                                secondBusType.setText("");
                                break;
                            case 2:
                                if(busTiming.bustype3.equals("SD")){
                                    thirdBusType.setText("SINGLE");
                                    break;
                                }
                                if(busTiming.bustype3.equals("DD")){
                                    thirdBusType.setText("DOUBLE");
                                    break;
                                }
                                if(busTiming.bustype3.equals("BD")){
                                    thirdBusType.setText("BENDY");
                                    break;
                                }
                                thirdBusType.setText("");
                                break;
                        }
                    }
                    firstBusSeats.setVisibility(View.VISIBLE);
                    secondBusSeats.setVisibility(View.VISIBLE);
                    thirdBusSeats.setVisibility(View.VISIBLE);
                    for(int i = 0; i < 3; i++){
                        switch (i){
                            case 0:
                                if(busTiming.loadCapacity1.equals("SEA")){
                                    firstBusSeats.setImageDrawable(getResources().getDrawable(R.drawable.seat_available));
                                    break;
                                }
                                if(busTiming.loadCapacity1.equals("SDA")){
                                    firstBusSeats.setImageDrawable(getResources().getDrawable(R.drawable.standing_available));
                                    break;
                                }
                                if(busTiming.loadCapacity1.equals("LSD")){
                                    firstBusSeats.setImageDrawable(getResources().getDrawable(R.drawable.standing_limited));
                                    break;
                                }
                                firstBusSeats.setVisibility(View.INVISIBLE);
                                break;
                            case 1:
                                if(busTiming.loadCapacity2.equals("SEA")){
                                    secondBusSeats.setImageDrawable(getResources().getDrawable(R.drawable.seat_available));
                                    break;
                                }
                                if(busTiming.loadCapacity2.equals("SDA")){
                                    secondBusSeats.setImageDrawable(getResources().getDrawable(R.drawable.standing_available));
                                    break;
                                }
                                if(busTiming.loadCapacity2.equals("LSD")){
                                    secondBusSeats.setImageDrawable(getResources().getDrawable(R.drawable.standing_limited));
                                    break;
                                }
                                secondBusSeats.setVisibility(View.INVISIBLE);
                                break;
                            case 2:
                                if(busTiming.loadCapacity3.equals("SEA")){
                                    thirdBusSeats.setImageDrawable(getResources().getDrawable(R.drawable.seat_available));
                                    break;
                                }
                                if(busTiming.loadCapacity3.equals("SDA")){
                                    thirdBusSeats.setImageDrawable(getResources().getDrawable(R.drawable.standing_available));
                                    break;
                                }
                                if(busTiming.loadCapacity3.equals("LSD")){
                                    thirdBusSeats.setImageDrawable(getResources().getDrawable(R.drawable.standing_limited));
                                    break;
                                }
                                thirdBusSeats.setVisibility(View.INVISIBLE);
                                break;

                        }

                    }
                    listOfBusTimings.addView(view);
                }
            }
        });


    }


    private class BusTiming{
        String busNumber;
        String bus1time;
        String bus2time;
        String bus3time;
        String loadCapacity1;
        String loadCapacity2;
        String loadCapacity3;
        String bustype1;
        String bustype2;
        String bustype3;
    }

}