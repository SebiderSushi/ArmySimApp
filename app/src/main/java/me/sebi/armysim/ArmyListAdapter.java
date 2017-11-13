package me.sebi.armysim;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

/**
 * Created by sebi on 20.10.17.
 */

class ArmyListAdapter extends BaseAdapter {
    private LayoutInflater inflater;
    private List<ArmyListViewEntryData> armyList;

    ArmyListAdapter(Context context, List<ArmyListViewEntryData> armyList) {
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.armyList = armyList;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewGroup row;
        if (view == null) {
            row = (ViewGroup) inflater.inflate(R.layout.element_listview_army, null);
        } else {
            row = (ViewGroup) view;
        }

        ArmyListViewEntryData data = armyList.get(i);
        TextView armyNameView = row.findViewById(R.id.textView_listViewElem_armyName);
        CheckBox checkBox = row.findViewById(R.id.checkbox_listViewElem);
        armyNameView.setText(data.armyName);
        checkBox.setChecked(data.checked);
        checkBox.setTag(data);
        checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ArmyListViewEntryData data = (ArmyListViewEntryData) view.getTag();
                data.checked = ((CheckBox) view).isChecked();
            }
        });

        return row;
    }

    @Override
    public ArmyListViewEntryData getItem(int i) {
        return armyList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public int getCount() {
        return armyList.size();
    }
}
