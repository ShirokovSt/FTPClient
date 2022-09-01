package src.test.FTPProtocol;

import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.testng.TestNG;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import src.main.FTPProtocol.FTPClient;

public class FTPClientTest {
	
	public static void main(String[] args) {
		TestNG testSuite = new TestNG();
		testSuite.setTestClasses(new Class[] { FTPClientTest.class });
		testSuite.run();
	}
    FTPClient ftpCl = new FTPClient("comp4621", "network", Inet4Address.getLocalHost().getHostAddress());

    public FTPClientTest() throws IOException {
    }

    @BeforeTest
    public void init() throws IOException {
        FileWriter writer = new FileWriter ("testJson.txt");
        writer.write("{  \"students\": [    {      \"id\": 1,      \"name\": \"Vano\"    }," +
                        "    {      \"id\": 2,      \"name\": \"Alekseev\"    },    {      " +
                        "\"id\": 3,      \"name\": \"Sharapov\"    },     {      \"id\": 6," +
                        "      \"name\": \"Karinov\"    }  ]  }");
        writer.close();
        ftpCl.connect();
        ftpCl.authorization();
        Socket socket = ftpCl.enPassiveMode();
        ftpCl.uploadToServer("testJson.txt", socket);
    }

    @Test(priority = 1)
    public void testListId() throws IOException {
        Socket socket = ftpCl.enPassiveMode();
        ftpCl.downloadFromServer("testJson.txt", socket);
        ArrayList<String> realList = ftpCl.getStudentsList("testJson.txt");
        ArrayList<String> expectedList = new ArrayList<>(Arrays.asList("Alekseev (id: 2)", "Karinov (id: 6)", "Sharapov (id: 3)", "Vano (id: 1)"));
        Assert.assertEquals(realList, expectedList, "The lists don't match!!!");
    }

    @Test(priority = 2)
    public void testGetId() throws IOException {
        Socket socket = ftpCl.enPassiveMode();
        ftpCl.downloadFromServer("testJson.txt", socket);
        String realStr = ftpCl.getInfo("2", "testJson.txt");
        String expectedStr = "INFO: id: 2, name: Alekseev";
        Assert.assertEquals(realStr, expectedStr, "The lines don't match!!!");
    }

    @Test(priority = 3)
    public void testAddSt() throws IOException {
        Socket socket = ftpCl.enPassiveMode();
        ftpCl.downloadFromServer("testJson.txt", socket);
        ftpCl.addStudent("Reshetov", "testJson.txt");
        socket = ftpCl.enPassiveMode();
        ftpCl.uploadToServer("testJson.txt", socket);
        socket = ftpCl.enPassiveMode();
        ftpCl.downloadFromServer("testJson.txt", socket);
        ArrayList<String> list = ftpCl.getStudentsList("testJson.txt");

        Set<String> set = new HashSet<String>(list);
        Assert.assertTrue(set.contains("Reshetov"), "Adding didn't work");
    }

    @Test(priority = 4)
    public void testDelSt() throws IOException {
        Socket socket = ftpCl.enPassiveMode();
        ftpCl.downloadFromServer("testJson.txt", socket);
        ftpCl.eraseStudent("1", "testJson.txt");
        socket = ftpCl.enPassiveMode();
        ftpCl.uploadToServer("testJson.txt", socket);
        socket = ftpCl.enPassiveMode();
        ftpCl.downloadFromServer("testJson.txt", socket);
        ArrayList<String> list = ftpCl.getStudentsList("testJson.txt");

        Set<String> set = new HashSet<String>(list);
        Assert.assertFalse(set.contains("Vano"), "Adding didn't work");
    }

    @Test(priority = 5)
    public void testExit() throws IOException, InterruptedException {
        ftpCl.disconnect();
        Assert.assertTrue(ftpCl.getSocket().isClosed(), "The shutdown did not work");
    }
}
