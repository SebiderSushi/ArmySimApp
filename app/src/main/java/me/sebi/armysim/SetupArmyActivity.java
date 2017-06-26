package me.sebi.armysim;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
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

public class SetupArmyActivity extends AppCompatActivity {

    private String armyName;
    private SharedPreferences sharedPrefs;
    private ArrayList<RelativeLayout> ROWS = new ArrayList<>(0);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup_army);

        sharedPrefs = this.getSharedPreferences("me.sebi.armysim.ARMIES", Context.MODE_PRIVATE);

        Intent intent = getIntent();
        armyName = intent.getStringExtra(MainActivity.EXTRA_MESSAGE_ARMY_NAME);

        if (intent.getBooleanExtra(MainActivity.EXTRA_MESSAGE_ARMY_LOAD, false)) {
            String army = sharedPrefs.getString(armyName, this.getResources().getString(R.string.namelessArmy));
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

        ViewGroup rowBox = (ViewGroup) findViewById(R.id.ll_rowBox);

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
            String numberSring = numberInt.toString();
            number.clearFocus();
            number.setText(numberSring);
        }
        setTitleRowNumber(ROWS.size());
    }

    private void setTitleRowNumber(int number) {
        setTitle(armyName + this.getResources().getString(R.string.editMode) + " " + number + " "
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
            Toast.makeText(getApplicationContext(),
                    this.getResources().getText(R.string.indexOutOfRange) + Integer.toString(targetRow + 1), Toast.LENGTH_LONG).show();

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
        ROWS = new ArrayList<>(0);

        String[] string = armyString.replace("\n", "").split(";");

        setTitle(string[0] + getResources().getString(R.string.editMode));

        for (int i = 1; i < string.length; i++) {
            addRow(null);
            RelativeLayout rowRL = ROWS.get(i - 1);
            EditText attack = (EditText) rowRL.findViewById(R.id.editText_attack);
            EditText defense = (EditText) rowRL.findViewById(R.id.editText_defense);
            EditText attackSpeed = (EditText) rowRL.findViewById(R.id.editText_attackSpeed);
            EditText roundsAfterDeath = (EditText) rowRL.findViewById(R.id.editText_roundsAfterDeath);
            CheckBox attackWeakest = (CheckBox) rowRL.findViewById(R.id.checkbox_atkWeakest);
            CheckBox distanceDamage = (CheckBox) rowRL.findViewById(R.id.checkbox_DistanceDamage);
            String[] strings = string[i].split(",");
            if (strings.length >= 7) {
                attack.setText(strings[1]);
                defense.setText(strings[2]);
                attackSpeed.setText(strings[3]);
                roundsAfterDeath.setText(strings[4]);
                attackWeakest.setChecked(strings[5].equals("1"));
                distanceDamage.setChecked(strings[6].equals("1"));
            }
        }
    }

    private String getArmyString() {
        String armyString = "\n" + armyName + ";";

        for (int i = 0; i < ROWS.size(); i++) {
            RelativeLayout row = ROWS.get(i);
            String attack = ((EditText) row.findViewById(R.id.editText_attack)).getText().toString();
            String defense = ((EditText) row.findViewById(R.id.editText_defense)).getText().toString();
            String attackSpeed = ((EditText) row.findViewById(R.id.editText_attackSpeed)).getText().toString();
            String roundsAfterDeath = ((EditText) row.findViewById(R.id.editText_roundsAfterDeath)).getText().toString();
            int attackWeakest = ((CheckBox) row.findViewById(R.id.checkbox_atkWeakest)).isChecked() ? 1 : 0;
            int distanceDamage = ((CheckBox) row.findViewById(R.id.checkbox_DistanceDamage)).isChecked() ? 1 : 0;
            armyString = armyString + "\n" + (i + 1) + "," + attack + "," + defense + "," + attackSpeed + "," + roundsAfterDeath + "," + attackWeakest + "," + distanceDamage + ";";
        }

        return armyString;
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
        editor.putString(armyName, getArmyString());
        if (editor.commit()) {
            toast(armyName + getResources().getString(R.string.toast_saved));
        } else {
            toast(armyName + getResources().getString(R.string.toast_save_failed));
        }
    }

    public void switchToTextMode(View view) {
        String armyString = getArmyString();
        Intent intent = new Intent(this, SetupArmyStringActivity.class);
        intent.putExtra(MainActivity.EXTRA_MESSAGE_ARMY_STRING, armyString);
        intent.putExtra(MainActivity.EXTRA_MESSAGE_ARMY_NAME, armyName);
        finish();
        startActivity(intent);
    }
}
