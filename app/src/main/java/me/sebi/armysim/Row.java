package me.sebi.armysim;

/**
 * Created by sebi on 16.04.17.
 */
class Row {

    int attack, defense, roundsAfterDeath, attackSpeed, reach;
    int lives, deathRound;
    boolean ATTACK_WEAKEST_ROW, DISTANCE_FIGHTER, DISTANCE_DAMAGE;
    private int lives_orig;

    Row() {
        attack = defense = roundsAfterDeath = attackSpeed = reach = lives = deathRound = lives_orig = 0;
        ATTACK_WEAKEST_ROW = DISTANCE_FIGHTER = false;
        DISTANCE_DAMAGE = true;
    }

    Row(String rowString) {
        this();
        String[] attributes = rowString.split(",");
        int length = attributes.length;
        if (length > 9)
            length = 10;
        switch (length) {
            case 10:
                if (!attributes[9].equals(""))
                    this.reach = Integer.parseInt(attributes[9]);
            case 9:
                if (!attributes[8].equals(""))
                    this.defense = Integer.parseInt(attributes[8]);
            case 8:
                this.DISTANCE_FIGHTER = attributes[7].equals("1");
            case 7:
                this.DISTANCE_DAMAGE = !attributes[6].equals("0");
            case 6:
                this.ATTACK_WEAKEST_ROW = attributes[5].equals("1");
            case 5:
                if (!attributes[4].equals(""))
                    this.roundsAfterDeath = Integer.parseInt(attributes[4]);
            case 4:
                if (!attributes[3].equals(""))
                    this.attackSpeed = Integer.parseInt(attributes[3]);
            case 3:
                if (!attributes[2].equals(""))
                    this.lives_orig = Integer.parseInt(attributes[2]);
            case 2:
                if (!attributes[1].equals(""))
                    this.attack = Integer.parseInt(attributes[1]);
        }
    }

    void reset() {
        lives = lives_orig;
        deathRound = 0;
    }
}
