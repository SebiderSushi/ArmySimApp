package me.sebi.armysim;

/**
 * Created by sebi on 16.04.17.
 */
class Row {

    final int attack;
    int lives;
    int deathRound;
    final int roundsAfterDeath;
    final int attackSpeed;
    boolean ATTACK_WEAKEST_ROW = false;
    boolean DISTANCE_DAMAGE = true;
    boolean DISTANCE_FIGHTER = false;

    /**
     * The constructor we'll be using in our App
     */
    Row(int attack, int lives, int attackSpeed, int roundsAfterDeath, boolean ATTACK_WEAKEST_ROW, boolean DISTANCE_DAMAGE, boolean DISTANCE_FIGHTER) {
        this.attack = attack;
        this.lives = lives;
        this.attackSpeed = attackSpeed;
        this.roundsAfterDeath = roundsAfterDeath;
        this.ATTACK_WEAKEST_ROW = ATTACK_WEAKEST_ROW;
        this.DISTANCE_DAMAGE = DISTANCE_DAMAGE;
        this.DISTANCE_FIGHTER = DISTANCE_FIGHTER;
    }

    /*
    Row(int attack, int lives) {
        this.attack = attack;
        this.lives = lives;
        this.attackSpeed = 0;
    }

    Row(int attack, int lives, String[] effects) {
        this(attack, lives);
        if (effects != null)
            for (String effect : effects)
                switch (effect) {
                    case "NO_DISTANCE_DAMAGE":
                        this.DISTANCE_DAMAGE = false;
                    case "ATTACK_WEAKEST":
                        this.ATTACK_WEAKEST_ROW = true;
                }
    }

    Row(int attack, int lives, int attackSpeed) {
        this(attack, lives);
        this.attackSpeed = attackSpeed;
    }

    Row(int attack, int lives, int attackSpeed, String[] effects) {
        this(attack, lives, effects);
        this.attackSpeed = attackSpeed;
    }
    */
}
