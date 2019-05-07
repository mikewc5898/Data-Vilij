package clustering;

import algorithms.Clusterer;
import data.DataSet;
import dataprocessors.TSDProcessor;
import javafx.concurrent.Task;

import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public class RandomClusterer extends Clusterer {
    private DataSet dataset;
    private final int maxIterations;
    private final int updateInterval;
    public int increment;
    private final AtomicBoolean tocontinue;
    private static final Random RAND = new Random();

    public RandomClusterer(DataSet dataset,
                           int maxIterations,
                           int updateInterval,
                           boolean tocontinue,
                           int k){
        super(k);
        this.dataset = dataset;
        this.maxIterations = maxIterations;
        this.updateInterval = updateInterval;
        this.tocontinue = new AtomicBoolean(tocontinue);
        this.increment=updateInterval;

    }
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

    @Override
    public void run() {
        Set<String> instances =  dataset.getLabels().keySet();
        String[] labels = new String[]{"cluster1","cluster2","cluster3","cluster4"};
        if(!tocontinue()){
            if(increment<=maxIterations){
                for(String label:instances) {
                    int newLabel = ((int) (Math.random() * this.getNumberOfClusters()));
                    dataset.updateLabel(label, labels[newLabel]);
                }
                increment+=updateInterval;
            }
        }
        else{
                for(String label:instances) {
                    int newLabel = (RAND.nextInt(this.getNumberOfClusters()));
                    dataset.updateLabel(label, labels[newLabel]);
                }

        }
    }

    public DataSet getDataset() {
        return dataset;
    }
}
