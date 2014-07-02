/*
 * TraceServerMain.java
 *
 * Created on 18 juillet 2002, 05:03
 */

package Testing;

import java.io.*;

import com.jpmorgan.tracer.*;

public class TraceServerMain {
    
    /** Creates a new instance of TraceServerMain */
    public TraceServerMain() {
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
        // check number of args
        if (args.length < 2) printUsage();
        
        //check output file
        FileWriter fw = null;
        File outf = new File(args[0]);
        try {
        fw = new FileWriter(outf);
        }
        catch (Exception e) {
            System.err.println("Problem opening output file : " + args[0]);
            e.printStackTrace();
            System.exit(-1);
        }
        
        TraceProvider tp = TraceProvider.getInstance();
        tp.setThreadToTrace("ServerThread");
        tp.setStepTrace(true);
        String vmargs = "Testing.TestServerMain ";
        for(int i=1; i<args.length; i++) vmargs += args[i] + " ";
        vmargs = vmargs.trim();
        tp.setArgs(vmargs);
        
        TraceThread tt = tp.generateTrace();
        
        try {
            fw.write(tt.toString());
            fw.close();
        }
        catch (Exception e) {
            System.err.println("Problem writing output file : " + args[0]);
            e.printStackTrace();
            System.exit(-1);
        }
        
    }
    
    public static void printUsage() {
        System.err.println("Compute the execution trace of the execution of TestServerMain");
        System.err.println("USAGE java TraceServerMain [output_file] [TestServerMain_Args]");
        System.err.println();
        System.err.println("   * [output_file]         : the output file to store trace.");
        System.err.println("   * [TestServerMain_Args] : arguments of the TestServerMain");
        System.err.println("   * command to trace (type \"TestServerMain -help\" for details.");
        System.exit(-1);
    }
}
