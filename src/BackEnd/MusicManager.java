package BackEnd;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import javazoom.jl.player.Player;

import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MusicManager {
    private static ArrayList<Lagu> databaseLagu = new ArrayList<>();
    private static final String DATABASE_FILE = "playlist.csv";

    // Player variables
    private static Player player;
    private static Thread playerThread;
    private static int currentIndex = -1;

    public static void main(String[] args) {
        // Matikan log merah JAudioTagger
        Logger.getLogger("org.jaudiotagger").setLevel(Level.OFF);

        loadDataDariCSV();

        Scanner sc = new Scanner(System.in);
        while (true) {
            System.out.println("\n======= JAVA MUSIC PLAYER (CRUD CSV) =======");
            System.out.println("Status: " + (currentIndex == -1 ? "Stopped" : "Playing [" + databaseLagu.get(currentIndex).getJudul() + "]"));
            System.out.println("1. Lihat Playlist (READ)");
            System.out.println("2. Tambah Lagu (CREATE)");
            System.out.println("3. Play Lagu (Index)");
            System.out.println("4. Stop Musik");
            System.out.println("5. Next Lagu");
            System.out.println("6. Previous Lagu");
            System.out.println("7. Hapus Lagu (DELETE)");
            System.out.println("0. Keluar");
            System.out.print("Pilih menu: ");

            try {
                int pilihan = Integer.parseInt(sc.nextLine());

                switch (pilihan) {
                    case 1: tampilkanPlaylist(); break;
                    case 2: tambahLaguProses(sc); break;
                    case 3:
                        System.out.print("Masukkan nomor urutan lagu: ");
                        int idx = Integer.parseInt(sc.nextLine()) - 1;
                        playLagu(idx);
                        break;
                    case 4: stopLagu(); break;
                    case 5: nextLagu(); break;
                    case 6: prevLagu(); break;
                    case 7: hapusLagu(sc); break;
                    case 0:
                        stopLagu();
                        simpanDataKeCSV();
                        System.out.println("Sampai jumpa!");
                        System.exit(0);
                    default: System.out.println("Pilihan tidak tersedia.");
                }
            } catch (Exception e) {
                System.out.println("Input harus berupa angka!");
            }
        }
    }

    // --- KONTROL MUSIK (STREAMING) ---
    public static void playLagu(int index) {
        if (index < 0 || index >= databaseLagu.size()) {
            System.out.println("Lagu tidak ditemukan!");
            return;
        }

        stopLagu(); // Hentikan yang sedang jalan
        currentIndex = index;
        Lagu lagu = databaseLagu.get(currentIndex);

        playerThread = new Thread(() -> {
            try {
                FileInputStream fis = new FileInputStream(lagu.getPath());
                player = new Player(fis);
                player.play();

                // Jika lagu selesai secara alami, lanjut ke next otomatis (optional)
                if (player.isComplete()) {
                    nextLagu();
                }
            } catch (Exception e) {
                System.err.println("Gagal memutar file: " + e.getMessage());
            }
        });
        playerThread.start();
        System.out.println("Memutar: " + lagu.getInfo());
    }

    public static void stopLagu() {
        if (player != null) {
            player.close();
            player = null;
        }
        if (playerThread != null) {
            playerThread.interrupt();
            playerThread = null;
        }
        currentIndex = -1;
    }

    public static void nextLagu() {
        if (databaseLagu.isEmpty()) return;
        int nextIdx = (currentIndex + 1) % databaseLagu.size();
        playLagu(nextIdx);
    }

    public static void prevLagu() {
        if (databaseLagu.isEmpty()) return;
        int prevIdx = (currentIndex - 1 < 0) ? databaseLagu.size() - 1 : currentIndex - 1;
        playLagu(prevIdx);
    }

    // --- FITUR CRUD ---
    public static void tambahLaguProses(Scanner sc) {
        try {
            System.out.print("Masukkan nama file di folder Music: ");
            String fileName = sc.nextLine();
            File file = new File("Music/" + fileName);

            if (!file.exists()) {
                System.out.println("File tidak ada!");
                return;
            }

            AudioFile f = AudioFileIO.read(file);
            Tag tag = f.getTag();

            String jDef = (tag != null) ? tag.getFirst(FieldKey.TITLE) : "Unknown";
            String aDef = (tag != null) ? tag.getFirst(FieldKey.ARTIST) : "Unknown";
            String albDef = (tag != null) ? tag.getFirst(FieldKey.ALBUM) : "Unknown";

            System.out.print("Edit Judul [" + jDef + "]: ");
            String judul = sc.nextLine();
            if(judul.isEmpty()) judul = jDef;

            System.out.print("Edit Artis [" + aDef + "]: ");
            String artis = sc.nextLine();
            if(artis.isEmpty()) artis = aDef;

            System.out.print("Edit Album [" + albDef + "]: ");
            String album = sc.nextLine();
            if(album.isEmpty()) album = albDef;

            databaseLagu.add(new Lagu(judul, artis, album, file.getPath()));
            simpanDataKeCSV();
            System.out.println("Lagu berhasil ditambahkan!");

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public static void tampilkanPlaylist() {
        System.out.println("\n--- DAFTAR PLAYLIST ---");
        if (databaseLagu.isEmpty()) {
            System.out.println("(Kosong)");
        } else {
            for (int i = 0; i < databaseLagu.size(); i++) {
                System.out.println((i + 1) + ". " + databaseLagu.get(i).getInfo());
            }
        }
    }

    public static void hapusLagu(Scanner sc) {
        tampilkanPlaylist();
        if (databaseLagu.isEmpty()) return;

        System.out.print("Pilih nomor lagu yang akan dihapus: ");
        int idx = Integer.parseInt(sc.nextLine()) - 1;

        if (idx >= 0 && idx < databaseLagu.size()) {
            if (idx == currentIndex) stopLagu();
            databaseLagu.remove(idx);
            simpanDataKeCSV();
            System.out.println("Lagu berhasil dihapus.");
        } else {
            System.out.println("Nomor tidak valid.");
        }
    }

    // --- DATABASE CSV ---
    private static void simpanDataKeCSV() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(DATABASE_FILE))) {
            for (Lagu lagu : databaseLagu) {
                // Gunakan semicolon (;) jika judul lagu mengandung koma
                writer.println(lagu.getJudul() + "," + lagu.getArtis() + "," + lagu.getAlbum() + "," + lagu.getPath());
            }
        } catch (IOException e) {
            System.out.println("Gagal simpan CSV.");
        }
    }

    private static void loadDataDariCSV() {
        File file = new File(DATABASE_FILE);
        if (!file.exists()) return;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String baris;
            while ((baris = reader.readLine()) != null) {
                String[] data = baris.split(",");
                if (data.length == 4) {
                    databaseLagu.add(new Lagu(data[0], data[1], data[2], data[3]));
                }
            }
        } catch (IOException e) {
            System.out.println("Gagal muat CSV.");
        }
    }
}