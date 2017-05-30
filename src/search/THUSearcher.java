package search;

import java.io.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DocsEnum;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.Weight;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.wltea.analyzer.lucene.IKAnalyzer;

import com.sun.xml.internal.ws.encoding.SwACodec;

import index.SimilarWords;


public class THUSearcher {
	private IndexReader reader;
	private IndexSearcher searcher;
	private Analyzer analyzer;
	private float avgLength=1.0f;
	

	private String[] fields = new String[] {"title", "keywords", "content", "link"};
	private Map<String, Float> boosts = new HashMap<String, Float>();
	
	@SuppressWarnings("deprecation")
	public THUSearcher(String indexdir){
		analyzer = new IKAnalyzer();
		try{
			boosts.put("title", 100.0f);
			boosts.put("keyword", 10.0f);
			boosts.put("content", 5.0f);    
			boosts.put("link", 1.0f);
			
			System.out.println(System.getProperty("user.dir"));
			reader = IndexReader.open(FSDirectory.open(new File(indexdir)));
			searcher = new IndexSearcher(reader);
			searcher.setSimilarity(new BM25Similarity());
	
//			System.out.println("Intialize Similarword.");
//			TermsEnum termEnum = MultiFields.getTerms(reader, "content").iterator(null);
//			int cc = 0;
//			while (termEnum.next() != null) {
//				cc ++;
//				if (cc % 10000 == 0) System.out.println("sim word : " + cc);
//				
//				DocsEnum docEnum = MultiFields.getTermDocsEnum(reader, MultiFields.getLiveDocs(reader), "content", termEnum.term());
//				int doc;
//				while((doc = docEnum.nextDoc())!= DocsEnum.NO_MORE_DOCS ){
//					SimilarWords.add(termEnum.term().utf8ToString(), docEnum.freq(), doc);
//				}
//			}
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	
	public TopDocs searchQuery(String queryString, int maxnum) {
		try {
			QueryParser parser = new MultiFieldQueryParser(Version.LUCENE_47, fields, analyzer, boosts);
			Query query = parser.parse(queryString);
			TopDocs results = searcher.search(query, maxnum);
			return results;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public TopDocs searchQuery(String queryString,String field,int maxnum){
		try {
			Term term=new Term(field,queryString);
			QueryParser parser = new QueryParser(Version.LUCENE_47, field, analyzer);
			Query query = parser.parse(queryString);
			query.setBoost(1.0f);
			TopDocs results = searcher.search(query, maxnum);
			return results;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public Document getDoc(int docID){
		try{
			return searcher.doc(docID);
		}catch(IOException e){
			e.printStackTrace();
		}
		return null;
	}
	
	public void loadGlobals(String filename){
		try{
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(filename)));
			String line=reader.readLine();
			avgLength=Float.parseFloat(line);
			reader.close();
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	
	public float getAvg(){
		return avgLength;
	}
	
	public static void main(String[] args){
		THUSearcher search=new THUSearcher("forIndex/index");
		search.loadGlobals("forIndex/global.txt");
		System.out.println("avg length = "+search.getAvg());
		
		TopDocs results=search.searchQuery("�����", "abstract", 100);
		ScoreDoc[] hits = results.scoreDocs;
		for (int i = 0; i < hits.length; i++) { // output raw format
			Document doc = search.getDoc(hits[i].doc);
			System.out.println("doc=" + hits[i].doc + " score="
					+ hits[i].score+" picPath= "+doc.get("picPath"));
		}
	}
}
