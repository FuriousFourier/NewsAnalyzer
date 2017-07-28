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

	public boolean addNewData() throws IOException {
		URL url = new URL("http://localhost:8080/addNewThingsToDB?secNum=" + NewsAnalyzerMain.getSecurityNumber());
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setRequestProperty("User-Agent", USER_AGENT);
		int responseCode = connection.getResponseCode();
		if (responseCode != 200) {
			throw new HTTPException(responseCode);
		}
		Scanner scanner = new Scanner(connection.getInputStream());
		return scanner.nextBoolean();
	}

	public boolean getNewFeeds() throws IOException {
		URL url = new URL("http://localhost:8080/getNewFeeds?secNum=" + NewsAnalyzerMain.getSecurityNumber());
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setRequestProperty("User-Agent", USER_AGENT);
		int responseCode = connection.getResponseCode();
		if (responseCode != 200) {
			throw new HTTPException(responseCode);
		}
		Scanner scanner = new Scanner(connection.getInputStream());
		return scanner.nextBoolean();
	}

	public boolean initDb() throws IOException{
		URL url = new URL("http://localhost:8080/initDB?secNum=" + NewsAnalyzerMain.getSecurityNumber());
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setRequestProperty("User-Agent", USER_AGENT);
		int responseCode = connection.getResponseCode();
		if (responseCode != 200) {
			throw new HTTPException(responseCode);
		}
		Scanner scanner = new Scanner(connection.getInputStream());
		return scanner.nextBoolean();
	}

	public boolean createCurrencyTagStats() throws IOException {
		URL url = new URL("http://localhost:8080/createTagStats?secNum=" + NewsAnalyzerMain.getSecurityNumber());
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setRequestProperty("User-Agent", USER_AGENT);
		int responseCode = connection.getResponseCode();
		if (responseCode != 200) {
			throw new HTTPException(responseCode);
		}
		Scanner scanner = new Scanner(connection.getInputStream());
		return scanner.nextBoolean();
	}
}
