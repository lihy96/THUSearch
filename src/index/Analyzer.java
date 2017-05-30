package index;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.spi.FileTypeDetector;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import util.DirectoryChecker;

public class Analyzer {
	private ArrayList<String> formatSupport = null;
	private int cc = 0;
	private int __id = 0;
	private Map<String, Integer> fileList = new HashMap<String, Integer>();
	private Map<String, Integer> typeList = new HashMap<String, Integer>();

	public static void main(String[] args) {
		ArrayList<String> fs = new ArrayList<String>();
		fs.add("html"); fs.add("txt"); fs.add("xml");
		fs.add("doc"); fs.add("docx"); fs.add("pdf");
		Analyzer an = new Analyzer();
		an.formatSupport = fs;
		String src_dir = "../heritrix-1.14.4/jobs/news_tsinghua-20170513083441917/mirror/";
		File file = new File(src_dir);
		an.indexSpecificWebsite(file);
		for (Entry<String, Integer> entry : an.typeList.entrySet()) {
			System.out.println(entry.getKey() + " : " + entry.getValue());
		}
	}
	
	private void indexSpecificWebsite(File website) {
    	try {
	    	File[] res = website.listFiles();
	    	for (File file : res) {
	    		if (file.isDirectory()) {
	    			indexSpecificWebsite(file);
	    			continue;
	    		}
	    		
	    		String type = Files.probeContentType(file.toPath());
	    		Integer count = typeList.get(type);
	    		if (count != null) typeList.put(type, ++count);
	    		else typeList.put(type, 1);
	    		
				cc ++;
				if(cc % 100==0){
					System.out.println("process "+cc);
				}
	    		// 文件太大, 大于100M, 删除, 减小存储压力
//	    		double mBytes = file.length() / (1024 * 1024);
//	    		if (mBytes > 100) {
//	    			file.delete();
//	    			continue;
//	    		}
	    		
//	    		int idx = file.getName().lastIndexOf(".");
//	    		if (idx < 1) continue;
//	    		String dotFile = file.getName().substring(idx+1);
//				
//	    		for (String format : formatSupport) {
//	    			if (dotFile.equalsIgnoreCase(format)) {
//	    				fileList.put(file.getPath(), __id++);
//						break;
//	    			}
//	    		}
	    	}
		}catch(Exception e){
			e.printStackTrace();
		}
    }
}