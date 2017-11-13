package me.sebi.armysim;

import java.util.ArrayList;

/**
 * Created by sebi on 09.11.17.
 */

public class ArmyForListView {
    final ArrayList<RowForListView> rows = new ArrayList<>();

    ArmyForListView() {
    }

    ArmyForListView(String armyString) {
        String[] armyRows = armyString.replace("\n", "").split(";");
        for (String rowString : armyRows) {
            this.rows.add(new RowForListView(rowString));
        }
    }

    @Override
    public String toString() {
        if (rows.size() > 0) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < rows.size() - 1; i++) {
                sb.append(i + 1);
                sb.append(",");
                sb.append(rows.get(i));
                sb.append(";\n");
            }
            int i = (rows.size() - 1);
            sb.append(i + 1);
            sb.append(",");
            sb.append(rows.get(i));
            sb.append(";");
            return sb.toString();
        }
        return "";
    }
}
