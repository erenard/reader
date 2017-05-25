package manga;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

public class OptionManager {
	
	public static final String WORKING_DIRECTORY = "workingDirectory";
	public static final String DEBUG = "debug";
	public static final String SENSIVITY_DIVIDER = "sensitivityDivider";
	public static final String LANGUAGE = "language";
	
	private static final long serialVersionUID = 1L;
	private static OptionManager instance = new OptionManager();

	public static OptionManager getInstance() {
		return instance;
	}
	
	private File file;
	private Properties properties;
	private OptionManager() {
		properties = new Properties();
		//Options par defaut:
		properties.setProperty(DEBUG, "false");
		properties.setProperty(SENSIVITY_DIVIDER, "4");
		properties.setProperty(LANGUAGE, "EN");
		file = new File("manga.xml");
		if(file.exists()) {
			try {
				FileInputStream inputStream = new FileInputStream(file);
				properties.loadFromXML(inputStream);
			} catch(FileNotFoundException e) {
			} catch(IOException e) {
			}
		}
	}

	public void save() {
		try {
			FileOutputStream outputStream = new FileOutputStream(file);
			properties.storeToXML(outputStream, "");
		} catch(FileNotFoundException e) {
		} catch(IOException e) {
		}
	}

	public String getProperty(String s) {
		return properties.getProperty(s);
	}

	public void setProperty(String name, String value) {
		properties.setProperty(name, value);
	}
}
