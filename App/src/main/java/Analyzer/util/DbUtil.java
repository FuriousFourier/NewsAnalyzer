package Analyzer.util;

import Analyzer.NewsAnalyzerMain;

import javax.xml.ws.http.HTTPException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

import static org.springframework.http.HttpHeaders.USER_AGENT;

public class DbUtil {

	public boolean checkIfDbEmpty() throws IOException {
		URL url = new URL("http://localhost:8080/checkIfDBEmpty?secNum=" + NewsAnalyzerMain.getSecurityNumber());
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setRequestProperty("User-Agent", USER_AGENT);
		int responseCode = connection.getResponseCode();
		if (responseCode != 200) {
			throw new HTTPException(responseCode);
		}
		Scanner scanner = new Scanner(connection.getInputStream());
		return scanner.nextBoolean();
	}

	public boolean addSomething() throws IOException {
		URL url = new URL("http://localhost:8080/addThingsToDB?secNum=" + NewsAnalyzerMain.getSecurityNumber());
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setRequestProperty("User-Agent", USER_AGENT);
		int responseCode = connection.getResponseCode();
		if (responseCode != 200) {
			throw new HTTPException(responseCode);
		}
		Scanner scanner = new Scanner(connection.getInputStream());
		return scanner.nextBoolean();
	}

	public void getNewFeeds() throws IOException {
		URL url = new URL("http://localhost:8080/getNewFeeds?secNum=" + NewsAnalyzerMain.getSecurityNumber());
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setRequestProperty("User-Agent", USER_AGENT);
		int responseCode = connection.getResponseCode();
		if (responseCode != 200) {
			throw new HTTPException(responseCode);
		}
	}
}
