/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cc;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;

/**
 *
 * @author andregeraldes
 */
public class PDU {
    private char version;
    private char security;
    private char type;
    private char[] options;
  
    public PDU(){
        this.security = '0';
        this.version = '1';
        this.options = new char[4];
    }
    
    //Char tipo = in/out, char = i/o
    public byte[] makeRegister(char tipo, String id, String ip, String port) throws UnsupportedEncodingException{
        this.type = '1';
        
        //Colocar bytes a 0 nas opçoes
        Arrays.fill(this.options,'0');
                
        StringBuilder d = new StringBuilder();
        d.append(this.version);
        d.append(this.security);
        d.append(this.type);
        d.append(this.options);
        String data = "|" + tipo 
                + "|" + id
                + "|" + ip
                + "|" + port
                + "|";
        d.append(data);
        
        final ByteBuffer b = ByteBuffer.allocate(d.toString().length());
        b.put(d.toString().getBytes());
        
        // String value = new String(b.array(), "UTF-8");
        //System.out.println(value);
        
        return b.array();
    }
    
    public byte[] makeConsult(String band, String song) throws UnsupportedEncodingException{
        this.type = '2';
        
        //Colocar bytes a 0 nas opçoes
        Arrays.fill(this.options,'0');
                
        StringBuilder d = new StringBuilder();
        d.append(this.version);
        d.append(this.security);
        d.append(this.type);
        d.append(this.options);
        String data = "|" + band 
                + "|" + song
                + "|";
        d.append(data);
        
        final ByteBuffer b = ByteBuffer.allocate(d.toString().length());
        b.put(d.toString().getBytes());
        
        return b.array();
    }
    
    /*
        t -> FOUND(1) / NOT_FOUND(0)
        hosts -> nr de hosts 
        id -> id dos hosts
        ip -> ip dos hosts
        port -> porta udp
    */
    public byte[] makeResponse(String t, int hosts, String id, String ip, String port) throws UnsupportedEncodingException{
        this.type = '3';
        
        //Colocar bytes a 0 nas opçoes
        Arrays.fill(this.options,'0');
                
        StringBuilder d = new StringBuilder();
        d.append(this.version);
        d.append(this.security);
        d.append(this.type);
        d.append(this.options);
        String data = "|" + t 
                + "|" + hosts
                + "|" + id
                + "|" + ip
                + "|" + port
                + "|";
        d.append(data);
        
        final ByteBuffer b = ByteBuffer.allocate(d.toString().length());
        b.put(d.toString().getBytes());
        
        return b.array();
    }
    
    public byte[] makeRegisterResponse(String resp){
        this.type = '8';
        
        //Colocar bytes a 0 nas opçoes
        Arrays.fill(this.options,'0');
                
        StringBuilder d = new StringBuilder();
        d.append(this.version);
        d.append(this.security);
        d.append(this.type);
        d.append(this.options);
        String data = "|" + resp
                + "|";
        d.append(data);
        
        final ByteBuffer b = ByteBuffer.allocate(d.toString().length());
        b.put(d.toString().getBytes());
        
        return b.array();
    }
}
