/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cc;

import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.HashMap;

/**
 *
 * @author andregeraldes
 */
public class ServerHandler implements Runnable {
    private Socket s;
    private HashMap servers;   
    private InputStream is;
    private OutputStream os;
    
    public ServerHandler(Socket s, HashMap servers){
        this.s = s;
        this.servers = servers;
    }
    
    @Override
    public void run() {
        try{
            is = this.s.getInputStream();
            os = this.s.getOutputStream();
            
            String server = "";
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
                            String ip = p[3];
                            String port = p[4];
                            if(!this.servers.containsKey(ip)){
                                this.servers.put(ip, port);
                                server = ip;
                                System.out.println("[+] New server " + ip + " registed");
                                //Enviar ok ao cliente
                                os.write(new PDU().makeRegisterResponse("ok"));
                            }
                            else{
                                System.out.println("[-] Server " + ip + " already registed");
                                // Enviar erro ao cliente
                                os.write(new PDU().makeRegisterResponse("error"));
                            }
                        }
                        else {
                            System.out.println("[-] Server " + server + " disconnected");
                        }
                        break;
                    case '2':
                        // Enviar o pedido para os outros servidores
                        HashMap<String, String> serversTo = new HashMap(this.servers);
                        serversTo.remove(server);
                        boolean found = false;
                        byte[] response = new byte[256];
                        PDU pdu = new PDU();
                        if(serversTo.size() > 0){
                            for(String ip : serversTo.keySet()){
                                
                                // Mudar o tipo de pedido para 9
                                StringBuilder newValue = new StringBuilder(value);
                                newValue.setCharAt(2, '9');
                                
                                //Ligar ao socket do cliente
                                Socket clientSocket = new Socket(ip, Integer.valueOf(serversTo.get(ip)));
                                DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());

                                outToServer.write(newValue.toString().getBytes());
                                
                                InputStream inFromServer = clientSocket.getInputStream();
                                
                                //Esperar pela resposta, 3 seg
                                try {
                                    Thread.sleep(3000);
                                } catch(InterruptedException ex) {
                                    Thread.currentThread().interrupt();
                                }

                                // Verificar se o servidor respondeu
                                if(inFromServer.available() == 0)
                                    System.out.println("[+] No response from " + ip);
                                else {
                                    response = new byte[256];
                                    inFromServer.read(response);

                                    String resp = new String(response, "UTF-8");
                                    resp = resp.trim();

                                    System.out.println("[+] Request response: " + resp);
                                    // Se o cliente tem a musica
                                    if(resp.contains("FOUND(1)")){
                                        found = true;
                                    }
                                }
                                if(found) break;
                            }
                        }
                        // Enviar ao servidor a resposta
                        if(found) {
                            os.write(response);
                            System.out.println("[+] Song found!");
                        }
                        else {
                            os.write(pdu.makeResponse("NOT_FOUND(0)", 0, "", "", ""));
                            System.out.println("[-] Song not found");
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
                    System.out.println("[+] PDU received: " + value +" from user: " + server);
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
