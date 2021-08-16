package sample;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class RegistrationController {//контроллер окна регистрации
    @FXML//Аннотация, помечающая класс или член как доступный для разметки.
    public TextField login;
    @FXML
    public PasswordField password;
    @FXML
    public TextField nickname;
    @FXML
    public Label resultLabel;
    @FXML//Аннотация, помечающая класс или член как доступный для разметки.
    public Button signupBtn;

    public void signUp(ActionEvent actionEvent) {
        if ("Sign Up".equals(signupBtn.getText())) {//При нажатии на кнопку авторизации
            Socket socket = null; //Устанавливаем сокет и всё остальное в null
            DataOutputStream out = null;
            DataInputStream in = null;
            String result = null;

            try {
                socket = new Socket(Controller.ADDRESS, Controller.PORT);//создаём сокет с параметрами адресса и порта из контроллера окна чата
                out = new DataOutputStream(socket.getOutputStream());//создаём исходящий поток
                in = new DataInputStream(socket.getInputStream());//создаём входящий поток
                out.writeUTF("/signup " + login.getText() + " " + password.getText() + " " + nickname.getText()); //передаём команду регистрации, ник и пароль
                result = in.readUTF();//получаем результат действия из входящего потока
            } catch (IOException e) {
                e.printStackTrace();
            }

            resultLabel.setText(result);//присваиваем resultLabel полученный ответ
            resultLabel.setVisible(true);//устанавливаем видимость в true
            if (resultLabel.getText().contains("Successful registration")) {//берём текст из resultLabel и сообщаем о результате
                signupBtn.setText("Exit");//меняем надпись на кнопке на "ВЫХОД"
            }
        } else {
            exitSignup();//или просто выходим
        }
    }

    public void exitSignup() {//обрабатываем выход по нажатию
        Stage stage = (Stage) login.getScene().getWindow();
        stage.close();
    }


}
