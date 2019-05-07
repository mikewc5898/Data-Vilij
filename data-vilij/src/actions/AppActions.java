package actions;


import dataprocessors.AppData;
import dataprocessors.TSDProcessor;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.image.WritableImage;

import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import ui.AppUI;
import vilij.components.ActionComponent;
import vilij.components.ConfirmationDialog;
import vilij.components.Dialog;
import vilij.templates.ApplicationTemplate;
import javafx.stage.FileChooser;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.stream.Stream;
import javafx.scene.control.TextField;
import static settings.AppPropertyTypes.*;
import static vilij.settings.PropertyTypes.*;

/**
 * This is the concrete implementation of the action handlers required by the application.
 *
 * @author Ritwik Banerjee
 */
@SuppressWarnings("SpellCheckingInspection")
public final class AppActions implements ActionComponent {

    /** The application to which this class of actions belongs. */
    private final ApplicationTemplate applicationTemplate;

    /** Path to the data file currently active. */
    private Path dataFilePath;
    private HBox labelBox = new HBox();




    private String fullData(){
        String data = ((AppUI) applicationTemplate.getUIComponent()).getTextArea().getText();
        if(AppData.line.get()>= 10) {
            int outsideText = AppData.line.get();
            while(outsideText < AppData.names.size()){
                data = data.concat("\n").concat(AppData.names.get(outsideText));
                outsideText++;
            }
        }
        return data;
    }
    public AppActions(ApplicationTemplate applicationTemplate) {
        this.applicationTemplate = applicationTemplate;
    }

    public void handleDisplayRequest(){
        String data = ((AppUI)applicationTemplate.getUIComponent()).getTextArea().getText();
        ((AppUI)applicationTemplate.getUIComponent()).getAlgorithm().getChildren().clear();
        ((AppUI)applicationTemplate.getUIComponent()).getTextInfo().setText(AppUI.EMPTY);
        ((AppData) applicationTemplate.getDataComponent()).getProcessor().clear();
        ((AppUI) applicationTemplate.getUIComponent()).getChart().getData().clear();
        if(!data.equals(AppUI.EMPTY)) {
            try {
                int formCheck = lineCheck(data);
                if(formCheck == -1) {
                    String dupeCheck = nameCheck(data);
                    if (dupeCheck.equals(AppUI.EMPTY)) {
                        ((AppData) applicationTemplate.getDataComponent()).loadData(data);
                        ((AppData) applicationTemplate.getDataComponent()).processData();
                        if(((AppUI)applicationTemplate.getUIComponent()).getTextArea().isDisable()){
                            ((AppUI)applicationTemplate.getUIComponent()).getTextArea().setDisable(false);
                            ((AppUI)applicationTemplate.getUIComponent()).getSaveButton().setDisable(true);
                        }
                        else{
                            ((AppUI)applicationTemplate.getUIComponent()).getTextArea().setDisable(true);
                            ((AppUI)applicationTemplate.getUIComponent()).newAlgorithmView();
                            ((AppUI)applicationTemplate.getUIComponent()).createAlgorithmSelection();
                            }
                    }
                    else {
                        ((AppData) applicationTemplate.getDataComponent()).getProcessor().clear();
                        ((AppUI) applicationTemplate.getUIComponent()).getChart().getData().clear();
                        Dialog saveError = applicationTemplate.getDialog(Dialog.DialogType.ERROR);
                        saveError.show(applicationTemplate.manager.getPropertyValue(SAVE_ERROR_TITLE.name()),
                                applicationTemplate.manager.getPropertyValue(DUPLICATE_ERROR_MSG.name()) + dupeCheck);

                        }
                } else{
                    Dialog saveError = applicationTemplate.getDialog(Dialog.DialogType.ERROR);
                    saveError.show(applicationTemplate.manager.getPropertyValue(SAVE_ERROR_TITLE.name()),
                            applicationTemplate.manager.getPropertyValue(LINESAVE_ERROR_MSG.name()) + formCheck);
                    }
            } catch (Exception e) {
                ((AppData) applicationTemplate.getDataComponent()).getProcessor().clear();
                ((AppUI) applicationTemplate.getUIComponent()).getChart().getData().clear();
                }
            }
            else{
                ((AppData) applicationTemplate.getDataComponent()).getProcessor().clear();
                ((AppUI) applicationTemplate.getUIComponent()).getChart().getData().clear();
            }

        }

    @Override
    public void handleNewRequest() {
        ((AppUI)applicationTemplate.getUIComponent()).getLeftColumn().setVisible(true);
        ((AppUI)applicationTemplate.getUIComponent()).newAlgorithmInformation();
        applicationTemplate.getUIComponent().clear();
        ((AppUI)applicationTemplate.getUIComponent()).getVisibleChart().getData().clear();
        applicationTemplate.getDataComponent().clear();
        AppData.names = new java.util.ArrayList<>();
        for(double i = 0;i<=AppData.names.size()/5;i++){
            ((AppUI)applicationTemplate.getUIComponent()).getTextArea().clear();
        }
        ((AppUI)applicationTemplate.getUIComponent()).newAlgorithmInformation();
        ((AppUI)applicationTemplate.getUIComponent()).getScrnshotButton().setDisable(true);

    }

    @Override
    public void handleSaveRequest() {
        String nameError = nameCheck(fullData());
        if(nameError.equals(AppUI.EMPTY)) {
            int lineError = lineCheck(fullData());
            if (lineError == -1) {
                Dialog saveDialog = applicationTemplate.getDialog(Dialog.DialogType.CONFIRMATION);
                saveDialog.show(applicationTemplate.manager.getPropertyValue(SAVE_UNSAVED_WORK_TITLE.name()),
                        applicationTemplate.manager.getPropertyValue(SAVE_UNSAVED_WORK.name()));
                if (((ConfirmationDialog) saveDialog).getSelectedOption().equals(ConfirmationDialog.Option.YES) ||
                        ((ConfirmationDialog) saveDialog).getSelectedOption().equals(ConfirmationDialog.Option.NO)) {
                    if (((ConfirmationDialog) saveDialog).getSelectedOption().equals(ConfirmationDialog.Option.YES)) {
                        FileChooser savePrompt = new FileChooser();
                        savePrompt.setTitle(applicationTemplate.manager.getPropertyValue(SAVE_WORK_TITLE.name()));
                        savePrompt.setInitialFileName(applicationTemplate.manager.getPropertyValue(SPECIFIED_FILE.name())
                                + applicationTemplate.manager.getPropertyValue(DATA_FILE.name()));
                        FileChooser.ExtensionFilter fileType = new FileChooser.ExtensionFilter(applicationTemplate.manager
                                .getPropertyValue(DATA_FILE_EXT_DESC.name()), applicationTemplate.manager.getPropertyValue(DATA_FILE_EXT.name()));
                            savePrompt.getExtensionFilters().add(fileType);
                            try {
                                File file = new File(System.getProperty(applicationTemplate.manager.getPropertyValue(USER_DIRECTORY.name()))
                                        + applicationTemplate.manager.getPropertyValue(RESOURCES_RESOURCE_PATH.name())
                                        + applicationTemplate.manager.getPropertyValue(DATA_RESOURCE_PATH.name()));
                                //savePrompt.setInitialDirectory(file);
                            } catch (Exception e) {
                                Dialog saveError = applicationTemplate.getDialog(Dialog.DialogType.ERROR);
                                saveError.show(applicationTemplate.manager.getPropertyValue(SAVE_ERROR_TITLE.name()),
                                        applicationTemplate.manager.getPropertyValue(RESOURCE_SUBDIR_NOT_FOUND.name()));
                            }
                            try {
                                File saveFile = savePrompt.showSaveDialog(applicationTemplate.getUIComponent().getPrimaryWindow());
                                dataFilePath = saveFile.toPath();
                                (applicationTemplate.getDataComponent()).saveData(dataFilePath);
                            } catch (Exception e) {
                                Dialog saveError = applicationTemplate.getDialog(Dialog.DialogType.ERROR);
                                saveError.show(applicationTemplate.manager.getPropertyValue(SAVE_ERROR_TITLE.name()),
                                        applicationTemplate.manager.getPropertyValue(SAVE_ERROR_MSG.name()));
                            }
                        }
                    }

            } else {
                Dialog saveError = applicationTemplate.getDialog(Dialog.DialogType.ERROR);
                saveError.show(applicationTemplate.manager.getPropertyValue(SAVE_ERROR_TITLE.name()),
                        applicationTemplate.manager.getPropertyValue(LINESAVE_ERROR_MSG.name()) + lineError);
            }
        }
        else{
            Dialog saveError = applicationTemplate.getDialog(Dialog.DialogType.ERROR);
            saveError.show(applicationTemplate.manager.getPropertyValue(SAVE_ERROR_TITLE.name()),
                    applicationTemplate.manager.getPropertyValue(DUPLICATE_ERROR_MSG.name()) + nameError);
        }
        ((AppUI)applicationTemplate.getUIComponent()).getSaveButton().setDisable(true);
    }

    @Override
    public void handleLoadRequest() {
        FileChooser loadPrompt = new FileChooser();
        loadPrompt.setTitle(applicationTemplate.manager.getPropertyValue(LOAD_WORK_TITLE.name()));
        loadPrompt.setInitialFileName(applicationTemplate.manager.getPropertyValue(SPECIFIED_FILE.name())
                + applicationTemplate.manager.getPropertyValue(DATA_FILE.name()));
        FileChooser.ExtensionFilter fileType = new FileChooser.ExtensionFilter(applicationTemplate.manager
                .getPropertyValue(DATA_FILE_EXT_DESC.name()),
                applicationTemplate.manager.getPropertyValue(DATA_FILE_EXT.name()));
        loadPrompt.getExtensionFilters().add(fileType);
        try {
            File file = new File(System.getProperty(applicationTemplate.manager.getPropertyValue(USER_DIRECTORY.name()))
                    + applicationTemplate.manager.getPropertyValue(RESOURCES_RESOURCE_PATH.name())
                    + applicationTemplate.manager.getPropertyValue(DATA_RESOURCE_PATH.name()));
            //loadPrompt.setInitialDirectory(file);

            try{
                File loadFile = loadPrompt.showOpenDialog(applicationTemplate.getUIComponent().getPrimaryWindow());
                (applicationTemplate.getDataComponent()).loadData(loadFile.toPath());
                dataFilePath = loadFile.toPath();
                ((AppUI)applicationTemplate.getUIComponent()).setAlgorithmInformation();
                ((AppUI)applicationTemplate.getUIComponent()).createAlgorithmSelection();
                ((AppUI)applicationTemplate.getUIComponent()).getSaveButton().setDisable(true);
                ((AppUI)applicationTemplate.getUIComponent()).getScrnshotButton().setDisable(true);
            }
            catch(Exception e){
                Dialog saveError = applicationTemplate.getDialog(Dialog.DialogType.ERROR);
                saveError.show(applicationTemplate.manager.getPropertyValue(LOAD_ERROR_TITLE.name()),
                        applicationTemplate.manager.getPropertyValue(LOAD_ERROR_MSG.name()));
            }
        } catch (Exception e) {
            Dialog saveError = applicationTemplate.getDialog(Dialog.DialogType.ERROR);
            saveError.show(applicationTemplate.manager.getPropertyValue(SAVE_ERROR_TITLE.name()),
                    applicationTemplate.manager.getPropertyValue(RESOURCE_SUBDIR_NOT_FOUND.name()));
        }
    }

    @Override
    public void handleExitRequest() {
        Stage stage = applicationTemplate.getUIComponent().getPrimaryWindow();
        if(((AppData)applicationTemplate.getDataComponent()).increment<=((AppData)applicationTemplate.getDataComponent()).exitCase && ((AppData)applicationTemplate.getDataComponent()).increment !=0 ) {
            try{
                Dialog exitDialog = applicationTemplate.getDialog(Dialog.DialogType.CONFIRMATION);
                exitDialog.show(applicationTemplate.manager.getPropertyValue(EXIT_TITLE.name()),
                        applicationTemplate.manager.getPropertyValue(ALGORITHM_MSG.name()));
                if (((ConfirmationDialog) exitDialog).getSelectedOption().equals(ConfirmationDialog.Option.YES)) {
                    if (!((AppUI) applicationTemplate.getUIComponent()).getSaveButton().isDisable()) {
                        try {
                            promptToSave();
                            } catch (IOException e) {
                            Dialog saveError = applicationTemplate.getDialog(Dialog.DialogType.ERROR);
                            saveError.show(applicationTemplate.manager.getPropertyValue(SAVE_ERROR_TITLE.name()),
                                    applicationTemplate.manager.getPropertyValue(RESOURCE_SUBDIR_NOT_FOUND.name()));
                            }
                            }
                            stage.close();
                    }
                    } catch(
                            NullPointerException e)
            {
                handlePrintRequest();
                }
        }
        else if(!((AppUI)applicationTemplate.getUIComponent()).getSaveButton().isDisable()){
            try {
                if(promptToSave()){
                    stage.close();
                }
            } catch (IOException e) {
                Dialog saveError = applicationTemplate.getDialog(Dialog.DialogType.ERROR);
                saveError.show(applicationTemplate.manager.getPropertyValue(SAVE_ERROR_TITLE.name()),
                        applicationTemplate.manager.getPropertyValue(RESOURCE_SUBDIR_NOT_FOUND.name()));
            }
        }
        else {
            stage.close();
        }
    }

    @Override
    public void handlePrintRequest() {

    }

    public void handleOkRequest(TextField iteration, TextField interval, CheckBox continuous,TextField numClusters, int option){
        switch(option){
            case 0:
                try {
                    ((AppData) applicationTemplate.getDataComponent()).contA = continuous.isSelected();
                    if (Integer.parseInt(iteration.getText()) > 0) {
                        ((AppData) applicationTemplate.getDataComponent()).classA[0] = Integer.parseInt(iteration.getText());
                    }
                    else throw new NumberFormatException();
                    if (Integer.parseInt(interval.getText()) > 0 && Integer.parseInt(interval.getText()) <= Integer.parseInt(iteration.getText())) {
                        ((AppData) applicationTemplate.getDataComponent()).classA[1] = Integer.parseInt(interval.getText());
                    }
                    else throw new NumberFormatException();
                    AppUI.runConfig.close();
                }
                catch(NumberFormatException e){
                    Label label = new Label(applicationTemplate.manager.getPropertyValue(INVALID_DATA.name()));
                    label.getStylesheets().add(applicationTemplate.manager.getPropertyValue(CSS_PATH.name()));
                    AppUI.space.getChildren().remove(labelBox);
                    labelBox.getChildren().clear();
                    labelBox.getChildren().add(label);
                    AppUI.space.getChildren().add(labelBox);
                }
                break;
            case 1:
                try {
                    ((AppData) applicationTemplate.getDataComponent()).contB = continuous.isSelected();
                    if (Integer.parseInt(iteration.getText()) > 0) {
                        ((AppData) applicationTemplate.getDataComponent()).classB[0] = Integer.parseInt(iteration.getText());
                    }
                    else throw new NumberFormatException();
                    if (Integer.parseInt(interval.getText()) > 0 && Integer.parseInt(interval.getText()) <= Integer.parseInt(iteration.getText())) {
                        ((AppData) applicationTemplate.getDataComponent()).classB[1] = Integer.parseInt(interval.getText());
                    }
                    else throw new NumberFormatException();
                    AppUI.runConfig.close();
                }
                catch(NumberFormatException e){
                    Label label = new Label(applicationTemplate.manager.getPropertyValue(INVALID_DATA.name()));
                    label.getStylesheets().add(applicationTemplate.manager.getPropertyValue(CSS_PATH.name()));
                    AppUI.space.getChildren().remove(labelBox);
                    labelBox.getChildren().clear();
                    labelBox.getChildren().add(label);
                    AppUI.space.getChildren().add(labelBox);
                }
                break;
            case 2:
                try {
                    ((AppData) applicationTemplate.getDataComponent()).contC = continuous.isSelected();
                    if (Integer.parseInt(iteration.getText()) > 0) {
                        ((AppData) applicationTemplate.getDataComponent()).classC[0] = Integer.parseInt(iteration.getText());
                    }
                    else throw new NumberFormatException();
                    if (Integer.parseInt(interval.getText()) > 0 && Integer.parseInt(interval.getText()) <= Integer.parseInt(iteration.getText())) {
                        ((AppData) applicationTemplate.getDataComponent()).classC[1] = Integer.parseInt(interval.getText());
                    }
                    else throw new NumberFormatException();
                    AppUI.runConfig.close();
                }
                catch(NumberFormatException e){
                    Label label = new Label(applicationTemplate.manager.getPropertyValue(INVALID_DATA.name()));
                    label.getStylesheets().add(applicationTemplate.manager.getPropertyValue(CSS_PATH.name()));
                    AppUI.space.getChildren().remove(labelBox);
                    labelBox.getChildren().clear();
                    labelBox.getChildren().add(label);
                    AppUI.space.getChildren().add(labelBox);
                }
                break;
            case 3:
                try {
                    ((AppData) applicationTemplate.getDataComponent()).conA = continuous.isSelected();
                    if (Integer.parseInt(iteration.getText()) > 0) {
                        ((AppData) applicationTemplate.getDataComponent()).clusterA[0] = Integer.parseInt(iteration.getText());
                    }
                    else throw new NumberFormatException();
                    if (Integer.parseInt(interval.getText()) > 0 && Integer.parseInt(interval.getText()) <= Integer.parseInt(iteration.getText())) {
                        ((AppData) applicationTemplate.getDataComponent()).clusterA[1] = Integer.parseInt(interval.getText());
                    }
                    else throw new NumberFormatException();
                    if(Integer.parseInt(numClusters.getText())>0) {
                        ((AppData) applicationTemplate.getDataComponent()).numClustersA = Integer.parseInt(numClusters.getText());
                    }
                    else throw new NumberFormatException();
                    AppUI.runConfig.close();
                }
                catch(NumberFormatException e){
                    Label label = new Label(applicationTemplate.manager.getPropertyValue(INVALID_DATA.name()));
                    label.getStylesheets().add(applicationTemplate.manager.getPropertyValue(CSS_PATH.name()));
                    AppUI.space.getChildren().remove(labelBox);
                    labelBox.getChildren().clear();
                    labelBox.getChildren().add(label);
                    AppUI.space.getChildren().add(labelBox);
                }
                break;
            case 4:
                try {
                    ((AppData) applicationTemplate.getDataComponent()).conB = continuous.isSelected();
                    if (Integer.parseInt(iteration.getText()) > 0) {
                        ((AppData) applicationTemplate.getDataComponent()).clusterB[0] = Integer.parseInt(iteration.getText());
                    }
                    else throw new NumberFormatException();
                    if (Integer.parseInt(interval.getText()) > 0 && Integer.parseInt(interval.getText()) <= Integer.parseInt(iteration.getText())) {
                        ((AppData) applicationTemplate.getDataComponent()).clusterB[1] = Integer.parseInt(interval.getText());
                    }
                    else throw new NumberFormatException();
                    if(Integer.parseInt(numClusters.getText())>0) {
                        ((AppData) applicationTemplate.getDataComponent()).numClustersB = Integer.parseInt(numClusters.getText());
                    }
                    else throw new NumberFormatException();
                    AppUI.runConfig.close();
                }
                catch(NumberFormatException e){
                    Label label = new Label(applicationTemplate.manager.getPropertyValue(INVALID_DATA.name()));
                    label.getStylesheets().add(applicationTemplate.manager.getPropertyValue(CSS_PATH.name()));
                    AppUI.space.getChildren().remove(labelBox);
                    labelBox.getChildren().clear();
                    labelBox.getChildren().add(label);
                    AppUI.space.getChildren().add(labelBox);
                }
                break;
            case 5:
                try {
                    ((AppData) applicationTemplate.getDataComponent()).conC = continuous.isSelected();
                    if (Integer.parseInt(iteration.getText()) > 0) {
                        ((AppData) applicationTemplate.getDataComponent()).clusterC[0] = Integer.parseInt(iteration.getText());
                    }
                    else throw new NumberFormatException();
                    if (Integer.parseInt(interval.getText()) > 0 && Integer.parseInt(interval.getText()) <= Integer.parseInt(iteration.getText())) {
                        ((AppData) applicationTemplate.getDataComponent()).clusterC[1] = Integer.parseInt(interval.getText());
                    }
                    else throw new NumberFormatException();
                    if(Integer.parseInt(numClusters.getText())>0) {
                        ((AppData) applicationTemplate.getDataComponent()).numClustersC = Integer.parseInt(numClusters.getText());
                    }
                    else throw new NumberFormatException();
                    AppUI.runConfig.close();
                }
                catch(NumberFormatException e){
                    Label label = new Label(applicationTemplate.manager.getPropertyValue(INVALID_DATA.name()));
                    label.getStylesheets().add(applicationTemplate.manager.getPropertyValue(CSS_PATH.name()));
                    AppUI.space.getChildren().remove(labelBox);
                    labelBox.getChildren().clear();
                    labelBox.getChildren().add(label);
                    AppUI.space.getChildren().add(labelBox);
                }
                break;
        }
    }

    public void handleClassRequest() {
        ((AppUI)applicationTemplate.getUIComponent()).buildAlgorithmselect(1);
    }
    public void handleClusterRequest() {
        ((AppUI)applicationTemplate.getUIComponent()).buildAlgorithmselect(2);
    }
    public void handleButtonRequest(int button){
        ((AppUI)applicationTemplate.getUIComponent()).getAlgorithm().getChildren().add(labelBox);
        Label labelRun = new Label(applicationTemplate.manager.getPropertyValue(RUN_CONFIG.name()));
        labelRun.getStylesheets().add(applicationTemplate.manager.getPropertyValue(CSS_PATH.name()));
        labelBox.getChildren().clear();
        switch(button){
            case 0:
                labelBox.getChildren().clear();
                if(((AppData)applicationTemplate.getDataComponent()).classA[0] > 0 &&
                ((AppData)applicationTemplate.getDataComponent()).classA[1] > 0){
                    ((AppUI)applicationTemplate.getUIComponent()).getRunButton().setDisable(false);
                    labelBox.getChildren().clear();
                    ((AppUI)applicationTemplate.getUIComponent()).getRunButton().setOnAction(e -> ((AppActions)applicationTemplate.getActionComponent()).handleRunRequest(0));
                }
                else{
                    labelBox.getChildren().add(labelRun);
                }
                ((AppUI)applicationTemplate.getUIComponent()).getAlgorithm().getChildren().remove(labelBox);
                break;
            case 1:
                if(((AppData)applicationTemplate.getDataComponent()).classB[0] > 0 &&
                        ((AppData)applicationTemplate.getDataComponent()).classB[1] > 0){
                    ((AppUI)applicationTemplate.getUIComponent()).getRunButton().setDisable(false);
                    ((AppUI)applicationTemplate.getUIComponent()).getRunButton().setOnAction(e -> ((AppActions)applicationTemplate.getActionComponent()).handleRunRequest(1));
                }
                break;
            case 2:
                if(((AppData)applicationTemplate.getDataComponent()).classC[0] > 0 &&
                        ((AppData)applicationTemplate.getDataComponent()).classC[1] > 0){
                    ((AppUI)applicationTemplate.getUIComponent()).getRunButton().setDisable(false);
                    ((AppUI)applicationTemplate.getUIComponent()).getRunButton().setOnAction(e -> ((AppActions)applicationTemplate.getActionComponent()).handleRunRequest(2));
                }
                break;
            case 3:
                labelBox.getChildren().clear();
                if(((AppData)applicationTemplate.getDataComponent()).clusterA[0] > 0 &&
                        ((AppData)applicationTemplate.getDataComponent()).clusterA[1] > 0){
                    ((AppUI)applicationTemplate.getUIComponent()).getRunButton().setDisable(false);
                    labelBox.getChildren().clear();
                    ((AppUI)applicationTemplate.getUIComponent()).getRunButton().setOnAction(e -> ((AppActions)applicationTemplate.getActionComponent()).handleRunRequest(3));
                }
                else {
                    labelBox.getChildren().add(labelRun);
                }
                ((AppUI)applicationTemplate.getUIComponent()).getAlgorithm().getChildren().remove(labelBox);
                break;
            case 4:
                labelBox.getChildren().clear();
                if(((AppData)applicationTemplate.getDataComponent()).clusterB[0] > 0 &&
                        ((AppData)applicationTemplate.getDataComponent()).clusterB[1] > 0){
                    ((AppUI)applicationTemplate.getUIComponent()).getRunButton().setDisable(false);
                    labelBox.getChildren().clear();
                    ((AppUI)applicationTemplate.getUIComponent()).getRunButton().setOnAction(e -> ((AppActions)applicationTemplate.getActionComponent()).handleRunRequest(4));
                }
                else {
                    labelBox.getChildren().add(labelRun);
                }
                ((AppUI)applicationTemplate.getUIComponent()).getAlgorithm().getChildren().remove(labelBox);
                break;
            case 5:
                if(((AppData)applicationTemplate.getDataComponent()).clusterC[0] > 0 &&
                        ((AppData)applicationTemplate.getDataComponent()).clusterC[1] > 0){
                    ((AppUI)applicationTemplate.getUIComponent()).getRunButton().setDisable(false);
                    ((AppUI)applicationTemplate.getUIComponent()).getRunButton().setOnAction(e -> ((AppActions)applicationTemplate.getActionComponent()).handleRunRequest(5));
                }
                break;
        }

    }

    public void handleSettingsRequest(int choice){
        ((AppUI)applicationTemplate.getUIComponent()).getAlgorithm().getChildren().remove(labelBox);
        ((AppUI)applicationTemplate.getUIComponent()).getClassGroup().selectToggle(null);
        ((AppUI)applicationTemplate.getUIComponent()).getClusterGroup().selectToggle(null);
        switch(choice){
            case 0:
                ((AppUI)applicationTemplate.getUIComponent()).createRunConfig(((AppData)applicationTemplate.getDataComponent()).classA[0],
                        ((AppData)applicationTemplate.getDataComponent()).classA[1], ((AppData)applicationTemplate.getDataComponent()).contA,0,0,
                        ((AppData)applicationTemplate.getDataComponent()).numClustersA);
                break;
            case 1:
                ((AppUI)applicationTemplate.getUIComponent()).createRunConfig(((AppData)applicationTemplate.getDataComponent()).classB[0],
                        ((AppData)applicationTemplate.getDataComponent()).classB[1], ((AppData)applicationTemplate.getDataComponent()).contB,1,0,
                        ((AppData)applicationTemplate.getDataComponent()).numClustersB);
                break;
            case 2:
                ((AppUI)applicationTemplate.getUIComponent()).createRunConfig(((AppData)applicationTemplate.getDataComponent()).classC[0],
                        ((AppData)applicationTemplate.getDataComponent()).classC[1], ((AppData)applicationTemplate.getDataComponent()).contC,2,0,
                        ((AppData)applicationTemplate.getDataComponent()).numClustersC);
                break;
            case 3:
                ((AppUI)applicationTemplate.getUIComponent()).createRunConfig(((AppData)applicationTemplate.getDataComponent()).clusterA[0],
                        ((AppData)applicationTemplate.getDataComponent()).clusterA[1], ((AppData)applicationTemplate.getDataComponent()).conA,3,1,
                        ((AppData)applicationTemplate.getDataComponent()).numClustersA);
                break;
            case 4:
                ((AppUI)applicationTemplate.getUIComponent()).createRunConfig(((AppData)applicationTemplate.getDataComponent()).clusterB[0],
                        ((AppData)applicationTemplate.getDataComponent()).clusterB[1], ((AppData)applicationTemplate.getDataComponent()).conB,4,1,
                        ((AppData)applicationTemplate.getDataComponent()).numClustersB);
                break;
            case 5:
                ((AppUI)applicationTemplate.getUIComponent()).createRunConfig(((AppData)applicationTemplate.getDataComponent()).clusterC[0],
                        ((AppData)applicationTemplate.getDataComponent()).clusterC[1], ((AppData)applicationTemplate.getDataComponent()).conC,5,1,
                        ((AppData)applicationTemplate.getDataComponent()).numClustersC);
                break;
        }
    }

    public void handleScreenshotRequest() throws IOException {
        WritableImage screenshot = ((AppUI)applicationTemplate.getUIComponent()).getVisibleChart().snapshot(new SnapshotParameters(),
                new WritableImage(500,500));
        FileChooser scrnshotPrompt = new FileChooser();
        scrnshotPrompt.setTitle(applicationTemplate.manager.getPropertyValue(SCREENSHOT_TITLE.name()));
        scrnshotPrompt.setInitialFileName(applicationTemplate.manager.getPropertyValue(SPECIFIED_FILE.name())
                + applicationTemplate.manager.getPropertyValue(IMG_FILE.name()));
        FileChooser.ExtensionFilter fileType = new FileChooser.ExtensionFilter(applicationTemplate.manager
                .getPropertyValue(IMG_FILE_EXT_DESC.name()),
                applicationTemplate.manager.getPropertyValue(IMG_FILE_EXT.name()));
        scrnshotPrompt.getExtensionFilters().add(fileType);
        try {
            File file = new File(System.getProperty(applicationTemplate.manager.getPropertyValue(USER_DIRECTORY.name()))
                    + applicationTemplate.manager.getPropertyValue(RESOURCES_RESOURCE_PATH.name())
                    + applicationTemplate.manager.getPropertyValue(DATA_RESOURCE_PATH.name()));
            //scrnshotPrompt.setInitialDirectory(file);
        } catch (Exception e) {
            Dialog saveError = applicationTemplate.getDialog(Dialog.DialogType.ERROR);
            saveError.show(applicationTemplate.manager.getPropertyValue(SAVE_ERROR_TITLE.name()),
                    applicationTemplate.manager.getPropertyValue(RESOURCE_SUBDIR_NOT_FOUND.name()));
        }
        try {
            File saveFile = scrnshotPrompt.showSaveDialog(applicationTemplate.getUIComponent().getPrimaryWindow());
            BufferedImage chartImage = SwingFXUtils.fromFXImage(screenshot, null);
            BufferedImage colorCorrect = new BufferedImage(chartImage.getWidth(), chartImage.getHeight(), BufferedImage.OPAQUE);
            Graphics2D graphics = colorCorrect.createGraphics();
            graphics.drawImage(chartImage, 0, 0, null);
            ImageIO.write(colorCorrect,applicationTemplate.manager.getPropertyValue(IMG_NAME.name()),saveFile);
            ((AppUI)applicationTemplate.getUIComponent()).getScrnshotButton().setDisable(true);

        } catch (Exception e) {
            throw new IOException();
        }



    }

    private void handleRunRequest(int i){
        ((AppUI)applicationTemplate.getUIComponent()).getDisplayButton().setDisable(true);
        try {
            ((AppData)applicationTemplate.getDataComponent()).runAlgorithm(i);
        } catch (Exception e) {
            Dialog saveError = applicationTemplate.getDialog(Dialog.DialogType.ERROR);
            saveError.show(applicationTemplate.manager.getPropertyValue(REFLECTION_ERROR_TITLE.name()),
                    applicationTemplate.manager.getPropertyValue(REFLECTION_ERROR_MSG.name()));
        }
    }
    /**
     * This helper method verifies that the user really wants to save their unsaved work, which they might not want to
     * do. The user will be presented with three options:
     * <ol>
     * <li><code>yes</code>, indicating that the user wants to save the work and continue with the action,</li>
     * <li><code>no</code>, indicating that the user wants to continue with the action without saving the work, and</li>
     * <li><code>cancel</code>, to indicate that the user does not want to continue with the action, but also does not
     * want to save the work at this point.</li>
     * </ol>
     *
     * @return <code>false</code> if the user presses the <i>cancel</i>, and <code>true</code> otherwise.
     */
     private boolean promptToSave() throws IOException{
        Dialog saveDialog = applicationTemplate.getDialog(Dialog.DialogType.CONFIRMATION);
        saveDialog.show(applicationTemplate.manager.getPropertyValue(SAVE_UNSAVED_WORK_TITLE.name()),
                applicationTemplate.manager.getPropertyValue(SAVE_UNSAVED_WORK.name()));
        if(((ConfirmationDialog)saveDialog).getSelectedOption().equals(ConfirmationDialog.Option.YES) ||
                ((ConfirmationDialog)saveDialog).getSelectedOption().equals(ConfirmationDialog.Option.NO) ){
            if(((ConfirmationDialog)saveDialog).getSelectedOption().equals(ConfirmationDialog.Option.YES)){
                FileChooser savePrompt = new FileChooser();
                savePrompt.setTitle(applicationTemplate.manager.getPropertyValue(SAVE_WORK_TITLE.name()));
                savePrompt.setInitialFileName(applicationTemplate.manager.getPropertyValue(SPECIFIED_FILE.name())
                        + applicationTemplate.manager.getPropertyValue(DATA_FILE.name()));
                FileChooser.ExtensionFilter fileType = new FileChooser.ExtensionFilter(applicationTemplate.manager
                        .getPropertyValue(DATA_FILE_EXT_DESC.name()),
                        applicationTemplate.manager.getPropertyValue(DATA_FILE_EXT.name()));
                savePrompt.getExtensionFilters().add(fileType);

                try {
                   File file = new File(System.getProperty(applicationTemplate.manager.getPropertyValue(USER_DIRECTORY.name()))
                           + applicationTemplate.manager.getPropertyValue(RESOURCES_RESOURCE_PATH.name())
                           + applicationTemplate.manager.getPropertyValue(DATA_RESOURCE_PATH.name()));
                    //savePrompt.setInitialDirectory(file);
                } catch (Exception e) {
                    Dialog saveError = applicationTemplate.getDialog(Dialog.DialogType.ERROR);
                    saveError.show(applicationTemplate.manager.getPropertyValue(SAVE_ERROR_TITLE.name()),
                            applicationTemplate.manager.getPropertyValue(RESOURCE_SUBDIR_NOT_FOUND.name()));
                    return false;
                }
                try {
                    String dupeCheck = nameCheck(fullData());
                    if(dupeCheck.equals(AppUI.EMPTY)) {
                        int errorCheck = lineCheck(fullData());
                        if (errorCheck == -1) {
                            File saveFile = savePrompt.showSaveDialog(applicationTemplate.getUIComponent().getPrimaryWindow());
                            ((AppData)applicationTemplate.getDataComponent()).saveData(saveFile);
                            dataFilePath = saveFile.toPath();
                        } else {
                            Dialog saveError = applicationTemplate.getDialog(Dialog.DialogType.ERROR);
                            saveError.show(applicationTemplate.manager.getPropertyValue(SAVE_ERROR_TITLE.name()),
                                    applicationTemplate.manager.getPropertyValue(LINESAVE_ERROR_MSG.name()) + errorCheck);
                            return false;
                        }
                    }
                    else{
                        Dialog saveError = applicationTemplate.getDialog(Dialog.DialogType.ERROR);
                        saveError.show(applicationTemplate.manager.getPropertyValue(SAVE_ERROR_TITLE.name()),
                                applicationTemplate.manager.getPropertyValue(DUPLICATE_ERROR_MSG.name()) + dupeCheck);
                        return false;
                    }

                }
                catch(NullPointerException e){
                    throw new IOException();
                }


            }
            return true ;
        }
        else return false;
    }
    public int lineCheck(String data){
        int i;
        TSDProcessor processor = new TSDProcessor();
        try {
            processor.processString(data);
            return -1;
        } catch (Exception e) {
            java.util.ArrayList<String> names = new java.util.ArrayList<>();
            Stream.of(data.split("\n"))
                    .map(line -> Arrays.asList(line.split("\t")))
                    .forEach(list -> {
                        String   name  = list.get(0);
                        names.add(name);
                    });
            for(i = 0;i<names.size();i++){
                if(!names.get(i).substring(0,1).equals("@")){
                    break;
                }
            }
           return i + 1;
        }
    }
    public String nameCheck(String data){
        java.util.ArrayList<String> names = new java.util.ArrayList<>();
        Stream.of(data.split("\n"))
                .map(line -> Arrays.asList(line.split("\t")))
                .forEach(list -> {
                    String   name  = list.get(0);
                    names.add(name);
                    });
        String duplicate = AppUI.EMPTY;
        for(int i = 0;i<names.size();i++){
            for(int j = i + 1;j<names.size();j++){
                if(names.get(i).equals(names.get(j))){
                    return names.get(j);
                }
            }
        }
        return duplicate;
    }

    public String getDataFilePath(){
        int i = dataFilePath.toString().indexOf("\\") + 1;
        return dataFilePath.toString().substring(i);
    }

   /* public void handleCheckRequest(){
        if(((AppUI)applicationTemplate.getUIComponent()).getTextArea().isDisable()) {
            ((AppUI) applicationTemplate.getUIComponent()).getTextArea().setDisable(false);
        }
        else{
            ((AppUI) applicationTemplate.getUIComponent()).getTextArea().setDisable(true);
        }
    }*/
}
