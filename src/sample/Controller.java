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
import org.shaded.etsi.uri.x01903.v13.SignatureProductionPlaceDocument;

import java.awt.event.ItemEvent;
import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Scanner;

public class Controller implements Initializable {

    public TextArea fileTextid;
    public Label labelTitleId;
    @FXML public TableView table;
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
    List<Double> xColumnDataTest;
    List<Double> yColumnDataTest;
    List<Point> listOfPoints;
    List<Point> smothedPoints;
    String algName1 = "ALG1";  // tutaj do zmiany nazwy algorytmów jak juz bedziemy wiedziały dokladnie jakie
    String algName2 = "ALG2";
    String algName3 = "ALG2";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        fileTextid.setVisible(false);
        labelTitleId.setVisible(false);
        fileTextid.setEditable(false);
        table.setVisible(false);
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
        ExcelWorksheet worksheet = workbook.getWorksheet(0);
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
        ExcelFile file = new ExcelFile();
        ExcelWorksheet worksheet = file.addWorksheet("sheet");
        for (int row = 0; row < table.getItems().size(); row++) {
            ObservableList cells = (ObservableList) table.getItems().get(row);
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
        File saveFile = fileChooser.showSaveDialog(table.getScene().getWindow());

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

        ObservableList<ObservableList<String>> data = FXCollections.observableArrayList();
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
        System.out.println("numberOfColumns" + numberOfColumns);

        xCombo.setVisible(true);
        yCombo.setVisible(true);
        xLabelId.setVisible(true);
        yLabelId.setVisible(true);


        for(int i=0; i<numberOfColumns; i++){
            xCombo.getItems().add(Column.values()[i]);
            yCombo.getItems().add(Column.values()[i]);
        }
    }

    public void xSelectedAction(ActionEvent actionEvent) {
        int xColumnNumber = 0;
        Column xColumnString = (Column) xCombo.getValue();

        for(Column col : Column.values()) {
            if(xColumnString == col){
                xColumnNumber = col.getValue();
            }
        }

        TableColumn tableColumn = (TableColumn) table.getColumns().get(xColumnNumber);
        xColumnData = new ArrayList<>();

        // ignoruje pierwszy wiersz na sztywno bo zakładam że tam zawsze jest nazwa kolumny XD
        for (int i = 1; i < table.getItems().size(); i++) {
            xColumnData.add((String) tableColumn.getCellObservableValue(table.getItems().get(i)).getValue());
        }

        System.out.println("xColumnData" + xColumnData.size());
        for(String x: xColumnData){
            System.out.println(x);
        }

        if(xColumnData.size()>0 && yColumnData.size()>0){
            submitButtonId.setVisible(true);
        }

    }

    public void ySelectedAction(ActionEvent actionEvent) {
        int yColumnNumber = 0;
        Column yColumnString = (Column) yCombo.getValue();

        for(Column col : Column.values()) {
            if(yColumnString == col){
                yColumnNumber = col.getValue();
            }
        }

        TableColumn tableColumn = (TableColumn) table.getColumns().get(yColumnNumber);
        yColumnData = new ArrayList<>();

        // ignoruje pierwszy wiersz na sztywno bo zakładam że tam zawsze jest nazwa kolumny XD
        for (int i = 1; i < table.getItems().size(); i++) {
            yColumnData.add((String) tableColumn.getCellObservableValue(table.getItems().get(i)).getValue());
        }

        System.out.println("yColumnData" + yColumnData.size());
        for(String y: yColumnData){
            System.out.println(y);
        }

        if(xColumnData.size()>0 && yColumnData.size()>0){
            submitButtonId.setVisible(true);
        }
    }

    private void drawChart(List<Point> points) {
        XYChart.Series series = new XYChart.Series();

        for (Point point: points ) {

            series.getData().add(new XYChart.Data<>(point.getX(),point.getY()));
        }

        lineChart.getData().addAll(series);

    }

    private void generateOneListOfPointsFromTwoLists(){

        listOfPoints =  new ArrayList<>();
        xColumnDataTest =  new ArrayList<>();
        yColumnDataTest =  new ArrayList<>();
        listOfPoints =  new ArrayList<>();
        if(xColumnData.size() > 0 && yColumnData.size() > 0)
        for(int i= 0; i< xColumnData.size(); i++){
            if(xColumnData.get(i) != null && yColumnData.get(i)!= null ){

                Point point =  new Point(Double.parseDouble(xColumnData.get(i)), Double.parseDouble(yColumnData.get(i)));
                listOfPoints.add(point);
                xColumnDataTest.add(Double.parseDouble(xColumnData.get(i)));
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

        switch (selectedAlghoritm){
            case "ALG1" :
                smothedPoints = smoothUsingFirstAlg();
                break;
            case "ALG2" :
                smothedPoints = smoothUsingSecondtAlg();
                break;
            case "ALG3" :
                smothedPoints = smoothUsingThirdAlg();
                break;
        }

        if(smothedPoints != null)
        drawChart(smothedPoints);
    }

    private List<Point> smoothUsingFirstAlg() throws IOException {
        System.out.println("pierwszy");
        String pathToData = "hotel.txt";
        List<Double> file = readFile(pathToData);

        // pierwszy test
        file = xColumnDataTest;
        SimpleMovingAverage movingAverage = new SimpleMovingAverage(2);
        System.out.println(movingAverage.getMA(file));

        System.out.println("\n");

        // drugi test
        int[] windowSizes = {2};
        for (int windSize : windowSizes) {
            SimpleMovingAverage ma = new SimpleMovingAverage(windSize);
            for (double x : xColumnDataTest) {
                ma.newNum(x);
                System.out.println("Next number = " + x + ", SMA = " + ma.getAvg());
            }
        }

        return null; // zwracana będzie nowa wygladzona lista punktów
    }

    public List<Double> readFile(String filepath) throws IOException {
        FileReader fileReader = new FileReader(filepath);
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        List<Double> data = new ArrayList<Double>();
        String line;

        while ((line = bufferedReader.readLine()) != null)
        {
            data.add(Double.parseDouble(line));
        }

        bufferedReader.close();
        return data;
    }

    private List<Point> smoothUsingSecondtAlg() throws IOException {

        System.out.println("drugi");
        int period = 12;
        int m = 2;
        double alpha =  0.5411;
        double beta =  0.0086;
        double gamma = 1e-04;
        boolean debug = true;

        String pathToData = "hotel.txt";
        List<Double> file = readFile(pathToData);
        file = xColumnDataTest;

        List<Double> prediction = TripleExpSmoothing.forecast(file, alpha, beta, gamma, period, m, debug);
        System.out.println(prediction.size());
        for(Double p: prediction){
            System.out.println(p);
        }
        return null;
    }

    private List<Point> smoothUsingThirdAlg(){

        System.out.println("trzeci");
        return null;
    }
}

