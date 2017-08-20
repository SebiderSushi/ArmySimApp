package me.sebi.armysim;

import java.util.ArrayList;

/**
 * Created by sebi on 16.04.17.
 */
class Army {

    final ArrayList<Row> rows = new ArrayList<>();
    final String name;
    private final ArrayList<Row> distanceFighterRows = new ArrayList<>();
    private final Simulation containingSimulation;

    Army(String name, Simulation containingSimulation) {
        this.name = name;
        this.containingSimulation = containingSimulation;
        this.containingSimulation.armies.add(this);
    }

    void addRow(Row row) {
        if (row.DISTANCE_FIGHTER) //&& rows.size() > 1)

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
        } //else if (this.distanceFighterRows.size() < 0 && this.distanceFighterRows.get(0) == this.rows.get(0))
          //  this.distanceFighterRows.remove(0);
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
        Row attackedRow = enemy.rows.get(0);
        int aAtk = this.rows.get(0).attack;
        int bDef = attackedRow.defense;
        if (this.containingSimulation.useRandom) {
            aAtk *= 1 + 0.5 * Math.random();
            bDef *= 1 + 0.5 * Math.random();
        }
        attackedRow.lives -= aAtk - bDef;
    }

    void distanceAttack(Army enemy) {
        Row firstRow = this.rows.get(0);
        int enemyRowCount = enemy.rows.size();
        for (Row distanceFighter : this.distanceFighterRows) {
            if (distanceFighter != firstRow) {
                int aAtk;
                int bDef;
                Row attackedRow;
                if (distanceFighter.ATTACK_WEAKEST_ROW)
                    attackedRow = enemy.weakestRow();
                else {
                    int attackedRowIndex = (int) (distanceFighter.reach + 5 * Math.random());
                    if (attackedRowIndex < enemyRowCount) {
                        if (attackedRowIndex < 0)
                            attackedRow = enemy.rows.get(0);
                        else
                            attackedRow = enemy.rows.get(attackedRowIndex);
                    } else
                        attackedRow = enemy.rows.get(enemyRowCount - 1);
                }
                aAtk = attackedRow.DISTANCE_DAMAGE ? distanceFighter.attack : 0;
                bDef = attackedRow.defense;
                if (this.containingSimulation.useRandom) {
                    aAtk *= 1 + 0.5 * Math.random();
                    bDef *= 1 + 0.5 * Math.random();
                }
                attackedRow.lives -= aAtk - bDef;
            }
        }
    }
}
