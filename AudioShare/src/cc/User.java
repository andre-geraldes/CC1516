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
    private int portaUDP;
    
    public User() {
    }

    public User(String id, String ip, int porta, int portaUDP) {
        this.id = id;
        this.ip = ip;
        this.porta = porta;
        this.portaUDP = portaUDP;
    }
    
    public User(User u) {
        this.id = u.getId();
        this.ip = u.getIp();
        this.porta = u.getPorta();
        this.portaUDP = u.getPortaUDP();
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

    public int getPortaUDP() {
        return portaUDP;
    }

    public void setPortaUDP(int portaUDP) {
        this.portaUDP = portaUDP;
    }

    @Override
    public String toString() {
        return "User{" + "id=" + id + ", ip=" + ip + ", porta=" + porta + ", portaUDP=" + portaUDP + '}';
    }
}
