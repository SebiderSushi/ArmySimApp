package me.sebi.armysim;

import java.util.Comparator;

/**
 * Created by sebi on 20.10.17.
 */

class ArmyListSortByNameComparator implements Comparator<ArmyListViewEntryData> {
    @Override
    public int compare(ArmyListViewEntryData data0, ArmyListViewEntryData data1) {
        return data0.armyName.compareTo(data1.armyName);
    }
}
