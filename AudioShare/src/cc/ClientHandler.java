/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cc;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.HashMap;

/**
 *
 * @author andregeraldes
 * This handler is used in the server, to serve each request from clients
 */
public class ClientHandler implements Runnable {
    private Socket s;
    private HashMap users;   
    private InputStream is;
    private OutputStream os;
    private Socket centralSocket;
    
    public ClientHandler(Socket s, HashMap users, Socket cSocket){
        this.s = s;
        this.users = users;
        this.centralSocket = cSocket;
    }
    
    @Override
    public void run() {
        try{
            is = this.s.getInputStream();
            os = this.s.getOutputStream();
            OutputStream osCentral = this.centralSocket.getOutputStream();
            InputStream isCentral = this.centralSocket.getInputStream();
            
            String user = "";
            boolean exit = false;

            byte[] n = new byte[256];
            this.is.read(n);
            
            String value = new String(n, "UTF-8");
            value = value.trim();
            System.out.println("[+] PDU received: " + value);
            
            while(!exit){
                switch(value.charAt(2)){
                    case '1':
                        // Register
                        String[] p = value.split("\\|");
                        if(p[1].equals("i")){
                            User u = new User(p[2],p[3],Integer.parseInt(p[4]), 0);
                            if(!this.users.containsKey(p[2])){
                                this.users.put(p[2], u);
                                user = p[2];
                                System.out.println("[+] New user " + p[2] + " created");
                                //Enviar ok ao cliente
                                os.write(new PDU().makeRegisterResponse("ok"));
                            }
                            else{
                                System.out.println("[-] User " + p[2] + " already exists");
                                // Enviar erro ao cliente
                                os.write(new PDU().makeRegisterResponse("error"));
                            }
                        }
                        else {
                            System.out.println("[-] User " + user + " disconnected");
                            users.remove(user);
                            exit = true;
                        }
                        break;
                    case '2':
                        // Consult request
                        String[] a = value.split("\\|");
                        String band = a[1];
                        String song = a[2];
                        
                        // HashMap com os utilizadores a quem fazer pedidos
                        HashMap<String, User> toSend = new HashMap(users);
                        // Remover o utilizador que fez o consult_request
                        toSend.remove(user);
                        // Guardar utilizadores que tem a musica
                        HashMap<String, User> containsSong = new HashMap<>();
                        
                        // Enviar pedidos a todos os clientes registados
                        for(User u : toSend.values()){
                            String ipS = u.getIp();
                            int portS = u.getPorta();
                            
                            //Ligar ao socket do cliente
                            Socket clientSocket = new Socket(ipS, portS);
                            DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
                            
                            PDU pdu = new PDU();
                            outToServer.write(pdu.makeConsult(band, song));
                            
                            InputStream inFromServer = clientSocket.getInputStream();
                            
                            //Esperar pela resposta, 2 seg
                            try {
                                Thread.sleep(2000);
                            } catch(InterruptedException ex) {
                                Thread.currentThread().interrupt();
                            }
                            
                            // Verificar se o cliente respondeu
                            if(inFromServer.available() == 0)
                                System.out.println("[+] No response from " + ipS);
                            else{
                                byte[] response = new byte[256];
                                inFromServer.read(response);
                                
                                String resp = new String(response, "UTF-8");
                                resp = resp.trim();

                                System.out.println("[+] Request response: " + resp);
                                // Se o cliente tem a musica, guardar as suas informacoes
                                if(resp.contains("FOUND(1)")){
                                    String [] getUDP = resp.split("\\|");
                                    User unew = new User(u);
                                    unew.setPortaUDP(Integer.valueOf(getUDP[5]));
                                    containsSong.put(u.getId(), unew);
                                }
                            }
                        }
                        PDU pdu = new PDU();
                        if(containsSong.size() > 0){
                                // Enviar clientes
                                int nr = 0;
                                String hosts = "";
                                String ips = "";
                                String ports = "";
                                
                                for(User usr : containsSong.values()){
                                    nr++;
                                    hosts += usr.getId()+"/";
                                    ips += usr.getIp()+"/";
                                    ports += usr.getPortaUDP()+"/";
                                }
                                
                                os.write(pdu.makeResponse("FOUND(1)", nr, hosts, ips, ports));
                        }
                        else {
                            // Enviar ao servidor central o pedido
                            osCentral.write(value.getBytes());
                            // Esperar pela resposta
                            byte[] response = new byte[256];
                            isCentral.read(response);
                            
                            String resp = new String(response, "UTF-8");
                            resp = resp.trim();

                            System.out.println("[+] Request response: " + resp);
                            if(resp.contains("FOUND(1)")){
                                //Enviar info ao cliente
                            }
                            else {
                                os.write(pdu.makeResponse("NOT_FOUND(0)", 0, "", "", ""));
                            }
                        }
                        
                        break;
                    case '9':
                        // Consult request
                        String[] b = value.split("\\|");
                        String band1 = b[1];
                        String song1 = b[2];
                        
                        // HashMap com os utilizadores a quem fazer pedidos
                        HashMap<String, User> toSendS = new HashMap(users);
                        // Guardar utilizadores que tem a musica
                        HashMap<String, User> containsSongS = new HashMap<>();
                        
                        // Enviar pedidos a todos os clientes registados
                        for(User u : toSendS.values()){
                            String ipS = u.getIp();
                            int portS = u.getPorta();
                            
                            //Ligar ao socket do cliente
                            Socket clientSocket = new Socket(ipS, portS);
                            DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
                            
                            pdu = new PDU();
                            outToServer.write(pdu.makeConsult(band1, song1));
                            
                            InputStream inFromServer = clientSocket.getInputStream();
                            
                            //Esperar pela resposta, 2 seg
                            try {
                                Thread.sleep(2000);
                            } catch(InterruptedException ex) {
                                Thread.currentThread().interrupt();
                            }
                            
                            // Verificar se o cliente respondeu
                            if(inFromServer.available() == 0)
                                System.out.println("[+] No response from " + ipS);
                            else{
                                byte[] response = new byte[256];
                                inFromServer.read(response);
                                
                                String resp = new String(response, "UTF-8");
                                resp = resp.trim();

                                System.out.println("[+] Request response: " + resp);
                                // Se o cliente tem a musica, guardar as suas informacoes
                                if(resp.contains("FOUND(1)")){
                                    String [] getUDP = resp.split("\\|");
                                    User unew = new User(u);
                                    unew.setPortaUDP(Integer.valueOf(getUDP[5]));
                                    containsSongS.put(u.getId(), unew);
                                }
                            }
                        }
                        pdu = new PDU();
                        if(containsSongS.size() > 0){
                                // Enviar clientes
                                int nr = 0;
                                String hosts = "";
                                String ips = "";
                                String ports = "";
                                
                                for(User usr : containsSongS.values()){
                                    nr++;
                                    hosts += usr.getId()+"/";
                                    ips += usr.getIp()+"/";
                                    ports += usr.getPortaUDP()+"/";
                                }
                                
                                os.write(pdu.makeResponse("FOUND(1)", nr, hosts, ips, ports));
                        }
                        else {
                            os.write(pdu.makeResponse("NOT_FOUND(0)", 0, "", "", ""));
                        }
                        break;
                    default:
                        System.err.println("[-] Error with PDU");
                        break;
                }
                
                if(!exit){
                    n = new byte[256];
                    this.is.read(n);
                    value = new String(n, "UTF-8");
                    value = value.trim();
                    System.out.println("[+] PDU received: " + value +" from user: " + user);
                }
            }
            
            is.close();
            os.close();
        }
        catch(Exception e){
            System.err.println("[-] Thread Error");
        };
    }
}
