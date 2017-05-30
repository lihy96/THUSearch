package parser;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.jsoup.Jsoup;

import cn.edu.hfut.dmic.contentextractor.ContentExtractor;
import index.THUIndexer;

public class HTMLParser {

	public static void htmlParser(String html, Document document) {
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
			
//			org.jsoup.nodes.Element keywordsE = doc.select("meta[name=\"keywords\"]").first();
//			org.jsoup.nodes.Element descriptionE = doc.select("meta[name=\"description\"]").first();
			org.jsoup.nodes.Element body = ContentExtractor.getContentElementByDoc(doc);
			String content = body.text();
			System.out.println("content : " + content);
			THUIndexer.averageLength += content.length() / THUIndexer.DIV_NUM;
			
			Field titleField = new Field("title", title, Field.Store.YES, Field.Index.ANALYZED);
			Field keywordsField = new Field("keywords", keywords, Field.Store.YES, Field.Index.ANALYZED);
			Field linkField = new Field("link", links, Field.Store.YES, Field.Index.ANALYZED);
			Field contentField = new Field("content", content, Field.Store.YES, Field.Index.ANALYZED);
			document.add(titleField);
			document.add(keywordsField);
			document.add(linkField);
			document.add(contentField);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
