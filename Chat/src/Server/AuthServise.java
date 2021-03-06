package Server;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.*;
import java.util.Vector;

public class AuthServise {//Сервис авторизации пользователей
    private static Connection connection;//создаём сессию (подключение) к базе данных
    private static Statement statement;//
    private static String query = "SELECT nickname FROM users";
    private static PreparedStatement ps;


    //    Объект, используемый для выполнения статического оператора SQL и возврата полученных результатов.
    //    По умолчанию одновременно может быть открыт только один объект ResultSet для каждого объекта Statement.
    //    Следовательно, если чтение одного объекта ResultSet чередуется с чтением другого,
    //    каждый из них должен быть сгенерирован разными объектами Statement.
    //    Все методы выполнения в интерфейсе Statement неявно закрывают текущий объект ResultSet оператора, если он существует.

    public static void connect() {//метод подключения
        try {
            Class.forName("org.sqlite.JDBC");//Возвращает объект Class, связанный с классом или интерфейсом с заданным строковым именем.(путь к драйверу)
            connection = DriverManager.getConnection("jdbc:sqlite:main.db");//соообщаем драверу куда подключаться - к какой БД
             // Базовый сервис для управления набором драйверов JDBC.
             // ПРИМЕЧАНИЕ. Интерфейс javax.sql.DataSource, новый в JDBC 2.0 API, предоставляет другой способ подключения к источнику данных.
             // Использование объекта DataSource является предпочтительным средством подключения к источнику данных.
             // В рамках инициализации класс DriverManager попытается загрузить классы драйверов, указанные в системном свойстве «jdbc.drivers».
             // Это позволяет пользователю настраивать драйверы JDBC, используемые их приложениями.
             // Методы getConnection и getDrivers DriverManager были улучшены для поддержки механизма поставщика услуг Java Standard Edition.
             // Драйверы JDBC 4.0 должны включать файл META-INF / services / java.sql.Driver.
             // Этот файл содержит имя реализации драйвера JDBC для java.sql.Driver.
             // Например, чтобы загрузить класс my.sql.Driver, файл META-INF / services / java.sql.Driver будет содержать запись: my.sql.Driver
             // Приложениям больше не нужно явно загружать драйверы JDBC с помощью Class.forName ().
             // Существующие программы, которые в настоящее время загружают драйверы JDBC с помощью Class.forName (),
             // будут продолжать работать без изменений.
             // Когда вызывается метод getConnection, DriverManager будет пытаться найти подходящий драйвер из числа загруженных при инициализации
             // и загруженных явно с использованием того же загрузчика классов, что и текущий апплет или приложение.
             // Начиная с Java 2 SDK, Standard Edition, версии 1.3, поток регистрации может быть установлен только в том случае,
             // если было предоставлено надлежащее разрешение. Обычно это делается с помощью инструмента PolicyTool,
             // который можно использовать для предоставления разрешения java.sql.SQLPermission «setLog».

            statement = connection.createStatement();//Создает объект Statement для отправки операторов SQL в базу данных.
             // Операторы SQL без параметров обычно выполняются с использованием объектов Statement.
             // Если один и тот же оператор SQL выполняется много раз, может быть более эффективным использовать объект PreparedStatement.
             // Наборы результатов, созданные с использованием возвращенного объекта Statement,
             // по умолчанию будут иметь тип TYPE_FORWARD_ONLY и иметь уровень параллелизма CONCUR_READ_ONLY.
             // Удерживаемость созданных наборов результатов может быть определена путем вызова
            ps = connection.prepareStatement(query);



        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

//    public static String getNicknameByLoginAndPassword(String login, String password){//Запрос к БД по логину и паролю получить ник
//        String query = String.format("select nickname, password from users where login='%s' and password='%s'", login);//отправляем запрос
//        try {
//            ResultSet rs = statement.executeQuery(query);//получаем результат
//            int myHash = password.hashCode();
//            if (rs.next()){//если результат есть
//                String nick = rs.getNString(1);
//                //Изменяем тип PASSWORD на integer в БД
//                int dbHash = rs.getInt(2);
//                if (myHash == dbHash){
//                    return nick;
//                }
//            }
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//        return null;//в противном случае NULL
//    }



    public static int addUser(String login, String pass, String nickname) {
        try {

            query = "INSERT INTO users (login, password, nickname) VALUES (?, ?, ?);";
            ps.setString(1, login);
            ps.setInt(2, pass.hashCode());
            ps.setString(3, nickname);
            return ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
    public static int changeNick(String oldNick, String newNick) throws SQLException {
       try {
           query = "UPDATE users SET nickname = ?  WHERE nickname = ? ";
           ps.setString(1, newNick);
           ps.setString(2, oldNick);
           return ps.executeUpdate();
       }catch (SQLException e){
           e.printStackTrace();
       }
       return 0;
    }



    public static String getNicknameByLoginAndPass(String login, String pass) {
        query = String.format("select nickname, password from users where login='%s'", login);
        try {
            ResultSet rs = statement.executeQuery(query); // возвращает выборку через select
            int myHash = pass.hashCode();
            // кеш числа 12345
            // изменим пароли в ДБ на хеш от строки pass1
            if (rs.next()) {
                String nick = rs.getString(1);
                int dbHash = rs.getInt(2);
                if (myHash == dbHash) {
                    return nick;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void disconnect() {//метод для отключения
        if (connection != null) {
            try {
                connection.close();//закрываем сессию
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
