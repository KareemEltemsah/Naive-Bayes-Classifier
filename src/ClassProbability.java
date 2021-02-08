
public class ClassProbability {
	String label;
	int count = 0;
	float probability;

	public ClassProbability(String l) {
		label = l;
	}

	public String toString() {
		String output = String.format("%-6s", label) + String.format("%.4f", probability);
		return output;
	}
}
