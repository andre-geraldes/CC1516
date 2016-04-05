/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cc;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.HashMap;

/**
 *
 * @author andregeraldes
 */
public class ClientHandler implements Runnable {
    private Socket s;
    private HashMap users;   
    private InputStream is;
    private OutputStream os;
    
    public ClientHandler(Socket s, HashMap users){
        this.s = s;
        this.users = users;
    }
    
    @Override
    public void run() {
        try{
            is = this.s.getInputStream();
            os = this.s.getOutputStream();
            
            String user = "";
            boolean exit = false;

            byte[] n = new byte[48 * 1024];
            this.is.read(n);
            
            String value = new String(n, "UTF-8");
            value = value.trim();
            System.out.println("[+] PDU received: " + value);
            
            while(!exit){
                switch(value.charAt(2)){
                    case '1':
                        //Register
                        String[] p = value.split("\\|");
                        if(p[1].equals("i")){
                            User u = new User(p[2],p[3],Integer.parseInt(p[4]));
                            if(!this.users.containsKey(p[2])){
                                this.users.put(p[2], u);
                                user = p[2];
                                System.out.println("[+] New user " + p[2] + " created");
                            }
                            else{
                                System.err.println("[-] User " + p[2] + " already exists");
                            }
                        }
                        else {
                            System.err.println("[-] User " + user + " desconnected");
                            exit = true;
                        }
                        break;
                    default:
                        System.err.println("[-] Error with PDU");
                        break;
                }
                
                if(!exit){
                    n = new byte[48 * 1024];
                    this.is.read(n);
                    value = new String(n, "UTF-8");
                    value = value.trim();
                    System.out.println("[+] PDU received: " + value);
                }
            }
            
            //os.write("Ok".getBytes());
            is.close();
            os.close();
        }
        catch(Exception e){
            System.err.println("[-] Thread Error");
        };
    }
}
