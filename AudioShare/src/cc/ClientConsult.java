/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cc;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author andregeraldes
 */
public class ClientConsult implements Runnable {
    private int port;
    private ServerSocket serverSocket;
    private InputStream is;
    private OutputStream os;
    
    public ClientConsult(int port){
        this.port = port;
    }
    
    @Override
    public void run() {
        try {
            this.serverSocket = new ServerSocket(this.port);
        } catch (IOException e) {
            System.err.println("[-] Port " + this.port + " occupied.");
        }
        
        while(true){
            Socket s = null;
            try {
                s = this.serverSocket.accept();
                System.out.println("[+] Connection from " + s.getInetAddress());
                is = s.getInputStream();
                os = s.getOutputStream();
                
                byte[] n = new byte[48 * 1024];
                this.is.read(n);

                String value = new String(n, "UTF-8");
                value = value.trim();
                System.out.println("[+] PDU received: " + value);
                
            } catch (IOException ex) {
                Logger.getLogger(ClientConsult.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
}
