package util;

import java.io.File;
import java.io.FileInputStream;
import java.io.PrintWriter;
import java.util.List;

import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;

public class DocReader {

	public static String readDocFile(String fileName) {
		String content = "";
		try {
			File file = new File(fileName);
			FileInputStream fis = new FileInputStream(file.getAbsolutePath());

			HWPFDocument doc = new HWPFDocument(fis);

			WordExtractor we = new WordExtractor(doc);
			String[] paragraphs = we.getParagraphText();
			we.close();
			
			for (String para : paragraphs) {
				content += para + "\n";
			}
			fis.close();
		} catch (Exception e) {
//			e.printStackTrace();
		}
		return content;
	}

	public static String readDocxFile(String fileName) {
		String content = "";
		try {
			File file = new File(fileName);
			FileInputStream fis = new FileInputStream(file.getAbsolutePath());

			XWPFDocument document = new XWPFDocument(fis);
			List<XWPFParagraph> paragraphs = document.getParagraphs();
			document.close();
			
			for (XWPFParagraph para : paragraphs) {
				// System.out.println(para.getText());
				content += para.getText() + "\n";
			}
			fis.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return content;
	}

	public static void main(String[] args) {
		try {
			PrintWriter pw = new PrintWriter("test/党课检查报告.txt");
			String content = readDocxFile("test/党课检查报告.docx");
			pw.write(content);
			pw.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		try{
			PrintWriter pw = new PrintWriter("test/党章.txt");
			String content = readDocFile("test/党章.DOC");
			pw.write(content);
			pw.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}

	}
}