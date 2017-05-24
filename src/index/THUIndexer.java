package index;

import java.io.*;
import java.util.*;
import java.util.Map.Entry;

import org.wltea.analyzer.lucene.IKAnalyzer;

import lucene.SimpleSimilarity;
import pagerank.PageRank;
import pagerank.WebSite;
import parser.CommonParser;
import parser.HTMLParser;
import util.ConfReader;
import util.DocReader;
import util.FileOperator;
import util.PDFReader;
import util.XMLReader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.jsoup.Jsoup;

import javax.xml.parsers.*; 

public class THUIndexer {
	private Analyzer analyzer; 
    private IndexWriter indexWriter;
//    private static int cc = 0;
	private static String indexDir, globalDir, srcDir;

    public static float averageLength=1.0f;
    public static float DIV_NUM = 1000.0f;
    
    @SuppressWarnings("deprecation")
	public THUIndexer(String indexDir){
    	analyzer = new IKAnalyzer();
    	try{
    		IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_35, analyzer);
    		// 覆盖源文件，而不是追加模式
    		iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
    		Directory dir = FSDirectory.open(new File(indexDir));
    		indexWriter = new IndexWriter(dir,iwc);
    		indexWriter.setSimilarity(new SimpleSimilarity());
    	}catch(IOException e){
    		e.printStackTrace();
    	}
    }
    
    
    public void saveGlobals(String filename){
    	try{
    		PrintWriter pw=new PrintWriter(new File(filename));
    		pw.println(averageLength);
    		pw.close();
    	}catch(IOException e){
    		e.printStackTrace();
    	}
    }
	
    public void indexSpecificWebsite(File website) {
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
	    		String name = file.getName().substring(0, idx);
	    		
	    		// 下载错误的文件，直接删除
	    		if (dotFile.equalsIgnoreCase("wmv")		||
	    			dotFile.equalsIgnoreCase("flv")) {
	    			System.out.println(file.getAbsolutePath());
	    			file.delete();
	    			continue;
	    		}
	    		
	    		String content = "";
				Document document = new Document();
				
				/**
				 *  对不用格式文档进行解析，目前支持如下格式：
				 *  	html, txt, xml,
				 *  	doc, docx, pdf
				 */
	    		if (dotFile.equalsIgnoreCase("html")) {
					content = FileOperator.readFile(file.getAbsolutePath());
					HTMLParser.htmlParser(content, document, false);
				}
	    		else if (dotFile.equalsIgnoreCase("txt")) {
					content = FileOperator.readFile(file.getAbsolutePath());
	    			CommonParser.commParser(name, content, document);
	    		}
	    		else if (dotFile.equalsIgnoreCase("xml")) {
	    			content = XMLReader.readXMLFile(file.getAbsolutePath());
	    			CommonParser.commParser(name, content, document);
	    		}
	    		else if (dotFile.equalsIgnoreCase("doc")) {
	    			content = DocReader.readDocFile(file.getAbsolutePath());
	    			CommonParser.commParser(name, content, document);
	    		}
	    		else if (dotFile.equalsIgnoreCase("docx")) {
	    			content = DocReader.readDocxFile(file.getAbsolutePath());
	    			CommonParser.commParser(name, content, document);
	    		}
	    		else if (dotFile.equalsIgnoreCase("pdf")) {
	    			content = PDFReader.readPDFFile(file.getAbsolutePath());
	    			CommonParser.commParser(name, content, document);
	    		}

	    		if (!content.equals("")) {
//		    		cc ++;
//					if(cc % 100==0){
//						System.out.println("process "+cc);
//					}
					
					String filePath = file.getPath().substring(srcDir.length());
					Field UrlPath = new Field("urlPath", filePath, 
													Field.Store.YES, Field.Index.NO);
					document.add(UrlPath);
					indexWriter.addDocument(document);
	    		}
	    	}
		}catch(Exception e){
			e.printStackTrace();
		}
    }

	public void indexMirrorWebSites(String srcDir){
		try{
			File websites = new File(srcDir);
			File[] wss = websites.listFiles();
			for(File website : wss) {
				indexSpecificWebsite(website);
			}
			
			averageLength /= indexWriter.numDocs();
			System.out.println("average length = "+averageLength);
			System.out.println("total "+indexWriter.numDocs()+" documents");
			indexWriter.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {		
		Map<String, String> confs = new HashMap<String, String>();
		ConfReader.confRead("conf/indexer.conf", confs);
		
		if ((indexDir = confs.get("IndexDir")) == null)
			indexDir = "forIndex/index";
		if ((globalDir = confs.get("GlobalDir")) == null)
			globalDir = "forIndex/global.txt";
		if ((srcDir = confs.get("SrcDir")) == null)
			srcDir = "../heritrix-1.14.4/jobs/news_tsinghua-20170513083441917/mirror/";
		
		THUIndexer indexer=new THUIndexer(indexDir);
		
		/**
		 * page rank preparation
		 */
		SimpleIndex si = new SimpleIndex();
		ArrayList<String> fs = new ArrayList<String>();
		fs.add("html"); fs.add("txt"); fs.add("xml");
		fs.add("doc"); fs.add("docx"); fs.add("pdf");
		si.setParam(srcDir, fs);
		si.simpleIndex();

		/**
		 * calculate page rank for each html file
		 */
		PageRank pr = new PageRank(0.15, 20, si.map, si.webs);
		pr.calPageRank();
		pr.sort(); 
		pr.saveInfo();
		
		/**
		 * index websites for later search
		 */
		Map<String, Integer> fileList = si.getFileList();
		try {
			indexer.indexDocuments(fileList, si.webs);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
//		pr.saveInfo();
//		indexer.indexMirrorWebSites(srcDir);
		
		indexer.saveGlobals(globalDir);
	}
	
	/**
	 * Set page rank as boost in lucene
	 * @param fileList
	 */
	public void indexDocuments(Map<String, Integer> fileList, Map<Integer, WebSite> webs) 
				throws Exception {
		PrintStream out = System.out;
		PrintStream err = System.err;
		PrintStream dump = new PrintStream(new FileOutputStream("/dev/null"));
		int count = 0;
		for (Entry<String, Integer> entry : fileList.entrySet()) {
			count ++;
			if (count % 100 == 0) {
				System.out.println("Index document : " + count);
			}
			
			File file = new File(entry.getKey());
			int idx = file.getName().lastIndexOf(".");
    		String dotFile = file.getName().substring(idx+1);
    		String name = file.getName().substring(0, idx);
    		
    		String content = "";
			Document document = new Document();
			double pagerank = webs.get(entry.getValue()).pagerank * 10;
			document.setBoost((float)pagerank);

			/**
			 *  对不用格式文档进行解析，目前支持如下格式：
			 *  	html, txt, xml,
			 *  	doc, docx, pdf
			 */
			System.setErr(dump);
    		if (dotFile.equalsIgnoreCase("html")) {
				content = FileOperator.readFile(file.getPath());
				HTMLParser.htmlParser(content, document, false);
			}
    		else if (dotFile.equalsIgnoreCase("txt")) {
				content = FileOperator.readFile(file.getPath());
    			CommonParser.commParser(name, content, document);
    		}
    		else if (dotFile.equalsIgnoreCase("xml")) {
    			content = XMLReader.readXMLFile(file.getPath());
    			CommonParser.commParser(name, content, document);
    		}
    		else if (dotFile.equalsIgnoreCase("doc")) {
    			content = DocReader.readDocFile(file.getPath());
    			CommonParser.commParser(name, content, document);
    		}
    		else if (dotFile.equalsIgnoreCase("docx")) {
    			content = DocReader.readDocxFile(file.getPath());
    			CommonParser.commParser(name, content, document);
    		}
    		else if (dotFile.equalsIgnoreCase("pdf")) {
    			content = PDFReader.readPDFFile(file.getPath());
    			CommonParser.commParser(name, content, document);
    		}
    		System.setErr(err);

    		if (!content.equals("")) {				
				String filePath = file.getPath().substring(srcDir.length());
				Field UrlPath = new Field("urlPath", filePath, 
												Field.Store.YES, Field.Index.NO);
				document.add(UrlPath);
				indexWriter.addDocument(document);
    		}
		}
		
		averageLength /= indexWriter.numDocs();
		averageLength *= DIV_NUM;
		System.out.println("average length = "+averageLength);
		System.out.println("total "+indexWriter.numDocs()+" documents");
		indexWriter.close();
    }
}
