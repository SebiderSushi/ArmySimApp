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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@SuppressWarnings("deprecation")
public class ArmyListActivity extends Activity {

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
    private final ArmyListSortByNameComparator nameComparator = new ArmyListSortByNameComparator();
    private SharedPreferences prefs, prefs_armies;
    private CheckBox checkbox_randomness;
    private boolean allChecked;
    private ArmyListAdapter adapter;
    private List<ArmyListViewEntryData> armyList = new ArrayList<>();
    private Context context;

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
        setContentView(R.layout.activity_army_list);

        context = this;
        prefs = this.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
        prefs_armies = this.getSharedPreferences(PREFERENCES_ARMIES, Context.MODE_PRIVATE);
        checkbox_randomness = findViewById(R.id.checkbox_default_randomness);

        checkbox_randomness.setChecked(prefs.getBoolean(ArmyListActivity.KEY_RANDOMNESS, true));

        ListView listView = findViewById(R.id.lv_armies);
        adapter = new ArmyListAdapter(this, armyList);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String armyName = armyList.get(position).armyName;
                toast(getString(R.string.editing, armyName));
                startArmySetup(armyName, true, -1);
            }
        });
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                String armyName = armyList.get(position).armyName;
                new RenameArmyDialog(context, armyName).show();
                return true;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_army_list_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_armylist_checkAll:
                checkAllArmies(null);
                return true;
            case R.id.menu_armylist_copy:
                copySelectedArmies();
                return true;
            case R.id.menu_armylist_delete:
                deleteButton();
                return true;
            case R.id.menu_armylist_export:
                exportSelectedArmiesButton();
                return true;
            case R.id.menu_armylist_invert:
                invertChecks(null);
                return true;
            case R.id.menu_armylist_runSim:
                startSimulationSetup(null);
                return true;
            case R.id.menu_armylist_share:
                shareSelectedArmies();
                return true;
            case R.id.menu_armylist_create:
                new CreateArmyDialog(this).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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
        boolean found;
        ArmyListViewEntryData data;
        Iterator<ArmyListViewEntryData> armyListIterator = armyList.iterator();
        ArrayList<ArmyListViewEntryData> trash = new ArrayList<>();
        Map<String, ?> allEntries = prefs_armies.getAll();
        Iterator<String> allEntriesIterator;

        while (armyListIterator.hasNext()) {
            data = armyListIterator.next();
            found = false;
            allEntriesIterator = allEntries.keySet().iterator();
            while (allEntriesIterator.hasNext()) {
                String entry = allEntriesIterator.next();
                if (entry.equals(data.armyName)) {
                    found = true;
                    allEntriesIterator.remove();
                    break;
                }
            }
            if (!found) {
                armyListIterator.remove();
                trash.add(data);
            }
        }
        Iterator<ArmyListViewEntryData> trashRecycler = trash.iterator();
        allEntriesIterator = allEntries.keySet().iterator();
        while (trashRecycler.hasNext() && allEntriesIterator.hasNext()) {
            data = trashRecycler.next();
            data.armyName = allEntriesIterator.next();
            data.checked = false;
            armyList.add(data);
        }
        while (allEntriesIterator.hasNext())
            armyList.add(new ArmyListViewEntryData(allEntriesIterator.next()));
        Collections.sort(armyList, nameComparator);
        adapter.notifyDataSetChanged();
    }

    private ArrayList<String> getCheckedArmies() {
        ArrayList<String> armies = new ArrayList<>(0);

        allChecked = true;
        for (ArmyListViewEntryData data : armyList) {
            if (data.checked)
                armies.add(data.armyName);
            else
                allChecked = false;
        }

        if (armies.size() == 0)
            toast(this.getString(R.string.echo_noArmies));
        return armies;
    }

    public void checkAllArmies(View view) {
        for (ArmyListViewEntryData data : armyList)
            data.checked = true;
        adapter.notifyDataSetChanged();
    }

    public void invertChecks(View view) {
        for (ArmyListViewEntryData data : armyList)
            data.checked = !data.checked;
        adapter.notifyDataSetChanged();
    }

    public void saveRandomness(View view) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(KEY_RANDOMNESS, checkbox_randomness.isChecked());
        editor.commit();
    }

    public void createArmy(View view) {
        EditText editText_name = findViewById(R.id.editText_create_name);
        EditText editText_rowCount = findViewById(R.id.editText_create_rowCount);
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

    public void deleteButton() {
        ArrayList<String> armies = getCheckedArmies();
        if (armies.size() > 0) {
            String cancel = getString(android.R.string.cancel);
            String delete = getString(R.string.delete);
            StringBuilder body = new StringBuilder();
            if (allChecked) {
                body.append(getString(R.string.delete_confirm_all));
            } else {
                body.append(getString(R.string.delete_confirm));
                for (String army : armies) {
                    body.append("\n");
                    body.append(army);
                }
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
        StringBuilder sb = new StringBuilder();
        sb.append(saveTextHead);
        ArrayList<String> armies = getCheckedArmies();
        if (armies.size() > 0) {
            for (String armyName : armies) {
                sb.append("\n\n");
                sb.append(armyName);
                sb.append("\n");
                sb.append(prefs_armies.getString(armyName, getString(R.string.error_could_not_get_army)));
            }
            return sb.toString();
        }
        return null;
    }

    public void copySelectedArmies() {
        String saveText = selectedArmiesToString();
        if (saveText != null) {
            copyToClipboard(this, saveText);
            toast(getString(R.string.copied_to_clipboard));
        }
    }

    public void shareSelectedArmies() {
        String saveText = selectedArmiesToString();
        if (saveText != null) {
            if (saveText.length() > 64000) {
                toast(getString(R.string.toast_too_much_data));
                return;
            }
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, saveText);
            sendIntent.setType("text/plain");
            startActivity(sendIntent);
        }
    }

    public void exportSelectedArmiesButton() {
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
                    toast(getString(R.string.export_could_not_save, armyName));
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

    class CreateArmyDialog extends AlertDialog.Builder {
        private View rootView;

        CreateArmyDialog(Context context) {
            super(context);
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            rootView = inflater.inflate(R.layout.dialog_army_create, null);
            setView(rootView);
            setPositiveButton(context.getString(R.string.create), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    createArmy();
                }

            });
            setNeutralButton(context.getString(android.R.string.cancel), null);
        }

        void createArmy() {
            EditText editText_name = rootView.findViewById(R.id.editText_create_name);
            EditText editText_rowCount = rootView.findViewById(R.id.editText_create_rowCount);
            String name = editText_name.getText().toString();
            String str_rowCount = editText_rowCount.getText().toString();
            int rowCount = 1;
            if (!str_rowCount.equals(""))
                rowCount = Integer.parseInt(str_rowCount);

            startArmySetup(name, prefs_armies.contains(name), rowCount);
        }
    }

    class RenameArmyDialog extends AlertDialog.Builder {
        private View rootView;
        private EditText editText_name;
        private String armyName;

        RenameArmyDialog(Context context, String armyName) {
            super(context);
            this.armyName = armyName;
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            rootView = inflater.inflate(R.layout.dialog_army_rename, null);
            setView(rootView);
            setTitle(String.format(getResources().getString(R.string.renaming), armyName));
            setPositiveButton(context.getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    renameArmy();
                }

            });
            setNeutralButton(context.getString(android.R.string.cancel), null);
            editText_name = rootView.findViewById(R.id.rename_editText_name);
            editText_name.setText(armyName);
            editText_name.setHint(armyName);
        }

        void renameArmy() {
            CheckBox keepOriginal = rootView.findViewById(R.id.rename_checkbox_keep_original);
            String newName = editText_name.getText().toString();

            String armyString = prefs_armies.getString(armyName, "");
            SharedPreferences.Editor editor = prefs_armies.edit();
            editor.putString(newName, armyString);
            if (keepOriginal.isChecked()) {
                if (editor.commit()) {
                    toast(String.format(getResources().getString(R.string.toast_copied), armyName, newName));
                } else {
                    toast(String.format(getResources().getString(R.string.toast_copy_failed), newName));
                }
            } else {
                editor.remove(armyName);
                if (editor.commit()) {
                    toast(String.format(getResources().getString(R.string.toast_renamed), newName));
                } else {
                    toast(String.format(getResources().getString(R.string.toast_rename_failed), newName));
                }
            }
            refreshListView();
        }
    }
}
