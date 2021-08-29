package Server;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ClientHandler {
    private ConsolServer server;//экземпляр сервера
    private Socket socket;//экземпляр сервера
    private DataOutputStream out;//экземпляр исходящего потока
    private DataInputStream in;//экземпляр входящего потока
    private String nickname;
    private List<String> blacklist;// черный список у пользователя, а не у сервера
//    private ExecutorService executorService;



    public ClientHandler(ConsolServer server, Socket socket) {
        try {
            this.server = server;
            this.socket = socket;
            this.in = new DataInputStream(socket.getInputStream());//обработчик входящего
            this.out = new DataOutputStream(socket.getOutputStream());//обработчик исходящего потокаиз сокета
            this.blacklist = new ArrayList<>();
//            this.executorService = Executors.newSingleThreadExecutor();
            server.getService().execute(()->{
                boolean isExit = false;
                try {
                    // auth - /auth login pass
                    while(true){//Авторизация

                        String str = in.readUTF();//Создаём строку из обработанного  в in

                        if (str.startsWith("/auth")){//Ловим команду авторизации
                            String[] tokens = str.split(" ");//создаём архив из строк
                            if (tokens.length > 3){
                                if (tokens[1] !=null && tokens[2] != null) {
                                    String nick = AuthServise.getNicknameByLoginAndPass(tokens[1], tokens[2]);//обравляем запрос к бд и присваиваем ответ переменной

                                    if (nick != null) {//если ник имеет значение
//                                if (server.verificationNickname(nick)){//Проверка а
//                                    sendMsg("/auth-ok");//отправляем подтверждение авторизации
//                                    setNickname(nick);//присваиваем экземпляру клиента полученный ник
//                                    server.subscribe(ClientHandler.this);//додовляем клиента в вектор на сервере
//                                    System.out.printf("Client [%s] connected \n", getNickname());//сообщаем в консоль сервера о подключении
//                                    System.out.printf("");
//                                    break;//если проверка пройдена выходим из цикла
                                        if (!server.isNickBusy(nick)) {
                                            sendMsg("/auth-OK");
                                            setNickname(nick);
                                            server.subscribe(ClientHandler.this);
                                            break;
//                                    }

                                        } else {
                                            sendMsg("user is already logged in");//сообщаем об ошибке
                                        }

                                    } else {
                                        sendMsg("wrong login/password");//сообщаем об ошибке логина или пароля
                                    }
                                }else {
                                    sendMsg("Please, enter your Login and Password");
                                }
                            }else {
                                sendMsg("You don't enter your Login or Password");
                            }

                        }
                        // регистрация
                        if (str.startsWith("/signup ")) {
                            String[] tokens = str.split(" ");
                            int result = AuthServise.addUser(tokens[1], tokens[2], tokens[3]);
                            if (result > 0) {
                                sendMsg("Successful registration");
                            } else {
                                sendMsg("Registration failed");
                            }
                        }
                        // выход
                        if ("/end".equals(str)){
                            isExit = true;
                            break;
                        }
                    }
                    if (!isExit) {
                        while (true) {//Запускаем бесконечный цикл отслеживания событий входящего потока
                            String str = in.readUTF();//Создаём строку из обработанного сканером в in
                            // для всех служебных команд и личных сообщений
                            if (str.startsWith("/") || str.startsWith("@")) {//Ловим команду выхода
                                if ("/end".equals(str)) {
                                    // для оповещения клиента, т.к. без сервера клиент работать не должен
                                    out.writeUTF("/serverClosed");//передаём команду закрытия в поток
                                    System.out.printf("Client (" + socket.getInetAddress() + ") exited");//сообщаем об отключении в консоль
                                    break;//выходим
                                }
                                // вторая часть ДЗ. выполнение
                                if (str.startsWith("@")) {
                                    String[] tokens = str.split(" ", 2);
                                    server.sendPrivateMsg(this, tokens[0].substring(1), tokens[1]);
                                }
                                // черный список для пользователя. но пока что только в рамках одного запуска программы
                                if (str.startsWith("/blacklist ")) {
                                    String[] tokens = str.split(" ");
                                    blacklist.add(tokens[1]);
                                    sendMsg("You added " + tokens[1] + " to blacklist");
                                }
                                //Смена ника
                                if (str.startsWith("/changeNick")){
                                    String[] tokens = str.split(" ", 2);

                                    AuthServise.changeNick(this.getNickname(), tokens[1]);
                                }
//                                } else if (str.startsWith("@")) {
//                                    String[] tokens = str.split(" ", 2);
//                                    server.setPrivateMsg(this, tokens[0].substring(1, tokens[0].length()), tokens[1]);
//                                } else if ("/blacklist".equals(str)) {
//                                    //Добавить проверку наличия пользователя в бд
//                                    String[] tokens = str.split(" ");
//                                    blacklist.add(tokens[1]);
//                                    sendMsg("You added " + tokens[1] + " add to blacklist.");
//                                }
                            } else {
                                Date date = new Date();//Выделяет объект Date и инициализирует его, чтобы он представлял время, в которое он был выделен.
                                SimpleDateFormat timeObj = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");//SimpleDateFormat
                                // - это конкретный класс для форматирования и анализа дат с учетом локали.
                                // Он позволяет выполнять форматирование (дата → текст), синтаксический анализ (текст → дата) и нормализацию.
                                // SimpleDateFormat позволяет начать с выбора любых пользовательских шаблонов для форматирования даты и времени.
                                server.broadcastMessage(this, nickname + ": " + str);
                            }
                            System.out.printf("Client [%s]  %s\n", nickname, str);
                        }
                    }
                }catch (IOException | SQLException e){
                    e.printStackTrace();
                }finally {

                    try {

                        in.close();//закрываем входящий поток

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {

                            out.close();//закрываем исходящий поток

                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    try {
                        if (!socket.isClosed()) {

                            socket.close();//закрываем сокет
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }


                    server.unsubscribe(this);//убираем клиента из вектора
                }
            });
        }catch (IOException e){
            e.printStackTrace();
        }
    }


    public void sendMsg(String msg){//Создаём метод отправки сообщения принимающий в качестве параметра строку
        try {
            out.writeUTF(msg);//записываем сообщение в исходящий поток
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    private void setNickname(String nick) {
        this.nickname = nick;
    }
    public String getNickname() {
        return nickname;
    }


    public boolean checkBlacklist(String nickname){
        return blacklist.contains(nickname);
    }
}
