package com.jpmorgan.debugger;

import com.sun.jdi.Bootstrap;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.VirtualMachineManager;
import com.sun.jdi.connect.Connector.Argument;
import com.sun.jdi.connect.IllegalConnectorArgumentsException;
import com.sun.jdi.connect.ListeningConnector;

import java.io.IOException;

import java.util.List;
import java.util.Map;


/**
 * This is the class that watches the junit tests as a debugger. It makes the
 * connection and spawns a {@link DebugCallbackEventThread} to handle all the
 * callbacks from the debugged process.
 *
 * @author  Matt
 */
public class Debugger {
	/** The identifier of socket listen connector */
	private static final String SOCKET_LISTENER = "com.sun.jdi.SocketListen";
	
	private static final int PORT = 1388;

	/**
	 * Start the debugger that watches the tests
	 */
	public static void main(String[] args) throws IOException, IllegalConnectorArgumentsException, InterruptedException {
		VirtualMachineManager vmm = Bootstrap.virtualMachineManager();
		List<ListeningConnector> listeningConnectors = vmm
				.listeningConnectors();

		for (ListeningConnector listeningConnector : listeningConnectors) {

			if (SOCKET_LISTENER.equals(listeningConnector.name())) {
				System.out.println("\nlisteningConnector: " +
						listeningConnector.name());

				Map<String, ? extends Argument> argMap = listeningConnector
						.defaultArguments();
				Argument port = argMap.get("port");

				port.setValue(String.valueOf(PORT));

				for (String k : argMap.keySet()) {
					Argument a = argMap.get(k);
					System.out.println("  " + k + " : " + a);
				}

				String addr = listeningConnector.startListening(argMap);
				System.out.println("Address :" + addr);

				VirtualMachine vm = listeningConnector.accept(argMap);
				System.out.println("Connection established!");

				DebugCallbackEventThread et = new DebugCallbackEventThread(vm);
				et.start();

				vm.resume();

				et.join();
			}
		}
	}

}
