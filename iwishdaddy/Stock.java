package com.iwishdaddy;

import java.io.InputStream; 
import java.net.*; 
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Iterator;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.stream.Collectors;
import java.io.InputStreamReader;
import java.util.Date;
import java.text.SimpleDateFormat;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

import org.ini4j.Ini;
import org.ini4j.Profile.Section;
import java.io.FileReader;
import java.util.concurrent.*;
import java.io.File;
import java.io.*;
import java.util.*;
import java.io.FileReader;
import java.io.FileNotFoundException;
import org.apache.log4j.*;
import java.io.PrintWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.ini4j.Wini;
import org.ini4j.InvalidFileFormatException;
import java.sql.Timestamp;

class Stock extends StockReflection{
    private static String EX_IDX;
    private static String EX_CH;
    private static String EX_CH_CONSOLE;
    private static String NG_CH;
    private static String LOG_LEVEL;
    static int  j;
    public static Logger log;
    private static String t = "vol";
    private static String e = "tse";
    private static String n = "100";
    private static String c = "1783";
    private static String d = "2018-10-05";
    private static String f = "1783/1783.csv";
    private static String g = "EX_CH";
    private static String y = "buy_top";
    private static String settings = "settings";
    private static String loop_end = "5";
    private static ConsoleColors ConsoleColors = new ConsoleColors();
    private static String  newValue ="";
    private static String  seleniumNodeHost  ="192.168.1.13:5555";
    private static String  seleniumNodePlatform  ="linux";
    
    private static int timestamp_start = (int) (new Date().getTime()/1000);
    public static void writeIniSetting(String key , String jsonFilename) throws IOException, FileNotFoundException{

        try {


            Ini ini = new Ini(new File("stock.ini"));

            BufferedReader focus = new BufferedReader(new InputStreamReader(new FileInputStream(Stock.class.getProtectionDomain().getCodeSource().getLocation().getPath()+jsonFilename), "UTF-8"));
            String myfocus = new String();
            for (String line; (line = focus.readLine()) != null; myfocus += line);
            JSONArray focusObj = new JSONArray(myfocus);
        //System.out.println(focusObj.toString());
            String newValue ="";
            for(int i = 0 ; i < focusObj.length() ; i++){
                String a = focusObj.getString( i );
                newValue += "tse_"+a+".tw";
                newValue += "|";
                newValue += "otc_"+a+".tw";
                newValue += "|";

            }
            ini.put("STOCK", key, newValue);
            ini.store();
        } catch (InvalidFileFormatException e) {
            System.out.println("Invalid file format.");
        } catch (IOException e) {
            System.out.println("Problem reading file.");
        }
    }
    public static void readIniSetting(String g) throws IOException, FileNotFoundException{
        writeIniSetting("EX_CH_9","focuslist.json");
        writeIniSetting(g,g+".json");
        try
        {    
            Ini ini = new Ini(new FileReader("stock.ini"));
            EX_IDX = ini.get("STOCK", "EX_IDX");
            EX_CH = ini.get("STOCK", g);
            EX_CH_CONSOLE = ini.get("STOCK", "EX_CH_CONSOLE");
            NG_CH = ini.get("STOCK", "NG_CH");
          } catch (final IOException e) {
            throw new RuntimeException(e);
            // whatever.
        }
    }
    public static void readIniSetting() throws IOException, FileNotFoundException{
        try
        {    
            Ini ini = new Ini(new FileReader("stock.ini"));
            EX_IDX = ini.get("STOCK", "EX_IDX");
            EX_CH = ini.get("STOCK", "EX_CH");
            EX_CH_CONSOLE = ini.get("STOCK", "EX_CH_CONSOLE");
            NG_CH = ini.get("STOCK", "NG_CH");
          } catch (final IOException e) {
            throw new RuntimeException(e);
            // whatever.
        }
    }


    public static void download_my_stock(String g)  throws Exception , InterruptedException{
        Stock stock = new Stock();
        try {     
            String ch;
            String startTimestamp = "0";
            String endTimestamp = Long.toString( System.currentTimeMillis() /1000 );
            readIniSetting(g);
            String[] tokens = EX_CH.split("\\|");
            List<String> list = Arrays.asList(tokens);
            Collections.reverse(list);
            tokens = (String[]) list.toArray();

            for (String token:tokens) {
                System.out.println("Downloading: " + token);
                String regEx="[^0-9LR]";
                Pattern p = Pattern.compile(regEx);
                Matcher m = p.matcher(token);
                ch = m.replaceAll("").trim();
                log.debug(ch);
                stock.getStockHistoryPrice(ch, startTimestamp, endTimestamp);

            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    public static void main(String args[])  throws Exception , InterruptedException {
        SeleniumStandaloneServer seleniumStandaloneServer = new SeleniumStandaloneServer();

        PropertyConfigurator.configure(Stock.class.getProtectionDomain().getCodeSource().getLocation().getPath()+"log4j.properties");
        log = Logger.getLogger(Stock.class);

        JSONObject jsonStockSettings = new JSONObject();
        Stock stock = new Stock();
        ScheduledExecutorService ses = Executors.newSingleThreadScheduledExecutor();
        PostgreSQLJDBC PostgreSQLJDBC = new PostgreSQLJDBC();
        YoutubeAPI YoutubeAPI = new YoutubeAPI();
        BufferedReader reader;
        String response = new String();
        switch(args[0]){
            case "youtube_video_list":
                try{
                    seleniumStandaloneServer.videoList();
                } catch (IOException e) {
                    e.printStackTrace();
                }



            break;
            /*
            *   20個交易日以來 出現長紅K，6.5%
            */
            case "export":
                try{
                    reader = new BufferedReader(new InputStreamReader(new FileInputStream(Stock.class.getProtectionDomain().getCodeSource().getLocation().getPath()+"settings.json"), "UTF-8"));
                    response = new String();
                    for (String line; (line = reader.readLine()) != null; response += line);

                    String ch;
                    JSONObject my_stock = new JSONObject(response);
                    JSONArray root = my_stock.getJSONArray("my_stock");
                    JSONArray upperArray = new JSONArray();
                     for(int i = 0 ; i< root.length() ; i++){
                        ch = root.getJSONObject(i).getString("ch");
                        log.debug(ch);
                        seleniumStandaloneServer.executeExport(ch);

                     }                    
                } catch (IOException e) {
                    e.printStackTrace();
                }

            break;
            case "ipass":
                if(args.length == 3){
                    seleniumNodeHost = args[1];
                    seleniumNodePlatform = args[2];
                }
                seleniumStandaloneServer.setSeleniumNodeHost(seleniumNodeHost);
                seleniumStandaloneServer.setSeleniumNodePlatform(seleniumNodePlatform);
                seleniumStandaloneServer.ipass();


            break;  
            case "ipass2":
                    seleniumStandaloneServer.ipass2();
            break;
            /*
            *   20個交易日以來 出現長紅K，6.5%
            */
            case "full_red":
                try{
                    reader = new BufferedReader(new InputStreamReader(new FileInputStream(Stock.class.getProtectionDomain().getCodeSource().getLocation().getPath()+"settings.json"), "UTF-8"));
                    response = new String();
                    for (String line; (line = reader.readLine()) != null; response += line);

                    String ch;
                    JSONObject my_stock = new JSONObject(response);
                    JSONArray root = my_stock.getJSONArray("all_stock");
                    PrintWriter writer = new PrintWriter("../website/data/full_red.txt", "UTF-8");
                    JSONArray upperArray = new JSONArray();
                     for(int i = 0 ; i< root.length() ; i++){
                        ch = root.getJSONObject(i).getString("ch");
                        BufferedReader reader2 = new BufferedReader(new InputStreamReader(new FileInputStream(Stock.class.getProtectionDomain().getCodeSource().getLocation().getPath()+"..//website/data//"+ch+".json"), "UTF-8"));
                        String response2 = new String();
                        for (String line2; (line2 = reader2.readLine()) != null; response2 += line2);
                        JSONObject stockHistory = new JSONObject(response2);
                        boolean fullRedScore = stock.fullRedScore(ch,stockHistory);

                     }                    
                } catch (IOException e) {
                    e.printStackTrace();
                }
            break;
            /*
            *   頭頭高 底底高 多頭走勢
            */
            case "upper":
                try{
                    reader = new BufferedReader(new InputStreamReader(new FileInputStream(Stock.class.getProtectionDomain().getCodeSource().getLocation().getPath()+"settings.json"), "UTF-8"));
                    response = new String();
                    for (String line; (line = reader.readLine()) != null; response += line);

                    String ch;
                    JSONObject my_stock = new JSONObject(response);
                    JSONArray root = my_stock.getJSONArray("my_stock");
                    PrintWriter writer = new PrintWriter("../website/data/upper.json", "UTF-8");
                    JSONArray upperArray = new JSONArray();
                     for(int i = 0 ; i< root.length() ; i++){
                        ch = root.getJSONObject(i).getString("ch");

                        BufferedReader reader2 = new BufferedReader(new InputStreamReader(new FileInputStream(Stock.class.getProtectionDomain().getCodeSource().getLocation().getPath()+"..//website/data//"+ch+".json"), "UTF-8"));
                        String response2 = new String();
                        for (String line2; (line2 = reader2.readLine()) != null; response2 += line2);
                        JSONObject stockHistory = new JSONObject(response2);


                        int upperScore = stock.upperStockScore(ch,stockHistory);
                        if(upperScore > 0){
                            JSONObject upperObject = new JSONObject();
                            upperObject.put("ch",String.valueOf(ch));
                            upperObject.put("score",upperScore);
                            upperArray.put(upperObject);

                        }
                     }
                    JSONArray jsonArr = upperArray;
                    JSONArray sortedJsonArray = new JSONArray();

                    List<JSONObject> jsonValues = new ArrayList<JSONObject>();
                    for (int i = 0; i < jsonArr.length(); i++) {
                        jsonValues.add(jsonArr.getJSONObject(i));
                    }
                    Collections.sort( jsonValues, new Comparator<JSONObject>() {
                        //You can change "Name" with "ID" if you want to sort by ID
                        private static final String KEY_NAME = "score";
                        @Override
                        public int compare(JSONObject lhs, JSONObject rhs) {
                            try {
                                return lhs.getInt(KEY_NAME) < rhs.getInt(KEY_NAME) ? 1 : (lhs.getInt(KEY_NAME) > rhs.getInt(KEY_NAME) ? -1 : 0);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            return 0;
                        }
                    });
                     for (int i = 0; i < jsonArr.length(); i++) {
                        sortedJsonArray.put(jsonValues.get(i));
                    }
                    JSONObject jsonObj = new JSONObject(  );
                    jsonObj.put("upper",sortedJsonArray);

                    writer.println(jsonObj.toString());

                    writer.close();
                    
                } catch (IOException e) {
                    e.printStackTrace();
                }
            break;
            /*
            *   頭頭低 底底低 空頭走勢
            */
            case "lower":
                try{
                    reader = new BufferedReader(new InputStreamReader(new FileInputStream(Stock.class.getProtectionDomain().getCodeSource().getLocation().getPath()+"settings.json"), "UTF-8"));
                    response = new String();
                    for (String line; (line = reader.readLine()) != null; response += line);

                    String ch;
                    JSONObject my_stock = new JSONObject(response);
                    JSONArray root = my_stock.getJSONArray("my_stock");
                    PrintWriter writer = new PrintWriter("../website/data/lower.json", "UTF-8");
                    JSONArray lowerArray = new JSONArray();

                     for(int i = 0 ; i< root.length() ; i++){
                        ch = root.getJSONObject(i).getString("ch");

                        BufferedReader reader2 = new BufferedReader(new InputStreamReader(new FileInputStream(Stock.class.getProtectionDomain().getCodeSource().getLocation().getPath()+"..//website/data//"+ch+".json"), "UTF-8"));
                        String response2 = new String();
                        for (String line2; (line2 = reader2.readLine()) != null; response2 += line2);
                        JSONObject stockHistory = new JSONObject(response2);


                        int lowerScore = stock.lowerStockScore(ch,stockHistory);
                        if(lowerScore < 0){
                            JSONObject lowerObject = new JSONObject();
                            lowerObject.put("ch",String.valueOf(ch));
                            lowerObject.put("score",lowerScore);
                            lowerArray.put(lowerObject);
                        }

                     }
                    JSONArray jsonArr = lowerArray;
                    JSONArray sortedJsonArray = new JSONArray();

                    List<JSONObject> jsonValues = new ArrayList<JSONObject>();
                    for (int i = 0; i < jsonArr.length(); i++) {
                        jsonValues.add(jsonArr.getJSONObject(i));
                    }
                    Collections.sort( jsonValues, new Comparator<JSONObject>() {
                        //You can change "Name" with "ID" if you want to sort by ID
                        private static final String KEY_NAME = "score";
                        @Override
                        public int compare(JSONObject lhs, JSONObject rhs) {
                            try {
                                return lhs.getInt(KEY_NAME) > rhs.getInt(KEY_NAME) ? 1 : (lhs.getInt(KEY_NAME) < rhs.getInt(KEY_NAME) ? -1 : 0);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            return 0;
                        }
                    });
                     for (int i = 0; i < jsonArr.length(); i++) {
                        sortedJsonArray.put(jsonValues.get(i));
                    }
                    JSONObject jsonObj = new JSONObject(  );
                    jsonObj.put("lower",sortedJsonArray);

                    writer.println(jsonObj.toString());

                    writer.close();                    
                } catch (IOException e) {
                    e.printStackTrace();
                }
            break;
            case "download_my_stock":
                download_my_stock("EX_CH");

            break;
            case "download_all_data":
                try {     
                
                    reader = new BufferedReader(new InputStreamReader(new FileInputStream(Stock.class.getProtectionDomain().getCodeSource().getLocation().getPath()+"settings_reset.json"), "UTF-8"));
                    System.out.println( Stock.class.getProtectionDomain().getCodeSource().getLocation().getPath() );
                    response = new String();
                    for (String line; (line = reader.readLine()) != null; response += line);

                    JSONObject all_stock = new JSONObject(response);
                    JSONArray root = all_stock.getJSONArray("all_stock");
                    String ch;
                    String startTimestamp = all_stock.getString("start_timestamp");
                    String endTimestamp = Long.toString( System.currentTimeMillis() /1000 );
                     for(int i = 0 ; i< root.length() ; i++){
                        ch = root.getJSONObject(i).getString("ch");
                        log.debug(ch);

                        stock.getStockHistoryPrice(ch, startTimestamp, endTimestamp);
                     }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            break;
            case "download":
                stock.download();
            /*
                try {     
                    jsonStockSettings = Stock.getSettingsByJson("settings.json");
                    JSONArray root = jsonStockSettings.getJSONArray("all_stock");
                    String ch;
                    String startTimestamp = jsonStockSettings.getString("start_timestamp");
                    String endTimestamp = Long.toString( System.currentTimeMillis() /1000 );
                     for(int i = 0 ; i< root.length() ; i++){
                        ch = root.getJSONObject(i).getString("ch");
                        log.debug(ch);

                        stock.getStockHistoryPrice(ch, startTimestamp, endTimestamp);
                     }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                    */
            break;
            case "MA":
                try{
                    reader = new BufferedReader(new InputStreamReader(new FileInputStream(Stock.class.getProtectionDomain().getCodeSource().getLocation().getPath()+"settings.json"), "UTF-8"));
                    response = new String();
                    for (String line; (line = reader.readLine()) != null; response += line);

                    String ch;
                    JSONObject my_stock = new JSONObject(response);
                    JSONArray root = my_stock.getJSONArray("all_stock");
                    JSONArray lowerArray = new JSONArray();

                     for(int i = 0 ; i< root.length() ; i++){
                        ch = root.getJSONObject(i).getString("ch");
                        log.debug(ch);

                        BufferedReader reader2 = new BufferedReader(new InputStreamReader(new FileInputStream(Stock.class.getProtectionDomain().getCodeSource().getLocation().getPath()+"..//website/data//"+ch+".json"), "UTF-8"));
                        String response2 = new String();
                        for (String line2; (line2 = reader2.readLine()) != null; response2 += line2);
                        JSONObject stockHistory = new JSONObject(response2);

                        PrintWriter writer = new PrintWriter("../website/data/"+ch+".json", "UTF-8");
                        JSONArray D = stockHistory.getJSONArray("D");

                        JSONObject jsonObj = stock.calculateMA(ch,D);
                        stockHistory.put("D_MA",jsonObj.getJSONArray("MA"));
                        stockHistory.put("D_WAVE",jsonObj.getJSONArray("wave"));
                        writer.println(stockHistory.toString());

                        writer.close();                    
                    }
                    

                } catch (IOException e) {
                    e.printStackTrace();
                }
            break;
            case "all":
                try {     
                
                    reader = new BufferedReader(new InputStreamReader(new FileInputStream(Stock.class.getProtectionDomain().getCodeSource().getLocation().getPath()+"settings.json"), "UTF-8"));
                    System.out.println( Stock.class.getProtectionDomain().getCodeSource().getLocation().getPath() );
                    response = new String();
                    for (String line; (line = reader.readLine()) != null; response += line);

                    JSONObject my_stock = new JSONObject(response);
                    JSONArray root = my_stock.getJSONArray("my_stock");
                    String ch;
                    String startTimestamp = my_stock.getString("start_timestamp");
                    String endTimestamp = Long.toString( System.currentTimeMillis() /1000 );
                     for(int i = 0 ; i< root.length() ; i++){

                        ch = root.getJSONObject(i).getString("ch");
                        log.debug(ch);

                        stock.getStockHistoryPrice2(ch, startTimestamp, endTimestamp);
                     }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            break;
            case "DKD":
                String ch = args[1];
                String startTimestamp = "1291161600";
                String endTimestamp = Long.toString( System.currentTimeMillis() /1000 );
                if(args.length < 2){
                    startTimestamp = "1291161600";
                }
                stock.getStockHistoryPrice(ch, startTimestamp, endTimestamp);
            break;
            case "five":
                if(args.length == 2){
                    g = args[1];
                }
                ses.scheduleAtFixedRate(new Runnable() {
                    @Override
                    public void run() {
                        try{
                            readIniSetting();
                            stock.fibest();
                            int x= g.indexOf("_");
                            String MY_CH_CONSOLE = g.substring(x+1);


                            JSONObject jsonObj = stock.getStockInfo("",EX_IDX , g , MY_CH_CONSOLE , true);
                            
                        }catch (Exception e) {

                        }
                        System.out.println("timer to run in 5 sec: ");
                    }
                }, 0, 5, TimeUnit.SECONDS);  // every 5 minutes

            break;
            case "hotStock":
//https://tw.stock.yahoo.com/d/i/rank.php?t=vol&e=otc&n=100  
//https://tw.stock.yahoo.com/d/i/rank.php?t=vol&e=tse&n=100
                if(args.length > 3){
                    t = args[2];
                    e = args[1];
                    n = args[3];
                }
                ses.scheduleAtFixedRate(new Runnable() {
                    @Override
                    public void run() {
                        try{
                            readIniSetting();
                            EX_CH = stock.hotStock(NG_CH, t,e,n);
                            
                            stock.fibest();
                            JSONObject jsonObj = stock.getStockInfo("",EX_IDX , EX_CH , EX_CH_CONSOLE , false);
                        }catch (Exception e) {

                        }
                            String title = "";
                            if(e.equals("tse")){
                                title += ConsoleColors.BLUE_BACKGROUND + " 上市 "+ConsoleColors.RESET;
                            }
                            if(e.equals("otc")){
                                title += ConsoleColors.PURPLE_BACKGROUND + " 上櫃 "+ConsoleColors.RESET;
                            }
                            if(t.equals("vol")){
                                title += " " + ConsoleColors.YELLOW + "成交量"+ConsoleColors.RESET;
                            }
                            if(t.equals("up")){
                                title += " " + ConsoleColors.RED + "漲幅" +ConsoleColors.RESET;
                            }
                            if(t.equals("down")){
                                title += " " + ConsoleColors.GREEN + "跌幅" +ConsoleColors.RESET;
                            }

                            title += " 前 " + n +" 名";
                            System.out.println(ConsoleColors.RESET + title + ConsoleColors.RESET);
                        System.out.println("timer to run in 30 sec: ");
                        
                        
                    }
                }, 0, 30, TimeUnit.SECONDS);  // every 30 sec.



     
            break;
            case "db":
                c = args[1];
                y = args[2];
                switch(y)
                {
                    case "buy_top":
                        PostgreSQLJDBC.buy_top(c);
                    break;
                    case "cost":
                        PostgreSQLJDBC.cost(c);
                    break;
                }
                
            break;
            case "pg_dump":
                PostgreSQLJDBC.pg_dump();
            break;
            case "insertDB":

                if(args.length == 3){
                    d = args[2];
                }else{
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("YYYY-MM-dd");
                    d = LocalDate.now().format(formatter);
                }
               
                f = args[1];
              
                c = StringUtils.substring(f, 0, 4);

                

                reader = new BufferedReader(new InputStreamReader(new FileInputStream(Stock.class.getProtectionDomain().getCodeSource().getLocation().getPath()+"base/"+c+"/"+f), "Big5"));
                System.out.println( Stock.class.getProtectionDomain().getCodeSource().getLocation().getPath() );
                response = new String();
                String line = new String();
                int lineNum = 0;

                while ((line = reader.readLine()) != null) { 
                    if(lineNum > 2){
                        line = StringUtils.removeEnd(line, " ");
                        if(line.indexOf('"')!=-1){
                            line = line.replaceAll("(?<=\\d),(?=\\d)|\\$", "");
                        }
                        
                        line = line.replaceAll("(,)+$", "");
                        System.out.println(line);
                        line = line.replaceAll("\"", "");
                        line = line.replaceAll(",", "','");
                        line = line.replaceAll(",'',", "\\),\\(");
                        line = "('"+line+"'),";
                        line = line.replaceAll("\\)", ",'"+d+"'\\)");

                        response += line;                            
                    }
                    lineNum ++;
                }
                response =StringUtils.removeEnd(response, ",");

                PostgreSQLJDBC.insertDB("db_"+c , response);

                



            break;
            case "fubonetf":
                g = "EX_CH_7";
                download_my_stock(g);
                ses.scheduleAtFixedRate(new Runnable() {
                    @Override
                    public void run() {
                        try{
                            readIniSetting(g);
                            stock.fubonetf();
                            stock.fibest();
                            JSONObject jsonObj = stock.getStockInfo(g , EX_IDX , EX_CH , EX_CH_CONSOLE , false);
                            
                        }catch (Exception e) {

                        }
                        System.out.println("timer to run in 5 sec: ");
                    }
                }, 0, 5, TimeUnit.SECONDS);  // every 5 sec
            break;
            case "bbands":
                try{
                    reader = new BufferedReader(new InputStreamReader(new FileInputStream(Stock.class.getProtectionDomain().getCodeSource().getLocation().getPath()+"settings.json"), "UTF-8"));
                    response = new String();
                    for (String line2; (line2 = reader.readLine()) != null; response += line2);
                    JSONObject my_stock = new JSONObject(response);
                    JSONArray root = my_stock.getJSONArray("all_stock");
                    JSONArray lowerArray = new JSONArray();

                    for(int i = 0 ; i< root.length() ; i++){
                        ch = root.getJSONObject(i).getString("ch");
                        log.debug(ch);
                        PostgreSQLJDBC.update_bbands(ch,"3000");
                        //PostgreSQLJDBC.add_columns(ch);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            break;
            case "update_else_db":
                if(args.length == 2){
                    settings = args[1];
                    loop_end = "3000";
                }
                try{
                    reader = new BufferedReader(new InputStreamReader(new FileInputStream(Stock.class.getProtectionDomain().getCodeSource().getLocation().getPath()+settings+".json"), "UTF-8"));
                    response = new String();
                    for (String line2; (line2 = reader.readLine()) != null; response += line2);
                    JSONObject my_stock = new JSONObject(response);
                    JSONArray root = my_stock.getJSONArray("all_stock");
                    JSONArray lowerArray = new JSONArray();

                    for(int i = 0 ; i< root.length() ; i++){
                        ch = root.getJSONObject(i).getString("ch");
                        log.debug(ch);
                        
                        //PostgreSQLJDBC.alter_day_ma(ch);
                        
                        PostgreSQLJDBC.update_db_0001_52_week_height(ch);
                        PostgreSQLJDBC.update_db_0003_price_volumn_recent_days(ch);
                        PostgreSQLJDBC.update_day_ma(ch,loop_end);
                        
                        PostgreSQLJDBC.update_db_0004_ma(ch);
                        PostgreSQLJDBC.uodate_db_0004_ma200_rise(ch);
                        PostgreSQLJDBC.uodate_db_0004_ma240_rise(ch);
                        PostgreSQLJDBC.uodate_db_0004_ma1_rise(ch);
                        PostgreSQLJDBC.uodate_db_0006_volumn_explode(ch);
                        PostgreSQLJDBC.update_bbands(ch,loop_end);
                        PostgreSQLJDBC.updata_db_0008_bbands(ch);
                    }
                    PostgreSQLJDBC.create_52_week_day_by_day();
                    PostgreSQLJDBC.create_bbands_day_by_day();
                    PostgreSQLJDBC.db_0005_trend_template();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            break;
            case "create_new_stock_table":
                //PostgreSQLJDBC.createStockPriceTable();
                try{
                    reader = new BufferedReader(new InputStreamReader(new FileInputStream(Stock.class.getProtectionDomain().getCodeSource().getLocation().getPath()+"settings_reset.json"), "UTF-8"));
                    response = new String();
                    for (String line2; (line2 = reader.readLine()) != null; response += line2);
                    String name;

                    JSONObject my_stock = new JSONObject(response);
                    JSONArray root = my_stock.getJSONArray("all_stock");
                    JSONArray lowerArray = new JSONArray();

                    for(int i = 0 ; i< root.length() ; i++){
                        ch = root.getJSONObject(i).getString("ch");
                        name = root.getJSONObject(i).getString("name");
                        log.debug(ch);
                        PostgreSQLJDBC.createStockPriceTable(ch,name);
                        PostgreSQLJDBC.insert_db_0010_profile(ch , name);
                        PostgreSQLJDBC.createDailyReport(ch);
                        PostgreSQLJDBC.createTradDetail(ch);
                        PostgreSQLJDBC.createkdtable(ch);
                    }
                    

                } catch (IOException e) {
                    e.printStackTrace();
                }
            break;
            case "update_main_price_db":
                if(args.length == 2){
                    settings = args[1];
                }
                stock.update_main_price_db(settings);



            break;
            case "realtime":
                if(args.length == 2){
                    g = args[1];
                }
                log.debug(g);
                readIniSetting(g);

                download_my_stock(g);
                ses.scheduleAtFixedRate(new Runnable() {
                    @Override
                    public void run() {
                        try{
                            readIniSetting(g);
                            stock.fibest();
                            log.debug(EX_IDX);
                            JSONObject jsonObj = stock.getStockInfo(g , EX_IDX , EX_CH , EX_CH_CONSOLE , false);
                            //stock.getStockInfoJsoup(g , EX_IDX , EX_CH , EX_CH_CONSOLE , false);
                            
                        }catch (Exception e) {

                        }
                        System.out.println("timer to run in 5 sec: ");
                    }
                }, 0, 5, TimeUnit.SECONDS);  // every 5 sec
            break;
            case "branch":
                PostgreSQLJDBC.importBranchCSVtoDB();
            break;
            case "extract60min":
                stock.extract60min();
            break;

            case "createkdtable":
                stock.createkdtable();
            break;

            case "profile":
                stock.profile();



            break;
            case "create_daily_report_table":
                stock.daily_report();
            break;

            case "realtime_to_db":
                if(args.length == 2){
                    g = args[1];
                }

                ses.scheduleAtFixedRate(new Runnable() {
                    @Override
                    public void run() {
                        try{
                            readIniSetting("EX_CH_9");

                            PostgreSQLJDBC.fibest();
                            PostgreSQLJDBC.getStockInfo(EX_CH);
                            
                        }catch (Exception e) {

                        }
                        //System.out.println("timer to run in 5 sec: ");
                    }
                }, 0, 5, TimeUnit.SECONDS);  // every 5 sec
            break;
            case "getRandomProfile":
                String mych =  PostgreSQLJDBC.getRandomProfile("2019-04-05");
                System.out.println(mych);
            break;
            case "import_trade_detail":
                //stock.importTradeDetail("settings");
            break;
            case "daily_report":
                String it = new String();
                String ic = new String();
                if(args.length == 3){
                    it = args[1];
                    ic = args[2];
                }
                if(args.length == 2){
                    it = args[1];
                }
                
                if(ic.isEmpty() && it.isEmpty()){
                    System.out.println("1");
                    PostgreSQLJDBC.CSVConvertToTemp( );    
                }
                if(ic.isEmpty()  && !it.isEmpty()){
                    System.out.println("2");

                    PostgreSQLJDBC.CSVConvertToTemp( it  );    
                }
                if(!ic.isEmpty() && !it.isEmpty()){
                    System.out.println("3");
                    PostgreSQLJDBC.CSVConvertToTemp( it , ic  );
                }
                
            break;

            case "loadcsv":
                CSVLoader loader = new CSVLoader();
                loader.setSeparator(' ');
                loader.loadCSV(System.getProperty("user.dir")+File.separator+"CSV"+File.separator+"2019-03-21"+File.separator+"temp.csv", "db_4426_daily_report", true);
            break;
            case "box":
                stock.box();


            break;

            case "trade_detail_reset":
                if(args.length == 2){
                    settings = args[1];
                }
                stock.resetDBTradeDetail(settings);
            break;
            case "trade_detail_download":
                if(args.length == 2){
                    settings = args[1];
                }
                stock.tradeDetailDownload(settings);
            break;
            case "youtube_dl_by_video_id":
                YoutubeAPI.downloadVideoByVideoId();
            break;
            case "youtube_dl_by_channel_id":
                if(args.length == 2){
                    String channelId = args[1];
                    YoutubeAPI.downloadVideoByChannelId(channelId);
                }
            break;
            case "youtube_dl_auto":
                YoutubeAPI.downloadVideoByChannelId();
            break;
            case "youtube_dl":
                YoutubeAPI.collectVideoIdsByUser("UCbHD6HX38bufVR1pF076Z0g");

                
            break;
            case "trade_detail":
            
                if(args.length == 2){
                    ch = args[1];
                }
                ses.scheduleAtFixedRate(new Runnable() {
                    @Override
                    public void run() {
                        try{
                            readIniSetting();
                            List<String> chList = PostgreSQLJDBC.getFocusCH();
                            for (String mych : chList) {
                                stock.tradeDetail(mych);
                            }                            
                        }catch (Exception e) {

                        }
                        System.out.println("timer to run in 5 sec: ");
                        int timestamp_end = (int) (new Date().getTime()/1000);

                        System.out.println( "程式跑 : " + (timestamp_end - timestamp_start) +"/1800 秒自動關閉");
                        if((timestamp_end - timestamp_start) > 1800){
                            System.out.println("30分程式自動關閉");

                            System.exit(0); 

                        }
                        
                    }
                }, 0, 5, TimeUnit.SECONDS);  // every 30 sec.



     
            break;            
            default:
            break;

        }



    }
}
