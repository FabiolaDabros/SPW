package sample;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;
import com.gembox.spreadsheet.*;
import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Scanner;

public class Controller implements Initializable {

    public TextArea fileTextid;
    public Label labelTitleId;
    @FXML
    public TableView table;
    public TableView tableCopy1;
    public TableView tableCopy2;
    public TableView tableCopy3;
    public ComboBox xCombo;
    public ComboBox yCombo;
    public Label xLabelId;
    public Label yLabelId;
    public LineChart lineChart;
    public Button submitButtonId;
    public NumberAxis y;
    public NumberAxis x;
    public ComboBox alghoritmsId;

    List<String> xColumnData;
    List<String> yColumnData;
    List<Double> xColumnDataTest1;
    List<Double> xColumnDataTest2;
    List<Double> xColumnDataTest3;
    List<Double> xsmothed1 =  new ArrayList<>();
    List<Double> xsmothed2 = new ArrayList<>();
    List<Double> xsmothed3 = new ArrayList<>();
    List<Double> yColumnDataTest;
    List<Point> listOfPoints;
    List<Point> smothedPoints;
    String algName1 = "Simple Moving Average";
    String algName2 = "Cumulative Moving Average";
    String algName3 = "Single Exponential Smoothing";
    ExcelWorksheet worksheet;
    ObservableList<ObservableList<String>> data;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        fileTextid.setVisible(false);
        labelTitleId.setVisible(false);
        fileTextid.setEditable(false);
        table.setVisible(false);
        tableCopy1.setVisible(false);
        tableCopy2.setVisible(false);
        tableCopy3.setVisible(false);
        xCombo.setVisible(false);
        yCombo.setVisible(false);
        xLabelId.setVisible(false);
        yLabelId.setVisible(false);
        lineChart.setCreateSymbols(false);
        submitButtonId.setVisible(false);
        lineChart.setVisible(false);
        alghoritmsId.setVisible(false);
        y.setLabel("Y");
        x.setLabel("X");
        alghoritmsId.getItems().add(algName1);
        alghoritmsId.getItems().add(algName2);
        alghoritmsId.getItems().add(algName3);
    }

    public void onOpenFile(ActionEvent event) throws IOException {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("XLSX files (*.xlsx)", "*.xlsx"),
                new FileChooser.ExtensionFilter("XLS files (*.xls)", "*.xls"),
                new FileChooser.ExtensionFilter("Txt Files", "*.txt")
        );

        File file = fileChooser.showOpenDialog(table.getScene().getWindow());

        if (file.getPath().contains("txt")) {
            loadTxtFile(file);
        } else {
            loadExcelFile(file);
        }
    }

    private void loadTxtFile(File file) {
        try {
            Scanner s = new Scanner(file).useDelimiter(" !");
            if (s.hasNext()) {
                labelTitleId.setText("File: " + file.getName());
                labelTitleId.setVisible(true);
                fileTextid.clear();
                fileTextid.setVisible(true);
                table.setVisible(false);
            } else {
                labelTitleId.setText("File: " + file.getName() + "could not be loaded properly");
            }
            while (s.hasNext()) {
                fileTextid.appendText(s.next() + " ");
            }
        } catch (FileNotFoundException e) {
            System.err.println(e);
        }
    }

    private void loadExcelFile(File file) throws IOException {
        ExcelFile workbook = ExcelFile.load(file.getAbsolutePath());
         worksheet = workbook.getWorksheet(0);
        String[][] sourceData = new String[100][26];
        for (int row = 0; row < sourceData.length; row++) {
            for (int column = 0; column < sourceData[row].length; column++) {
                ExcelCell cell = worksheet.getCell(row, column);
                if (cell.getValueType() != CellValueType.NULL)
                    sourceData[row][column] = cell.getValue().toString();
            }
        }
        fillTable(sourceData);
        labelTitleId.setText("File: " + file.getName());
        labelTitleId.setVisible(true);
    }

    public void onSaveFile(ActionEvent actionEvent) throws IOException {
        save(tableCopy2);
    }

    private void save(TableView table1) throws IOException {
        ExcelFile file = new ExcelFile();
        ExcelWorksheet worksheet = file.addWorksheet("sheet");
        for (int row = 0; row < table1.getItems().size(); row++) {
            ObservableList cells = (ObservableList) table1.getItems().get(row);
            for (int column = 0; column < cells.size(); column++) {
                if (cells.get(column) != null)
                    worksheet.getCell(row, column).setValue(cells.get(column).toString());
            }
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("XLSX files (*.xlsx)", "*.xlsx"),
                new FileChooser.ExtensionFilter("XLS files (*.xls)", "*.xls"),
                new FileChooser.ExtensionFilter("ODS files (*.ods)", "*.ods"),
                new FileChooser.ExtensionFilter("CSV files (*.csv)", "*.csv"),
                new FileChooser.ExtensionFilter("HTML files (*.html)", "*.html")
        );
        File saveFile = fileChooser.showSaveDialog(table1.getScene().getWindow());
        file.save(saveFile.getAbsolutePath());
    }


    static {
        SpreadsheetInfo.setLicense("FREE-LIMITED-KEY");
        SpreadsheetInfo.addFreeLimitReachedListener(args -> {
            System.out.println("LIMIT EXCEEDED");
            args.setFreeLimitReachedAction(FreeLimitReachedAction.CONTINUE_AS_TRIAL);
        });
    }

    private void fillTable(String[][] dataSource) {
        table.getColumns().clear();
        fileTextid.setVisible(false);
        table.setVisible(true);

         data = FXCollections.observableArrayList();
        for (String[] row : dataSource)
            data.add(FXCollections.observableArrayList(row));
        table.setItems(data);

        for (int i = 0; i < dataSource[0].length; i++) {
            final int currentColumn = i;
            TableColumn<ObservableList<String>, String> column = new TableColumn<>(ExcelColumnCollection.columnIndexToName(currentColumn));
            column.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue().get(currentColumn)));
            column.setEditable(true);
            column.setCellFactory(TextFieldTableCell.forTableColumn());
            column.setOnEditCommit(
                    (TableColumn.CellEditEvent<ObservableList<String>, String> t) -> {
                        t.getTableView().getItems().get(t.getTablePosition().getRow()).set(t.getTablePosition().getColumn(), t.getNewValue());
                    });
            table.getColumns().add(column);
        }

        countNumberOfColumns();
        table.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        table.getSelectionModel().setCellSelectionEnabled(true);
    }

    public void clickedColumn(MouseEvent mouseEvent) {
        TablePosition tablePosition = (TablePosition) table.getSelectionModel().getSelectedCells().get(0);
        int column = tablePosition.getColumn();
        TableColumn tableColumn = tablePosition.getTableColumn();
        System.out.println("Selected column" + column);
    }

    private void countNumberOfColumns() {
        ObservableList<TableColumn> columns = table.getColumns();

        int numberOfColumns = 0;
        for (Object row1 : table.getItems()) {
            for (TableColumn column1 : columns) {
                String col = (String) column1.getCellObservableValue(row1).getValue();
                if (col != null) {
                    numberOfColumns++;
                } else {
                    break;
                }
            }
            break;
        }

        xCombo.setVisible(true);
        yCombo.setVisible(true);
        xLabelId.setVisible(true);
        yLabelId.setVisible(true);


        for (int i = 0; i < numberOfColumns; i++) {
            xCombo.getItems().add(Column.values()[i]);
            yCombo.getItems().add(Column.values()[i]);
        }
    }

    public void xSelectedAction(ActionEvent actionEvent) {
        int xColumnNumber = 0;
        Column xColumnString = (Column) xCombo.getValue();

        for (Column col : Column.values()) {
            if (xColumnString == col) {
                xColumnNumber = col.getValue();
            }
        }

        TableColumn tableColumn = (TableColumn) table.getColumns().get(xColumnNumber);
        xColumnData = new ArrayList<>();

        for (int i = 1; i < table.getItems().size(); i++) {
            xColumnData.add((String) tableColumn.getCellObservableValue(table.getItems().get(i)).getValue());
        }

        if (xColumnData.size() > 0 && yColumnData.size() > 0) {
            submitButtonId.setVisible(true);
        }

    }

    public void ySelectedAction(ActionEvent actionEvent) {
        int yColumnNumber = 0;
        Column yColumnString = (Column) yCombo.getValue();

        for (Column col : Column.values()) {
            if (yColumnString == col) {
                yColumnNumber = col.getValue();
            }
        }

        TableColumn tableColumn = (TableColumn) table.getColumns().get(yColumnNumber);
        yColumnData = new ArrayList<>();

        for (int i = 1; i < table.getItems().size(); i++) {
            yColumnData.add((String) tableColumn.getCellObservableValue(table.getItems().get(i)).getValue());
        }


        if (xColumnData.size() > 0 && yColumnData.size() > 0) {
            submitButtonId.setVisible(true);
        }
    }

    private void drawChart(List<Point> points) {
        XYChart.Series series = new XYChart.Series();

        for (Point point : points) {

            series.getData().add(new XYChart.Data<>(point.getX(), point.getY()));
        }

        lineChart.getData().addAll(series);

    }

    private void generateOneListOfPointsFromTwoLists() {

        listOfPoints = new ArrayList<>();
        xColumnDataTest1 = new ArrayList<>();
        xColumnDataTest2 = new ArrayList<>();
        xColumnDataTest3 = new ArrayList<>();
        yColumnDataTest = new ArrayList<>();
        listOfPoints = new ArrayList<>();
        if (xColumnData.size() > 0 && yColumnData.size() > 0)
            for (int i = 0; i < xColumnData.size(); i++) {
                if (xColumnData.get(i) != null && yColumnData.get(i) != null) {

                    Point point = new Point(Double.parseDouble(xColumnData.get(i)), Double.parseDouble(yColumnData.get(i)));
                    listOfPoints.add(point);
                    xColumnDataTest1.add(Double.parseDouble(xColumnData.get(i)));
                    xColumnDataTest2.add(Double.parseDouble(xColumnData.get(i)));
                    xColumnDataTest3.add(Double.parseDouble(xColumnData.get(i)));
                    yColumnDataTest.add(Double.parseDouble(yColumnData.get(i)));
                }

            }

    }

    public void sumbit(ActionEvent actionEvent) {

        lineChart.setVisible(true);
        alghoritmsId.setVisible(true);

        generateOneListOfPointsFromTwoLists();
        drawChart(listOfPoints);
    }

    public void selectAlghoritm(ActionEvent actionEvent) throws IOException {

        String selectedAlghoritm = alghoritmsId.getValue().toString();
        smothedPoints = new ArrayList<>();

        switch (selectedAlghoritm) {
            case "Simple Moving Average":
                smothedPoints = smoothUsingFirstAlg();
                drawChart(smothedPoints);
                break;
            case "Cumulative Moving Average":
                smothedPoints = smoothUsingSecondtAlg();
                drawChart(smothedPoints);
                break;
            case "Single Exponential Smoothing":
                smothedPoints = smoothUsingThirdAlg();
                drawChart(smothedPoints);
                break;
        }

        fillTableToSave();
    }

    private List<Point> smoothUsingFirstAlg() {
        List<Double> file = xColumnDataTest1;

        SimpleMovingAverage movingAverage = new SimpleMovingAverage(2);
        List<Double> smoothed = movingAverage.getMA(file);
        List<Point> listOfSmoothedPoints = new ArrayList<>();
        for (int i = 0; i < smoothed.size(); i++) {
            Point point = new Point(smoothed.get(i), yColumnDataTest.get(i));
            xsmothed1.add(smoothed.get(i));
            listOfSmoothedPoints.add(point);
        }

        return listOfSmoothedPoints;
    }


    private List<Point> smoothUsingThirdAlg() {

        SingleExpSmoothing singleExpSmoothing = new SingleExpSmoothing();
        List<Double> file = xColumnDataTest2;
        double[] fcast = singleExpSmoothing.singleExponentialForecast(file, .5, 2);
        List<Point> listOfSmoothedPoints = new ArrayList<>();

        for (int i = 0; i < yColumnDataTest.size(); i++) {
            if (fcast[i] != 0.0) {
                Point point = new Point(fcast[i], yColumnDataTest.get(i));
                xsmothed2.add(fcast[i]);
                listOfSmoothedPoints.add(point);
                System.out.println(fcast[i]);
            }
        }
        return listOfSmoothedPoints;
    }

    private List<Point> smoothUsingSecondtAlg(){
        List<Double> file = xColumnDataTest3;

        CumulativeMovingAverage movingAverage = new CumulativeMovingAverage();
        List<Double> smoothed = movingAverage.getCMA(file);
        List<Point> listOfSmoothedPoints = new ArrayList<>();
        for (int i = 0; i < smoothed.size(); i++) {
            Point point = new Point(smoothed.get(i), yColumnDataTest.get(i));
            xsmothed2.add(smoothed.get(i));
            listOfSmoothedPoints.add(point);
        }

        return listOfSmoothedPoints;
    }


    private void fillTableToSave(){

        ObservableList<ObservableList<String>> data1 = FXCollections.observableArrayList();
        ObservableList<ObservableList<String>> data2 = FXCollections.observableArrayList();
        ObservableList<ObservableList<String>> data3 = FXCollections.observableArrayList();
        ObservableList<String> dat1 = FXCollections.observableArrayList();
        ObservableList<String> dat2 = FXCollections.observableArrayList();
        ObservableList<String> dat3 = FXCollections.observableArrayList();
        ObservableList<String> dat4 = FXCollections.observableArrayList();
        TableView temp =  table;

        for (double a : xsmothed1){
            dat1.add(String.valueOf(a));
        }
        for (double a : xsmothed2){
            dat2.add(String.valueOf(a));
        }
        for (double a : xsmothed3){
            dat3.add(String.valueOf(a));
        }
        for (double a : yColumnDataTest){
            dat4.add(String.valueOf(a));
        }

        tableCopy1.getColumns().clear();
        tableCopy2.getColumns().clear();
        tableCopy3.getColumns().clear();

        for(int i = 0; i< dat4.size(); i++){
            ObservableList<String> temp1 = FXCollections.observableArrayList();
            temp1.add(dat1.get(i));
            temp1.add(dat4.get(i));
            data1.add(temp1);
        }

        for(int i = 0; i< dat4.size(); i++){
            ObservableList<String> temp1 = FXCollections.observableArrayList();
            temp1.add(dat2.get(i));
            temp1.add(dat4.get(i));
            data2.add(temp1);
        }

        for(int i = 0; i< dat4.size(); i++){
            ObservableList<String> temp1 = FXCollections.observableArrayList();
            temp1.add(dat3.get(i));
            temp1.add(dat4.get(i));
            data3.add(temp1);
        }

        tableCopy1.setItems(data1);
        tableCopy2.setItems(data2);
        tableCopy3.setItems(data3);

    }

    public void onSaveFirst(ActionEvent actionEvent) throws IOException {
        save(tableCopy1);
    }

    public void onSaveThird(ActionEvent actionEvent) throws IOException {
        save(tableCopy3);
    }
}

