package cz.dolejsi.petr.weather;

import android.app.Service;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.location.LocationManager;
import android.os.Build;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.text.DateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    public static LocationManager locationManager;
    public static boolean isGPS = false;
    public static boolean isNetwork = false;

    private static DBHelperCities mydbCities;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mydbCities = new DBHelperCities(this);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        /*FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/

        locationManager = (LocationManager) getSystemService(Service.LOCATION_SERVICE);
        isGPS = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        isNetwork = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        Typeface weatherFont;

        TextView cityField;
        TextView updatedField;
        TextView detailsField;
        TextView currentTemperatureField;
        TextView weatherIcon;

        Handler handler;

        public PlaceholderFragment() {
            handler = new Handler();
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        private void updateWeatherData(final String city){
            new Thread(){
                public void run(){
                    final JSONObject json = OpenWeatherMap.getJSON(getActivity(), city, "weather");
                    if(json == null){
                        handler.post(new Runnable(){
                            public void run(){
                                Toast.makeText(getActivity(),"Offline předpověď",
                                        Toast.LENGTH_LONG).show();
                                if (mydbCities.IfCityExist(1)) {
                                    readAndSetDatas();
                                } else {
                                    Log.e("mesto","neexistuje");
                                }
                            }
                        });

                    } else {
                        handler.post(new Runnable(){
                            public void run(){
                                renderWeather(json);
                            }
                        });
                    }
                }
            }.start();
        }

        private void readAndSetDatas () {
            Cursor city = mydbCities.getData(1);
            city.moveToFirst();

            cityField.setText(city.getString(city.getColumnIndex("name")));
            currentTemperatureField.setText(city.getString(city.getColumnIndex("temperature")));
            detailsField.setText(
                    city.getString(city.getColumnIndex("weather")) +
                            "\n" + "Vlhkost: " + city.getString(city.getColumnIndex("humidity")) + "%" +
                            "\n" + "Tlak: " + city.getString(city.getColumnIndex("pressure")) + " hPa");
            updatedField.setText("Aktualizováno: " + city.getString(city.getColumnIndex("date")));
            setWeatherIcon(city.getInt(city.getColumnIndex("icon")),city.getLong(city.getColumnIndex("sunrise")),city.getLong(city.getColumnIndex("sunset")));

            city.close();
        }

        private void renderWeather(JSONObject json){
            try {
                try {
                    cityField.setText(json.getString("name").toUpperCase() +
                            ", " +
                            json.getJSONObject("sys").getString("country"));
                } catch(Exception e) {
                    Log.e("error", e.toString());
                    Log.e("country", "no country");
                }

                JSONObject details = json.getJSONArray("weather").getJSONObject(0);
                JSONObject main = json.getJSONObject("main");
                detailsField.setText(
                        details.getString("description").toUpperCase() +
                                "\n" + "Vlhkost: " + main.getString("humidity") + "%" +
                                "\n" + "Tlak: " + main.getString("pressure") + " hPa");

                currentTemperatureField.setText(
                        String.format("%.1f", main.getDouble("temp"))+ " ℃");

                DateFormat df = DateFormat.getDateTimeInstance();
                String updatedOn = df.format(new Date());
                updatedField.setText("Aktualizováno: " + updatedOn);

                setWeatherIcon(details.getInt("id"),
                        json.getJSONObject("sys").getLong("sunrise") * 1000,
                        json.getJSONObject("sys").getLong("sunset") * 1000);

                if (!(mydbCities.IfCityExist(1))) {
                    Log.e("city","neexistuje");
                    City city = new City();
                    city.id = 1;
                    city.lat = json.getJSONObject("coord").getString("lon");
                    city.lot = json.getJSONObject("coord").getString("lat");
                    city.type = 1;
                    city.temperature = String.format("%.1f", main.getDouble("temp"));
                    try {
                        city.name = json.getString("name").toUpperCase() + ", " + json.getJSONObject("sys").getString("country");
                    } catch(Exception e) {
                        city.name = "";
                    }
                    city.date = updatedOn;
                    city.icon = details.getInt("id");
                    city.weather = details.getString("description").toUpperCase();
                    city.humidity = main.getString("humidity");
                    city.pressure = main.getString("pressure");
                    city.sunrise = json.getJSONObject("sys").getLong("sunrise") * 1000;
                    city.sunset = json.getJSONObject("sys").getLong("sunset") * 1000;
                    if (mydbCities.insertCity(city)) {
                        Log.e("city","vloženo");
                    } else {
                        Log.e("city","nevloženo");
                    }
                } else {
                    Log.e("city","existuje");
                }

            } catch(Exception e){
                Log.e("error", e.toString());
                Log.e("SimpleWeather", "One or more fields not found in the JSON data");
                if (mydbCities.IfCityExist(1)) {
                    readAndSetDatas();
                }
            }
        }

        private void setWeatherIcon(int actualId, long sunrise, long sunset){
            int id = actualId / 100;
            String icon = "";
            Toolbar toolbar = ((Toolbar) this.getActivity().findViewById(R.id.toolbar));
            if(actualId == 800){
                long currentTime = new Date().getTime();
                if(currentTime>=sunrise && currentTime<sunset) {
                    icon = getActivity().getString(R.string.weather_sunny);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        toolbar.setBackgroundColor(getResources().getColor(R.color.colorSunny));
                        toolbar.setTitleTextColor(getResources().getColor(R.color.colorBlack));
                    }
                } else {
                    icon = getActivity().getString(R.string.weather_clear_night);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        toolbar.setBackgroundColor(getResources().getColor(R.color.colorClearNight));
                        toolbar.setTitleTextColor(getResources().getColor(R.color.colorWhite));
                    }
                }
            } else {
                switch(id) {
                    case 2:
                        icon = getActivity().getString(R.string.weather_thunder);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            toolbar.setBackgroundColor(getResources().getColor(R.color.colorThunder));
                            toolbar.setTitleTextColor(getResources().getColor(R.color.colorWhite));
                        }
                        break;
                    case 3:
                        icon = getActivity().getString(R.string.weather_drizzle);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            toolbar.setBackgroundColor(getResources().getColor(R.color.colorDrizzle));
                            toolbar.setTitleTextColor(getResources().getColor(R.color.colorWhite));
                        }
                        break;
                    case 7:
                        icon = getActivity().getString(R.string.weather_foggy);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            toolbar.setBackgroundColor(getResources().getColor(R.color.colorFoggy));
                            toolbar.setTitleTextColor(getResources().getColor(R.color.colorBlack));
                        }
                        break;
                    case 8 :
                        icon = getActivity().getString(R.string.weather_cloudy);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            toolbar.setBackgroundColor(getResources().getColor(R.color.colorSunny));
                            toolbar.setTitleTextColor(getResources().getColor(R.color.colorBlack));
                        }
                        break;
                    case 6 :
                        icon = getActivity().getString(R.string.weather_snowy);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            toolbar.setBackgroundColor(getResources().getColor(R.color.colorSnowy));
                            toolbar.setTitleTextColor(getResources().getColor(R.color.colorBlack));
                        }
                        break;
                    case 5 :
                        icon = getActivity().getString(R.string.weather_rainy);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            toolbar.setBackgroundColor(getResources().getColor(R.color.colorRainy));
                            toolbar.setTitleTextColor(getResources().getColor(R.color.colorBlack));
                        }
                        break;
                }
            }
            weatherIcon.setText(icon);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            TextView TypeText = (TextView) rootView.findViewById(R.id.typ);
            int cislo = getArguments().getInt(ARG_SECTION_NUMBER);
            String cisloString = getString(R.string.section_format,cislo);
            Toolbar toolbar = ((Toolbar) this.getActivity().findViewById(R.id.toolbar));
            if (cislo==1) {
                TypeText.setText("Aktuální poloha");
                weatherFont = Typeface.createFromAsset(getActivity().getAssets(), "fonts/weathericons-regular-webfont.ttf");
                updateWeatherData(new GetPosition(getActivity()).getCity());


                cityField = (TextView)rootView.findViewById(R.id.city_field);
                updatedField = (TextView)rootView.findViewById(R.id.updated_field);
                detailsField = (TextView)rootView.findViewById(R.id.details_field);
                currentTemperatureField = (TextView)rootView.findViewById(R.id.current_temperature_field);
                weatherIcon = (TextView)rootView.findViewById(R.id.weather_icon);

                weatherIcon.setTypeface(weatherFont);
                return rootView;
            } else{
                TypeText.setText(cisloString);
            }
            return rootView;
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {


        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            int count = mydbCities.getCount();
            if (count==0) {
                return 1;
            }
            return count;
        }
    }
}
