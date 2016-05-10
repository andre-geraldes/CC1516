/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cc;

import java.io.IOException;
import java.net.Socket;
import java.io.*;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Scanner;

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
        
        // Criar thread para receber pedidos
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
