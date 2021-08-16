package sample;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import java.awt.*;

public class ChangeConroller {
    @FXML
   public TextField changeField;
    @FXML
    public Button change;
    @FXML
    public Label resultLabel;

public void change (ActionEvent actionEvent){
    if ("Change Nick".equals(change.getText())) {//При нажатии на кнопку смены ника
        Socket socket = null; //Устанавливаем сокет и всё остальное в null
        DataOutputStream out = null;
        DataInputStream in = null;
        String result = null;

        try {
            socket = new Socket(Controller.ADDRESS, Controller.PORT);//создаём сокет с параметрами адресса и порта из контроллера окна чата
            out = new DataOutputStream(socket.getOutputStream());//создаём исходящий поток
            in = new DataInputStream(socket.getInputStream());//создаём входящий поток
            out.writeUTF("/ChangeNick " + change.getText()); //передаём команду смены, ник
            result = in.readUTF();//получаем результат действия из входящего потока
        } catch (IOException e) {
            e.printStackTrace();
        }

        resultLabel.setText(result);//присваиваем resultLabel полученный ответ
        resultLabel.setVisible(true);//устанавливаем видимость в true
        if (resultLabel.getText().contains("Successful registration")) {//берём текст из resultLabel и сообщаем о результате
            change.setText("Exit");//меняем надпись на кнопке на "ВЫХОД"
        }
    } else {
        exitChange();//или просто выходим
    }
}

    public void exitChange() {//обрабатываем выход по нажатию
        Stage stage = (Stage) change.getScene().getWindow();
        stage.close();
    }

}
