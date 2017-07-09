package me.sebi.armysim;

import java.util.ArrayList;

/**
 * Created by sebi on 16.04.17.
 */
public class Army {

    final ArrayList<Row> rows = new ArrayList<>();
    private final ArrayList<Row> distanceFighterRows = new ArrayList<>();
    final String name;
    private final Simulation containingSimulation;

    Army(String name, Simulation containingSimulation) {
        this.name = name;
        this.containingSimulation = containingSimulation;
        this.containingSimulation.armies.add(this);
    }

    void addRow(Row row) {
        if (row.DISTANCE_FIGHTER)
            this.distanceFighterRows.add(row);
        this.rows.add(row);
    }

    void rmDead(int round) {
        ArrayList<Row> deadRows = new ArrayList<>();
        for (Row row : this.rows) {
            if (row.lives <= 0)
                if (row.roundsAfterDeath > 0) {
                    if (row.deathRound == 0)
                        row.deathRound = round;
                    else if (round - row.deathRound >= row.roundsAfterDeath)
                        deadRows.add(row);
                } else deadRows.add(row);
        }
        for (Row deadRow : deadRows) {
            this.rows.remove(deadRow);
            this.distanceFighterRows.remove(deadRow);
        }
        if (this.rows.size() == 0) {
            this.containingSimulation.armies.remove(this);
            for (ArrayList<Army> armies : this.containingSimulation.sortedArmies)
                armies.remove(this);
        }
    }

    private Row weakestRow() {
        int weakestNess = this.rows.get(0).lives;
        int weakestI = 0;
        for (int i = 0; i < this.rows.size(); i++) {
            int lives = this.rows.get(i).lives;
            if (0 < lives && lives < weakestNess) {
                weakestI = i;
                weakestNess = lives;
            }
        }
        return this.rows.get(weakestI);
    }

    void attack(Army enemy) {
        Row attackedRow = this.rows.get(0).ATTACK_WEAKEST_ROW ? enemy.weakestRow() : enemy.rows.get(0);
        int aAtk = this.rows.get(0).attack;
        for (Row distanceFighter : distanceFighterRows)
            aAtk += attackedRow.DISTANCE_DAMAGE ? distanceFighter.attack : 0;
        int bDef = attackedRow.lives;
        if (this.containingSimulation.useRandom) {
            aAtk *= 1 + 0.5 * Math.random();
            bDef *= 1 + 0.5 * Math.random();
        }
        attackedRow.lives = bDef - aAtk;
    }
}
