/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cc;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author andregeraldes
 */
public class ClientUDP implements Runnable {
    private int portUDP;
    private String ip;
    private String userID;
    
    public ClientUDP(int portUDP, String ip, String userID){
        this.portUDP = portUDP;
        this.ip = ip;
        this.userID = userID;
    }
    
    @Override
    public void run() {
        DatagramSocket serverSocket = null;
        try {
            serverSocket = new DatagramSocket(this.portUDP);
        } catch (SocketException ex) {
            Logger.getLogger(ClientUDP.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        byte[] receiveData = new byte[48 * 1024];
        byte[] sendData = new byte[48 * 1024];
        
        while(true) {
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            try {
                serverSocket.receive(receivePacket);
            } catch (IOException ex) {
                Logger.getLogger(ClientUDP.class.getName()).log(Level.SEVERE, null, ex);
            }
            String sentence = new String( receivePacket.getData());
            System.out.println("[+] Probe request received: " + sentence);
            InetAddress IPAddress = receivePacket.getAddress();
            int port = receivePacket.getPort();
            
            PDU p = new PDU();
            
            if(sentence.charAt(2) == '4'){
                String timeStamp = new SimpleDateFormat("HH.mm.ss.dd.MM.yyyy").format(new Date());
                sendData = p.makeProbeResponse(timeStamp);
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
                try {
                    serverSocket.send(sendPacket);
                } catch (IOException ex) {
                    Logger.getLogger(ClientUDP.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
}
