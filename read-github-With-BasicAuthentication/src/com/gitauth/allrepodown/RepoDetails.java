package com.gitauth.allrepodown;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class RepoDetails {
	public static void main(String[] args) throws IOException {
		URLConnection openConnection = new URL("https://api.github.com/repos/cr7blackpearl/TestProject/commits/6932b7b787aca72ea42e0cf73da5ccf7d654d2f4").openConnection();
		System.out.println(openConnection);
		Object content = openConnection.getContent();
		System.out.println(content);
	}

}
