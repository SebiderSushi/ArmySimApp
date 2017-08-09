package me.sebi.armysim;

import android.content.Context;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by sebi on 16.04.17.
 */
class Simulation {

    final ArrayList<Army> armies = new ArrayList<>();
    final boolean useRandom;
    private final LinearLayout echoView;
    private final Context echoContext;
    private final Counter counter;
    private final LinearLayout.LayoutParams echoParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

    ArrayList<ArrayList<Army>> sortedArmies = new ArrayList<>(0);

    Simulation(boolean useRandom, Counter counter, Context echoContext, LinearLayout echoView) {
        this.useRandom = useRandom;
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

    private boolean stillRunning(int round) {
        if (armies.size() == 0) {
            counter.ties++;
            echo(echoContext.getResources().getString(R.string.echo_tie) + " (" + counter.ties + ")");
            return false;
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
            return false;
        }
        return true;
    }

    void rmAllDead(int round) {
        ArrayList<Army> armies_copy = (ArrayList<Army>) armies.clone();
        for (Army army : armies_copy)
            army.rmDead(round);
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
        if (armies.size() == 0) {
            echo(echoContext.getResources().getString(R.string.echo_noArmies));
            return;
        }
        int round = 0;
        //Remove Rows already defined with 0 LP
        rmAllDead(round);
        if (!stillRunning(round))
            return;
        while (true) {
            round += 1;
            for (Army attacker : armies)
                for (Army enemy : armies)
                    if (attacker != enemy)
                        attacker.distanceAttack(enemy);
            rmAllDead(round);
            //Here iteration so armies attack in best order.
            //When a group of peers haz attaqzt, les deads be colektid.
            //this way the best armies attack first and equal armies attack
            //each other simultaneously
            sortedArmies = this.armiesSortedBySpeed();
            for (ArrayList<Army> attackingArmies : sortedArmies) {
                for (Army attacker : attackingArmies)
                    for (ArrayList<Army> enemyArmies : sortedArmies)
                        for (Army enemy : enemyArmies)
                            if (attacker != enemy)
                                attacker.attack(enemy);
                rmAllDead(round);
                if (!stillRunning(round))
                    return;
            }
        }
    }
}
