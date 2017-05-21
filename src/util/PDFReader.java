package util;

import java.io.File;
import java.io.FileInputStream;
import java.io.PrintWriter;

import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.io.RandomAccessRead;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

public class PDFReader {

	public static String readPDFFile(String fileName) {
		PDFParser parser = null;
	    PDDocument pdDoc = null;
	    COSDocument cosDoc = null;
	    PDFTextStripper pdfStripper;

	    String content = "";
	    File file = new File(fileName);
	    try {
	    	pdDoc = PDDocument.load(file);
	        pdfStripper = new PDFTextStripper();
	        content = pdfStripper.getText(pdDoc);
	        pdDoc.close();
	        // System.out.println(parsedText.replaceAll("[^A-Za-z0-9. ]+", ""));
	    } catch (Exception e) {
	        e.printStackTrace();
	        try {
	            if (cosDoc != null)
	                cosDoc.close();
	            if (pdDoc != null)
	                pdDoc.close();
	        } catch (Exception e1) {
	            e.printStackTrace();
	        }

	    }
	    
	    return content;
	}
	
	public static void main(String[] args) {
		try {
			PrintWriter pw = new PrintWriter("test/report.txt");
			String content = readPDFFile("test/report.pdf");
			pw.write(content);
			pw.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
