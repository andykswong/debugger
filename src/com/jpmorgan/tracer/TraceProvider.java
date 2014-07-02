
package com.jpmorgan.tracer;

import com.sun.jdi.*;
import com.sun.jdi.connect.*;
import com.sun.jdi.event.*;
import com.sun.jdi.request.*;
import java.util.*;

/** Facade of the trace framework */
public class TraceProvider {

	/** Implementation as a singleton */
	protected static TraceProvider singleton = null;

	/** get the only instance of the singleton
	 * @return get the only instance of TraceProvider
	 */    
	public static TraceProvider getInstance() {
		if (singleton == null) {
			singleton = new TraceProvider();
		}
		return singleton;
	}

	/** resets the singleton
	 */    
	public static void resetTraceProvider() {
		singleton = new TraceProvider();
	}

	/** make a new Trace provider
	 * (only called by getInstance())
	 */    
	protected TraceProvider() {}

	/** The target virtual machine */
	private VirtualMachine vm = null;

	/** Classes we do not want to trace */
	private String[] excludes = {"java.*", "javax.*", "sun.*", "com.sun.*"};

	/** Classes we want to trace */
	private String[] includes = {"YourClass"};

	/** Are we using exclusion of specified classes or not */
	protected boolean exclusionMode = true;

	/** name of the thread to trace */
	protected String threadToTrace = "main";

	/** the last trace computed */
	protected TraceThread trace;

	/** the arguments of the target VM */
	protected String vmArgs = null;

	/** do we want full step trace to trace */
	protected boolean stepTrace = true;

	/** Do we want to trace every step
	 * @param v Do we want to trace every step
	 */
	public void setStepTrace(boolean v) {
		stepTrace = v;	
	}

	/** Is step trace enabled
	 * @return Is step trace enabled
	 */
	public boolean getStepTrace() {
		return stepTrace;
	}

	/** Do we exclude from trace classes that are in the exclutes variable
	 * @return is exclusion mode enabled
	 */
	public boolean isExclusionMode() {
		return exclusionMode;
	}

	/** set the Virtual machine args to trace
	 * @param args Arguments of the virtual machine to trace
	 */    
	public void setArgs(String args) {
		vmArgs = args;
	}

	/** set classe to exclude from trace (if exclusionMode)
	 * @param l Vector of class names to exclude from trace
	 */
	public void setExcludes(Vector l) {
		excludes = new String[l.size()];
		for (int i = 0; i<l.size(); i++) {
			excludes[i] = (String)l.get(i);	
		}
	}
	/** set classes to include in trace (if !exclusionMode)
	 * @param l vector of class names to trace
	 */
	public void setIncludes(Vector l) {
		includes = new String[l.size()];
		for (int i = 0; i<l.size(); i++) {
			includes[i] = (String)l.get(i);	
		}
	}
	/** get classe to exclude from trace (if exclusionMode)
	 * @return classe to exclude from trace
	 */
	public Vector getExcludes() {
		Vector Result = new Vector();
		for (int i=0; i<excludes.length; ++i) 
			Result.add(excludes[i]);
		return Result;
	}

	/** get classes to include in trace (if !exclusionMode)
	 * @return classes to include in trace
	 */
	public Vector getIncludes() {
		Vector Result = new Vector();
		for (int i=0; i<includes.length; ++i) 
			Result.add(includes[i]);
		return Result;
	}

	/** Get the list of threads names (it needs to run system)
	 * @return A vector that contains every threads names executed in the VirtualMachine to trace
	 */
	public ArrayList<String> getThreadList() {
		ArrayList<String> result = new ArrayList<String>();
		startVM(vmArgs);
		vm.setDebugTraceMode(0); // 1 pour voir ce qu'il se passe
		EventRequestManager mgr = vm.eventRequestManager();
		ThreadStartRequest tdr = mgr.createThreadStartRequest();
		tdr.setSuspendPolicy(EventRequest.SUSPEND_ALL);
		tdr.enable();
		EventQueue queue = vm.eventQueue();
		vm.resume();
		while(true) {
			try {
				EventSet eventSet = queue.remove();
				EventIterator it = eventSet.eventIterator();
				Event ev;
				while (it.hasNext()) {
					ev = it.nextEvent();
					if (ev instanceof ThreadStartEvent)
						result.add(((ThreadStartEvent)ev).thread().name());
				}
				eventSet.resume();
			}
			catch (VMDisconnectedException discExc) {
				break;
			}
			catch (Exception e) {
				System.out.println("Something is going wrong ! : "+e+"\n");
				e.printStackTrace();
			}
		}
		vm = null;
		return result;
	}
	/** set the name of the thread to trace
	 * @param name the name of the thread to trace
	 */
	public void setThreadToTrace(String name) {
		threadToTrace = name;
	}

	/** generate the execution trace
	 * @return the trace object after execution of the system
	 */
	public TraceThread generateTrace() {
		trace = new TraceThread();
		startVM(vmArgs);
		vm.setDebugTraceMode(0); // 1 pour voir ce qu'il se passe
		setEventRequests();
		EventQueue queue = vm.eventQueue();
		vm.resume();
		while(true) {
			try {
				EventSet eventSet = queue.remove();
				EventIterator it = eventSet.eventIterator();
				Event ev;
				while (it.hasNext()) {
					ev = it.nextEvent();
					System.out.println("DEBUG : " + ev);

					if (ev instanceof ThreadStartEvent)
						handleThreadStartEvent((ThreadStartEvent) ev);
					else if (ev instanceof MethodEntryEvent)
						handleMethodEntryEvent((MethodEntryEvent)ev);
					else if (ev instanceof MethodExitEvent) 
						handleMethodExitEvent((MethodExitEvent)ev);
					else if (ev instanceof StepEvent)
						handleStepEvent((StepEvent)ev);
					else if (ev instanceof VMDeathEvent)
						handleVMDeathEvent((VMDeathEvent)ev);
				}
				eventSet.resume();
			}
			catch (VMDisconnectedException discExc) {
				break;
			}
			catch (Exception e) {
				System.out.println("Something is going wrong ! : "+e+"\n");
				e.printStackTrace();
			}
		}
		return trace;
	}

	protected void handleVMDeathEvent(VMDeathEvent e) {
		trace.handleEnd();
	}

	protected void handleMethodEntryEvent(MethodEntryEvent e) {
		if (threadToTrace.equals(e.thread().name()))
			trace.handleCall(e.method());
	}

	protected void handleMethodExitEvent(MethodExitEvent e) {
		if (threadToTrace.equals(e.thread().name()))
			trace.handleRet();
	}

	protected void handleThreadStartEvent(ThreadStartEvent e) {
		if (threadToTrace.equals(e.thread().name()) && stepTrace) {
			EventRequestManager mgr = vm.eventRequestManager();
			StepRequest req = mgr.createStepRequest(e.thread(), 
					StepRequest.STEP_LINE, 
					StepRequest.STEP_INTO);
			if (exclusionMode) {
				for (int i=0; i<excludes.length; ++i) {
					req.addClassExclusionFilter(excludes[i]);
				}
			}
			else {
				for (int i=0; i<includes.length; ++i) {
					req.addClassFilter(includes[i]);
				}
			}
			req.setSuspendPolicy(EventRequest.SUSPEND_EVENT_THREAD);
			req.enable();
		}
	}

	protected void handleStepEvent(StepEvent e) {
		try {
			if (threadToTrace.equals(e.thread().name()))
				trace.handleStep(e.location().sourceName(),e.location().lineNumber());
		} catch(Exception ex) { ex.printStackTrace(); }    
	}

	// set the requested events :
	protected void setEventRequests() {
		EventRequestManager mgr = vm.eventRequestManager();

		ThreadStartRequest tdr = mgr.createThreadStartRequest();
		tdr.setSuspendPolicy(EventRequest.SUSPEND_ALL);
		tdr.enable();

		// trace method calls
		MethodEntryRequest menr = mgr.createMethodEntryRequest();
		if (exclusionMode) {
			for (int i=0; i<excludes.length; ++i) {
				menr.addClassExclusionFilter(excludes[i]);
			}
		}
		else {
			for (int i=0; i<includes.length; ++i) {
				menr.addClassFilter(includes[i]);
			}
		}
		menr.setSuspendPolicy(EventRequest.SUSPEND_EVENT_THREAD);
		menr.enable();

		// trace method exit
		MethodExitRequest mexr = mgr.createMethodExitRequest();
		if (exclusionMode) {
			for (int i=0; i<excludes.length; ++i) {
				mexr.addClassExclusionFilter(excludes[i]);
			}
		}
		else {
			for (int i=0; i<includes.length; ++i) {
				mexr.addClassFilter(includes[i]);
			}
		}
		mexr.setSuspendPolicy(EventRequest.SUSPEND_EVENT_THREAD);
		mexr.enable();
	}


	// start the VM :
	protected void startVM(String Args) {
		LaunchingConnector connector = findLaunchingConnector();
		Map arguments = connectorArguments(connector, Args);
		try {
			vm = connector.launch(arguments);
		} 
		catch (Exception e) {
			System.out.println("Unable to start VM :" + e);
			e.printStackTrace();
		}
	}
	// Find a com.sun.jdi.CommandLineLaunch connector
	LaunchingConnector findLaunchingConnector() {
		List<Connector> connectors = Bootstrap.virtualMachineManager().allConnectors();
		for(Connector connector : connectors) {
			if (connector.name().equals("com.sun.jdi.CommandLineLaunch")) {
				return (LaunchingConnector)connector;
			}
		}
		throw new Error("No launching connector");
	}

	// launching connector's arguments.
	Map connectorArguments(LaunchingConnector connector, String mainArgs) {
		Map arguments = connector.defaultArguments();
		Connector.Argument mainArg = 
				(Connector.Argument)arguments.get("main");
		if (mainArg == null) {
			throw new Error("Bad launching connector");
		}
		mainArg.setValue(mainArgs);

		// We need a VM that supports watchpoints
		Connector.Argument optionArg = 
				(Connector.Argument)arguments.get("options");
		if (optionArg == null) {
			throw new Error("Bad launching connector");
		}
		optionArg.setValue("-classic");
		return arguments;
	}

	/** Setter for property exclusionMode.
	 * @param exclusionMode New value of property exclusionMode.
	 */
	public void setExclusionMode(boolean exclusionMode) {
		this.exclusionMode = exclusionMode;
	}

	/** Getter for property threadToTrace.
	 * @return Value of property threadToTrace.
	 */
	public java.lang.String getThreadToTrace() {
		return threadToTrace;
	}

	/** prints the help message on the standard output
	 */    
	public static void printUsage() {
		System.out.println("Usage : java TraceProvider [option] <class> <args>");
		System.out.println();
		System.out.println("Options : -threadlist                  : list all threads used by the <class> program");
		System.out.println("           -thread <name> <output_file> : compute the trace of the thread named <name>");
		System.out.println("            <name>        : the name of the thread to trace");
		System.out.println("            <output_file> : the trace file to create");
		System.out.println(" <class> : the target program to trace");
		System.out.println(" <args>  : arguments of the target program\n");
		System.out.println();
		System.out.println("Exemple :");
		System.out.println("     java TraceProvider -thread main MyTrace.txt MyClass arg1 arg2");
		System.out.println("  This will trace execution of \"MyClass arg1 arg2\" and store trace");
		System.out.println("  of thread main in a file called MyTrace.txt");
		System.out.println();
		System.out.println("Remark :");
		System.out.println("   The program to trace must be reachable by the CLASSPATH environement");
		System.out.println("   variable.");
		System.exit(0);
	}

	/** Entry point for tracing programs in console mode.
	 */    
	public static void main(String[] Argv) {
		if (Argv.length < 1 || Argv[0].equals("-help")) printUsage();

		String VMargs = "";
		TraceProvider evl =TraceProvider.getInstance();

		if (Argv[0].equals("-threadlist")) {
			if (Argv.length < 2) printUsage();
			for (int i=1; i<Argv.length; i++) VMargs += Argv[i] + " ";
			ArrayList tl = evl.getThreadList();
			for (int i=0; i<tl.size(); i++)
				System.out.println(tl.get(i).toString());
			System.exit(0);
		} else {

			int ind = 0;
			String tname = "main"; // by default
			String outfile = "";
			if (Argv[0].equals("-thread")) {
				tname = Argv[1];
				ind = 3;
			}
			for (int i=ind; i<Argv.length; i++) VMargs += Argv[i] + " ";
			evl.threadToTrace = tname;
			evl.vmArgs = VMargs;
			TraceThread t = evl.generateTrace();
			//  t.getThreadTrace(tname).writeFileForContracts("Z:\\java\\ResContrats_java.csv");

			//  try {
			//  PrintStream out =  new PrintStream(new FileOutputStream("Z:\\java\\ResTrace_java.csv"));
			//  out.println(t.toString());
			//  out.close();
			//  } catch (Exception e) { e.printStackTrace(); }

			System.out.println("Done");
		}

	}
}
