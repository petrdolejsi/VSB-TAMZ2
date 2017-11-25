package cz.dolejsi.petr.weather;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.sql.Date;
import java.util.ArrayList;

/**
 * Created by Petrd on 23.11.17.
 */

public class DBHelperCities extends SQLiteOpenHelper {

        public static final String DATABASE_NAME = "Cities.db";
        public static final String CITIES_TABLE_NAME = "cities";

        public static ArrayList<City> arrayList = new ArrayList<City>();

        public DBHelperCities(Context context)
        {
            super(context, DATABASE_NAME, null, 1);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            //db.execSQL("create table contacts " + "(id integer primary key, name text)");
            db.execSQL("CREATE TABLE " + CITIES_TABLE_NAME + " (id INTEGER PRIMARY KEY, type INTEGER, lat TEXT, lot TEXT, temperature TEXT, name TEXT, date TEXT, icon INTEGER, weather TEXT, humidity TEXT, pressure TEXT, sunrise LONG, sunset LONG)");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + CITIES_TABLE_NAME );
            onCreate(db);
        }

        public boolean insertCity(City city)
        {
            try {
                SQLiteDatabase db = this.getWritableDatabase();
                ContentValues contentValues = new ContentValues();
                contentValues.put("id", city.id);
                contentValues.put("lat", city.lat);
                contentValues.put("lot", city.lot);
                contentValues.put("type", city.type);
                contentValues.put("temperature", city.temperature);
                contentValues.put("name", city.name);
                contentValues.put("date", city.date);
                contentValues.put("icon", city.icon);
                contentValues.put("weather", city.weather);
                contentValues.put("humidity", city.humidity);
                contentValues.put("pressure", city.pressure);
                contentValues.put("sunrise", city.sunrise);
                contentValues.put("sunset", city.sunset);
                db.insertOrThrow(CITIES_TABLE_NAME, null, contentValues);
            } catch (Exception e) {
                Log.e("inserting", e.toString());
                return false;
            }

            return true;
        }

        //Cursor representuje vracena data
        public Cursor getData(int id){
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor res =  db.rawQuery("select * from " + CITIES_TABLE_NAME + " where id=" + id + "", null);
            //Cursor res =  db.rawQuery( "select * from contacts LIMIT 1 OFFSET "+id+"", null );
            return res;
        }

        public boolean updateCity (City city)
        {
            SQLiteDatabase db = this.getReadableDatabase();
            ContentValues contentValues = new ContentValues();
            contentValues.put("lat", city.lat);
            contentValues.put("lot", city.lot);
            contentValues.put("type", city.type);
            contentValues.put("temperature", city.temperature);
            contentValues.put("name", city.name);
            contentValues.put("date", city.date);
            contentValues.put("icon", city.icon);
            contentValues.put("weather", city.weather);
            contentValues.put("humidity", city.humidity);
            contentValues.put("pressure", city.pressure);
            contentValues.put("sunrise", city.sunrise);
            contentValues.put("sunset", city.sunset);
            db.update(CITIES_TABLE_NAME,contentValues," id="+(city.id),null);
            return true;
        }

        public ArrayList<City> getAllContacts()
        {
            ArrayList<City> cities = new ArrayList<>();
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor res =  db.rawQuery( "select * from " + CITIES_TABLE_NAME, null );
            res.moveToFirst();

            while(res.isAfterLast() == false){
                City city = new City();
                city.id = res.getInt(res.getColumnIndex("id"));
                city.lat = res.getString(res.getColumnIndex("lat"));
                city.lot = res.getString(res.getColumnIndex("lot"));
                city.type = res.getInt(res.getColumnIndex("type"));
                city.temperature = res.getString(res.getColumnIndex("temperature"));
                city.name = res.getString(res.getColumnIndex("name"));
                city.date = res.getString(res.getColumnIndex("date"));
                city.icon = res.getInt(res.getColumnIndex("icon"));
                city.weather = res.getString(res.getColumnIndex("weather"));
                city.humidity = res.getString(res.getColumnIndex("humidity"));
                city.pressure = res.getString(res.getColumnIndex("pressure"));
                city.sunrise = res.getLong(res.getColumnIndex("sunrise"));
                city.sunset = res.getLong(res.getColumnIndex("sunset"));

                cities.add(city);
                res.moveToNext();
            }
            return cities;
        }

        public boolean IfCityExist(int id) {
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor cursor = db.rawQuery("select count(*) from " + CITIES_TABLE_NAME + " where id = '" + id + "'", null);
            cursor.moveToFirst();
            if(cursor.getInt(0) <= 0){
                cursor.close();
                return false;
            }
            cursor.close();
            return true;
        }

        public void removeAll()
        {
            SQLiteDatabase db = this.getWritableDatabase();
            db.delete(CITIES_TABLE_NAME, "1", null);
        }

        public int getCount() {
            SQLiteDatabase db = this.getReadableDatabase();
            int cnt  = (int) DatabaseUtils.queryNumEntries(db,CITIES_TABLE_NAME);
            db.close();
            return cnt;
        }


    }
