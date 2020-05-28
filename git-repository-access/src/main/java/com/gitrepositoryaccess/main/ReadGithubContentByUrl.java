package com.gitrepositoryaccess.main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;
import java.net.URLConnection;
import java.util.Properties;
import java.util.logging.Logger;

import org.apache.commons.codec.binary.Base64;

import com.gitrepositoryaccess.util.UtilityFile;

public class ReadGithubContentByUrl {
	private final static Logger logger = Logger.getLogger(ReadGithubContentByUrl.class.getName());

	public static void main(String[] args) throws IOException {
		// ReadPropertyFromFile readProperty=new ReadPropertyFromFile();
		Properties property = UtilityFile.getValuesFromProperty();

		getGithubContentUsingURLConnection(property.getProperty("token"), property.getProperty("url"));

	}

	private static void getGithubContentUsingURLConnection(String token, String url) {
		String newUrl = "https://" + url;
		logger.info("FUll Github Url : " + newUrl);
		try {
			URL myURL = new URL(newUrl);
			URLConnection connection = myURL.openConnection();
			token = token + ":x-oauth-basic";
			String authString = "Basic " + Base64.encodeBase64String(token.getBytes());
			connection.setRequestProperty("Authorization", authString);
			InputStream authInStream = connection.getInputStream();
			System.out.println(getStringFromStream(authInStream));

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static String getStringFromStream(InputStream authInStream) throws IOException {
		if (authInStream != null) {
			Writer contenWriter = new StringWriter();

			char[] contentBuffer = new char[2048];
			try {
				Reader crunchifyReader = new BufferedReader(new InputStreamReader(authInStream, "UTF-8"));
				int counter;
				while ((counter = crunchifyReader.read(contentBuffer)) != -1) {
					contenWriter.write(contentBuffer, 0, counter);
				}
			} finally {
				authInStream.close();
			}
			return contenWriter.toString();
		} else {
			return "No Contents";
		}
	}

}
