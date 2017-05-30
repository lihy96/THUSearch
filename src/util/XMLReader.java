package util;

import java.io.File;
import java.io.PrintWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XMLReader {

	public static String readXMLFile(String filePath) {
		String content = "";
		try {
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(new File(filePath));
			
			//optional, but recommended
			//read this - http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
			doc.getDocumentElement().normalize();
			
			Node node = doc.getFirstChild();
			while (node != null) {
				content += parseNode(node);
				node = node.getNextSibling();
			}
		}
		catch (Exception e) {
//			e.printStackTrace();
		}
		return content;
	}
	
	private static String parseNode(Node node) {
		String content = "";
		if (node != null) {
			String str = node.getNodeValue();
			if (str != null && !str.equals("")) {
				String[] strs = str.trim().split("\n");
				for (int i = 0; i < strs.length; ++i) {
					if (strs[i].equals("")) continue;
					content += strs[i] + "\n";
				}
			}
			
			NodeList list = node.getChildNodes();
			for (int i = 0; i < list.getLength(); ++i) {
				content += parseNode(list.item(i));
			}
		}
		return content;
	}
	
	public static void main(String[] args) {
		try {
			PrintWriter pw = new PrintWriter("test/build.txt");
			String content = readXMLFile("test/build.xml");
			if (content != null)
				// System.out.println(content);
				pw.write(content);
			pw.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
