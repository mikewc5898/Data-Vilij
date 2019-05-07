package data;
import dataprocessors.TSDProcessor;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import vilij.propertymanager.PropertyManager;


import java.util.*;
import java.util.stream.Stream;

import static settings.AppPropertyTypes.LABEL;

/**
 * This class specifies how an algorithm will expect the dataset to be. It is
 * provided as a rudimentary structure only, and does not include many of the
 * sanity checks and other requirements of the use cases. As such, you can
 * completely write your own class to represent a set of data instances as long
 * as the algorithm can read from and write into two {@link java.util.Map}
 * objects representing the name-to-label map and the name-to-location (i.e.,
 * the x,y values) map. These two are the {@link DataSet#labels} and
 * {@link DataSet#locations} maps in this class.
 *
 * @author Ritwik Banerjee
 */
public class DataSet {

    private static class InvalidDataNameException extends Exception {

        private static final String NAME_ERROR_MSG = "All data instance names must start with the @ character.";

        private InvalidDataNameException(String name) {
            super(String.format("Invalid name '%s'." + NAME_ERROR_MSG, name));
        }
    }

    private static String nameFormatCheck(String name) throws InvalidDataNameException {
        if (!name.startsWith("@"))
            throw new InvalidDataNameException(name);
        return name;
    }

    private static Point2D locationOf(String locationString) {
        String[] coordinateStrings = locationString.trim().split(",");
        return new Point2D(Double.parseDouble(coordinateStrings[0]), Double.parseDouble(coordinateStrings[1]));
    }

    private Map<String, String>  labels;
    private Map<String, Point2D> locations;
    private final Map<Point2D, String> dataName;

    /** Creates an empty dataset. */
    public DataSet() {
        labels = new HashMap<>();
        locations = new HashMap<>();
        dataName = new LinkedHashMap<>();
    }

    public Map<String, String> getLabels()     { return labels; }

    public Map<String, Point2D> getLocations() { return locations; }

    public void updateLabel(String instanceName, String newlabel) {
        if (labels.get(instanceName) == null)
            throw new NoSuchElementException();
       labels.put(instanceName, newlabel);
    }

    private void addInstance(String tsdLine) throws InvalidDataNameException {
        String[] arr = tsdLine.split("\t");
        labels.put(nameFormatCheck(arr[0]), arr[1]);
        locations.put(arr[0], locationOf(arr[2]));
        dataName.put(locationOf(arr[2]),arr[0]);
    }

    public static DataSet dataProcess(String text){
        DataSet dataset = new DataSet();
       Stream.of(text.split("\n")).forEach(line -> {
            try {
                dataset.addInstance(line);
            } catch (InvalidDataNameException e) {
                e.printStackTrace();
            }
        });
        return dataset;
    }
    public void toChartData(XYChart<Number, Number> chart) {
        Set<String> label = new HashSet<>(labels.values());
        for (String eachLabel : label) {
            XYChart.Series<Number, Number> series = new XYChart.Series<>();
            series.setName(eachLabel);
            labels.entrySet().stream().filter(entry -> entry.getValue().equals(eachLabel)).forEach(entry -> {
                Point2D point = locations.get(entry.getKey());
                XYChart.Data<Number,Number> plot = new XYChart.Data<>(point.getX(), point.getY());
                plot.setNode(nodeMaker(point));
                series.getData().add(plot);
            });
            chart.getData().add(series);
        }
    }
    private StackPane nodeMaker(Point2D point){
        StackPane boxPoint = new StackPane();
        Label label = new Label(dataName.get(point));
        label.setMinSize(Label.USE_PREF_SIZE, Label.USE_PREF_SIZE);
        boxPoint.setOnMouseEntered(mouseEvent -> {
            VBox container = new VBox();
            container.getChildren().add(label);
            container.setLayoutX(Label.USE_PREF_SIZE);
            container.setLayoutY(Label.USE_PREF_SIZE);
            container.setStyle(PropertyManager.getManager().getPropertyValue(LABEL.name()));
            boxPoint.getChildren().setAll(container);
            boxPoint.setCursor(Cursor.NONE);
            boxPoint.toFront();
        });
        boxPoint.setOnMouseExited(mouseEvent -> {
            boxPoint.getChildren().clear();
            boxPoint.setCursor(Cursor.DEFAULT);
        });
        return boxPoint;
    }
}
