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

import com.sun.org.apache.xml.internal.resolver.helpers.PublicId;

import java.util.*;

import java.math.*;
import java.net.*;
import java.io.*;

public class ImageServer extends HttpServlet {
	public static final int PAGE_RESULT = 10;
	public static final String indexDir = "forIndex";
	public static final String picDir = "";
	private ImageSearcher search = null;

	public ImageServer() {
		super();
		search = new ImageSearcher(new String(indexDir + "/index"));
		search.loadGlobals(new String(indexDir + "/global.txt"));
	}

	public ScoreDoc[] showList(ScoreDoc[] results,int page){
		System.out.println("show list " + results.length + " " + page);
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

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/html;charset=utf-8");
		request.setCharacterEncoding("utf-8");
		String queryString = request.getParameter("query");
		String pageString = request.getParameter("page");
		System.out.println(" page str " + pageString);
		int page = 1;
		if (pageString != null) {
			page = Integer.parseInt(pageString);
		}
		if (queryString == null) {
			System.out.println("null query");
			// request.getRequestDispatcher("/Image.jsp").forward(request,
			// response);
		} else {
			System.out.println(queryString);
			System.out.println(URLDecoder.decode(queryString, "utf-8"));
			System.out.println(URLDecoder.decode(queryString, "gb2312"));
			
			
			ArrayList<ScoreDoc> hits = new ArrayList<ScoreDoc>();
			String[] strs = split_string(queryString);
			for (String s : strs) {
				add_hit_to_left_hit(hits, get_hit_by_queryString(s));
			}
			System.out.println();
			
			hits = remove_duplicate(hits);
			
			set_request_attribute(request, response, hits, queryString, page);
			
			
			/*
			String[] tags = null;
			String[] paths = null;

			TopDocs results = search.searchQuery(queryString, "abstract", 100);
			if (results != null) {
				ScoreDoc[] hits = showList(results.scoreDocs, page);
				ArrayList<ScoreDoc> hits_only_one = new ArrayList<ScoreDoc>();
				System.out.println("cnthit1 " + hits.length);
				for (int i = 0; i < hits.length; i++) {
					if (is_scoreDoc_in_list(hits[i], hits_only_one) == -1) {
						hits_only_one.add(hits[i]);
					}
				}
				hits = hits_only_one.toArray(new ScoreDoc[hits_only_one.size()]);
				System.out.println("cnthit " + hits.length);
				
				if (hits != null) {
					tags = new String[hits.length];
					paths = new String[hits.length];
					for (int i = 0; i < hits.length && i < PAGE_RESULT; i++) {
						Document doc = search.getDoc(hits[i].doc);
						System.out.println("doc=" + hits[i].doc + " score=" + hits[i].score + " picPath= "
								+ doc.get("picPath") + " tag= " + doc.get("abstract"));
						tags[i] = doc.get("abstract");
						paths[i] = picDir + doc.get("picPath");
						// System.out.println("haha " + paths[i]);
					}

				} else {
					System.out.println("page null");
				}
			} else {
				System.out.println("result null");
			}
			request.setAttribute("currentQuery", queryString);
			System.out.println("page " + page);
			request.setAttribute("currentPage", page);
			request.setAttribute("imgTags", tags);
			request.setAttribute("imgPaths", paths);
			request.getRequestDispatcher("/imageshow.jsp").forward(request, response);
			*/
		}
	}
	
	public ArrayList<ScoreDoc> remove_duplicate(ArrayList<ScoreDoc> hits) {
		System.out.println("length of hits 1 " + hits.size());
		ImageSearcher search=new ImageSearcher("forIndex/index");		
		ArrayList<ScoreDoc> ret = new ArrayList<ScoreDoc>();
		for (ScoreDoc e : hits){
			String path = search.getDoc(e.doc).get("picPath");
			System.out.println("path " + path);
			boolean exist = false;
			for (ScoreDoc i : ret){
				if (path.equals(search.getDoc(i.doc).get("picPath"))){
					exist = true;
					break;
				}
			}
			if (!exist) {
				ret.add(e);
			}
		}
		return ret;
		
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		this.doGet(request, response);
	}

	public String[] split_string(String queryString) {
		ArrayList<String> str_list = new ArrayList<String>();
		try {
			Query queryClass = new QueryParser(Version.LUCENE_35, "content", new IKAnalyzer(false)).parse(queryString);
			Set<Term> terms = new HashSet<Term>();
			queryClass.extractTerms(terms);
			for (Iterator<Term> iter = terms.iterator(); iter.hasNext();) {
				Term term = iter.next();
				str_list.add(term.text().trim());
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return str_list.toArray(new String[str_list.size()]);
	}

	private int is_scoreDoc_in_list(ScoreDoc hit, ArrayList<ScoreDoc> moreHits) {
		Iterator<ScoreDoc> iter = moreHits.iterator();
		int match = -1;
		while (iter.hasNext()) {
			match ++;
			ScoreDoc ele = iter.next();
			if (ele.doc == hit.doc) {
				System.out.println("samedoc");
				return match;
			}
		}
		return -1;
	}

	public ScoreDoc[] get_hit_by_queryString(String queryString) {
		System.out.println("query string " + queryString);
		TopDocs results = search.searchQuery(queryString, "abstract", 100);
		ScoreDoc[] hits = null;
		// get result according to key word
		if (results != null) {
			hits = results.scoreDocs;
			if (hits != null && hits.length > 0) {
				System.out.println("abstract search have result!");
				return hits;
			}
		}

		System.out.println("abstract search doesn't have result!");
		// get result according to title, content ..... and is only happen after
		// searching by keyword but no result
		ArrayList<ScoreDoc> otherFieldHits = new ArrayList<ScoreDoc>();
		TopDocs titleRes = search.searchQuery(queryString, "title", 100);
		TopDocs keywordsRes = search.searchQuery(queryString, "keywords", 100);
		TopDocs descriptionRes = search.searchQuery(queryString, "description", 100);
		TopDocs contentRes = search.searchQuery(queryString, "content", 100);
		ArrayList<TopDocs> topDocs = new ArrayList<TopDocs>();
		topDocs.add(titleRes);
		topDocs.add(keywordsRes);
		topDocs.add(descriptionRes);
		topDocs.add(contentRes);

		// get result searching by content to title
		for (int i = topDocs.size() - 1; i >= 0; i--) {
			TopDocs t = topDocs.get(i);
			if (t == null || t.scoreDocs == null)
				continue;
			ScoreDoc[] scoreDocs = t.scoreDocs;
			for (int j = 0; j < scoreDocs.length; j++) {
				int flag = is_scoreDoc_in_list(scoreDocs[j], otherFieldHits);
				// scoreDoc not in otherFieldHits
				if (flag == -1) {
					otherFieldHits.add(scoreDocs[j]);
				} else {
					otherFieldHits.get(flag).score = (otherFieldHits.get(flag).score + scoreDocs[j].score) / 2;
				}
			}
		}
		if (otherFieldHits.size() != 0)
			hits = otherFieldHits.toArray(new ScoreDoc[otherFieldHits.size()]);
		else
			System.out.println("relative search doesn't have result!");

		return hits;
	}

	public void add_hit_to_left_hit(ArrayList<ScoreDoc> left, ScoreDoc[] right) {
		if (right == null || right.length <= 0)
			return;
		for (int i = 0; i < right.length; i++) {
			int index = is_scoreDoc_in_list(right[i], left);
			if (index == -1) {
				left.add(right[i]);
			} else {
				left.get(index).score += right[i].score;
			}

		}
	}

	private void set_request_attribute(HttpServletRequest request, HttpServletResponse response, ArrayList<ScoreDoc> hits,
			String queryString, int page) {
		String[] tags = null;
		String[] paths = null;
		if (hits.size() != 0) {
			Collections.sort(hits, new ScoreComparator());
			ScoreDoc[] imgs = showList(hits.toArray(new ScoreDoc[hits.size()]), page);
			if (imgs == null)
				imgs = new ScoreDoc[0];
			tags = new String[imgs.length];
			paths = new String[imgs.length];
			getTagsAndPaths(tags, paths, imgs, search);
		} else {
			System.out.println("Result Null");
		}

		try {
			request.setAttribute("currentQuery", queryString);
			request.setAttribute("currentPage", page);
			request.setAttribute("imgTags", tags);
			request.setAttribute("imgPaths", paths);
			request.getRequestDispatcher("/imageshow.jsp").forward(request, response);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	private void getTagsAndPaths(String[] tags, String[] paths, ScoreDoc[] hits, ImageSearcher search) {
		for (int i = 0; i < hits.length && i < PAGE_RESULT; i++) {
			Document doc = search.getDoc(hits[i].doc);
			tags[i] = doc.get("abstract");
			paths[i] = picDir + doc.get("picPath");

		}
	}
}
