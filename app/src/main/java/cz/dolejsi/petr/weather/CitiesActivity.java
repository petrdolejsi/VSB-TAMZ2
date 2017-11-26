package cz.dolejsi.petr.weather;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

public class CitiesActivity extends Activity {

    Button addCity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cities);

        addCity = (Button) findViewById(R.id.btnAddCity);

        addCity.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch (View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_UP: {
                        Intent intent = new Intent(v.getContext(), PickerActivity.class);
                        startActivity(intent);

                        break;
                    }
                }
                return true;
            }
        });
    }
}
