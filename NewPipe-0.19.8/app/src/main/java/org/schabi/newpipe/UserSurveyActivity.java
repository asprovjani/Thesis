package org.schabi.newpipe;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Switch;

import java.util.ArrayList;


public class UserSurveyActivity extends AppCompatActivity {
    private static final String TAG = "UserSurveyActivity";
    public final int WRITE_EXTERNAL_STORAGE_PERMISSION_CODE = 99;

    Spinner spinnerAge, spinnerFieldOfStudy;
    Switch glassesSwitch;
    RadioGroup genderRadioGroup;
    RadioButton radioButtonMale, radioButtonFemale;
    String gender;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: Starting " + TAG);
        setContentView(R.layout.activity_user_survey);

        //initialize layout elements
        spinnerAge = (Spinner) findViewById(R.id.spinnerAge);
        spinnerFieldOfStudy = (Spinner) findViewById(R.id.spinnerFieldOfStudy);
        glassesSwitch = (Switch) findViewById(R.id.switch1);
        genderRadioGroup = (RadioGroup) findViewById(R.id.radioGroupGender);
        radioButtonMale = (RadioButton) findViewById(R.id.ButtonMale);
        radioButtonFemale = (RadioButton) findViewById(R.id.ButtonFemale);

        //populate age spinner
        ArrayList<String> ageList = new ArrayList<>();
        for(int i = 18; i < 70; i++)
            ageList.add(Integer.toString(i));

        ArrayAdapter<String> spinnerAgeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, ageList);
        spinnerAge.setAdapter(spinnerAgeAdapter);
    }
}