package com.gitrepositoryaccess.modifiedfilesondate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.gitrepositoryaccess.util.UtilityFile;

public class ReadListOfModifiedFiles {
	private final static Logger logger = Logger.getLogger(ReadListOfModifiedFiles.class.getName());
	
	public static void main(String[] args) throws IOException, ParseException {
		getFilesOnDate();
		logger.info("Request GET DONE");
	}

	private static void getFilesOnDate() throws ParseException, IOException {
		StringBuffer response = UtilityFile.connectToUrl();
		@SuppressWarnings("resource")
		Scanner scanner = new Scanner(System.in);
		System.out.println("Enter the Date ");

		String date = scanner.next();

		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd");
		String strDate = dateFormat.format(dateFormat.parse(date));
		System.out.println("Files modified on date :" + strDate);

		List<String> data = Arrays.stream(response.toString().split("\"commit\":")).skip(1)
				.collect(Collectors.toList());
		if (data.toString().contains(strDate)) {
			for (String alldata : data) {
				if (alldata.contains(strDate)) {
					List<String> collect = Arrays.stream(alldata.toString().split("\"url\":")).skip(2)
							.map(l -> l.split(",")[0]).collect(Collectors.toList());
					getModifiedFilesName(collect.get(1).replaceAll("\"", ""));
				}
			}
		} else {
			logger.info("No file modified on this Date.");
		}
	}

	private static String getModifiedFilesName(String collect) throws IOException {
		Properties property = UtilityFile.getValuesFromProperty();
		URL obj = new URL(collect);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
		con.setRequestMethod("GET");
		con.setRequestProperty("User-Agent", property.getProperty("USER_AGENT"));
		int responseCode = con.getResponseCode();
		logger.info("GET Response Code :: " + responseCode);
		String fileName = null;
		if (responseCode == HttpURLConnection.HTTP_OK) {
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));

			String inputLine;
			StringBuffer response = new StringBuffer();
			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
				fileName = Arrays.stream(response.toString().split("\"filename\":")).skip(1).map(l -> l.split(",")[0])
						.collect(Collectors.joining("\n"));
				System.out.println(fileName);
			}
			in.close();
		} else {
			logger.info("GET request not worked");
		}
		return fileName;
	}
}
