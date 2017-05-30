package pagerank;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import util.DirectoryChecker;
import util.FileOperator;

public class PageRank {
	private double alpha;
	private int TN;
	private String outDir;
	
	private String pr_filepath;
	private String pr_analyze;

	public Map<Integer, ArrayList<Integer>> map = null;
	public Map<Integer, WebSite> webs = null;
//	public Map<Integer, ArrayList<Integer>> map = new HashMap<Integer, ArrayList<Integer>>();
//	public Map<Integer, WebSite> webs = new HashMap<Integer, WebSite>();
	
	private int N; // total web site number
	private int ZERO_OUT_DEGREE = 0; // count of web site with zero out degree	
	private Double S; // total page rank with no out degree web site
	
	public PageRank(double alpha, int n, Map<Integer, ArrayList<Integer>> _map,
					Map<Integer, WebSite> _webs, String _out) {
		this.alpha = alpha;
		this.TN = n;
		this.map = _map;
		this.webs = _webs;
		this.outDir = _out;
		pr_filepath = outDir + "/pagerank.txt";
		pr_analyze = outDir + "/analyze.txt";
	}

	public void calPageRank() {
		assert(outDir == null);
		
		File od = new File(pr_filepath);
		if (od.exists()) {
			loadWebs();
		}
		else {
			initPageRank();
			iteratePageRank();
			sort();
			saveInfo();
		}
	}
	
	private void loadWebs() {
		try {
			System.out.println("Loading page rank file.");
			BufferedReader br = new BufferedReader(new FileReader(pr_filepath));
			String line;
			while ((line = br.readLine()) != null) {
				if (line.equals("")) continue;
				String[] info = line.trim().split("\t");
				assert(info.length != 3);
				WebSite ws = new WebSite();
				ws.name = info[0];
				ws.id = Integer.parseInt(info[1]);
				ws.pagerank = Double.parseDouble(info[2]);
				webs.put(ws.id, ws);
			}
			br.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void sort() {
		Map<Integer, WebSite> sortedWebs = new LinkedHashMap<Integer, WebSite>();

		// 使用pagerank对网站排序
		Comparator<Entry<Integer, WebSite>> byValue = 
				(entry1, entry2) -> 
				compare(entry1.getValue().pagerank, entry2.getValue().pagerank);

		webs.entrySet().stream()
			.sorted(byValue.reversed())
			.forEach(x -> sortedWebs.put(x.getKey(), x.getValue()));
	    
		webs.clear();
		webs.putAll(sortedWebs);
	}
	
	private void iteratePageRank() {
		assert(TN < 0);
		
		for (int k = 0; k < TN; ++k) {
			System.out.println("Process iteration : " + k);
			// 遍历每一个网站
			for (Entry<Integer, ArrayList<Integer>> entry : map.entrySet()) {
				int siteNum = entry.getKey();
				WebSite ws = webs.get(siteNum);
				double delta = (1 - alpha) * ws.pagerank / ws.outdegree;
				
				ArrayList<Integer> outSiteNums = entry.getValue();
				// 遍历每一条链接关系E(i, j)
				for (Integer outSiteNum : outSiteNums) {
					WebSite wso = webs.get(outSiteNum);
					// 存在超链接指向的网站并不存在已分析的网站中，需特殊考虑
					// 直接忽略该网站
					if (wso == null) continue;
					
					wso.In += delta;
				}
			}
			
			// 更新pagerank的值和初始化In
			double zerooutdegree = (1 - alpha) * S / N;
			// 重新计算出度为0的pagerank之和
			S = 0.0;
			for (Entry<Integer, WebSite> entry : webs.entrySet()) {
				WebSite ws = entry.getValue();
				// 更新pagerank的值
				ws.pagerank = ws.In + zerooutdegree;
				// 重新设置In的值：alpha / N
				ws.initIn();
				
				// 如果出度为0，则统计在S中
				if (ws.outdegree == 0) {
					S += ws.pagerank;
				}
			}
		}
	}
	
	private void initPageRank() {
		System.out.println("Init page rank");
		N = map.size();
		WebSite.setParam(N, alpha);
		
		S = 0.0;
		for (Entry<Integer, WebSite> entry : webs.entrySet()) {
			WebSite ws = entry.getValue();
			ws.pagerank = 1.0 / N;
			ws.initIn();
			
			if (ws.outdegree == 0) {
				ZERO_OUT_DEGREE ++;
				S += ws.pagerank;
			}
		}
	}
	
	public void saveInfo() {
		
		DirectoryChecker.dirCheck(pr_filepath);
		
		try {
			System.out.println("Saving page rank file.");
			BufferedWriter bw = new BufferedWriter(new FileWriter(pr_filepath));
			for (Entry<Integer, WebSite> entry : webs.entrySet()) {
				WebSite ws = entry.getValue();
				bw.write(ws.name + "\t" + ws.id + "\t" + ws.pagerank + "\n");
			}
			bw.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		try {
			System.out.println("Saving abstract analyze file.");
			BufferedWriter bw = new BufferedWriter(new FileWriter(pr_analyze));
			
			bw.write("***********************Your Confiure Settings***************************\n");
			bw.write("* alpha : " + alpha + "\n");
			bw.write("* iteration number : " + TN + "\n");
			bw.write("\n\n");
			
			bw.write("*****************************Output Note********************************\n");
			bw.write("* analyze.txt : some abstract infomation are stored in here.\n");
			bw.write("* pagerank.txt : the unsorted website stored.\n");
			bw.write("* sortedpagerank.txt : the website are listed by pagerank value.\n");
			bw.write("\n\n");
			
			bw.write("***************************Abstract Output******************************\n");
			bw.write("* Website of zero outdegree count : " + ZERO_OUT_DEGREE + "\n");
			bw.write("* Total web site number : " + N + "\n");
			bw.write("* Max page rank web site : " + "\n");
			int i = 0;
			for (Entry<Integer, WebSite> entry : webs.entrySet()) {
				if (i > 9) break;
				WebSite ws = entry.getValue();
				bw.write("* " + i + "\t" + ws.name + "\t\t" + ws.id + ":" + ws.pagerank + "\n");
				++i;
			}
			
			bw.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	private int compare(double a, double b) {
		if (a > b) return 1;
		else if (a < b) return -1;
		return 0;
	}
}