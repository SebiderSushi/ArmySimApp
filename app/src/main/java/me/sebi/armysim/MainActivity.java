package me.sebi.armysim;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
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

@SuppressWarnings("deprecation")
public class MainActivity extends Activity {

    public final static String EXTRA_MESSAGE_ARMY_NAME = "me.sebi.armysim.ARMYNAME";
    public final static String EXTRA_MESSAGE_ARMY_NAMES = "me.sebi.armysim.ARMIES";
    public final static String EXTRA_MESSAGE_ARMY_LOAD = "me.sebi.armysim.ARMYLOAD";
    public final static String EXTRA_MESSAGE_ARMY_ROWCOUNT = "me.sebi.armysim.ARMYROWCOUNT";
    public final static String EXTRA_MESSAGE_ARMY_STRING = "me.sebi.armysim.ARMYSTRING";
    public final static String EXTRA_MESSAGE_ARMY_STRING_LOAD = "me.sebi.armysim.ARMYSTRINGLOAD";
    public final static String KEY_RANDOMNESS = "Randomness";
    public final static String PREFERENCES = "me.sebi.armysim.PREFERENCES";
    public final static String PREFERENCES_ARMIES = "me.sebi.armysim.ARMIES";
    public final static String saveTextHead = "rowNumber,attack,lp,roundsAfterDeath,attackSpeed,attackWeakest,distanceFighter;";
    private final static int PERMISSION_REQUEST_EXPORT_ARMIES = 1;
    private final int sdk = Integer.parseInt(Build.VERSION.SDK);
    private final String exportpath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/ArmySim";
    private ListView listView;
    private SharedPreferences prefs, prefs_armies;
    private CheckBox checkbox_randomness;
    private boolean allChecked;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> armyNames;

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static boolean saveTextToFile(File path, String text) {
        try {
            path.getParentFile().mkdirs();
            path.createNewFile();
            FileOutputStream outputStream = new FileOutputStream(path);
            outputStream.write(text.getBytes());
            outputStream.flush();
            outputStream.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @TargetApi(11)
    private static void copyToClipboard(Context context, String string) {
        int sdk = Integer.parseInt(Build.VERSION.SDK);
        if (sdk < 11) {
            android.text.ClipboardManager clipboard = (android.text.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            clipboard.setText(string);
        } else {
            ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("", string);
            clipboard.setPrimaryClip(clip);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefs = this.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
        prefs_armies = this.getSharedPreferences(PREFERENCES_ARMIES, Context.MODE_PRIVATE);
        checkbox_randomness = (CheckBox) findViewById(R.id.checkbox_default_randomness);

        checkbox_randomness.setChecked(prefs.getBoolean(MainActivity.KEY_RANDOMNESS, true));

        listView = (ListView) findViewById(R.id.lv_armies);

        armyNames = getAllArmyNames();
        Collections.sort(armyNames);

        adapter = new ArrayAdapter<>(this,
                R.layout.element_listview_army,
                R.id.textView_listViewElem_armyName,
                armyNames);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String armyName = (String) listView.getItemAtPosition(position);
                toast(MainActivity.this.getText(R.string.editing) + armyName);
                startArmySetup(armyName, true, -1);
            }
        });
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                String armyName = (String) listView.getItemAtPosition(position);
                toast("Rename" + armyName);
                return true;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        refreshListView();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_EXPORT_ARMIES:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    exportSelectedArmies();
        }
    }

    @SuppressWarnings("SameParameterValue")
    @TargetApi(23)
    private boolean checkPermission(String permission, int request) {
        if (checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED)
            return true;
        else {
            requestPermissions(new String[]{permission}, request);
            return false;
        }
    }

    private void refreshListView() {
        armyNames.clear();
        armyNames.addAll(getAllArmyNames());
        Collections.sort(armyNames);
        adapter.notifyDataSetChanged();
    }

    private ArrayList<String> getAllArmyNames() {
        ArrayList<String> armyNames = new ArrayList<>();

        Map<String, ?> allEntries = prefs_armies.getAll();
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            armyNames.add(entry.getKey());
        }

        return armyNames;
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
            toast(this.getString(R.string.echo_noArmies));
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
        editor.commit();
    }

    public void createArmy(View view) {
        EditText editText_name = (EditText) findViewById(R.id.editText_create_name);
        EditText editText_rowCount = (EditText) findViewById(R.id.editText_create_rowCount);
        String name = editText_name.getText().toString();
        String str_rowCount = editText_rowCount.getText().toString();
        int rowCount = 1;
        if (!str_rowCount.equals(""))
            rowCount = Integer.parseInt(str_rowCount);

        startArmySetup(name, prefs_armies.contains(name), rowCount);

        editText_name.setText("");
        editText_rowCount.setText("");
    }

    private void toast(String text) {
        Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
    }

    public void deleteButton(View view) {
        ArrayList<String> armies = getCheckedArmies();
        if (armies.size() > 0) {
            String cancel = getString(R.string.cancel);
            String delete = getString(R.string.delete);
            String body;
            if (allChecked) {
                body = getString(R.string.delete_confirm_all);
            } else {
                body = getString(R.string.delete_confirm);
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

    private void deleteSelectedArmies() {
        ArrayList<String> armies = getCheckedArmies();
        if (armies.size() != 0) {
            SharedPreferences.Editor editPrefs = prefs_armies.edit();
            for (String army : armies)
                editPrefs.remove(army);
            editPrefs.commit();

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
        if (armies.size() > 0) {
            for (String armyName : armies)
                armyString = armyString + "\n\n" + armyName + "\n" + prefs_armies.getString(armyName, getString(R.string.error_could_not_get_army));
            return armyString;
        }
        return null;
    }

    public void copySelectedArmies(View view) {
        String saveText = selectedArmiesToString();
        if (saveText != null) {
            copyToClipboard(this, saveText);
            toast(getString(R.string.copied_to_clipboard));
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

    public void exportSelectedArmiesButton(View view) {
        if (sdk < 23 || checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, PERMISSION_REQUEST_EXPORT_ARMIES))
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
                exportSelectedArmies();
            else toast(getString(R.string.export_not_writable));
        else toast(getString(R.string.toast_permissiondenied_storage));
    }

    private void exportSelectedArmies() {
        ArrayList<String> armies = getCheckedArmies();
        if (armies.size() > 0) {
            for (String armyName : armies) {
                String saveText = saveTextHead + "\n" + prefs_armies.getString(armyName, getString(R.string.error_could_not_get_army));
                if (!saveTextToFile(new File(exportpath, armyName + ".txt"), saveText))
                    toast(armyName + getString(R.string.export_could_not_save));
            }
            toast(getString(R.string.export_done));
        }
    }

    public void startSimulationSetup(View view) {
        ArrayList<String> armies = getCheckedArmies();
        if (armies.size() != 0) {
            Intent intent = new Intent(this, SetupSimulationActivity.class);
            intent.putExtra(EXTRA_MESSAGE_ARMY_NAMES, armies);
            startActivity(intent);
        }
    }
}
