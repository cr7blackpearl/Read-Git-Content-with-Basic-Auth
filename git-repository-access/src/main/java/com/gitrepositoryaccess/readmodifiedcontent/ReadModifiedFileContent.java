package com.gitrepositoryaccess.readmodifiedcontent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.Base64.Decoder;
import java.util.stream.Collectors;

public class ReadModifiedFileContent {
	private static final String USER_AGENT = "Mozilla/5.0";

	private static final String GET_URL = "https://api.github.com/repos/cr7blackpearl/TestProject/git/blobs/5fca53f730a0c49db162f2a30fc648835530cae6";

	public static void main(String[] args) throws IOException {
		sendGET();
		System.out.println("GET DONE");
	}

	private static void sendGET() throws IOException {
		URL obj = new URL(GET_URL);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
		con.setRequestMethod("GET");
		con.setRequestProperty("User-Agent", USER_AGENT);
		int responseCode = con.getResponseCode();
		System.out.println("GET Response Code :: " + responseCode);

		if (responseCode == HttpURLConnection.HTTP_OK) {
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();
			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
				getContent(response);

			}
			in.close();
			System.out.println(response.toString());
		} else {
			System.out.println("GET request not worked");
		}
	}

	private static void getContent(StringBuffer response) {
		Decoder decoder = java.util.Base64.getMimeDecoder();
		String data = Arrays.stream(response.toString().split("\"content\":")).skip(1).map(l -> l.split(",")[0])
				.collect(Collectors.joining());
		System.out.println(new String(decoder.decode(data)));

	}
}
