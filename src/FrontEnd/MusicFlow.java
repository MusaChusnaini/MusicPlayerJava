package FrontEnd;

// --- IMPORT LENGKAP ---
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.RoundRectangle2D;
import java.io.*;
import java.util.ArrayList;
import java.util.Random;

public class MusicFlow extends JFrame {

    // --- PALET WARNA ---
    public static final Color COL_BG_MAIN = new Color(18, 18, 18);
    public static final Color COL_SIDEBAR = new Color(10, 10, 10);
    public static final Color COL_ACCENT  = new Color(189, 0, 255);
    public static final Color COL_ACCENT_2= new Color(255, 0, 128);
    public static final Color COL_CARD    = new Color(30, 30, 30);
    public static final Color COL_INPUT   = new Color(40, 40, 40);
    public static final Color COL_TEXT    = Color.WHITE;
    public static final Color COL_TEXT_SEC= new Color(170, 170, 170);

    private final String SPLIT_CHAR = ",";

    private CardLayout cardLayout = new CardLayout();
    private JPanel mainContentPanel = new JPanel(cardLayout);
    private java.util.List<SidebarButton> navButtons = new ArrayList<>();

    // --- LOGIKA PLAYER & QUEUE ---
    private PlayerPanel playerPanel;

    // List Global (Semua lagu di Library)
    private ArrayList<String[]> globalSongList = new ArrayList<>();

    // Antrian Aktif (Bisa berisi Global, bisa berisi Playlist saja)
    private ArrayList<String[]> currentQueue = new ArrayList<>();
    private int currentQueueIndex = -1;

    public MusicFlow() {
        setTitle("MusicFlow Premium");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Load data library ke memori
        loadGlobalSongData();

        add(createSidebar(), BorderLayout.WEST);

        mainContentPanel.setBackground(COL_BG_MAIN);

        playerPanel = new PlayerPanel(this);

        mainContentPanel.add(playerPanel, "PLAYER");
        mainContentPanel.add(new LibraryPanel(this), "LIBRARY");
        mainContentPanel.add(new PlaylistManagerPanel(this), "PLAYLISTS");
        mainContentPanel.add(new AddMusicPanel(this), "ADD_MUSIC");

        add(mainContentPanel, BorderLayout.CENTER);

        navigate("LIBRARY");
    }

    // --- FUNGSI UTAMA PLAYER ---

    public void loadGlobalSongData() {
        globalSongList.clear();
        try {
            File file = new File("musics.csv");
            if (file.exists()) {
                BufferedReader br = new BufferedReader(new FileReader(file));
                String line;
                while ((line = br.readLine()) != null) {
                    String[] data = line.split(SPLIT_CHAR);
                    if (data.length >= 4) {
                        globalSongList.add(data);
                    }
                }
                br.close();
            }
        } catch (Exception e) {}
    }

    // Method untuk mengatur antrian aktif (Dipanggil saat klik lagu di Library atau Playlist)
    public void setQueue(ArrayList<String[]> newQueue, int startIndex) {
        // Copy isi queue agar aman
        this.currentQueue = new ArrayList<>(newQueue);
        this.currentQueueIndex = startIndex;

        // Mainkan lagu yang dipilih
        playSongByQueueIndex(currentQueueIndex);
    }

    // Helper: Putar lagu berdasarkan index di dalam antrian (Queue)
    private void playSongByQueueIndex(int index) {
        if (index >= 0 && index < currentQueue.size()) {
            String[] data = currentQueue.get(index);
            String title = data[0];
            String artist = data[1];

            // --- CARI REAL INDEX & GAMBAR ---
            // Kita harus cari "index asli" di musics.csv untuk mengambil Cover Album dari Backend
            int realIndexGlobal = -1;
            for(int i=0; i<globalSongList.size(); i++) {
                // Mencocokkan Judul & Artis untuk menemukan ID aslinya
                if(globalSongList.get(i)[0].equals(title) && globalSongList.get(i)[1].equals(artist)) {
                    realIndexGlobal = i;
                    break;
                }
            }

            // Ambil gambar cover
            ImageIcon albumIcon = null;
            if(realIndexGlobal != -1) {
                try {
                    // Panggil Backend play lagu (Audio)
                    BackEnd.MusicManager.playLagu(realIndexGlobal);

                    // Panggil Backend ambil gambar
                    byte[] rawImg = BackEnd.MusicManager.getRawCover(realIndexGlobal);
                    if(rawImg != null) albumIcon = new ImageIcon(rawImg);
                } catch(Exception e) {}
            }

            // Update UI
            playerPanel.setSongInfo(title, artist, albumIcon);
            navigate("PLAYER");
        }
    }

    // Logika Next (Berdasarkan Queue, bukan Global)
    public void playNext() {
        if (!currentQueue.isEmpty() && currentQueueIndex < currentQueue.size() - 1) {
            currentQueueIndex++;
            playSongByQueueIndex(currentQueueIndex);
        } else {
            JOptionPane.showMessageDialog(this, "Akhir dari playlist.");
        }
    }

    // Logika Previous (Berdasarkan Queue, bukan Global)
    public void playPrev() {
        if (!currentQueue.isEmpty() && currentQueueIndex > 0) {
            currentQueueIndex--;
            playSongByQueueIndex(currentQueueIndex);
        } else {
            JOptionPane.showMessageDialog(this, "Awal dari playlist.");
        }
    }

    private void navigate(String sceneName) {
        cardLayout.show(mainContentPanel, sceneName);
        for (SidebarButton btn : navButtons) btn.setActive(btn.targetScene.equals(sceneName));
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(COL_SIDEBAR);
        sidebar.setPreferredSize(new Dimension(240, getHeight()));
        sidebar.setBorder(new EmptyBorder(30, 20, 30, 20));

        JLabel logo = new JLabel(" MusicFlow");
        logo.setIcon(new IconSymbol(COL_ACCENT, 24));
        logo.setFont(new Font("SansSerif", Font.BOLD, 22));
        logo.setForeground(Color.WHITE);
        logo.setAlignmentX(Component.LEFT_ALIGNMENT);

        sidebar.add(logo);
        sidebar.add(Box.createVerticalStrut(50));

        navButtons.add(new SidebarButton("Player", "PLAYER"));
        navButtons.add(new SidebarButton("Library", "LIBRARY"));
        navButtons.add(new SidebarButton("Playlists", "PLAYLISTS"));
        navButtons.add(new SidebarButton("Add Music", "ADD_MUSIC"));

        for (SidebarButton btn : navButtons) {
            btn.addActionListener(e -> navigate(btn.targetScene));
            sidebar.add(btn); sidebar.add(Box.createVerticalStrut(10));
        }
        sidebar.add(Box.createVerticalGlue());
        return sidebar;
    }

    public static void main(String[] args) {
        try { BackEnd.MusicManager.loadDataDariCSV(); } catch(Exception e) {}
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception e){}
        SwingUtilities.invokeLater(() -> new MusicFlow().setVisible(true));
    }

    // ========================================================
    // SCENE: LIBRARY
    // ========================================================
    class LibraryPanel extends JPanel {
        public LibraryPanel(MusicFlow frame) {
            setLayout(new BorderLayout()); setBackground(COL_BG_MAIN); setBorder(new EmptyBorder(40, 40, 40, 40));
            JLabel title = new JLabel("Your Library"); title.setFont(new Font("SansSerif", Font.BOLD, 32)); title.setForeground(COL_TEXT); add(title, BorderLayout.NORTH);

            String[] cols = {"#", "TITLE", "ARTIST", "ALBUM", ""};
            DefaultTableModel model = new DefaultTableModel(cols, 0) { public boolean isCellEditable(int r, int c) { return false; } };

            loadDataToModel(model);

            JTable table = new JTable(model);
            setupTableLogic(table, model, this,
                    (t, a) -> { // PLAY
                        int row = table.getSelectedRow();
                        // [PERBAIKAN] Set Antrian menjadi Global List saat main dari Library
                        frame.setQueue(globalSongList, row);
                    },
                    (row) -> { // DELETE
                        deleteSongFromCSV(row);
                        model.removeRow(row);
                        for(int i=0; i<model.getRowCount(); i++) model.setValueAt(String.valueOf(i+1), i, 0);
                        frame.loadGlobalSongData();
                    }
            );
            JScrollPane scroll = new JScrollPane(table); scroll.getViewport().setBackground(COL_BG_MAIN); scroll.setBorder(null); add(scroll, BorderLayout.CENTER);
        }

        private void loadDataToModel(DefaultTableModel model) {
            try {
                File file = new File("musics.csv");
                if (file.exists()) {
                    BufferedReader br = new BufferedReader(new FileReader(file)); String line; int rowNum = 1;
                    while ((line = br.readLine()) != null) {
                        String[] data = line.split(SPLIT_CHAR);
                        if (data.length >= 4) {
                            model.addRow(new Object[]{ String.valueOf(rowNum++), data[0], data[1], data[2], "⋮" });
                        }
                    } br.close();
                }
            } catch (Exception e) {}
        }

        private void deleteSongFromCSV(int rowToDelete) {
            try {
                File inputFile = new File("musics.csv");
                File tempFile = new File("musics_temp.csv");
                BufferedReader reader = new BufferedReader(new FileReader(inputFile));
                BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));
                String currentLine; int i = 0;
                while ((currentLine = reader.readLine()) != null) {
                    if (i != rowToDelete) { writer.write(currentLine + System.getProperty("line.separator")); }
                    i++;
                }
                writer.close(); reader.close();
                if (!inputFile.delete()) return;
                if (!tempFile.renameTo(inputFile)) return;
                BackEnd.MusicManager.loadDataDariCSV();
            } catch (IOException e) { e.printStackTrace(); }
        }
    }

    // ========================================================
    // SCENE: PLAYLIST MANAGER
    // ========================================================
    class PlaylistManagerPanel extends JPanel {
        private CardLayout plLayout = new CardLayout();
        private JPanel plContent = new JPanel(plLayout);
        private MusicFlow mainFrame;
        private PlaylistGrid gridView;
        private ArrayList<PlaylistData> playlists = new ArrayList<>();
        private final String CSV_FILE = "data_playlists.csv";
        private final String SONG_DELIMITER = "||";

        class PlaylistData {
            String title, desc; Color color;
            ArrayList<String[]> songs = new ArrayList<>();
            public PlaylistData(String t, String d, Color c) { title = t; desc = d; color = c; }
        }

        public PlaylistManagerPanel(MusicFlow frame) {
            this.mainFrame = frame;
            setLayout(new BorderLayout());
            add(plContent, BorderLayout.CENTER);
            loadPlaylists();
            gridView = new PlaylistGrid();
            plContent.add(gridView, "GRID");
            plLayout.show(plContent, "GRID");
        }

        private void savePlaylists() {
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(CSV_FILE))) {
                for (PlaylistData p : playlists) {
                    StringBuilder sb = new StringBuilder();
                    sb.append(p.title).append(SPLIT_CHAR).append(p.desc).append(SPLIT_CHAR);
                    for (int i = 0; i < p.songs.size(); i++) {
                        String[] s = p.songs.get(i);
                        String dur = (s.length > 3) ? s[3] : "3:00";
                        String path = (s.length > 4) ? s[4] : "";
                        sb.append(s[0]).append(SPLIT_CHAR).append(s[1]).append(SPLIT_CHAR).append(s[2]).append(SPLIT_CHAR).append(dur).append(SPLIT_CHAR).append(path);
                        if (i < p.songs.size() - 1) sb.append(SONG_DELIMITER);
                    }
                    bw.write(sb.toString()); bw.newLine();
                }
            } catch (Exception e) { e.printStackTrace(); }
        }

        private void loadPlaylists() {
            playlists.clear();
            File f = new File(CSV_FILE);
            if (!f.exists()) return;
            try (BufferedReader br = new BufferedReader(new FileReader(f))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] parts = line.split(SPLIT_CHAR, 3);
                    if (parts.length >= 2) {
                        PlaylistData newPl = new PlaylistData(parts[0], parts[1], getRandomColor());
                        if (parts.length > 2 && !parts[2].isEmpty()) {
                            String[] songList = parts[2].split("\\|\\|");
                            for (String sRaw : songList) {
                                String[] sDet = sRaw.split(SPLIT_CHAR);
                                if (sDet.length >= 4) newPl.songs.add(sDet);
                            }
                        }
                        playlists.add(newPl);
                    }
                }
            } catch (Exception e) { e.printStackTrace(); }
        }

        class PlaylistGrid extends JPanel {
            private JPanel gridContainer;
            public PlaylistGrid() {
                setLayout(new BorderLayout()); setBackground(COL_BG_MAIN); setBorder(new EmptyBorder(40, 40, 40, 40));
                JPanel header = new JPanel(new BorderLayout()); header.setBackground(COL_BG_MAIN);
                JLabel title = new JLabel("Your Playlists"); title.setFont(new Font("SansSerif", Font.BOLD, 32)); title.setForeground(COL_TEXT);
                PurpleButton btnCreate = new PurpleButton("+ Create Playlist"); btnCreate.addActionListener(e -> showCreateDialog());
                header.add(title, BorderLayout.WEST); header.add(btnCreate, BorderLayout.EAST); add(header, BorderLayout.NORTH);
                gridContainer = new JPanel(new FlowLayout(FlowLayout.LEFT, 30, 30)); gridContainer.setBackground(COL_BG_MAIN);
                refreshGrid();
                JScrollPane scroll = new JScrollPane(gridContainer); scroll.setBorder(null); scroll.getViewport().setBackground(COL_BG_MAIN);
                add(scroll, BorderLayout.CENTER);
            }
            public void refreshGrid() {
                gridContainer.removeAll();
                for (PlaylistData p : playlists) gridContainer.add(createCard(p));
                gridContainer.revalidate(); gridContainer.repaint();
            }

            private JPanel createCard(PlaylistData p) {
                JPanel card = new JPanel(); card.setPreferredSize(new Dimension(220, 290)); card.setBackground(COL_CARD);
                card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS)); card.setBorder(new EmptyBorder(15, 15, 15, 15)); card.setCursor(new Cursor(Cursor.HAND_CURSOR));

                JPanel cover = new JPanel(new GridBagLayout()); cover.setPreferredSize(new Dimension(190, 180)); cover.setBackground(p.color);
                JLabel icon = new JLabel("▶"); icon.setForeground(Color.WHITE); icon.setFont(new Font("Segoe UI Symbol", Font.BOLD, 24)); cover.add(icon);

                JPanel infoPanel = new JPanel(new BorderLayout()); infoPanel.setBackground(COL_CARD); infoPanel.setMaximumSize(new Dimension(200, 60));
                JPanel textP = new JPanel(new GridLayout(2,1)); textP.setOpaque(false);
                JLabel lTitle = new JLabel(p.title); lTitle.setFont(new Font("SansSerif", Font.BOLD, 16)); lTitle.setForeground(Color.WHITE);
                JLabel lDesc = new JLabel(p.desc); lDesc.setFont(new Font("SansSerif", Font.PLAIN, 12)); lDesc.setForeground(COL_TEXT_SEC);
                textP.add(lTitle); textP.add(lDesc);

                JButton btnMenu = new JButton("⋮");
                btnMenu.setFont(new Font("Segoe UI Symbol", Font.BOLD, 18));
                btnMenu.setForeground(COL_TEXT_SEC);
                btnMenu.setContentAreaFilled(false); btnMenu.setBorderPainted(false); btnMenu.setFocusPainted(false);
                btnMenu.setCursor(new Cursor(Cursor.HAND_CURSOR));

                JPopupMenu popup = new JPopupMenu();
                popup.setBackground(COL_CARD);
                popup.setBorder(new LineBorder(new Color(60,60,60)));

                JMenuItem itemRename = new JMenuItem("Rename");
                itemRename.setFont(new Font("Segoe UI", Font.BOLD, 13));
                itemRename.setBackground(COL_CARD);
                itemRename.setForeground(new Color(5, 5, 5));
                itemRename.addActionListener(e -> showRenameDialog(p));

                JMenuItem itemDelete = new JMenuItem("Delete");
                itemDelete.setBackground(COL_CARD);
                itemDelete.setForeground(new Color(255, 80, 80));
                itemDelete.addActionListener(e -> {
                    int confirm = JOptionPane.showConfirmDialog(this, "Delete playlist '" + p.title + "'?", "Confirm", JOptionPane.YES_NO_OPTION);
                    if(confirm == JOptionPane.YES_OPTION) { playlists.remove(p); savePlaylists(); refreshGrid(); }
                });

                popup.add(itemRename); popup.add(itemDelete);
                btnMenu.addActionListener(e -> popup.show(btnMenu, 0, btnMenu.getHeight()));
                infoPanel.add(textP, BorderLayout.CENTER); infoPanel.add(btnMenu, BorderLayout.EAST);
                card.add(cover); card.add(Box.createVerticalStrut(15)); card.add(infoPanel);
                card.addMouseListener(new MouseAdapter() { public void mouseClicked(MouseEvent e) { openDetail(p); } });
                return card;
            }

            private void openDetail(PlaylistData data) {
                plContent.add(new PlaylistDetail(data), "DETAIL_TEMP"); plLayout.show(plContent, "DETAIL_TEMP");
            }
        }

        class PlaylistDetail extends JPanel {
            private PlaylistData currentData;
            private DefaultTableModel model;
            public PlaylistDetail(PlaylistData data) {
                this.currentData = data; setLayout(new BorderLayout()); setBackground(COL_BG_MAIN); setBorder(new EmptyBorder(40, 40, 0, 40));
                JPanel header = new JPanel(new BorderLayout()); header.setBackground(COL_BG_MAIN);
                JButton btnBack = new JButton("← Back to Playlists"); btnBack.setForeground(COL_ACCENT); btnBack.setContentAreaFilled(false); btnBack.setBorderPainted(false);
                btnBack.setFont(new Font("SansSerif", Font.BOLD, 14)); btnBack.setCursor(new Cursor(Cursor.HAND_CURSOR)); btnBack.addActionListener(e -> plLayout.show(plContent, "GRID"));
                JPanel info = new JPanel(); info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS)); info.setBackground(COL_BG_MAIN);
                JLabel title = new JLabel(currentData.title); title.setFont(new Font("SansSerif", Font.BOLD, 40)); title.setForeground(Color.WHITE);
                JLabel desc = new JLabel(currentData.desc); desc.setForeground(COL_TEXT_SEC); info.add(title); info.add(desc);
                PurpleButton btnAddSong = new PurpleButton("+ Add Song"); btnAddSong.setPreferredSize(new Dimension(120, 35)); btnAddSong.addActionListener(e -> showAddSongDialog());
                JPanel topRow = new JPanel(new BorderLayout()); topRow.setBackground(COL_BG_MAIN); topRow.add(btnBack, BorderLayout.WEST);
                JPanel infoRow = new JPanel(new BorderLayout()); infoRow.setBackground(COL_BG_MAIN); infoRow.setBorder(new EmptyBorder(20,0,20,0)); infoRow.add(info, BorderLayout.WEST); infoRow.add(btnAddSong, BorderLayout.EAST);
                header.add(topRow, BorderLayout.NORTH); header.add(infoRow, BorderLayout.CENTER); add(header, BorderLayout.NORTH);

                String[] cols = {"#", "TITLE", "ARTIST", "ALBUM", ""};
                model = new DefaultTableModel(cols, 0) { public boolean isCellEditable(int r, int c) { return false; } };
                loadSongsToTable();
                JTable table = new JTable(model);
                setupTableLogic(table, model, this,
                        (t, a) -> {
                            // [PERBAIKAN] Set Antrian menjadi Lagu Playlist ini saja
                            int playlistIndex = table.getSelectedRow();
                            mainFrame.setQueue(currentData.songs, playlistIndex);
                        },
                        (row) -> {
                    currentData.songs.remove(row); // 1. Hapus dari listsavePlaylists();
                            // 2. Simpan ke CSV
                            loadSongsToTable();
                    savePlaylists(); }
                );
                JScrollPane scroll = new JScrollPane(table); scroll.getViewport().setBackground(COL_BG_MAIN); scroll.setBorder(BorderFactory.createEmptyBorder(0,0,20,0));
                add(scroll, BorderLayout.CENTER);
            }
            private void loadSongsToTable() {
                model.setRowCount(0); int no = 1;
                for (String[] song : currentData.songs) {
                    model.addRow(new Object[]{ String.valueOf(no++), song[0], song[1], song[2], "⋮" });
                }
            }
            private void showAddSongDialog() {
                JDialog d = new JDialog((Frame)SwingUtilities.getWindowAncestor(this), "Add Song", true); d.setSize(400, 200); d.setLocationRelativeTo(this); d.setUndecorated(true);
                JPanel p = new JPanel(new BorderLayout()); p.setBackground(new Color(30,30,30)); p.setBorder(BorderFactory.createLineBorder(new Color(60,60,60)));
                JLabel lbl = new JLabel("Select a song from Library:"); lbl.setForeground(Color.WHITE); lbl.setBorder(new EmptyBorder(15,15,15,15));
                JComboBox<String> comboSongs = new JComboBox<>(); ArrayList<String[]> librarySongs = new ArrayList<>();
                try {
                    File file = new File("musics.csv");
                    if(file.exists()) {
                        BufferedReader br = new BufferedReader(new FileReader(file)); String line;
                        while((line = br.readLine()) != null) {
                            String[] data = line.split(SPLIT_CHAR);
                            if(data.length >= 4) { librarySongs.add(data); comboSongs.addItem(data[0] + " - " + data[1]); }
                        } br.close();
                    }
                } catch(Exception e) {}
                JPanel centerP = new JPanel(); centerP.setOpaque(false); centerP.add(comboSongs);
                JPanel btnP = new JPanel(new FlowLayout(FlowLayout.RIGHT)); btnP.setOpaque(false);
                PurpleButton btnAdd = new PurpleButton("Add"); JButton btnCancel = new JButton("Cancel"); btnCancel.setContentAreaFilled(false); btnCancel.setBorderPainted(false); btnCancel.setForeground(Color.GRAY);
                btnCancel.addActionListener(e -> d.dispose());
                btnAdd.addActionListener(e -> {
                    int idx = comboSongs.getSelectedIndex();
                    if(idx >= 0) {
                        String[] selected = librarySongs.get(idx);
                        boolean exists = false;
                        for(String[] s : currentData.songs) if(s[0].equals(selected[0])) exists = true;
                        if(!exists) { currentData.songs.add(selected); savePlaylists(); loadSongsToTable(); d.dispose(); }
                        else JOptionPane.showMessageDialog(d, "Song already added!");
                    }
                });
                btnP.add(btnCancel); btnP.add(btnAdd); p.add(lbl, BorderLayout.NORTH); p.add(centerP, BorderLayout.CENTER); p.add(btnP, BorderLayout.SOUTH); d.add(p); d.setVisible(true);
            }
        }

        private void showCreateDialog() { showPlaylistDialog(null); }
        private void showRenameDialog(PlaylistData pData) { showPlaylistDialog(pData); }

        private void showPlaylistDialog(PlaylistData existingData) {
            boolean isEdit = (existingData != null);
            String titleStr = isEdit ? "Rename Playlist" : "Create New Playlist";
            String btnStr = isEdit ? "Save" : "Create";
            JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), titleStr, true);
            dialog.setUndecorated(true); dialog.setSize(400, 320); dialog.setLocationRelativeTo(this);
            JPanel p = new JPanel(); p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS)); p.setBackground(new Color(30, 30, 30)); p.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(60, 60, 60)), new EmptyBorder(25, 25, 25, 25)));
            JLabel l = new JLabel(titleStr); l.setForeground(Color.WHITE); l.setFont(new Font("SansSerif", Font.BOLD, 20)); l.setAlignmentX(LEFT_ALIGNMENT);
            JLabel lName = new JLabel("Name"); lName.setForeground(COL_TEXT_SEC); lName.setAlignmentX(LEFT_ALIGNMENT); JTextField tfName = new JTextField(); tfName.setBackground(new Color(50,50,50)); tfName.setForeground(Color.WHITE); tfName.setCaretColor(Color.WHITE); tfName.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10)); tfName.setMaximumSize(new Dimension(400, 35)); tfName.setAlignmentX(LEFT_ALIGNMENT);
            JLabel lDesc = new JLabel("Description"); lDesc.setForeground(COL_TEXT_SEC); lDesc.setAlignmentX(LEFT_ALIGNMENT); JTextArea taDesc = new JTextArea(3, 20); taDesc.setBackground(new Color(50,50,50)); taDesc.setForeground(Color.WHITE); taDesc.setCaretColor(Color.WHITE); taDesc.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10)); JScrollPane scrollDesc = new JScrollPane(taDesc); scrollDesc.setBorder(null); scrollDesc.setMaximumSize(new Dimension(400, 80)); scrollDesc.setAlignmentX(LEFT_ALIGNMENT);
            if(isEdit) { tfName.setText(existingData.title); taDesc.setText(existingData.desc); }
            PurpleButton btn = new PurpleButton(btnStr); btn.setAlignmentX(LEFT_ALIGNMENT);
            btn.addActionListener(e -> {
                String name = tfName.getText(); String desc = taDesc.getText();
                if (!name.trim().isEmpty()) {
                    if(isEdit) { existingData.title = name; existingData.desc = desc; }
                    else { playlists.add(new PlaylistData(name, desc, getRandomColor())); }
                    savePlaylists(); gridView.refreshGrid(); dialog.dispose();
                } else { JOptionPane.showMessageDialog(dialog, "Name required!"); }
            });
            JButton cancel = new JButton("Cancel"); cancel.setContentAreaFilled(false); cancel.setBorderPainted(false); cancel.setForeground(Color.GRAY); cancel.addActionListener(e -> dialog.dispose());
            p.add(l); p.add(Box.createVerticalStrut(20)); p.add(lName); p.add(Box.createVerticalStrut(5)); p.add(tfName); p.add(Box.createVerticalStrut(15)); p.add(lDesc); p.add(Box.createVerticalStrut(5)); p.add(scrollDesc); p.add(Box.createVerticalStrut(25));
            JPanel btnP = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0)); btnP.setOpaque(false); btnP.setAlignmentX(LEFT_ALIGNMENT); btnP.setMaximumSize(new Dimension(400, 40)); btnP.add(cancel); btnP.add(btn); p.add(btnP); dialog.add(p); dialog.setVisible(true);
        }
        private Color getRandomColor() { Random r = new Random(); return new Color(r.nextInt(100), r.nextInt(100), r.nextInt(150) + 50); }
    }

    // ========================================================
    // SCENE: ADD MUSIC
    // ========================================================
    class AddMusicPanel extends JPanel {
        private JTextField txtTitle, txtArtist, txtAlbum, txtDuration, txtFilePath;
        private MusicFlow mainFrame;

        public AddMusicPanel(MusicFlow frame) {
            this.mainFrame = frame;
            setLayout(new BorderLayout()); setBackground(COL_BG_MAIN);
            JPanel content = new JPanel(); content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS)); content.setBackground(COL_BG_MAIN); content.setBorder(new EmptyBorder(40, 60, 40, 60));
            JLabel title = new JLabel("Add New Music"); title.setFont(new Font("SansSerif", Font.BOLD, 32)); title.setForeground(COL_TEXT); title.setAlignmentX(LEFT_ALIGNMENT); content.add(title); content.add(Box.createVerticalStrut(30));
            JPanel formContainer = new JPanel(); formContainer.setLayout(new BoxLayout(formContainer, BoxLayout.Y_AXIS)); formContainer.setBackground(COL_BG_MAIN); formContainer.setAlignmentX(LEFT_ALIGNMENT);
            JPanel albumSection = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0)); albumSection.setBackground(COL_BG_MAIN); albumSection.setAlignmentX(LEFT_ALIGNMENT);
            JPanel singleCoverPanel = new ArtPanel(1, 180, 180); JPanel coverWrapper = new JPanel(new BorderLayout(0, 10)); coverWrapper.setBackground(COL_BG_MAIN);
            JLabel lblCover = new JLabel("Album Cover"); lblCover.setForeground(COL_TEXT_SEC); coverWrapper.add(lblCover, BorderLayout.NORTH); coverWrapper.add(singleCoverPanel, BorderLayout.CENTER);
            albumSection.add(coverWrapper); formContainer.add(albumSection); formContainer.add(Box.createVerticalStrut(25));
            JPanel inputGrid = new JPanel(new GridBagLayout()); inputGrid.setBackground(COL_BG_MAIN); inputGrid.setAlignmentX(LEFT_ALIGNMENT); inputGrid.setMaximumSize(new Dimension(800, 150));
            GridBagConstraints gbc = new GridBagConstraints(); gbc.fill = GridBagConstraints.HORIZONTAL; gbc.insets = new Insets(10, 0, 10, 20); gbc.weightx = 0.5;
            txtTitle = createStyledTextField(); txtArtist = createStyledTextField(); txtAlbum = createStyledTextField(); txtDuration = createStyledTextField();
            gbc.gridx = 0; gbc.gridy = 0; inputGrid.add(createInputWrapper("Song Title *", txtTitle), gbc); gbc.gridx = 1; inputGrid.add(createInputWrapper("Artist *", txtArtist), gbc);
            gbc.gridx = 0; gbc.gridy = 1; inputGrid.add(createInputWrapper("Album", txtAlbum), gbc); gbc.gridx = 1; inputGrid.add(createInputWrapper("Duration", txtDuration), gbc);
            formContainer.add(inputGrid); formContainer.add(Box.createVerticalStrut(20));
            JLabel lblAudio = new JLabel("Audio File"); lblAudio.setForeground(COL_TEXT_SEC); lblAudio.setAlignmentX(LEFT_ALIGNMENT); formContainer.add(lblAudio); formContainer.add(Box.createVerticalStrut(5));
            JPanel filePanel = new JPanel(new BorderLayout(10, 0)); filePanel.setBackground(COL_BG_MAIN); filePanel.setMaximumSize(new Dimension(800, 45)); filePanel.setAlignmentX(LEFT_ALIGNMENT);
            txtFilePath = new JTextField("No file selected"); txtFilePath.setEditable(false); txtFilePath.setBackground(COL_INPUT); txtFilePath.setForeground(Color.GRAY); txtFilePath.setBorder(BorderFactory.createCompoundBorder(new RoundedBorder(10), new EmptyBorder(10, 15, 10, 15)));
            JButton btnBrowse = new JButton("Choose File"); btnBrowse.setPreferredSize(new Dimension(120, 45)); btnBrowse.setBackground(new Color(50, 50, 50)); btnBrowse.setForeground(Color.WHITE); btnBrowse.setFocusPainted(false); btnBrowse.setBorderPainted(false); btnBrowse.setFont(new Font("SansSerif", Font.BOLD, 12)); btnBrowse.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btnBrowse.addActionListener(e -> {
                JFileChooser fileChooser = new JFileChooser(); fileChooser.setDialogTitle("Select Audio File");
                fileChooser.setFileFilter(new FileNameExtensionFilter("Audio Files (WAV, MP3)", "wav", "mp3"));
                if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = fileChooser.getSelectedFile();
                    txtFilePath.setText(selectedFile.getAbsolutePath()); txtFilePath.setForeground(Color.WHITE);
                    txtTitle.setText(selectedFile.getName().replace(".mp3","").replace(".wav",""));
                    txtDuration.setText("3:00");
                }
            });
            filePanel.add(txtFilePath, BorderLayout.CENTER); filePanel.add(btnBrowse, BorderLayout.EAST); formContainer.add(filePanel); formContainer.add(Box.createVerticalStrut(30));
            JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0)); btnPanel.setBackground(COL_BG_MAIN); btnPanel.setAlignmentX(LEFT_ALIGNMENT); btnPanel.setMaximumSize(new Dimension(800, 50));
            GradientButton btnAdd = new GradientButton("♫ Add to Library"); btnAdd.setPreferredSize(new Dimension(500, 45));
            btnAdd.addActionListener(e -> {
                String t = txtTitle.getText(), a = txtArtist.getText(), al = txtAlbum.getText(), d = txtDuration.getText(), p = txtFilePath.getText();
                if(t.isEmpty() || p.equals("No file selected")) { JOptionPane.showMessageDialog(this, "Please select file!"); return; }
                if(d.isEmpty()) d = "3:00";
                try (BufferedWriter bw = new BufferedWriter(new FileWriter("musics.csv", true))) {
                    bw.write(t + SPLIT_CHAR + a + SPLIT_CHAR + al + SPLIT_CHAR + d + SPLIT_CHAR + p);
                    bw.newLine();
                    BackEnd.MusicManager.loadDataDariCSV();
                    mainFrame.loadGlobalSongData();
                    JOptionPane.showMessageDialog(this, "Song added!");
                    txtTitle.setText(""); txtArtist.setText(""); txtAlbum.setText(""); txtDuration.setText(""); txtFilePath.setText("No file selected");
                } catch (Exception ex) { ex.printStackTrace(); }
            });
            JButton btnClear = new JButton("Clear"); btnClear.setPreferredSize(new Dimension(100, 45)); btnClear.setBackground(new Color(50,50,50)); btnClear.setForeground(Color.WHITE); btnClear.setFocusPainted(false); btnClear.setBorderPainted(false);
            btnPanel.add(btnAdd); btnPanel.add(btnClear); formContainer.add(btnPanel); content.add(formContainer); content.add(Box.createVerticalStrut(40));
            JPanel cardsPanel = new JPanel(new GridLayout(1, 3, 20, 0)); cardsPanel.setBackground(COL_BG_MAIN); cardsPanel.setMaximumSize(new Dimension(800, 120)); cardsPanel.setAlignmentX(LEFT_ALIGNMENT);
            cardsPanel.add(createFeatureCard("♫", "Quick", "Easy to add", new Color(189, 0, 255))); cardsPanel.add(createFeatureCard("⬆", "Simple", "", new Color(255, 0, 128))); cardsPanel.add(createFeatureCard("✔", "Instant", "Ready to play", new Color(0, 255, 128)));
            content.add(cardsPanel); JScrollPane scroll = new JScrollPane(content); scroll.setBorder(null); scroll.getVerticalScrollBar().setUnitIncrement(16); add(scroll, BorderLayout.CENTER);
        }
        private JTextField createStyledTextField() { JTextField t = new JTextField(); t.setBackground(COL_INPUT); t.setForeground(Color.WHITE); t.setCaretColor(Color.WHITE); t.setBorder(BorderFactory.createCompoundBorder(new RoundedBorder(10), new EmptyBorder(10, 15, 10, 15))); return t; }
        private JPanel createInputWrapper(String label, JTextField field) { JPanel p = new JPanel(new BorderLayout(0, 5)); p.setBackground(COL_BG_MAIN); JLabel l = new JLabel(label); l.setForeground(COL_TEXT_SEC); p.add(l, BorderLayout.NORTH); p.add(field, BorderLayout.CENTER); return p; }
        private JPanel createFeatureCard(String icon, String title, String desc, Color c) { JPanel card = new JPanel(); card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS)); card.setBackground(COL_SIDEBAR); card.setBorder(BorderFactory.createLineBorder(new Color(50, 50, 50), 1, true)); JLabel l1 = new JLabel(icon); l1.setFont(new Font("SansSerif", Font.PLAIN, 30)); l1.setForeground(c); l1.setAlignmentX(CENTER_ALIGNMENT); JLabel l2 = new JLabel(title); l2.setFont(new Font("SansSerif", Font.BOLD, 16)); l2.setForeground(Color.WHITE); l2.setAlignmentX(CENTER_ALIGNMENT); JLabel l3 = new JLabel(desc); l3.setFont(new Font("SansSerif", Font.PLAIN, 12)); l3.setForeground(Color.GRAY); l3.setAlignmentX(CENTER_ALIGNMENT); card.add(Box.createVerticalGlue()); card.add(l1); card.add(Box.createVerticalStrut(10)); card.add(l2); card.add(Box.createVerticalStrut(5)); card.add(l3); card.add(Box.createVerticalGlue()); return card; }
    }

    // ========================================================
    // CLASS: PLAYER PANEL
    // ========================================================
    class PlayerPanel extends JPanel {
        private JLabel lblTitle, lblArtist, lblCover;
        private GradientStopButton barPlayBtn;
        private MusicFlow mainFrame;

        public PlayerPanel(MusicFlow frame) {
            this.mainFrame = frame;
            setLayout(new BorderLayout()); setBackground(COL_BG_MAIN);
            JPanel centerPanel = new JPanel(new GridBagLayout()); centerPanel.setOpaque(false);
            JPanel contentBox = new JPanel(); contentBox.setLayout(new BoxLayout(contentBox, BoxLayout.Y_AXIS)); contentBox.setOpaque(false);
            lblCover = new JLabel(); lblCover.setAlignmentX(CENTER_ALIGNMENT); lblCover.setPreferredSize(new Dimension(300, 300));
            setPlaceholderCover();
            lblTitle = new JLabel("No Song Playing"); lblTitle.setFont(new Font("SansSerif", Font.BOLD, 32)); lblTitle.setForeground(Color.WHITE); lblTitle.setAlignmentX(CENTER_ALIGNMENT);
            lblArtist = new JLabel("Select from library"); lblArtist.setForeground(COL_TEXT_SEC); lblArtist.setFont(new Font("SansSerif", Font.PLAIN, 18)); lblArtist.setAlignmentX(CENTER_ALIGNMENT);
            contentBox.add(lblCover); contentBox.add(Box.createVerticalStrut(30)); contentBox.add(lblTitle); contentBox.add(Box.createVerticalStrut(10)); contentBox.add(lblArtist); contentBox.add(Box.createVerticalStrut(40));
            centerPanel.add(contentBox); add(centerPanel, BorderLayout.CENTER); add(createFullPlaybackBar(), BorderLayout.SOUTH);
        }
        public void setSongInfo(String title, String artist, ImageIcon coverIcon) {
            lblTitle.setText(title); lblArtist.setText(artist);
            if(coverIcon != null) { Image img = coverIcon.getImage().getScaledInstance(300, 300, Image.SCALE_SMOOTH); lblCover.setIcon(new ImageIcon(img)); } else setPlaceholderCover();
            repaint();
        }
        private void setPlaceholderCover() {
            lblCover.setIcon(new Icon() {
                public void paintIcon(Component c, Graphics g, int x, int y) { Graphics2D g2 = (Graphics2D)g; g2.setPaint(new GradientPaint(0,0,COL_CARD,300,300,COL_SIDEBAR)); g2.fillRoundRect(0,0,300,300,20,20); g2.setColor(COL_TEXT_SEC); g2.drawString("No Cover", 120, 150); }
                public int getIconWidth() { return 300; } public int getIconHeight() { return 300; }
            });
        }
        private JPanel createFullPlaybackBar() {
            JPanel bar = new JPanel(new BorderLayout(0, 20)); bar.setBackground(COL_CARD); bar.setBorder(new EmptyBorder(25, 40, 25, 40));
            JPanel progressPanel = new JPanel(new BorderLayout(15, 0)); progressPanel.setOpaque(false);
            JPanel controls = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 0)); controls.setOpaque(false);

            JButton btnPrev = createIconButton("⏮", 28);
            barPlayBtn = new GradientStopButton(60);
            JButton btnNext = createIconButton("⏭", 28);

            btnNext.addActionListener(e -> mainFrame.playNext());
            btnPrev.addActionListener(e -> mainFrame.playPrev());

            controls.add(btnPrev); controls.add(barPlayBtn); controls.add(btnNext);
            bar.add(progressPanel, BorderLayout.NORTH); bar.add(controls, BorderLayout.CENTER); return bar;
        }
        private JButton createIconButton(String icon, int size) {
            JButton btn = new JButton(icon); btn.setContentAreaFilled(false); btn.setBorderPainted(false); btn.setFocusPainted(false);
            btn.setForeground(COL_TEXT_SEC); btn.setFont(new Font("Segoe UI Symbol", Font.PLAIN, size)); btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btn.addMouseListener(new MouseAdapter() { public void mouseEntered(MouseEvent e) { btn.setForeground(Color.WHITE); } public void mouseExited(MouseEvent e) { btn.setForeground(COL_TEXT_SEC); } });
            return btn;
        }
    }

    // --- SHARED UTILS ---
    interface TableAction { void onPlay(String t, String a); }
    interface DeleteAction { void onDelete(int row); }
    private void setupTableLogic(JTable table, DefaultTableModel model, Component parent, TableAction action, DeleteAction deleteAction) {
        table.setBackground(COL_BG_MAIN); table.setForeground(COL_TEXT); table.setRowHeight(55); table.setShowGrid(false); table.setSelectionBackground(new Color(35, 35, 35)); table.setSelectionForeground(COL_TEXT); JTableHeader th = table.getTableHeader(); th.setBackground(COL_BG_MAIN); th.setForeground(COL_TEXT_SEC); th.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(50,50,50)));
        table.putClientProperty("hoveredRow", -1); table.addMouseMotionListener(new MouseMotionAdapter() { public void mouseMoved(MouseEvent e) { int row = table.rowAtPoint(e.getPoint()); if (row != (int)table.getClientProperty("hoveredRow")) { table.putClientProperty("hoveredRow", row); table.repaint(); } } });
        table.addMouseListener(new MouseAdapter() { public void mouseExited(MouseEvent e) { table.putClientProperty("hoveredRow", -1); table.repaint(); } public void mouseClicked(MouseEvent e) { int row = table.rowAtPoint(e.getPoint()); int col = table.columnAtPoint(e.getPoint()); if (row != -1) { if (col == model.getColumnCount() - 1) showPopupMenu(e, row, model, parent, deleteAction); else if (action != null) action.onPlay(model.getValueAt(row, 1).toString(), model.getValueAt(row, 2).toString()); } } });
        table.getColumnModel().getColumn(0).setCellRenderer(new DefaultTableCellRenderer() { public Component getTableCellRendererComponent(JTable table, Object value, boolean isSel, boolean hasFocus, int row, int col) { super.getTableCellRendererComponent(table, value, isSel, hasFocus, row, col); setHorizontalAlignment(CENTER); if (row == (int) table.getClientProperty("hoveredRow")) { setText("▶"); setForeground(COL_ACCENT); setFont(new Font("Segoe UI Symbol", Font.BOLD, 18)); } else { setText(value.toString()); setForeground(COL_TEXT_SEC); setFont(new Font("SansSerif", Font.PLAIN, 14)); } return this; } });
        table.getColumnModel().getColumn(model.getColumnCount()-1).setCellRenderer(new DefaultTableCellRenderer() { public Component getTableCellRendererComponent(JTable table, Object value, boolean isSel, boolean hasFocus, int row, int col) { super.getTableCellRendererComponent(table, value, isSel, hasFocus, row, col); setForeground(COL_TEXT_SEC); setFont(new Font("Segoe UI Symbol", Font.BOLD, 18)); setHorizontalAlignment(CENTER); return this; } });
        table.getColumnModel().getColumn(0).setMaxWidth(50); table.getColumnModel().getColumn(model.getColumnCount()-1).setMaxWidth(50);
    }
    private void showPopupMenu(MouseEvent e, int row, DefaultTableModel model, Component parent, DeleteAction deleteAction) {
        JPopupMenu popup = new JPopupMenu(); popup.setBackground(new Color(40, 40, 40)); popup.setBorder(BorderFactory.createLineBorder(new Color(60,60,60)));
        JMenuItem delItem = new JMenuItem("Delete Song"); delItem.setBackground(new Color(40, 40, 40)); delItem.setForeground(new Color(255, 80, 80)); delItem.setBorder(new EmptyBorder(10, 20, 10, 20));
        delItem.addActionListener(evt -> {
            if (deleteAction != null) deleteAction.onDelete(row);
            else model.removeRow(row);
        });
        popup.add(delItem); popup.show(e.getComponent(), e.getX(), e.getY());
    }

    class RoundedBorder implements Border {
        private int radius;
        RoundedBorder(int radius) { this.radius = radius; }
        public Insets getBorderInsets(Component c) { return new Insets(this.radius+1, this.radius+1, this.radius+2, this.radius); }
        public boolean isBorderOpaque() { return true; }
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) { g.drawRoundRect(x, y, width-1, height-1, radius, radius); }
    }
    class GradientStopButton extends JButton {
        private int size; public GradientStopButton(int size) { this.size = size; setPreferredSize(new Dimension(size, size)); setContentAreaFilled(false); setFocusPainted(false); setBorderPainted(false); setCursor(new Cursor(Cursor.HAND_CURSOR)); addActionListener(e -> { BackEnd.MusicManager.stopLagu(); repaint(); }); }
        protected void paintComponent(Graphics g) { Graphics2D g2 = (Graphics2D) g; g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); g2.setPaint(new GradientPaint(0, 0, COL_ACCENT, getWidth(), getHeight(), COL_ACCENT_2)); g2.fillOval(0, 0, getWidth(), getHeight()); g2.setColor(Color.WHITE); int cx = getWidth() / 2; int cy = getHeight() / 2; int stopSize = size / 3; g2.fillRoundRect(cx - (stopSize / 2), cy - (stopSize / 2), stopSize, stopSize, 5, 5); }
    }
    class PurpleButton extends JButton { public PurpleButton(String text) { super(text); setContentAreaFilled(false); setFocusPainted(false); setBorderPainted(false); setForeground(Color.WHITE); setFont(new Font("SansSerif", Font.BOLD, 14)); setCursor(new Cursor(Cursor.HAND_CURSOR)); } protected void paintComponent(Graphics g) { Graphics2D g2 = (Graphics2D) g; g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); g2.setColor(COL_ACCENT); g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 15, 15)); super.paintComponent(g); } }
    class SidebarButton extends JButton { String targetScene; boolean isActive; public SidebarButton(String text, String scene) { super(text); targetScene = scene; setContentAreaFilled(false); setFocusPainted(false); setBorderPainted(false); setForeground(COL_TEXT_SEC); setFont(new Font("SansSerif", Font.BOLD, 14)); setHorizontalAlignment(SwingConstants.LEFT); setBorder(new EmptyBorder(10,20,10,10)); setCursor(new Cursor(Cursor.HAND_CURSOR)); } public void setActive(boolean b) { isActive = b; setForeground(b ? Color.WHITE : COL_TEXT_SEC); repaint(); } protected void paintComponent(Graphics g) { if(isActive) { Graphics2D g2 = (Graphics2D) g; g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); g2.setColor(COL_ACCENT); g2.fill(new RoundRectangle2D.Double(0,0,getWidth(),getHeight(),15,15)); } super.paintComponent(g); } }
    class IconSymbol implements Icon { Color c; int s; public IconSymbol(Color c, int s) { this.c=c; this.s=s; } public int getIconWidth() { return s; } public int getIconHeight() { return s; } public void paintIcon(Component cmp, Graphics g, int x, int y) { g.setColor(c); g.fillRoundRect(x, y, s, s, 5, 5); g.setColor(Color.WHITE); g.drawOval(x+5, y+5, s-10, s-10); } }
    class ArtPanel extends JPanel { int t, w, h; public ArtPanel(int t, int w, int h) { this.t=t; this.w=w; this.h=h; setPreferredSize(new Dimension(w,h)); setBackground(COL_BG_MAIN); } protected void paintComponent(Graphics g) { Graphics2D g2 = (Graphics2D)g; g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); int s = Math.min(getWidth(), getHeight()); if (t==1) { g2.setPaint(new GradientPaint(0,0,new Color(100,50,150),s,s,new Color(255,100,100))); g2.fillRoundRect(0,0,s,s,20,20); } else if (t==2) { g2.setColor(new Color(20,20,50)); g2.fillRoundRect(0,0,s,s,20,20); g2.setColor(Color.CYAN); g2.drawOval(10,10,s-20,s-20); } else { g2.setColor(new Color(240,220,100)); g2.fillRoundRect(0,0,s,s,20,20); g2.setColor(Color.BLACK); g2.fillOval(5,5,s-10,s-10); g2.setColor(Color.RED); g2.fillOval(s/2-15,s/2-15,30,30); } } }
    class DashedBorder extends EmptyBorder { Color c; int t, a; public DashedBorder(Color c, int t, int a) { super(t,t,t,t); this.c=c; this.t=t; this.a=a; } public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) { Graphics2D g2 = (Graphics2D)g.create(); g2.setColor(this.c); g2.setStroke(new BasicStroke(t, 0, 0, 10, new float[]{10}, 0)); g2.drawRoundRect(x+t/2, y+t/2, w-t, h-t, a, a); g2.dispose(); }}
    class GradientButton extends JButton { public GradientButton(String text) { super(text); setContentAreaFilled(false); setFocusPainted(false); setBorderPainted(false); setForeground(Color.WHITE); setFont(new Font("SansSerif", Font.BOLD, 15)); setCursor(new Cursor(Cursor.HAND_CURSOR)); } protected void paintComponent(Graphics g) { Graphics2D g2 = (Graphics2D) g; g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); g2.setPaint(new GradientPaint(0,0, COL_ACCENT, getWidth(), 0, COL_ACCENT_2)); g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 15, 15)); super.paintComponent(g); }}
}