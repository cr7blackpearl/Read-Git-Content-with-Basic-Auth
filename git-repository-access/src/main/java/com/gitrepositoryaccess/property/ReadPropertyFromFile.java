package com.gitrepositoryaccess.property;

import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

public class ReadPropertyFromFile {

	public Properties getProperty() throws IOException {
		FileReader reader = new FileReader("token.properties");
		Properties properties = new Properties();
		properties.load(reader);
		
		return properties;
	}

}
