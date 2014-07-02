
package com.jpmorgan.tracer;

import java.util.*;
import com.sun.jdi.*;

import java.io.*;

// Store the trace of a thread
public class TraceThread {

	// source files on the execution path of the test case for the thread
	protected HashMap<String, SourceFileTrace> files;
	// Stack history (store every calls and ret)
	protected ArrayList<StackTrace> calls;
	// Count of executed instructions between 2 stack events
	protected IntegerList icount;

	private int cpt;

	// constructor
	public TraceThread() {
		files = new HashMap<String, SourceFileTrace>();
		calls = new ArrayList<StackTrace>();
		icount = new IntegerList();
		cpt = 0;
	}

	public String toString() {
		return stackTrace() + "\n" + lineCovered() + "\n" + contractRepartition();
	}

	public String stackTrace() {
		String result = "";
		result += "**** Method call trace ****\n";
		String indent = "";
		StackTrace st;
		for(int i=0; i<calls.size(); i++) {
			st = (StackTrace)calls.get(i);
			if (st.type == StackTrace.CALL) {
				result += indent + calls.get(i).toString()+"\n";
				indent += "| ";
			}
			else if (indent.length() > 0)
				indent = indent.substring(0, indent.length()-2);
		}
		result += "***************************\n";
		return result;
	}

	public String lineCovered() {
		String result = "";
		result += "**** Line of code covered ****\n\n";
		for (SourceFileTrace sft : files.values()) {
			result += sft.toString() + "\n";
		}
		result += "******************************\n";
		return result;
	}

	public String contractRepartition() {
		String result = "**** Contract repartition ****\n\n";
		float totalInstr = 0f;
		float totalCont = calls.size();
		float score = 0f;
		float exeCode = 0f;
		try {
			for (icount.start(); !icount.off(); icount.next()) totalInstr += icount.current();

			for (icount.start(); !icount.off(); icount.next()) {
				exeCode += icount.current();
				score += (1f/totalCont) * exeCode/totalInstr;
				result += "*";
				for (int i=0; i<icount.current(); i++) result += "-";
			}
		} catch (Exception e) { e.printStackTrace(); }
		result += "\n\n  Contracts on execution path : " + calls.size();
		result += "\n     Length of execution path : " + totalInstr;
		result += "\nContracts distribution indice : " + score;
		result += "\n****************************\n";
		return result;
	}

	public String contractCSVLine() {
		String result = "";
		float totalInstr = 0f;
		float totalCont = calls.size();
		float score = 0f;
		float exeCode = 0f;
		try {
			for (icount.start(); !icount.off(); icount.next()) totalInstr += icount.current();

			for (icount.start(); !icount.off(); icount.next()) {
				exeCode += icount.current();
				score += (1f/totalCont) * exeCode/totalInstr;
			}
		} catch (Exception e) { e.printStackTrace(); }
		result += totalCont+";"+totalInstr+";"+score;
		return result.replace('.', ',');
	}

	public void writeFileForContracts(String fileName) {
		float totalInstr = 0f;
		float totalCont = calls.size();
		float score = 0f;
		float exeCode = 0f;
		float exeContract = 0f;
		try {
			PrintStream out =  new PrintStream(new FileOutputStream(fileName));
			out.println("#Instructions executed;#contracts executed;% code executed along exec path; % contracts executed along exec path");

			for (icount.start(); !icount.off(); icount.next()) totalInstr += icount.current();
			String str;
			for (icount.start(); !icount.off(); icount.next()) {
				exeCode += icount.current();
				exeContract++;
				str = exeCode + ";" + exeContract + ";" + (exeCode*100)/totalInstr + ";" + (exeContract * 100)/calls.size();
				out.println(str.replace('.',','));
			}
			out.close();
		} catch (Exception e) { e.printStackTrace(); }
	}

	public void handleStep(String sPath, int line) {
		cpt++;
		SourceFileTrace trace = (SourceFileTrace)files.get(sPath);
		if (trace == null)
			trace = new SourceFileTrace(sPath,sPath);
		trace.addLine(line);
	}

	public void handleCall(Method m) {
		icount.pushBack(cpt);
		cpt = 0;
		try {
			SourceFileTrace trace = (SourceFileTrace)files.get(m.location().sourceName());
			if (trace == null) {
				trace = new SourceFileTrace(m.location().sourceName(),m.location().sourceName());
			}
			files.put(m.location().sourceName(), trace);
			calls.add(new StackTrace(StackTrace.CALL, m.name(), m.declaringType().name()));
		}
		catch (Exception e) { e.printStackTrace(); }
	}

	public void handleRet() {
		icount.pushBack(cpt);
		cpt = 0;
		calls.add(new StackTrace(StackTrace.RET));
	}

	public void handleEnd() {
		icount.pushBack(cpt);
	}

}

/**
 * Represent the trace for a source file 
 */
class SourceFileTrace {
	// name of the source file
	protected String name;
	// Path to the source file
	protected String path;
	// Executed lines in the source file
	protected IntegerList lines;

	// returns the name of the class
	public String getName() {
		return name;
	}
	// return the path to the source file
	public String getPath() {
		return path;
	}

	public String toString() {
		return path + " : " + lines.toString();
	}

	// constructor
	public SourceFileTrace(String n, String p) {
		name = n;
		path = p;
		lines = new IntegerList();
	}
	// check if 2 traces are equals (same class and same executed lines
	public boolean equals(SourceFileTrace other) {
		if (this.name.equals(other.name) && 
				this.path.equals(other.path) && 
				this.lines.size() == other.lines.size()) {
			try {
				for (lines.start(); !lines.off(); lines.next()) 
					if (!other.lines.contains(lines.current())) return false;
			} catch (Exception e) { e.printStackTrace(); }
			return true;
		}
		else return false;
	}
	// add an executed line to the class trace
	public void addLine(int ln) {
		if (!lines.contains(ln)) lines.pushSorted(ln);
	}
}   

class StackTrace {
	public static final int CALL = 1;
	public static final int RET = 2;

	public int type; // is it a CALL or a RET
	public String mName; // name of the method (for call)
	public String cName; // name of the class (for call)

	public StackTrace(int frame_type, String name, String className) {
		type = frame_type;
		mName = name;
		cName = className;
	}

	public StackTrace(int frame_type) {
		type = frame_type;
	}

	public String toString() {
		if (type == StackTrace.RET)
			return "RET";
		return "CALL " + cName + "." + mName;
	}
}
