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
public class OpenDbHelper extends SQLiteOpenHelper {

    public static final String CONTEXTTABLE = "usable_contexts";
    public static final String RECEIVERTABLE = "usable_receivers";
    public static final String DEBUGEVENTSTABLE = "events_data";
    private static final int DATABASE_VERSION = 2;
    private static final String DB_NAME = "contextDB";
    private static final String CONTEXTTABLE_CREATE = "create table usable_contexts (_id integer primary key autoincrement, "
            + "packagename text,"
            + "name text,"
            + "owner text,"
            + "permission int not null,"
            + "dex_file text);";
    private static final String RECEIVERTABLE_CREATE = "create table usable_receivers (_id integer primary key autoincrement, "
            + "packagename text,"
            + "name text,"
            + "owner text,"
            + "dex_file text);";
    private static final String DEBUGEVENTSTABLE_CREATE = "create table events_data (_id integer primary key autoincrement, "
            + "eventOrigin smallint,"
            + "eventLocation text,"
            + "eventDateTime text,"
            + "eventText text);";

    private static final String CONTEXTRESULT_CREATE = "create table context_result (_id integer primary key autoincrement, "
            + "contextState text,"
            + "value integer,"
            + "fromtime integer,"
            + "totime integer);";

    public OpenDbHelper(Context context) {
        super(context, DB_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CONTEXTTABLE_CREATE);
        db.execSQL(RECEIVERTABLE_CREATE);
        db.execSQL(DEBUGEVENTSTABLE_CREATE);
        db.execSQL(CONTEXTRESULT_CREATE);
        insertStandardContexts(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int curv, int newv) {

        db.execSQL("DROP TABLE IF EXISTS " + CONTEXTTABLE);
        db.execSQL(CONTEXTTABLE_CREATE);
        insertStandardContexts(db);

        db.execSQL("DROP TABLE IF EXISTS " + RECEIVERTABLE);
        db.execSQL(RECEIVERTABLE_CREATE);

        db.execSQL("DROP TABLE IF EXISTS " + DEBUGEVENTSTABLE);
        db.execSQL(DEBUGEVENTSTABLE_CREATE);

    }

    private void insertStandardContexts(SQLiteDatabase db) {

        //Usable Context DB
        //Example
        //db.execSQL("insert into usable_contexts values (1, 'uk.ac.tvu.mdse.contextengine.contexts', 'BatteryContext', 'contextengine', 0, 'classes.dex')");
        db.execSQL("insert into usable_contexts values (1, 'org.poseidon_project.contexts.envir', 'LocationWeatherContext', 'contextengine', 0, 'classes.dex')");
        db.execSQL("insert into usable_contexts values (2, 'org.poseidon_project.contexts.envir.weather', 'BadWeatherContext', 'contextengine', 0, 'classes.dex')");
        db.execSQL("insert into usable_contexts values (3, 'org.poseidon_project.contexts.hardware', 'BatteryContext', 'contextengine', 0, 'classes.dex')");
        db.execSQL("insert into usable_contexts values (4, 'org.poseidon_project.contexts.hardware', 'CompassContext', 'contextengine', 0, 'classes.dex')");
        db.execSQL("insert into usable_contexts values (5, 'org.poseidon_project.contexts.hardware', 'ExternalStorageSpaceContext', 'contextengine', 0, 'classes.dex')");
        db.execSQL("insert into usable_contexts values (6, 'org.poseidon_project.contexts.hardware', 'GPSIndoorOutdoorContext', 'contextengine', 0, 'classes.dex')");
        db.execSQL("insert into usable_contexts values (7, 'org.poseidon_project.contexts.hardware', 'LightContext', 'contextengine', 0, 'classes.dex')");
        db.execSQL("insert into usable_contexts values (8, 'org.poseidon_project.contexts.hardware', 'TelephonyContext', 'contextengine', 0, 'classes.dex')");
        db.execSQL("insert into usable_contexts values (9, 'org.poseidon_project.contexts.hardware', 'WifiContext', 'contextengine', 0, 'classes.dex')");
        db.execSQL("insert into usable_contexts values (10, 'org.poseidon_project.contexts.hardware', 'StepCounter', 'contextengine', 0, 'classes.dex')");
        db.execSQL("insert into usable_contexts values (11, 'org.poseidon_project.contexts.hardware', 'DistanceTravelledContext', 'contextengine', 0, 'classes.dex')");
        db.execSQL("insert into usable_contexts values (12, 'org.poseidon_project.contexts.hardware', 'PluggedInContext', 'contextengine', 0, 'classes.dex')");
        db.execSQL("insert into usable_contexts values (13, 'org.poseidon_project.contexts.hardware', 'CurrentLocationContext', 'contextengine', 0, 'classes.dex')");
        //Context Receiver DB
        //Example
        //db.execSQL("insert into usable_receivers values (1, 'org.poseidon_project.context.management', 'POSEIDONReceiver', 'contextengine', 'classes.dex')");

    }
}
