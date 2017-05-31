package index;

import java.io.*;
import java.util.*;
import java.util.Map.Entry;

import org.wltea.analyzer.lucene.IKAnalyzer;

//import lucene.SimpleSimilarity;
import pagerank.PageRank;
import pagerank.WebSite;
import parser.CommonParser;
import parser.HTMLParser;
import util.ConfReader;
import util.DocReader;
import util.FileOperator;
import util.PDFReader;
import util.StaticValue;
import util.XMLReader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.jsoup.Jsoup;
import org.junit.experimental.theories.Theories;

import javax.xml.parsers.*; 

public class THUIndexer {
	private Analyzer analyzer; 
    private IndexWriter indexWriter;
    private static String outDir;
	private static String indexDir, globalPath, srcDir, fileListPath;

    public static float averageLength=1.0f;
    public static float DIV_NUM = 1000.0f;
    
	public THUIndexer(String indexDir){
    	analyzer = new IKAnalyzer();
//    	analyzer = new Analyzer();
    	try{
    		IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_47, analyzer);
    		// 覆盖源文件，而不是追加模式
    		iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
    		Directory dir = FSDirectory.open(new File(indexDir));
    		indexWriter = new IndexWriter(dir,iwc);
//    		indexWriter.setSimilarity(new SimpleSimilarity());
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
	
	
	public static void main(String[] args) {		
		Map<String, String> confs = new HashMap<String, String>();
		ConfReader.confRead("conf/indexer.conf", confs);
		
		if ((outDir = confs.get("output.dir")) == null)
			outDir = StaticValue.INDEX_DIR;
		indexDir = outDir + StaticValue.INDEX_DIR;
		globalPath = outDir + StaticValue.GLOBAL_PATH;
		fileListPath = outDir + StaticValue.FILE_LIST_PATH;
		
		if ((srcDir = confs.get("SrcDir")) == null)
			srcDir = "../heritrix-1.14.4/jobs/news_tsinghua-20170513083441917/mirror/";
		
		THUIndexer indexer=new THUIndexer(indexDir);
		/**
		 * page rank 
		 */
		SimpleIndex si = new SimpleIndex();
		ArrayList<String> fs = new ArrayList<String>();
		fs.add("html"); fs.add("htm"); fs.add("txt"); fs.add("xml");
		fs.add("doc"); fs.add("docx"); fs.add("pdf");
		si.setParam(srcDir, fileListPath, outDir, fs);
		si.simpleIndex();
		
		/**
		 * index websites for later search
		 */
		Map<String, Integer> fileList = si.getFileList();
		System.out.println("Index Document : " + fileList.size());
//		try {
//			indexer.indexDocuments(fileList, si.webs);
//		}
//		catch (Exception e) {
//			e.printStackTrace();
//		}
		
		indexer.dataMining(fileList);
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
			float pagerank = (float)Math.sqrt(webs.get(entry.getValue()).pagerank  * 100);

			System.setOut(dump);
//			System.setErr(dump);
    		if (dotFile.equalsIgnoreCase("html") ||
    			dotFile.equalsIgnoreCase("htm")) {
				content = FileOperator.readFile(file.getPath());
				HTMLParser.htmlParser(content, document, pagerank);
			}
    		else if (dotFile.equalsIgnoreCase("txt")) {
				content = FileOperator.readFile(file.getPath());
    			CommonParser.commParser(name, content, document, pagerank);
    		}
    		else if (dotFile.equalsIgnoreCase("xml")) {
    			content = XMLReader.readXMLFile(file.getPath());
    			CommonParser.commParser(name, content, document, pagerank);
    		}
    		else if (dotFile.equalsIgnoreCase("doc")) {
    			content = DocReader.readDocFile(file.getPath());
    			CommonParser.commParser(name, content, document, pagerank);
    		}
    		else if (dotFile.equalsIgnoreCase("docx")) {
    			content = DocReader.readDocxFile(file.getPath());
    			CommonParser.commParser(name, content, document, pagerank);
    		}
    		else if (dotFile.equalsIgnoreCase("pdf")) {
    			content = PDFReader.readPDFFile(file.getPath());
    			CommonParser.commParser(name, content, document, pagerank);
    		}
//    		System.setErr(err);
    		System.setOut(out);

    		if (!content.equals("")) {				
				String filePath = file.getPath().substring(srcDir.length());
				Field UrlPath = new StringField("urlPath", filePath, Field.Store.YES);
				document.add(UrlPath);
				indexWriter.addDocument(document);
    		}
		}
		
		averageLength /= indexWriter.numDocs();
		averageLength *= DIV_NUM;
		saveGlobals(globalPath);
		System.out.println("average length = "+averageLength);
		System.out.println("total "+indexWriter.numDocs()+" documents");
		indexWriter.close();


    }
	
	public void dataMining(Map<String, Integer> fileList) {
		String relaPath = outDir + StaticValue.SIM_WORD_PATH;
		String autocomPath = outDir + StaticValue.AUTO_COM_PATH;
		
		DataMining sw = new DataMining();
		int count = 0;
		IKAnalyzer ikAnalyzer = new IKAnalyzer();
		for (Entry<String, Integer> entry : fileList.entrySet()) {
			count ++;
			if (count % 100 == 0) {
				System.out.println("Token read : " + count);
			}
			
			if (entry.getKey().endsWith(".html") || 
				entry.getKey().endsWith(".htm")) {
				String content = FileOperator.readFile(entry.getKey());
				HTMLParser.tokenParser(sw, entry.getValue(), content, ikAnalyzer);
			}
		}

		File reFile = new File(relaPath);
		if (!reFile.exists()) {
			sw.init_sim_words();
			sw.save_sim_words(relaPath);
		}
		
		File acFile = new File(autocomPath);
		if (!acFile.exists()) {
			sw.save_autocom_word(autocomPath);
		}
	}

}
