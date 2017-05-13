package search;

import java.util.Comparator;

import org.apache.lucene.search.ScoreDoc;

public class ScoreComparator implements Comparator<ScoreDoc> {
	
	@Override
	public int compare(ScoreDoc s1, ScoreDoc s2) {
		if (s1.score > s2.score) 
			return -1;
		else if (s1.score < s2.score)
			return 1;
		return 0;
   }
}
