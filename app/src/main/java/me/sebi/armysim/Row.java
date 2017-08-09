package me.sebi.armysim;

/**
 * Created by sebi on 16.04.17.
 */
class Row {

    final int attack, defense, roundsAfterDeath, attackSpeed, reach;
    int lives, deathRound;
    final boolean ATTACK_WEAKEST_ROW, DISTANCE_FIGHTER, DISTANCE_DAMAGE;

    Row(int attack, int lives, int defense, int attackSpeed, int roundsAfterDeath,
        boolean ATTACK_WEAKEST_ROW, boolean DISTANCE_DAMAGE, boolean DISTANCE_FIGHTER, int reach) {
        this.attack = attack;
        this.lives = lives;
        this.defense = defense;
        this.attackSpeed = attackSpeed;
        this.roundsAfterDeath = roundsAfterDeath;
        this.ATTACK_WEAKEST_ROW = ATTACK_WEAKEST_ROW;
        this.DISTANCE_DAMAGE = DISTANCE_DAMAGE;
        this.DISTANCE_FIGHTER = DISTANCE_FIGHTER;
        this.reach = reach;
    }
}
