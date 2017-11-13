package me.sebi.armysim;

/**
 * Created by sebi on 20.10.17.
 */

class ArmyListViewEntryData {
    String armyName;
    boolean checked = false;

    ArmyListViewEntryData(String armyName) {
        this.armyName = armyName;
    }

    ArmyListViewEntryData(String armyName, boolean checked) {
        this(armyName);
        this.checked = checked;
    }
}
