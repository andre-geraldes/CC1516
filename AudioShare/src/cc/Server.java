/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cc;

import static cc.Client.availableSongs;
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
    private HashMap<String, User> users;
    // Socket para conectar ao servidor central:
    private Socket clientSocket;
    
    public final static int DEFAULT_PORT = 3000;
    
    public Server(){
        this.port = DEFAULT_PORT;
        this.users = new HashMap<String, User>();
    }
    
    public Server(int port){
        this.port = port;
    }

    public HashMap<String, User> getUsers() {
        return users;
    }

    public void setUsers(HashMap<String, User> users) {
        this.users = users;
    }
    
    public void start() throws IOException, InterruptedException {
        InetAddress IP = InetAddress.getLocalHost();
        this.ip = IP.getHostAddress();
        
        // Criar o server socket
        
        try {
            this.serverSocket = new ServerSocket(this.port);
            System.out.println("[+] Server socket created on IP " + this.ip + " port " + this.port);
        } catch (IOException e) {
            System.err.println("[-] Port " + this.port + " occupied.");
        }
        
        // Registar servidor no central
        // Se estiver na mesma maquina
        InetAddress ipServer = InetAddress.getLocalHost();
        String ip = ipServer.getHostAddress(); //Alterar para o ip do servidor
        //String ip = "192.168.204.1";
        // Conectar com o servidor
        this.clientSocket = new Socket(ip, 4000);
        DataOutputStream outToServer = new DataOutputStream(this.clientSocket.getOutputStream());
        PDU p = new PDU();
        outToServer.write(p.makeRegister('i', "server", IP.getHostAddress(), String.valueOf(this.port)));
        
        // Esperar confirmação do registo
        Thread.sleep(1000); //Esperar 1 segundo pela resposta
        //Receber resposta
        InputStream is = clientSocket.getInputStream();
        byte[] n = new byte[256];
        is.read(n);
        String value = new String(n, "UTF-8");
        value = value.trim();
        boolean ok = false;
        if(value.contains("ok")){
            System.out.println("[+] Server registed!");
            ok = true;
        }
        else
            System.out.println("[-] Server not registed!");
        
        if(ok){
            while(true){
                Socket s = this.serverSocket.accept();
                System.out.println("[+] Connection from " + s.getInetAddress());
                ClientHandler c = new ClientHandler(s, users, this.clientSocket);
                new Thread(c).start();
            }
        }
    }
}
