package me.sebi.armysim;

/**
 * Created by sebi on 15.06.17.
 */

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class SetupSimulationActivity extends Activity {

    private final Counter counter = new Counter();
    private final LinearLayout.LayoutParams echoParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
    ArrayList<String> armyNames;
    private String[] armies;
    private CheckBox checkbox_randomness;
    private EditText edit_iterations;
    private LinearLayout echoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup_simulation);

        echoView = (LinearLayout) findViewById(R.id.echoView);
        edit_iterations = (EditText) findViewById(R.id.editText_sim_iterations);
        checkbox_randomness = (CheckBox) findViewById(R.id.checkbox_randomness);

        Intent intent = getIntent();
        armyNames = intent.getStringArrayListExtra(MainActivity.EXTRA_MESSAGE_ARMY_NAMES);
        armies = new String[armyNames.size()];

        //iterate through armynames and get each armystring from file
        SharedPreferences prefs_armies = this.getSharedPreferences(MainActivity.PREFERENCES_ARMIES, Context.MODE_PRIVATE);
        for (int i = 0; i < armyNames.size(); i++)
            armies[i] = prefs_armies.getString(armyNames.get(i), this.getResources().getString(R.string.namelessArmy));


        SharedPreferences sharedPrefs = this.getSharedPreferences(MainActivity.PREFERENCES, Context.MODE_PRIVATE);
        checkbox_randomness.setChecked(sharedPrefs.getBoolean(MainActivity.KEY_RANDOMNESS, true));
    }

    private void echo(String color, String text) {
        TextView echo = new TextView(this);
        echo.setText(text);
        if (color != null)
            echo.setTextColor(Color.parseColor(color));
        echo.setLayoutParams(echoParams);
        if (echoView.getChildCount() >= 1000)
            echoView.removeViewAt(999);
        echoView.addView(echo, 0);
    }

    public void echoRandomness(View view) {
        echo(this.getResources().getString(R.string.echo_randomness_color),
                this.getResources().getString(
                        checkbox_randomness.isChecked() ? R.string.randomness_on : R.string.randomness_off
                ));
    }

    private Simulation loadSim(String[] armies, boolean useRandom) {
        Simulation sim = new Simulation(useRandom, counter, this, echoView);
        //iterate through armystrings
        for (int i = 0; i < armies.length; i++) {
            String armyString = armies[i];
            String[] armyRows = armyString.replace("\n", "").split(";");
            //create army with name and add to simulation
            Army army = new Army(armyNames.get(i), sim);
            //iterate through rows and add to army
            for (String armyRow : armyRows) {
                String[] attributes = armyRow.split(",");
                Row row = new Row(
                        (attributes.length > 1) && !(attributes[1].equals("")) ? Integer.parseInt(attributes[1]) : 0,
                        (attributes.length > 2) && !(attributes[2].equals("")) ? Integer.parseInt(attributes[2]) : 0,
                        (attributes.length > 8) && !(attributes[8].equals("")) ? Integer.parseInt(attributes[8]) : 0,
                        (attributes.length > 3) && !(attributes[3].equals("")) ? Integer.parseInt(attributes[3]) : 0,
                        (attributes.length > 4) && !(attributes[4].equals("")) ? Integer.parseInt(attributes[4]) : 0,
                        (attributes.length > 5) && (attributes[5].equals("1")),
                        (attributes.length > 6) && !(attributes[6].equals("0")),
                        (attributes.length > 7) && (attributes[7].equals("1")),
                        (attributes.length > 9) && !(attributes[9].equals("")) ? Integer.parseInt(attributes[9]) : 0);
                army.addRow(row);
            }
        }
        return sim;
    }

    public void startSimulation(View view) {
        String str_iterations = edit_iterations.getText().toString();
        int iterations = 1;
        if (!str_iterations.equals(""))
            iterations = Integer.parseInt(str_iterations);
        for (int i = 0; i < iterations; i++) {
            Simulation sim = loadSim(armies, checkbox_randomness.isChecked());
            sim.simulate();
        }
        String text = getResources().getString(R.string.echo_ties) + counter.ties;
        for (String armyName : armyNames) {
            Integer wins = counter.armyWins.get(armyName);
            text = text + " " + armyName + ": " + ((wins != null) ? wins : "0");
        }
        echo(null, text);
    }
}