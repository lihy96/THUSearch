package index;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import pagerank.PageRank;
import pagerank.WebSite;
import util.DirectoryChecker;
import util.FileOperator;

public class SimpleIndex {
	private String srcDir = null;
	private String outDir = null;
	private String desFile = null;
	private ArrayList<String> formatSupport = null;
	
	private int cc = 0;
	private int __id = 0;
	private Map<String, Integer> fileList = new HashMap<String, Integer>();

	public Map<Integer, ArrayList<Integer>> map = new HashMap<Integer, ArrayList<Integer>>();
	public Map<Integer, WebSite> webs = new LinkedHashMap<Integer, WebSite>();

	public void simpleIndex() {
		
		File df = null;
		if (desFile != null) {
			df = new File(desFile); 
		}
		
		if (df == null || !df.exists()) {
			this.loadFileListByIterator();
		}
		else
			this.loadFileListByHistory();


		/**
		 * calculate page rank for each html file
		 */
		File od = new File(outDir + "/pagerank.txt");
		if (!od.exists()) {
			this.loadLinkGraph(this.map, this.webs);
		}
		PageRank pr = new PageRank(0.15, 20, map, webs, outDir);
		pr.calPageRank();
	}

	/**
	 * total 16665 line
	 */
	public static void main(String[] args) {
		SimpleIndex fpi = new SimpleIndex();
		String src_dir = "../heritrix-1.14.4/jobs/news_tsinghua-20170513083441917/mirror/";
		String des_file = "forIndex/fileList.txt";
		ArrayList<String> fs = new ArrayList<String>();
		fs.add("html"); fs.add("htm"); fs.add("txt"); fs.add("xml");
		fs.add("doc"); fs.add("docx"); fs.add("pdf");
		fpi.setParam(src_dir, des_file, "forIndex", fs);
		fpi.simpleIndex();

		PageRank pr = new PageRank(0.15, 20, fpi.map, fpi.webs, "forIndex");
		pr.calPageRank();
		pr.saveInfo();
	}
	
	public Map<String, Integer> getFileList() {
		return fileList;
	}
	
	public void setParam(String src_dir, ArrayList<String> fs) {
		setParam(src_dir, null, null, fs);
	}
	
	public void setParam(String src_dir, String des_file, String out_dir, ArrayList<String> fs) {
		this.srcDir = src_dir;
		this.outDir = out_dir;
		this.desFile = des_file;
		this.formatSupport = fs;
	}
	
	private void loadFileListByHistory() {
		assert(desFile == null);
		
		try {
			BufferedReader br = new BufferedReader(new FileReader(desFile));
			String line;
			while ((line = br.readLine()) != null) {
				if (line.equals("")) continue;
				cc ++;
				if (cc % 100 == 0) {
					System.out.println("Process " + cc);
				}
				int idx = line.trim().indexOf("\t");
				Integer id = Integer.parseInt(line.trim().substring(0, idx));
				fileList.put(line.trim().substring(idx+1), id);
			}
			br.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void loadFileListByIterator() {
		assert(srcDir == null);
		assert(formatSupport == null);
		
		try{
			File websites = new File(srcDir);
			File[] wss = websites.listFiles();
			for(File website : wss) {
				indexSpecificWebsite(website);
			}
			
		}catch(Exception e){
			e.printStackTrace();
		}
		
		try {
			if (desFile != null) {
				DirectoryChecker.dirCheck(desFile);
				BufferedWriter bw = new BufferedWriter(new FileWriter(desFile));
				for (Entry<String, Integer> file : fileList.entrySet()) {
					bw.write(file.getValue() + "\t" + file.getKey() + "\n");
				}
				bw.close();
			}
		}
		catch (Exception e) {
			e.printStackTrace();
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

	    		// 文件太大, 大于100M, 删除, 减小存储压力
	    		double mBytes = file.length() / (1024 * 1024);
	    		if (mBytes > 100) {
	    			file.delete();
	    			continue;
	    		}
	    		
	    		int idx = file.getName().lastIndexOf(".");
	    		if (idx < 1) continue;
	    		
	    		String dotFile = file.getName().substring(idx+1);
	    		
	    		// 下载错误的文件，直接删除
	    		if (dotFile.equalsIgnoreCase("wmv")		||
	    			dotFile.equalsIgnoreCase("flv")) {
	    			// System.out.println(file.getAbsolutePath());
	    			file.delete();
	    			continue;
	    		}
				
	    		for (String format : formatSupport) {
	    			if (dotFile.equalsIgnoreCase(format)) {
	    				fileList.put(file.getPath(), __id++);
	    				
	    				cc ++;
						if(cc % 100==0){
							System.out.println("process "+cc);
						}
						break;
	    			}
	    		}
	    	}
		}catch(Exception e){
			e.printStackTrace();
		}
    }
    
    public void loadLinkGraph(	Map<Integer, ArrayList<Integer>> map,
								Map<Integer, WebSite> webs) {
    	int count = 0;
    	for (Entry<String, Integer> file : fileList.entrySet()) {
    		count ++;
    		if (count % 100 == 0) {
    			System.out.println("Load link graph : " + count);
    		}
    		
    		int idx = file.getKey().lastIndexOf(".");
    		String dotFile = file.getKey().substring(idx+1);

	    	ArrayList<Integer> outSiteNum = new ArrayList<Integer>();
    		//　检测每个HTML文档
    		if (dotFile.equalsIgnoreCase("html")) {
        		String content = FileOperator.readFile(file.getKey());
        		Document doc = Jsoup.parse(content);
        		Elements links = doc.select("a");
        		// 查找url
        		for (Element link : links) {
        			String url = link.absUrl("href");
        			if (url == null || url.equals("")) continue;
        			if (!url.matches(".*tsinghua\\.edu\\.cn.*")) continue;
        			try {
        				URI uri = new URI(url);
        				StringBuffer absPath = new StringBuffer(srcDir + uri.getHost() + uri.getPath());
        				File absFile = new File(absPath.toString());
        				if (absFile.isDirectory()){
        					if (!absPath.toString().endsWith("/"))
        						absPath.append("/");
        					absPath.append("index.html");
        				}
        				// System.out.println(absPath.toString());
        				Integer siteNum;
        				if ((siteNum = fileList.get(absPath.toString())) != null) {
        					outSiteNum.add(siteNum);
        					// System.out.println(absPath.toString() + " : " + siteNum);
        				}
        			}
        			catch (Exception e) {
        				e.printStackTrace();
        			}
//        			break;
        		}
    		}

    		// 添加至map, 和webs参数
    		ArrayList<Integer> value = map.get(file.getValue());
			if (value != null) {
				outSiteNum.addAll(value);
			}
			map.put(file.getValue(), outSiteNum);
			
			WebSite ws = new WebSite();
			ws.id = file.getValue();
			ws.name = file.getKey();
			ws.outdegree = outSiteNum.size();
			webs.put(file.getValue(), ws);
    	}
    	
    }
}