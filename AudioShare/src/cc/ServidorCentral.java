/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cc;

import static cc.Client.availableSongs;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author andregeraldes
 */
public class ServidorCentral {
    private HashMap<String, String> servers;
    public final static int DEFAULT_PORT = 4000;
    private String ip;
    private ServerSocket serverSocket;
    
    public ServidorCentral(){
        this.servers = new HashMap<String, String>();
    }
    
    public void start() throws IOException {
        InetAddress IP = InetAddress.getLocalHost();
        this.ip = IP.getHostAddress();
        
        // Criar o server socket
        try {
            this.serverSocket = new ServerSocket(DEFAULT_PORT);
            System.out.println("[+] Server Central socket created on IP " + this.ip + " port " + DEFAULT_PORT);
        } catch (IOException e) {
            System.err.println("[-] Port " + DEFAULT_PORT + " occupied.");
        }
        
        while(true){
            Socket s = this.serverSocket.accept();
            System.out.println("[+] Connection from " + s.getInetAddress());
            ServerHandler c = new ServerHandler(s, servers);
            new Thread(c).start();
        }
    }
}
