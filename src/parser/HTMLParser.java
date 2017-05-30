package parser;

import java.io.StringReader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.jsoup.Jsoup;

import cn.edu.hfut.dmic.contentextractor.ContentExtractor;
import index.SimilarWords;
import index.THUIndexer;

public class HTMLParser {

	public static void htmlParser(String html, Document document, float pagerank) {
		try {
			org.jsoup.nodes.Document doc = Jsoup.parse(html);
			org.jsoup.nodes.Element titleE = doc.select("title").first();
			String title = "";
			if (titleE!=null && titleE.hasText()) {
				title = titleE.text();
				THUIndexer.averageLength += title.length() / THUIndexer.DIV_NUM;
			}
			String keywords = doc.select("h1,h2,h3,h4,h5,h6").text();
			String links = doc.select("a").text();
			THUIndexer.averageLength += (keywords.length() + links.length()) / THUIndexer.DIV_NUM;
			
			String content = "";
			try {
				org.jsoup.nodes.Element body = ContentExtractor.getContentElementByDoc(doc);
				content = body.text();
			} catch (Exception e) {}
			if (content.equals("")) {
				org.jsoup.select.Elements body = doc.select("p");
				content = body.text();
			}
			THUIndexer.averageLength += content.length() / THUIndexer.DIV_NUM;
			
			Field titleField = new TextField("title", title, Field.Store.YES);
			Field keywordsField = new TextField("keywords", keywords, Field.Store.YES);
			Field linkField = new TextField("link", links, Field.Store.YES);
			Field contentField = new TextField("content", content, Field.Store.YES);
			titleField.setBoost(pagerank);
			keywordsField.setBoost(pagerank);
			linkField.setBoost(pagerank);
			contentField.setBoost(pagerank);
			document.add(titleField);
			document.add(keywordsField);
			document.add(linkField);
			document.add(contentField);
			
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void tokenParser(SimilarWords sw, Integer docId, String html, Analyzer analyzer) {
		try {
			org.jsoup.nodes.Document doc = Jsoup.parse(html);
			
			String content = "";
			try {
				org.jsoup.nodes.Element body = ContentExtractor.getContentElementByDoc(doc);
				content = body.text();
			} catch (Exception e) {}
			if (content.equals("")) {
				org.jsoup.select.Elements body = doc.select("p");
				content = body.text();
			}
			
			TokenStream ts = analyzer.tokenStream("content", new StringReader(content));
			ts.reset();
			CharTermAttribute term = ts.getAttribute(CharTermAttribute.class);
			while (ts.incrementToken()) {
	            sw.add(term.toString(), docId);
	        }
			ts.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
