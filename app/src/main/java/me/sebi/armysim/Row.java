package me.sebi.armysim;

/**
 * Created by sebi on 16.04.17.
 */
class Row {

    int attack, defense, roundsAfterDeath, attackSpeed, reach = 0;
    int lives, deathRound = 0;
    int lives_orig = 0;
    boolean ATTACK_WEAKEST_ROW, DISTANCE_FIGHTER = false;
    boolean DISTANCE_DAMAGE = true;

    Row() {}

    Row(int attack, int lives, int defense, int attackSpeed, int roundsAfterDeath,
        boolean ATTACK_WEAKEST_ROW, boolean DISTANCE_DAMAGE, boolean DISTANCE_FIGHTER, int reach) {
        this.attack = attack;
        this.lives_orig = lives;
        this.defense = defense;
        this.attackSpeed = attackSpeed;
        this.roundsAfterDeath = roundsAfterDeath;
        this.ATTACK_WEAKEST_ROW = ATTACK_WEAKEST_ROW;
        this.DISTANCE_DAMAGE = DISTANCE_DAMAGE;
        this.DISTANCE_FIGHTER = DISTANCE_FIGHTER;
        this.reach = reach;
    }

    Row(String rowString) {
        String[] attributes = rowString.split(",");
        this.attack = (attributes.length > 1) && !(attributes[1].equals("")) ? Integer.parseInt(attributes[1]) : 0;
        this.lives_orig = (attributes.length > 2) && !(attributes[2].equals("")) ? Integer.parseInt(attributes[2]) : 0;
        this.attackSpeed = (attributes.length > 3) && !(attributes[3].equals("")) ? Integer.parseInt(attributes[3]) : 0;
        this.roundsAfterDeath = (attributes.length > 4) && !(attributes[4].equals("")) ? Integer.parseInt(attributes[4]) : 0;
        this.ATTACK_WEAKEST_ROW = (attributes.length > 5) && (attributes[5].equals("1"));
        this.DISTANCE_DAMAGE = (attributes.length > 6) && !(attributes[6].equals("0"));
        this.DISTANCE_FIGHTER = (attributes.length > 7) && (attributes[7].equals("1"));
        this.defense = (attributes.length > 8) && !(attributes[8].equals("")) ? Integer.parseInt(attributes[8]) : 0;
        this.reach = (attributes.length > 9) && !(attributes[9].equals("")) ? Integer.parseInt(attributes[9]) : 0;
    }

    void reset() {
        lives = lives_orig;
        deathRound = 0;
    }
}
