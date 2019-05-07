package classification;

import algorithms.Classifier;
import data.DataSet;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;



/**
 * @author Ritwik Banerjee
 */
public class RandomClassifier extends Classifier {

    private static final Random RAND = new Random();


    // this mock classifier doesn't actually use the data, but a real classifier will
    private DataSet dataset;
    private final int maxIterations;
    private final int updateInterval;
    public int increment;
    public ArrayList<List<Integer>> outputs;


    // currently, this value does not change after instantiation
    private final AtomicBoolean tocontinue;





    @Override
    public int getMaxIterations() {
        return maxIterations;
    }

    @Override
    public int getUpdateInterval() {
        return updateInterval;
    }

    @Override
    public boolean tocontinue() {
        return tocontinue.get();
    }


    public RandomClassifier(DataSet dataset,
                            int maxIterations,
                            int updateInterval,
                            boolean tocontinue) {
        this.dataset = dataset;
        this.maxIterations = maxIterations;
        this.updateInterval = updateInterval;
        this.tocontinue = new AtomicBoolean(tocontinue);
        this.increment=updateInterval;
        this.outputs= new ArrayList<>();
    }

    @Override
    public void run() {
        if(!tocontinue()){
           if(increment <=maxIterations){
               int xCoefficient = new Double(RAND.nextDouble() * 100).intValue();
               int yCoefficient = new Double(RAND.nextDouble() * 100).intValue();
               int constant = new Double(RAND.nextDouble() * 100).intValue();
               output = Arrays.asList(xCoefficient, yCoefficient, constant);
               increment +=updateInterval;
           }
        }
        else {
            for (int i = 1; i <= maxIterations; i++) {
                int xCoefficient = new Double(RAND.nextDouble() * 100).intValue();
                int yCoefficient = new Double(RAND.nextDouble() * 100).intValue();
                int constant = new Double(RAND.nextDouble() * 100).intValue();

                // this is the real output of the classifier
                output = Arrays.asList(xCoefficient, yCoefficient, constant);
                // everything below is just for internal viewing of how the output is changing
                // in the final project, such changes will be dynamically visible in the UI
                if (i % updateInterval == 0) {
                    //System.out.printf("Iteration number %d: ", i);
                    this.outputs.add(output);
                    //flush();
                }
                if (i > maxIterations  && RAND.nextDouble() < 0.05) {
                    //System.out.printf("Iteration number %d: ", i);
                    this.outputs.add(output);
                    //flush();
                    break;
                }
            }
        }
    }



    /** A placeholder main method to just make sure this code runs smoothly */
    public static void main(String... args){
        DataSet dataset   =  DataSet.dataProcess("@Instance3\tlabel1\t10.0,2.9\n" +
                "@Instance1\tlabel1\t1.5,2.2\n" +
                "@Instance2\tlabel1\t1.8,3.0\n" +
                "@Instance4\tlabel2\t10.0,9.4\n" +
                "@Instance5\tlabel3\t2.1,28.0\n" +
                "@Instance6\tlabel3\t-3.0,19.0");
        RandomClassifier classifier = new RandomClassifier(dataset, 100, 5, true );
        classifier.run(); // no multithreading yet
    }


}