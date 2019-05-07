package ui;

import actions.AppActions;
import com.sun.javafx.charts.Legend;
import dataprocessors.AppData;

import javafx.scene.Scene;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import vilij.components.Dialog;
import vilij.propertymanager.PropertyManager;
import vilij.templates.ApplicationTemplate;
import vilij.templates.UITemplate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


import static settings.AppPropertyTypes.*;
import static vilij.settings.PropertyTypes.*;


/**
 * This is the application's user interface implementation.
 *
 * @author Ritwik Banerjee
 */
@SuppressWarnings("SpellCheckingInspection")
public final class AppUI extends UITemplate {

    /** The application to which this class of actions belongs. */
    private final ApplicationTemplate applicationTemplate;
    private Button                       scrnshotButton; // toolbar button to take a screenshot of the data
    private String                       scrnshoticonPath;
    private LineChart<Number, Number>    chart;          // the chart where data is stored for purpose of processing
    private LineChart<Number, Number>    visibleChart;  // the chart which is seen
    private Button                       displayButton;  // workspace button to display data on the chart
    private TextArea                     textArea;       // text area for new data input
    private Label                        textTitle;
    private Button                       runButton;
    private Pane                         leftColumn;
    private VBox                         side;          //holder of algorithm selection information
    public static Stage                  runConfig;
    public static VBox                   space;
    private static HBox                  labelBox = new HBox();
    private Label textInfo = new Label();
    private static final String SEPARATOR = "/";
    public static final String EMPTY = "";
    private ToggleGroup                  classGroup;
    private ToggleGroup                  clusterGroup;
    public Button                        settingsA;
    public Button                        settingsB;
    public NumberAxis xAxis;
    public NumberAxis yAxis;



    public LineChart<Number, Number> getChart() { return chart; }
    public LineChart<Number, Number> getVisibleChart() { return visibleChart; }



    public TextArea getTextArea() {
        return textArea;
    }
    /*public boolean getHasNewText(){
        return hasNewText;
    }
    public void setHasNewText(Boolean value){
        hasNewText = value;
    }*/

    @SuppressWarnings("WeakerAccess")
    public AppUI(Stage primaryStage, ApplicationTemplate applicationTemplate) {
        super(primaryStage, applicationTemplate);
        this.applicationTemplate = applicationTemplate;
    }

    @Override
    protected void setResourcePaths(ApplicationTemplate applicationTemplate) {
        super.setResourcePaths(applicationTemplate);
        PropertyManager manager = applicationTemplate.manager;
        String iconsPath = SEPARATOR + String.join(SEPARATOR,
                manager.getPropertyValue(GUI_RESOURCE_PATH.name()),
                manager.getPropertyValue(ICONS_RESOURCE_PATH.name()));
        scrnshoticonPath = String.join(SEPARATOR, iconsPath, manager.getPropertyValue(SCREENSHOT_ICON.name()));

    }

    @Override
    protected void setToolBar(ApplicationTemplate applicationTemplate) {
        PropertyManager manager = applicationTemplate.manager;
        newButton = setToolbarButton(newiconPath, manager.getPropertyValue(NEW_TOOLTIP.name()), false);
        saveButton = setToolbarButton(saveiconPath, manager.getPropertyValue(SAVE_TOOLTIP.name()), true);
        loadButton = setToolbarButton(loadiconPath, manager.getPropertyValue(LOAD_TOOLTIP.name()), false);
        exitButton = setToolbarButton(exiticonPath, manager.getPropertyValue(EXIT_TOOLTIP.name()), false);
        scrnshotButton = setToolbarButton(scrnshoticonPath,manager.getPropertyValue(SCREENSHOT_TOOLTIP.name()), true);
        toolBar = new ToolBar(newButton, saveButton, loadButton,scrnshotButton, exitButton);
    }

    @Override
    protected void setToolbarHandlers(ApplicationTemplate applicationTemplate) {
        newButton.setOnAction(e -> applicationTemplate.getActionComponent().handleNewRequest());
        saveButton.setOnAction(e -> applicationTemplate.getActionComponent().handleSaveRequest());
        loadButton.setOnAction(e -> applicationTemplate.getActionComponent().handleLoadRequest());
        exitButton.setOnAction(e -> applicationTemplate.getActionComponent().handleExitRequest());
        scrnshotButton.setOnAction(e -> {
            try {
                ((AppActions)applicationTemplate.getActionComponent()).handleScreenshotRequest();
            } catch (IOException e1) {
                vilij.components.Dialog saveError = applicationTemplate.getDialog(Dialog.DialogType.ERROR);
                saveError.show(applicationTemplate.manager.getPropertyValue(SAVE_ERROR_TITLE.name()),
                        applicationTemplate.manager.getPropertyValue(SAVE_ERROR_MSG.name()));
            }
        });
    }

    @Override
    public void initialize() {
        layout();
        setWorkspaceActions();
    }

    @Override
    public void clear() {
        textArea.clear();
        chart.getData().clear();
        saveButton.setDisable(true);
        scrnshotButton.setDisable(true);

    }

    private void layout() {
        workspace = new HBox();
        appPane.getChildren().add(workspace);
        leftColumn = new VBox();
        xAxis = new NumberAxis();
        yAxis = new NumberAxis();
        xAxis.getStylesheets().add(applicationTemplate.manager.getPropertyValue(CSS_PATH.name()));
        yAxis.getStylesheets().add(applicationTemplate.manager.getPropertyValue(CSS_PATH.name()));
        chart = new LineChart<>(xAxis, yAxis);
        chart.setHorizontalGridLinesVisible(false);
        chart.setHorizontalZeroLineVisible(false);
        chart.setVerticalGridLinesVisible(false);
        chart.setVerticalZeroLineVisible(false);
        chart.getStylesheets().add(applicationTemplate.manager.getPropertyValue(CSS_PATH.name()));
        applicationTemplate.manager.addProperty(GRAPH_TITLE.toString(),
                applicationTemplate.manager.getPropertyValue(GRAPH_TITLE.name()));
        chart.setTitle(applicationTemplate.manager.getPropertyValue(GRAPH_TITLE.name()));
        chart.setScaleY(1.25);
        chart.setTranslateY(100);
        textArea = new TextArea();
        textArea.setScaleX(.8);
        textArea.setScaleY(0.9);
        visibleChart = new LineChart<>(xAxis, yAxis);
        visibleChart.setHorizontalGridLinesVisible(false);
        visibleChart.setHorizontalZeroLineVisible(false);
        visibleChart.setVerticalGridLinesVisible(false);
        visibleChart.setVerticalZeroLineVisible(false);
        visibleChart.getStylesheets().add(applicationTemplate.manager.getPropertyValue(CSS_PATH.name()));
        applicationTemplate.manager.addProperty(GRAPH_TITLE.toString(),
                applicationTemplate.manager.getPropertyValue(GRAPH_TITLE.name()));
        visibleChart.setTitle(applicationTemplate.manager.getPropertyValue(GRAPH_TITLE.name()));
        visibleChart.setScaleY(1.25);
        visibleChart.setTranslateY(100);
        workspace.getChildren().add(leftColumn);
        workspace.getChildren().add(visibleChart);
        textArea.setDisable(true);
        textArea.setVisible(false);
        textTitle = new Label(applicationTemplate.manager.getPropertyValue(TEXT_AREA.name()));
        textTitle.getStylesheets().add(applicationTemplate.manager.getPropertyValue(CSS_PATH.name()));
        textTitle.setVisible(false);
        textTitle.setFont(Font.font(15));
        textTitle.setTranslateY(7);
        leftColumn.getChildren().add(textTitle);
        textTitle.setTranslateX(200);
        leftColumn.getChildren().add(textArea);
        displayButton = new Button();
        leftColumn.getChildren().add(displayButton);
        displayButton.setTranslateX(50);
        displayButton.setText(applicationTemplate.manager.getPropertyValue(DONE.name()));
        displayButton.setDisable(true);
        displayButton.setVisible(false);
        textArea.textProperty().addListener((observable, oldValue, newValue) -> {
            //hasNewText = true;
            saveButton.setDisable(false);
            if(newValue.equals(EMPTY)) {
                saveButton.setDisable(true);
            }
        });
        applicationTemplate.manager.addProperty(DISPLAY.toString(),
                applicationTemplate.manager.getPropertyValue(DISPLAY.name()));
        side = new VBox();
        leftColumn.getChildren().add(side);
        side.setTranslateX(50);
        side.setTranslateY(10);

        applicationTemplate.getUIComponent().getPrimaryWindow().setScene(getPrimaryScene());
        applicationTemplate.getUIComponent().getPrimaryWindow().setTitle(getTitle());


    }

    private VBox algorithm = new VBox();
    public void setAlgorithmInformation(){
        side.getChildren().clear();
        textArea.setDisable(true);
        displayButton.setDisable(true);
        displayButton.setVisible(false);
        textArea.setVisible(true);
        textTitle.setVisible(true);
        Label textInfo=new Label();
        String dataInfo = ((AppData)applicationTemplate.getDataComponent()).getProcessor().getDataLabels().size() +
                applicationTemplate.manager.getPropertyValue(instance.name())+
                ((AppData)applicationTemplate.getDataComponent()).getProcessor().getSeries().size() + applicationTemplate.manager.getPropertyValue(load.name())
                +  "\n" +
                ((AppActions)applicationTemplate.getActionComponent()).getDataFilePath() + "\n" +
                applicationTemplate.manager.getPropertyValue(label.name()) + "\n";
        for(int i = 0; i< ((AppData)applicationTemplate.getDataComponent()).getProcessor().getSeries().size(); i ++){
            dataInfo = dataInfo.concat(applicationTemplate.manager.getPropertyValue(dash.name()) +
                    ((AppData) applicationTemplate.getDataComponent()).getProcessor().getSeries().get(i) + "\n");
        }

        textInfo.setText(dataInfo);
        textInfo.getStylesheets().add(applicationTemplate.manager.getPropertyValue(CSS_PATH.name()));
        side.getChildren().add(textInfo);
        side.getChildren().add(algorithm);

    }
    public void newAlgorithmInformation(){
        side.getChildren().clear();
        textArea.setVisible(true);
        textTitle.setVisible(true);
        displayButton.setDisable(false);
        displayButton.setVisible(true);
        textArea.setVisible(true);
        textArea.setDisable(false);
    }
    public void newAlgorithmView(){
        side.getChildren().clear();
        Map data = ((AppData)applicationTemplate.getDataComponent()).getProcessor().getDataLabels();
        textInfo=new Label();
        String dataInfo = data.size() + applicationTemplate.manager.getPropertyValue(instance.name()) +
                ((AppData)applicationTemplate.getDataComponent()).getProcessor().getSeries().size() + applicationTemplate.manager.getPropertyValue(labels.name()) + "\n";
        for(int i = 0; i< ((AppData)applicationTemplate.getDataComponent()).getProcessor().getSeries().size(); i ++){
            dataInfo = dataInfo.concat(applicationTemplate.manager.getPropertyValue(dash.name()) + ((AppData) applicationTemplate.getDataComponent()).getProcessor().getSeries().get(i) + "\n");
        }
        textInfo.setText(dataInfo);
        textInfo.getStylesheets().add(applicationTemplate.manager.getPropertyValue(CSS_PATH.name()));
        side.getChildren().add(textInfo);
        side.getChildren().add(algorithm);

    }
    @SuppressWarnings("unchecked")
    public void createAlgorithmSelection(){
        algorithm.getChildren().clear();
        Label algorithmType = new Label(applicationTemplate.manager.getPropertyValue(type.name()));
        algorithmType.getStylesheets().add(applicationTemplate.manager.getPropertyValue(CSS_PATH.name()));
        algorithm.getChildren().add(algorithmType);
        Button classification = new Button(applicationTemplate.manager.getPropertyValue(Classification.name()));
        Button clustering = new Button(applicationTemplate.manager.getPropertyValue(Clustering.name()));
        classification.setOnAction(e -> ((AppActions)applicationTemplate.getActionComponent()).handleClassRequest());
        clustering.setOnAction(e -> ((AppActions)applicationTemplate.getActionComponent()).handleClusterRequest());
        algorithm.getChildren().add(classification);
        algorithm.getChildren().add(clustering);
        int nullCount = 0;
        String data = ((AppData)applicationTemplate.getDataComponent()).getProcessor().labelInstance();
        String[] names = data.split("\n");
        ArrayList<String> labels = new ArrayList();
        for (String name : names) {
            if (name.split("\t")[1].equals(applicationTemplate.manager.getPropertyValue(NULL.name()))) {
                nullCount++;
            }
            if(labels.indexOf(name.split("\t")[1])== -1){
                labels.add(name.split("\t")[1]);
            }

        }
        if(labels.size()-nullCount != 2){
            classification.setDisable(true);
        }
    }

    public void buildAlgorithmselect(int choice){
        algorithm.getChildren().clear();
        classGroup = new ToggleGroup();
        clusterGroup = new ToggleGroup();
        RadioButton aAlgo = new RadioButton(applicationTemplate.manager.getPropertyValue(classificationA.name()));
        aAlgo.getStylesheets().add(applicationTemplate.manager.getPropertyValue(CSS_PATH.name()));
        aAlgo.setToggleGroup(classGroup);
        aAlgo.setUserData(0);
        RadioButton bAlgo = new RadioButton(applicationTemplate.manager.getPropertyValue(classificationB.name()));
        bAlgo.getStylesheets().add(applicationTemplate.manager.getPropertyValue(CSS_PATH.name()));
        bAlgo.setToggleGroup(classGroup);
        bAlgo.setUserData(1);
        RadioButton cAlgo = new RadioButton(applicationTemplate.manager.getPropertyValue(classificationC.name()));
        cAlgo.getStylesheets().add(applicationTemplate.manager.getPropertyValue(CSS_PATH.name()));
        cAlgo.setToggleGroup(classGroup);
        cAlgo.setUserData(2);
        RadioButton dAlgo = new RadioButton(applicationTemplate.manager.getPropertyValue(clusterA.name()));
        dAlgo.getStylesheets().add(applicationTemplate.manager.getPropertyValue(CSS_PATH.name()));
        dAlgo.setToggleGroup(clusterGroup);
        dAlgo.setUserData(3);
        RadioButton eAlgo = new RadioButton(applicationTemplate.manager.getPropertyValue(clusterB.name()));
        eAlgo.getStylesheets().add(applicationTemplate.manager.getPropertyValue(CSS_PATH.name()));
        eAlgo.setToggleGroup(clusterGroup);
        eAlgo.setUserData(4);
        RadioButton fAlgo = new RadioButton(applicationTemplate.manager.getPropertyValue(clusterC.name()));
        fAlgo.getStylesheets().add(applicationTemplate.manager.getPropertyValue(CSS_PATH.name()));
        fAlgo.setToggleGroup(clusterGroup);
        fAlgo.setUserData(5);
        runButton = new Button(applicationTemplate.manager.getPropertyValue(RUN.name()));
        runButton.setDisable(true);
        runButton.setTranslateY(100);
        classGroup.selectedToggleProperty().addListener((ov, old_toggle, new_toggle) -> {
            if (classGroup.getSelectedToggle() != null) {
                        runButton.setDisable(true);
                ((AppActions)applicationTemplate.getActionComponent()).handleButtonRequest((int)classGroup.getSelectedToggle().getUserData());
            }
        });
        clusterGroup.selectedToggleProperty().addListener((ov, old_toggle, new_toggle) -> {
            if (clusterGroup.getSelectedToggle() != null) {
                runButton.setDisable(true);
                ((AppActions)applicationTemplate.getActionComponent()).handleButtonRequest((int)clusterGroup.getSelectedToggle().getUserData());
            }
        });
        if(choice==1){
            Label algorithmChoice = new Label(applicationTemplate.manager.getPropertyValue(Classification.name()));
            algorithmChoice.getStylesheets().add(applicationTemplate.manager.getPropertyValue(CSS_PATH.name()));
            algorithm.getChildren().add(algorithmChoice);
            settingsA = new Button(applicationTemplate.manager.getPropertyValue(Settings.name()));
            settingsB = new Button(applicationTemplate.manager.getPropertyValue(Settings.name()));
            Button settingsC = new Button(applicationTemplate.manager.getPropertyValue(Settings.name()));
            settingsA.setOnAction(e -> ((AppActions)applicationTemplate.getActionComponent()).handleSettingsRequest(0));
            settingsB.setOnAction(e -> ((AppActions)applicationTemplate.getActionComponent()).handleSettingsRequest(1));
            settingsC.setOnAction(e -> ((AppActions)applicationTemplate.getActionComponent()).handleSettingsRequest(2));
            HBox a = new HBox();
            a.getChildren().add(aAlgo);
            a.getChildren().add(settingsA);
            HBox b = new HBox();
            b.getChildren().add(bAlgo);
            b.getChildren().add(settingsB);
            HBox c = new HBox();
            c.getChildren().add(cAlgo);
            c.getChildren().add(settingsC);
            b.setTranslateY(10);
            c.setTranslateY(20);
            algorithm.getChildren().add(a);
            //algorithm.getChildren().add(b);
            //algorithm.getChildren().add(c);
        }
        else{
            Label algorithmChoice = new Label(applicationTemplate.manager.getPropertyValue(Clustering.name()));
            algorithmChoice.getStylesheets().add(applicationTemplate.manager.getPropertyValue(CSS_PATH.name()));
            algorithm.getChildren().add(algorithmChoice);
            settingsA = new Button(applicationTemplate.manager.getPropertyValue(Settings.name()));
            settingsB = new Button(applicationTemplate.manager.getPropertyValue(Settings.name()));
            Button settingsC = new Button(applicationTemplate.manager.getPropertyValue(Settings.name()));
            settingsA.setOnAction(e -> ((AppActions)applicationTemplate.getActionComponent()).handleSettingsRequest(3));
            settingsB.setOnAction(e -> ((AppActions)applicationTemplate.getActionComponent()).handleSettingsRequest(4));
            settingsC.setOnAction(e -> ((AppActions)applicationTemplate.getActionComponent()).handleSettingsRequest(5));
            HBox a = new HBox();
            a.getChildren().add(dAlgo);
            a.getChildren().add(settingsA);
            HBox b = new HBox();
            b.getChildren().add(eAlgo);
            b.getChildren().add(settingsB);
            HBox c = new HBox();
            c.getChildren().add(fAlgo);
            c.getChildren().add(settingsC);
            b.setTranslateY(10);
            c.setTranslateY(20);
            algorithm.getChildren().add(a);
            algorithm.getChildren().add(b);
            //algorithm.getChildren().add(c);

        }
        algorithm.getChildren().add(runButton);

    }

    public void createRunConfig(int max,int iterations,boolean continuous,int option,int cluster,int numClusters){
        runConfig = new Stage();
        space = new VBox();
        space.setMinHeight(120);
        Scene scene = new Scene(space);
        runConfig.setScene(scene);
        Label title = new Label(applicationTemplate.manager.getPropertyValue(config.name()));
        title.getStylesheets().add(applicationTemplate.manager.getPropertyValue(CSS_PATH.name()));
        title.setTranslateX(50);
        space.getChildren().add(title);
        HBox iteration = new HBox();
        TextField clusterNum = new TextField(String.valueOf(numClusters));
        if(cluster == 1){
            space.setMinHeight(150);
            HBox clusters = new HBox();
            Label label = new Label();
            String text = applicationTemplate.manager.getPropertyValue(numLabels.toString());
            label.setText(text);
            label.getStylesheets().add(applicationTemplate.manager.getPropertyValue(CSS_PATH.name()));
            space.getChildren().add(label);
            clusters.getChildren().add(label);
            clusters.getChildren().add(clusterNum);
            space.getChildren().add(clusters);
        }
        Label label = new Label(applicationTemplate.manager.getPropertyValue(maximum.name()));
        label.getStylesheets().add(applicationTemplate.manager.getPropertyValue(CSS_PATH.name()));
        TextField config = new TextField(String.valueOf(max));
        iteration.getChildren().add(label);
        iteration.getChildren().add(config);
        space.getChildren().add(iteration);
        HBox interval = new HBox();
        Label labelInt = new Label(applicationTemplate.manager.getPropertyValue(intervals.name()));
        labelInt.getStylesheets().add(applicationTemplate.manager.getPropertyValue(CSS_PATH.name()));
        TextField inter = new TextField(String.valueOf(iterations));
        interval.getChildren().add(labelInt);
        interval.getChildren().add(inter);
        space.getChildren().add(interval);
        CheckBox continueRun = new CheckBox(applicationTemplate.manager.getPropertyValue(contRun.name()));
        continueRun.getStylesheets().add(applicationTemplate.manager.getPropertyValue(CSS_PATH.name()));
        continueRun.setAllowIndeterminate(false);
        continueRun.setSelected(continuous);
        space.getChildren().add(continueRun);
        Button okButton = new Button(applicationTemplate.manager.getPropertyValue(OK.name()));
        okButton.setOnAction(e -> ((AppActions)applicationTemplate.getActionComponent()).handleOkRequest(config,inter,continueRun,clusterNum,option));
        space.getChildren().add(okButton);
        space.getChildren().add(labelBox);
        runConfig.setResizable(false);
        runConfig.show();
    }


    private void setWorkspaceActions() {
        displayButton.setOnAction(e -> ((AppActions)applicationTemplate.getActionComponent()).handleDisplayRequest());
        //lockText.setAllowIndeterminate(false);
        //lockText.setOnAction(e -> ((AppActions)applicationTemplate.getActionComponent()).handleCheckRequest());

    }
    public Button getSaveButton(){
        return saveButton;
    }
    public Button getScrnshotButton(){
        return scrnshotButton;
    }
    public Button getRunButton(){
        return runButton;
    }
    public Button getDisplayButton(){return displayButton;}
    public Pane getLeftColumn(){
        return leftColumn;
    }
    public VBox getAlgorithm(){
        return algorithm;
    }
    public Label getTextInfo(){
        return textInfo;
    }
    public ToggleGroup getClassGroup(){return classGroup;}
    public ToggleGroup getClusterGroup(){return clusterGroup;}




    public void plotLine(LineChart<Number,Number> chart, List<Integer> list){
        Double point = (AppData.getMaxX(chart)* list.get(0) + list.get(2))/(list.get(1).doubleValue());
        for(XYChart.Series<Number,Number> series : chart.getData()){
            if(series.getName().equals(applicationTemplate.manager.getPropertyValue(AVERAGE.name()))){
                chart.getData().remove(series);
            }
        }
        XYChart.Series<Number,Number> average = new XYChart.Series<>();
        average.setName(applicationTemplate.manager.getPropertyValue(AVERAGE.name()));
        average.getData().add(new XYChart.Data<>(AppData.getMaxX(chart), AppData.getMinY(chart)));
        average.getData().add(new XYChart.Data<>((AppData.getMinX(chart)), point));
        average.setName(EMPTY);
        chart.getData().add(average);
        javafx.scene.Node line = average.getNode().lookup(applicationTemplate.manager.getPropertyValue(LINE.name()));
        line.setStyle(applicationTemplate.manager.getPropertyValue(BLUE.name()));
        for(XYChart.Data<Number,Number> node: average.getData()){
            javafx.scene.Node symbol = node.getNode().lookup(applicationTemplate.manager.getPropertyValue(SYMBOL.name()));
            symbol.setStyle(applicationTemplate.manager.getPropertyValue(TRANSPARENT.name()));
        }
        for(javafx.scene.Node child : chart.getChildrenUnmodifiable()){
            if(child instanceof Legend){
                for(Legend.LegendItem labels : ((Legend)child).getItems()){
                    if(labels.getText().equals(EMPTY)) {
                        labels.getSymbol().setStyle(applicationTemplate.manager.getPropertyValue(TRANSPARENT.name()));
                    }
                }
            }
        }

    }
}
