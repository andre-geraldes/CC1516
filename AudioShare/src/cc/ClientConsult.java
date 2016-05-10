/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cc;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author andregeraldes
 * This class is used in Client.java to receive requests from servers
 */
public class ClientConsult implements Runnable {
    private int port;
    private int portUDP;
    private String ip;
    private String userID;
    private ServerSocket serverSocket;
    private InputStream is;
    private OutputStream os;
    private ArrayList<String> songs;
    
    private volatile boolean run = true;
    
    public ClientConsult(int port, int portUDP, InetAddress ip, String userID, ArrayList<String> s){
        this.port = port;
        this.portUDP = portUDP;
        this.ip = ip.getHostAddress();
        this.userID = userID;
        this.songs = s;
    }

    public ServerSocket getServerSocket() {
        return serverSocket;
    }
    
    @Override
    public void run() {
        try {
            this.serverSocket = new ServerSocket(this.port);
        } catch (IOException e) {
            System.err.println("[-] Port " + this.port + " occupied.");
        }
        
        while(run){
            Socket s = null;
            try {
                s = this.serverSocket.accept();
                System.out.println("[+] Connection from server at " + s.getInetAddress());
                
                is = s.getInputStream();
                os = s.getOutputStream();
                
                
                byte[] n = new byte[48 * 1024];
                this.is.read(n);

                String value = new String(n, "UTF-8");
                value = value.trim();
                System.out.println("[+] PDU received: " + value);
                
                // Split do pdu
                String[] a = value.split("\\|");
                String band = a[1];
                String song = a[2];
                
                // Verificar se existe a musica e enviar resposta
                PDU pdu = new PDU();
                if(songs.contains(song)){
                    os.write(pdu.makeResponse("FOUND(0)", 1, userID, ip, ""+portUDP));
                }
                else {
                    os.write(pdu.makeResponse("NOT_FOUND(1)", 1, userID, ip, ""+portUDP));
                }
                
                s.close();
            } catch (IOException ex) {
                //Logger.getLogger(ClientConsult.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    // Usado para matar esta thread no fim da execucao do cliente
    public void killThread() {
        run = false;
    }
    
}
