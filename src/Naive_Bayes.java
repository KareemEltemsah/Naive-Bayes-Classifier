import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.Scanner;

public class Naive_Bayes {
	public static void main(String[] args) throws IOException, InterruptedException {
		BufferedReader reader = new BufferedReader(new FileReader("car.data.txt"));
		String line = new String();
		ArrayList<Record> records = new ArrayList<Record>();
		while ((line = reader.readLine()) != null) {
			String[] temp = line.split(",");// splitting record content
			ArrayList<String> values = new ArrayList<String>();

			for (int i = 0; i < temp.length - 1; i++)
				values.add(temp[i]);

			Record r = new Record(values);
			r.Class = temp[temp.length - 1];
			records.add(r);
		}
		reader.close();
		ArrayList<ArrayList<String>> featuresValues = getFeaturesValues(records);
		ArrayList<Record> trainingSet = new ArrayList<Record>();
		ArrayList<Record> testSet = new ArrayList<Record>();
		devideRecords(records, trainingSet, testSet);
		
		// classes probabilities
		ArrayList<ClassProbability> classesProbabilities = new ArrayList<ClassProbability>();
		// features probabilities
		ArrayList<ArrayList<FeatureProbability>> featuresProbabilities = new ArrayList<ArrayList<FeatureProbability>>();
		learnFromTrainingSet(trainingSet, featuresValues, classesProbabilities, featuresProbabilities);
		
		System.out.println("Check the output in the files");
		System.setOut(new PrintStream("learningPhase.txt"));
		printLearningResult(classesProbabilities, featuresProbabilities);
		System.setOut(new PrintStream("testingResult.txt"));
		testingClassifier(testSet, classesProbabilities, featuresProbabilities);
	}

	public static ArrayList<Record> devideRecords(ArrayList<Record> records, ArrayList<Record> trainingSet,
			ArrayList<Record> testSet) {
		Random r = new Random();
		int trainingSetSize = records.size() * 3 / 4;
		while (trainingSetSize-- > 0) {
			int randIndex = r.nextInt(records.size());
			trainingSet.add(records.get(randIndex));
			records.remove(randIndex);
		}

		while (records.size() > 0) {// random numbers here are just shuffling the test set
			int randIndex = r.nextInt(records.size());
			testSet.add(records.get(randIndex));
			records.remove(randIndex);
		}

		return trainingSet;
	}

	public static ArrayList<ArrayList<String>> getFeaturesValues(ArrayList<Record> trainingSet) {
		ArrayList<ArrayList<String>> featuresValues = new ArrayList<ArrayList<String>>();
		int featuresN = trainingSet.get(0).features.size();
		while (featuresValues.size() < featuresN + 1)
			featuresValues.add(new ArrayList<String>());

		for (int i = 0; i < trainingSet.size(); i++) {
			for (int j = 0; j < featuresN; j++) {
				if (!featuresValues.get(j).contains(trainingSet.get(i).features.get(j)))
					featuresValues.get(j).add(trainingSet.get(i).features.get(j));
			}
			// adding classes values too
			if (!featuresValues.get(featuresN).contains(trainingSet.get(i).Class))
				featuresValues.get(featuresN).add(trainingSet.get(i).Class);
		}
		return featuresValues;
	}

	public static void learnFromTrainingSet(ArrayList<Record> trainingSet, ArrayList<ArrayList<String>> featuresValues,
			ArrayList<ClassProbability> classesProbabilities,
			ArrayList<ArrayList<FeatureProbability>> featuresProbabilities) {
		int featuresValuesN = 0; // count features to add 1 for each to avoid zero probability
		for (int i = 0; i < featuresValues.size(); i++) {
			for (int j = 0; j < featuresValues.get(i).size(); j++) {
				if (i < featuresValues.size() - 1) {
					if (j == 0)
						featuresProbabilities.add(new ArrayList<FeatureProbability>());
					featuresProbabilities.get(i).add(new FeatureProbability(featuresValues.get(i).get(j)));
					featuresValuesN++;
				} else
					classesProbabilities.add(new ClassProbability(featuresValues.get(i).get(j)));
			}
		}
		for (int i = 0; i < classesProbabilities.size(); i++) {
			int sum = 0;
			for (int j = 0; j < trainingSet.size(); j++) {
				if (trainingSet.get(j).Class.equals(classesProbabilities.get(i).label))
					sum++;
			}
			classesProbabilities.get(i).count = sum;
			classesProbabilities.get(i).probability = (float) sum / trainingSet.size();
		}
		for (int i = 0; i < featuresProbabilities.size(); i++) {// column index
			for (int j = 0; j < featuresProbabilities.get(i).size(); j++) {// values index
				for (int k = 0; k < classesProbabilities.size(); k++) {// class index
					float sum = 0;
					for (int row = 0; row < trainingSet.size(); row++) {// records index
						if (trainingSet.get(row).features.get(i).equals(featuresProbabilities.get(i).get(j).value)
								&& trainingSet.get(row).Class.equals(classesProbabilities.get(k).label)) {
							sum++;
						}
					}
					featuresProbabilities.get(i).get(j).classDependency.put(classesProbabilities.get(k).label,
							(sum + 1) / (classesProbabilities.get(k).count + (featuresProbabilities.get(i).size())));
					// +1 to avoid zero probability, features n * classes
					// to add at least 1 tuple for each class
				}
			}
		}
	}

	public static void printLearningResult(ArrayList<ClassProbability> classesProbabilities,
			ArrayList<ArrayList<FeatureProbability>> featuresProbabilities) {
		System.out.println("Learning Phase:\n");
		String tableHeader = "";
		for (int i = 0; i < classesProbabilities.size(); i++) {
			tableHeader += (String.format("%-8s", classesProbabilities.get(i).label));
		}

		for (int i = 0; i < featuresProbabilities.size(); i++) {
			System.out.println(String.format("%-10s", "Feature:" + (i + 1)) + tableHeader);
			for (int j = 0; j < featuresProbabilities.get(i).size(); j++) {
				System.out.print(String.format("%-10s", featuresProbabilities.get(i).get(j).value));
				for (int k = 0; k < classesProbabilities.size(); k++) {
					System.out.print(String.format("%-8s",
							String.format("%.4f", featuresProbabilities.get(i).get(j).classDependency
									.get(classesProbabilities.get(k).label))));
				}
				System.out.println();
			}
			System.out.println();
		}
		System.out.println("Classes Probabilities:");
		for (int i = 0; i < classesProbabilities.size(); i++)
			System.out.println(classesProbabilities.get(i));
	}

	public static void testingClassifier(ArrayList<Record> testSet, ArrayList<ClassProbability> classesProbabilities,
			ArrayList<ArrayList<FeatureProbability>> featuresProbabilities) {
		System.out.println("Testing Phase:\n");
		System.out.println(String.format("%50s", "classified") + String.format("%12s", "real class"));
		for (int i = 0; i < testSet.size(); i++) {// records index
			float max = 0;
			int classIndex = 0;
			System.out.print(testSet.get(i));
			for (int j = 0; j < classesProbabilities.size(); j++) {// classes index
				float product = 1;
				for (int k = 0; k < testSet.get(i).features.size(); k++) {// features index
					product *= featuresProbabilities.get(k).get(
							findIndex(testSet.get(i).features.get(k), featuresProbabilities.get(k))).classDependency
									.get(classesProbabilities.get(j).label);

				}
				product *= classesProbabilities.get(j).probability;
				// System.out.print(" P(X|" + classesProbabilities.get(j).label + ")="
				// + String.format("%-11s", String.format("%.9f", product)));
				if (product > max) {
					max = product;
					classIndex = j;
				}
			}
			testSet.get(i).toBeClassified = classesProbabilities.get(classIndex).label;
			System.out.println(" ->   " + String.format("%-10s", testSet.get(i).toBeClassified) + "  "
					+ String.format("%-6s", testSet.get(i).Class));
		}
		int n = 0;
		for (int i = 0; i < testSet.size(); i++) {
			if (testSet.get(i).Class.equals(testSet.get(i).toBeClassified))
				n++;
		}
		float accuracy = ((float) n / testSet.size());
		System.out.println("\nAccuracy = " + n + "/" + testSet.size() + " = " + String.format("%.4f", accuracy));
		System.out.println("Accuracy = " + String.format("%.2f", accuracy * 100) + "%");

	}

	public static int findIndex(String value, ArrayList<FeatureProbability> featuresProbabilities) {
		for (int i = 0; i < featuresProbabilities.size(); i++) {
			if (featuresProbabilities.get(i).value.equals(value))
				return i;
		}
		return -1;
	}
}
