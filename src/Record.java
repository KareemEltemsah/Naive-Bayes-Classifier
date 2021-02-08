import java.util.ArrayList;

public class Record {
	ArrayList<String> features = new ArrayList<String>();
	String Class = "";
	String toBeClassified = "";

	public Record(ArrayList<String> input) {
		features = input;
	}

	public String toString() {
		String output = "";
		for (int i = 0; i < features.size() - 1; i++)
			output += String.format("%-6s", features.get(i)) + "";
		output += String.format("%-6s", features.get(features.size() - 1));
		return output;
	}
}
