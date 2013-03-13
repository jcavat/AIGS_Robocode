/*
 * This file is part of JGAP.
 *
 * JGAP offers a dual license model containing the LGPL as well as the MPL.
 *
 * For licensing information please see the file license.txt included with JGAP
 * or have a look at the top of class org.jgap.Chromosome which representatively
 * includes the JGAP license policy applicable for any file delivered with JGAP.
 */
package evolvingsubsumptionforrobocode;

import org.jgap.*;
import robocode.control.BattleSpecification;
import robocode.control.BattlefieldSpecification;
import robocode.control.RobocodeEngine;
import robocode.control.RobotSpecification;


public class TestSubsumptionWithRobocodeFitnessFunction extends FitnessFunction {
    
    public TestSubsumptionWithRobocodeFitnessFunction(RobocodeEngine re, RobotSpecification[] robots) {
        engine = re;
        battleObserver = new BattleObserver();
        engine.addBattleListener(battleObserver);
        
        otherRobots = new RobotSpecification[robots.length-1];
        for (int i = 0, j = 0; i < robots.length; i++) {
            if (robots[i].getName().compareTo("evolvablerobot.EvolvableRobot") == 0) {
                evolvable = robots[i];
            }
            else {
                otherRobots[j++] = robots[i];
            }
        }

        eventPriority = new int[EvolvingSubsumptionForRobocode.numberOfEvents];
        behaviourOverwrite = new boolean[EvolvingSubsumptionForRobocode.numberOfBehaviours];
        behaviourActions = new int[EvolvingSubsumptionForRobocode.numberOfBehaviours][EvolvingSubsumptionForRobocode.behaviourSize];
    }

    /**
    * Determine the fitness of the given Chromosome instance. The higher the
    * return value, the more fit the instance. This method should always
    * return the same fitness value for two equivalent Chromosome instances.
    *
    * @param a_subject the Chromosome instance to evaluate
    *
    * @return positive double reflecting the fitness rating of the given
    * Chromosome
    * @since 2.0 (until 1.1: return type int)
    * @author Neil Rotstan, Klaus Meffert, John Serri
    */
    public double evaluate(IChromosome a_subject) {
        int i = 0, j, k;
        
        for (j = 0; j < EvolvingSubsumptionForRobocode.numberOfEvents; j++) {
            eventPriority[j] = ((Integer)a_subject.getGene(i++).getAllele()).intValue();
        }
        for (j = 0; j < EvolvingSubsumptionForRobocode.numberOfBehaviours; j++) {
            behaviourOverwrite[j] = ((Integer)a_subject.getGene(i++).getAllele()).intValue() > 50;
        }
        for (j = 0; j < EvolvingSubsumptionForRobocode.numberOfBehaviours; j++) {
            for (k = 0; k < EvolvingSubsumptionForRobocode.behaviourSize; k++) {
                behaviourActions[j][k] = ((Integer)a_subject.getGene(i++).getAllele()).intValue();
            }
        }
        
        double tempFitness;
        double fitness = 0;
        for (j = 0; j < otherRobots.length; j++) {
            RobotSpecification[] tempRobots = new RobotSpecification[2];
            tempRobots[0] = evolvable;
            tempRobots[1] = otherRobots[j];
            battleSpecs = new BattleSpecification(1, new BattlefieldSpecification(800, 600), tempRobots);
            tempFitness= 0;
            System.out.println("Testing against " + otherRobots[j].getName() + "...");
            for (i = 0; i < EvolvingSubsumptionForRobocode.numberOfBattles; i++) {
                engine.runBattle(battleSpecs, true);
                tempFitness += battleObserver.getScoreRobot()/500;
            }
            tempFitness /= EvolvingSubsumptionForRobocode.numberOfBattles;
            fitness += tempFitness;
        }
        fitness /= otherRobots.length;

        return Math.min(1.0d, fitness);
    }

    public static int[] getEventPriority() {
        return eventPriority;
    }
    
    public static boolean[] getBehaviourOverwrite() {
        return behaviourOverwrite;
    }

    public static int[][] getBehaviourActions() {
        return behaviourActions;
    }
    
    public void setBattleSpecs(BattleSpecification bs) {
        battleSpecs = bs;
    }
    
    private static int[] eventPriority;
    private static boolean[] behaviourOverwrite;
    private static int[][] behaviourActions;
    
    private RobocodeEngine engine;
    private BattleSpecification battleSpecs;
    private BattleObserver battleObserver;
    private RobotSpecification evolvable;
    private RobotSpecification[] otherRobots;
}
