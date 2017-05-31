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
import org.json.JSONArray;
import org.json.JSONObject;
import org.wltea.analyzer.lucene.IKAnalyzer;

import index.DataMining;
import sun.swing.StringUIClientPropertyKey;

import java.util.*;

import java.math.*;
import java.net.*;
import java.io.*;


public class THUServer extends HttpServlet{
	public static final int PAGE_RESULT=10;
	public static final String indexDir="forIndex";
	public static final String htmlDir="http://";
	private DataMining sw = new DataMining();
	private THUSearcher search=null;
	public THUServer(){
		super();
		search=new THUSearcher(new String(indexDir+"/index"));
		search.loadGlobals(new String(indexDir+"/global.txt"));
		sw.load_words(indexDir+"/relation.txt", indexDir+"/autocom.txt");
	}
	
//	public class AutoComplete extends HttpServlet {
//		public void doGet(HttpServletRequest request, HttpServletResponse response) 
//				throws ServletException, IOException{
//			response.setContentType("text/html;charset=utf-8");
//			request.setCharacterEncoding("utf-8");
//			String word=request.getParameter("query");
//			ArrayList<String> completeWords = sw.find_autocom_words(word, 5);
//			String[] recommendWords = completeWords.toArray(new String[completeWords.size()]);
//			
//			try {
//				request.setAttribute("words",recommendWords);
//				PrintWriter out = response.getWriter();
//				for (String string : recommendWords) {
//					out.println(string);
//				}
////				request.getRequestDispatcher("/thushow.jsp").forward(request,
////						response);
//			}
//			catch (Exception e) {
//				e.printStackTrace();
//			}
//		}
//
//		public void doPost(HttpServletRequest request, HttpServletResponse response)
//				throws ServletException, IOException {
//			this.doGet(request, response);
//		}
//	}
	
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
		response.setContentType("text/html;charset=utf-8");
		request.setCharacterEncoding("utf-8");
		
		String autoComWord=request.getParameter("autocomplete");
		if (autoComWord != null) {
			PrintWriter out = response.getWriter();
			ArrayList<String> completeWords = sw.find_autocom_words(autoComWord, 5);
			JSONArray jsonArray = new JSONArray();
			String[] recommendWords = completeWords.toArray(new String[completeWords.size()]);
			for (String string : recommendWords) {
				jsonArray.put(string);
			}
			out.println(jsonArray.toString());
			return ;
		}
				
		queryWebSearch(request, response);
	}
	
	private void queryWebSearch(HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, IOException {
		System.out.println("**************Search Begin*****************");
		String queryString=request.getParameter("query");
		String pageString=request.getParameter("page");

		int page=1;
		if(pageString!=null){
			page=Integer.parseInt(pageString);
		}
		if(queryString==null){
			System.out.println("null query");
		}else{	
			//分词
			// IKAnalyzer ika = new IKAnalyzer(false);
			ArrayList<ScoreDoc> hits = new ArrayList<ScoreDoc>();
			
			/**
			 * ansj_seg word-spliter package
			 */
			PrintStream out = System.out;
			System.setOut(new PrintStream("err.log"));
			MyStaticValue.isRealName = true;
			MyStaticValue.isNameRecognition = true;
			MyStaticValue.isNumRecognition = true;
			MyStaticValue.isQuantifierRecognition = true;
			org.ansj.domain.Result queryWords = ToAnalysis.parse(queryString);
			System.setOut(out);
			
			Map<Integer, ArrayList<String>> mutiSimWords = new HashMap<Integer, ArrayList<String>>();
			Map<Integer, ArrayList<String>> mutiCorrWords = new HashMap<Integer, ArrayList<String>>();
			System.out.print("ansj result : ");
			int id = 0;
			for (org.ansj.domain.Term word : queryWords) {
				if (word.getName().matches(" *")) continue;
				System.out.print("<" + word.getName() + ">");
	//			simWords.addAll(sw.find(word.getName(), 5));
				mutiSimWords.put(id, sw.find_sim_words(word.getName(), 5));
				mutiCorrWords.put(id++, sw.find_correct_words(word.getName(), 4));
	//			ScoreDoc[] tmpHits = getHits(word.getName(), page);
				TopDocs td = search.searchQuery(word.getName(), 100);
	            addHits(hits, td.scoreDocs, 1.0f);
			}
			System.out.println("");
			ArrayList<String> simList = new ArrayList<String>();
			for (int i = 0; i < 10; ++i) {
				ArrayList<String> words = mutiSimWords.get(i % id);
				if (words.size() > (i / id)) {
					simList.add(words.get(i / id));
				}
			}
			ArrayList<String> corrList = new ArrayList<String>();
			for (int i = 0; i < 4; ++i) {
				ArrayList<String> words = mutiCorrWords.get(i % id);
				if (words.size() > (i / id)) {
					corrList.add(words.get(i / id));
				}
			}
			
			/**
			 * IKAnalyzer split word package
			 */
	//		QueryParser queryParser = new QueryParser(Version.LUCENE_35, "content", new IKAnalyzer(false));
	//		Query queryClass;
	//		try {
	//			queryClass = queryParser.parse(queryString);
	//			// System.out.println(queryClass);
	//			Set<Term> terms = new HashSet<Term>();
	//			queryClass.extractTerms(terms);
	//			System.out.print("IKAnalyzer result : ");
	//			for (Iterator<Term> iter = terms.iterator(); iter.hasNext(); ) {
	//				Term term = iter.next();
	//				String query = term.text().trim();
	//				System.out.print("<" + query + ">");
	//				ScoreDoc[] tmpHits = getHits(query, page);
	//	            addHits(hits, tmpHits, 1);
	//			}
	//			System.out.println("");
	//		} catch (ParseException e) {
	//			// TODO Auto-generated catch block
	//			e.printStackTrace();
	//		}
			
	        setRequestAttribute(request, response, hits, simList, corrList, queryString, page);

		}
	}
	
	private void setRequestAttribute(HttpServletRequest request, HttpServletResponse response,
				ArrayList<ScoreDoc> hits, ArrayList<String> _simWords, ArrayList<String> _corrList,
				String queryString, int page) {
		String[] tags=null;
		String[] paths=null;
		String[] absContent=null;
		String[] imgPaths = null;

		String[] autoComplete=null;
		String[] recommendWords=_simWords.toArray(new String[_simWords.size()]);
		String[] spellCheckWords=_corrList.toArray(new String[_corrList.size()]);
		
		if (hits.size() != 0) {
//			Collections.sort(hits, new ScoreComparator());
			Collections.sort(hits, new Comparator<ScoreDoc>() {
				public int compare(ScoreDoc s1, ScoreDoc s2) {
					if (s1.score > s2.score) 
						return -1;
					else if (s1.score < s2.score)
						return 1;
					return 0;
			   }
			});
			ScoreDoc[] htmls = showList(hits.toArray(new ScoreDoc[hits.size()]), page);
			if (htmls == null) {
				htmls = new ScoreDoc[0];
			}
			tags = new String[htmls.length];
			paths = new String[htmls.length];
			absContent = new String[htmls.length];
			imgPaths = new String[htmls.length];
			autoComplete = new String[htmls.length];
//			spellCheckWords = new String[htmls.length];
			
			// lihy96's temp code for testing
//			imgPaths[0] = "main2.png";
//			imgPaths[1] = "bj2.jpeg";
			autoComplete[0] = "buquan 1";
			autoComplete[1] = "buquan 2";
			autoComplete[2] = "buquan 3";
//			spellCheckWords[0] = "spell1";
//			spellCheckWords[1] = "spell22";
			//end of lihy96's code 

			
			getTagsAndPaths(tags, paths, absContent, imgPaths, htmls, search);
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
			request.setAttribute("autoComplete", autoComplete);
			request.setAttribute("absContent", absContent);
			request.setAttribute("recommendWords", recommendWords);
			request.setAttribute("spellCheckWords", spellCheckWords);
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
	
	private void getTagsAndPaths(String[] tags, String[] paths, 
						String[] absContent, String[] imgurls,
						ScoreDoc[] hits, THUSearcher search) {
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
			String img = doc.get("imgurl");
			int snippet = (img == null) ? 257 : 157;
			absContent[i] = content.substring(0, Math.min(content.length(), snippet));
			imgurls[i] = img;
		}
	}
	
}
