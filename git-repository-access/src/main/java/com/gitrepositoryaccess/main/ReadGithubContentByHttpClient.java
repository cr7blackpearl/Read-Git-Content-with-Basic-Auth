package com.gitrepositoryaccess.main;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;

public class ReadGithubContentByHttpClient {

	public static void main(String[] args) {

		// Replace this token with your actual token.
		String token = "3bac278d2dc0bbfa36e7677365e5a5491119099c";

		String url = "raw.githubusercontent.com/cr7blackpearl/demo/master/README.md";

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
