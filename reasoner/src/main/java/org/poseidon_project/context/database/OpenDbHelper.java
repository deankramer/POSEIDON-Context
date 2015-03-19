/*Copyright 2015 POSEIDON Project

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package org.poseidon_project.context.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * The SQLiteOpenHelper needed for the db
 *
 * @author Dean Kramer <d.kramer@mdx.ac.uk>
 */
public class OpenDbHelper extends SQLiteOpenHelper{

    private static final int DATABASE_VERSION = 1;
    private static final String DB_NAME = "contextDB";
    public static final String CONTEXTTABLE = "usable_contexts";
    private static final String CONTEXTTABLE_CREATE = "create table usable_contexts (_id integer primary key autoincrement, "
            + "packagename text,"
            + "name text,"
            + "owner text,"
            + "permission int not null,"
            + "dex_file text);";

    private static final String USEDCONTEXTTABLE = "used_contexts";
    private static final String USEDCONTEXTABLE_CREATE = "create table used_contexts (_id integer primary key autoincrement, "
            + "contextname text,"
            + "activationdate text,"
            + "diactivationdate text);";

    public OpenDbHelper(Context context) {
        super(context, DB_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CONTEXTTABLE_CREATE);
        db.execSQL(USEDCONTEXTABLE_CREATE);
        insertStandardContexts(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {

    }

    private void insertStandardContexts(SQLiteDatabase db) {
        //Example
        //db.execSQL("insert into usable_contexts values (1, 'uk.ac.tvu.mdse.contextengine.contexts', 'BatteryContext', 'contextengine', 0, 'classes.dex')");
        /*db.execSQL("insert into usable_contexts values (1, 'org.poseidon_project.contexts.envir.LocationWeatherContext', 'LocationWeatherContext', 'contextengine', 0, 'classes.dex')");
        db.execSQL("insert into usable_contexts values (1, 'org.poseidon_project.contexts.envir.weather.BadWeatherContext', 'BadWeatherContext', 'contextengine', 0, 'classes.dex')");
        db.execSQL("insert into usable_contexts values (1, 'org.poseidon_project.contexts.hardware.BatteryContext', 'BatteryContext', 'contextengine', 0, 'classes.dex')");
        db.execSQL("insert into usable_contexts values (1, 'org.poseidon_project.contexts.hardware.CompassContext', 'CompassContext', 'contextengine', 0, 'classes.dex')");
        db.execSQL("insert into usable_contexts values (1, 'org.poseidon_project.contexts.hardware.ExternalStorageSpaceContext', 'ExternalStorageSpaceContext', 'contextengine', 0, 'classes.dex')");
        db.execSQL("insert into usable_contexts values (1, 'org.poseidon_project.contexts.hardware.GPSIndoorOutdoorContext', 'GPSIndoorOutdoorContext', 'contextengine', 0, 'classes.dex')");
        db.execSQL("insert into usable_contexts values (1, 'org.poseidon_project.contexts.hardware.LightContext', 'LightContext', 'contextengine', 0, 'classes.dex')");
        db.execSQL("insert into usable_contexts values (1, 'org.poseidon_project.contexts.hardware.TelephonyContext', 'TelephonyContext', 'contextengine', 0, 'classes.dex')");
        db.execSQL("insert into usable_contexts values (1, 'org.poseidon_project.contexts.hardware.WifiContext', 'WifiContext', 'contextengine', 0, 'classes.dex')");

        */
    }
}