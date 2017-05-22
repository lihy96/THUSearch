package index;

import java.io.*;
import java.util.*;


import org.wltea.analyzer.lucene.IKAnalyzer;

import lucene.SimpleSimilarity;
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
    private static int cc = 0;
	private static String indexDir, globalDir, srcDir;

    public static float averageLength=1.0f;
    public static float DIV_NUM = 10000.0f;
    
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
		    		cc ++;
					if(cc % 100==0){
						System.out.println("process "+cc);
					}
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
//		try {
//			String url, fileType;
//			File file;
//			url = "/home/cunxinshuihua/Desktop/homework/searchEngine/hw2/ImageSearch/1.test";
//			file = new File(url);
//			fileType = Files.probeContentType(file.toPath());
//			System.out.println(fileType);
//		}
//		catch (Exception e) {
//			e.printStackTrace();
//		}
		
		Map<String, String> confs = new HashMap<String, String>();
		ConfReader.confRead("conf/indexer.conf", confs);
		
		if ((indexDir = confs.get("IndexDir")) == null)
			indexDir = "forIndex/index";
		if ((globalDir = confs.get("GlobalDir")) == null)
			globalDir = "forIndex/global.txt";
		if ((srcDir = confs.get("SrcDir")) == null)
			srcDir = "../heritrix-1.14.4/jobs/news_tsinghua-20170513083441917/mirror/";
		
		THUIndexer indexer=new THUIndexer(indexDir);
		indexer.indexMirrorWebSites(srcDir);
		indexer.saveGlobals(globalDir);
		
	}
	
}
