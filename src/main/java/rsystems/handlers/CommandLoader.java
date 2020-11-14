package rsystems.handlers;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import rsystems.SherlockBot;
import rsystems.objects.Command;

import java.util.ArrayList;

public class CommandLoader {

    private JSONFileHandler commandFile = new JSONFileHandler("commandData.json");
    private Object commandObject = commandFile.getDatafileData();
    private JSONObject commandData = (JSONObject) commandObject;

    public CommandLoader(){
        commandData.keySet().forEach(keyStr -> {

            Object keyValue = commandData.get(keyStr);
            JSONObject parsedData = (JSONObject) keyValue;

            int commmandID = Integer.parseInt(parsedData.get("id").toString());

            Command tempCommand = new Command(keyStr.toString(),commmandID);
            try{
                tempCommand.setAlias(getArrayList(parsedData,"alias"));
            }catch(NullPointerException e){
                //do nothing
            }

            try{
                tempCommand.setWikiLink(parsedData.get("wikiLink").toString());
            }catch (NullPointerException e){

            }

            try {
                if (Integer.parseInt(parsedData.get("permission").toString()) > 0) {
                    tempCommand.setRank(Integer.parseInt(parsedData.get("permission").toString()));
                }
            }catch(NullPointerException e){
                //
            }

            SherlockBot.commandMap.putIfAbsent(commmandID,tempCommand);
        });
    }

    private ArrayList<String> getArrayList(JSONObject parsedValue,String key){
        //Get links from datafile
        JSONArray jsonArray = (JSONArray) parsedValue.get(key);
        ArrayList<String> arrayList = new ArrayList<>();

        for(Object linkObject:jsonArray){
            arrayList.add(linkObject.toString());
        }
        return arrayList;
    }

}
