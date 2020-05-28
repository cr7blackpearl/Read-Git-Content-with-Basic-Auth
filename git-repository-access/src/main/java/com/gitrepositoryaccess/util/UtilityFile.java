package com.gitrepositoryaccess.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Properties;
import java.util.logging.Logger;

public class UtilityFile {
	private final static Logger logger = Logger.getLogger(UtilityFile.class.getName());

	public static Properties getValuesFromProperty() throws IOException {
		FileReader reader = new FileReader("token.properties");
		Properties properties = new Properties();
		properties.load(reader);

		return properties;
	}

	public static StringBuffer connectToUrl() {
		Properties property;
		try {
			property = UtilityFile.getValuesFromProperty();
			URL obj = new URL(property.getProperty("url"));
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();
			con.setRequestMethod("GET");
			con.setRequestProperty("User-Agent", property.getProperty("USER_AGENT"));
			int responseCode = con.getResponseCode();
			logger.info("GET Response Code :: " + responseCode);
			if (responseCode == HttpURLConnection.HTTP_OK) {
				BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
				String inputLine;
				StringBuffer response = new StringBuffer();
				while ((inputLine = in.readLine()) != null) {

					return response.append(inputLine);
				}
				in.close();
			} else {
				logger.info("GET Response Code :: " + responseCode);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}	
}
