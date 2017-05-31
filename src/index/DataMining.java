package index;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.pdfbox.contentstream.operator.state.SetRenderingIntent;

import com.sun.java.swing.plaf.windows.WindowsInternalFrameTitlePane.WindowsPropertyChangeHandler;
import com.sun.org.apache.xerces.internal.xs.StringList;
import com.sun.xml.internal.bind.v2.runtime.unmarshaller.XsiNilLoader.Array;

import pagerank.WebSite;
import util.StaticValue;

public class DataMining {

	private HashMap<Integer, HashMap<Integer, Integer>> tf = new HashMap<Integer, HashMap<Integer, Integer>>();
	private HashMap<Integer, Integer> idf = new HashMap<Integer, Integer>();
	private HashMap<String, Integer> s2i = new HashMap<String, Integer>();
	private HashMap<Integer, String> i2s = new HashMap<Integer, String>();
	private int icnt = 0;
	
	private final double MAX_RECOMMEND_NUM = 8;
	private final double RELATE_THRESHOLD = 0.10;
	private final int Q = 2;	
	private final double ED_RATIO = 0.2;
	
	private HashMap<Integer, ArrayList<Integer>> relate = new HashMap<Integer, ArrayList<Integer>>();
	private Map<Integer, Integer> icount = new LinkedHashMap<Integer, Integer>();
	HashMap<String, ArrayList<Integer>> correct = new HashMap<String, ArrayList<Integer>>();
	
	
	public void init_sim_words() {
		System.out.println("Build inverted index ...");
		HashMap<Integer, ArrayList<Integer>> invertedIndex = new HashMap<Integer, ArrayList<Integer>>();
		for (Entry<Integer, HashMap<Integer, Integer>> pagework : tf.entrySet()) {
			int pageId = pagework.getKey();
			for (Entry<Integer, Integer> entry : pagework.getValue().entrySet()) {
				int wordId = entry.getKey();
				if (!invertedIndex.containsKey(wordId)) {
					invertedIndex.put(wordId, new ArrayList<Integer>());
				}
				invertedIndex.get(wordId).add(pageId);
				int _idf = 0;
				if (idf.containsKey(wordId)) {
					_idf = idf.get(wordId);
				}
				idf.put(wordId, _idf + 1);
			}
		}
		System.out.println("Build inverted index finish !");
		
		System.out.println("Get releations ... ");
		int cc = 0;
		for (Entry<Integer, ArrayList<Integer>> wordwork : invertedIndex.entrySet()) {
			cc ++;
			if (cc % 100 == 0) {
				System.out.println("Token relation map : " + cc);
			}
			
			int wordId = wordwork.getKey();
			ArrayList<Integer> pageList = wordwork.getValue();
			HashMap<Integer, Double> relateMap = new HashMap<Integer, Double>();
			for (int pageId : pageList) {
				HashMap<Integer, Integer> relateList = tf.get(pageId);
				for (Entry<Integer, Integer> relatework : relateList.entrySet()) {
					int word2Id = relatework.getKey();
					if (wordId == word2Id) continue;
					double r = 0;
					if (relateMap.containsKey(word2Id)) {
						r = relateMap.get(word2Id);
					}   
					relateMap.put(word2Id, r + 1);
				}
			}
			ArrayList<Entry<Integer, Double>> relateArray = new ArrayList<Entry<Integer, Double>>();
			for (Entry<Integer, Double> relatework : relateMap.entrySet()) {
				int word2Id = relatework.getKey();
				double r = relatework.getValue();
				if (r < 3 || r > 200) continue;
				relatework.setValue(r / Math.sqrt(idf.get(word2Id)));
				relateArray.add(relatework);  
			}   
			Collections.sort(relateArray, new Comparator<Entry<Integer, Double>>() {
				public int compare(Entry<Integer, Double> o0, Entry<Integer, Double> o1) {
					Double d0 = o0.getValue();
					Double d1 = o1.getValue();
					return d1.compareTo(d0);
				}
			});
			relate.put(wordId, new ArrayList<Integer>());
			for (Entry<Integer, Double> relatework : relateArray) {
				int word2Id = relatework.getKey();
				double r = relatework.getValue();
				if (r < RELATE_THRESHOLD) break;
				//System.out.println(indexToWord.get(wordId) + " " + indexToWord.get(word2Id) + " " + r);
				ArrayList<Integer> relateList = relate.get(wordId);
				relateList.add(word2Id);
				if (relateList.size() >= MAX_RECOMMEND_NUM) break;
			}
		}
		System.out.println("Get relations finish !");
	}
	
	public void save_corr_table(String filePath) {
		System.out.println("Save correct table : " + filePath);
		try {
			BufferedWriter bWriter = new BufferedWriter(new FileWriter(filePath));
			for (Entry<String, ArrayList<Integer>> entry : correct.entrySet()) {
				String gram = entry.getKey();
				bWriter.write(gram + "\t");
				ArrayList<Integer> list = entry.getValue();
				for (Integer id : list) {
					bWriter.write(i2s.get(id) + "\t");
				}
				bWriter.write("\n");
			}
			bWriter.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void save_sim_words(String filePath) {
		System.out.println("Save similar words : " + filePath);
		try {
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath)));
			for (Entry<Integer, ArrayList<Integer>> relatework : relate.entrySet()) {
				String word = i2s.get(relatework.getKey());
				ArrayList<Integer> relateIdList = relatework.getValue();
				if (relateIdList.size() == 0) continue;
				writer.write(word + "\t");
				writer.write(relateIdList.size() + "\t");
				for (Integer word2Id : relateIdList) {
					writer.write(i2s.get(word2Id) + "\t");
				}
				writer.write("\n");
				writer.flush();
			}
			writer.close(); 
		} catch (Exception e) {
			System.out.println(e.toString());
		}
	}

	public void save_autocom_word(String filePath) {
		System.out.println("Build autocompleted word table.");
		Map<Integer, Integer> sortedICount = new LinkedHashMap<Integer, Integer>();

		Comparator<Entry<Integer, Integer>> byValue = 
				(entry1, entry2) -> 
				entry1.getValue().compareTo(entry2.getValue());

		icount.entrySet().stream()
			.sorted(byValue.reversed())
			.forEach(x -> sortedICount.put(x.getKey(), x.getValue()));
		
		icount.clear();
		icount.putAll(sortedICount);
		
		System.out.println("Save auto completed word table.");
		try {
			BufferedWriter bWriter = new BufferedWriter(new FileWriter(filePath));
			for (Entry<Integer, Integer> entry : icount.entrySet()) {
				String word = i2s.get(entry.getKey());
				bWriter.write(word + "\t");
				bWriter.write(entry.getValue() + "\n");
			}
			bWriter.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void init_correct_table() {
		System.out.println("Build correct inverted index ...");
		for (Entry<String, Integer> wordwork : s2i.entrySet()) {
			String word = wordwork.getKey();
			Integer wordId = wordwork.getValue();
			for (int i = 0; i + Q <= word.length(); ++i) {
				String gram = word.substring(i, i + Q);
				if (!correct.containsKey(gram)) {
					correct.put(gram, new ArrayList<Integer>());
				}
				correct.get(gram).add(wordId);
			}
		}
		System.out.println("Build correct inverted index finish !");
	}
	
	public void load_words(String simPath, String autocomPath) {
		int id = 0;
		try {
			BufferedReader reader = new BufferedReader(
					new InputStreamReader(new FileInputStream(simPath), "utf-8"));
			String line;
			while ((line = reader.readLine()) != null) {
				if (line.equals("")) continue;
				
				String[] info = line.split("\t");
				if (info[1].equals("0")) continue;
//				if (info[0].equals("清华")) System.out.println("清华搜索到了");
				if (!s2i.containsKey(info[0])) {
					s2i.put(info[0], id);
					i2s.put(id ++, info[0]);
				}
				int wordId = s2i.get(info[0]);
				relate.put(wordId, new ArrayList<Integer>());
				for (int i = 2; i < info.length; ++i) {
					String word = info[i];
					if (!s2i.containsKey(word)) {
						s2i.put(word, id);
						i2s.put(id ++, word);
					}
					int word2Id = s2i.get(word);
					relate.get(wordId).add(word2Id);
				}
			}
			
			reader.close();
		} catch (Exception e) {
			System.out.println(e.toString());
		}

		try {
			BufferedReader bReader = new BufferedReader(
					new InputStreamReader(new FileInputStream(autocomPath), "utf-8"));
			String line;
			while((line = bReader.readLine()) != null) {
				if (line.equals("")) continue;
				
				String[] info = line.trim().split("\t");
				int weight = Integer.parseInt(info[1]);
//				if (weight < StaticValue.AUTO_COM_THRESHOLD) break;
				if (!s2i.containsKey(info[0])) {
					s2i.put(info[0], id);
					i2s.put(id ++, info[0]);
				}
				int wordId = s2i.get(info[0]);
				icount.put(wordId, weight);
			}
			bReader.close();
			System.out.println("Auto complete words number : " + icount.size());
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		

		System.out.println("Build correct inverted index ...");
		for (Entry<Integer, Integer> wordwork : icount.entrySet()) {
//			String word = wordwork.getKey();
			Integer wordId = wordwork.getKey();
			String word = i2s.get(wordId);
			for (int i = 0; i + Q <= word.length(); ++i) {
				String gram = word.substring(i, i + Q);
				if (!correct.containsKey(gram)) {
					correct.put(gram, new ArrayList<Integer>());
				}
				correct.get(gram).add(wordId);
			}
		}
		System.out.println("Build correct inverted index finish !");
	}
	
	public ArrayList<String> find_correct_words(String word, int num) {
		int ed = (int) Math.ceil(word.length() * ED_RATIO);
		int t = Math.max((int)Math.ceil(word.length() - Q + 1 - ed * Q), 1);
		//System.out.print(ed + " " + t + " ");
		HashMap<Integer, Double> count = new HashMap<Integer, Double>();
		for (int i = 0; i + Q < word.length(); ++i) {
			String gram = word.substring(i, i + Q);
			if (!correct.containsKey(gram))
				continue;
			for (int word2Id : correct.get(gram)) {
				double c = 0;
				if (count.containsKey(word2Id)) {
					c = count.get(word2Id);
				}
				count.put(word2Id, c + 1);
			}
		}
		ArrayList<Entry<Integer, Double>> similarList = new ArrayList<Entry<Integer, Double>>();
		for (Entry<Integer, Double> similarwork : count.entrySet()) {
			int word2Id = similarwork.getKey();
			double times = similarwork.getValue();
			if (times < t) continue;
			String word2 = i2s.get(word2Id);
			if (editDistance(word, word2, ed) > ed) continue;
			similarwork.setValue(1.0 * icount.get(word2Id));
			similarList.add(similarwork);
		}
		Collections.sort(similarList, new Comparator<Entry<Integer, Double>>() {
			public int compare(Entry<Integer, Double> o0, Entry<Integer, Double> o1) {
				Double d0 = o0.getValue();
				Double d1 = o1.getValue();
				return d1.compareTo(d0);
			}
		});
		ArrayList<String> resultList = new ArrayList<String>();
		for (int i = 0; i < Math.min(num, similarList.size()); ++i) {
			resultList.add(i2s.get(similarList.get(i).getKey()));
		}
//		for (Entry<Integer, Double> similarwork : similarList) {
//			resultList.add(i2s.get(similarwork.getKey()));
//		}
		return resultList;
	}
	
	public ArrayList<String> find_autocom_words(String word, int num) {
		if (word.equals("")) return new ArrayList<String>();
		ArrayList<String> completeList = new ArrayList<String>();
		Iterator<Entry<Integer, Integer>> iterator = icount.entrySet().iterator();
		for (int i = 0; i < num; ++i) {
			while (iterator.hasNext()) {
				Entry<Integer, Integer> entry = iterator.next();
				if (entry.getValue() < StaticValue.AUTO_COM_THRESHOLD) break;
				String word2 = i2s.get(entry.getKey());
				if (word2.startsWith(word)) {
					completeList.add(word2);
					break;
				}
			}
		}
		return completeList;
	}
	
	public ArrayList<String> find_sim_words(String word, int num) {
		if (!s2i.containsKey(word)) {
			return new ArrayList<String>();
		}
		int wordId = s2i.get(word);
		//System.out.println("##########"+wordId);
		ArrayList<Integer> relateIdList = relate.get(wordId);
		ArrayList<String> relateList = new ArrayList<String>();
		for (int i = 0; i < Math.min(num, relateIdList.size()); ++i) {
			relateList.add(i2s.get(relateIdList.get(i)));
		}
		return relateList;
	}
	
	
	public void add(String word, int docId) {
		if (word.length() < 2) return ;
		for (int i = 0; i < word.length(); ++i) {
			char c = word.charAt(i);
			if (c >= '0' && c <= '9') return;
		}
		if (word.endsWith(".")) word = word.substring(0, word.length()-1);
		
		int wordId;
		if (s2i.containsKey(word)) {
			wordId = s2i.get(word);
		} else {
			s2i.put(word, icnt);
			i2s.put(icnt, word);
//			idf.put(icnt, 0);
			icount.put(icnt, 0);
			wordId = icnt++;
		}
		if (!tf.containsKey(docId)) {
			tf.put(docId, new HashMap<Integer, Integer>());
		}
//		idf.put(wordId, idf.get(wordId) + 1);
		icount.put(wordId, icount.get(wordId) + 1);
		HashMap<Integer, Integer> docHash = tf.get(docId);
		Integer cc = docHash.get(wordId);
		cc = (cc == null) ? 0 : cc;
		docHash.put(wordId, ++cc);
	}
	
	private int editDistance(String a, String b, int ed) {
		if (Math.abs(a.length() - b.length()) > ed) return ed + 1;
		int f[][] = new int[a.length() + 1][b.length() + 1];
		for (int i = 0; i <= a.length(); ++i)
			for (int j = 0; j <= b.length(); ++j)
				f[i][j] = ed + 1;
		f[0][0] = 0;
		for (int i = 0; i <= a.length(); ++i) {
			int js = Math.max(0, i - ed);
			int jt = Math.min(b.length(), i + ed);
			for (int j = js; j <= jt; ++j) {
				if (f[i][j] + Math.abs(i - j) > ed) continue;
				if (i < a.length() && j < b.length()) {
					int d = (a.charAt(i) == b.charAt(j)) ? 0 : 1;
					f[i + 1][j + 1] = Math.min(f[i + 1][j + 1], f[i][j] + d);
				}
				if (i < a.length()) {
					f[i + 1][j] = Math.min(f[i + 1][j], f[i][j] + 1);
				}
				if (j < b.length()) {
					f[i][j + 1] = Math.min(f[i][j + 1], f[i][j] + 1);
				}
			}
		}
		return f[a.length()][b.length()];
	}
}
