package parser;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;

import index.THUIndexer;

public class CommonParser {

	public static void commParser(String title, String content, Document document, float pagerank) {
		pagerank = 0.0011511079962592142f;
		Field titleField = new TextField("title", title, Field.Store.YES);
		titleField.setBoost(pagerank);
		THUIndexer.averageLength += title.length() / THUIndexer.DIV_NUM;
		
		Field contentField = new TextField("content", content, Field.Store.YES);
		contentField.setBoost(pagerank);
		THUIndexer.averageLength += content.length() / THUIndexer.DIV_NUM;
		
		document.add(titleField);
		document.add(contentField);
	}
}
