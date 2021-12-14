/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package me.internalizable.musically.database;

import com.google.common.hash.Hashing;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Base64;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import me.internalizable.musically.CoreFrame;
import me.internalizable.musically.persistence.Session;
import me.internalizable.musically.persistence.Song;
import me.internalizable.musically.utilities.FileUtils;

/**
 *
 * @author Internalizable
 */
public class DatabaseConnector {
    
    private HikariConfig config = new HikariConfig();
    private HikariDataSource ds;

    private static DatabaseConnector instance = null;
    
    private Session activeSession;
    
    public static DatabaseConnector getInstance() {
        if(instance == null)
            instance = new DatabaseConnector();
        
        return instance;
    }
    
    public Session getActiveSession() {
        return activeSession;
    }
        
    private DatabaseConnector() {
        config.setJdbcUrl( "jdbc:mysql://localhost:3306/mpire" );
        config.setUsername( "root" );
        config.setPassword( "" );
        config.addDataSourceProperty( "cachePrepStmts" , "true" );
        config.addDataSourceProperty( "prepStmtCacheSize" , "250" );
        config.addDataSourceProperty( "prepStmtCacheSqlLimit" , "2048" );
        
        ds = new HikariDataSource( config );
        
        activeSession = null;
    }

    public Connection getConnection() throws SQLException {
        return ds.getConnection();
    }
    
    public void initializeTables() {
        try (Connection con = getConnection()){
            String userTable = "CREATE TABLE IF NOT EXISTS USERS(id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY, email VARCHAR(50) NOT NULL, username VARCHAR(50) NOT NULL, password VARCHAR(100) NOT NULL, listenFor INT NOT NULL);";
            String songTable = "CREATE TABLE IF NOT EXISTS SONGS(id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY, name VARCHAR(50) NOT NULL, artist VARCHAR(50) NOT NULL, album VARCHAR(50) NOT NULL, year VARCHAR(20) NOT NULL, duration VARCHAR(20) NOT NULL, artwork VARCHAR(300));";
            String historyTable = "CREATE TABLE IF NOT EXISTS HISTORY(userId BIGINT NOT NULL, songId BIGINT NOT NULL, timestamp BIGINT NOT NULL, PRIMARY KEY(userId, songId, timestamp), FOREIGN KEY (userId) REFERENCES USERS(id), FOREIGN KEY (songId) REFERENCES SONGS(id));";
            
            con.prepareCall(userTable).executeUpdate();
            con.prepareCall(songTable).executeUpdate();
            con.prepareCall(historyTable).executeUpdate();
            
        } catch (SQLException ex) {
            System.out.println(ex);
        }
    }
    
    public boolean hasUsername(String username) {
        try (Connection con = getConnection()) {
            String query = "SELECT id FROM USERS WHERE username=?";

            PreparedStatement prep = con.prepareStatement(query);
            prep.setString(1, username);
            
            return prep.executeQuery().next();
        } catch (SQLException ex) {
            System.out.println(ex);
        }
        
        return false;
    }
    
    public boolean hasEmail(String email) {
        try (Connection con = getConnection()) {
            String query = "SELECT id FROM USERS WHERE email=?";

            PreparedStatement prep = con.prepareStatement(query);
            prep.setString(1, email);
            
            return prep.executeQuery().next();
        } catch (SQLException ex) {
            System.out.println(ex);
        }
        
        return false;
    }
   
    public boolean register(String email, String username, String password) {
        
        String hashedPassword = Hashing.sha256().hashString(password, StandardCharsets.UTF_8).toString();
        
         try (Connection con = getConnection()) {
            String query = "INSERT INTO USERS VALUES(?,?,?,?,?);";

            PreparedStatement prep = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            prep.setLong(1, 0);
            prep.setString(2, email);
            prep.setString(3, username);
            prep.setString(4, hashedPassword);
            prep.setInt(5, 15);
            
            prep.executeUpdate();
            
            ResultSet rs = prep.getGeneratedKeys();
            
            if (rs.next()){
                long userId = rs.getLong(1);
                
                activeSession = new Session(userId, email, username, hashedPassword, 15);
                CoreFrame.getInstance().getProfilePanel().loadProfile();
                return true;
            }
        } catch (SQLException ex) {
            System.out.println(ex);
        }
        
        return false;       
    }
    
    public boolean login(String username, String password) {
        String hashedPassword = Hashing.sha256().hashString(password, StandardCharsets.UTF_8).toString();
        
         try (Connection con = getConnection()) {
            String query = "SELECT * FROM USERS WHERE username=?;";

            PreparedStatement prep = con.prepareStatement(query);
            prep.setString(1, username);

            ResultSet rs = prep.executeQuery();
            
            if (rs.next()){
                long userId = rs.getLong("id");
                String email = rs.getString("email");
                int listenFor = rs.getInt("listenFor");
                
                if(rs.getString("password").equals(hashedPassword)) {
                    activeSession = new Session(userId, email, username, hashedPassword, listenFor);
                    
                    CoreFrame.getInstance().getProfilePanel().loadProfile();
                    loadDatabaseHistory();
                    return true;
                }
                
                return false;
            }
        } catch (SQLException ex) {
            System.out.println(ex);
        }
        
        return false;       
    }
    
    public void addToSessionCache(Song song) {
        this.activeSession.getHistory().add(song);
    }
    
    public long insertSong(Song song) {
         try (Connection con = getConnection()) {
            String query = "INSERT INTO SONGS VALUES(?, ?, ?, ?, ?, ?, ?);";

            PreparedStatement prep = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            prep.setLong(1, 0);
            prep.setString(2, song.getName());
            prep.setString(3, song.getArtist());
            prep.setString(4, song.getAlbum());
            prep.setString(5, song.getReleaseDate());
            prep.setString(6, song.getDuration());
            prep.setNString(7, song.getArtworkLink());
            
            prep.executeUpdate();
            
            ResultSet rs = prep.getGeneratedKeys();
            
            if (rs.next()){
                return rs.getLong(1);
            }
        } catch (SQLException ex) {
            System.out.println(ex);
        }
        
        return -1;          
    }
    
    public void insertHistorySong(Song song) {
         try (Connection con = getConnection()) {
            String query = "SELECT id FROM SONGS s WHERE name=? AND artist=? AND album=?;";

            PreparedStatement prep = con.prepareStatement(query);
            prep.setString(1, song.getName());
            prep.setString(2, song.getArtist());
            prep.setString(3, song.getAlbum());
            
            ResultSet rs = prep.executeQuery();
           
            long id = -1;
            
            if (!rs.next())
                id = insertSong(song);
            else
                id = rs.getLong(1);
         
            String insertIntoHistory = "INSERT INTO HISTORY VALUES (?, ?, ?);";
            
            PreparedStatement historyPrep = con.prepareStatement(insertIntoHistory);
            historyPrep.setLong(1, activeSession.getId());
            historyPrep.setLong(2, id);
            historyPrep.setLong(3, System.currentTimeMillis());
            
            historyPrep.executeUpdate();
        } catch (SQLException ex) {
            System.out.println(ex);
        }        
    }
    
    public void loadDatabaseHistory() {
         try (Connection con = getConnection()) {
            String query = "SELECT s.* FROM SONGS s JOIN HISTORY h ON h.songId = s.id JOIN USERS u ON h.userId = u.id WHERE u.id=?;";

            PreparedStatement prep = con.prepareStatement(query);
            prep.setLong(1, activeSession.getId());

            ResultSet rs = prep.executeQuery();
            
            while (rs.next()){
                Song song = new Song(rs.getString("name"), rs.getString("artist"), rs.getString("album"), rs.getString("year"), rs.getString("duration"), null, rs.getNString("artwork"));
                activeSession.getHistory().add(song);
            }
        } catch (SQLException ex) {
            System.out.println(ex);
        }
               
    }
    
    public void populateTable(JTable table) {
        if(activeSession.getHistory().size() == 0)
            return;
        
        DefaultTableModel model = (DefaultTableModel) table.getModel();
           
        activeSession.getHistory().stream().distinct().forEach(song -> {
            
            ImageIcon icon = FileUtils.getImage(song, 64, 64);
            
            String songName = song.getName();
            String artist = song.getArtist();
            String album = song.getAlbum();
            String year = song.getReleaseDate();
            String duration = song.getDuration();
            
            Object[] data = {icon, songName, artist, album, year, duration};
   
            model.addRow(data);
        });
    }
    
    public void signOut() {
        this.activeSession = null;
    }

    public void changeListenFor(int value) {
          try (Connection con = getConnection()) {
            String query = "UPDATE USERS SET listenFor=? WHERE id=?";

            PreparedStatement prep = con.prepareStatement(query);
            prep.setInt(1, value);
            prep.setLong(2, activeSession.getId());
            prep.executeUpdate();
            
            activeSession.setListenFor(value);
        } catch (SQLException ex) {
            System.out.println(ex);
        }       
    }
}
