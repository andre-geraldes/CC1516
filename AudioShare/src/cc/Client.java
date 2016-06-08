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
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
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
            byte[] n = new byte[256];
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
        ClientConsult c = new ClientConsult(portConsulta, portUDP, IP, userID, songs, serverSocket);
        Thread y = new Thread(c);
        y.start();
        
        // Manter o cliente ativo
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
                // Esperar no maximo 10 segundos por uma resposta
                int tout = 0;
                while(is.available() == 0){
                    Thread.sleep(1000);
                    tout++;
                    // Timeout waiting response
                    if(tout == 10)
                        break;
                };
                
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
                    
                    // Fazer probe para descobrir melhor cliente
                    User best = new User();
                    long diff = 1000000000;
                    for(User u : usersSong.values()){
                        PDU q = new PDU();
                        DatagramPacket sendPacket = new DatagramPacket(q.makeProbeRequest(), q.makeProbeRequest().length, InetAddress.getByName(u.getIp()), u.getPortaUDP());
                        try {
                            serverSocket.send(sendPacket);
                        } catch (IOException ex) {
                            Logger.getLogger(ClientUDP.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        
                        // Fazer timestamp
                        // String timeStamp = new SimpleDateFormat("HH-mm-ss.dd-MM-yyyy").format(new Date());
                        String timeStamp = new SimpleDateFormat("HH-mm-ss.SSS").format(new Date());
                        System.out.println("[+] My timeStamp: " + timeStamp);
                        byte[] receiveData = new byte[48 * 1024];
                        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                        try {
                            serverSocket.receive(receivePacket);
                        } catch (IOException ex) {
                            Logger.getLogger(ClientUDP.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        
                        // Filtrar timestamp recebido
                        String sentenceR = new String(receivePacket.getData(), 0, receivePacket.getLength());
                        System.out.println("[+] Timestamp: " + sentenceR);
                        String [] cliTimeStamp = sentenceR.split("\\|");
                        
                        // Comparar timestamps
                        SimpleDateFormat format = new SimpleDateFormat("HH-mm-ss.SSS");
                        
                        // Calcular a diferenca
                        Date myDate = format.parse(timeStamp);
                        Date cliDate = format.parse(cliTimeStamp[1]);
                        long newDiff = cliDate.getTime() - myDate.getTime();
                        if(newDiff < diff){
                            diff = newDiff;
                            best = u;
                        }
                    }
                    // Melhor cliente
                    System.out.println("[+] Best client: " +best.toString() + " with a OWD of " + diff + " milis");
                    
                    // Pedir musica ao cliente
                    PDU q = new PDU();
                    String [] songExt = song.split("\\.");
                    DatagramPacket sendPacket = new DatagramPacket(q.makeRequest(band, songExt[0], songExt[1]), 
                            q.makeRequest(band, songExt[0], songExt[1]).length, 
                            InetAddress.getByName(best.getIp()), best.getPortaUDP());
                    try {
                        serverSocket.send(sendPacket);
                    } catch (IOException ex) {
                        Logger.getLogger(ClientUDP.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    
                    // Esperar um pouco para o cliente ler a musica
                    Thread.sleep(500);
                    
                    // Receber datagram com a quantidade de partes
                    byte[] receiveData = new byte[48 * 1024];
                    DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                    try {
                        serverSocket.receive(receivePacket);
                    } catch (IOException ex) {
                        Logger.getLogger(ClientUDP.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    
                    // Split do numero de partes
                    String sentenceR = new String(receivePacket.getData(), 0, receivePacket.getLength());
                    String [] quant = sentenceR.split("\\|");
                    int parts = Integer.valueOf(quant[1]);
                    
                    // Receber musica
                    System.out.println("[+] Receiving song " + song);
                    HashMap<Integer, byte []> songParts = new HashMap<>();
                    System.out.print("<");
                    // Iniciar tudo a vazio
                    for(int i = 1; i <= parts; i++){
                        songParts.put(i, new byte[48*1024]);
                    }
                    
                    
                    // Receber 1o pdu
                    // Receber parte
                    receiveData = new byte[48 * 1024];
                    receivePacket = new DatagramPacket(receiveData, receiveData.length);
                    try {
                        serverSocket.receive(receivePacket);
                    } catch (IOException ex) {
                        Logger.getLogger(ClientUDP.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    // Guardar o numero do pdu
                    String data = new String(receivePacket.getData(), 0, receivePacket.getLength());
                    String number = "";
                    number += data.charAt(3);
                    number += data.charAt(4);
                    number += data.charAt(5);
                    number += data.charAt(6);
                    int nrPDU = Integer.valueOf(number);
                    
                    while(nrPDU != 0){
                        System.out.print("=");
                        if(nrPDU == parts/2)
                            System.out.print("50%");
                        
                        //Receber parte
                        receiveData = new byte[48 * 1024];
                        receivePacket = new DatagramPacket(receiveData, receiveData.length);
                        try {
                            serverSocket.receive(receivePacket);
                        } catch (IOException ex) {
                            Logger.getLogger(ClientUDP.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        
                        // Guardar o numero do pdu
                        data = new String(receivePacket.getData(), 0, receivePacket.getLength());
                        number = "";
                        number += data.charAt(3);
                        number += data.charAt(4);
                        number += data.charAt(5);
                        number += data.charAt(6);
                        nrPDU = Integer.valueOf(number);
                        
                        // Guardar parte
                        byte[] part = new byte[48*1024];
                        part = receivePacket.getData();
                        // Retirar cabecalho
                        byte[] npart = new byte[48*1024-8];
                        for(int j = 0; j < 48*1024-8; j++)
                            npart[j] = part[j+8];
                        
                        if(nrPDU != 0)
                            songParts.put(nrPDU, npart);
                    }

                    System.out.print(">");
                    System.out.println("");
                    
                    // Ver quais partes faltam
                    for(int i = 1; i <= parts; i++){
                        if(songParts.get(i).length == 0)
                            System.out.println("Missing " + i);
                    }
                    
                    // Guardar musica
                    try{
			FileOutputStream fos = new FileOutputStream(song);
			for(byte[] b : songParts.values())
                            fos.write(b);
			fos.close();
                    }
                    catch(IOException e){
                            System.out.println("[-] Error saving song");
                    }
                    
                    System.out.println("[+] Song received");
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
            System.out.println("[+] 1. CONSULT_REQUEST");
            System.out.println("[+] 2. EXIT");
            String k = inFromUser.readLine();
            if(k.equals("1"))
                sentence = "CONSULT_REQUEST";
            else if(k.equals("2"))
                sentence = "EXIT";
            else
                sentence = "ERROR";
            //sentence = inFromUser.readLine();
            if(sentence.equals("EXIT")){
                p = new PDU();
                outToServer.write(p.makeRegister('o', "", IP.getHostAddress(), String.valueOf(portConsulta)));
                System.out.println("[-] Exiting");
            }
        }
        
        // Fechar socket e matar threads
        clientSocket.close();
        
        c.getClientUDP().killUDP();
        c.getServerUdp().close();
        
        c.getServerSocket().close();
        c.killThread();
    }
    
    // Ler as musicas do ficheiro txt 
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
