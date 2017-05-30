package parser;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.jsoup.Jsoup;

import cn.edu.hfut.dmic.contentextractor.ContentExtractor;
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
			
//			org.jsoup.nodes.Element keywordsE = doc.select("meta[name=\"keywords\"]").first();
//			org.jsoup.nodes.Element descriptionE = doc.select("meta[name=\"description\"]").first();
			String content = "";
			try {
				org.jsoup.nodes.Element body = ContentExtractor.getContentElementByDoc(doc);
				content = body.text();
			} catch (Exception e) {
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
			
			
//			org.jsoup.nodes.Document doc = Jsoup.parse(content);
//			org.jsoup.nodes.Element titleE = doc.select("title").first();
//			org.jsoup.nodes.Element keywordsE = doc.select("meta[name=\"keywords\"]").first();
//			org.jsoup.nodes.Element descriptionE = doc.select("meta[name=\"description\"]").first();
//			org.jsoup.nodes.Element body = ContentExtractor.getContentElementByDoc(doc);
//			
//			String title = "";
//			if (titleE!=null && titleE.hasText()) {
//				title = titleE.text();
//				THUIndexer.averageLength += title.length() / THUIndexer.DIV_NUM;
//			}
//			String keywords = "";
//			if (keywordsE!=null && keywordsE.hasAttr("content")) {
//				keywords = keywordsE.attr("content");
//				THUIndexer.averageLength += keywords.length() / THUIndexer.DIV_NUM;
//			}
//			String description = "";
//			if (descriptionE!=null && descriptionE.hasAttr("content")) {
//				description = descriptionE.attr("content");
//				THUIndexer.averageLength += description.length() / THUIndexer.DIV_NUM;
//			}
//			
//			String relativeContent = "";
//			if (body!=null) {
//				for (org.jsoup.nodes.Element ele : body.children()) {
//					String text = ele.text();
//					relativeContent += text;
//				}
//				THUIndexer.averageLength += relativeContent.length() / THUIndexer.DIV_NUM;
//			}
//			
//			Field titleField = new TextField("title", title, Field.Store.YES);
//			Field keywordsField = new TextField("keywords", keywords, Field.Store.YES);
//			Field descriptionField = new TextField("description", description, Field.Store.YES);
//			Field contentField = new TextField("content", relativeContent, Field.Store.YES);
//			titleField.setBoost(pagerank);
//			keywordsField.setBoost(pagerank);
//			descriptionField.setBoost(pagerank);
//			contentField.setBoost(pagerank);
//			document.add(titleField);
//			document.add(keywordsField);
//			document.add(descriptionField);
//			document.add(contentField);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
