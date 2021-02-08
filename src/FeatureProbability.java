import java.util.HashMap;

public class FeatureProbability {
	String value = "";
	HashMap<String, Float> classDependency = new HashMap<String, Float>();

	public FeatureProbability(String v) {
		value = v;
	}

	public String toString() {
		String output = value + " ";
		for (Float f : classDependency.values())
			output += String.format("%-6s", String.format("%.4f", f)) + " ";
		return output;
	}
}
