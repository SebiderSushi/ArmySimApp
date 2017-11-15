package me.sebi.armysim;

/**
 * Created by sebi on 09.11.17.
 */

public class RowForListView {
    String s = ",";
    String attack, lives, attackSpeed, roundsAfterDeath, defense, reach;
    boolean ATTACK_WEAKEST_ROW, DISTANCE_FIGHTER = false;
    boolean DISTANCE_DAMAGE = true;

    RowForListView() {
        attack = lives = attackSpeed = roundsAfterDeath = defense = reach = "";
    }

    RowForListView(String rowString) {
        attack = lives = attackSpeed = roundsAfterDeath = defense = reach = "";
        String[] attributes = rowString.split(s);
        int length = attributes.length;
        if (length > 9)
            length = 10;
        switch (length) {
            case 10:
                this.reach = attributes[9];
            case 9:
                this.defense = attributes[8];
            case 8:
                this.DISTANCE_FIGHTER = attributes[7].equals("1");
            case 7:
                this.DISTANCE_DAMAGE = !attributes[6].equals("0");
            case 6:
                this.ATTACK_WEAKEST_ROW = attributes[5].equals("1");
            case 5:
                this.roundsAfterDeath = attributes[4];
            case 4:
                this.attackSpeed = attributes[3];
            case 3:
                this.lives = attributes[2];
            case 2:
                this.attack = attributes[1];
        }
    }

    @Override
    public String toString() {
        return attack + s + lives + s + attackSpeed + s + roundsAfterDeath
                + s + (ATTACK_WEAKEST_ROW ? "1" : "") + s + (DISTANCE_DAMAGE ? "" : "0")
                + s + (DISTANCE_FIGHTER ? "1" : "") + s + defense + s + reach;
    }
}
