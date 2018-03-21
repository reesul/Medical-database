package edu.tamu.ecen.capstone.patientmd.activity;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
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
import edu.tamu.ecen.capstone.patientmd.util.Util;

import static edu.tamu.ecen.capstone.patientmd.util.Const.IMG_FILEPATH;


public class MainActivity extends AppCompatActivity {

    private TextView mTextMessage;

    private final String TAG = "MainActivity: ";

    //app specific permission codes; to add more permissions, include in setPermissions and AndroidManifest.xml
    private final int PERMISSION_READ_EXT_STORAGE_CODE=2;
    private final int PERMISSION_WRITE_EXT_STORAGE_CODE=3;

    private Button cameraButton;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {

            //TODO use setContentView to show different screens
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    mTextMessage.setText(null);
                    return true;
                case R.id.navigation_archive:
                    mTextMessage.setText(R.string.title_archive);
                    return true;
                case R.id.navigation_plots:
                    mTextMessage.setText(R.string.title_plots);
                    return true;
            }
            Log.d(TAG, "NavigationMenuListener");
            return false;
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

        initView();

        //get set of permissions from the user


    }




/*
Function sets the initial view whenever the app is opened
 */
    private void initView() {
        Log.d(TAG, "initView");
        //setup the button for accessing the camera
        cameraButton = (Button) findViewById(R.id.new_record_button);
        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "CameraButton: onClick");

                /*
                if (MainActivity.this.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "CameraButton: User must provide storage write access");
                }*/

                //when the user clicks the button, they should be prompted to confirm they want to take a picture
                new AlertDialog.Builder(MainActivity.this)
                        .setMessage(R.string.camera_confirmation)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //begin process to taking a picture
                               dispatchTakePictureIntent();
                                /*Intent intent = new Intent(MainActivity.this, PhotoActivity.class);
                                startActivity(intent);
                                */
                            }
                        })
                        .show();
                /*
                if (!RecordPhoto.isRunning) {
                    //TODO provide new record here, give popup window to select new photo or existing file
                    //Maybe not use this type of context??

                    //todo change this to start an activity that handles all of the camera stuff
                    RecordPhoto.takePhoto(getApplicationContext());
                }
                else Log.d(TAG, "Cam button click; unavailable");
                */
            }
        });


    }

    static final int REQUEST_IMAGE_CAPTURE = 1;

    static final int REQUEST_TAKE_PHOTO = 1559;

    private void dispatchTakePictureIntent() {
        Util.permissionCamera(this);
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                Log.e(TAG, "dispatchTakePictureIntent:: error", ex);
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "edu.tamu.ecen.capstone.input.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }


    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQUEST_TAKE_PHOTO && resultCode == Activity.RESULT_OK) {
            int index = mCurrentPhotoPath.lastIndexOf("/");
            String toastMessage = mCurrentPhotoPath.substring(index+1, mCurrentPhotoPath.length());
            Log.d(TAG,"onActivityResult:: File: "+mCurrentPhotoPath);
            Toast.makeText(getApplicationContext(), "Filename: "+toastMessage, Toast.LENGTH_LONG)
                .show();

            //ToDo: anything that requires the picture as soon as it is taken
        }
    }


    String mCurrentPhotoPath;

    private File createImageFile() throws IOException {
        // Create an image file name
        Util.permissionExternalWrite(this);
        String imageFileName = IMG_FILEPATH + "/" + Util.dateForFile(System.currentTimeMillis()) + ".jpg";
        File image = new File(imageFileName);
        image.getParentFile().mkdirs();
        image.createNewFile();

        if(image.exists())
            Log.d(TAG,"createImageFile:: file created successfully");

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        Log.d(TAG, "CreateImageFile:: path is "+mCurrentPhotoPath);
        return image;
    }




}
