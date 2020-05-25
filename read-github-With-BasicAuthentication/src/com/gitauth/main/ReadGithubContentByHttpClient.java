package com.gitauth.main;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;

public class ReadGithubContentByHttpClient {

	public static void main(String[] args) {

		// Replace this token with your actual token.
		String token = "f129213328d183f3b9c8f34e5f5716afd9f84aaf";

		String url = "raw.githubusercontent.com/cr7blackpearl/TestProject/master/README.md";

		// HttpClient Method to get Private Github content with Basic OAuth token.
		getGithubContentUsingHttpClient(token, url);

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
