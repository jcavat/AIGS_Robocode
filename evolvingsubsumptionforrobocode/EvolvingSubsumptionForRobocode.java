/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package evolvingsubsumptionforrobocode;

import java.io.*;

//import java.util.Enumeration;
import java.util.List;
import org.jgap.*;
import org.jgap.data.DataTreeBuilder;
import org.jgap.data.IDataCreators;
import org.jgap.event.EventManager;
import org.jgap.impl.*;
import org.jgap.xml.XMLDocumentBuilder;
import org.jgap.xml.XMLManager;
import org.w3c.dom.Document;
import robocode.control.BattleSpecification;
import robocode.control.BattlefieldSpecification;
import robocode.control.RobocodeEngine;
import robocode.control.RobotSpecification;

/**
 *
 * @author hsatizab
 */
public class EvolvingSubsumptionForRobocode {

    private static void evolve(int mi) throws Exception {

        int i, j, k;
        // Create random initial population of Chromosomes.
        // Here we try to read in a previous run via XMLManager.readFile(..)
        // -----------------------------------------------------------------
        Genotype population;
        try {
            Document doc = XMLManager.readFile(new File("EvolvingRobocode.xml"));
            population = XMLManager.getGenotypeFromDocument(configuration, doc);
        }
        catch (UnsupportedRepresentationException uex) {
        // JGAP codebase might have changed between two consecutive runs.
        // --------------------------------------------------------------
            System.out.println("Previous population has a different configuration");
            population = Genotype.randomInitialGenotype(configuration);
        }
        catch (FileNotFoundException fex) {
            System.out.println("Previous population not found");
            population = Genotype.randomInitialGenotype(configuration);
        }

        List<IChromosome> pop;
        double min, max, avg, v;
        BufferedWriter logger = new BufferedWriter(new FileWriter("evolutionLog.txt"));
        logger.write("Size\tMin\tMax\tAvg\n");
        

        // Evolve the population. Since we don't know what the best answer
        // is going to be, we just evolve the max number of times.
        long startTime = System.currentTimeMillis();
        for (i = 0; i < mi; i++) {
            System.out.print("Iteration: ");
            System.out.println(i);
            population.evolve();
            System.out.print("Best robot: ");
            System.out.println(population.getFittestChromosome().getFitnessValue());
            
            min = Double.MAX_VALUE;
            max = Double.MIN_VALUE;
            avg = 0;
            pop = population.getPopulation().getChromosomes();
            for (j = 0; j < pop.size(); j++) {
                v = pop.get(j).getFitnessValue();
                if (v < min) min = v;
                if (v > max) max = v;
                avg += v;
            }
            avg /= pop.size();
            logger.write(String.valueOf(pop.size()) + "\t" + String.valueOf(min) + "\t" + String.valueOf(max) + "\t" + String.valueOf(avg) + "\n");
			logger.flush();
        }
        long endTime = System.currentTimeMillis();
        System.out.println("Total evolution time: " + (endTime - startTime) + " ms");
        
        logger.close();

        // Save progress to file. A new run of this test will then be able to
        // resume where it stopped before!
        // ---------------------------------------------------------------------

        // Represent Genotype as tree with elements Chromomes and Genes.
        // -------------------------------------------------------------
        DataTreeBuilder builder = DataTreeBuilder.getInstance();
        IDataCreators doc2 = builder.representGenotypeAsDocument(population);
        // create XML document from generated tree
        XMLDocumentBuilder docbuilder = new XMLDocumentBuilder();
        Document xmlDoc = (Document) docbuilder.buildDocument(doc2);
        XMLManager.writeFile(xmlDoc, new File("EvolvingRobocode.xml"));
        // Display the best solution we found.
        // -----------------------------------
        IChromosome bestSolutionSoFar = population.getFittestChromosome();
        System.out.println("The best solution has a fitness value of " + bestSolutionSoFar.getFitnessValue());

        BufferedWriter writer = new BufferedWriter(new FileWriter("genome"));
        k = 0;
        for (i = 0; i < EvolvingSubsumptionForRobocode.numberOfEvents; i++) {
            writer.write(bestSolutionSoFar.getGene(k++).getAllele().toString());
            if ((i + 1) < EvolvingSubsumptionForRobocode.numberOfEvents) {
                writer.write("\t");
            }
        }
        writer.write("\n");
        writer.flush();
        
        for (i = 0; i < EvolvingSubsumptionForRobocode.numberOfBehaviours; i++) {
            writer.write(bestSolutionSoFar.getGene(k++).getAllele().toString());
            if ((i + 1) < EvolvingSubsumptionForRobocode.numberOfBehaviours) {
                writer.write("\t");
            }
        }
        writer.write("\n");
        writer.flush();
        
        for (j = 0; j < EvolvingSubsumptionForRobocode.numberOfBehaviours; j++) {
            for (i = 0; i < EvolvingSubsumptionForRobocode.behaviourSize; i++) {
                writer.write(bestSolutionSoFar.getGene(k++).getAllele().toString());
                if ((i + 1) < EvolvingSubsumptionForRobocode.behaviourSize) {
                    writer.write("\t");
                }
            }
            writer.write("\n");
            writer.flush();
        }
        writer.close();
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        // TODO code application logic here
        if (args.length < 2) {
            System.out.println("Syntax: EvolvingSubsumptionForRobocode <population size> <iterations>");
        }
        else {
            int popSize = 0;
            int maxIter = 0;
            try {
                popSize = Integer.parseInt(args[0]);
				System.out.println("Population size: " + popSize);
            }
            catch (NumberFormatException e) {
                System.out.println("The <population size> argument must be a valid integer value.");
                System.exit(1);
            }
            try {
                maxIter = Integer.parseInt(args[1]);
				System.out.println("Maximum generations: " + maxIter);
            }
            catch (NumberFormatException e) {
                System.out.println("The <iterations> argument must be a valid integer value.");
                System.exit(1);
            }
            if ( (popSize < 1) && (maxIter < 1) ) {
                System.out.println("The <population size> and the <iterations> arguments must be greater than 0.");
            }
            else {
                System.setProperty("NOSECURITY", "true");
                System.setProperty("ROBOTPATH", "./robots/");
                RobocodeEngine engine = new RobocodeEngine(new File("."));
                engine.setVisible(false);
                RobotSpecification[] robots = engine.getLocalRepository();
                boolean evolvable = false;
                System.out.println("Available robots:");
                for (int k = 0; k < robots.length; k++) {
                    if (robots[k].getName().compareTo("evolvablerobot.EvolvableRobot") == 0) {
                        evolvable = true;
                    }
                    System.out.println(robots[k].getName());
                }
                if (evolvable && (robots.length > 2)) {
                    myFitnessFunction = new TestSubsumptionWithRobocodeFitnessFunction(engine, robots);

//-------------------- Default configuration ----------------------------------
//                    // Start with a DefaultConfiguration, which comes setup with the most
//                    // common settings.
//                    configuration = new DefaultConfiguration();
//                    
//                    // Care that the fittest individual of the current population is
//                    // always taken to the next generation.
//                    // Consider: With that, the pop. size may exceed its original
//                    // size by one sometimes!
//                    configuration.setPreservFittestIndividual(true);
//                    configuration.setKeepPopulationSizeConstant(false);
//-------------------- end of default configuration ----------------------------------
                    
//-------------------- Custom configuration ----------------------------------
                    configuration = new Configuration("Robocode");
                    //fitness function
                    configuration.setFitnessEvaluator(new DefaultFitnessEvaluator());
                    //selection
                    BestChromosomesSelector bestChromsSelector = new BestChromosomesSelector(configuration, 0.9);                   //ranking
                    bestChromsSelector.setDoubletteChromosomesAllowed(false);
//                    WeightedRouletteSelector rouletteSelector = new WeightedRouletteSelector(configuration);                        //roulette
//                    rouletteSelector.setDoubletteChromosomesAllowed(false);
//                    TournamentSelector tournamentSelector = new TournamentSelector(configuration, (int)Math.ceil(popSize/10), 0.9); //tournament
//                    tournamentSelector.setDoubletteChromosomesAllowed(false);
                    configuration.addNaturalSelector(bestChromsSelector, false);
                    //elitism
                    configuration.setPreservFittestIndividual(true);
                    configuration.setKeepPopulationSizeConstant(false);
                    //random generator
                    configuration.setRandomGenerator(new StockRandomGenerator());
                    configuration.setEventManager(new EventManager());
                    //crossover
                    configuration.addGeneticOperator(new CrossoverOperator(configuration, 0.35, true));
                    //mutation
                    configuration.addGeneticOperator(new MutationOperator(configuration, 12));
//-------------------- end of custom configuration ----------------------------------

                    // Now we need to tell the Configuration object how we want our
                    // Chromosomes to be setup. We do that by actually creating a
                    // sample Chromosome and then setting it on the Configuration
                    // object. We want our Chromosomes to each have 7+10+(3*6*10)
                    // genes, 7 for the events' priorities, 10 for event reset,
                    // and 180 for the actions of the behaviours; 10 behaviours,
                    // each one with 3 groups of 6 integers. We therefore use the
                    // IntegerGene class to represent each of the genes.
                    int i, j;
                    Gene[] sampleGenes = new Gene[numberOfEvents+numberOfBehaviours+(behaviourSize*numberOfBehaviours)];
                    for (i = 0, j = 0; j < numberOfEvents; i++, j++) {               // Event priorities
                        sampleGenes[i] = new IntegerGene(configuration, 0, 100);
                    }
                    for (j = 0; j < numberOfBehaviours; i++, j++) {                     // Event subsumption
                        sampleGenes[i] = new IntegerGene(configuration, 0, 100);
                    }
                    for (j = 0; j < numberOfBehaviours; j++) {
                        sampleGenes[i++] = new IntegerGene(configuration, -20, 100);   // Forward
                        sampleGenes[i++] = new IntegerGene(configuration, 0, 6);       // Turn robot action
                        sampleGenes[i++] = new IntegerGene(configuration, -90, 90);    // Turn angle
                        sampleGenes[i++] = new IntegerGene(configuration, 0, 2);       // Turn gun action
                        sampleGenes[i++] = new IntegerGene(configuration, -90, 90);    // Turn gun angle
                        sampleGenes[i++] = new IntegerGene(configuration, 0, 3);       // Fire

                        sampleGenes[i++] = new IntegerGene(configuration, 0, 0);   // Forward
                        sampleGenes[i++] = new IntegerGene(configuration, 0, 0);       // Turn robot action
                        sampleGenes[i++] = new IntegerGene(configuration, 0, 0);    // Turn angle
                        sampleGenes[i++] = new IntegerGene(configuration, 0, 0);       // Turn gun action
                        sampleGenes[i++] = new IntegerGene(configuration, 0, 0);    // Turn gun angle
                        sampleGenes[i++] = new IntegerGene(configuration, 0, 0);       // Fire

                        sampleGenes[i++] = new IntegerGene(configuration, 0, 0);   // Forward
                        sampleGenes[i++] = new IntegerGene(configuration, 0, 0);       // Turn robot action
                        sampleGenes[i++] = new IntegerGene(configuration, 0, 0);    // Turn angle
                        sampleGenes[i++] = new IntegerGene(configuration, 0, 0);       // Turn gun action
                        sampleGenes[i++] = new IntegerGene(configuration, 0, 0);    // Turn gun angle
                        sampleGenes[i++] = new IntegerGene(configuration, 0, 0);       // Fire
                    }

                    IChromosome sampleChromosome = new Chromosome(configuration, sampleGenes);
                    configuration.setSampleChromosome(sampleChromosome);

                    // Finally, we need to tell the Configuration object how many
                    // Chromosomes we want in our population. The more Chromosomes,
                    // the larger number of potential solutions (which is good for
                    // finding the answer), but the longer it will take to evolve
                    // the population (which could be seen as bad).
                    configuration.setPopulationSize(popSize);

                    // Set the fitness function we want to use, which is our
                    // TestSubsumptionWithRobocodeFitnessFunction.
                    configuration.setFitnessFunction(myFitnessFunction);
                    
                    //----------------------------------------------------------
                    System.out.println(configuration.toString());
                    List go = configuration.getGeneticOperators();
                    System.out.println("Crossover rate: " + ((CrossoverOperator)go.get(0)).getCrossOverRatePercent());
                    System.out.println("Allow full crossover: " + ((CrossoverOperator)go.get(0)).isAllowFullCrossOver());
                    System.out.println("Mutation rate: " + ((MutationOperator)go.get(1)).getMutationRate());
                    //----------------------------------------------------------

                    evolve(maxIter);
                }
                else {
                    System.out.println("Error. No evolvable robot was found, or not enough robots.");
                }
                engine.close();
                System.exit(0);
            }
            
        }
    }

    public static final int groupSize = 6;
    public static final int numberOfGroupsPerBehaviour = 3;
    public static final int behaviourSize = numberOfGroupsPerBehaviour * groupSize;
    public static final int numberOfEvents = 7;
    public static final int numberOfBehaviours = 11;
    public static final int numberOfBattles = 5;
    
    public static final int defaultBehaviour = 0;
    public static final int bulletHitBehaviour = 1;
    public static final int bulletHitBulletBehaviour = 2;
    public static final int bulletMissedBehaviour = 3;
    public static final int hitByBulletBehaviour = 4;
    public static final int pushRobotBehaviour = 5;
    public static final int pushedByRobotBehaviour = 6;
    public static final int hitWallBehaviour = 7;
    public static final int scannedCloseDistRobotBehaviour = 8;
    public static final int scannedMidDistRobotBehaviour = 9;
    public static final int scannedLongDistRobotBehaviour = 10;
    
    private static TestSubsumptionWithRobocodeFitnessFunction myFitnessFunction;
    private static Configuration configuration;
}
