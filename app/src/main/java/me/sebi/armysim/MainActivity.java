package me.sebi.armysim;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    public final static String EXTRA_MESSAGE_ARMY_NAME = "me.sebi.armysim.ARMYNAME";
    public final static String EXTRA_MESSAGE_ARMY_NAMES = "me.sebi.armysim.ARMIES";
    public final static String EXTRA_MESSAGE_ARMY_LOAD = "me.sebi.armysim.ARMYLOAD";
    public final static String EXTRA_MESSAGE_ARMY_ROWCOUNT = "me.sebi.armysim.ARMYROWCOUNT";
    public final static String KEY_RANDOMNESS = "Randomness";
    public final static String NAME_PREFS = "me.sebi.armysim.PREFERENCES";
    public final static String NAME_PREFS_ARMIES = "me.sebi.armysim.ARMIES";

    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        CheckBox checkbox_randomness = (CheckBox) findViewById(R.id.checkbox_default_randomness);
        SharedPreferences sharedPrefs = this.getSharedPreferences(NAME_PREFS, Context.MODE_PRIVATE);
        checkbox_randomness.setChecked(sharedPrefs.getBoolean(MainActivity.KEY_RANDOMNESS, true));

        refreshListView();
    }

    @Override
    protected void onResume() {
        super.onResume();

        refreshListView();
    }

    private void refreshListView() {
        ArrayList<String> armyNames_list = new ArrayList<>();

        SharedPreferences sharedPrefs = this.getSharedPreferences(NAME_PREFS_ARMIES, Context.MODE_PRIVATE);
        Map<String, ?> allEntries = sharedPrefs.getAll();
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            armyNames_list.add(entry.getKey());
        }

        Collections.sort(armyNames_list);

        // Get ListView object from xml
        listView = (ListView) findViewById(R.id.lv_armies);

        // Define a new Adapter
        // First parameter - Context
        // Second parameter - Layout for the row
        // Third parameter - ID of the TextView to which the data is written
        // Forth - the Array of data
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                R.layout.element_listview_army, R.id.textView_listViewElem_armyName, armyNames_list);
        // Assign adapter to ListView
        listView.setAdapter(adapter);
        // ListView Item Click Listener
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // ListView Clicked item value
                String itemValue = (String) listView.getItemAtPosition(position);
                // Show Alert
                Toast.makeText(getApplicationContext(), MainActivity.this.getResources().getText(R.string.editing) + itemValue, Toast.LENGTH_LONG).show();

                startArmySetup(itemValue, true, -1);
            }
        });
    }

    private ArrayList<String> getCheckedArmies() {
        ArrayList<String> armies = new ArrayList<>(0);

        ListView lv = (ListView) findViewById(R.id.lv_armies);
        View v;
        CheckBox box;
        TextView name;
        for (int i = 0; i < lv.getChildCount(); i++) {
            v = lv.getChildAt(i);
            box = (CheckBox) v.findViewById(R.id.checkbox_listViewElem);
            if (box.isChecked()) {
                name = (TextView) v.findViewById(R.id.textView_listViewElem_armyName);
                armies.add(name.getText().toString());
            }
        }

        if (armies.size() == 0) {
            Toast toast = Toast.makeText(this, this.getResources().getString(R.string.echo_noArmies), Toast.LENGTH_LONG);
            TextView toastText = (TextView) toast.getView().findViewById(android.R.id.message);
            if (toastText != null) toastText.setGravity(Gravity.CENTER);
            toast.show();
        }
        return armies;
    }

    public void saveRandomness(View view) {
        CheckBox checkbox_randomness = (CheckBox) view;

        SharedPreferences sharedPrefs = this.getSharedPreferences(NAME_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putBoolean(KEY_RANDOMNESS, checkbox_randomness.isChecked());
        editor.apply();
    }

    public void createArmy(View view) {
        EditText editText_name = (EditText) findViewById(R.id.editText_create_name);
        EditText editText_rowCount = (EditText) findViewById(R.id.editText_create_rowCount);
        String name = editText_name.getText().toString();
        String str_rowCount = editText_rowCount.getText().toString();
        int rowCount = 1;
        if (!str_rowCount.equals(""))
            rowCount = Integer.parseInt(str_rowCount);

        editText_name.setText("");
        editText_rowCount.setText("");

        startArmySetup(name, false, rowCount);
    }

    public void deleteSelectedArmies(View view) {
        ArrayList<String> armies = getCheckedArmies();
        if (armies.size() != 0) {
            SharedPreferences sharedPrefs = this.getSharedPreferences("me.sebi.armysim.ARMIES", Context.MODE_PRIVATE);
            SharedPreferences.Editor editPrefs = sharedPrefs.edit();
            for (String army : armies)
                editPrefs.remove(army);
            editPrefs.apply();

            refreshListView();
        }
    }

    private void startArmySetup(String armyName, boolean load, int rowCount) {
        Intent intent = new Intent(this, SetupArmyActivity.class);
        intent.putExtra(EXTRA_MESSAGE_ARMY_NAME, armyName);
        intent.putExtra(EXTRA_MESSAGE_ARMY_LOAD, load);
        if (rowCount >= 0) intent.putExtra(EXTRA_MESSAGE_ARMY_ROWCOUNT, rowCount);
        startActivity(intent);
    }

    public void setupSimulation(View view) {
        ArrayList<String> armies = getCheckedArmies();
        if (armies.size() != 0) {
            Intent intent = new Intent(this, SetupSimulationActivity.class);
            intent.putExtra(EXTRA_MESSAGE_ARMY_NAMES, armies);
            startActivity(intent);
        }
    }
}
