/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package evolvablerobot;

import evolvingsubsumptionforrobocode.TestSubsumptionWithRobocodeFitnessFunction;
import evolvingsubsumptionforrobocode.EvolvingSubsumptionForRobocode;

import java.awt.Color;
import java.util.Arrays;
import robocode.AdvancedRobot;
import robocode.BulletHitBulletEvent;
import robocode.BulletHitEvent;
import robocode.BulletMissedEvent;
import robocode.HitByBulletEvent;
import robocode.HitRobotEvent;
import robocode.HitWallEvent;
import robocode.ScannedRobotEvent;

/**
 *
 * @author hector
 */
public class EvolvableRobot extends AdvancedRobot {

    private boolean init() {
            
        eventPriority = TestSubsumptionWithRobocodeFitnessFunction.getEventPriority();
        behaviourSubsumption = TestSubsumptionWithRobocodeFitnessFunction.getBehaviourOverwrite();
        behaviourActions = TestSubsumptionWithRobocodeFitnessFunction.getBehaviourActions();
        
        setEventPriority("BulletHitEvent", eventPriority[0]);
        setEventPriority("BulletHitBulletEvent", eventPriority[1]);
        setEventPriority("BulletMissedEvent", eventPriority[2]);
        setEventPriority("HitByBulletEvent", eventPriority[3]);
        setEventPriority("HitRobotEvent", eventPriority[4]);
        setEventPriority("HitWallEvent", eventPriority[5]);
        setEventPriority("ScannedRobotEvent", eventPriority[6]);
        
        return true;
    }
    
    public void run() {
        setColors(Color.BLACK, Color.BLUE, Color.YELLOW);
        
        init();
        
        if (verbose > 0) {
            out.println(Arrays.toString(eventPriority));
            out.println(Arrays.toString(behaviourSubsumption));
            for (int i = 0; i < EvolvingSubsumptionForRobocode.numberOfBehaviours; i++) {
                out.println(Arrays.toString(behaviourActions[i]));
            }
            out.flush();
        }
        
        while (true) {
            executeBehaviour(EvolvingSubsumptionForRobocode.defaultBehaviour, 0);
            if (verbose > 1) {
                out.flush();
            }
        }
    }
    
    public void onBulletHit(BulletHitEvent event) {
        executeBehaviour(EvolvingSubsumptionForRobocode.bulletHitBehaviour, putInRange(event.getBullet().getHeading() - getHeading()));

        if (verbose > 0) {
            out.print("Target hit\t");
            out.println(putInRange(event.getBullet().getHeading() - getHeading()));
        }
    }
    
    public void onBulletHitBullet(BulletHitBulletEvent event) {
        executeBehaviour(EvolvingSubsumptionForRobocode.bulletHitBulletBehaviour, putInRange(event.getBullet().getHeading() - getHeading()));

        if (verbose > 0) {
            out.print("Bullet hit\t");
            out.println(putInRange(event.getBullet().getHeading() - getHeading()));
        }
    }
    
    public void onBulletMissed(BulletMissedEvent event) {
        executeBehaviour(EvolvingSubsumptionForRobocode.bulletMissedBehaviour, putInRange(event.getBullet().getHeading() - getHeading()));

        if (verbose > 0) {
            out.print("Missed bullet\t");
            out.println(putInRange(event.getBullet().getHeading() - getHeading()));
        }
    }
    
    public void onHitByBullet(HitByBulletEvent event) {
        executeBehaviour(EvolvingSubsumptionForRobocode.hitByBulletBehaviour, event.getBearing());

        if (verbose > 0) {
            out.print("Hit by enemy\t");
            out.println(event.getBearing());
        }
    }
    
    public void onHitRobot(HitRobotEvent event) {
        if (event.isMyFault()) {                    // I am pushing
            executeBehaviour(EvolvingSubsumptionForRobocode.pushRobotBehaviour, event.getBearing());

            if (verbose > 0) {
                out.print("Pushing\t");
                out.println(event.getBearing());
            }
        }
        else {                                      // Enemy is pushing
            executeBehaviour(EvolvingSubsumptionForRobocode.pushedByRobotBehaviour, event.getBearing());

            if (verbose > 0) {
                out.print("Pushed\t");
                out.println(event.getBearing());
            }
        }
    }
    
    public void onHitWall(HitWallEvent event) {
        executeBehaviour(EvolvingSubsumptionForRobocode.hitWallBehaviour, event.getBearing());

        if (verbose > 0) {
            out.print("Wall\t");
            out.println(event.getBearing());
        }
    }
    
    public void onScannedRobot(ScannedRobotEvent event) {
        if (event.getDistance() < 100) {            // Enemy is at close distance
            executeBehaviour(EvolvingSubsumptionForRobocode.scannedCloseDistRobotBehaviour, event.getBearing());

            if (verbose > 0) {
                out.print("Target near\t");
                out.println(event.getBearing());
            }
        }
        else if (event.getDistance() < 300) {       // Enemy is at mid distance
            executeBehaviour(EvolvingSubsumptionForRobocode.scannedMidDistRobotBehaviour, event.getBearing());

            if (verbose > 0) {
                out.print("Target mid\t");
                out.println(event.getBearing());
            }
        }
        else {                                      // Enemy is at long distance
            executeBehaviour(EvolvingSubsumptionForRobocode.scannedLongDistRobotBehaviour, event.getBearing());

            if (verbose > 0) {
                out.print("Target far\t");
                out.println(event.getBearing());
            }
        }
    }
    
    private void executeBehaviour(int behaviour, double bearing) {
        for (int i = 0; i < EvolvingSubsumptionForRobocode.numberOfGroupsPerBehaviour; i++) {
            prepareMoveAction(behaviourActions[behaviour][(i * EvolvingSubsumptionForRobocode.groupSize) + 0]);
            prepareTurnRobotAction(behaviourActions[behaviour][(i * EvolvingSubsumptionForRobocode.groupSize) + 1], behaviourActions[behaviour][(i * EvolvingSubsumptionForRobocode.groupSize) + 2], bearing);
            prepareTurnGunAction(behaviourActions[behaviour][(i * EvolvingSubsumptionForRobocode.groupSize) + 3], behaviourActions[behaviour][(i * EvolvingSubsumptionForRobocode.groupSize) + 4], bearing);
            fireAction(behaviourActions[behaviour][(i * EvolvingSubsumptionForRobocode.groupSize) + 5]);
            execute();
        }
        
        if (behaviourSubsumption[behaviour]) {
            clearAllEvents();
        }
    }
    
    private void prepareMoveAction(int d) {
        if (d < 0) {
            setBack(-d);
        }
        else if (d > 0) {
            setAhead(d);
        }
    }
    
    private void prepareTurnRobotAction(int action, int a, double bearing) {
        double angle = 0;
        
        switch (action) {
            case 1:                         // Turn with respect to bearing
                setAdjustGunForRobotTurn(false);
                angle = a + bearing;
                break;
            case 2:                         // Turn with respect to bearing, gun is independent
                setAdjustGunForRobotTurn(true);
                angle = a + bearing;
                break;
            case 3:                         // Turn with respect to heading
                setAdjustGunForRobotTurn(false);
                angle = a;
                break;
            case 4:                         // Turn with respect to heading, gun is independent
                setAdjustGunForRobotTurn(true);
                angle = a;
                break;
            case 5:                         // Mirror turn
                setAdjustGunForRobotTurn(false);
                if ( (bearing > 90) || (bearing < -90) ) {
                    angle = 0;
                }
                else {
                    angle = 180-(2*bearing);
                }
                break;
            case 6:                         // Mirror turn, gun is independent
                setAdjustGunForRobotTurn(true);
                if ( (bearing > 90) || (bearing < -90) ) {
                    angle = putInRange(bearing - 180) / 2;
                }
                else {
                    angle = (2*bearing) - 180;
                }
                break;
        }
        
        angle = putInRange(angle);
        if (angle > 0) {
            setTurnRight(angle);
        }
        else if (angle < 0) {
            setTurnLeft(-angle);
        }
    }
    
    private void prepareTurnGunAction(int action, int a, double bearing) {
        double angle = 0;
        
        switch (action) {
            case 1:                         // Turn with respect to bearing
                angle = a + (bearing - (getGunHeading() - getHeading()));
                break;
            case 2:                         // Turn with respect to heading
                angle = a;
                break;
        }

        angle = putInRange(angle);
        if (angle > 0) {
            setTurnGunRight(angle);
        }
        else if (angle < 0) {
            setTurnGunLeft(-angle);
        }
    }
    
    private void fireAction(int action) {
        if (action > 0) {
            fire(action);
        }
    }
    
    private double putInRange(double angle) {
        if (angle > 180) {
            return 360 - angle;
        }
        else if (angle < -180) {
            return 360 + angle;
        }
        else {
            return angle;
        }
    }
    
    private static final int verbose = 1;
    
    private static int[] eventPriority;
    private static boolean[] behaviourSubsumption;
    private static int[][] behaviourActions;
    
}
