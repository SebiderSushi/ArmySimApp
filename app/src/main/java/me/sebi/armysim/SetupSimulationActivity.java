package me.sebi.armysim;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by sebi on 15.06.17.
 */

public class SetupSimulationActivity extends Activity {

    private final Counter counter = new Counter();
    private final LinearLayout.LayoutParams echoParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
    ProgressBar progressBar;
    private SimulationAsyncTask asyncTask = null;
    private Simulation sim;
    private ArrayList<String> armyNames;
    private CheckBox checkbox_randomness;
    private EditText edit_iterations;
    private LinearLayout echoView;
    private TextView tv_counter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup_simulation);

        echoView = (LinearLayout) findViewById(R.id.echoView);
        edit_iterations = (EditText) findViewById(R.id.editText_sim_iterations);
        checkbox_randomness = (CheckBox) findViewById(R.id.checkbox_randomness);
        tv_counter = (TextView) findViewById(R.id.tv_global);

        Intent intent = getIntent();
        armyNames = intent.getStringArrayListExtra(ArmyListActivity.EXTRA_MESSAGE_ARMY_NAMES);
        String[] armies = new String[armyNames.size()];

        //iterate through armynames and get each armystring from file
        SharedPreferences prefs_armies = this.getSharedPreferences(ArmyListActivity.PREFERENCES_ARMIES, Context.MODE_PRIVATE);
        for (int i = 0; i < armyNames.size(); i++)
            armies[i] = prefs_armies.getString(armyNames.get(i), this.getResources().getString(R.string.namelessArmy));

        sim = loadSim(armies);

        SharedPreferences sharedPrefs = this.getSharedPreferences(ArmyListActivity.PREFERENCES, Context.MODE_PRIVATE);
        checkbox_randomness.setChecked(sharedPrefs.getBoolean(ArmyListActivity.KEY_RANDOMNESS, true));

        progressBar = (ProgressBar) findViewById(R.id.progress);

        for (String armyName : armyNames)
            counter.armyWins.put(armyName, 0);
        onCounterChanged();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        cancel(null);
        finish();
    }

    public void cancel(View v) {
        if (asyncTask != null)
            asyncTask.cancel(true);
    }

    public void echo(String color, String text) {
        TextView echo;
        if (echoView.getChildCount() >= 100) {
            echo = (TextView) echoView.getChildAt(99);
            echoView.removeViewAt(99);
        } else {
            echo = new TextView(this);
        }
        echo.setText(text);
        if (color != null)
            echo.setTextColor(Color.parseColor(color));
        else
            echo.setTextColor(Color.WHITE);
        echo.setLayoutParams(echoParams);
        echoView.addView(echo, 0);
    }

    public void echoRandomness(View view) {
        echo(this.getResources().getString(R.string.echo_randomness_color),
                this.getResources().getString(
                        checkbox_randomness.isChecked() ? R.string.randomness_on : R.string.randomness_off
                ));
        sim.useRandom = checkbox_randomness.isChecked();
    }

    private Simulation loadSim(String[] armies) {
        Simulation sim = new Simulation(counter);
        //iterate through armystrings
        for (int i = 0; i < armies.length; i++) {
            //create army with name and add to simulation
            new Army(armyNames.get(i), sim, armies[i]);
        }
        return sim;
    }

    public void startSimulation(View view) {
        if (asyncTask == null || asyncTask.getStatus() == AsyncTask.Status.FINISHED) {
            sim.useRandom = checkbox_randomness.isChecked();
            String str_iterations = edit_iterations.getText().toString();
            int iterations = 1;
            if (!str_iterations.equals(""))
                iterations = Integer.parseInt(str_iterations);
            progressBar.setMax(iterations);
            progressBar.setProgress(0);
            asyncTask = new SimulationAsyncTask(this, sim, iterations);
            asyncTask.execute();
            echo(null, "");
        }
    }

    void onCounterChanged() {
        StringBuilder text = new StringBuilder();
        text.append(getResources().getString(R.string.echo_total));
        text.append(counter.total);
        text.append(" ");
        text.append(getResources().getString(R.string.echo_ties));
        text.append(counter.ties);
        for (String armyName : armyNames) {
            text.append(" ");
            text.append(armyName);
            text.append(": ");
            text.append(counter.armyWins.get(armyName));
        }
        tv_counter.setText(text);
    }
}

class SimulationAsyncTask extends AsyncTask<Void, String, Void> {

    private final int iterations;
    private final long start;
    private Context context;
    private SetupSimulationActivity setupSimulationActivity;
    private Simulation sim;

    SimulationAsyncTask(Context context, Simulation sim, int iterations) {
        this.start = System.currentTimeMillis();
        this.context = context;
        this.setupSimulationActivity = (SetupSimulationActivity) context;
        this.sim = sim;
        this.iterations = iterations;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        for (int i = 0; i < iterations && !isCancelled(); i++) {
            sim.simulate(this);
            String result = context.getString(R.string.error);
            if (sim.armies.size() == 1) {
                int rowsLeft = sim.armies.get(0).rows.size();
                String name = sim.armies.get(0).name;
                if (rowsLeft == 1)
                    result = context.getString(R.string.echo_winInRound_singular,
                            name,
                            sim.round,
                            sim.counter.armyWins.get(name));
                else
                    result = context.getString(R.string.echo_winInRound_plural,
                            name,
                            sim.round,
                            rowsLeft,
                            sim.counter.armyWins.get(name));
            }
            if (sim.armies.size() == 0)
                result = context.getResources().getString(R.string.echo_tie, sim.counter.ties);
            publishProgress(result, Integer.toString(i + 1));
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(String... values) {
        super.onProgressUpdate(values);
        setupSimulationActivity.echo(null, values[0]);
        setupSimulationActivity.progressBar.setProgress(Integer.parseInt(values[1]));
        setupSimulationActivity.onCounterChanged();
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        long end = System.currentTimeMillis();
        setupSimulationActivity.echo(null,
                String.format(Locale.getDefault(), context.getString(R.string.echo_duration), end - start));
        context = null;
        setupSimulationActivity = null;
        sim = null;
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        long end = System.currentTimeMillis();
        setupSimulationActivity.echo(null,
                String.format(Locale.getDefault(), context.getString(R.string.echo_cancelled_duration), end - start));
        context = null;
        setupSimulationActivity = null;
        sim = null;
    }
}