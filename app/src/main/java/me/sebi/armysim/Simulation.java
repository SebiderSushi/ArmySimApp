package me.sebi.armysim;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by sebi on 16.04.17.
 */
class Simulation {

    final ArrayList<Army> armies_orig = new ArrayList<>();
    final Counter counter;
    final ArrayList<Army> armies = new ArrayList<>();
    boolean useRandom;
    int round;
    private ArrayList<ArrayList<Army>> sortedArmies = new ArrayList<>(0);

    Simulation(Counter counter) {
        if (counter == null)
            this.counter = new Counter();
        else
            this.counter = counter;
    }

    private boolean gameOver() {
        if (armies.size() == 0) {
            counter.ties++;
            return true;
        }
        if (armies.size() == 1) {
            counter.armyWins.put(armies.get(0).name, counter.armyWins.get(armies.get(0).name) + 1);
            return true;
        }
        return false;
    }

    private void rmAllDead(int round) {
        Iterator<Army> armyIterator = armies.iterator();
        while (armyIterator.hasNext()) {
            Army army = armyIterator.next();
            army.rmDead(round);
            if (army.rows.size() == 0) {
                armyIterator.remove();
                for (ArrayList<Army> armies : army.containingSimulation.sortedArmies)
                    armies.remove(army);
            }
        }
    }

    private ArrayList<ArrayList<Army>> armiesSortedBySpeed() {
        Map<Integer, ArrayList<Army>> speedclasses = new HashMap<>();
        ArrayList<Integer> keys = new ArrayList<>();
        for (Army army : this.armies) {
            Row row = army.rows.get(0);
            if (speedclasses.containsKey(row.attackSpeed))
                speedclasses.get(row.attackSpeed).add(army);
            else {
                speedclasses.put(row.attackSpeed, new ArrayList<>(Collections.singletonList(army)));
                keys.add(row.attackSpeed);
            }
        }
        Collections.sort(keys, Collections.reverseOrder());
        ArrayList<ArrayList<Army>> sortedSpeedClasses = new ArrayList<>();
        for (int key : keys)
            sortedSpeedClasses.add(speedclasses.get(key));
        return sortedSpeedClasses;
    }

    void simulate(SimulationAsyncTask simulationAsyncTask) {
        reset();
        counter.total++;
        round = 0;
        //Remove Rows already defined with 0 LP
        rmAllDead(round);
        if (gameOver())
            return;
        while (!simulationAsyncTask.isCancelled()) {
            round += 1;
            for (Army attacker : armies)
                for (Army enemy : armies)
                    if (attacker != enemy)
                        attacker.distanceAttack(enemy);
            rmAllDead(round);
            //Here iteration so armies attack in best order.
            //When a group of peers has attacked, all dead rows are removed.
            //This way the best armies attack first and equally fast armies attack
            //each other simultaneously
            sortedArmies = this.armiesSortedBySpeed();
            for (ArrayList<Army> attackingArmies : sortedArmies) {
                for (Army attacker : attackingArmies)
                    for (ArrayList<Army> enemyArmies : sortedArmies)
                        for (Army enemy : enemyArmies)
                            if (attacker != enemy)
                                attacker.attack(enemy);
                rmAllDead(round);
                if (gameOver())
                    return;
            }
        }
    }

    private void reset() {
        armies.clear();
        armies.addAll(armies_orig);
        for (Army army : armies_orig)
            army.reset();
    }
}
