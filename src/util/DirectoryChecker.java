package util;

import java.io.File;
import java.util.ArrayList;

public class DirectoryChecker {
	
	public static void dirCheck(ArrayList<String> paths) {
		for (String path : paths) {
			dirCheck(path);
		}
	}
	
	public static void dirCheck(String path) {
		dirCheck(path, false);
	}
	
	public static void dirCheck(String path, boolean isDir) {
		File file = new File(path);
		if (!file.getParentFile().exists()) {
			// System.out.println("Create parent directory : " + file.getParent());
			file.getParentFile().mkdirs();
		}

		if (!file.exists()) {
			String lastName = file.getName();
			if (isDir) {
				// is a file, do nothing
				// System.out.println("Create sub directory : " + lastName);
				file.mkdir();
			}
		}
	}
	
	public static void main(String[] args) {
		DirectoryChecker.dirCheck("./data/ECCE/data.txt");
	}
}
