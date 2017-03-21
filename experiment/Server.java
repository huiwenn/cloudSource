/* CloudSource
 * Sophia Sun & Hanae Yaskawa
 * server.java
 */

        
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

// for startSender
import java.io.PrintWriter;

//for methond getMyIP
import java.net.InetAddress;
import java.net.UnknownHostException;
        
//for methond executeCommand
import java.io.BufferedReader;
import java.io.InputStreamReader;

import java.util.*;


public class Server implements Experiment {
        
    public Server() {}


    /*------------------------------------------
      tells @param senderIP to start a pathload sender.
      then start a pathload receiver on this machine
      access pathload.log, extract useful information, return.

      MAYBE GOOD?
    -----------------------------------------*/
    public String experientOneRound( String senderIP ){

        String data = "";

        try {
            Registry registry = LocateRegistry.getRegistry(host);
            Experiment sender = (Experiment) registry.lookup(senderIP); //get sender RMI object
            boolean response = sender.startSender();
            
            if (response){ //Sender started is true

                //make sure there is no previous data in log
                Process p1 = Runtime.getRuntime().exec("rm pathload_1.3.2/pathload.log");
                p1.waitFor();

                // start reciever
                Process p = Runtime.getRuntime().exec("./pathload_1.3.2/pathload_rcv -s "+ senderIP);
                p.waitFor();

                // parse log file
                Scanner reader = new Scanner(new File("pathload_1.3.2/pathload.log"));

                while (reader.hasNext()) {
                    String line = reader.nextLine();
                    if (line.toLowerCase().contains("result")) {
                        data = reader.nextLine().split(": ")[1]; //get the useful line.
                        break;
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Receiver exception: " + e.toString());
            e.printStackTrace();
        }

        return data;
    }


    /*------------------------------------------
      Starts a Pathload sender on this machine.
      GOOD
    -----------------------------------------*/
    public boolean startSender() {

        String send = executeCommand("./pathload_1.3.2/pathload_snd");

        PrintWriter log = new PrintWriter("pathload_sender_log.txt");
        log.println(send);

        return true;
    }

    /*------------------------------------------
      gets the ip of this machine.
    -----------------------------------------*/
    private static String getMyIP(){
        InetAddress ip;
        try {
            ip = InetAddress.getLocalHost();
            //System.out.println("Your current IP address : " + ip);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return ip;
    }



    /*------------------------------------------
      uses java Runtime to execute a shell command,
      waits for the process to finish
      returms the logs after process finishes.
    -----------------------------------------*/
    private static String executeCommandWait(String command) {

        StringBuffer output = new StringBuffer();

        Process p;
        try {

            System.out.println("started excecuting: " + command);
            BufferedReader reader =
                            new BufferedReader(new InputStreamReader(p.getInputStream()));

                        String line = "";
            while ((line = reader.readLine())!= null) {
                System.out.println(line);
                output.append(line + "\n");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return output.toString();
    }


    /*------------------------------------------
      uses java Runtime to execute a shell command,
      returms the logs after process finishes.

      TODO: do we keep //p.waitFor(); ?
    -----------------------------------------*/
    private static String executeCommand(String command) {

        StringBuffer output = new StringBuffer();

        Process p;
        try {
            p = Runtime.getRuntime().exec(command);
            System.out.println("started excecuting: " + command);
            BufferedReader reader =
                            new BufferedReader(new InputStreamReader(p.getInputStream()));

                        String line = "";
            while ((line = reader.readLine())!= null) {
                System.out.println(line);
                output.append(line + "\n");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return output.toString();
    }
        
    public static void main(String args[]) {

        try {
            Server obj = new Server();
            Experiment stub = (Experiment) UnicastRemoteObject.exportObject(obj, 0);

            // Bind the remote object's stub in the registry
            Registry registry = LocateRegistry.getRegistry();
            myIP = getMyIP();

            //diferentiate machines by local registry (ip) (identified at lookup), not by name
            //because its the same application
            registry.bind("CloudSource", stub);

            System.err.println("Server ready");

        } catch (Exception e) {
            System.err.println("Server exception: " + e.toString());
            e.printStackTrace();
        }
    }
}