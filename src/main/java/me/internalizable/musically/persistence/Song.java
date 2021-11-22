/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package me.internalizable.musically.persistence;

/**
 *
 * @author Internalizable
 */
public class Song {
    
    private String name;
    private String artist;
    private String album;
    private String releaseDate;
    private String duration;
    private String previewLink;
    private String artworkLink;
    
    public Song(String name, String artist, String album, String releaseDate, String duration, String previewLink, String artworkLink) {
        this.name = name;
        this.artist = artist;
        this.album = album;
        this.releaseDate = releaseDate;
        this.duration = duration;
        this.previewLink = previewLink;
        this.artworkLink = artworkLink;
    }
    
    public String getName() {
        return name;
    }

    public String getArtist() {
        return artist;
    }

    public String getAlbum() {
        return album;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public String getDuration() { 
        return duration;
    }
    
    public String getPreviewLink() {
        return previewLink;
    }

    public String getArtworkLink() {
        return artworkLink;
    }
    
    @Override
    public String toString() {
        return "Song{" + "name=" + name + ", artist=" + artist + ", album=" + album + ", releaseDate=" + releaseDate + ", previewLink=" + previewLink + ", artworkLink=" + artworkLink + '}';
    }
}
