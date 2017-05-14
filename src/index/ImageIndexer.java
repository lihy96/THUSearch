package index;

import java.io.*;
import java.util.*;


import org.wltea.analyzer.lucene.IKAnalyzer;

import lucene.SimpleSimilarity;
import util.ConfReader;
import util.FileOperator;

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

public class ImageIndexer {
	private Analyzer analyzer; 
    private IndexWriter indexWriter;
    private float averageLength=1.0f;
    private static int cc = 0;
    
    @SuppressWarnings("deprecation")
	public ImageIndexer(String indexDir){
    	analyzer = new IKAnalyzer();
    	try{
    		IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_35, analyzer);
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
	    		
	    		int idx = file.getName().lastIndexOf(".");
	    		String dotFile = "";
	    		if (idx != -1) {
	    			dotFile = file.getName().substring(idx+1);
	    		}
	    		if (dotFile.equalsIgnoreCase("wmv")		||
	    			dotFile.equalsIgnoreCase("flv")) {
	    			file.delete();
	    		}
	    		
	    		if (dotFile.equalsIgnoreCase("html")  	|| 
	    			dotFile.equalsIgnoreCase("txt")  	|| 
	    			dotFile.equalsIgnoreCase("xml")) {
//					Document document  =   new  Document();
//					String content = FileOperator.readFile(file.getAbsolutePath());
//					parseText(content, document, false);
//					indexWriter.addDocument(document);
		    		cc ++;
					if(cc % 100==0){
						System.out.println("process "+cc);
					}
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
		String indexDir, globalDir, srcDir;
		
		if ((indexDir = confs.get("IndexDir")) == null)
			indexDir = "forIndex/index";
		if ((globalDir = confs.get("GlobalDir")) == null)
			globalDir = "forIndex/global.txt";
		if ((srcDir = confs.get("SrcDir")) == null)
			srcDir = "../heritrix-1.14.4/jobs/news_tsinghua-20170513083441917/mirror/";
		
		ImageIndexer indexer=new ImageIndexer(indexDir);
		indexer.indexMirrorWebSites(srcDir);
		indexer.saveGlobals(globalDir);
		
	}
	
	public void parseText(String content, Document document, boolean debug) {
		try {
			org.jsoup.nodes.Document doc = Jsoup.parse(content);
			org.jsoup.nodes.Element titleE = doc.select("title").first();
			org.jsoup.nodes.Element keywordsE = doc.select("meta[name=\"keywords\"]").first();
			org.jsoup.nodes.Element descriptionE = doc.select("meta[name=\"description\"]").first();
			org.jsoup.nodes.Element bodyE = doc.select("body").first();
			
			String title = "";
			if (titleE!=null && titleE.hasText()) {
				title = titleE.text();
				averageLength += title.length();
			}
			String keywords = "";
			if (keywordsE!=null && keywordsE.hasAttr("content")) {
				keywords = keywordsE.attr("content");
				averageLength += keywords.length();
			}
			String description = "";
			if (descriptionE!=null && descriptionE.hasAttr("content")) {
				description = descriptionE.attr("content");
				averageLength += description.length();
			}
			
			String relativeContent = "";
			if (bodyE!=null) {
				for (org.jsoup.nodes.Element ele : bodyE.children()) {
					String text = ele.text();
					relativeContent += text;
				}
				averageLength += relativeContent.length();
			}
			
			if (debug) {
				System.out.println("***********DEBUG INFO***********");
				System.out.println("title : " + title + "\n" + 
								"keywords : " + keywords + "\n" + 
								"description : " + description + "\n" + 
								"relativeContent : " + relativeContent);
			}
			
			Field titleField = new Field("title", title, Field.Store.YES, Field.Index.ANALYZED);
			Field keywordsField = new Field("keywords", keywords, Field.Store.YES, Field.Index.ANALYZED);
			Field descriptionField = new Field("description", description, Field.Store.YES, Field.Index.ANALYZED);
			Field contentField = new Field("content", relativeContent, Field.Store.YES, Field.Index.ANALYZED);
			document.add(titleField);
			document.add(keywordsField);
			document.add(descriptionField);
			document.add(contentField);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}






















