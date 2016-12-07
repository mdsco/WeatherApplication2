/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.mike.weatherapplication.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowApplication;

import java.util.HashSet;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

@RunWith(RobolectricTestRunner.class)
@Config(manifest=Config.NONE)
public class TestDb {


    static final String TEST_LOCATION = "99705";

    private static final String REMINDER_DATABASE_NAME = "weather.db";

    private WeatherDbHelper myDatabaseHelper;

    @Test
    public void whenTheDBHelperIsCreatedThenTheDatabaseNameShouldBeSet(){

        ShadowApplication context = Shadows.shadowOf(RuntimeEnvironment.application);
        myDatabaseHelper = new WeatherDbHelper(context.getApplicationContext());
        assertEquals(REMINDER_DATABASE_NAME, myDatabaseHelper.getDatabaseName());
    }

    @Test
    public void whenTheWriteableDatabaseIsRequestedItIsReturnedWithCorrectTables(){

        final HashSet<String> tableNameHashSet = new HashSet<String>();
        tableNameHashSet.add(WeatherContract.LocationEntry.TABLE_NAME);
        tableNameHashSet.add(WeatherContract.WeatherEntry.TABLE_NAME);

        ShadowApplication context = Shadows.shadowOf(RuntimeEnvironment.application);
        myDatabaseHelper = new WeatherDbHelper(context.getApplicationContext());

        SQLiteDatabase writableDatabase = myDatabaseHelper.getWritableDatabase();

        assertEquals(true, writableDatabase.isOpen());

        Cursor cursor = writableDatabase.rawQuery
                ("SELECT name FROM sqlite_master WHERE type='table'", null);

        assertTrue("Error: database not correctly created", cursor.moveToFirst());

        do {
            tableNameHashSet.remove(cursor.getString(0));
        } while (cursor.moveToNext());

        assertTrue("All tables created for database", tableNameHashSet.isEmpty());

        Cursor locationTableCursor = writableDatabase.rawQuery("PRAGMA table_info("
                + WeatherContract.LocationEntry.TABLE_NAME + ");", null);

        assertTrue("Unable to query database for table information",
                locationTableCursor.moveToFirst());

        final HashSet<String> locationColumnHashSet = new HashSet<String>();
        locationColumnHashSet.add(WeatherContract.LocationEntry._ID);
        locationColumnHashSet.add(WeatherContract.LocationEntry.COLUMN_CITY_NAME);
        locationColumnHashSet.add(WeatherContract.LocationEntry.COLUMN_COORD_LAT);
        locationColumnHashSet.add(WeatherContract.LocationEntry.COLUMN_COORD_LONG);
        locationColumnHashSet.add(WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING);

        int columnNameIndex = locationTableCursor.getColumnIndex("name");

        do {
            String columnName = locationTableCursor.getString(columnNameIndex);
            locationColumnHashSet.remove(columnName);
        } while(locationTableCursor.moveToNext());

        assertTrue("Columns are missing: "
                + locationColumnHashSet.toString(), locationColumnHashSet.isEmpty());

        locationTableCursor.close();
        writableDatabase.close();

    }

    /*
        Students:  Here is where you will build code to test that we can insert and query the
        location database.  We've done a lot of work for you.  You'll want to look in TestUtilities
        where you can uncomment out the "createNorthPoleLocationValues" function.  You can
        also make use of the ValidateCurrentRecord function from within TestUtilities.
    */
    @Test
    public void testLocationTable() {
        ShadowApplication application =
                Shadows.shadowOf(RuntimeEnvironment.application);
        WeatherDbHelper dbHelper =
                new WeatherDbHelper(application.getApplicationContext());
        SQLiteDatabase sqliteDatabase =
                dbHelper.getWritableDatabase();
        insertLocation(sqliteDatabase);
        sqliteDatabase.close();
    }

    private long insertLocation(SQLiteDatabase sqliteDatabase) {

        ContentValues northPoleLocationValues = TestUtilities.createNorthPoleLocationValues();

        long locationRowId = sqliteDatabase.insert(
                WeatherContract.LocationEntry.TABLE_NAME, null, northPoleLocationValues);

        // Insert ContentValues into database and get a row ID back
        long rowId = sqliteDatabase.insert(WeatherContract.LocationEntry.TABLE_NAME, null, northPoleLocationValues);

        // Query the database and receive a Cursor back
        Cursor locationTableCursor = sqliteDatabase.rawQuery("SELECT * FROM location", null);

        // Move the cursor to a valid database row
        locationTableCursor.moveToFirst();

        // Validate data in resulting Cursor with the original ContentValues
        // (you can use the validateCurrentRecord function in TestUtilities to validate the
        // query if you like)

        String locationSetting = northPoleLocationValues.get(WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING).toString();
        String city = northPoleLocationValues.get(WeatherContract.LocationEntry.COLUMN_CITY_NAME).toString();
        String latitude = northPoleLocationValues.get(WeatherContract.LocationEntry.COLUMN_COORD_LAT).toString();
        String longitude = northPoleLocationValues.get(WeatherContract.LocationEntry.COLUMN_COORD_LONG).toString();

        assertEquals("The correct location setting info should have been returned"
                , locationSetting, locationTableCursor.getString(1));
        assertEquals("The correct city should have been returned"
                , city, locationTableCursor.getString(2));
        assertEquals("The correct latitude info should have been returned"
                , latitude, locationTableCursor.getString(3));
        assertEquals("The correct longitude info should have been returned"
                , longitude, locationTableCursor.getString(4));

        assertFalse("there shouldn't be this many records",locationTableCursor.moveToNext());

        // Finally, close the cursor and database
        locationTableCursor.close();

        return rowId;
    }



    /*
        Students:  Here is where you will build code to test that we can insert and query the
        database.  We've done a lot of work for you.  You'll want to look in TestUtilities
        where you can use the "createWeatherValues" function.  You can
        also make use of the validateCurrentRecord function from within TestUtilities.
     */
    @Test
    public void testWeatherTable() {

        ShadowApplication application =
                Shadows.shadowOf(RuntimeEnvironment.application);
        WeatherDbHelper dbHelper =
                new WeatherDbHelper(application.getApplicationContext());
        SQLiteDatabase sqliteDatabase =
                dbHelper.getWritableDatabase();
        long locationRowID = insertLocation(sqliteDatabase);


        // Create ContentValues of what you want to insert
        // (you can use the createWeatherValues TestUtilities function if you wish)
        ContentValues weatherValues = TestUtilities.createWeatherValues(locationRowID);

        // Insert ContentValues into database and get a row ID back
        sqliteDatabase.insert(WeatherContract.WeatherEntry.TABLE_NAME, null, weatherValues);
        // Query the database and receive a Cursor back
        Cursor weatherDbCursor = sqliteDatabase.query(WeatherContract.WeatherEntry.TABLE_NAME, null, null, null, null, null, null);
        // Move the cursor to a valid database row
        weatherDbCursor.moveToFirst();

        // Validate data in resulting Cursor with the original ContentValues
        // (you can use the validateCurrentRecord function in TestUtilities to validate the
        // query if you like)
        TestUtilities.validateCursor("", weatherDbCursor, weatherValues);

        // Finally, close the cursor and database
        weatherDbCursor.close();
        sqliteDatabase.close();
    }

}
