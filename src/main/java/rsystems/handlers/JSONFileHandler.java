package rsystems.handlers;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class JSONFileHandler {
    public static JSONObject fileData;
    private String path = "";

    public JSONFileHandler(String path){
        this.path = path;
        loadDataFile();
    }

    public void loadDataFile() {
        JSONParser parser = new JSONParser();
        Object obj;

        try{
            obj  = parser.parse(new FileReader(path));
            fileData = (JSONObject) obj;
        } catch(FileNotFoundException e){
            System.out.println("Could not find JSON File");
        } catch (ParseException | IOException e) {
            e.printStackTrace();
        }

    }

    public JSONObject getDatafileData(){
        return fileData;
    }

}
