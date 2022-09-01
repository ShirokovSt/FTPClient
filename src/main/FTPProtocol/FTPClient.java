package src.main.FTPProtocol;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.logging.*;
import java.util.stream.Collectors;

public class FTPClient {
    static Logger logger = Logger.getLogger(FTPClient.class.getName());
    private final String login;
    private final String password;
    private final String ip;
    private Socket socket;
    private InputStream is;
    private OutputStream os;
    private BufferedReader br;
    private ServerSocket serv;
    private static final int OFFSET_NAME = 9;
    private static final int OFFSET_ID = 6;
    private static final int OFFSET_ID_TO_BEG_OBJ = 11;
    private static final int OFFSET_TO_END_OBJ = 29;
    public FTPClient(String login, String password, String ip) throws IOException {
        this.login = login;
        this.password = password;
        this.ip = ip;
        LogManager.getLogManager().reset();
        logger.addHandler(new FileHandler("myLog.log"));
    }
    public void connect() throws IOException {
        this.socket = new Socket(ip, 1025);
        br = new BufferedReader(new InputStreamReader(System.in)); // читает данные с консоли
        is = socket.getInputStream(); //читает данные с сервера
        os = socket.getOutputStream(); //отправляет данные на сервер
        log(is);
    }
    public static void log(InputStream is) throws IOException {
        while(is.available() > 0) {
            byte[] buffer = new byte[150];
            int length = is.read(buffer);
            String s = new String(buffer, 0, length);
            logger.log(Level.INFO, s);
        }
    }
    public void authorization() throws IOException {
        os.write(("USER " + login + "\n").getBytes());
        byte[] buffer = new byte[100];
        int length = is.read(buffer);
        String s = new String(buffer, 0, length);
        System.out.println(s);
        os.write(("PASS " + password + "\n").getBytes());
        log(is);
    }
    public void disconnect() throws IOException, InterruptedException {
        if(!socket.isClosed()) {
            os.write("QUIT\n".getBytes());
            log(is);
            os.close();
            is.close();
            br.close();
            this.socket.close();
            Thread.sleep(100);
        }
    }
    public Socket enPassiveMode() throws IOException {
        os.write("PASV\n".getBytes());
        String[] sSplit = new String[0];
        while (sSplit.length <= 1) {
            byte[] buffer = new byte[100];
            int length = is.read(buffer);
            String s = new String(buffer, 0, length);
            logger.log(Level.INFO, s);
            sSplit = s.split(",|\\)");
        }

        int serverPortPassive = Integer.parseInt(sSplit[sSplit.length - 3]) * 256 + Integer.parseInt(sSplit[sSplit.length - 2]);
        return new Socket(ip, serverPortPassive);
    }
    public void uploadToServer(String path, Socket transferSocket) throws IOException {
        this.os.write(("STOR " + path + "\n").getBytes());
        log(is);

        BufferedReader br = new BufferedReader(new FileReader(path));
        OutputStream os = transferSocket.getOutputStream();
        os.write(((br.lines().collect(Collectors.joining())) + "\n").getBytes());
        os.close();
        br.close();
        if(serv != null)
            if(!serv.isClosed())
                serv.close();
        log(is);
    }
    public void downloadFromServer(String path, Socket transferSocket) throws IOException {
        this.os.write(("RETR " + path + "\n").getBytes());
        log(this.is);

        BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(path));
        BufferedInputStream is = new BufferedInputStream(transferSocket.getInputStream());
        byte[] buf = new byte[1024];
        int l = 0;
        while ((l = is.read(buf, 0, 1024)) != -1)
            os.write(buf, 0, l);
        os.close();
        is.close();
        if(serv != null)
            if(!serv.isClosed())
                serv.close();

        log(this.is);
    }
    public Socket enActiveMode(String ip, String port) throws IOException {
        ip = ip.replace('.',',');
        serv = new ServerSocket(Integer.parseInt(port));

        int p1 = Integer.parseInt(port) / 256;
        int p2 = Integer.parseInt(port) % 256;
        this.os.write(("PORT " + ip + "," + p1 + "," + p2 + "\n").getBytes());

        Socket clientSocket = serv.accept();

        log(is);
        return clientSocket;
    }
    public ArrayList<String> getStudentsList(String path) throws FileNotFoundException { // получение списка студентов по имени
        BufferedReader br = new BufferedReader(new FileReader(path));
        String str = br.lines().collect(Collectors.joining());
        int index = -1;
        int indexID = -1;
        ArrayList<String> namesList = new ArrayList<>();
        while(true) {
            indexID = str.indexOf("\"id\":");
            index = str.indexOf("\"name\":");
            if(index == -1)
                break;
            index += OFFSET_NAME;
            indexID += OFFSET_ID;
            StringBuilder tmpStr = new StringBuilder();
            StringBuilder tmpStrId = new StringBuilder();
            while (str.charAt(index) != '\"') {
                char c = str.charAt(index);
                tmpStr.append(c);
                index++;
            }
            while (str.charAt(indexID) != ',') {
                char c = str.charAt(indexID);
                tmpStrId.append(c);
                indexID++;
            }
            namesList.add(tmpStr.toString() + " (id: " + tmpStrId.toString() +")");
            str = str.substring(index);
        }
        Collections.sort(namesList);
        return namesList;
    }
    public String getInfo(String id, String path) throws FileNotFoundException {
        BufferedReader br = new BufferedReader(new FileReader(path));
        String str = br.lines().collect(Collectors.joining());
        int index = -1;
        while(true) {
            index = str.indexOf("\"id\":");
            if(index == -1)
                break;
            index += OFFSET_ID;
            StringBuilder tmpStr = new StringBuilder();
            while (str.charAt(index) != ',') {
                char c = str.charAt(index);
                tmpStr.append(c);
                index++;
            }
            if (tmpStr.toString().equals(id)) {
                str = str.substring(index);
                index = str.indexOf("\"name\":");
                index += OFFSET_NAME;
                StringBuilder name = new StringBuilder();
                while (str.charAt(index) != '\"') {
                    char c = str.charAt(index);
                    name.append(c);
                    index++;
                }
                return new String("INFO: id: " + id + ", name: " + name);
            }
            str = str.substring(index);
        }
        return new String("INFO: The student was not found...");
    }
    public void addStudent(String name, String path) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(path));
        String str = br.lines().collect(Collectors.joining());
        int index = -1;

        index = str.lastIndexOf("\"id\":");
        if (index == -1) {
            FileWriter fw = new FileWriter(path);
            fw.write("{  \"students\": [     {      \"id\": 1,      \"name\": \"" + name + "\"    }  ]  }");
            fw.close();
            return;
        }
        index += 6;
        StringBuilder tmpStr = new StringBuilder();
        while (str.charAt(index) != ',') {
            char c = str.charAt(index);
            tmpStr.append(c);
            index++;
        }
        String newId = String.valueOf((Integer.parseInt(tmpStr.toString()) + 1));
        String jsonObj = new String(",    {      \"id\": " + newId + ",      \"name\": \"" + name + "\"    }  ]  }");
        index = str.lastIndexOf("}");
        str = str.substring(0, index);
        index = str.lastIndexOf("}");
        str = str.substring(0, index + 1);
        str += jsonObj;
        FileWriter fw = new FileWriter(path);
        fw.write(str);
        fw.close();
    }
    public boolean eraseStudent(String id, String path) throws IOException {
        String name = getInfo(id, path);
        if(name.equals("INFO: The student was not found..."))
            return false;
        name = name.substring(name.indexOf("e:") + 3, name.length());

        BufferedReader br = new BufferedReader(new FileReader(path));
        String str = br.lines().collect(Collectors.joining());
        int curEr = str.indexOf("\"id\": " + id);
        int startDel = curEr - OFFSET_ID_TO_BEG_OBJ;
        int endDel = curEr + OFFSET_TO_END_OBJ + name.length() + id.length();
        int p = str.charAt(endDel - 1);
        if(str.charAt(endDel - 1) == ' ') {
            endDel--;
            while(str.charAt(startDel) != ',') {
                startDel--;
                if(str.charAt(startDel) == '[') {
                    str = "";
                    FileWriter fw = new FileWriter(path);
                    fw.write(str);
                    fw.close();
                    return true;
                }
            }
            startDel--;
        }
        str = str.substring(0, startDel + 1) + str.substring(endDel);
        FileWriter fw = new FileWriter(path);
        fw.write(str);
        fw.close();
        return true;
    }
    public Socket getSocket() {
        return socket;
    }
}
