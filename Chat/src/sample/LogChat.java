package sample;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Paths;

public class LogChat implements Serializable {

    public void CreateLog(String name){
        if (!Files.exists(Paths.get(name))) {
            File file = new File(name + ".txt");
        }
    }



    private void LogChat (String [] args) {
        byte[] outData = "Java".getBytes();
        try (FileOutputStream out = new FileOutputStream("demo.txt")) {
            out.write(outData);
        } catch (IOException e) {
            e.printStackTrace();
        }



    }
}
