/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cc;

/**
 *
 * @author andregeraldes
 */
public class User {
    private String id;
    private String ip;
    private int porta;

    public User(String id, String ip, int porta) {
        this.id = id;
        this.ip = ip;
        this.porta = porta;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPorta() {
        return porta;
    }

    public void setPorta(int porta) {
        this.porta = porta;
    }
    
    
}
