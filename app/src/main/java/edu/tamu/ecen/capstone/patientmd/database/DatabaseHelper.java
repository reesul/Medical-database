package edu.tamu.ecen.capstone.patientmd.database;

/**
 * Created by Jonathan on 2/27/2018.
 */

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

import edu.tamu.ecen.capstone.patientmd.activity.SignUpActivity;
import edu.tamu.ecen.capstone.patientmd.util.MedicalSample;

public class DatabaseHelper extends SQLiteOpenHelper {
    // constants for database name and column names
    public static final String DATABASE_NAME = "PMD.db";
    public static final String TABLE_NAME = "PMD_table";
    public static final String COL_1 = "ID";
    public static final String COL_2 = "DATE";
    public static final String COL_3 = "TESTS";
    public static final String COL_4 = "RESULT";
    public static final String COL_5 = "UNITS";
    public static final String COL_6 = "REFERENCE_INTERVAL";

    public static Context mContext;

    private DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference().child(SignUpActivity.username).child("Entries");

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + TABLE_NAME +" (ID INTEGER,DATE TEXT,TESTS TEXT,RESULT TEXT, UNITS TEXT, REFERENCE_INTERVAL TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS "+TABLE_NAME);
        onCreate(db);
    }

    // pull data from firebase to SQLtie
    public void pullData() {
        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                for (DataSnapshot child: children) {
                    String date = child.child("Date").getValue(String.class);
                    String tests = child.child("Tests").getValue(String.class);
                    String result = child.child("Result").getValue(String.class);
                    String units = child.child("Units").getValue(String.class);
                    String reference_interval = child.child("Reference Interval").getValue(String.class);

                    insertDataSQLite(date, tests, result, units, reference_interval);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        rootRef.addListenerForSingleValueEvent(valueEventListener);
    }

    // sync firebase with SQLite
    public void syncData() {
        // delete all data in firebase
        rootRef.removeValue();

        Cursor res = this.getAllData();

        // push entire SQLite database into firebase
        while (res.moveToNext()) {
            DatabaseReference entryRef = rootRef.push();
            entryRef.child("ID").setValue(res.getString(0));
            entryRef.child("Date").setValue(res.getString(1));
            entryRef.child("Tests").setValue(res.getString(2));
            entryRef.child("Result").setValue(res.getString(3));
            entryRef.child("Units").setValue(res.getString(4));
            entryRef.child("Reference Interval").setValue(res.getString(5));
        }
    }

    // insert data just into SQLite database
    public boolean insertDataSQLite(String date,String tests,String result, String units, String reference_interval) {
        SQLiteDatabase db = this.getWritableDatabase();

        // building an object to be inserted
        ContentValues contentValues = new ContentValues();
        // the ID field will increase by 1 for each entry
        contentValues.put(COL_1,(int) (long) this.getProfilesCount() + 1);
        contentValues.put(COL_2,date);
        contentValues.put(COL_3,tests);
        contentValues.put(COL_4,result);
        contentValues.put(COL_5,units);
        contentValues.put(COL_6,reference_interval);

        // insert data
        // test < 0 signifies an insertion error
        long test = db.insert(TABLE_NAME,null ,contentValues);

        if(test == -1)
            return false;
        else
            return true;
    }

    // insert an entry into the database
    public boolean insertData(String date,String tests,String result, String units, String reference_interval) {
        SQLiteDatabase db = this.getWritableDatabase();

        // building an object to be inserted
        ContentValues contentValues = new ContentValues();
        // the ID field will increase by 1 for each entry
        contentValues.put(COL_1,(int) (long) this.getProfilesCount() + 1);
        contentValues.put(COL_2,date);
        contentValues.put(COL_3,tests);
        contentValues.put(COL_4,result);
        contentValues.put(COL_5,units);
        contentValues.put(COL_6,reference_interval);

        // firebase insertion
        DatabaseReference entryRef = rootRef.push();
        entryRef.child("ID").setValue(String.valueOf(this.getProfilesCount() + 1));
        entryRef.child("Date").setValue(date);
        entryRef.child("Tests").setValue(tests);
        entryRef.child("Result").setValue(result);
        entryRef.child("Units").setValue(units);
        entryRef.child("Reference Interval").setValue(reference_interval);

        // insert data
        // test < 0 signifies an insertion error
        long test = db.insert(TABLE_NAME,null ,contentValues);

        if(test == -1)
            return false;
        else
            return true;
    }

    // retrieve all entries from the database
    public Cursor getAllData() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select * from "+TABLE_NAME,null);
        return res;
    }

    // build database entry based on cursor
    public DatabaseEntry buildEntry(Cursor res) {
        DatabaseEntry entry = new DatabaseEntry();
        entry.setId(res.getString(0));
        entry.setDate(res.getString(1));
        entry.setTests(res.getString(2));
        entry.setResult(res.getString(3));
        entry.setUnits(res.getString(4));
        entry.setReference_interval(res.getString(5));

        return entry;
    }

    // get all values of a specific field from database
    public Set<String> getAllField(String field) {
        // sets by design do not contain duplicates
        // tree set is sorted as well
        Set<String> array = new TreeSet<>();

        // get entire database table
        Cursor res = getAllData();

        // build set based on field
        switch(field.toLowerCase()) {
            case "date":
                while (res.moveToNext()) {
                    array.add(res.getString(1));
                }

            case "tests":
                while (res.moveToNext()) {
                    array.add(res.getString(2));
                }

            case "result":
                while (res.moveToNext()) {
                    array.add(res.getString(3));
                }

            case "units":
                while (res.moveToNext()) {
                    array.add(res.getString(4));
                }

            case "reference_interval":
                while (res.moveToNext()) {
                    array.add(res.getString(5));
                }
        }

        return array;
    }

    // query database based on specific field
    public List<DatabaseEntry> queryData(String field, String text) {
        List<DatabaseEntry> entries = new ArrayList<>();

        if (text.contains("(") || text.contains(")")) {
            Log.d("DatabaseHelper", "queryData:: has parentheses: " + text);
            StringBuffer text_buffer = new StringBuffer(text);

            int i = text.indexOf("(");
            text_buffer.insert(i, "\\");
            int j = text.indexOf(")");
            text_buffer.insert(j + 1, "\\"); // +1 because text_buffer is changed, but text is not

            text = text_buffer.toString();

        }



        Log.d("DatabaseHelper", "queryData:: query field " + text);

        // get entire database table
        Cursor res = getAllData();

        // loop through each row
        while (res.moveToNext()) {

            // build list based on field
            switch(field.toLowerCase()) {
                case "date":
                    if (res.getString(1).matches(text))
                        entries.add(buildEntry(res));
                    break;

                case "tests":
                    if (res.getString(2).matches(text))
                        entries.add(buildEntry(res));
                    break;

                case "result":
                    if (res.getString(3).matches(text))
                        entries.add(buildEntry(res));
                    break;

                case "units":
                    if (res.getString(4).matches(text))
                        entries.add(buildEntry(res));
                    break;

                case "reference_interval":
                    if (res.getString(5).matches(text))
                        entries.add(buildEntry(res));
                    break;
            }
        }
        return entries;
    }

    // query database based on "tests" field and range of dates
    public List<DatabaseEntry> queryRangeData(String test, String date_low, String date_high) {
        List<DatabaseEntry> entries = new ArrayList<>();

        if (test.contains("(") || test.contains(")")) {
            Log.d("DatabaseHelper", "queryData:: has parentheses: " + test);
            StringBuffer text_buffer = new StringBuffer(test);

            int i = test.indexOf("(");
            text_buffer.insert(i, "\\");
            int j = test.indexOf(")");
            text_buffer.insert(j + 1, "\\"); // +1 because text_buffer is changed, but text is not

            test = text_buffer.toString();

        }

        // get entire database table
        Cursor res = getAllData();

        // dates for comparison
        Date date_entry = new Date();
        Date date_date_high = new Date();
        Date date_date_low = new Date();

        // loop through each row
        // add entry to array if it matches test and is contained in the date interval
        while (res.moveToNext()) {

            SimpleDateFormat format = new SimpleDateFormat("M/dd/yyyy", Locale.US);
            try {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(format.parse(res.getString(1)));
                date_entry = calendar.getTime();

                calendar.setTime(format.parse(date_low));
                date_date_low = calendar.getTime();

                calendar.setTime(format.parse(date_high));
                date_date_high = calendar.getTime();

            } catch (ParseException e) {
                e.printStackTrace();
            }

            if (res.getString(2).matches(test) &&
                    (date_entry.getTime() >= date_date_low.getTime()) &&
                    (date_entry.getTime() <= date_date_high.getTime()))
                entries.add(buildEntry(res));
        }

        return entries;
    }

    // update a specific entry in the database
    public boolean updateData(String id, String date, String tests, String result, String units, String reference_interval) {
        SQLiteDatabase db = this.getWritableDatabase();

        // building an object to be updated
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_1,id);
        contentValues.put(COL_2,date);
        contentValues.put(COL_3,tests);
        contentValues.put(COL_4,result);
        contentValues.put(COL_5,units);
        contentValues.put(COL_6,reference_interval);

        // updating database entry
        db.update(TABLE_NAME, contentValues, "ID = ?",new String[] { id });

        syncData();

        return true;
    }

    // delete all data in the database
    public Integer deleteAllData() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = this.getAllData();
        int i = 0;
        Integer rows_deleted;

        while(res.moveToNext()) {
            db.delete(TABLE_NAME, "ID = ?", new String[]{String.valueOf(i)});
            ++i;
        }

        rows_deleted = db.delete(TABLE_NAME, "ID = ?", new String[]{String.valueOf(i)});

        return rows_deleted;
    }

    // delete a specific entry in the database
    public Integer deleteData (final String id) {
        SQLiteDatabase db = this.getWritableDatabase();

        // delete database entry specified by ID
        Integer rows_deleted = db.delete(TABLE_NAME, "ID = ?",new String[] {id});

        // building an object to be updated
        ContentValues contentValues = new ContentValues();
        Cursor res = this.getAllData();

        // sliding down all entries with ID greater than the deleted row
        while (res.moveToNext()) {
            if (Integer.parseInt(res.getString(0)) > Integer.parseInt(id))
                contentValues.put(COL_1,Integer.parseInt(res.getString(0)) - 1);
                contentValues.put(COL_2,res.getString(1));
                contentValues.put(COL_3,res.getString(2));
                contentValues.put(COL_4,res.getString(3));
                contentValues.put(COL_5,res.getString(4));
                contentValues.put(COL_6,res.getString(5));

                // slide down row by 1 ID an update current row
                db.update(TABLE_NAME, contentValues, "ID = ?",new String[] { res.getString(0) });
        }

        syncData();

        // will be positive if the row was successfully deleted
        return rows_deleted;
    }

    // get total number of entries in the database
    public long getProfilesCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        return DatabaseUtils.queryNumEntries(db, TABLE_NAME);
    }


    /**ADDED BY REESE 10/27     **/

    public boolean ReadRecordCSV(File csv) {
        ArrayList<MedicalSample> medical_samples = new ArrayList<>();
        String TAG = "DatabaseHelper: ";
        Log.d(TAG, "ReadMedicalData:: Begin");
        //InputStream is = getResources().openRawResource(R.raw.data);
        if (!csv.getName().contains("csv")) {
            Log.e(TAG, "input csv file is bad!!!");
            return false;
        }

        Log.d(TAG, "CSV file has " + csv.length() + " bytes");
        if (csv.length()==0) {
            return false;
        }


        String line = "";

        try {
            InputStream is = new FileInputStream(csv);

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(is, Charset.forName("UTF-8"))
            );

            //reader.readLine();

            while ( (line = reader.readLine()) != null) {
                Log.d(TAG, "ReadMedicalDatabase:: " + line);
                //String[] tokens = line.split(",");
                //String[] tokens = line.split("(?:,)(?=(?:[^\'][^\'])*[^\']*$)", -1);
                //String[] tokens = line.split("(?:[,]{1})(?=(?:[^\'][^\'])*[^\']*$)", -1);
                //String[] tokens = line.split(",(?=([^\'][^\'])*[^\']*$)", -1);
                String[] tokens = line.split(",(?=(?:[^\']*\'[^\']*\')*(?![^\']*\'))", -1);

                for (int i=0; i<tokens.length; i++) {
                    Log.d(TAG, tokens[i]);
                }

                if (tokens.length <=1 ) {
                    Log.d(TAG, "Found bad line in CSV::   " + line);
                    continue;
                }



                MedicalSample sample = new MedicalSample();
                sample.setDate(tokens[0]);
                sample.setTests(tokens[1].replaceAll("\'",""));

                if (tokens[2].length() > 0) {
                    sample.setResult(tokens[2]);

                }
                else sample.setResult("NA");

                if (tokens[3].length() > 0)
                    sample.setUnits(tokens[3]);
                else
                    sample.setUnits("NA");

                if (tokens[4].length() > 0 && tokens.length >= 5)
                    sample.setReference_interval(tokens[4]);
                else
                    sample.setReference_interval("NA");


                //fix any issues
                if (sample.getUnits().equals("0") || sample.getUnits().equals("o")) {
                    sample.setUnits("%");
                }

                medical_samples.add(sample);
            }
        } catch (IOException e) {
            Log.wtf("DatabaseHelper:", "Error reading data file on line " + line, e);
            e.printStackTrace();
        } catch (IndexOutOfBoundsException e) {
            Log.wtf(TAG, "Error reading data file", e);
            return false;
        }

        boolean isInserted = false;

        boolean added[] = new boolean[medical_samples.size()];

        for (int i = 0; i < medical_samples.size(); ++i) {
            isInserted = insertData(
                    medical_samples.get(i).getDate(),
                    medical_samples.get(i).getTests(),
                    medical_samples.get(i).getResult(),
                    medical_samples.get(i).getUnits(),
                    medical_samples.get(i).getReference_interval()
            );

            added[i] = isInserted;
        }

        isInserted = true;
        for (int i = 0; i < added.length; ++i) {
            if (!added[i])
                isInserted = false;
        }

        return true;
    }




}
