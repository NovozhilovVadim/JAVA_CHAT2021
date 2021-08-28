package sample;



import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class LogChat implements Serializable {
    private String name;

    public LogChat(String name) {
        this.name = name;
    }

    public void createLog() throws IOException {
        if (!Files.exists(Paths.get(this.name))) {
            Files.createFile(Paths.get(this.name));
            System.out.println("file " +this.name + " " + Files.getLastModifiedTime(Paths.get(this.name)) + " create" );
        }else {
            System.out.println("file " + " " + Files.getLastModifiedTime(Paths.get(this.name)) + " no create" );
        }
    }

     public void logChat (String str)  {
//        byte[] outData = str.getBytes();
//        try (FileOutputStream out = new FileOutputStream(this.name, true))
//        {
//            out.write(outData);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
         try( FileWriter out = new FileWriter(this.name, true)){
                out.write(str);
         } catch (IOException e) {
             e.printStackTrace();
         }
     }

//    public String LogPrint(String name) throws IOException {
//        return new String(Files.readAllBytes(Paths.get(name)));
//    }

    public List logPrint() throws IOException {
        List<String> logList = new ArrayList<>();
        FileReader fileReader = new FileReader(this.name);

        try (BufferedReader bufferedReader = new BufferedReader(fileReader)) {
            String line;
            while((line = bufferedReader.readLine()) != null) {

                logList.add(line);
            }
        }
        return logList;


    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
