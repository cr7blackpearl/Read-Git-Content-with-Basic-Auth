package com.gitrepositoryaccess.main;

import java.io.IOException;
import java.util.Properties;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;

import com.gitrepositoryaccess.util.UtilityFile;

public class ReadGithubContentByHttpClient {

	public static void main(String[] args) throws IOException {

		// ReadPropertyFromFile readProperty = new ReadPropertyFromFile();
		Properties property = UtilityFile.getValuesFromProperty();

		// HttpClient Method to get Private Github content with Basic OAuth token.
		getGithubContentUsingHttpClient(property.getProperty("token"), property.getProperty("url"));

	}

	private static void getGithubContentUsingHttpClient(String token, String url) {
		String newUrl = "https://" + token + ":x-oauth-basic@" + url;
		HttpClient client = new DefaultHttpClient();
		HttpGet request = new HttpGet(newUrl);
		System.out.println(newUrl);
		try {
			HttpResponse response = client.execute(request);
			String responseString = new BasicResponseHandler().handleResponse(response);
			System.out.println(responseString);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
