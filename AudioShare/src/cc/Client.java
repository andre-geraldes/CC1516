/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cc;

import java.io.IOException;
import java.net.Socket;
import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author andregeraldes
 */
public class Client {
    private static ArrayList<String> songs;
    private static String userID;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException, InterruptedException, Throwable {
        int portServer = 3000;
        int portConsulta = Integer.parseInt(args[0]);
        int portUDP = Integer.parseInt(args[1]);
        InetAddress IP = InetAddress.getLocalHost();
        
        // Leitura das musicas disponiveis
        songs = new ArrayList<>(availableSongs("src/cc/songs.txt"));
        
        // IP do servidor
        String ip = IP.getHostAddress(); //Alterar para o ip do servidor
        //String ip = "192.168.204.1";
        
        // Conectar com o servidor
        Socket clientSocket = new Socket(ip, portServer);
             
        // Rececao e envio de dados
        DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
        BufferedReader inFromUser = new BufferedReader( new InputStreamReader(System.in));
        PDU p = new PDU();
        
        DatagramSocket serverSocket = null;
        try {
            serverSocket = new DatagramSocket(portUDP);
        } catch (SocketException ex) {
            Logger.getLogger(ClientUDP.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        // Registar cliente
        boolean registed = false;
        while(!registed){
            System.out.println("[+] Please regist yourself");
            System.out.println("[+] User ID?");
            String user = inFromUser.readLine();
            userID = user;
            outToServer.write(p.makeRegister('i', user, IP.getHostAddress(), String.valueOf(portConsulta)));
            System.out.println("[+] Sent to server");

            Thread.sleep(1000); //Esperar 1 segundo pela resposta
            //Receber resposta
            InputStream is = clientSocket.getInputStream();
            byte[] n = new byte[48 * 1024];
            is.read(n);
            String value = new String(n, "UTF-8");
            value = value.trim();
            if(value.contains("ok")){
                registed = true;
                System.out.println("[+] User " + userID + " registed!");
            } else
                System.out.println("[-] Error with user ID");
        }
        
        // Criar thread para receber pedidos TCP
        ClientConsult c = new ClientConsult(portConsulta, portUDP, IP, userID, songs);
        Thread y = new Thread(c);
        y.start();
        
        String sentence = "";
        while(!sentence.equals("EXIT")){
            if(sentence.equals("CONSULT_REQUEST")){
                System.out.println("[+] Insert band name");
                String band = inFromUser.readLine();
                System.out.println("[+] Insert song name");
                String song = inFromUser.readLine();
                outToServer.write(p.makeConsult(band, song));
                System.out.println("[+] Sent to server");
                
                // Receber resposta
                InputStream is = clientSocket.getInputStream();
                // Esperar pela resposta
                System.out.println("[+] Waiting for response...");
                while(is.available() == 0);
                
                byte[] n = new byte[48 * 1024];
                is.read(n);
                String value = new String(n, "UTF-8");
                value = value.trim();
                //System.out.println(value);
                
                if(value.contains("FOUND(1)")){
                    // Split do value para retirar info dos clientes
                    String [] v = value.split("\\|");
                    int nrHosts = Integer.valueOf(v[2]);
                    // Get Clients id
                    String [] ids = v[3].split("/");
                    // Get Clients ip
                    String [] ips = v[4].split("/");
                    // Get Clients udp port
                    String [] udps = v[5].split("/");
                    
                    // Guardar informação dos clientes que tem a musica
                    HashMap<String, User> usersSong = new HashMap<>(nrHosts);
                    for(int i = 0; i < nrHosts; i++){
                        User nuser = new User(ids[i], ips[i], 0, Integer.valueOf(udps[i]));
                        usersSong.put(nuser.getId(), nuser);
                    }
                    
                    // Fazer probe
                    User best = new User();
                    for(User u : usersSong.values()){
                        byte[] sendData = new byte[48 * 1024];
                        sendData = new PDU().makeProbeRequest();
                        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName(u.getIp()), u.getPortaUDP());
                        try {
                            serverSocket.send(sendPacket);
                        } catch (IOException ex) {
                            Logger.getLogger(ClientUDP.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        
                        //Fazer timestamp
                        String timeStamp = new SimpleDateFormat("HH.mm.ss.dd.MM.yyyy").format(new Date());
                        byte[] receiveData = new byte[48 * 1024];
                        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                        try {
                            serverSocket.receive(receivePacket);
                        } catch (IOException ex) {
                            Logger.getLogger(ClientUDP.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        String sentenceR = new String(receivePacket.getData());
                        System.out.println("[+] Timestamp: " + sentenceR);
                    }
                }
                else {
                    System.out.println("[-] Song not found!");
                }
            }
            else{
                if(sentence != "")
                    System.out.println("[-] Unknow request ");
            }
            
            //Proxima iteração e possivel saida
            System.out.println("[+] New Request:");
            sentence = inFromUser.readLine();
            if(sentence.equals("EXIT")){
                p = new PDU();
                outToServer.write(p.makeRegister('o', "", IP.getHostAddress(), String.valueOf(portConsulta)));
                System.out.println("[-] Exiting");
            }
        }
        
        // Fechar socket e matar thread
        clientSocket.close();
        c.getServerSocket().close();
        c.killThread();
    }
    
    /* Read available songs from a txt */
    public static ArrayList<String> availableSongs(String filename) throws FileNotFoundException, IOException{
        ArrayList<String> n = new ArrayList<String>();
        File file = new File(filename);
        BufferedReader reader = null;
        
        reader = new BufferedReader(new FileReader(file));
        
        String text;
        while ((text = reader.readLine()) != null) {
            n.add(text);
        }
        
        return n;
    }
}
