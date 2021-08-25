package sample;



import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class LogChat implements Serializable {

    public void CreateLog(String name) throws IOException {
        if (!Files.exists(Paths.get(name))) {
            Files.createFile(Paths.get(name));
            System.out.println("file " +name + " " + Files.getLastModifiedTime(Paths.get(name)) + " create" );
        }else {
            System.out.println("file " + " " + Files.getLastModifiedTime(Paths.get(name)) + " no create" );
        }
    }

     public void LogChat (String str, String filename)  {
        byte[] outData = str.getBytes();
        try (FileOutputStream out = new FileOutputStream(filename, true))
        {
            out.write(outData);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

//    public String LogPrint(String name) throws IOException {
//        return new String(Files.readAllBytes(Paths.get(name)));
//    }

    public List LogPrint(String name) throws IOException {
        List<String> logList = new ArrayList<>();
        FileReader fileReader = new FileReader(name);

        try (BufferedReader bufferedReader = new BufferedReader(fileReader)) {
            String line;
            while((line = bufferedReader.readLine()) != null) {

                logList.add(line);
            }
        }
        return logList;


    }


}
