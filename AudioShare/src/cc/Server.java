/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cc;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.*;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author andregeraldes
 */
public class Server {
    private int port;
    private String ip;
    private ServerSocket serverSocket;
    private Socket socket;
    private HashMap<String, User> users;
    
    public final static int DEFAULT_PORT = 3000;
    
    public Server(){
        this.port = DEFAULT_PORT;
        this.users = new HashMap<String, User>();
    }
    
    public Server(int port){
        this.port = port;
    }
    
    public void start() throws IOException{
        InetAddress IP = InetAddress.getLocalHost();
        this.ip = IP.getHostAddress();
        
        try {
            this.serverSocket = new ServerSocket(this.port);
            System.out.println("[+] Server socket created on IP " + this.ip + " port " + this.port);
        } catch (IOException e) {
            System.err.println("[-] Port " + this.port + " occupied.");
        }
        
        while(true){
            Socket s = this.serverSocket.accept();
            System.out.println("[+] Connection from " + s.getInetAddress());
            ClientHandler c = new ClientHandler(s, users);
            new Thread(c).start();
        }
    }
}
