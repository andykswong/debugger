package com.jpmorgan.debugger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.Map;
import java.util.TreeMap;

/**
 * This class takes the various CSV files created from the JUnit batches and
 * rolls them up into a single XML file for processing into reports, etc.
 */
public class CsvAccumulator {

	/**
	 * The accumulated data from all the CSV files. Data is a map of packagedata
	 * keyed on the package name, returning a map of classdata keyed on the
	 * unqualified class name, returning a map of method data keyed on the
	 * method name, returning an array of integers. The array holds the number
	 * of calls at the test distance corresponding to its index. For example,
	 * the number of calls at test distance 3 will be found in the 3rd element
	 * of the array.
	 */
	private static Map<String, Map<String, Map<String, int[]>>> data = new TreeMap<String, Map<String, Map<String, int[]>>>();

	public static void main(String[] args) throws IOException {

		if (args.length == 0) {
			System.err.println("The name of the XML file to write is required as the first argument.");
		}

		String workDirName = Settings.getWorkDirName();
		File dir = new File(workDirName);
		String[] contents = dir.list();

		for (String fn : contents) {

			if (fn.matches("pea[0-9]*\\.csv")) {
				addFileContents(workDirName+fn);
			}
		}

		writeXmlFile(args[0]);
	}

	/**
	 * Add one file's contents to the accumulated data
	 * @param fn the filename to add
	 * @throws IOException
	 */
	private static void addFileContents(String fn) throws IOException {
		BufferedReader r = new BufferedReader(new FileReader(fn));

		try {
			String s = r.readLine();

			while (s != null) {
				s = r.readLine();

				if (s == null) {
					break;
				}

				String[] field = s.split(",");
				String className = field[0];
				String methodName = field[1];
				int testDistance = Integer.parseInt(field[2]);
				int numCallsAtDistance = Integer.parseInt(field[3]);

				addStat(className, methodName, testDistance, numCallsAtDistance);
			}
		} finally {
			r.close();
		}
	}

	/**
	 * @param qualifiedClassName
	 *            className
	 * @param methodName
	 * @param testDistance
	 * @param numCallsAtDistance
	 */
	private static void addStat(String qualifiedClassName, String methodName,
			int testDistance, int numCallsAtDistance) {

		String packageName;
		String className;

		if (qualifiedClassName.indexOf(".") > -1) {
			packageName = qualifiedClassName.substring(0, qualifiedClassName
					.lastIndexOf("."));
			className = qualifiedClassName.substring(qualifiedClassName
					.lastIndexOf(".") + 1);
		} else {
			packageName = "";
			className = qualifiedClassName;
		}

		Map<String, Map<String, int[]>> packageData = data.get(packageName);
		if (packageData == null) {
			packageData = new TreeMap<String, Map<String, int[]>>();
			data.put(packageName, packageData);
		}

		// Get the map of data for the specific class from the total data map
		Map<String, int[]> classData = packageData.get(className);

		if (classData == null) {
			classData = new TreeMap<String, int[]>();
			packageData.put(className, classData);
		}

		// Get the array of calls about the method from the class data
		int[] methodData = classData.get(methodName);

		if (methodData == null) {
			methodData = new int[Settings.MAX_DISTANCE + 1];
			classData.put(methodName, methodData);
		}

		// Add the number of calls to any previous number of
		// calls at that distance
		methodData[testDistance] += numCallsAtDistance;
	}

	/**
	 * @param fileName
	 * 
	 * @throws FileNotFoundException
	 */
	private static void writeXmlFile(String fileName)
			throws FileNotFoundException {
		PrintWriter pw = new PrintWriter(fileName);
		pw.println("<pea-coverage>");

		for (String packageName : data.keySet()) {
			pw.println("\t<package name=\"" + packageName + "\">");
			Map<String, Map<String, int[]>> packageData = data.get(packageName);

			for (String className : packageData.keySet()) {
				pw.println("\t\t<class name=\"" + className + "\">");
				Map<String, int[]> classData = packageData.get(className);

				for (String methodName : classData.keySet()) {
					int[] callCounts = classData.get(methodName);
					double testedness = 0.0;
					for (int i = 1; i < callCounts.length; i++) {
						testedness += (1.0*callCounts[i]/(i*i*i));
					}

					pw.println("\t\t\t<method name=\"" + xmlSafe(methodName) + "\" testedness=\"" +
							new DecimalFormat("###,###,##0.00").format(testedness) +
							"\">");

					for (int i = 1; i < callCounts.length; i++) {
						pw.println("\t\t\t\t<call depth=\"" + i + "\" count=\"" + callCounts[i] + "\"/>");
					}
					pw.println("\t\t\t</method>");
				}
				pw.println("\t\t</class>");
			}
			pw.println("\t</package>");
		}
		pw.println("</pea-coverage>");
		pw.flush();
		pw.close();
	}

	private static String xmlSafe(String s) {
		return s.replaceAll("\\<", "&lt;").replaceAll("\\>", "&gt;");
	}
}
