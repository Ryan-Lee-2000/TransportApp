package com.example.transportapp.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.transportapp.MainActivity;
import com.example.transportapp.R;
import com.example.transportapp.databinding.FavBusStopsBinding;

public class FavBusStop extends Fragment {
    private FavBusStopsBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FavBusStopsBinding.inflate(inflater, container, false);
        LinearLayout list = binding.listOfBusStops;

//        View view = LayoutInflater.from(getContext()).inflate(R.layout.bus_stop_list_item, null);
//        TextView text = (TextView) view.findViewById(R.id.bus_text);
//        ImageButton imageButton = (ImageButton) view.findViewById(R.id.press_bus_stop);

        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        String code = sharedPref.getString(getString(R.string.preference_file_key), null);
        if(code != null){
            String[] favStops = code.split(",");

            for(int i = 0; i < favStops.length; i++){
                String stop = favStops[i];
                View view = LayoutInflater.from(getContext()).inflate(R.layout.bus_stop_list_item, null);
                TextView text = (TextView) view.findViewById(R.id.bus_text);
                ImageButton imageButton = (ImageButton) view.findViewById(R.id.press_bus_stop);
                text.setText("Bus Stop: " + stop);
                imageButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        System.out.println("Hello, pressing button " + stop);
                        setFavStop(stop);
                        ((MainActivity) getActivity()).setTab(1);
                        ((MainActivity) getActivity()).navigate(R.id.action_show_checkBusStop);
                    }
                });
                view.setPadding(0,0,0,20);
                list.addView(view);
                System.out.println("Hello, number of children " + list.getChildCount());
            }
        }

        ((MainActivity) getActivity()).resetFavStop();


//        for(int i = 0; i < 20; i++){
//            View view = LayoutInflater.from(getContext()).inflate(R.layout.bus_stop_list_item, null);
//            TextView text = (TextView) view.findViewById(R.id.bus_text);
//            ImageButton imageButton = (ImageButton) view.findViewById(R.id.press_bus_stop);
//            text.setText("Bus Stop: " + i);
//            imageButton.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    System.out.println("Hello, pressing button ");
//                }
//            });
//            view.setPadding(0,0,0,20);
//            list.addView(view);
//            System.out.println("Hello, number of children " + list.getChildCount());
//        }


        return binding.getRoot();
    }

    private void setFavStop(String busStop){
        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(getString(R.string.chosen_fav_stop), busStop);
        editor.apply();
    }

}
