import java.io.*;
import java.util.*;

import org.w3c.dom.*;
import org.wltea.analyzer.lucene.IKAnalyzer;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.jsoup.Jsoup;
import org.jsoup.select.Elements;

import javax.xml.parsers.*;

public class ImageIndexer {
	private Analyzer analyzer;
	private IndexWriter indexWriter;
	private float averageLength = 1.0f;

	@SuppressWarnings("deprecation")
	public ImageIndexer(String indexDir) {
		analyzer = new IKAnalyzer();
		try {
			IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_35, analyzer);
			Directory dir = FSDirectory.open(new File(indexDir));
			indexWriter = new IndexWriter(dir, iwc);
			indexWriter.setSimilarity(new SimpleSimilarity());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void saveGlobals(String filename) {
		try {
			PrintWriter pw = new PrintWriter(new File(filename));
			pw.println(averageLength);
			pw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * <p>
	 * index sogou.xml
	 * 
	 */
	public void indexSpecialFile(String filename) {
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			org.w3c.dom.Document doc = db.parse(new File(filename));
			NodeList nodeList = doc.getElementsByTagName("pic");
			for (int i = 0; i < nodeList.getLength(); i++) {
				Node node = nodeList.item(i);
				NamedNodeMap map = node.getAttributes();
				Node locate = map.getNamedItem("locate");
				Node bigClass = map.getNamedItem("bigClass");
				Node smallClass = map.getNamedItem("smallClass");
				Node query = map.getNamedItem("query");
				String absString = bigClass.getNodeValue() + " " + smallClass.getNodeValue() + " "
						+ query.getNodeValue();
				Document document = new Document();
				Field PicPathField = new Field("picPath", locate.getNodeValue(), Field.Store.YES, Field.Index.NO);
				Field abstractField = new Field("abstract", absString, Field.Store.YES, Field.Index.ANALYZED);
				averageLength += absString.length();
				document.add(PicPathField);
				document.add(abstractField);

				String html_path = "";
				String pic_dir = "D:/myprogram/java_src/.metadata/.plugins/org.eclipse.wst.server.core/tmp0/webapps/";
				html_path += pic_dir;
				html_path += locate.getNodeValue().replace(".jpg", ".html");
				add_other_field_to_document(html_path, query.getNodeValue(), document);

				indexWriter.addDocument(document);
				if (i % 10000 == 0) {
					System.out.println("process " + i);
				}
			}
			averageLength /= indexWriter.numDocs();
			System.out.println("average length = " + averageLength);
			System.out.println("total " + indexWriter.numDocs() + " documents");
			indexWriter.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 1. read html from path 2. add field to document
	 * 
	 * @param path
	 * @param query
	 * @param document
	 */
	public void add_other_field_to_document(String path, String query, Document document) {

		String html = read_file(path);
		org.jsoup.nodes.Document doc = Jsoup.parse(html);
		org.jsoup.nodes.Element title_eleme = doc.select("title").first();
		org.jsoup.nodes.Element keywords_eleme = doc.select("meta[name=\"keywords\"]").first();
		org.jsoup.nodes.Element description_eleme = doc.select("meta[name=\"description\"]").first();
		org.jsoup.nodes.Element body_eleme = doc.select("body").first();

		String title = "", keywords = "", description = "", content = "";
		if (title_eleme != null && title_eleme.hasText()) {
			title = title_eleme.text();
			averageLength += title.length();
		}
		if (keywords_eleme != null && keywords_eleme.hasAttr("content")) {
			keywords = keywords_eleme.attr("content");
			averageLength += keywords.length();
		}
		if (description_eleme != null && description_eleme.hasAttr("content")) {
			description = description_eleme.attr("content");
			averageLength += description.length();
		}
		if (body_eleme != null) {
			for (org.jsoup.nodes.Element ele : body_eleme.children()) {
				String text = ele.text();
				if (text.contains(query) || text.contains(title))
					content += text;
			}
			averageLength += content.length();
		}
		document.add(new Field("title", title, Field.Store.YES, Field.Index.ANALYZED));
		document.add(new Field("keywords", keywords, Field.Store.YES, Field.Index.ANALYZED));
		document.add(new Field("description", description, Field.Store.YES, Field.Index.ANALYZED));
		document.add(new Field("content", content, Field.Store.YES, Field.Index.ANALYZED));
	}

	/**
	 * read file to a string by path
	 * 
	 * @param path
	 * @return string
	 */
	public static String read_file(String path) {
		String data = null;
		// To determine whether the file exists
		File file = new File(path);
		if (!file.exists()) {
			System.out.println(file.toString());
			return data;
		}
		// Get file encoding format
		String code = get_file_encode(path);
		InputStreamReader isr = null;
		try {
			// Parse the file according to the encoding format
			if ("asci".equals(code)) {
				code = "GBK";
			}
			isr = new InputStreamReader(new FileInputStream(file), code);
			// Read the contents of the file
			int length = -1;
			char[] buffer = new char[1024];
			StringBuffer sb = new StringBuffer();
			while ((length = isr.read(buffer, 0, 1024)) != -1) {
				sb.append(buffer, 0, length);
			}
			data = new String(sb);
		} catch (Exception e) {
			e.printStackTrace();
			// log.info("getFile IO Exception:"+e.getMessage());
		} finally {
			try {
				if (isr != null) {
					isr.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
				// log.info("getFile IO Exception:"+e.getMessage());
			}
		}
		return data;
	}

	/**
	 * get the way of file encoding
	 * 
	 * @param path
	 * @return way of file encoding
	 */
	public static String get_file_encode(String path) {
		String charset = "asci";
		byte[] first3Bytes = new byte[3];
		BufferedInputStream bis = null;
		try {
			boolean checked = false;
			bis = new BufferedInputStream(new FileInputStream(path));
			bis.mark(0);
			int read = bis.read(first3Bytes, 0, 3);
			if (read == -1)
				return charset;
			if (first3Bytes[0] == (byte) 0xFF && first3Bytes[1] == (byte) 0xFE) {
				charset = "Unicode";// UTF-16LE
				checked = true;
			} else if (first3Bytes[0] == (byte) 0xFE && first3Bytes[1] == (byte) 0xFF) {
				charset = "Unicode";// UTF-16BE
				checked = true;
			} else if (first3Bytes[0] == (byte) 0xEF && first3Bytes[1] == (byte) 0xBB
					&& first3Bytes[2] == (byte) 0xBF) {
				charset = "UTF8";
				checked = true;
			}
			bis.reset();
			if (!checked) {
				int len = 0;
				int loc = 0;
				while ((read = bis.read()) != -1) {
					loc++;
					if (read >= 0xF0)
						break;
					if (0x80 <= read && read <= 0xBF) 
						break;
					if (0xC0 <= read && read <= 0xDF) {
						read = bis.read();
						if (0x80 <= read && read <= 0xBF)
							continue;
						else
							break;
					} else if (0xE0 <= read && read <= 0xEF) { 
						read = bis.read();
						if (0x80 <= read && read <= 0xBF) {
							read = bis.read();
							if (0x80 <= read && read <= 0xBF) {
								charset = "UTF-8";
								break;
							} else
								break;
						} else
							break;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (bis != null) {
				try {
					bis.close();
				} catch (IOException ex) {
				}
			}
		}
		return charset;
	}

	public static void main(String[] args) {
		ImageIndexer indexer = new ImageIndexer("forIndex/index");
		indexer.indexSpecialFile("input/sogou-utf8.xml");
		indexer.saveGlobals("forIndex/global.txt");
	}
}
