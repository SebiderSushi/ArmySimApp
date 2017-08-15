package me.sebi.armysim;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by sebi on 15.06.17.
 */

public class SetupArmyActivity extends Activity {

    private EditText editText_armyName;
    private SharedPreferences prefs_armies;
    private ArrayList<RelativeLayout> ROWS = new ArrayList<>(0);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup_army);

        prefs_armies = this.getSharedPreferences("me.sebi.armysim.ARMIES", Context.MODE_PRIVATE);
        editText_armyName = (EditText) findViewById(R.id.armySetup_et_armyName);

        Intent intent = getIntent();
        String armyName = intent.getStringExtra(MainActivity.EXTRA_MESSAGE_ARMY_NAME);
        setTitle(armyName + getResources().getString(R.string.editMode));
        editText_armyName.setText(armyName);

        if (intent.getBooleanExtra(MainActivity.EXTRA_MESSAGE_ARMY_LOAD, false)) {
            String army = prefs_armies.getString(armyName, "");
            loadArmy(army);
        } else if (intent.getBooleanExtra(MainActivity.EXTRA_MESSAGE_ARMY_STRING_LOAD, false)) {
            String army = intent.getStringExtra(MainActivity.EXTRA_MESSAGE_ARMY_STRING);
            loadArmy(army);
        } else {
            setTitle(this.getResources().getText(R.string.create) + " (" + armyName + ")");
            int rowCount = intent.getIntExtra(MainActivity.EXTRA_MESSAGE_ARMY_ROWCOUNT, 1);
            for (int i = 0; i < rowCount; i++)
                addRow(null);
        }
    }

    public void toast(String text) {
        Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
    }

    public void addRow(View view) {
        LayoutInflater inflater;
        inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        ViewGroup rowBox = (ViewGroup) findViewById(R.id.armySetup_ll_rowBox);

        RelativeLayout row = (RelativeLayout) inflater.inflate(R.layout.element_army_row, null);
        ROWS.add(row);

        EditText rowNumber = (EditText) row.findViewById(R.id.armyRow_editText_rowNumber);
        Integer number = ROWS.size();
        rowNumber.setText(number.toString());

        setTitleRowNumber(number);

        rowBox.addView(row);
    }

    private void reloadRowNumbers() {
        for (int i = 0; i < ROWS.size(); i++) {
            RelativeLayout rowRL = ROWS.get(i); // Finally i've got some RL
            EditText number = (EditText) rowRL.findViewById(R.id.armyRow_editText_rowNumber);
            Integer numberInt = i + 1;
            String numberString = numberInt.toString();
            number.clearFocus();
            number.setText(numberString);
        }
        setTitleRowNumber(ROWS.size());
    }

    private void setTitleRowNumber(int number) {
        setTitle(getArmyName() + this.getResources().getString(R.string.editMode) + " " + number + " "
                + this.getResources().getString(R.string.row)
                + ((number != 1) ? this.getResources().getString(R.string.echo_win_plural) : ""));
    }

    public void deleteRow(View view) {
        RelativeLayout row = (RelativeLayout) view.getParent().getParent();
        LinearLayout rowBox = (LinearLayout) row.getParent();
        rowBox.removeView(row);
        ROWS.remove(row);
        reloadRowNumbers();
    }

    public void moveToRowButton(View view) {
        RelativeLayout row = (RelativeLayout) view.getParent().getParent();
        EditText rowNumberView = (EditText) row.findViewById(R.id.armyRow_editText_rowNumber);
        moveToRow(row, Integer.parseInt(rowNumberView.getText().toString()));
    }

    private void moveToRow(RelativeLayout row, int targetRow) {
        EditText et_rowNumber = (EditText) row.findViewById(R.id.armyRow_editText_rowNumber);
        LinearLayout rowBox = (LinearLayout) row.getParent();
        ScrollView sv = (ScrollView) rowBox.getParent();

        targetRow--; //since we count from zero here and from 1 in editText_armyString

        if (targetRow >= (0 - ROWS.size()) && targetRow < ROWS.size() && targetRow != 0) {
            if (targetRow < 0)
                targetRow += ROWS.size();
            else if (targetRow >= ROWS.size())
                targetRow -= ROWS.size();
            ROWS.remove(row);
            ROWS.add(targetRow, row);

            rowBox.removeView(row);
            rowBox.addView(row, targetRow);

            reloadRowNumbers();
            sv.scrollTo(0, targetRow * row.getHeight());
            et_rowNumber.requestFocus();
        } else
            toast(this.getResources().getText(R.string.indexOutOfRange) + Integer.toString(targetRow + 1));

    }

    private void moveRow(View view, int shift) {
        RelativeLayout row = (RelativeLayout) view.getParent().getParent();
        LinearLayout rowBox = (LinearLayout) row.getParent();
        ScrollView sv = (ScrollView) rowBox.getParent();

        int rowHeight = row.getHeight();
        int rowBoxHeight = rowBox.getHeight(); //since rowBox is the only child of the ScrollView they share heights
        int y = sv.getScrollY() + shift * rowHeight;
        int number = rowBox.indexOfChild(row) + shift;

        if (number < 0) {
            number += ROWS.size();
            y += rowBoxHeight;
        } else if (number >= ROWS.size()) {
            number -= ROWS.size();
            y -= rowBoxHeight;
        }

        ROWS.remove(row);
        ROWS.add(number, row);

        rowBox.removeView(row);
        rowBox.addView(row, number);

        reloadRowNumbers();
        sv.scrollTo(0, y);
    }

    public void rowUp(View view) {
        moveRow(view, -1);
    }

    public void rowDown(View view) {
        moveRow(view, 1);
    }

    private void loadArmy(String armyString) {
        if (armyString != null && !armyString.equals("")) {
            ROWS = new ArrayList<>(0);

            String[] rowStrings = armyString.replace("\n", "").split(";");

            for (int i = 0; i < rowStrings.length; i++) {
                String[] attributes = rowStrings[i].split(",");
                addRow(null);
                RelativeLayout rowRL = ROWS.get(i);
                EditText attack = (EditText) rowRL.findViewById(R.id.editText_attack);
                EditText defense = (EditText) rowRL.findViewById(R.id.editText_defense);
                EditText attackSpeed = (EditText) rowRL.findViewById(R.id.editText_attackSpeed);
                EditText roundsAfterDeath = (EditText) rowRL.findViewById(R.id.editText_roundsAfterDeath);
                CheckBox attackWeakest = (CheckBox) rowRL.findViewById(R.id.checkbox_atkWeakest);
                CheckBox distanceDamage = (CheckBox) rowRL.findViewById(R.id.checkbox_DistanceDamage);
                CheckBox distanceFighter = (CheckBox) rowRL.findViewById(R.id.checkbox_DistanceFighter);
                if (attributes.length > 1)
                    attack.setText(attributes[1]);
                if (attributes.length > 2)
                    defense.setText(attributes[2]);
                if (attributes.length > 3)
                    attackSpeed.setText(attributes[3]);
                if (attributes.length > 4)
                    roundsAfterDeath.setText(attributes[4]);
                if (attributes.length > 5)
                    attackWeakest.setChecked(attributes[5].equals("1"));
                if (attributes.length > 6)
                    distanceDamage.setChecked(!attributes[6].equals("0"));
                if (attributes.length > 7)
                    distanceFighter.setChecked(attributes[7].equals("1"));
            }
        }
    }

    private String getArmyName() {
        return editText_armyName.getText().toString();
    }

    private String getArmyString() {
        String armyString = "";
        int rowsSize = ROWS.size();

        for (int i = 0; i < rowsSize; i++) {
            RelativeLayout row = ROWS.get(i);
            String attack = ((EditText) row.findViewById(R.id.editText_attack)).getText().toString();
            String defense = ((EditText) row.findViewById(R.id.editText_defense)).getText().toString();
            String attackSpeed = ((EditText) row.findViewById(R.id.editText_attackSpeed)).getText().toString();
            String roundsAfterDeath = ((EditText) row.findViewById(R.id.editText_roundsAfterDeath)).getText().toString();
            String attackWeakest = ((CheckBox) row.findViewById(R.id.checkbox_atkWeakest)).isChecked() ? "1" : "0";
            String distanceDamage = ((CheckBox) row.findViewById(R.id.checkbox_DistanceDamage)).isChecked() ? "1" : "0";
            String distanceFighter = ((CheckBox) row.findViewById(R.id.checkbox_DistanceFighter)).isChecked() ? "1" : "0";
            armyString = armyString + (i + 1) + "," + attack + "," + defense + "," + attackSpeed + ","
                    + roundsAfterDeath + "," + attackWeakest + "," + distanceDamage + "," + distanceFighter + ";" + "\n";
        }

        return rowsSize >= 1 ? armyString.substring(0, armyString.length() - 1) : "";
    }

    public void saveButton(View view) {
        saveArmy();
    }

    public void saveExitButton(View view) {
        saveArmy();
        finish();
    }

    private void saveArmy() {
        SharedPreferences sharedPrefs = this.getSharedPreferences("me.sebi.armysim.ARMIES", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPrefs.edit();
        String armyName = getArmyName();
        editor.putString(armyName, getArmyString());
        if (editor.commit()) {
            toast(armyName + getResources().getString(R.string.toast_saved));
        } else {
            toast(armyName + getResources().getString(R.string.toast_save_failed));
        }
    }

    public void switchToTextMode(View view) {
        Intent intent = new Intent(this, SetupArmyStringActivity.class);
        intent.putExtra(MainActivity.EXTRA_MESSAGE_ARMY_STRING, getArmyString());
        intent.putExtra(MainActivity.EXTRA_MESSAGE_ARMY_NAME, getArmyName());
        finish();
        startActivity(intent);
    }
}
