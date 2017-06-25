package me.sebi.armysim;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Environment;
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

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    public final static String EXTRA_MESSAGE_ARMY_NAME = "me.sebi.armysim.ARMYNAME";
    public final static String EXTRA_MESSAGE_ARMY_NAMES = "me.sebi.armysim.ARMIES";
    public final static String EXTRA_MESSAGE_ARMY_LOAD = "me.sebi.armysim.ARMYLOAD";
    public final static String EXTRA_MESSAGE_ARMY_ROWCOUNT = "me.sebi.armysim.ARMYROWCOUNT";
    public final static String KEY_RANDOMNESS = "Randomness";
    public final static String PREFERENCES = "me.sebi.armysim.PREFERENCES";
    public final static String PREFERENCES_ARMIES = "me.sebi.armysim.ARMIES";
    public final String saveTextHead = "rowNumber,attack,lp,roundsAfterDeath,attackSpeed,attackWeakest;";

    private ListView listView;
    SharedPreferences prefs, prefs_armies;
    CheckBox checkbox_randomness;
    boolean allChecked;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefs = this.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
        prefs_armies = this.getSharedPreferences(PREFERENCES_ARMIES, Context.MODE_PRIVATE);
        checkbox_randomness = (CheckBox) findViewById(R.id.checkbox_default_randomness);

        checkbox_randomness.setChecked(prefs.getBoolean(MainActivity.KEY_RANDOMNESS, true));

        refreshListView();
    }

    @Override
    protected void onResume() {
        super.onResume();

        refreshListView();
    }

    private void refreshListView() {
        ArrayList<String> armyNames_list = new ArrayList<>();

        Map<String, ?> allEntries = prefs_armies.getAll();
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
                toast(MainActivity.this.getResources().getText(R.string.editing) + itemValue);

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
        allChecked = true;
        for (int i = 0; i < lv.getChildCount(); i++) {
            v = lv.getChildAt(i);
            box = (CheckBox) v.findViewById(R.id.checkbox_listViewElem);
            if (box.isChecked()) {
                name = (TextView) v.findViewById(R.id.textView_listViewElem_armyName);
                armies.add(name.getText().toString());
            } else
                allChecked = false;
        }

        if (armies.size() == 0)
            toast(this.getResources().getString(R.string.echo_noArmies));
        return armies;
    }

    public void checkAllArmies(View view) {
        ListView lv = (ListView) findViewById(R.id.lv_armies);
        View v;
        CheckBox box;
        for (int i = 0; i < lv.getChildCount(); i++) {
            v = lv.getChildAt(i);
            box = (CheckBox) v.findViewById(R.id.checkbox_listViewElem);
            box.setChecked(true);
        }
    }

    public void invertChecks(View view) {
        ListView lv = (ListView) findViewById(R.id.lv_armies);
        View v;
        CheckBox box;
        for (int i = 0; i < lv.getChildCount(); i++) {
            v = lv.getChildAt(i);
            box = (CheckBox) v.findViewById(R.id.checkbox_listViewElem);
            box.setChecked(!box.isChecked());
        }
    }

    public void saveRandomness(View view) {
        SharedPreferences.Editor editor = prefs.edit();
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

        startArmySetup(name, prefs_armies.contains(name), rowCount);
    }

    public void toast(String text) {
        Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
    }

    public void deleteButton(View view) {
        ArrayList<String> armies = getCheckedArmies();
        if (armies.size() > 0) {
            String cancel = getResources().getString(R.string.cancel);
            String delete = getResources().getString(R.string.delete);
            String body = "";
            if (allChecked) {
                body = getResources().getString(R.string.delete_confirm_all);
            } else {
                body = getResources().getString(R.string.delete_confirm);
                for (String army : armies)
                    body = body + "\n" + army;
            }
            new AlertDialog.Builder(this)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle(delete)
                    .setMessage(body)
                    .setPositiveButton(delete, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            deleteSelectedArmies();
                        }

                    })
                    .setNeutralButton(cancel, null)
                    .show();
            // the neutral button is on the left side in Marshmallow while the negative button would be right beneath 'delete'
            // therefore using neutral button here to prevent mistyping
        }
    }

    public void deleteSelectedArmies() {
        ArrayList<String> armies = getCheckedArmies();
        if (armies.size() != 0) {
            SharedPreferences.Editor editPrefs = prefs_armies.edit();
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

    private String selectedArmiesToString() {
        String armyString = saveTextHead;
        ArrayList<String> armies = getCheckedArmies();
        if (armies.size() > 0) { // '> 0' just in case of undetermined future developments
            for (String armyName : armies)
                armyString = armyString + "\n" + prefs_armies.getString(armyName, getResources().getString(R.string.error_could_not_get_army));
            return armyString;
        }
        return null;
    }

    public void copySelectedArmies(View view) {
        String saveText = selectedArmiesToString();
        if (saveText != null) {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("", saveText);
            clipboard.setPrimaryClip(clip);
            toast(getResources().getString(R.string.copied_to_clipboard));
        }
    }

    public void shareSelectedArmies(View view) {
        String saveText = selectedArmiesToString();
        if (saveText != null) {
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, saveText);
            sendIntent.setType("text/plain");
            startActivity(sendIntent);
        }
    }

    public void exportSelectedArmies(View view) {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            ArrayList<String> armies = getCheckedArmies();
            if (armies.size() > 0) { // '> 0' just in case of undetermined future developments
                File path = Environment.getExternalStoragePublicDirectory("ArmySim");
                for (String armyName : armies) {
                    String saveText = saveTextHead + "\n" + prefs_armies.getString(armyName, getResources().getString(R.string.error_could_not_get_army));
                    if (!saveTextToFile(new File(path.getAbsolutePath(), armyName + ".txt"), saveText))
                        toast(armyName + getResources().getString(R.string.export_could_not_save));
                }
                toast(getResources().getString(R.string.export_done));
                refreshListView();
            }
        } else {
            toast(getResources().getString(R.string.export_not_writable));
        }
    }

    public boolean saveTextToFile(File path, String text) {
        try {
            path.getParentFile().mkdirs();
            path.createNewFile();
            FileOutputStream outputStream = new FileOutputStream(path);
            outputStream.write(text.getBytes());
            outputStream.flush();
            outputStream.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
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
