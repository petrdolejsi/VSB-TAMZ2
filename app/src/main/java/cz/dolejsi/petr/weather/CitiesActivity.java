package cz.dolejsi.petr.weather;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import java.util.ArrayList;

public class CitiesActivity extends AppCompatActivity {

    private static final int PLACE_PICKER_REQUEST = 1;
    private TextView mName;
    private TextView mAddress;
    private TextView mAttributions;
    private Location location = new GetPosition().getLoc();
    private final LatLngBounds BOUNDS_MOUNTAIN_VIEW = new LatLngBounds(
            new LatLng((location.getLatitude()-0.01), (location.getLongitude()-0.01)), new LatLng((location.getLatitude()+0.01), (location.getLongitude()+0.01)));

    private static DBHelperCities mydbCities;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cities);

        mydbCities = new DBHelperCities(this);

        mydbCities.getAllCities();

        ArrayList<String> cities = mydbCities.getAllCitiesName();

        ArrayAdapter arrayAdapter=new ArrayAdapter(this,android.R.layout.simple_list_item_1, cities);
        ListView obj = (ListView)findViewById(R.id.CitiesList);
        obj.setAdapter(arrayAdapter);
        obj.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                //abych vedel jake id v poli mam hledat

                City searchedCity = MainActivity.cities.get(arg2+1);

                int id_To_Search = searchedCity.id;
                Log.d("Clicked item id", " "+ arg2);
                Bundle dataBundle = new Bundle();
                dataBundle.putInt("id", id_To_Search);
                Intent intent = new Intent(getApplicationContext(), DetailActivity.class);
                intent.putExtra("id",id_To_Search);
                startActivity(intent);
            }
        });


        Button pickerButton = (Button) findViewById(R.id.pickerButton);
        pickerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    PlacePicker.IntentBuilder intentBuilder =
                            new PlacePicker.IntentBuilder();
                    intentBuilder.setLatLngBounds(BOUNDS_MOUNTAIN_VIEW);
                    Intent intent = intentBuilder.build(CitiesActivity.this);
                    startActivityForResult(intent, PLACE_PICKER_REQUEST);

                } catch (GooglePlayServicesRepairableException
                        | GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode,
                                    int resultCode, Intent data) {

        if (requestCode == PLACE_PICKER_REQUEST
                && resultCode == Activity.RESULT_OK) {

            final Place place = PlacePicker.getPlace(this, data);
            final CharSequence name = place.getName();
            final CharSequence address = place.getAddress();

            String attributions = (String) place.getAttributions();
            if (attributions == null) {
                attributions = "";
            }

            Location location = new Location("asd");
            location.setLatitude(place.getLatLng().latitude);
            location.setLongitude(place.getLatLng().longitude);

            int countInDb = mydbCities.getCount();
            City newCity = new City();
            newCity.id=countInDb + 1;
            newCity.lat=String.valueOf(round(location.getLatitude(), 6));
            newCity.lot=String.valueOf(round(location.getLongitude(), 6));
            mydbCities.insertCity(newCity);

            MainActivity.doNotifyDataSetChangedOnce = true;
            MainActivity.newCities();

        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void update() {
        mydbCities.getAllCities();

        ArrayList<String> cities = mydbCities.getAllCitiesName();

        ArrayAdapter arrayAdapter=new ArrayAdapter(this,android.R.layout.simple_list_item_1, cities);
        ListView obj = (ListView)findViewById(R.id.CitiesList);
        obj.setAdapter(arrayAdapter);
    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }
}
