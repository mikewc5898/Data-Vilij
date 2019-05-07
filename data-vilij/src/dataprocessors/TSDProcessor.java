package dataprocessors;


import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import ui.AppUI;
import vilij.propertymanager.PropertyManager;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;
import static settings.AppPropertyTypes.LABEL;

/**
 * The data files used by this data visualization applications follow a tab-separated format, where each data point is
 * named, labeled, and has a specific location in the 2-dimensional X-Y plane. This class handles the parsing and
 * processing of such data. It also handles exporting the data to a 2-D plot.
 * <p>
 * A sample file in this format has been provided in the application's <code>resources/data</code> folder.
 *
 * @author Ritwik Banerjee
 * @see XYChart
 */
@SuppressWarnings("SpellCheckingInspection")
public final class TSDProcessor {

    private static class InvalidDataNameException extends Exception {

        private static final String NAME_ERROR_MSG = "All data instance names must start with the @ character.";

        private InvalidDataNameException(String name) {
            super(String.format("Invalid name '%s'." + NAME_ERROR_MSG, name));
        }
    }

    private final LinkedHashMap<String, String>  dataLabels;
    private final Map<String, Point2D> dataPoints;
    private final Map<Point2D, String> dataName;
    //private final Map<Point2D, String> reverseLabels;

    public TSDProcessor() {
        dataLabels = new LinkedHashMap<>();
        dataPoints = new LinkedHashMap<>();
        dataName = new LinkedHashMap<>();
        //reverseLabels = new LinkedHashMap<>();
    }
    /**
     * Processes the data and populated two {@link Map} objects with the data.
     *
     * @param tsdString the input data provided as a single {@link String}
     * @throws Exception if the input string does not follow the <code>.tsd</code> data format
     */
    public void processString(String tsdString) throws Exception {
        clear();
        AtomicBoolean hadAnError   = new AtomicBoolean(false);
        StringBuilder errorMessage = new StringBuilder();
        Stream.of(tsdString.split("\n"))
              .map(line -> Arrays.asList(line.split("\t")))
              .forEach(list -> {
                  try {
                      String   name  = checkedname(list.get(0));
                      String   label = list.get(1);
                      String[] pair  = list.get(2).split(",");
                      Point2D  point = new Point2D(Double.parseDouble(pair[0]), Double.parseDouble(pair[1]));
                      dataLabels.put(name, label);
                      dataPoints.put(name, point);
                      dataName.put(point,name);
                      //reverseLabels.put(point,label);
                  } catch (Exception e) {
                      errorMessage.setLength(0);
                      errorMessage.append(e.getClass().getSimpleName()).append(": ").append(e.getMessage());
                      hadAnError.set(true);
                  }
              });
        if (errorMessage.length() > 0)
            throw new Exception(errorMessage.toString());
    }
    public ArrayList<String> getSeries() {
        ArrayList<String> seriesNames = new ArrayList<>();
        Set<String> labels = new HashSet<>(dataLabels.values());
        for (String label : labels) {
            if(seriesNames.indexOf(label)== -1){
                seriesNames.add(label);
            }
        }
        return seriesNames;
    }

    /**
     * Exports the data to the specified 2-D chart.
     *
     * @param chart the specified chart
     */
    public void toChartData(XYChart<Number, Number> chart) {
        Set<String> labels = new HashSet<>(dataLabels.values());
        for (String label : labels) {
            XYChart.Series<Number, Number> series = new XYChart.Series<>();
            series.setName(label);
            dataLabels.entrySet().stream().filter(entry -> entry.getValue().equals(label)).forEach(entry -> {
                Point2D point = dataPoints.get(entry.getKey());
                XYChart.Data<Number,Number> plot = new XYChart.Data<>(point.getX(), point.getY());
                plot.setNode(nodeMaker(point));
                series.getData().add(plot);
            });
            chart.getData().add(series);
        }
    }

    public void clear() {
        dataPoints.clear();
        dataLabels.clear();
        dataName.clear();
        //reverseLabels.clear();
    }

    private String checkedname(String name) throws InvalidDataNameException {
        if (!name.startsWith("@"))
            throw new InvalidDataNameException(name);
        return name;
    }
    public LinkedHashMap getDataLabels(){
        return dataLabels;
    }
    public Map getDataPoints() {return dataPoints;}



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

    public String labelInstance(){
        StringBuilder chartData = new StringBuilder(AppUI.EMPTY);
        for( String name : dataPoints.keySet()) {
            chartData.append(name).append("\t")
                    .append(dataLabels.get(name)).append("\t")
                    .append(dataPoints.get(name).getX()).append(",").append(dataPoints.get(name).getY()).append("\n");

        }
        return chartData.toString();
    }
}
