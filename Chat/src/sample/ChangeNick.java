package sample;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.awt.*;
import java.io.DataOutputStream;
import java.io.IOException;

public class ChangeNick extends Stage {


    DataOutputStream out;

    public ChangeNick(DataOutputStream out) {
        this.out = out;
        Parent root = null;
        try {
            root = FXMLLoader.load(getClass().getResource("change.fxml"));
            setTitle("Change");
            Scene scene = new Scene(root, 300, 200);
            setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
