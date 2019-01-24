import java.io.*;
import java.net.*;
import java.nio.*;
import java.util.*;

/*
 * Primitive TCP Tagging java client for OpenViBE 1.2.x
 *
 * @author Prasanth Sasikumar & Jussi T. Lindgren / Inria
 * @date 25.Jan.2019
 * @version 0.1
 * @todo Add error handling
 */
class StimulusSender
{
Socket m_clientSocket;
DataOutputStream m_outputStream;

// Open connection to Acquisition Server TCP Tagging
boolean open(String host, Integer port) throws Exception
{
        m_clientSocket = new Socket(host, port);
        m_outputStream = new DataOutputStream(m_clientSocket.getOutputStream());

        return true;
}

// Close connection
boolean close() throws Exception
{
        m_clientSocket.close();

        return true;
}

// Send stimulation with a timestamp.
boolean send(Long stimulation, Long timestamp) throws Exception
{
        ByteBuffer b = ByteBuffer.allocate(24);
        b.order(ByteOrder.LITTLE_ENDIAN); // Assumes AS runs on LE architecture
        b.putLong(0);          // Not used
        b.putLong(stimulation); // Stimulation id
        b.putLong(timestamp);  // Timestamp: 0 = immediate

        m_outputStream.write(b.array());

        return true;
}

public static void main(String argv[]) throws Exception
{
        StimulusSender sender1 = new StimulusSender();
        StimulusSender sender2 = new StimulusSender();
        String client1 = "127.0.0.1";
        String client2 = "130.216.209.14";
        Long startMarker = 1111L;
        Long endMarker = 1234L;
        Scanner in = new Scanner(System.in);
        int choice;
        boolean isConnected = false, enableSecondClient = false;

        do {
                System.out.print("\n\nTrigger for OpenViBE.\n********************* \n\n1)Connect\n2)Start Trigger\n3)End Trigger\n4)Disconnect\n5)Enable Second Client\n6)Exit. ");
                if(isConnected) {
                        System.out.println("\nClient 1 connected at "+client1 + ".\n"
                        if(enableSecondClient){
                          System.out.println("Client 2 conneted at "+client2+".\n");
                        }
                }
                System.out.println("\n\nEnter Selection :  ");
                choice = in.nextInt();

                if (choice == 1) {

                        System.out.print("\nOpening port for Client 1");
                        sender1.open(client1, 15361);
                        System.out.print("\n############Client 1 Connected at "+ client1 + ".############\n");
                        if(enableSecondClient) {
                                System.out.print("\nOpening port for Client 2");
                                sender2.open("130.216.209.14", 15361);
                                System.out.print("\n############Client 2 Connected at "+ client2 + ".############\n");
                        }
                        isConnected = true;

                } else if (choice == 2) {
                        if(!isConnected) {
                                System.out.println("\nPlease Connect to server first.");
                                continue;
                        }
                        System.out.print("\nSending Start marker..\n ");
                        // Send identity of the event (stimulation id), time of occurrence.
                        // The preferred mechanism is to use time '0' and call the send()
                        // function immediately after each event has been rendered/played.
                        //sender.send(278L, 0L);  // Some event
                        sender1.send(startMarker, 0L); // Another one...
                        if(enableSecondClient) {
                                sender2.send(startMarker, 0L); // Another one...
                        }
                        System.out.print("\nSuccess! Look for "+startMarker+" in csv file.");
                        // etc ...

                        // To verify that the stimulations are received correctly by
                        // AS, set LogLevel to Trace in 'openvibe.conf' before running AS.
                        // Note that instead of stamp=0, AS may print the stamp it replaces
                        // the 0 with. Finally, network-acquisition.xml (in box-tutorials/)
                        // scenario can be used to display the events in Designer as combined
                        // with the signal, for example using the Generic Oscillator driver
                        // in AS.
                } else if (choice == 3) {
                        if(!isConnected) {
                                System.out.println("\nPlease Connect to server first.");
                                continue;
                        }
                        System.out.print("\nSending End marker.. \n");
                        sender1.send(endMarker, 0L);
                        if(enableSecondClient) {
                                sender2.send(endMarker, 0L); // Another one...
                        }
                        System.out.print("\nSuccess! Look for "+endMarker+" in csv file.");

                } else if (choice == 4) {
                        if(!isConnected) {
                                System.out.println("\nPlease Connect to server first.");
                                continue;
                        }
                        sender1.close();
                        if(enableSecondClient) {
                                sender2.close();
                        }
                        isConnected = false;
                        System.out.print("\nTerminated all connections");
                } else if (choice == 5) {
                        enableSecondClient = true;
                        System.out.print("\nSecond Client Enabled.\n");
                }


        } while(!(choice > 5 || choice < 1));

        if(isConnected) {
                sender1.close();
                if(enableSecondClient) {
                        sender2.close();
                }
        }
        System.out.print("\nCheers");

}
}
