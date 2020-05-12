package sample;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.stage.FileChooser;
import com.gembox.spreadsheet.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Scanner;

public class Controller implements Initializable {

    public TextArea fileTextid;
    public Label labelTitleId;
    @FXML
    public TableView table;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        fileTextid.setVisible(false);
        labelTitleId.setVisible(false);
        fileTextid.setEditable(false);
        table.setVisible(false);
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
    }

}

