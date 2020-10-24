package org.schabi.newpipe;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import com.opencsv.CSVWriter;

import org.schabi.newpipe.player.local.LocalVideoPlayer;
import org.schabi.newpipe.player.local.VideosListActivity;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;



public class UserSurveyActivity extends AppCompatActivity {
    private static final String TAG = "UserSurveyActivity";
    public final int EXTERNAL_STORAGE_PERMISSION_CODE = 99;

    Spinner spinnerAge;
    RadioGroup genderRadioGroup, glassesRadioGroup;
    RadioButton radioButtonMale, radioButtonFemale, radioButtonYes, radioButtonNo;
    Button confirmBtn;

    /*//////////////////////////////////////////////////////////////////////////
    // Activity's LifeCycle
    //////////////////////////////////////////////////////////////////////////*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: Starting " + TAG);
        setContentView(R.layout.activity_user_survey);

        //initialize layout elements
        spinnerAge = (Spinner) findViewById(R.id.spinnerAge);
        genderRadioGroup = (RadioGroup) findViewById(R.id.radioGroupGender);
        glassesRadioGroup = (RadioGroup) findViewById(R.id.radioGroupGlasses);
        radioButtonMale = (RadioButton) findViewById(R.id.ButtonMale);
        radioButtonFemale = (RadioButton) findViewById(R.id.ButtonFemale);
        radioButtonYes = (RadioButton) findViewById(R.id.ButtonYes);
        radioButtonNo = (RadioButton) findViewById(R.id.ButtonNo);
        confirmBtn = (Button) findViewById(R.id.buttonConfirm);

        confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //check if gender is selected
                if(buttonSelected()) {
                    //check for write external permission
                    if(checkPermissions()) {
                        try {
                            saveUserDataToFile();
                            startMainActivity();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                else {
                    Toast.makeText(getApplicationContext(),"Please make sure you've filled the survey and try again.", Toast.LENGTH_LONG).show();
                }
            }
        });

        //populate age spinner
        ArrayList<String> ageList = new ArrayList<>();
        for(int i = 18; i < 70; i++)
            ageList.add(Integer.toString(i));

        ArrayAdapter<String> spinnerAgeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, ageList);
        spinnerAge.setAdapter(spinnerAgeAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        //If first app run, show alert to inform the user about the app's purpose, else ask for write external permission
        if(!sharedPreferences.getBoolean("showInformation", false)) {
            AlertDialog informationDialog = new AlertDialog.Builder(this, R.style.FilePickerAlertDialogThemeDark)
                    .setTitle("Information for the user")
                    .setMessage("This application uses user context information(standing, walking, running...)" +
                            " to determine the optimal resolution for the video playback." +
                            " For the purposes of this research it needs permission to external memory" +
                            " when you change the playback resolution manually.")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            SharedPreferences.Editor preferencesEdit = sharedPreferences.edit();
                            preferencesEdit.putBoolean("showInformation", true);
                            preferencesEdit.apply();
                        }
                    })
                    .setCancelable(false)
                    .create();

            informationDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    checkPermissions();
                }
            });
            informationDialog.show();
        }
        else {
            checkPermissions();
        }
    }

    /*//////////////////////////////////////////////////////////////////////////
    // Utils
    //////////////////////////////////////////////////////////////////////////*/
    private boolean buttonSelected() {
        if(genderRadioGroup.getCheckedRadioButtonId() == -1 || glassesRadioGroup.getCheckedRadioButtonId() == -1)
            return false;

        return true;
    }

    private void saveUserDataToFile() throws IOException {
        String gender = radioButtonMale.isChecked() ? "Male" : "Female";
        String glasses = radioButtonYes.isChecked() ? "Yes" : "No";
        String age = (String) spinnerAge.getSelectedItem();

        Log.d(TAG, "USER DATA: " + gender + " " + glasses + " " + age);

        String dir = android.os.Environment.getExternalStorageDirectory().getAbsolutePath();
        String fileName = "UserData.csv";
        String path = dir + File.separator + fileName;
        File f = new File(path);
        CSVWriter w;

        if(f.exists() && !f.isDirectory()) {
            FileWriter fWriter = new FileWriter(path, false);
            w = new CSVWriter(fWriter);
        }
        else {
            w = new CSVWriter(new FileWriter(path));
        }

        String[] keys = {"GENDER", "AGE", "GLASSES"};
        String[] values = {gender,   age,   glasses};
        w.writeNext(keys);
        w.writeNext(values);
        w.close();
        Log.d(TAG, "USER DATA SAVED TO FILE:" + path);
    }

    private void startMainActivity() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor preferencesEdit = sharedPreferences.edit();
        preferencesEdit.putBoolean("FIRST", false);
        preferencesEdit.apply();

        //startActivity(new Intent(this, MainActivity.class));
        startActivity(new Intent(this, VideosListActivity.class));
    }

    private boolean checkPermissions() {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            //Permission not granted
            //Should we show explanation
            if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) &&
                    ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE) ) {
                new AlertDialog.Builder(this, R.style.FilePickerAlertDialogThemeDark)
                        .setMessage("The application needs access to external storage " +
                                "where it will store the data collected from this test " +
                                "and load the needed videos.")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions(UserSurveyActivity.this,
                                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE},
                                        EXTERNAL_STORAGE_PERMISSION_CODE);
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .create()
                        .show();

                return false;
            }
            else {
                //No explanation needed, proceed to request permission
                ActivityCompat.requestPermissions(UserSurveyActivity.this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE},
                        EXTERNAL_STORAGE_PERMISSION_CODE);

                return false;
            }
        }
        else {
            //Permission granted
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch(requestCode) {
            case EXTERNAL_STORAGE_PERMISSION_CODE:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "External storage permission granted.");
                }
                else {
                    Log.d(TAG, "External storage permission denied.");
                }
                break;
        }
    }
}