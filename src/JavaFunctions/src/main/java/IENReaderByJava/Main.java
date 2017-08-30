package IENReaderByJava;

// Import functions
import java.io.*;
import java.util.*;


public class Main {

    public Main(){
    }

    public List readIENData(String IENDataFileName){

        List<List<String>> arr = new ArrayList<List<String>>();
        List<String> listDevInv = new ArrayList<String>();
        List<String> listDevData = new ArrayList<String>();
        List<String> listIntSigInv = new ArrayList<String>();
        List<String> listIntSigData = new ArrayList<String>();
        List<String> listPlanPhase = new ArrayList<String>();
        List<String> listLastCyclePhase = new ArrayList<String>();

        String tmpDevInvString;
        String tmpDevDataString;
        String tmpIntSigInvString;
        String tmpIntSigDataString;
        String tmpPlanPhaseString;
        String tmpLastCyclePhaseString;

        // Open a new file
        File ienFile = new File(IENDataFileName);

        // Check the existence of the file
        if(!ienFile.exists())
        {
            System.out.println("Can not find the file!");
            return null;
        }

        // If the file exists, do the following steps
        try {
            FileReader frIEN = new FileReader(ienFile);
            BufferedReader brIEN = new BufferedReader(frIEN);

            String text = null;
            String [] tmpArray;
            String [] tmpDateTime;
            String [] tmpPhase;
            int j;
            while ((text = brIEN.readLine())!=null) {

                tmpArray=text.split(","); // Split strings

                //***********First: If it is for device inventory**************
                if(tmpArray[0].equals("Device Inventory list")) {

                    //Ignore the first line
                    brIEN.readLine();
                    while((text = brIEN.readLine()) != null && text.length()!=0) {
                        //Get the date and time
                        tmpDevInvString=stringProcessing(text,11, "DetInv");
                        listDevInv.add(tmpDevInvString);
                    }
                }

                //***********Second: If it is for device data**************
                if(tmpArray[0].equals("Device Data")) {

                    //Ignore the first line
                    brIEN.readLine();
                    while((text = brIEN.readLine()) != null && text.length()!=0) {
                        tmpDevDataString=stringProcessing(text,10,"DetData");
                        listDevData.add(tmpDevDataString);
                    }
                }

                //***********Third: If it is for intersection signal inventory**************
                if(tmpArray[0].equals("Intersection Signal Inventory list")) {

                    //Ignore the first line
                    brIEN.readLine();
                    while((text = brIEN.readLine()) != null && text.length()!=0) {
                        tmpIntSigInvString=stringProcessing(text,9,"SigInv");
                        listIntSigInv.add(tmpIntSigInvString);
                    }
                }

                //***********Fourth: If it is for intersection signal data*************
                if(tmpArray[0].equals("Intersection Signal Data")) {

                    //Ignore the first line
                    brIEN.readLine();
                    while((text = brIEN.readLine()) != null && text.length()!=0) {
                        tmpIntSigDataString=stringProcessing(text,10,"SigData");
                        listIntSigData.add(tmpIntSigDataString);
                    }
                }

                //***********Fifth: If it is for intersection planned phases *************
                if(tmpArray[0].equals("Intersection Signal Planned Phases")) {

                    //Ignore the first line
                    brIEN.readLine();
                    while((text = brIEN.readLine()) != null && text.length()!=0) {
                        tmpArray=text.split(",");
                        if(tmpArray.length!=4){
                            System.out.println("Wrong input string type!");
                            return null;
                        }
                        tmpArray[0] = tmpArray[0].replace(" ", ""); //Get rid of space
                        tmpArray[1] = tmpArray[1].replace(" ", ""); //Get rid of space
                        tmpArray[3] = tmpArray[3].replace(" ", ""); //Get rid of space

                        tmpDateTime = (tmpArray[2]).split(" ");

                        tmpPhase =(tmpArray[3]).split("\\[");
                        tmpPhase =(tmpPhase[1]).split("]");
                        tmpArray[2]=tmpArray[2].replace(" ","/");

                        tmpPlanPhaseString=tmpArray[0]+","+tmpArray[1]+","+tmpArray[2]+","+tmpDateTime[1]+","+tmpDateTime[2]+","+tmpPhase[0];
                        listPlanPhase.add(tmpPlanPhaseString);
                    }
                }

                //***********Sixth: If it is for intersection last-cycle phases *************
                if(tmpArray[0].equals("Intersection Signal Last Cycle Phases")) {

                    //Ignore the first line
                    brIEN.readLine();
                    while((text = brIEN.readLine()) != null && text.length()!=0) {
                        tmpArray=text.split(",");
                        if(tmpArray.length!=5){
                            System.out.println("Wrong input string type!");
                            return null;
                        }
                        tmpArray[0] = tmpArray[0].replace(" ", ""); //Get rid of space
                        tmpArray[1] = tmpArray[1].replace(" ", ""); //Get rid of space
                        tmpArray[3] = tmpArray[3].replace(" ", ""); //Get rid of space
                        tmpArray[4] = tmpArray[4].replace(" ", ""); //Get rid of space

                        tmpDateTime = (tmpArray[2]).split(" ");
                        tmpPhase =(tmpArray[4]).split("\\[");
                        tmpPhase =(tmpPhase[1]).split("]");
                        tmpArray[2]=tmpArray[2].replace(" ","/");

                        tmpLastCyclePhaseString=tmpArray[0]+","+tmpArray[1]+","+tmpArray[2]+","+tmpDateTime[1]
                                +","+tmpDateTime[2]+","+tmpArray[3]+","+tmpPhase[0];
                        listLastCyclePhase.add(tmpLastCyclePhaseString);
                    }
                }
            }
            brIEN.close();
        }catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        arr.add(listDevInv);
        arr.add(listDevData);
        arr.add(listIntSigInv);
        arr.add(listIntSigData);
        arr.add(listPlanPhase);
        arr.add(listLastCyclePhase);
        return arr;
    }

    public String stringProcessing(String text,int DefaultLength, String Type)
    {
        String [] tmpArray;
        String [] tmpDateTime;
        String tmpString;

        tmpArray=text.split(",");
        tmpArray[0] = tmpArray[0].replace(" ", ""); //Get rid of space
        tmpArray[1] = tmpArray[1].replace(" ", ""); //Get rid of space

        tmpDateTime = (tmpArray[2]).split(" ");
        tmpArray[2]=tmpArray[2].replace(" ","/");
        tmpString=tmpArray[0]+","+tmpArray[1]+","+tmpArray[2]+","+tmpDateTime[1]+","+tmpDateTime[2];

        int addLoc;
        String tmp;
        if(Type.equals("DetInv")){ //If it is device inventory
            // Reconstruct the description part
            if(tmpArray.length==DefaultLength){
                tmp=tmpArray[3];
                addLoc=4;
            }
            else if(tmpArray.length==DefaultLength+1){
                tmp=tmpArray[3]+"&"+tmpArray[4];
                addLoc=5;
            }else if(tmpArray.length==DefaultLength+2){
                tmp=tmpArray[3]+"&"+tmpArray[4]+"&"+tmpArray[5];
                addLoc=6;
            }else{
                System.out.println("Wrong input string type!");
                return null;
            }
            tmp=tmp.replace(" / ","&");
            tmp=tmp.replace("/","&");
            tmp=tmp.replace(" @ ","&");
            tmp=tmp.replace("@","&");
            tmp=tmp.replace(" ","/");
        }
        else if(Type.equals("SigInv")){ //If it is signal inventory
            // Reconstruct the description part
            if(tmpArray.length==DefaultLength){
                tmp=tmpArray[4];
                addLoc=5;
            }
            else if(tmpArray.length==DefaultLength+1){
                tmp=tmpArray[4]+"&"+tmpArray[5];
                addLoc=6;
            }else if(tmpArray.length==DefaultLength+2){
                tmp=tmpArray[4]+"&"+tmpArray[5]+"&"+tmpArray[6];
                addLoc=7;
            }else{
                System.out.println("Wrong input string type!");
                return null;
            }
            tmp=tmp.replace(" / ","&");
            tmp=tmp.replace("/","&");
            tmp=tmp.replace(" @ ","&");
            tmp=tmp.replace("@","&");
            tmp=tmp.replace(" ","");

            tmpArray[3]=tmpArray[3].replace(" ","/");
            tmp=tmpArray[3]+","+tmp;

        }
        else{ //For other cases
            // Reconstruct the description part
            if(tmpArray.length==DefaultLength){
                tmp=tmpArray[3];
                addLoc=4;
            }
            else if(tmpArray.length==DefaultLength+1){
                tmp=tmpArray[3]+"&"+tmpArray[4];
                addLoc=5;
            }else if(tmpArray.length==DefaultLength+2){
                tmp=tmpArray[3]+"&"+tmpArray[4]+"&"+tmpArray[5];
                addLoc=6;
            }else{
                System.out.println("Wrong input string type!");
                return null;
            }
            tmp=tmp.replace(" ","");
        }
        tmpString= tmpString+","+tmp;

        // Add the rest of the string
        for (int i=addLoc;i<tmpArray.length;i++)
        {
            if(tmpArray[i].equals(" ")) {
                tmpString = tmpString + "," + "NA";
            }
            else{
                tmpArray[i]=tmpArray[i].replace(" ","");
                tmpString= tmpString+","+tmpArray[i];
            }
        }
        return tmpString;
    }


    public List readIENConnectionDataStatus(String IENDataFileName){

        List<String> listIENStatus = new ArrayList<String>();

        // Open a new file
        File ienFile = new File(IENDataFileName);

        // Check the existence of the file
        if(!ienFile.exists())
        {
            System.out.println("Can not find the file!");
            return null;
        }

        // If the file exists, do the following steps
        try {
            FileReader frIEN = new FileReader(ienFile);
            BufferedReader brIEN = new BufferedReader(frIEN);

            String text = null;
            String [] tmpArray;

            // Ignore the first two lines
            text = brIEN.readLine();
            text = brIEN.readLine();

            // Starting from the third line
            while ((text = brIEN.readLine())!=null) {

                tmpArray=text.split(","); // Split strings

                String tmpDate=tmpArray[0];
                String tmpTime=tmpArray[1];
                Double tmpRequestTime=Double.parseDouble(tmpArray[3]);
                Double tmpProcessTime=Double.parseDouble(tmpArray[5]);
                String Org;
                if(tmpArray[6].equals("Arcadia 5:1"))
                    Org="Arcadia";
                else
                    Org="LACO";
                Integer tmpStatus=Integer.parseInt(tmpArray[7]);
                Integer tmpNumDetector=Integer.parseInt(tmpArray[8]);

                listIENStatus.add(tmpDate+","+tmpTime+","+Org+","+tmpRequestTime+","+tmpProcessTime+","+tmpStatus+","+tmpNumDetector);
            }
            brIEN.close();
        }catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return listIENStatus;
    }
}
