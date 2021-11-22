package me.internalizable.musically.http;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.Request;
import okhttp3.Response;

import java.io.File;
import me.internalizable.musically.persistence.Song;

public class AudioVerifierRequest {

    private final File file;

    public AudioVerifierRequest(File file) {
        this.file = file;
    }

    public Song processRequest() {
        try {
            final MediaType MEDIA_TYPE_MP3 = MediaType.get("audio/mpeg; charset=utf-8");
            OkHttpClient client = new OkHttpClient();

            RequestBody data = new MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart("api_token", "06bb5ad79dc416844b77dde7060217d6")
                    .addFormDataPart("file", file.getName(),
                            RequestBody.Companion.create(file, MEDIA_TYPE_MP3))
                    .addFormDataPart("return", "apple_music,spotify").build();

            Request request = new Request.Builder().url("https://api.audd.io/")
                    .post(data).build();
            Response response = null;
            response = client.newCall(request).execute();
            String result = null;
            result = response.body().string();
            
            
            JsonObject element = JsonParser.parseString(result).getAsJsonObject();
            
            if(element.get("status") == null || element.get("result") == null)
                return null;
            
            JsonObject resultSet = element.get("result").getAsJsonObject();
            
            String name = resultSet.get("title").getAsJsonPrimitive().getAsString();
            String artist = resultSet.get("artist").getAsJsonPrimitive().getAsString();
            String album = resultSet.get("album").getAsJsonPrimitive().getAsString();
            String releaseDate = resultSet.get("release_date").getAsJsonPrimitive().getAsString();
             
            String previewLink = null;
            String artworkLink = null;
            
            JsonObject miscSet = resultSet.get("apple_music").getAsJsonObject();
            JsonObject spotifySet = resultSet.get("spotify").getAsJsonObject();
            
            String duration = null;
            
            if(miscSet != null) {
                previewLink = miscSet.get("previews").getAsJsonArray().get(0).getAsJsonObject().get("url").getAsJsonPrimitive().getAsString();
                artworkLink = miscSet.get("artwork").getAsJsonObject().get("url").getAsJsonPrimitive().getAsString();
                
                duration = millisecondsToTime(miscSet.get("durationInMillis").getAsLong());
            } else if(spotifySet != null) {
                duration = millisecondsToTime(miscSet.get("duration_ms").getAsLong());
            }
            
            return new Song(name, artist, album, releaseDate, duration, previewLink, artworkLink);
        } catch (Exception ignored) {
            
        }
        
        return null;
    }
    
    private String millisecondsToTime(long milliseconds) {
        long minutes = (milliseconds / 1000) / 60;
        long seconds = (milliseconds / 1000) % 60;
        
        String secondsStr = Long.toString(seconds);
        String secs;
        
        if (secondsStr.length() >= 2) {
            secs = secondsStr.substring(0, 2);
        } else {
            secs = "0" + secondsStr;
        }

        return minutes + ":" + secs;
}
}
