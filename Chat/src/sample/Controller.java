package sample;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.fxml.FXML;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.WindowEvent;
import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javafx.scene.control.Label;

public class Controller implements Initializable {
    @FXML //Аннотация, помечающая класс или член как доступный для разметки.
//    TextArea textArea;
    TextArea chatArea;
    @FXML//Аннотация, помечающая класс или член как доступный для разметки.
    TextField textField;
    @FXML//Аннотация, помечающая класс или член как доступный для разметки.
    HBox bottomPanel;
    @FXML//Аннотация, помечающая класс или член как доступный для разметки.
    HBox upperPanel;
    @FXML//Аннотация, помечающая класс или член как доступный для разметки.
    TextField loginField;
    @FXML//Аннотация, помечающая класс или член как доступный для разметки.
    PasswordField passwordField;
    @FXML
    ListView<String> clientList;
    @FXML
    private Label userName;
    @FXML
    VBox changeN;

    Socket socket;//Создаём сокет для подключения
    DataInputStream in;//Создаём обработчик входящего потока
    DataOutputStream out;//Создаём обработчик исходящего потока
//    public static final String ADDRESS = "localhost"; //Переменная c адресом подключения
//    public static final int PORT = 6003; //Перемення с портом для подключения
    static final String ADDRESS = "localhost";
    static final int PORT = 6003;
    List<TextArea> textAreas;//массив для textArea
    private boolean isAuthorized;//переменная отслеживающая состояние авторизации (ложно\истино)
    private String login;
    private String nameFile;
    private ExecutorService executorService;
    LogChat history = new LogChat(nameFile);

    public void setAuthorized (boolean authorized) {//метод авторизации
        this.isAuthorized = authorized;//экземляр переменной клиента
        if (!authorized) {//если не авторизован
            upperPanel.setVisible(true);//панель авторизации видна
            upperPanel.setManaged(true);//панель авторизации активнв
            bottomPanel.setVisible(false);//панель ввода скрыта
            bottomPanel.setManaged(false);//панель ввода не активна
            clientList.setVisible(false);//панель пользователей скрыта
            clientList.setManaged(false);//панель пользователей не активна
            changeN.setVisible(false);
            changeN.setManaged(false);
        } else {
            upperPanel.setVisible(false);//панель авторизации не видна
            upperPanel.setManaged(false);//панель авторизации не активна
            bottomPanel.setVisible(true);//панель ввода видна
            bottomPanel.setManaged(true);//панель ввода  активна
            clientList.setVisible(true);//панель пользователей не скрыта
            clientList.setManaged(true);//панель пользователей  активна
            changeN.setVisible(true);
            changeN.setManaged(true);
        }
    }

    @FXML////Аннотация, помечающая класс или член как доступный для разметки.
    void sendMsg() { //Отправка сообщений
        if (textField.getText().equals("/clear")) {//Ловим команду очистки чата
            chatArea.clear();//Очистка чата
            textField.clear();//Очищаем поле TextField
            textField.requestFocus();//Возвращаем фокус
        } else {
            try {
                out.writeUTF(textField.getText());//Отправляем в исходящий поток текст из TextField
                textField.clear();//Очищаем поле TextField
                textField.requestFocus();//Возвращаем фокус
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void connect() {//метод подключения
        try {
            userName.setText("Users");
            socket = new Socket(ADDRESS, PORT);//определяем сокету адрес и порт
            in = new DataInputStream(socket.getInputStream());//передаём в обработчик входящий поток с сокета
            out = new DataOutputStream(socket.getOutputStream());//передаём в обработчик исходящий поток с сокета
            setAuthorized(false); // Устанавливаем false для авторизации
            this.executorService = Executors.newSingleThreadExecutor();
            executorService.execute(() -> {//Запускаем поток
                try {
                    while (true) {// Запускам бесконечный цикл
                        if (Thread.currentThread().isInterrupted()){
                            break;
                        }
                        if (!socket.isClosed()){
                            String str = in.readUTF();//получаем строку из входящего потока в UTF
                            if ("/auth-OK".equals(str)) {//ловим строку авторизации клиента
                                setAuthorized(true);//устанавливаем авторизацию истина
                                nameFile = "history_" + this.login + ".txt";
                                history.setName(this.nameFile);
                                history.createLog();
                                chatArea.clear();//очищаем поле чата

                                for (Object string : history.logPrint()
                                ) {
                                    String log = (String) string;
                                    System.out.println(log);
                                    chatArea.appendText(log + "\n");
                                }
                                break;
                            } else {//если не авторизован, то принимаем сообщение об ошибке и выводим его в окно чата
                                for (TextArea ta : textAreas) {//проходим циклом по textArea
                                    ta.appendText(str + "\n");
                                    //печатаем сообщение об ошибке строку в окно чата
                                }
                            }
                        }
                    }

                    while (true) {// Запускам бесконечный цикл
                        if (Thread.currentThread().isInterrupted()){
                            break;
                        }
                        if (socket.isClosed()) {
                            System.exit(1);
                        } else {
                            String str = in.readUTF();//получаем строку из входящего потока в UTF
                            if ("/serverClosed".equals(str)) {//ловим команду закрытия сервера
                                System.exit(0);//выходим из экземпляра клиента + закрываем окно
                                break;//если сервер закрыт, то выходим из цикла
                            }
                            if (str.startsWith("/clientList ")) { //Ловим команду клиент лист
                                String[] tokens = str.split(" ");//делим строку на токены и добавляем в стринговый массив
                                Platform.runLater(new Runnable() { //запускаем в отдельном потоке и возвращаем управление
                                    @Override//переопределяем
                                    public void run() {
                                        clientList.getItems().clear();//очищаем клиент лист
                                        for (int i = 1; i < tokens.length; i++) {//проходимся циклом по токенам
                                            clientList.getItems().add(tokens[i]);//добавляем пользователей в клиентлист
                                        }
                                    }
                                });
                            } else {
                                chatArea.appendText(str + "\n");//печатаем эту строку в окно чата

                                //Логируем локально чат
                                history.logChat(str + "\r\n");
                            }
                        }
                    }
                } catch (IOException e) {//обрабатываем ошибку ввода
                    System.out.println("!!!!!!");
                    System.exit(5);//Закрытие окна по крестику после неудачного ввода пароля вызывает ошибку SecketExeption: Socket closed
                    e.printStackTrace();
                } finally {//Закрываем сокет (сетевое соединение)
                    try {
                        if (!socket.isClosed()) {
                            socket.close();//закрываем сокет
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    setAuthorized(false);//сбрасываем статус авторизации
                    executorService.shutdownNow();
                }

            });//старт потока
        } catch (IOException e) {
            e.printStackTrace();

            chatArea.appendText("Connection refused\n");//сообщаем об ошибке в чат
        }
    }

    public void tryToAuth(ActionEvent actionEvent) {//попытка авторизации
        if (socket == null || socket.isClosed()) {//проверяем, что мы не подключены к серверу(сокет не существует или закрыт
            connect();//вызываем метод подключения
        }

        try {
            if(loginField.getText() != null  ) {
                if (passwordField.getText() != null) {
                    out.writeUTF("/auth " + loginField.getText() + " " + passwordField.getText());//передаём в поток комманду авторизации, логин и пароль
                    login = loginField.getText();
                    loginField.clear();//очищаем поле логина
                    passwordField.clear();//очищаем поле пароля

                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void disconnect(){//метод дисконекта
        if (socket != null) {//если сокет существует
            if (!socket.isClosed()) {//если сокет ещё не закрыт
                try {
                    out.writeUTF("/end");//отправляем команду закончить
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    in.close();
                    out.close();
                    socket.close();//закрываем сокет
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {//И влюбом случае завершаем выполнение
                    login = null;
                        System.exit(0);
                }
            }
        }
    }

    public void selectClient(javafx.scene.input.MouseEvent mouseEvent){
        if (mouseEvent.getClickCount() == 2) {
            MiniStage ms = new MiniStage(clientList.getSelectionModel().getSelectedItem(), out, textAreas);
            ms.show();
        }
    }



    private javafx.event.EventHandler<WindowEvent> closeEventHandler = new javafx.event.EventHandler<WindowEvent>() {//создаём слушателя события
        @Override//переопределяем действие
        public void handle(WindowEvent event) {
            disconnect();
        }
    };
    public javafx.event.EventHandler<WindowEvent> getCloseEventHandler() {//гетер для слушателя

        return closeEventHandler;
    }



    @Override//переопределяем интерфейс
    public void initialize(URL location, ResourceBundle resources) {
        setAuthorized(false);//установить авторизацию false
        textAreas = new ArrayList<>();//Создать массив ArrayList из textArea
        textAreas.add(chatArea);//строку из chatArea добавлем в textArea
    }

    public void logUp(ActionEvent actionEvent) {
        RegistrationStage rs = new RegistrationStage(out);
        rs.show();
    }

    public void  chngNick(ActionEvent actionEvent){

    }

    public String getLogin() {
        return login;
    }
}


