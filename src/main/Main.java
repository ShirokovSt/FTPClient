package src.main;

import java.io.*;
import java.net.Socket;
import java.util.*;

import src.main.FTPProtocol.FTPClient;

public class Main {
    public static void main(final String[] args) throws IOException, InterruptedException {
        if(args.length == 3) {
            FTPClient ftpCl = new FTPClient(args[0], args[1], args[2]);
            ftpCl.connect();
            ftpCl.authorization();
            System.out.print("Authorization is successful!\nSelect the mode: passive - 0, active - 1\nYour choice: ");
            Scanner scan = new Scanner(System.in);
            boolean mode = scan.nextInt() == 1;
            scan.nextLine();
            String port = null;
            if(mode) {
                System.out.print("Input port: ");
                port = scan.nextLine();
            }
            boolean isExit = false;
            Socket socket;
            while(!isExit) {
                System.out.print("Select an action:\nGetting a list of students by name - getList\n" +
                        "Getting information about a student by id - getSt <id>\n" +
                        "Add a student - addSt <name>\nDelete a student - delSt <id>\nExit - exit\nYour choice: ");
                String commands = scan.nextLine();
                String[] parts = commands.split(" ");
                System.out.println();
                switch (parts[0]) {
                    case "getList":
                        if (mode)
                            socket = ftpCl.enActiveMode(args[2], port);
                        else
                            socket = ftpCl.enPassiveMode();
                        ftpCl.downloadFromServer("json.txt", socket);
                        ArrayList<String> namesList = ftpCl.getStudentsList("json.txt");
                        System.out.println("Result:");
                        for (String name : namesList) {
                            System.out.println(name);
                        }
                        break;
                    case "getSt":
                        if (mode)
                            socket = ftpCl.enActiveMode(args[2], port);
                        else
                            socket = ftpCl.enPassiveMode();
                        if (parts.length != 2)
                            System.out.println("Error args...");
                        else {
                            ftpCl.downloadFromServer("json.txt", socket);
                            System.out.println(ftpCl.getInfo(parts[1], "json.txt"));
                        }
                        break;
                    case "addSt":
                        if (mode)
                            socket = ftpCl.enActiveMode(args[2], port);
                        else
                            socket = ftpCl.enPassiveMode();
                        if (parts.length != 2)
                            System.out.println("Error args...");
                        else {
                            ftpCl.downloadFromServer("json.txt", socket);
                            ftpCl.addStudent(parts[1], "json.txt");
                            System.out.println("Successful");
                            if (mode)
                                socket = ftpCl.enActiveMode(args[2], port);
                            else
                                socket = ftpCl.enPassiveMode();
                            ftpCl.uploadToServer("json.txt", socket);
                        }
                        break;
                    case "delSt":
                        if (mode)
                            socket = ftpCl.enActiveMode(args[2], port);
                        else
                            socket = ftpCl.enPassiveMode();
                        if (parts.length != 2)
                            System.out.println("Error args...");
                        else {
                            ftpCl.downloadFromServer("json.txt", socket);
                            if (ftpCl.eraseStudent(parts[1], "json.txt"))
                                System.out.println("Successful");
                            else
                                System.out.println("Not found...");
                            if (mode)
                                socket = ftpCl.enActiveMode(args[2], port);
                            else
                                socket = ftpCl.enPassiveMode();
                            ftpCl.uploadToServer("json.txt", socket);
                        }
                        break;
                    case "exit":
                        ftpCl.disconnect();
                        isExit = true;
                        break;
                    default: System.out.println("Command not found...");
                }
                System.out.println();
            }
		}
    }
}