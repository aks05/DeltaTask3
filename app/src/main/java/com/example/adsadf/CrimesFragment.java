package com.example.adsadf;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.example.adsadf.Api.BASE_URL;

public class CrimesFragment extends Fragment
        implements ForceAdapter.ForceAdapterOnClickHandler {
    private ForceAdapter.ForceAdapterOnTouchHandler mTouchHandler;
    private String Month, Latitude, Longitude, Force;
    private List<Crime> crimes;
    private String[] crimeCategory;
    private ForceAdapter mForceAdapter;
    private EditText editTextSearch;

    public CrimesFragment() {
        //empty constructor required
    }

    CrimesFragment(ForceAdapter.ForceAdapterOnTouchHandler mTouchHandler) {
        this.mTouchHandler = mTouchHandler;
    }

    CrimesFragment(String Month, String Latitude, String Longitude,
                   ForceAdapter.ForceAdapterOnTouchHandler mTouchHandler) {
        this.Month = Month;
        this.Latitude = Latitude;
        this.Longitude = Longitude;
        this.mTouchHandler = mTouchHandler;
    }

    CrimesFragment(String Month, String Force,
                   ForceAdapter.ForceAdapterOnTouchHandler mTouchHandler) {
        this.Month = Month;
        this.Force = Force;
        this.mTouchHandler = mTouchHandler;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_crimes, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        RecyclerView mRecyclerView = getView().findViewById(R.id.fc_rv);
        LinearLayoutManager layoutManager
                = new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false);

        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setHasFixedSize(true);
        mForceAdapter = new ForceAdapter(this, mTouchHandler, getContext());
        mRecyclerView.setAdapter(mForceAdapter);
        editTextSearch = getView().findViewById(R.id.editTextSearch);

        if (this.getActivity().getClass().equals(MainActivity.class)) {
            refresh();
        }

        else {
            final Retrofit retrofit = new Retrofit.Builder().baseUrl(BASE_URL).
                    addConverterFactory(GsonConverterFactory.create()).build();
            Api api = retrofit.create(Api.class);
            Call<List<Crime>> call;
            if (Latitude == null) {
                call = api.getCrimesWithoutLocation("all-crime", Force, Month);
            } else {
                call = api.getCrimesAtLocation(Month, Latitude, Longitude);
            }
            call.enqueue(new Callback<List<Crime>>() {
                @Override
                public void onResponse(@NonNull Call<List<Crime>> call,
                                       @NonNull Response<List<Crime>> response) {
                    crimes = response.body();

                    if (crimes != null && crimes.size() != 0) {
                        crimeCategory = new String[crimes.size()];
                        Log.i("Ayush", "For Loop Started");
                        for (int i = 0; i < crimes.size(); i++) {
                            crimeCategory[i] = crimes.get(i).getCategory();
                        }
                        Log.i("Ayush", "For Loop completed");
                        Log.i("Ayush", "" + crimes.size());
                        mForceAdapter.setForceData(crimeCategory);
                    } else {
                        Toast.makeText(getActivity(), "No Crimes Found",
                                Toast.LENGTH_SHORT).show();
                        getActivity().finish();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<List<Crime>> call, @NonNull Throwable t) {
                    Toast.makeText(getActivity(),
                            "Request Failed\nCheck your Internet Connection",
                            Toast.LENGTH_SHORT).show();
                    editTextSearch.setEnabled(false);
                    getActivity().finish();
                }
            });
        }

        editTextSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                //after the change calling the method and passing the search input
                filter(editable.toString());
            }
        });
    }

    private void filter(String text) {
        //new array list that will hold the filtered data
        ArrayList<String> filterdNames = new ArrayList<>();

        //looping through existing elements
        if (crimeCategory != null) {
            for (String s : crimeCategory) {
                //if the existing elements contains the search input
                if (s.toLowerCase().contains(text.toLowerCase())) {
                    //adding the element to filtered list
                    filterdNames.add(s);
                }
            }
        }
        if (filterdNames.size() != 0) {
            String[] filter = new String[filterdNames.size()];
            for (int i = 0; i < filterdNames.size(); i++)
                filter[i] = filterdNames.get(i);
            //calling a method of the adapter class and passing the filtered list
            mForceAdapter.setForceData(filter);
        }
    }

    String[] getCrimeString(int position) {
        String[] crimeString = new String[10];
        Crime crime = crimes.get(position);
        crimeString[0] = crime.getCategory();
        crimeString[1] = crime.getMonth();
        crimeString[2] = crime.getLocation_type();
        if (crimeString[2] == null || crimeString[2].equals(""))
            crimeString[2] = "No Location Type Found";
        crimeString[3] = crime.getLocation_subtype();
        if (crimeString[3] == null || crimeString[3].equals(""))
            crimeString[3] = "No Location Subtype Found";
        if (crime.getLocation() != null) {
            crimeString[4] = crime.getLocation().getLatitude();
            crimeString[5] = crime.getLocation().getLongitude();
            crimeString[6] = crime.getLocation().getStreet().getName();
        } else {
            crimeString[4] = "No Latitude Found";
            crimeString[5] = "No Longitude Found";
            crimeString[6] = "No Street Found";
        }
        crimeString[7] = crime.getContext();
        if (crimeString[7] == null || crimeString[7].equals(""))
            crimeString[7] = "No Context Found";
        if (crimeString[8] != null) {
            crimeString[8] = crime.getOutcome_status().getCategory();
            crimeString[9] = crime.getOutcome_status().getDate();
        } else {
            crimeString[8] = "No Outcome Status Found";
            crimeString[9] = "No Last Date of Action Found";
        }
        return crimeString;
    }

    @Override
    public void onClick(int position) {
        String[] crimeString = getCrimeString(position);
        Intent intent = new Intent(getActivity(), SpecificCrimeActivity.class);
        intent.putExtra("crime", crimeString);
        startActivity(intent);
    }

    void refresh() {
        CrimeDatabase database = new CrimeDatabase(getContext());
        Cursor data = database.getData();
        if (data != null && data.getCount() > 0) {
            crimes = new ArrayList<>();
            while (data.moveToNext()) {
                Street street = new Street("null", data.getString(7));
                Location location = new Location(data.getString(5), street,
                        data.getString(6));
                OutcomeStatus outcomeStatus = new OutcomeStatus(data.getString(9),
                        data.getString(10));
                Crime crime = new Crime(data.getString(1),
                        data.getString(3), location, data.getString(8),
                        outcomeStatus, data.getString(4),
                        data.getString(2));
                crimes.add(crime);
            }
            crimeCategory = new String[crimes.size()];
            for (int i = 0; i < crimes.size(); i++) {
                crimeCategory[i] = crimes.get(i).getCategory();
            }
        } else {
            Toast.makeText(getActivity(),
                    "No Favourites Found\nAdd Crime using Swipe Gesture",
                    Toast.LENGTH_LONG).show();
            editTextSearch.setEnabled(false);
            crimeCategory = new String[1];
            crimeCategory[0] = "No Data";
            mForceAdapter.setHandler(new Handlers(), new Handlers());
        }
        mForceAdapter.setForceData(crimeCategory);
    }
    private class Handlers implements ForceAdapter.ForceAdapterOnTouchHandler,
            ForceAdapter.ForceAdapterOnClickHandler {
        @Override
        public void onTouch(int position) {
            //do nothing
        }

        @Override
        public void onClick(int position) {
            //do nothing
        }
    }
}

