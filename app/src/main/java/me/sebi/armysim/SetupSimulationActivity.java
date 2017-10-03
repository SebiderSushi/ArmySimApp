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
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by sebi on 15.06.17.
 */

public class SetupSimulationActivity extends Activity {

    private final Counter counter_global = new Counter();
    private final LinearLayout.LayoutParams echoParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
    SimulationAsyncTask asyncTask = null;
    Simulation sim;
    //List<AsyncTask> asyncTasks = new ArrayList<>();
    ProgressBar progressBar;
    private ArrayList<String> armyNames;
    private String[] armies;
    private CheckBox checkbox_randomness;
    private EditText edit_iterations;
    private LinearLayout echoView;
    private TextView tv_global;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup_simulation);

        echoView = (LinearLayout) findViewById(R.id.echoView);
        edit_iterations = (EditText) findViewById(R.id.editText_sim_iterations);
        checkbox_randomness = (CheckBox) findViewById(R.id.checkbox_randomness);
        tv_global = (TextView) findViewById(R.id.tv_global);

        Intent intent = getIntent();
        armyNames = intent.getStringArrayListExtra(MainActivity.EXTRA_MESSAGE_ARMY_NAMES);
        armies = new String[armyNames.size()];

        //iterate through armynames and get each armystring from file
        SharedPreferences prefs_armies = this.getSharedPreferences(MainActivity.PREFERENCES_ARMIES, Context.MODE_PRIVATE);
        for (int i = 0; i < armyNames.size(); i++)
            armies[i] = prefs_armies.getString(armyNames.get(i), this.getResources().getString(R.string.namelessArmy));

        sim = loadSim(armies);

        SharedPreferences sharedPrefs = this.getSharedPreferences(MainActivity.PREFERENCES, Context.MODE_PRIVATE);
        checkbox_randomness.setChecked(sharedPrefs.getBoolean(MainActivity.KEY_RANDOMNESS, true));

        progressBar = findViewById(R.id.progress);

        for (String armyName : armyNames)
            counter_global.armyWins.put(armyName, 0);
        onCounterChanged();
    }

    public void cancel(View v) {
        //for (AsyncTask asyncTask : asyncTasks)
        asyncTask.cancel(true);
    }

    public TextView echo(String color, String text) {
        TextView echo;
        if (echoView.getChildCount() >= 1000) {
            echo = (TextView) echoView.getChildAt(999);
            echoView.removeViewAt(999);
        } else {
            echo = new TextView(this);
        }
        echo.setText(text);
        if (color != null)
            echo.setTextColor(Color.parseColor(color));
        echo.setLayoutParams(echoParams);
        echoView.addView(echo, 0);
        return echo;
    }

    public void echoRandomness(View view) {
        echo(this.getResources().getString(R.string.echo_randomness_color),
                this.getResources().getString(
                        checkbox_randomness.isChecked() ? R.string.randomness_on : R.string.randomness_off
                ));
        sim.useRandom = checkbox_randomness.isChecked();
    }

    private Simulation loadSim(String[] armies) {
        Simulation sim = new Simulation(counter_global);
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
        if (asyncTask == null || asyncTask.getStatus() == AsyncTask.Status.FINISHED) {
            TextView echoTextView = echo(null, "Simulation in progress...");
            echoTextView.setMinLines(2);
            sim.useRandom = checkbox_randomness.isChecked();
            String str_iterations = edit_iterations.getText().toString();
            int iterations = 1;
            if (!str_iterations.equals(""))
                iterations = Integer.parseInt(str_iterations);
            progressBar.setMax(iterations);
            progressBar.setProgress(0);
            asyncTask = new SimulationAsyncTask(sim, this, echoTextView, iterations, counter_global);
            asyncTask.execute();
            Toast.makeText(this, "Simulation started", Toast.LENGTH_SHORT).show();
        } else
            Toast.makeText(this, "Already running", Toast.LENGTH_SHORT).show();
        //asyncTasks.add(asyncTask);
        /*
        echoTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                asyncTask.cancel(true);
                view.setOnClickListener(null);
            }
        });
        */
    }

    void onCounterChanged() {
        String text = getResources().getString(R.string.echo_total) + counter_global.total
                + " " + getResources().getString(R.string.echo_ties) + counter_global.ties;
        for (String armyName : counter_global.armyWins.keySet()) {
            Integer wins = counter_global.armyWins.get(armyName);
            text += " " + armyName + ": " + ((wins != null) ? wins : "0");
        }
        tv_global.setText(text);
    }
}

class SimulationAsyncTask extends AsyncTask<Void, String, Void> {

    private final TextView echoView;
    private final Simulation sim;
    private final Context context;
    private final SetupSimulationActivity setupSimulationActivity;
    private final Counter counter_global;
    private int iterations;
    private long start, end;

    SimulationAsyncTask(Simulation sim, Context context, TextView echoView, int iterations, Counter counter_global) {
        start = System.currentTimeMillis();
        this.sim = sim;
        this.context = context;
        this.setupSimulationActivity = (SetupSimulationActivity) context;
        this.echoView = echoView;
        this.iterations = iterations;
        this.counter_global = counter_global;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        echoView.setText("");
    }

    @Override
    protected Void doInBackground(Void... voids) {
        for (int i = 0; i < iterations; i++) {
            if (this.isCancelled())
                return null;
            sim.simulate();
            String result = "Error";
            if (sim.armies.size() == 1) {
                int rowsLeft = sim.armies.get(0).rows.size();
                String name = sim.armies.get(0).name;
                result = name + context.getResources().getString(R.string.echo_win_winInRound)
                        + sim.round + context.getResources().getString(R.string.echo_win_w) + rowsLeft
                        + context.getResources().getString(R.string.echo_win_row)
                        + (rowsLeft == 1 ? "" : context.getResources().getString(R.string.echo_win_plural))
                        + context.getResources().getString(R.string.echo_win_left)
                        + " (" + sim.counter.armyWins.get(name) + ")";
            }
            if (sim.armies.size() == 0)
                result = context.getResources().getString(R.string.echo_tie) + " (" + sim.counter.ties + ")";
            publishProgress(result, Integer.toString(i+1));
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
        /*
        counter_global.total += sim.counter.total;
        counter_global.ties += sim.counter.ties;
        String text = "\n" + context.getResources().getString(R.string.echo_total) + sim.counter.total
                + " " + context.getResources().getString(R.string.echo_ties) + sim.counter.ties;
        for (String armyName : sim.counter.armyWins.keySet()) {
            Integer wins = sim.counter.armyWins.get(armyName);
            Integer wins_global = counter_global.armyWins.get(armyName);
            counter_global.armyWins.put(armyName, ((wins_global != null) ? wins_global : 0) + ((wins != null) ? wins : 0));
            text += " " + armyName + ": " + ((wins != null) ? wins : "0");
        }
        */
        setupSimulationActivity.onCounterChanged();
        end = System.currentTimeMillis();
        setupSimulationActivity.echo(null, "Duration: " + Long.toString(end - start) + "ms");
        //setupSimulationActivity.asyncTasks.remove(this);
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        end = System.currentTimeMillis();
        setupSimulationActivity.echo(null, "Cancelled: Duration: " + Long.toString(end - start) + "ms");
        //setupSimulationActivity.asyncTasks.remove(this);
    }
}