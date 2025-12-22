package BackEnd;

import java.io.Serializable;

// Serializable supaya objek ini bisa disimpan ke file
public class Lagu implements Serializable {
    private String judul;
    private String artis;
    private String album;
    private String path;

    public String getArtis() {
        return artis;
    }

    public String getAlbum() {
        return album;
    }

    public String getPath() {
        return path;
    }

    public Lagu(String judul, String artis, String album, String path) {
        this.judul = judul;
        this.artis = artis;
        this.album = album;
        this.path = path;
    }

    // Getter untuk menampilkan data
    public String getInfo() {
        return String.format("[%s] %s - %s (%s)", judul, artis, album, path);
    }

    // Getter & Setter (Gunakan ini untuk fitur UPDATE nanti)
    public String getJudul() { return judul; }
    public void setJudul(String judul) { this.judul = judul; }
}