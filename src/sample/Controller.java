package sample;

import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.FileChooser;

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

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        fileTextid.setVisible(false);
        labelTitleId.setVisible(false);
        fileTextid.setEditable(false);
    }

    public void onOpenFile(ActionEvent event) throws IOException {
        FileChooser fileChooser = new FileChooser();
//        fileChooser.getExtensionFilters().addAll(
//                new FileChooser.ExtensionFilter("Files","*.pdf")
//        );

        File file = fileChooser.showOpenDialog(null);

        try {
            Scanner s = new Scanner(file).useDelimiter(" !"); // wczytuje z notatnika TODO excel
            labelTitleId.setText("File: "+file.getName());
            labelTitleId.setVisible(true);
            fileTextid.clear();
            fileTextid.setVisible(true);
            while (s.hasNext()) {
                fileTextid.appendText(s.next() + " ");
            }
        } catch (FileNotFoundException e) {
            System.err.println(e);
        }
    }

    public void onSaveFile(ActionEvent actionEvent) {
    }


}
