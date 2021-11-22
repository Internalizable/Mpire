/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package me.internalizable.musically.persistence;

import java.util.HashSet;

/**
 *
 * @author Internalizable
 */
public class Session {
    private long id;
    private String email;
    private String username;
    private String currentPassword;
    private int listenFor;
    
    private HashSet<Song> history;
    
    public Session(long id, String email, String username, String currentPassword, int listenFor) {
        this.id = id;
        this.email = email;
        this.username = username;
        this.currentPassword = currentPassword;
        this.listenFor = listenFor;
        
        this.history = new HashSet<>();
    }
    
    public long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getUsername() {
        return username;
    }

    public String getCurrentPassword() {
        return currentPassword;
    }
    
    public int getListenFor() {
        return listenFor;
    }
    
    public HashSet<Song> getHistory() {
        return history;
    }
    
    public void setId(long id) {
        this.id = id;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setCurrentPassword(String currentPassword) {
        this.currentPassword = currentPassword;
    }
    
    public void setListenFor(int listenFor) {
        this.listenFor = listenFor;
    }
}
