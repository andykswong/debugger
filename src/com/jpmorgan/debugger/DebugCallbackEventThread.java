package com.jpmorgan.debugger;

import com.sun.jdi.Location;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.EventIterator;
import com.sun.jdi.event.EventQueue;
import com.sun.jdi.event.EventSet;
import com.sun.jdi.event.ExceptionEvent;
import com.sun.jdi.event.MethodEntryEvent;
import com.sun.jdi.event.MethodExitEvent;
import com.sun.jdi.event.VMDeathEvent;
import com.sun.jdi.event.VMDisconnectEvent;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.EventRequestManager;
import com.sun.jdi.request.ExceptionRequest;
import com.sun.jdi.request.MethodEntryRequest;
import com.sun.jdi.request.MethodExitRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class DebugCallbackEventThread extends Thread {

	private static final String[] excludes = {
		"java.*", "javax.*", "sun.*", "com.sun.*"
	};

	private static Map<String, List<String>> stacks = new HashMap<String, List<String>>();

	int depth;

	private boolean connected = true;

	private VirtualMachine vm;

	public DebugCallbackEventThread(VirtualMachine vm) {
		this.vm = vm;

		EventRequestManager erm = vm.eventRequestManager();
		MethodEntryRequest menr = erm.createMethodEntryRequest();

		for (int i = 0; i < excludes.length; ++i) {
			menr.addClassExclusionFilter(excludes[i]);
		}

		menr.setSuspendPolicy(EventRequest.SUSPEND_NONE);
		menr.enable();

		MethodExitRequest mexr = erm.createMethodExitRequest();

		for (int i = 0; i < excludes.length; ++i) {
			mexr.addClassExclusionFilter(excludes[i]);
		}

		mexr.setSuspendPolicy(EventRequest.SUSPEND_NONE);
		mexr.enable();

		ExceptionRequest exr = erm.createExceptionRequest(null, true, true);

		for (int i = 0; i < excludes.length; ++i) {
			exr.addClassExclusionFilter(excludes[i]);
		}

		exr.setSuspendPolicy(EventRequest.SUSPEND_NONE);
		exr.enable();
	}

	/**
	 * Run the event handling thread. As long as we are connected, get event
	 * sets off the queue and dispatch the events within them.
	 */
	public void run() {
		EventQueue queue = vm.eventQueue();

		while (connected) {

			try {
				EventSet eventSet = queue.remove();
				EventIterator it = eventSet.eventIterator();

				while (it.hasNext()) {
					handleEvent(it.nextEvent());
				}

				eventSet.resume();
			} catch (InterruptedException exc) {
				// Ignore
			}
		}
	}

	private void handleEvent(Event event) {
		if (event instanceof MethodEntryEvent) {
			methodEntryEvent((MethodEntryEvent) event);
		} else if (event instanceof MethodExitEvent) {
			methodExitEvent((MethodExitEvent) event);
		} else if (event instanceof ExceptionEvent) {
			exceptionEvent((ExceptionEvent) event);
		} else if (event instanceof VMDeathEvent) {
			connected = false;
		} else if (event instanceof VMDisconnectEvent) {
			connected = false;
		}
	}
	
	/**
	 * Get the stack for the specified thread
	 * @param   thread  the thread for which we need the stack
	 * @return  the stack
	 */
	private List<String> getStack(ThreadReference thread) {
		List<String> stack = new ArrayList<String>();

		try {
			String threadName = thread.name();
			stack = stacks.get(threadName);

			if (stack == null) {
				stack = new ArrayList<String>();
				stacks.put(threadName, stack);
			}
		} catch (VMDisconnectedException e) {

			// The host process is done so our connection is gone
			connected = false;
		}

		return stack;
	}

	private void methodEntryEvent(MethodEntryEvent event) {
		List<String> stack = getStack(event.thread());
		stack.add(0, event.method().toString());
		Inspector.inspect(stack);
	}

	private void methodExitEvent(MethodExitEvent event) {
		List<String> stack = getStack(event.thread());

		if (stack.size() > 0)
			stack.remove(0);
	}

	private void exceptionEvent(ExceptionEvent event) {
		List<String> stack = getStack(event.thread());

		Location catchLocation = event.catchLocation();
		String popTo = catchLocation.method().toString();

		if (stack.size() > 0) {
			do {
				stack.remove(0);
			} while ((stack.size() > 0) && !stack.get(0).equals(popTo));
		}
	}
}
