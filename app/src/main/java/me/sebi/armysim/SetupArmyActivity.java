package me.sebi.armysim;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by sebi on 15.06.17.
 */

public class SetupArmyActivity extends Activity {

    private EditText editText_armyName;
    private SharedPreferences prefs_armies;
    private ArmyForListView army;
    private ListView rowBox;
    private SetupArmyAdapter adapter;
    private ArrayList<RelativeLayout> ROWS = new ArrayList<>(0);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup_army);

        prefs_armies = this.getSharedPreferences("me.sebi.armysim.ARMIES", Context.MODE_PRIVATE);
        editText_armyName = findViewById(R.id.armySetup_et_armyName);
        rowBox = findViewById(R.id.armySetup_lv_rowBox);

        Intent intent = getIntent();
        String armyName = intent.getStringExtra(ArmyListActivity.EXTRA_MESSAGE_ARMY_NAME);
        setTitle(armyName + getResources().getString(R.string.editMode));
        editText_armyName.setText(armyName);

        if (intent.getBooleanExtra(ArmyListActivity.EXTRA_MESSAGE_ARMY_LOAD, false)) {
            String armyString = prefs_armies.getString(armyName, "");
            army = new ArmyForListView(armyString);
        } else if (intent.getBooleanExtra(ArmyListActivity.EXTRA_MESSAGE_ARMY_STRING_LOAD, false)) {
            String armyString = intent.getStringExtra(ArmyListActivity.EXTRA_MESSAGE_ARMY_STRING);
            army = new ArmyForListView(armyString);
        } else {
            setTitle(this.getResources().getText(R.string.create) + " (" + armyName + ")");
            int rowCount = intent.getIntExtra(ArmyListActivity.EXTRA_MESSAGE_ARMY_ROWCOUNT, 1);
            army = new ArmyForListView();
            for (int i = 0; i < rowCount; i++)
                army.rows.add(new RowForListView());
        }

        adapter = new SetupArmyAdapter(this, army.rows);
        rowBox.setAdapter(adapter);
        reloadRowNumbers();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_setup_army_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_newRow:
                addRow(null);
                return true;
            case R.id.menu_text:
                switchToTextMode(null);
                return true;
            case R.id.menu_save:
                saveArmy();
                return true;
            case R.id.menu_saveExit:
                saveExitButton(null);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void toast(String text) {
        Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
    }

    @SuppressWarnings({"WeakerAccess", "SameParameterValue"})
    public void addRow(View view) {
        army.rows.add(new RowForListView());
        reloadRowNumbers();
    }

    private void reloadRowNumbers() {
        setTitleRowNumber(army.rows.size());
        adapter.notifyDataSetChanged();
    }

    private void setTitleRowNumber(int number) {
        setTitle(getArmyName() + this.getResources().getString(R.string.editMode) + " " + number + " " +
                ((number != 1) ? getResources().getString(R.string.rows) : getResources().getString(R.string.row)));
    }

    public void deleteRow(View view) {
        int i = (Integer) ((LinearLayout) view.getParent()).getTag();
        army.rows.remove(i);
        reloadRowNumbers();
    }

    public void moveToRowButton(View view) {
        int i = (Integer) ((LinearLayout) view.getParent()).getTag();
        RelativeLayout row = (RelativeLayout) view.getParent().getParent();
        EditText rowNumberView = row.findViewById(R.id.armyRow_editText_rowNumber);
        moveToRow(i, Integer.parseInt(rowNumberView.getText().toString()));

    }

    private void moveToRow(int index, int targetRow) {
        int armyRowsSize = army.rows.size();
        targetRow--; //since we count from zero here and from 1 in editText_armyString
        if (targetRow < 0)
            targetRow += armyRowsSize;
        if (targetRow < armyRowsSize && targetRow >= 0) {

            RowForListView pop = army.rows.remove(index);
            army.rows.add(targetRow, pop);

            reloadRowNumbers();
            if (Build.VERSION.SDK_INT >= 8)
                rowBox.smoothScrollToPosition(targetRow);
        } else
            toast(this.getResources().getText(R.string.indexOutOfRange) + Integer.toString(targetRow));

        //TODO hide keyboard
    }

    private void moveRow(int index, int shift) {
        int targetRow = index + shift;
        int armyRowsSize = army.rows.size();
        if (targetRow < 0)
            targetRow += armyRowsSize;
        else if (targetRow >= armyRowsSize)
            targetRow -= armyRowsSize;

        RowForListView pop = army.rows.remove(index);
        army.rows.add(targetRow, pop);

        if (Build.VERSION.SDK_INT >= 8)
            rowBox.smoothScrollToPosition(targetRow);

        reloadRowNumbers();
    }

    public void rowUp(View view) {
        int i = (Integer) ((LinearLayout) view.getParent()).getTag();
        moveRow(i, -1);
    }

    public void rowDown(View view) {
        int i = (Integer) ((LinearLayout) view.getParent()).getTag();
        moveRow(i, 1);
    }

    private String getArmyName() {
        return editText_armyName.getText().toString();
    }

    private String getArmyString() {
        return army.toString();
    }

    public void saveExitButton(View view) {
        saveArmy();
        finish();
    }

    private void saveArmy() {
        SharedPreferences.Editor editor = prefs_armies.edit();
        String armyName = getArmyName();
        editor.putString(armyName, getArmyString());
        if (editor.commit())
            toast(String.format(getResources().getString(R.string.toast_saved), armyName));
        else
            toast(String.format(getResources().getString(R.string.toast_save_failed), armyName));
    }

    public void switchToTextMode(View view) {
        Intent intent = new Intent(this, SetupArmyStringActivity.class);
        intent.putExtra(ArmyListActivity.EXTRA_MESSAGE_ARMY_STRING, getArmyString());
        intent.putExtra(ArmyListActivity.EXTRA_MESSAGE_ARMY_NAME, getArmyName());
        finish();
        startActivity(intent);
    }
}
