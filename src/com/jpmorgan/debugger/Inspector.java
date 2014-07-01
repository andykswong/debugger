package com.jpmorgan.debugger;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

/**
 * This class tabulates the method executions as test cases are run. It streams
 * output about method executions to a file in a work directory, whose path name
 * is supplied in a system property. Note that in a given batch, this class may
 * be started by multiple VM's depending on the fork mode, but for any given
 * instance of the VM, there will be one and only one output CSV file for the
 * call statistics. These will be accumulated by {@link CsvAccumulator}.
 */
public class Inspector {

	/** The output stream we're writing to */
	private static BufferedWriter outStream;

	/** The output csv log print writer */
	private static PrintWriter csv;

	/** The name of the work file */
	private static final String WORK_FILE_NAME;

	/**
	 * The data. The map is keyed by classname concatenated with method name,
	 * using a vertical bar as delimiter. The value of the map is an array of
	 * data. The index of the array is the test distance, and the value of the
	 * array is the number of times the method was called at that test distance.
	 */
	private static Map<String, int[]> data = new HashMap<String, int[]>();

	// Static initializer
	static {
		String workDirName = Settings.getWorkDirName();

		WORK_FILE_NAME = workDirName + "report" + System.currentTimeMillis()
				+ ".csv";

		System.out.println("Work file: " + WORK_FILE_NAME);

		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {

				try {
					outStream = new BufferedWriter(new FileWriter(
							WORK_FILE_NAME, false));
				} catch (FileNotFoundException e) {
					throw new DebuggerRuntimeException("Unable to create log file "
							+ WORK_FILE_NAME, e);
				} catch (IOException e) {
					throw new DebuggerRuntimeException("Unable to create log file "
							+ WORK_FILE_NAME, e);
				}

				csv = new PrintWriter(outStream, true);

				Set<Entry<String, int[]>> dataEntries = data.entrySet();

				for (Entry<String, int[]> entry : dataEntries) {
					String k = entry.getKey();
					int[] counts = entry.getValue();

					for (int i = 0; i < counts.length; i++) {

						if (counts[i] > 0) {
							csv.print(k);
							csv.print(",");
							csv.print(i);
							csv.print(",");
							csv.println(counts[i]);
						}
					}
				}

				csv.flush();
				csv.close();
			}
		});
	}

	/** A cache of class types from names */
	private static final Map<String, Boolean> classCache = new HashMap<String, Boolean>();

	/**
	 * Inspect the stack. Find out how far down the stack the JUnit test is, and
	 * log it in the in-memory database.
	 * 
	 * @param stack
	 *            The stack
	 */
	public static void inspect(List<String> stack) {
		String stackFrame = stack.get(0);
		String className = getClassNameFromStackFrame(stackFrame);
		String methodName = getMethodNameFromStackFrame(stackFrame);

		int testDistance = getDepthOfFirstJunitTest(stack, className);

		System.out.println("class " + className + ", method "
				+ methodName);

		if (testDistance > 0) {
			addCall(className, methodName, testDistance);
		}

	}

	/**
	 * Log a call in the in-memory database, along with its test distance
	 */
	private static void addCall(String classNameUnderTest,
			String methodNameUnderTest, int testDistance) {
		int[] calls = data.get(classNameUnderTest + "," + methodNameUnderTest);

		if (calls == null) {
			calls = new int[Settings.MAX_DISTANCE + 1];
			data.put(classNameUnderTest + "," + methodNameUnderTest, calls);
		}

		if (testDistance > Settings.MAX_DISTANCE) {
			testDistance = Settings.MAX_DISTANCE;
		}
		calls[testDistance]++;
	}

	/**
	 * Get the class name from a specific stack frame string
	 * 
	 * @param stackFrame
	 *            the stack frame string
	 * 
	 * @return the class name of the class being called
	 */
	private static String getClassNameFromStackFrame(String stackFrame) {
		int p = stackFrame.indexOf("(");
		String classAndMethod = stackFrame.substring(0, p);
		int p2 = classAndMethod.lastIndexOf(".");
		String className = classAndMethod.substring(0, p2);

		return className;
	}

	/**
	 * Determine the depth to the first junit test class
	 * 
	 * @param stack
	 *            the stack trace
	 * @param firstClass
	 *            the name of the first class in the stack (already parsed from
	 *            first entry in the stack)
	 * 
	 * @return the number of frames to go through before finding a test case
	 */
	private static int getDepthOfFirstJunitTest(List<String> stack,
			String firstClass) {
		int result = 0;
		boolean foundTestCase = false;

		if (firstClass.startsWith("junit.framework")) {
			return -1;
		}

		if (isClassTestCase(firstClass)) {
			return 0;
		}

		for (String stackFrame : stack) {
			String cn = getClassNameFromStackFrame(stackFrame);
			Boolean isTestCase = isClassTestCase(cn);

			if (isTestCase.booleanValue()) {
				String mn = getMethodNameFromStackFrame(stackFrame);

				if (mn.startsWith("test")) {
					foundTestCase = true;

					break;
				}
			}
			result++;
		}

		if (!foundTestCase) {
			result = -1;
		}

		// if (result > -1) {
		// System.out
		// .println("=====================================================\n"
		// + result + " frames to test case");
		// for (String string : stack) {
		// System.out.println(string);
		// }
		// }
		return result;
	}

	/**
	 * Get the unqualified method name from a specific stack frame string
	 * @param stackFrame  the stack frame string
	 * @return the unqualified name of the class being called
	 */
	private static String getMethodNameFromStackFrame(String stackFrame) {
		int p = stackFrame.indexOf("(");
		String classAndMethod = stackFrame.substring(0, p);
		int p2 = classAndMethod.lastIndexOf(".");
		String methodName = classAndMethod.substring(p2 + 1);

		return methodName;
	}

	/**
	 * Get the value of the class test case property
	 * @param cn class name
	 * @return the value of the class test case property
	 */
	private static boolean isClassTestCase(String cn) {
		Boolean isTestCase = classCache.get(cn);

		if (isTestCase == null) {

			try {
				Class<?> c = Class.forName(cn);
				isTestCase = junit.framework.TestCase.class
						.isAssignableFrom(c);
			} catch (ClassNotFoundException e) {
				System.err.println("Can't find " + cn + " in classpath");
				isTestCase = false;
			}

			classCache.put(cn, isTestCase);
		}

		return isTestCase;
	}
}
