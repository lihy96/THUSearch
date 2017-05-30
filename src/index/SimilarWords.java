package index;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map.Entry;

public class SimilarWords {

	private static HashMap<Integer, HashMap<Integer, Integer>> tf = new HashMap<Integer, HashMap<Integer, Integer>>();
	private static HashMap<Integer, Integer> idf = new HashMap<Integer, Integer>();
	private static HashMap<String, Integer> s2i = new HashMap<String, Integer>();
	private static HashMap<Integer, String> i2s = new HashMap<Integer, String>();
	private static int icnt = 0;
	private static int pcnt = 0;
	
	private final double MAX_RECOMMEND_NUM = 8;
	private final double RELATE_THRESHOLD = 0.05;
	
	HashMap<Integer, ArrayList<Integer>> relate = new HashMap<Integer, ArrayList<Integer>>();
	
	public void init() {
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
	
	public ArrayList<String> find(String word, int num) {
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

	public void save(String filePath) {
		System.out.println("Save similar words : " + filePath);
		try {
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath)));
			for (Entry<Integer, ArrayList<Integer>> relatework : relate.entrySet()) {
				String word = i2s.get(relatework.getKey());
				ArrayList<Integer> relateIdList = relatework.getValue();
				writer.write(word + " ");
				writer.write(relateIdList.size() + " ");
				for (Integer word2Id : relateIdList) {
					writer.write(i2s.get(word2Id) + " ");
				}
				writer.write("\n");
				writer.flush();
			}
			writer.close(); 
		} catch (Exception e) {
			System.out.println(e.toString());
		}
	}

	public void load(String filePath) {
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(filePath)));
			String line;
			int id = 0;
			while ((line = reader.readLine()) != null) {
				if (line.equals("")) continue;
				
				String[] info = line.split(" ");
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
	}
	
	
	public void add(String word, int docId) {
		for (int i = 0; i < word.length(); ++i) {
			char c = word.charAt(i);
			if (c >= '0' && c <= '9') return;
		}
		int wordId;
		if (s2i.containsKey(word)) {
			wordId = s2i.get(word);
		} else {
			s2i.put(word, icnt);
			i2s.put(icnt, word);
			idf.put(icnt, 0);
			wordId = icnt++;
		}
		if (!tf.containsKey(docId)) {
			tf.put(docId, new HashMap<Integer, Integer>());
		}
//		idf.put(wordId, idf.get(wordId) + 1);
		HashMap<Integer, Integer> docHash = tf.get(docId);
		Integer cc = docHash.get(wordId);
		cc = (cc == null) ? 0 : cc;
		docHash.put(wordId, ++cc);
	}
}
