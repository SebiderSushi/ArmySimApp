package me.sebi.armysim;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import java.util.List;
import java.util.Locale;

/**
 * Created by sebi on 20.10.17.
 */

class SetupArmyAdapter extends BaseAdapter {
    private LayoutInflater inflater;
    private List<RowForListView> rows;

    SetupArmyAdapter(Context context, List<RowForListView> rows) {
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.rows = rows;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewGroup rowView;
        boolean initViews = false;
        if (view == null) {
            rowView = (LinearLayout) inflater.inflate(R.layout.element_army_row, null);
            initViews = true;
        } else
            rowView = (LinearLayout) view;

        RowForListView row = rows.get(i);

        EditText number = rowView.findViewById(R.id.armyRow_editText_rowNumber);
        EditText attack = rowView.findViewById(R.id.editText_attack);
        EditText lives = rowView.findViewById(R.id.editText_lives);
        EditText attackSpeed = rowView.findViewById(R.id.editText_attackSpeed);
        EditText roundsAfterDeath = rowView.findViewById(R.id.editText_roundsAfterDeath);
        EditText defense = rowView.findViewById(R.id.editText_defense);
        EditText reach = rowView.findViewById(R.id.editText_reach);

        CheckBox attackWeakest = rowView.findViewById(R.id.checkbox_atkWeakest);
        CheckBox distanceDamage = rowView.findViewById(R.id.checkbox_DistanceDamage);
        CheckBox distanceFighter = rowView.findViewById(R.id.checkbox_DistanceFighter);

        Button delete = rowView.findViewById(R.id.button_deleteRow);
        ((LinearLayout) delete.getParent()).setTag(i);

        EditText[] ets = {attack, attackSpeed, defense, lives, reach, roundsAfterDeath};
        CheckBox[] cbs = {attackWeakest, distanceDamage, distanceFighter};

        if (initViews) {
            for (EditText et : ets) {
                ELVATextWatcher textWatcher = new ELVATextWatcher(et);
                et.addTextChangedListener(textWatcher);
                et.setTag(R.id.tag_ELVATextWatcher, textWatcher);

                et.setHint(String.format(Locale.getDefault(), "%d", 0));
            }
            for (CheckBox cb : cbs) {
                ELVACBOnClickListener onClickListener = new ELVACBOnClickListener(cb);
                cb.setOnClickListener(onClickListener);
                cb.setTag(R.id.tag_ELVACBOnClickListener, onClickListener);
            }
        }

        for (EditText et : ets)
            ((ELVATextWatcher) et.getTag(R.id.tag_ELVATextWatcher)).disable();
        for (CheckBox cb : cbs)
            ((ELVACBOnClickListener) cb.getTag(R.id.tag_ELVACBOnClickListener)).disable();

        number.setText(String.format(Locale.getDefault(), "%d", i + 1));
        attack.setText(row.attack);
        lives.setText(row.lives);
        attackSpeed.setText(row.attackSpeed);
        roundsAfterDeath.setText(row.roundsAfterDeath);
        defense.setText(row.defense);
        reach.setText(row.reach);

        attackWeakest.setChecked(row.ATTACK_WEAKEST_ROW);
        distanceDamage.setChecked(row.DISTANCE_DAMAGE);
        distanceFighter.setChecked(row.DISTANCE_FIGHTER);

        LinearLayout ll_reach = (LinearLayout) reach.getParent();
        if (row.DISTANCE_FIGHTER)
            ll_reach.setVisibility(View.VISIBLE);
        else
            ll_reach.setVisibility(View.GONE);

        for (EditText et : ets)
            ((ELVATextWatcher) et.getTag(R.id.tag_ELVATextWatcher)).update(row);
        for (CheckBox cb : cbs)
            ((ELVACBOnClickListener) cb.getTag(R.id.tag_ELVACBOnClickListener)).update(row);

        return rowView;
    }

    @Override
    public RowForListView getItem(int i) {
        return rows.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public int getCount() {
        return rows.size();
    }
}

class ELVACBOnClickListener implements CheckBox.OnClickListener {
    private CheckBox cb;
    private RowForListView row;
    private boolean enabled;

    ELVACBOnClickListener(CheckBox cb) {
        this.cb = cb;
    }

    void disable() {
        enabled = false;
    }

    void update(RowForListView row) {
        this.row = row;
        this.enabled = true;
    }

    @Override
    public void onClick(View view) {
        if (enabled)
            switch (cb.getId()) {
                case R.id.checkbox_atkWeakest:
                    row.ATTACK_WEAKEST_ROW = cb.isChecked();
                    break;
                case R.id.checkbox_DistanceDamage:
                    row.DISTANCE_DAMAGE = cb.isChecked();
                    break;
                case R.id.checkbox_DistanceFighter:
                    ViewGroup container = (ViewGroup) cb.getParent();
                    LinearLayout ll_reach = (LinearLayout) container.findViewById(R.id.editText_reach).getParent();
                    if (cb.isChecked()) {
                        row.DISTANCE_FIGHTER = true;
                        ll_reach.setVisibility(View.VISIBLE);
                    } else {
                        row.DISTANCE_FIGHTER = false;
                        ll_reach.setVisibility(View.GONE);
                    }
                    break;
            }
    }
}

class ELVATextWatcher implements TextWatcher {
    private RowForListView row;
    private EditText et;
    private boolean enabled;

    ELVATextWatcher(EditText et) {
        this.et = et;
    }

    void disable() {
        enabled = false;
    }

    void update(RowForListView r) {
        this.row = r;
        this.enabled = true;
    }

    @Override
    public void afterTextChanged(Editable editable) {
        if (enabled)
            switch (et.getId()) {
                case (R.id.editText_attack):
                    row.attack = editable.toString();
                    break;
                case (R.id.editText_lives):
                    row.lives = editable.toString();
                    break;
                case (R.id.editText_attackSpeed):
                    row.attackSpeed = editable.toString();
                    break;
                case (R.id.editText_roundsAfterDeath):
                    row.roundsAfterDeath = editable.toString();
                    break;
                case (R.id.editText_defense):
                    row.defense = editable.toString();
                    break;
                case (R.id.editText_reach):
                    row.reach = editable.toString();
                    break;
            }
    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
    }
}