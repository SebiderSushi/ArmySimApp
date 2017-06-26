package me.sebi.armysim;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Created by sebi on 25.06.17.
 */

public class SetupArmyStringActivity extends AppCompatActivity {

    EditText editText_armyString;
    EditText editText_armyName;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup_army_text);

        editText_armyString = (EditText) findViewById(R.id.editText_armyString);
        editText_armyName = (EditText) findViewById(R.id.editText_armyName);

        Intent intent = getIntent();
        String armyString = intent.getStringExtra(MainActivity.EXTRA_MESSAGE_ARMY_STRING);
        String armyName = intent.getStringExtra(MainActivity.EXTRA_MESSAGE_ARMY_NAME);

        editText_armyName.setText(armyName);
        editText_armyString.setText(armyString);
        setTitle(armyName + getResources().getString(R.string.editMode));
    }

    public void toast(String text) {
        Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
    }

    public void switchToGUIMode(View view) {
        String armyName = editText_armyName.getText().toString();
        Intent intent = new Intent(this, SetupArmyActivity.class);
        intent.putExtra(MainActivity.EXTRA_MESSAGE_ARMY_STRING, getArmyString());
        intent.putExtra(MainActivity.EXTRA_MESSAGE_ARMY_NAME, armyName);
        intent.putExtra(MainActivity.EXTRA_MESSAGE_ARMY_STRING_LOAD, true);
        finish();
        startActivity(intent);
    }

    private String getArmyString() {
        return editText_armyString.getText().toString().replace(MainActivity.saveTextHead, "");
    }

    public void saveExitButton(View view) {
        saveArmy();
        finish();
    }

    public void saveArmyButton(View view) {
        saveArmy();
    }

    public void saveArmy() {
        SharedPreferences sharedPrefs = this.getSharedPreferences("me.sebi.armysim.ARMIES", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPrefs.edit();
        String armyName = editText_armyName.getText().toString();
        editor.putString(armyName, getArmyString());
        if (editor.commit()) {
            toast(armyName + getResources().getString(R.string.toast_saved));
        } else {
            toast(armyName + getResources().getString(R.string.toast_save_failed));
        }
    }
}
