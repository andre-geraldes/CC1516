/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cc;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
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
    private DatagramSocket serverSocket;
    private boolean run;
    
    public ClientUDP() {
        this.run = false;
    }
    
    public ClientUDP(int portUDP, String ip, String userID, DatagramSocket s){
        this.portUDP = portUDP;
        this.ip = ip;
        this.userID = userID;
        this.serverSocket = s;
        this.run = true;
    }
    
    @Override
    public void run() {
        
        byte[] receiveData = new byte[48 * 1024];
        byte[] sendData = new byte[48 * 1024];
        
        while(this.run) {
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            try {
                this.serverSocket.receive(receivePacket);
            } catch (IOException ex) {
                Logger.getLogger(ClientUDP.class.getName()).log(Level.SEVERE, null, ex);
            }
            String sentence = new String(receivePacket.getData(), 0, receivePacket.getLength());
            System.out.println("[UDP] Request received: " + sentence);
            // get address and port
            InetAddress IPAddress = receivePacket.getAddress();
            int port = receivePacket.getPort();
            
            PDU p = new PDU();
            
            if(sentence.charAt(2) == '4'){
                // Enviar valor da data atual para timestamp 
                //String timeStamp = new SimpleDateFormat("HH-mm-ss.dd-MM-yyyy").format(new Date());
                String timeStamp = new SimpleDateFormat("HH-mm-ss.SSS").format(new Date());
                sendData = p.makeProbeResponse(timeStamp);
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
                try {
                    serverSocket.send(sendPacket);
                } catch (IOException ex) {
                    Logger.getLogger(ClientUDP.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            else if(sentence.charAt(2) == '6'){
                // Split de sentence para saber qual musica enviar
                
                String [] s = sentence.split("\\|");
                String band = s[1];
                String song = s[2];
                String exte = s[3];
                System.out.println("[UDP] Song " + s[2] + " requested.");
                
                // Procurar musica  
                
                //Abrir Musica
                Path filePath = Paths.get("src/cc/audiofiles/"+song+"."+exte);
                byte [] file = null;
                try {
                    file = Files.readAllBytes(filePath);
                } catch (IOException ex) {
                    Logger.getLogger(ClientUDP.class.getName()).log(Level.SEVERE, null, ex);
                }
                System.out.println("[UDP] Song loaded, ready to send.");
                
                // Calcular numero de partes
                int maxSize = 48*1024-8;
                float nr = file.length/((float) maxSize);
                // Arredondar em excesso
                int parts = (int) Math.ceil(nr);
                
                // Criar partes para enviar
                HashMap<Integer,byte []> songParts = new HashMap<>();
                for(int i = 1; i <= parts; i++){
                    byte[] part = null;
                    if(file.length - (i-1)*maxSize < maxSize){
                        part = new byte[file.length - (i-1)*maxSize];
                        for(int k = 0; k < (file.length - (i-1)*maxSize); k++){
                            part[k] = file[(i-1)*maxSize + k];
                        }    
                    }
                    else {
                        part = new byte[maxSize];
                        for(int k = 0; k < maxSize; k++){
                            part[k] = file[(i-1)*maxSize + k];
                        }
                    }
                    songParts.put(i, part);
                }
                
                //Enviar numero de partes
                sendData = p.makeProbeResponse(""+parts);
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
                try {
                    serverSocket.send(sendPacket);
                } catch (IOException ex) {
                    Logger.getLogger(ClientUDP.class.getName()).log(Level.SEVERE, null, ex);
                }
                
                // Enviar partes
                sendData = new byte[48*1024];
                for(int i = 1; i <= songParts.size(); i++){
                    //Enviar
                    
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(ClientUDP.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    sendData = p.makeResponse(""+i, songParts.get(i));
                    sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
                    try {
                        serverSocket.send(sendPacket);
                    } catch (IOException ex) {
                        Logger.getLogger(ClientUDP.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    
                    //Esperar resposta
                }
                
                System.out.println("[UDP] Song sent");
            }
            else {
                this.run = false;
            }
        }
    }
    
    public void killUDP(){
        this.run = false;
    }
}
