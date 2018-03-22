package edu.tamu.ecen.capstone.patientmd.activity;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

import edu.tamu.ecen.capstone.patientmd.R;
import edu.tamu.ecen.capstone.patientmd.activity.fragment.HomeFragment;
import edu.tamu.ecen.capstone.patientmd.activity.fragment.PlotFragment;
import edu.tamu.ecen.capstone.patientmd.activity.fragment.RecordsFragment;
import edu.tamu.ecen.capstone.patientmd.util.Util;

import static edu.tamu.ecen.capstone.patientmd.util.Const.IMG_FILEPATH;


public class MainActivity extends AppCompatActivity {

    private TextView mTextMessage;

    private final String TAG = "MainActivity: ";

    private int mSelectedItem;

    //app specific permission codes; to add more permissions, include in setPermissions and AndroidManifest.xml
    private final int PERMISSION_READ_EXT_STORAGE_CODE=2;
    private final int PERMISSION_WRITE_EXT_STORAGE_CODE=3;

    private Button cameraButton;



    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {

            //TODO use setContentView to show different screens/fragment
            return selectFragment(item);

            /*
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    mTextMessage.setText(R.string.title_home);
                    return true;
                case R.id.navigation_records:
                    mTextMessage.setText(R.string.title_archive);
                    return true;
                case R.id.navigation_plots:
                    mTextMessage.setText(R.string.title_plots);
                    return true;
            }

            Log.d(TAG, "NavigationMenuListener");
            return false;
            */
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "onCreate");

        mTextMessage = (TextView) findViewById(R.id.message);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        //initView();

        //get set of permissions from the user


    }



    private boolean selectFragment(MenuItem item) {
        Fragment fragment = null;

        Log.d(TAG, "SelectFragment::");

        //todo add all fragments here
        switch (item.getItemId()) {
            case R.id.navigation_home:
                fragment = HomeFragment.newInstance(null);
                mTextMessage.setText(R.string.title_home);
                break;
            case R.id.navigation_records:
                //fragment = RecordsFragment.newInstance();
                mTextMessage.setText(R.string.title_archive);
                break;
            case R.id.navigation_plots:
                //fragment = PlotFragment.newInstance();
                mTextMessage.setText(R.string.title_plots);
                break;


        }



        if(fragment != null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.add(R.id.container, fragment, fragment.getTag());
            ft.commit();
            return true;
        }

        return false;
    }


}
