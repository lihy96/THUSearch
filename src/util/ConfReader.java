package util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Map;

public class ConfReader {
	
	public static boolean confRead(String fileName, Map<String, String> conf) {
		FileReader fr = null;
		BufferedReader br = null;
		try {
			fr = new FileReader(fileName);
			br = new BufferedReader(fr);
			String line;
			
			while ((line = br.readLine()) != null) {
				if (line.equals("")) continue;
				if (line.startsWith("#")) continue;
				
				String[] infos = line.trim().split("=");
				if (infos.length != 2) {
					System.out.println(">>> Notice : config file is not set.");
					System.out.println("Line Info : " + line);
					continue;
				}
				
				conf.put(infos[0].trim(), infos[1].trim());
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	
}
