package parser;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

import index.THUIndexer;

public class CommonParser {

	public static void commParser(String title, String content, Document document) {
		Field titleField = new Field("title", title, Field.Store.YES, Field.Index.ANALYZED);
		THUIndexer.averageLength += title.length() / THUIndexer.DIV_NUM;
		
		Field contentField = new Field("content", content, Field.Store.YES, Field.Index.ANALYZED);
		THUIndexer.averageLength += title.length() / THUIndexer.DIV_NUM;
		
		document.add(titleField);
		document.add(contentField);
	}
}
