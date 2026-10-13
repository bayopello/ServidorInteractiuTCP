package aar;

import java.io.*;
import java.net.*;

public class ClienteUDPMulticast {
    private int groupPort = 0;
    private InetSocketAddress groupAddress = null;
    
    public ClienteUDPMulticast(String IP, int port) {
        this.groupPort = port;
        this.groupAddress = new InetSocketAddress(IP, groupPort);
    }
    
    public void execute() {
        MulticastSocket socket = null;
        try {
            socket = new MulticastSocket(groupPort);
            socket.joinGroup(groupAddress, null);
        } catch (IOException e) {
            System.err.println("Could not connect to the multicast group");
            System.exit(1);
        }
          
        DatagramPacket packet = null;
        
        for (int i = 0; i < 10; i++) {
            byte[] buf = new byte[256];
            packet = new DatagramPacket(buf, buf.length);
            try {
                socket.receive(packet);
            } catch (IOException e) {
                System.err.println("Error when receiving");
                System.exit(1);
             }
            String received = new String(packet.getData(), 0, packet.getLength());
            System.out.println("Fecha actual: " + received);
		}
        try {
            socket.leaveGroup(groupAddress, null);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        socket.close();
    } 
   
}