package pagerank;

public class WebSite {
	private static double INIT_IN;
	
	public int id;
	public String name;
	public int outdegree = 0, indegree = 0;
	public double pagerank;
	public double In;
	
	public static void setParam(int N, double alpha) {
		INIT_IN = alpha / N;
	}
	
	public void initIn() {
		In = INIT_IN;
	}
}
