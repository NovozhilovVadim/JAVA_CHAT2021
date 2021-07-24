package sample;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

import java.io.DataOutputStream;
import java.io.IOException;

public class PersonalController {//контроллер окна отправки личных сообщений
    @FXML
    TextArea textArea;//Аннотация, помечающая класс или член как доступный для разметки.

    @FXML
    Button btn;//Аннотация, помечающая класс или член как доступный для разметки.

    public void btnClick() {//обработка клика по кнопке
        if (!((MiniStage) btn.getScene().getWindow()).parentList.contains(textArea)) {//Если в textArea нет адресата из parentList
            ((MiniStage) btn.getScene().getWindow()).parentList.add(textArea);//то добавляем их туда
            System.out.println("1");
        }
        DataOutputStream out = ((MiniStage) btn.getScene().getWindow()).out;//создаём изходящий поток
        String nickTo = ((MiniStage) btn.getScene().getWindow()).nickTo;//берём ник адресата
        try {
            out.writeUTF("@" + nickTo + " " + textArea.getText());//передаём ник и сообщение в исходящий поток
        } catch (IOException e) {
            e.printStackTrace();
        }


        // get a handle to the stage
        Stage stage = (Stage) btn.getScene().getWindow();// создаём обработчик
        // do what you have to do
        stage.close();//закрываем окно
    }
}
