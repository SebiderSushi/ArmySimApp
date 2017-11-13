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
        if (attributes.length > 1) {
            this.attack = attributes[1];
            if (attributes.length > 2) {
                this.lives = attributes[2];
                if (attributes.length > 3) {
                    this.attackSpeed = attributes[3];
                    if (attributes.length > 4) {
                        this.roundsAfterDeath = attributes[4];
                        if (attributes.length > 5) {
                            this.ATTACK_WEAKEST_ROW = (attributes[5].equals("1"));
                            if (attributes.length > 6) {
                                this.DISTANCE_DAMAGE = !(attributes[6].equals("0"));
                                if (attributes.length > 7) {
                                    this.DISTANCE_FIGHTER = (attributes[7].equals("1"));
                                    if (attributes.length > 8) {
                                        this.defense = attributes[8];
                                        if (attributes.length > 9) {
                                            this.reach = attributes[9];
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public String toString() {
        return attack + s + lives + s + attackSpeed + s + roundsAfterDeath
                + s + (ATTACK_WEAKEST_ROW ? "1" : "") + s + (DISTANCE_DAMAGE ? "" : "0")
                + s + (DISTANCE_FIGHTER ? "1" : "") + s + defense + s + reach;
    }
}
