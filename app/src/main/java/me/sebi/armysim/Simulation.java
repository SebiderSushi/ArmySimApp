package me.sebi.armysim;

import android.content.Context;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by sebi on 16.04.17.
 */
class Simulation {

    private final ArrayList<Army> armies = new ArrayList<>();
    final ArrayList<Army> armies_orig = new ArrayList<>();
    boolean useRandom;
    private final LinearLayout echoView;
    private final Context echoContext;
    private final Counter counter;
    private final LinearLayout.LayoutParams echoParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

    private ArrayList<ArrayList<Army>> sortedArmies = new ArrayList<>(0);

    Simulation(Counter counter, Context echoContext, LinearLayout echoView) {
        this.counter = counter;
        this.echoView = echoView;
        this.echoContext = echoContext;
    }

    private void echo(String text) {
        TextView echo = new TextView(echoContext);
        echo.setText(text);
        echo.setLayoutParams(echoParams);
        if (echoView.getChildCount() >= 1000)
            echoView.removeViewAt(999);
        echoView.addView(echo, 0);
    }

    private boolean gameOver(int round) {
        if (armies.size() == 0) {
            counter.ties++;
            echo(echoContext.getResources().getString(R.string.echo_tie) + " (" + counter.ties + ")");
            return true;
        }
        if (armies.size() == 1) {
            int rowsLeft = armies.get(0).rows.size();
            String name = armies.get(0).name;
            Integer wins = counter.armyWins.get(name);
            if (wins == null) {
                counter.armyWins.put(name, 1);
                wins = 1;
            } else
                counter.armyWins.put(name, ++wins);
            echo(name + echoContext.getResources().getString(R.string.echo_win_winInRound)
                    + round + echoContext.getResources().getString(R.string.echo_win_w) + rowsLeft
                    + echoContext.getResources().getString(R.string.echo_win_row)
                    + (rowsLeft == 1 ? "" : echoContext.getResources().getString(R.string.echo_win_plural))
                    + echoContext.getResources().getString(R.string.echo_win_left)
                    + " (" + wins + ")");
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

    void simulate() {
        reset();
        counter.total++;
        int round = 0;
        //Remove Rows already defined with 0 LP
        rmAllDead(round);
        if (gameOver(round))
            return;
        while (true) {
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
                if (gameOver(round))
                    return;
            }
        }
    }

    private void reset(){
        armies.clear();
        armies.addAll(armies_orig);
        for (Army army : armies_orig)
            army.reset();
    }
}
