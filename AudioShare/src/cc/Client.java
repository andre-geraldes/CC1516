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
import java.util.Scanner;

/**
 *
 * @author andregeraldes
 */
public class Client {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        int portaServer = 3000;
        int portaConsulta = Integer.parseInt(args[0]);
        InetAddress IP = InetAddress.getLocalHost();
        
        
        String ip = IP.getHostAddress(); //Alterar para o servidor
        //String ip = 1.1.1.1;

        Socket clientSocket = new Socket(ip, portaServer);
        //portaConsulta = clientSocket.getLocalPort();
        
        // Rececao e envio de dados
        DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
        BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        BufferedReader inFromUser = new BufferedReader( new InputStreamReader(System.in));
        
        System.out.println("[+] New Request:");
        String sentence = inFromUser.readLine();
        while(!sentence.equals("EXIT")){
            PDU p = new PDU();
            if(sentence.equals("REGISTER")){
                System.out.println("[+] Insert User ID ");
                String user = inFromUser.readLine();
                outToServer.write(p.makeRegister('i', user, IP.getHostAddress(), String.valueOf(portaConsulta)));
                System.err.println("[+] Sent to server");
            }
            else{
                System.err.println("[-] Unknow request");
            }
            
            //Proxima iteração e possivel saida
            System.out.println("[+] New Request:");
            sentence = inFromUser.readLine();
            if(sentence.equals("EXIT")){
                System.err.println("[-] Exiting");
                p = new PDU();
                outToServer.write(p.makeRegister('o', "", IP.getHostAddress(), String.valueOf(portaConsulta)));
            }
        }
        
        
        //String modifiedSentence = inFromServer.readLine();
        //System.out.println("FROM SERVER: " + modifiedSentence);
        clientSocket.close();
    }
}
