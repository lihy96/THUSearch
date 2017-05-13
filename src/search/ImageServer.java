import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.util.Version;
import org.wltea.analyzer.lucene.IKAnalyzer;

import java.util.*;

import java.math.*;
import java.net.*;
import java.io.*;


public class ImageServer extends HttpServlet{
	public static final int PAGE_RESULT=10;
	public static final String indexDir="forIndex";
	public static final String picDir="";
	private ImageSearcher search=null;
	public ImageServer(){
		super();
		search=new ImageSearcher(new String(indexDir+"/index"));
		search.loadGlobals(new String(indexDir+"/global.txt"));
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
			/*System.out.println(queryString);
			System.out.println(URLDecoder.decode(queryString,"utf-8"));
			System.out.println(URLDecoder.decode(queryString,"gb2312"));*/
			
			//分词
			// IKAnalyzer ika = new IKAnalyzer(false);
			ArrayList<ScoreDoc> hits = new ArrayList<ScoreDoc>();
			QueryParser queryParser = new QueryParser(Version.LUCENE_35, "content", new IKAnalyzer(false));
			Query queryClass;
			try {
				queryClass = queryParser.parse(queryString);
				// System.out.println(queryClass);
				Set<Term> terms = new HashSet<Term>();
				queryClass.extractTerms(terms);
				for (Iterator<Term> iter = terms.iterator(); iter.hasNext(); ) {
					Term term = iter.next();
					// System.out.println(term.field() + " : " + term.text());
					ScoreDoc[] tmpHits = getHits(term.text().trim(), page);
		            addHits(hits, tmpHits, 1);
				}
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        
			/*for (int i = 0; i < hits.size(); ++i) {
				Document doc = search.getDoc(hits.get(i).doc);
				System.out.println("doc=" + hits.get(i).doc + " score="
					+ hits.get(i).score + " picPath= "
					+ doc.get("picPath")+ " tag= "+doc.get("abstract"));
			}*/
			
	        setRequestAttribute(request, response, hits, queryString, page);
			
		}
	}
	
	private ScoreDoc[] getHits(String queryString, int page) {
		TopDocs results=search.searchQuery(queryString, "abstract", 100);
		ScoreDoc[] hits = null;
		if (results != null) {
			// hits = showList(results.scoreDocs, page);
			hits = results.scoreDocs;
		}

		ArrayList<ScoreDoc> moreHits = new ArrayList<ScoreDoc>();
		/* Get the average score of images which matchs more than one query string,
		 * Set the status code 0.
		 */
		int statusCode = 0; 
		TopDocs contentRes = search.searchQuery(queryString, "content", 100);
		if (contentRes != null) {
			addHits(moreHits, contentRes.scoreDocs, statusCode);
		}
		
		TopDocs descriptionRes = search.searchQuery(queryString, "description", 100);
		if (descriptionRes != null) {
			addHits(moreHits, descriptionRes.scoreDocs, statusCode);
		}
		
		TopDocs keywordsRes = search.searchQuery(queryString, "keywords", 100);
		if (keywordsRes != null) {
			addHits(moreHits, keywordsRes.scoreDocs, statusCode);
		}
		
		TopDocs titleRes = search.searchQuery(queryString, "title", 100);
		if (titleRes != null) {
			addHits(moreHits, titleRes.scoreDocs, statusCode);
		}
		
		if (hits == null || hits.length == 0) {
			System.out.println("abstract search null");
			if (moreHits.size() != 0) {  
				hits = moreHits.toArray(new ScoreDoc[moreHits.size()]);
			}
			else {
				System.out.println("relative search null");
			}
		}
		
		return hits;
	}
	
	private void setRequestAttribute(HttpServletRequest request, HttpServletResponse response,
					ArrayList<ScoreDoc> hits, String queryString, int page) {
		String[] tags=null;
		String[] paths=null;
		
		if (hits.size() != 0) {
			Collections.sort(hits, new ScoreComparator());
			ScoreDoc[] imgs = showList(hits.toArray(new ScoreDoc[hits.size()]), page);
			if (imgs == null) {
				imgs = new ScoreDoc[0];
			}
			tags = new String[imgs.length];
			paths = new String[imgs.length];
			getTagsAndPaths(tags, paths, imgs, search);
		}
		else {
			System.out.println("Result Null");
		}
		
		try {
			request.setAttribute("currentQuery",queryString);
			request.setAttribute("currentPage", page);
			request.setAttribute("imgTags", tags);
			request.setAttribute("imgPaths", paths);
			request.getRequestDispatcher("/imageshow.jsp").forward(request,
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
	
	private void addHits(ArrayList<ScoreDoc> moreHits, ScoreDoc[] hits, int statusCode) {
		if (hits == null) return ;
		for (int i = 0; i < hits.length; ++i) {
/*			Document doc = search.getDoc(hits[i].doc);
			System.out.println("doc=" + hits[i].doc + " score="
				+ hits[i].score + " picPath= "
				+ doc.get("picPath")+ " tag= "+doc.get("abstract"));*/
			int flag = matchHits(hits[i], moreHits);
			if (flag != -1) {
				switch (statusCode) {
				case 0:
					/* For different field, such as "abstract", "content", "title"... and so on.
					 * I compute the score by importance in average.
					 * And the list sequence is :
					 * title, keywords, description, content.
					 */
					moreHits.get(flag).score = (moreHits.get(flag).score + hits[i].score) / 2;
					break;
				case 1:
					/* For different query string, need to merge the result of hits.
					 * It's obvious that pictures matching more query string will get higher score.
					 */
					moreHits.get(flag).score = moreHits.get(flag).score + hits[i].score;
					break;
				default:
					System.out.println("Error");
					break;
				}
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
	
	private void getTagsAndPaths(String[] tags, String[] paths, ScoreDoc[] hits, ImageSearcher search) {
		for (int i = 0; i < hits.length && i < PAGE_RESULT; i++) {
			Document doc = search.getDoc(hits[i].doc);
			/*System.out.println("doc=" + hits[i].doc + " score="
					+ hits[i].score + " picPath= "
					+ doc.get("picPath")+ " tag= "+doc.get("abstract"));*/
			tags[i] = doc.get("abstract");
			paths[i] = picDir + doc.get("picPath");

		}
	}
	
}
