package cz.dolejsi.petr.weather;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class DetailActivity extends Activity {

    DBHelperCities mydbCities;
    TextView name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        mydbCities = new DBHelperCities(this);
        name = (TextView) findViewById(R.id.editTextName);

        int id_To_Update = 0;

        Bundle extras = getIntent().getExtras();
        if(extras !=null)
        {
            //ziskam ID ktere se ma editovat/zobrazit/mazat poslane z main aktivity
            int value = extras.getInt("id");
            if (value >0)
            {
                //z database vytahnu zaznam pod hledanym ID a ulozim do id_To_Update
                Cursor rs = mydbCities.getData(value);
                id_To_Update = value;
                rs.moveToFirst();

                //vytahnu zaznam se jmenem
                String nam = rs.getString(rs.getColumnIndex("name"));

                if (!rs.isClosed())
                {
                    rs.close();
                }
                Button b = (Button)findViewById(R.id.buttonDelete);

                name.setText((CharSequence)nam);
                name.setEnabled(false);
                name.setFocusable(false);
                name.setClickable(false);
            }
        }
    }

    public void deleteButtonAction(View view)
    {
        Bundle extras = getIntent().getExtras();
        if(extras !=null)
        {
            int Value = extras.getInt("id");
            if(Value>0){

                mydbCities.deleteCity(Value);

            }
            MainActivity.newCities();

            MainActivity.doNotifyDataSetChangedOnce = true;

            onBackPressed();
        }
    }
}
