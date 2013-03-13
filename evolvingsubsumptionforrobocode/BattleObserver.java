/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package evolvingsubsumptionforrobocode;

import robocode.control.events.BattleAdaptor;
import robocode.control.events.BattleCompletedEvent;
import robocode.control.events.BattleErrorEvent;
import robocode.control.events.BattleMessageEvent;

/**
 *
 * @author hsatizab
 */
public class BattleObserver extends BattleAdaptor {
     // Called when the battle is completed successfully with battle results
     public void onBattleCompleted(BattleCompletedEvent e) {
		if (e.getIndexedResults().length > 1) {
			scoreRobot = e.getIndexedResults()[0].getScore();
			scoreEnemy = e.getIndexedResults()[1].getScore();
		}
		else {
			System.out.println("Error. Robocode did not send results.");
			scoreRobot = 0;
			scoreEnemy = 0;
		}
     }
 
     // Called when the game sends out an information message during the battle
     public void onBattleMessage(BattleMessageEvent e) {
     }
 
     // Called when the game sends out an error message during the battle
     public void onBattleError(BattleErrorEvent e) {
     }
     
     public double getScoreRobot() {
         return scoreRobot;
     }
     
     public double getScoreEnemy() {
         return scoreEnemy;
     }
     
     private double scoreRobot;
     private double scoreEnemy;
}
