import java.io.*;
import java.util.*;


import org.w3c.dom.*;   
import org.wltea.analyzer.lucene.IKAnalyzer;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexReader.FieldOption;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.jsoup.Jsoup;
import org.jsoup.select.Elements;

import javax.xml.parsers.*; 

public class ImageIndexer {
	private Analyzer analyzer; 
    private IndexWriter indexWriter;
    private float averageLength=1.0f;
    
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
	
	/** 
	 * <p>
	 * index sogou.xml 
	 * 
	 */
	public void indexSpecialFile(String filename){
		try{
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();   
			DocumentBuilder db = dbf.newDocumentBuilder();    
			org.w3c.dom.Document doc = db.parse(new File(filename));
			NodeList nodeList = doc.getElementsByTagName("pic");
			for(int i=0;i<nodeList.getLength();i++){
				Node node=nodeList.item(i);
				NamedNodeMap map=node.getAttributes();
				Node locate=map.getNamedItem("locate");
				Node bigClass=map.getNamedItem("bigClass");
				Node smallClass=map.getNamedItem("smallClass");
				Node query=map.getNamedItem("query");
				String absString=bigClass.getNodeValue()+" "+smallClass.getNodeValue()+" "+query.getNodeValue();
				Document document  =   new  Document();
				Field PicPathField  =   new  Field( "picPath" ,locate.getNodeValue(),Field.Store.YES, Field.Index.NO);
				Field abstractField  =   new  Field( "abstract" ,absString,Field.Store.YES, Field.Index.ANALYZED);
				
				//TODO: add other fields such as html title or html content 
				// String currentDir = System.getProperty("user.dir");
			    // System.out.println("Current dir using System:" +currentDir);
				// String root = "/home/cunxinshuihua/workspace/.metadata/.plugins/org.eclipse.wst.server.core/tmp0/webapps/";
				// root是pictures/sogou文件夹的绝对路径，可自行配置
				String root = "/home/cunxinshuihua/tomcat8/webapps/";
				String picPath = root + locate.getNodeValue();
				// String content = ImageIndexer.readHTML(picPath, true);
				String htmlPath = picPath.replace(".jpg", ".html");
				String content = FileOperator.readFile(htmlPath);
				parseText(content, query.getNodeValue(), document, false);
				// parseText(content, query.getNodeValue(), document, false);
				
				averageLength += absString.length();
				document.add(PicPathField);
				document.add(abstractField);
				indexWriter.addDocument(document);
				if(i%10000==0){
					System.out.println("process "+i);
				}
				
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
		ImageIndexer indexer=new ImageIndexer("forIndex/index");
		indexer.indexSpecialFile("input/sogou-utf8.xml");
		indexer.saveGlobals("forIndex/global.txt");
	}
	
	public static String readHTML(String picPath, boolean debug) {
		String content = "";
		String htmlPath = picPath.replace(".jpg", ".html");
		
		try {
			if (debug) {
				System.out.println("Html Path : " + htmlPath);
			}
			
			InputStreamReader fr = new InputStreamReader(new FileInputStream(htmlPath), "gbk");
			BufferedReader br = new BufferedReader(fr);
			String line;
			while ((line = br.readLine()) != null) {
				content += line + "\n";
			}
		}
		catch (FileNotFoundException e) {
			System.out.println("File " + htmlPath + " can not find.");
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		return content;
	}
	
	public void parseText(String content, String query, Document document, boolean debug) {
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
					if (text.contains(query) || text.contains(title)) {
						relativeContent += text;
					}
				}
				averageLength += relativeContent.length();
			}
			
			if (debug) {
				System.out.println("***********DEBUG INFO***********");
				System.out.println("query : " + query);
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






















