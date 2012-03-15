package crosslink.tools;

public class ResultSetAnalyser {
	ResultSet resultFirst = new ResultSet();
	ResultSet resultSecond = new ResultSet();
	
	public void compare(String rsFile1, String rsFile2) {
		resultFirst.load(rsFile1);
		resultSecond.load(rsFile2);
		
		resultFirst.compareTo(resultSecond);
	}
	
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		ResultSetAnalyser analyser = new ResultSetAnalyser();
		analyser.compare(args[0], args[1]);
	}

}
