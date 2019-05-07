package dataprocessors;

import java.lang.reflect.Constructor;
import actions.AppActions;
import algorithms.Clusterer;
import classification.RandomClassifier;
import clustering.KMeansClusterer;
import clustering.RandomClusterer;
import data.DataSet;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import ui.AppUI;
import vilij.components.DataComponent;
import vilij.components.Dialog;
import vilij.templates.ApplicationTemplate;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

import static settings.AppPropertyTypes.*;
import static vilij.settings.PropertyTypes.*;

/**
 * This is the concrete application-specific implementation of the data component defined by the Vilij framework.
 *
 * @author Ritwik Banerjee
 * @see DataComponent
 */
@SuppressWarnings("SpellCheckingInspection")
public class AppData implements DataComponent {

    private final TSDProcessor        processor;
    private final ApplicationTemplate applicationTemplate;
    public  static java.util.ArrayList<String> names = new java.util.ArrayList<>();
    public int[] classA = new int[]{0,0};
    public int[] classB = new int[]{0,0};
    public int[] classC = new int[]{0,0};
    public int[] clusterA = new int[]{0,0};
    public int[] clusterB = new int[]{0,0};
    public int[] clusterC = new int[]{0,0};
    public boolean contA = false;
    public boolean contB = false;
    public boolean contC = false;
    public boolean conA = false;
    public boolean conB = false;
    public boolean conC = false;
    public int numClustersA = 0;
    public int numClustersB = 0;
    public int numClustersC = 0;
    private DataSet dataset;
    public int increment;
    private HBox runBox = new HBox();
    private Label runLabel;
    public int exitCase = 0;


    public static  AtomicInteger line = new AtomicInteger(0);

    public AppData(ApplicationTemplate applicationTemplate) {
        this.processor = new TSDProcessor();
        this.applicationTemplate = applicationTemplate;
    }


    @Override
    public void loadData(Path dataFilePath){
        ((AppUI)applicationTemplate.getUIComponent()).getTextArea().setVisible(false);
        ((AppUI)applicationTemplate.getUIComponent()).getLeftColumn().setVisible(false);
        for(double i = 0;i<=names.size()/5;i++){
            ((AppUI)applicationTemplate.getUIComponent()).getTextArea().clear();
        }

        File loadedData = dataFilePath.toFile();
        try (java.util.Scanner fileReader = new java.util.Scanner(loadedData)) {
            String fileContents = AppUI.EMPTY;
            while(fileReader.hasNextLine()){
                fileContents = fileContents.concat(fileReader.nextLine() + "\n");
            }
            fileReader.close();
            int lineChecker = ((AppActions)applicationTemplate.getActionComponent()).lineCheck(fileContents);
            String errorChecker = ((AppActions)applicationTemplate.getActionComponent()).nameCheck(fileContents);
            if(lineChecker == -1){
               if(errorChecker.equals(AppUI.EMPTY)){
                   try {
                       processor.clear();
                       ((AppUI) applicationTemplate.getUIComponent()).getChart().getData().clear();
                       ((AppUI) applicationTemplate.getUIComponent()).getVisibleChart().getData().clear();
                       ((AppUI) applicationTemplate.getUIComponent()).getTextArea().clear();
                       ((AppData) applicationTemplate.getDataComponent()).loadData(fileContents);
                       ((AppData) applicationTemplate.getDataComponent()).processData();
                       //reference marker
                       names = new java.util.ArrayList<>(Arrays.asList(fileContents.split("\n")));
                       int count = 0;
                       for (String name :
                               names) {
                           if(count < 10) {
                               ((AppUI) applicationTemplate.getUIComponent()).getTextArea().appendText(name);
                               count++;
                           }
                           if (count < 10) {
                               ((AppUI) applicationTemplate.getUIComponent()).getTextArea().appendText("\n");
                           }
                       }
                       if (count >= 10) {
                           //Dialog lineCount = applicationTemplate.getDialog(Dialog.DialogType.ERROR);
                           //lineCount.show(applicationTemplate.manager.getPropertyValue(LOAD_WORK_TITLE.name()),
                                   //applicationTemplate.manager.getPropertyValue(LOAD_COUNT_MSG.name()) + names.size());
                           line.set(10);
                           ((AppUI) applicationTemplate.getUIComponent()).getTextArea().textProperty().addListener((observable, oldValue, newValue) -> {
                               int lineCounter = 0;
                               int newCounter = 0;
                               for(int i = 0;i<oldValue.length();i++){
                                   if(oldValue.substring(i,i+1).equals("\n")){
                                       lineCounter++;
                                   }
                               }
                               for(int i = 0;i< newValue.length();i++){
                                   if(newValue.substring(i,i+1).equals("\n")){
                                       newCounter++;
                                   }
                               }
                               lineCounter = lineCounter - newCounter;
                               if(lineCounter > 0){
                                   for(int i = 0;i<lineCounter;i++){
                                       if(line.get() <names.size()) {
                                           ((AppUI) applicationTemplate.getUIComponent()).getTextArea().appendText("\n");
                                           ((AppUI) applicationTemplate.getUIComponent()).getTextArea().appendText(names.get(line.get()));
                                           line.getAndIncrement();
                                       }
                                   }
                               }

                           });
                       }
                       ((AppUI)applicationTemplate.getUIComponent()).getLeftColumn().setVisible(true);
                   } catch (Exception e) {
                       Dialog saveError = applicationTemplate.getDialog(Dialog.DialogType.ERROR);
                       saveError.show(applicationTemplate.manager.getPropertyValue(LOAD_ERROR_TITLE.name()),
                               applicationTemplate.manager.getPropertyValue(LOAD_ERROR_MSG.name()));

                   }
               }
               else{
                   Dialog saveError = applicationTemplate.getDialog(Dialog.DialogType.ERROR);
                   saveError.show(applicationTemplate.manager.getPropertyValue(SAVE_ERROR_TITLE.name()),
                           applicationTemplate.manager.getPropertyValue(DUPLICATE_ERROR_MSG.name()) + errorChecker);
                   ((AppUI)applicationTemplate.getUIComponent()).getLeftColumn().setVisible(false);

               }
            }
            else{
                Dialog saveError = applicationTemplate.getDialog(Dialog.DialogType.ERROR);
                saveError.show(applicationTemplate.manager.getPropertyValue(SAVE_ERROR_TITLE.name()),
                        applicationTemplate.manager.getPropertyValue(LINESAVE_ERROR_MSG.name()) + lineChecker);
                ((AppUI)applicationTemplate.getUIComponent()).getLeftColumn().setVisible(false);

            }
        }
        catch (FileNotFoundException e){
            Dialog errorDialog = applicationTemplate.getDialog(Dialog.DialogType.ERROR);
            errorDialog.show(applicationTemplate.manager.getPropertyValue(LOAD_ERROR_TITLE.name()),
                    applicationTemplate.manager.getPropertyValue(LOAD_ERROR_MSG.name()));

        }

    }

    public void loadData(String dataString) throws Exception{
        try {
            processor.processString(dataString);
        } catch (Exception e) {
            Dialog errorDialog = applicationTemplate.getDialog(Dialog.DialogType.ERROR);
            errorDialog.show(applicationTemplate.manager.getPropertyValue(LOAD_ERROR_TITLE.name()),
                    applicationTemplate.manager.getPropertyValue(LOAD_ERROR_MSG.name()));
            throw new Exception();
        }
    }

    @Override
    public void saveData(Path dataFilePath) {
        try {
            processor.clear();
            try {
                processor.processString(((AppUI)applicationTemplate.getUIComponent()).getTextArea().getText());
            } catch (Exception e) {
                throw new IOException();
            }
            FileWriter dataWriter = new FileWriter(dataFilePath.toFile());
            dataWriter.write(processor.labelInstance());
            dataWriter.close();
        }
        catch(IOException e){
            Dialog saveError = applicationTemplate.getDialog(Dialog.DialogType.ERROR);
            saveError.show(applicationTemplate.manager.getPropertyValue(SAVE_ERROR_TITLE.name()),
                    applicationTemplate.manager.getPropertyValue(SAVE_ERROR_MSG.name()));
        }
    }
    private void classifierRun(RandomClassifier classifier){
        if(classifier.increment <= classA[0]){
            runBox.getChildren().clear();
            ((AppUI)applicationTemplate.getUIComponent()).getScrnshotButton().setDisable(true);
            classifier.run();
            increment+=classA[1];
            ((AppUI)applicationTemplate.getUIComponent()).getScrnshotButton().setDisable(false);
            runLabel = new Label(increment + applicationTemplate.manager.getPropertyValue(OUT.name()) + classA[0] + applicationTemplate.manager.getPropertyValue(ITERATIONS.name()));
            runLabel.getStylesheets().add(applicationTemplate.manager.getPropertyValue(CSS_PATH.name()));
            runBox.getChildren().add(runLabel);
            ((AppUI)applicationTemplate.getUIComponent()).getVisibleChart().getData().clear();
            displayData();
            ((AppUI)applicationTemplate.getUIComponent()).plotLine(((AppUI)applicationTemplate.getUIComponent()).getVisibleChart(),classifier.getOutput());
        }
        else{
            runBox.getChildren().clear();
            ((AppUI)applicationTemplate.getUIComponent()).settingsA.setDisable(false);
            ((AppUI)applicationTemplate.getUIComponent()).getDisplayButton().setDisable(false);
            ((AppUI)applicationTemplate.getUIComponent()).getRunButton().setDisable(true);
            increment = 0;
        }

    }
    private void clusterRun(Clusterer clusterer,int i){
        if(i==0){
            if(increment < clusterA[0]){
                runBox.getChildren().clear();
                ((AppUI)applicationTemplate.getUIComponent()).getScrnshotButton().setDisable(true);
                clusterer.run();
                increment+=clusterA[1];
                ((AppUI)applicationTemplate.getUIComponent()).getScrnshotButton().setDisable(false);
                runLabel = new Label(increment + applicationTemplate.manager.getPropertyValue(OUT.name()) + clusterA[0] + applicationTemplate.manager.getPropertyValue(ITERATIONS.name()));
                runLabel.getStylesheets().add(applicationTemplate.manager.getPropertyValue(CSS_PATH.name()));
                runBox.getChildren().add(runLabel);
                ((AppUI)applicationTemplate.getUIComponent()).getVisibleChart().getData().clear();
                displayData();
            }
            else{
                runBox.getChildren().clear();
                ((AppUI)applicationTemplate.getUIComponent()).settingsA.setDisable(false);
                ((AppUI)applicationTemplate.getUIComponent()).settingsB.setDisable(false);
                ((AppUI)applicationTemplate.getUIComponent()).getDisplayButton().setDisable(false);
                ((AppUI)applicationTemplate.getUIComponent()).getRunButton().setDisable(true);
                increment = 0;
            }

        }
        else if(i==1){
            if(increment < clusterB[0]){
                runBox.getChildren().clear();
                ((AppUI)applicationTemplate.getUIComponent()).getScrnshotButton().setDisable(true);
                clusterer.run();
                increment+=clusterB[1];
                ((AppUI)applicationTemplate.getUIComponent()).getScrnshotButton().setDisable(false);
                runLabel = new Label(increment + applicationTemplate.manager.getPropertyValue(OUT.name()) + clusterB[0] + applicationTemplate.manager.getPropertyValue(ITERATIONS.name()));
                runLabel.getStylesheets().add(applicationTemplate.manager.getPropertyValue(CSS_PATH.name()));
                runBox.getChildren().add(runLabel);
                ((AppUI)applicationTemplate.getUIComponent()).getVisibleChart().getData().clear();
                displayData();
            }
            else{
                runBox.getChildren().clear();
                ((AppUI)applicationTemplate.getUIComponent()).settingsA.setDisable(false);
                ((AppUI)applicationTemplate.getUIComponent()).settingsB.setDisable(false);
                ((AppUI)applicationTemplate.getUIComponent()).getDisplayButton().setDisable(false);
                ((AppUI)applicationTemplate.getUIComponent()).getRunButton().setDisable(true);
                increment = 0;
            }

        }
    }

    public void runAlgorithm(int i) throws ClassNotFoundException, IllegalAccessException, InvocationTargetException, InstantiationException {
        ((AppUI) applicationTemplate.getUIComponent()).getRunButton().setDisable(true);
        ((AppUI)applicationTemplate.getUIComponent()).getScrnshotButton().setDisable(true);
        ((AppUI)applicationTemplate.getUIComponent()).getAlgorithm().getChildren().remove(runBox);
        ((AppUI)applicationTemplate.getUIComponent()).getAlgorithm().getChildren().add(runBox);
        runBox.getChildren().clear();
        ((AppUI)applicationTemplate.getUIComponent()).getVisibleChart().getData().clear();
        switch(i){
            case 0:
                exitCase = classA[0];
                ((AppUI)applicationTemplate.getUIComponent()).settingsA.setDisable(true);
                increment = classA[1];
                dataset = DataSet.dataProcess(processor.labelInstance());
                Class<?> klass = Class.forName(applicationTemplate.manager.getPropertyValue(RANDOM_CLASSIFIER.name()));
                Constructor konstructor = klass.getConstructors()[0];
                RandomClassifier classifier = (RandomClassifier) konstructor.newInstance(dataset, classA[0], classA[1], contA);
                ((AppUI)applicationTemplate.getUIComponent()).getVisibleChart().getData().clear();
                if(!contA){
                    ((AppUI) applicationTemplate.getUIComponent()).getRunButton().setDisable(false);
                    ((AppUI) applicationTemplate.getUIComponent()).getRunButton().setOnAction(e -> classifierRun(classifier));
                    classifier.run();
                    displayData();
                    runLabel = new Label(increment + applicationTemplate.manager.getPropertyValue(OUT.name()) + classA[0] + applicationTemplate.manager.getPropertyValue(ITERATIONS.name()));
                    runLabel.getStylesheets().add(applicationTemplate.manager.getPropertyValue(CSS_PATH.name()));
                    runBox.getChildren().add(runLabel);
                    ((AppUI)applicationTemplate.getUIComponent()).plotLine(((AppUI)applicationTemplate.getUIComponent()).getVisibleChart(),classifier.getOutput());
                    ((AppUI)applicationTemplate.getUIComponent()).getScrnshotButton().setDisable(false);
                }
                else{
                    increment = 0;
                    ((AppUI)applicationTemplate.getUIComponent()).getVisibleChart().getData().clear();
                    classifier.run();
                    Task task = new Task(){
                        int numRuns = classifier.outputs.size();
                        @Override
                        protected Void call(){
                            for(int i = 0;i<numRuns;i++){
                                int finalI = i;
                                Platform.runLater(() -> {
                                    ((AppUI)applicationTemplate.getUIComponent()).getVisibleChart().getData().clear();
                                    displayData();
                                    runBox.getChildren().clear();
                                    increment += classA[1];
                                    runLabel = new Label(increment + applicationTemplate.manager.getPropertyValue(OUT.name()) + classA[0] + applicationTemplate.manager.getPropertyValue(ITERATIONS.name()));
                                    runLabel.getStylesheets().add(applicationTemplate.manager.getPropertyValue(CSS_PATH.name()));
                                    runBox.getChildren().add(runLabel);
                                    ((AppUI)applicationTemplate.getUIComponent()).plotLine(((AppUI)applicationTemplate.getUIComponent()).getVisibleChart(),classifier.outputs.get(finalI));
                                });
                                try {
                                    Thread.sleep(2000);
                                } catch (InterruptedException ie) {
                                    ie.printStackTrace();
                                }

                            }
                            increment = 0;
                            ((AppUI)applicationTemplate.getUIComponent()).settingsA.setDisable(false);
                            ((AppUI)applicationTemplate.getUIComponent()).getDisplayButton().setDisable(false);
                            ((AppUI)applicationTemplate.getUIComponent()).getScrnshotButton().setDisable(false);
                            return null;
                        }
                    };
                    Thread thread = new Thread(task);
                    thread.start();
                }
                break;
            case 1:
                break;
            case 2:
                break;
            case 3:
                exitCase = clusterA[0];
                ((AppUI)applicationTemplate.getUIComponent()).settingsA.setDisable(true);
                ((AppUI)applicationTemplate.getUIComponent()).settingsB.setDisable(true);
                increment = clusterA[1];
                dataset = DataSet.dataProcess(processor.labelInstance());
                klass = Class.forName(applicationTemplate.manager.getPropertyValue(RANDOM_CLUSTERER.name()));
                konstructor = klass.getConstructors()[0];
                RandomClusterer randCluster = (RandomClusterer) konstructor.newInstance(dataset, clusterA[0], clusterA[1], conA,numClustersA);
                ((AppUI)applicationTemplate.getUIComponent()).getVisibleChart().getData().clear();
                if(!conA){
                    ((AppUI) applicationTemplate.getUIComponent()).getRunButton().setDisable(false);
                    ((AppUI) applicationTemplate.getUIComponent()).getRunButton().setOnAction(e -> clusterRun(randCluster,0));
                    randCluster.run();
                    displayData();
                    runLabel = new Label(increment + applicationTemplate.manager.getPropertyValue(OUT.name()) + clusterA[0] + applicationTemplate.manager.getPropertyValue(ITERATIONS.name()));
                    runLabel.getStylesheets().add(applicationTemplate.manager.getPropertyValue(CSS_PATH.name()));
                    runBox.getChildren().add(runLabel);
                    ((AppUI)applicationTemplate.getUIComponent()).getScrnshotButton().setDisable(false);
                }
                else{
                    increment = 0;
                    ((AppUI)applicationTemplate.getUIComponent()).getVisibleChart().getData().clear();
                    Task task = new Task(){
                        @Override
                        protected Void call(){
                            for(int i = clusterA[1];i<=clusterA[0];i+=clusterA[1]){
                                Platform.runLater(() -> {
                                    ((AppUI)applicationTemplate.getUIComponent()).getVisibleChart().getData().clear();
                                    randCluster.run();
                                    displayData();
                                    runBox.getChildren().clear();
                                    increment += clusterA[1];
                                    runLabel = new Label(increment + applicationTemplate.manager.getPropertyValue(OUT.name()) + clusterA[0] + applicationTemplate.manager.getPropertyValue(ITERATIONS.name()));
                                    runLabel.getStylesheets().add(applicationTemplate.manager.getPropertyValue(CSS_PATH.name()));
                                    runBox.getChildren().add(runLabel);
                                });
                                try {
                                    Thread.sleep(2000);
                                } catch (InterruptedException ie) {
                                    ie.printStackTrace();
                                }


                            }
                            increment = 0;
                            ((AppUI)applicationTemplate.getUIComponent()).settingsA.setDisable(false);
                            ((AppUI)applicationTemplate.getUIComponent()).settingsB.setDisable(false);
                            ((AppUI)applicationTemplate.getUIComponent()).getDisplayButton().setDisable(false);
                            ((AppUI)applicationTemplate.getUIComponent()).getScrnshotButton().setDisable(false);
                            return null;
                        }
                    };
                    Thread thread = new Thread(task);
                    thread.start();
                }
                break;
            case 4:
                exitCase=clusterB[0];
                ((AppUI)applicationTemplate.getUIComponent()).settingsA.setDisable(true);
                ((AppUI)applicationTemplate.getUIComponent()).settingsB.setDisable(true);
                increment = clusterB[1];
                dataset = DataSet.dataProcess(processor.labelInstance());
                klass = Class.forName(applicationTemplate.manager.getPropertyValue(KMEANSCLUSTERER.name()));
                konstructor = klass.getConstructors()[0];
                KMeansClusterer kCluster = (KMeansClusterer) konstructor.newInstance(dataset, clusterB[0], clusterB[1],numClustersB,conB);
                if(!conB){
                    ((AppUI) applicationTemplate.getUIComponent()).getRunButton().setDisable(false);
                    ((AppUI) applicationTemplate.getUIComponent()).getRunButton().setOnAction(e -> clusterRun(kCluster,1));
                    kCluster.run();
                    displayData();
                    runLabel = new Label(increment + applicationTemplate.manager.getPropertyValue(OUT.name()) + clusterB[0] + applicationTemplate.manager.getPropertyValue(ITERATIONS.name()));
                    runLabel.getStylesheets().add(applicationTemplate.manager.getPropertyValue(CSS_PATH.name()));
                    runBox.getChildren().add(runLabel);
                    ((AppUI)applicationTemplate.getUIComponent()).getScrnshotButton().setDisable(false);
                }
                else{
                    increment = 0;
                    ((AppUI)applicationTemplate.getUIComponent()).getVisibleChart().getData().clear();
                    Task task = new Task(){
                        @Override
                        protected Void call(){
                            for(int j = 0;j<clusterB[0];j++){
                                Platform.runLater(() -> {
                                    kCluster.run();
                                    increment++;
                                    if(increment % clusterB[1] == 0) {
                                        ((AppUI)applicationTemplate.getUIComponent()).getVisibleChart().getData().clear();
                                        displayData();
                                        runBox.getChildren().clear();
                                        runLabel = new Label(increment + applicationTemplate.manager.getPropertyValue(OUT.name()) + clusterB[0] + applicationTemplate.manager.getPropertyValue(ITERATIONS.name()));
                                        runLabel.getStylesheets().add(applicationTemplate.manager.getPropertyValue(CSS_PATH.name()));
                                        runBox.getChildren().add(runLabel);
                                    }
                                });
                                    try {
                                        Thread.sleep(500);
                                    } catch (InterruptedException ie) {
                                        ie.printStackTrace();
                                    }
                            }
                            increment = 0;
                            ((AppUI)applicationTemplate.getUIComponent()).settingsA.setDisable(false);
                            ((AppUI)applicationTemplate.getUIComponent()).settingsB.setDisable(false);
                            ((AppUI)applicationTemplate.getUIComponent()).getDisplayButton().setDisable(false);
                            ((AppUI)applicationTemplate.getUIComponent()).getScrnshotButton().setDisable(false);
                            return null;
                        }
                    };
                    Thread thread = new Thread(task);
                    thread.start();
                }
                break;
            case 5:
                break;
        }
    }
     public void saveData(File saveFile){
        try {
            FileWriter dataWriter = new FileWriter(saveFile);
            dataWriter.write(processor.labelInstance());
            dataWriter.close();
        }
        catch(IOException e){
            Dialog saveError = applicationTemplate.getDialog(Dialog.DialogType.ERROR);
            saveError.show(applicationTemplate.manager.getPropertyValue(SAVE_ERROR_TITLE.name()),
                    applicationTemplate.manager.getPropertyValue(SAVE_ERROR_MSG.name()));
        }
    }

    @Override
    public void clear() {
        processor.clear();
    }

    public TSDProcessor getProcessor() {
        return processor;
    }

    private void displayData() {
        processData();
        ((AppUI)applicationTemplate.getUIComponent()).xAxis.setAutoRanging(false);
        ((AppUI)applicationTemplate.getUIComponent()).yAxis.setAutoRanging(false);
        ((AppUI)applicationTemplate.getUIComponent()).xAxis.setLowerBound(getMinX(((AppUI) applicationTemplate.getUIComponent()).getChart())-1);
        ((AppUI)applicationTemplate.getUIComponent()).xAxis.setUpperBound(getMaxX(((AppUI) applicationTemplate.getUIComponent()).getChart())+1);
        ((AppUI)applicationTemplate.getUIComponent()).yAxis.setLowerBound(getMinY(((AppUI) applicationTemplate.getUIComponent()).getChart())-1);
        ((AppUI)applicationTemplate.getUIComponent()).yAxis.setUpperBound(getMaxY(((AppUI) applicationTemplate.getUIComponent()).getChart())+1);
        ((AppUI)applicationTemplate.getUIComponent()).xAxis.setTickUnit((getMaxX(((AppUI) applicationTemplate.getUIComponent()).getChart())-getMinX(((AppUI) applicationTemplate.getUIComponent()).getChart()))/5);
        ((AppUI)applicationTemplate.getUIComponent()).yAxis.setTickUnit((getMaxY(((AppUI) applicationTemplate.getUIComponent()).getChart())-getMinY(((AppUI) applicationTemplate.getUIComponent()).getChart()))/5);
        dataset.toChartData(((AppUI) applicationTemplate.getUIComponent()).getVisibleChart());
    }
    public void processData() {
        processor.toChartData(((AppUI) applicationTemplate.getUIComponent()).getChart());
    }

    /*public static Double getAverageY(LineChart<Number,Number> chart){
       Double result = 0.0;
       Double count = 0.0;
        for( XYChart.Series<Number,Number> series: chart.getData()){
            for(XYChart.Data<Number,Number> node: series.getData()){
                result = result + ((Double)(node.getYValue()));
                count++;
            }
        }

        return result/count;
    }*/
    public static Double getMaxX(LineChart<Number,Number> chart){
        Double result = 0.0;
        for( XYChart.Series<Number,Number> series: chart.getData()){
            for(XYChart.Data<Number,Number> node: series.getData()){
                if(((Double)(node.getXValue()))>result)
                result = ((Double)(node.getXValue()));
            }
        }

        return result;
    }
    private static Double getMaxY(LineChart<Number,Number> chart){
        Double result = 0.0;
        for( XYChart.Series<Number,Number> series: chart.getData()){
            for(XYChart.Data<Number,Number> node: series.getData()){
                if(((Double)(node.getYValue()))>result)
                    result = ((Double)(node.getYValue()));
            }
        }

        return result;
    }
    public static Double getMinY(LineChart<Number,Number> chart){
        Double result = 100.0;
        for( XYChart.Series<Number,Number> series: chart.getData()){
            for(XYChart.Data<Number,Number> node: series.getData()){
                if(((Double)(node.getYValue()))< result)
                    result = ((Double)(node.getYValue()));
            }
        }

        return result;
    }
    public static Double getMinX(LineChart<Number,Number> chart){
        Double result = 100.0;
        for( XYChart.Series<Number,Number> series: chart.getData()){
            for(XYChart.Data<Number,Number> node: series.getData()){
                if(((Double)(node.getXValue()))<result)
                    result = ((Double)(node.getXValue()));
            }
        }

        return result;
    }


}
