Moch. Musa Chusnaini (202410370110411)
Faris Ramadhan Riyanto (202410370110402)

# 📖 Panduan Penggunaan Fitur MusicFlow

Dokumen ini berisi panduan langkah demi langkah untuk menjalankan fitur-fitur utama pada aplikasi MusicFlow. Pastikan aplikasi sudah dijalankan (Run `MusicFlow.java`) sebelum mengikuti panduan ini.

## 1. 🎵 Manajemen Library (Daftar Lagu)
Fitur ini menampilkan seluruh koleksi lagu yang tersimpan di dalam database.

Cara Mengakses:
    1.  Buka aplikasi, secara default Anda akan berada di tab **Library**.
    2.  Jika sedang di menu lain, klik tombol **"Library"** pada Sidebar kiri.
* **Cara Kerja:**
    * Aplikasi akan otomatis membaca file `musics.csv`.
    * Daftar lagu akan muncul dalam format tabel (Judul, Artis, Album).
    * Jika tabel kosong, artinya belum ada lagu yang ditambahkan.

## 2. 🎧 Player Control (Memutar Musik)
Fitur untuk mengontrol pemutaran audio (Play, Stop, Next, Previous).

* **Cara Memutar Lagu:**
    * Klik salah satu baris lagu di tabel **Library** atau di dalam **Playlist**.
    * Lagu akan langsung diputar dan informasi lagu muncul di panel Player.
* **Kontrol Player (Panel Bawah):**
    * **⏮ (Previous):** Kembali ke lagu sebelumnya.
    * **⏹ (Stop/Gradient Button):** Menghentikan lagu yang sedang berjalan.
    * **⏭ (Next):** Melompat ke lagu berikutnya.
* **Fitur Otomatis:**
    * Jika lagu selesai diputar, aplikasi akan otomatis memutar lagu selanjutnya (*Auto-Next*).

## 3. ➕ Add Music (Menambah Lagu Baru)
Fitur untuk mengimpor file MP3 dari komputer ke dalam Library aplikasi.

* **Langkah-langkah:**
    1.  Klik menu **"Add Music"** pada Sidebar kiri.
    2.  Isi **Judul**, **Artis**, dan **Album** secara manual, ATAU:
    3.  Klik tombol **"Choose File"** untuk memilih file `.mp3` dari komputer Anda.
        * *Catatan:* Aplikasi akan otomatis mengisi form Judul/Artis jika file MP3 memiliki metadata.
    4.  Klik tombol **"♫ Add to Library"**.
    5.  Pesan "Berhasil ditambahkan!" akan muncul, dan lagu tersimpan permanen di `musics.csv`.

## 4. 📂 Playlist System (Manajemen Playlist)
Fitur untuk mengelompokkan lagu ke dalam album/playlist khusus.

* **Membuat Playlist Baru:**
    1.  Masuk ke menu **"Playlists"**.
    2.  Klik tombol **"+ Create Playlist"**.
    3.  Masukkan **Nama Playlist** dan **Deskripsi**.
    4.  Klik **Save**. Kartu playlist baru akan muncul.
* **Mengubah Nama (Rename) / Hapus Playlist:**
    1.  Pada kartu playlist, klik tombol menu titik tiga (**⋮**).
    2.  Pilih **Rename** untuk mengubah nama, atau **Delete** untuk menghapus playlist.
* **Menambahkan Lagu ke Playlist:**
    1.  Klik pada **Kartu Playlist** untuk masuk ke detail.
    2.  Klik tombol **"+ Add Song"**.
    3.  Pilih lagu dari *Dropdown Menu* (lagu diambil dari Library).
    4.  Klik **Add**.

## 5. 🗑️ Delete Action (Menghapus Lagu)
Fitur untuk menghapus lagu, baik dari Library utama maupun dari Playlist.

* **Menghapus dari Library Utama:**
    1.  Masuk ke menu **Library**.
    2.  Klik icon Titik tiga :  pada lagu yang ingin dihapus.
    3.  Pilih menu munculan **"Delete Song"**.
    4.  *Hasil:* Lagu terhapus dari tabel dan hilang permanen dari `musics.csv`.
* **Menghapus dari Playlist:**
    1.  Masuk ke dalam detail Playlist.
    2.  **Klik titk tiga** pada lagu di dalam list playlist.
    3.  Pilih **"Delete Song"**.
    4.  *Hasil:* Lagu hanya hilang dari playlist tersebut, tetapi masih ada di Library utama.

