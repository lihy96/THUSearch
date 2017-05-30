package search;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.ansj.splitWord.analysis.ToAnalysis;
import org.ansj.util.MyStaticValue;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
//import org.apache.lucene.queryParser.MultiFieldQueryParser;
//import org.apache.lucene.queryParser.ParseException;
//import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.util.Version;
import org.apache.xmlbeans.impl.xb.xsdschema.Element;
import org.wltea.analyzer.lucene.IKAnalyzer;

import index.SimilarWords;

import java.util.*;

import java.math.*;
import java.net.*;
import java.io.*;


public class THUServer extends HttpServlet{
	public static final int PAGE_RESULT=10;
	public static final String indexDir="forIndex";
	public static final String htmlDir="http://";
	private SimilarWords sw = new SimilarWords();
	private THUSearcher search=null;
	public THUServer(){
		super();
		search=new THUSearcher(new String(indexDir+"/index"));
		search.loadGlobals(new String(indexDir+"/global.txt"));
		sw.load(indexDir+"/relation.txt");
	}
	
	public ScoreDoc[] showList(ScoreDoc[] results,int page){
		if(results==null || results.length<(page-1)*PAGE_RESULT){
			return null;
		}
		int start=Math.max((page-1)*PAGE_RESULT, 0);
		int docnum=Math.min(results.length-start,PAGE_RESULT);
		ScoreDoc[] ret=new ScoreDoc[docnum];
		for(int i=0;i<docnum;i++){
			ret[i]=results[start+i];
		}
		return ret;
	}
	
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		System.out.println("**************Search Begin*****************");
		response.setContentType("text/html;charset=utf-8");
		request.setCharacterEncoding("utf-8");
		String queryString=request.getParameter("query");
		String pageString=request.getParameter("page");
		int page=1;
		if(pageString!=null){
			page=Integer.parseInt(pageString);
		}
		if(queryString==null){
			System.out.println("null query");
			//request.getRequestDispatcher("/Image.jsp").forward(request, response);
		}else{			
			//分词
			// IKAnalyzer ika = new IKAnalyzer(false);
			ArrayList<ScoreDoc> hits = new ArrayList<ScoreDoc>();
			
			/**
			 * ansj_seg word-spliter package
			 */
			PrintStream out = System.out;
			System.setOut(new PrintStream("/dev/null"));
			MyStaticValue.isRealName = true;
			MyStaticValue.isNameRecognition = true;
			MyStaticValue.isNumRecognition = true;
			MyStaticValue.isQuantifierRecognition = true;
			org.ansj.domain.Result queryWords = ToAnalysis.parse(queryString);
			System.setOut(out);
			
			ArrayList<String> simWords = new ArrayList<String>();
			System.out.print("ansj result : ");
			for (org.ansj.domain.Term word : queryWords) {
				if (word.getName().matches(" *")) continue;
				System.out.print("<" + word.getName() + ">");
				simWords.addAll(sw.find(word.getName(), 5));
//				ScoreDoc[] tmpHits = getHits(word.getName(), page);
				TopDocs td = search.searchQuery(word.getName(), 100);
	            addHits(hits, td.scoreDocs, 1.0f);
			}
			System.out.println("");
			
			/**
			 * IKAnalyzer split word package
			 */
//			QueryParser queryParser = new QueryParser(Version.LUCENE_35, "content", new IKAnalyzer(false));
//			Query queryClass;
//			try {
//				queryClass = queryParser.parse(queryString);
//				// System.out.println(queryClass);
//				Set<Term> terms = new HashSet<Term>();
//				queryClass.extractTerms(terms);
//				System.out.print("IKAnalyzer result : ");
//				for (Iterator<Term> iter = terms.iterator(); iter.hasNext(); ) {
//					Term term = iter.next();
//					String query = term.text().trim();
//					System.out.print("<" + query + ">");
//					ScoreDoc[] tmpHits = getHits(query, page);
//		            addHits(hits, tmpHits, 1);
//				}
//				System.out.println("");
//			} catch (ParseException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
	        
			/*for (int i = 0; i < hits.size(); ++i) {
				Document doc = search.getDoc(hits.get(i).doc);
				System.out.println("doc=" + hits.get(i).doc + " score="
					+ hits.get(i).score + " picPath= "
					+ doc.get("picPath")+ " tag= "+doc.get("abstract"));
			}*/
			
	        setRequestAttribute(request, response, hits, simWords, queryString, page);
			
		}
	}
	
	@SuppressWarnings("unused")
	private ScoreDoc[] getHits(String queryString, int page) {
		ScoreDoc[] hits = null;

		ArrayList<ScoreDoc> moreHits = new ArrayList<ScoreDoc>();
		TopDocs linkRes = search.searchQuery(queryString, "link", 100);
		if (linkRes != null) {
			addHits(moreHits, linkRes.scoreDocs, 1.0f);
		}
		TopDocs contentRes = search.searchQuery(queryString, "content", 100);
		if (contentRes != null) {
			addHits(moreHits, contentRes.scoreDocs, 5.0f);
		}
		TopDocs keywordsRes = search.searchQuery(queryString, "keywords", 100);
		if (keywordsRes != null) {
			addHits(moreHits, keywordsRes.scoreDocs, 10.0f);
		}
		TopDocs titleRes = search.searchQuery(queryString, "title", 100);
		if (titleRes != null) {
			addHits(moreHits, titleRes.scoreDocs, 100.0f);
		}
		
		if (moreHits.size() != 0) {  
			hits = moreHits.toArray(new ScoreDoc[moreHits.size()]);
		}
		else {
			System.out.println("relative search null");
		}
		
		return hits;
	}
	
	private void setRequestAttribute(HttpServletRequest request, HttpServletResponse response,
					ArrayList<ScoreDoc> hits, ArrayList<String> _simWords, String queryString, int page) {
		String[] tags=null;
		String[] paths=null;
		String[] absContent=null;
		String[] imgPaths = null;
		String[] simWords = null;
		simWords = _simWords.toArray(new String[_simWords.size()]);
		for (String simword : simWords)
			System.out.print(simword + ", ");
		System.out.println("");
		
		if (hits.size() != 0) {
			Collections.sort(hits, new ScoreComparator());
			ScoreDoc[] htmls = showList(hits.toArray(new ScoreDoc[hits.size()]), page);
			if (htmls == null) {
				htmls = new ScoreDoc[0];
			}
			tags = new String[htmls.length];
			paths = new String[htmls.length];
			absContent = new String[htmls.length];
			imgPaths = new String[htmls.length];
			getTagsAndPaths(tags, paths, absContent, htmls, search);
		}
		else {
			System.out.println("Result Null");
		}
		
		try {
			request.setAttribute("currentQuery",queryString);
			request.setAttribute("currentPage", page);
			request.setAttribute("htmlTags", tags);
			request.setAttribute("htmlPaths", paths);
			request.setAttribute("imgPaths", imgPaths);
			request.setAttribute("absContent", absContent);
			request.getRequestDispatcher("/thushow.jsp").forward(request,
					response);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		this.doGet(request, response);
	}
	
	private void addHits(ArrayList<ScoreDoc> moreHits, ScoreDoc[] hits, float boost) {
		if (hits == null) return ;
		for (int i = 0; i < hits.length; ++i) {
/*			Document doc = search.getDoc(hits[i].doc);
			System.out.println("doc=" + hits[i].doc + " score="
				+ hits[i].score + " picPath= "
				+ doc.get("picPath")+ " tag= "+doc.get("abstract"));*/
			int flag = matchHits(hits[i], moreHits);
			if (flag != -1) {
				moreHits.get(flag).score += hits[i].score * boost;
			}
			else {
				moreHits.add(hits[i]);
			}
		}
	}
	
	private int matchHits(ScoreDoc hit, ArrayList<ScoreDoc> moreHits) {
		// Document doc = search.getDoc(hit.doc);
		Iterator<ScoreDoc> iter = moreHits.iterator();
		int match = -1;
		while (iter.hasNext()) {
			match ++;
			ScoreDoc ele = iter.next();
			if (ele.doc == hit.doc) {
				return match;
			}
		}
		return -1;
	}
	
	private void getTagsAndPaths(String[] tags, String[] paths, String[] absContent, ScoreDoc[] hits, THUSearcher search) {
		for (int i = 0; i < hits.length && i < PAGE_RESULT; i++) {
			Document doc = search.getDoc(hits[i].doc);
			System.out.println("doc=" + hits[i].doc + " score="
					+ hits[i].score + " title= "+doc.get("title"));
			tags[i] = doc.get("title");
			String absHtmlPath = htmlDir + doc.get("urlPath");
			if (absHtmlPath.endsWith("index.html")) {
				absHtmlPath = absHtmlPath.substring(0, absHtmlPath.length() - 10);
			}
			paths[i] = absHtmlPath;
			String content = doc.get("content");
			if (content.length() < 300)
				absContent[i] = doc.get("content");
			else
				absContent[i] = doc.get("content").substring(0, 300) + "...";
		}
	}
	
}
