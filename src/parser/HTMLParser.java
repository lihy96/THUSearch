package parser;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.jsoup.Jsoup;

import index.THUIndexer;

public class HTMLParser {

	public static void htmlParser(String content, Document document, boolean debug) {
		try {
			org.jsoup.nodes.Document doc = Jsoup.parse(content);
			org.jsoup.nodes.Element titleE = doc.select("title").first();
			org.jsoup.nodes.Element keywordsE = doc.select("meta[name=\"keywords\"]").first();
			org.jsoup.nodes.Element descriptionE = doc.select("meta[name=\"description\"]").first();
			org.jsoup.nodes.Element bodyE = doc.select("body").first();
			
			String title = "";
			if (titleE!=null && titleE.hasText()) {
				title = titleE.text();
				THUIndexer.averageLength += title.length() / THUIndexer.DIV_NUM;
			}
			String keywords = "";
			if (keywordsE!=null && keywordsE.hasAttr("content")) {
				keywords = keywordsE.attr("content");
				THUIndexer.averageLength += keywords.length() / THUIndexer.DIV_NUM;
			}
			String description = "";
			if (descriptionE!=null && descriptionE.hasAttr("content")) {
				description = descriptionE.attr("content");
				THUIndexer.averageLength += description.length() / THUIndexer.DIV_NUM;
			}
			
			String relativeContent = "";
			if (bodyE!=null) {
				for (org.jsoup.nodes.Element ele : bodyE.children()) {
					String text = ele.text();
					relativeContent += text;
				}
				THUIndexer.averageLength += relativeContent.length() / THUIndexer.DIV_NUM;
			}
			
			if (debug) {
				System.out.println("***********DEBUG INFO***********");
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
