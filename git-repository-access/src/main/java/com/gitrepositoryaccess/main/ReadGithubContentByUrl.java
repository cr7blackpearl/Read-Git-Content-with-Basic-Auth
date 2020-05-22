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

import org.apache.commons.codec.binary.Base64;

public class ReadGithubContentByUrl {
	
	public static void main(String[] args) {
		 
		// Replace this token with your actual token
		String token = "016c03bf6b03428949b7046535a32819cbe12aaf";
 
		String url = "raw.githubusercontent.com/cr7blackpearl/TestProject/master/README.md";
 
		//URLConnection Method to get Private Github content with Basic OAuth token
		getGithubContentUsingURLConnection(token, url);
 
	}
	
	private static void getGithubContentUsingURLConnection(String token, String url) {
		String newUrl = "https://" + url;
		System.out.println(newUrl);
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
			Writer crunchifyWriter = new StringWriter();
 
			char[] crunchifyBuffer = new char[2048];
			try {
				Reader crunchifyReader = new BufferedReader(new InputStreamReader(authInStream, "UTF-8"));
				int counter;
				while ((counter = crunchifyReader.read(crunchifyBuffer)) != -1) {
					crunchifyWriter.write(crunchifyBuffer, 0, counter);
				}
			} finally {
				authInStream.close();
			}
			return crunchifyWriter.toString();
		} else {
			return "No Contents";
		}
	}

}
