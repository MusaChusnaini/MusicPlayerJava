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

// Import library audio untuk Cover Art
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.images.Artwork;

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

    // --- DATA HANDLING ---
    private final String SPLIT_CHAR = ",";

    private CardLayout cardLayout = new CardLayout();
    private JPanel mainContentPanel = new JPanel(cardLayout);
    private java.util.List<SidebarButton> navButtons = new ArrayList<>();
    private PlayerPanel playerPanel;

    // --- QUEUE SYSTEM ---
    private ArrayList<String[]> currentPlaylistQueue = null;
    private int currentQueueIndex = -1;

    public MusicFlow() {
        setTitle("MusicFlow Premium");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        add(createSidebar(), BorderLayout.WEST);

        mainContentPanel.setBackground(COL_BG_MAIN);
        playerPanel = new PlayerPanel(this);

        mainContentPanel.add(playerPanel, "PLAYER");
        mainContentPanel.add(new LibraryPanel(this), "LIBRARY");
        mainContentPanel.add(new PlaylistManagerPanel(this), "PLAYLISTS");
        mainContentPanel.add(new AddMusicPanel(), "ADD_MUSIC");

        add(mainContentPanel, BorderLayout.CENTER);
        navigate("LIBRARY");
    }

    // --- LOGIKA PLAYLIST / QUEUE ---

    public void playFromPlaylist(ArrayList<String[]> songs, int startIndex) {
        this.currentPlaylistQueue = songs;
        this.currentQueueIndex = startIndex;
        playQueueSong();
    }

    private void playQueueSong() {
        if (currentPlaylistQueue != null && currentQueueIndex >= 0 && currentQueueIndex < currentPlaylistQueue.size()) {
            String[] data = currentPlaylistQueue.get(currentQueueIndex);
            // Format Data Playlist: {Title, Artist, Album, Duration, Path}
            String title = data[0];
            String artist = data[1];
            String path = "";
            // Pastikan mengambil path dengan benar (biasanya index terakhir)
            if(data.length >= 5) path = data[4];
            else if(data.length == 4) path = data[3]; // Fallback format lama

            // Cari index di database global backend untuk memutar audio
            int globalIndex = findGlobalIndex(title, artist);

            if(globalIndex != -1) {
                BackEnd.MusicManager.playLagu(globalIndex);
            } else {
                System.out.println("Lagu tidak ditemukan di Library utama.");
            }

            // [PERBAIKAN] Ambil Cover dari Path langsung agar muncul di Player
            ImageIcon albumCover = getCoverFromPath(path);
            playerPanel.setSongInfo(title, artist, albumCover);

            navigate("PLAYER");
        }
    }

    public void playNext() {
        if (currentPlaylistQueue != null) {
            if (currentQueueIndex < currentPlaylistQueue.size() - 1) {
                currentQueueIndex++;
                playQueueSong();
            } else {
                JOptionPane.showMessageDialog(this, "Akhir dari playlist.");
            }
        } else {
            BackEnd.MusicManager.nextLagu();
            updatePlayerUI_Global();
        }
    }

    public void playPrev() {
        if (currentPlaylistQueue != null) {
            if (currentQueueIndex > 0) {
                currentQueueIndex--;
                playQueueSong();
            } else {
                JOptionPane.showMessageDialog(this, "Awal dari playlist.");
            }
        } else {
            BackEnd.MusicManager.prevLagu();
            updatePlayerUI_Global();
        }
    }

    public void playFromLibrary(int index, String title, String artist, ImageIcon album) {
        this.currentPlaylistQueue = null;
        this.currentQueueIndex = -1;
        BackEnd.MusicManager.playLagu(index);
        playerPanel.setSongInfo(title, artist, album);
        navigate("PLAYER");
    }

    // --- HELPERS ---

    private void updatePlayerUI_Global() {
        int idx = BackEnd.MusicManager.getCurrentIndex();
        if (idx != -1) {
            BackEnd.Lagu lagu = BackEnd.MusicManager.getDatabaseLagu().get(idx);
            byte[] imgData = BackEnd.MusicManager.getRawCover(idx);
            ImageIcon icon = (imgData != null) ? new ImageIcon(imgData) : null;
            playerPanel.setSongInfo(lagu.getJudul(), lagu.getArtis(), icon);
        }
    }

    private int findGlobalIndex(String title, String artist) {
        ArrayList<BackEnd.Lagu> db = BackEnd.MusicManager.getDatabaseLagu();
        for(int i=0; i<db.size(); i++) {
            if(db.get(i).getJudul().equals(title) && db.get(i).getArtis().equals(artist)) {
                return i;
            }
        }
        return -1;
    }

    private ImageIcon getCoverFromPath(String path) {
        try {
            File f = new File(path);
            if(f.exists()) {
                AudioFile audioFile = AudioFileIO.read(f);
                Tag tag = audioFile.getTag();
                Artwork artwork = tag.getFirstArtwork();
                if(artwork != null) return new ImageIcon(artwork.getBinaryData());
            }
        } catch (Exception e) {}
        return null;
    }

    private void navigate(String sceneName) {
        cardLayout.show(mainContentPanel, sceneName);
        if (sceneName.equals("LIBRARY")) {
            for (Component comp : mainContentPanel.getComponents())
                if (comp instanceof LibraryPanel)
                    ((LibraryPanel) comp).refreshData();
        }
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
            sidebar.add(btn);
            sidebar.add(Box.createVerticalStrut(10));
        }
        sidebar.add(Box.createVerticalGlue());
        return sidebar;
    }

    public static void main(String[] args) {
        try {
            BackEnd.MusicManager.loadDataDariCSV();
        } catch(Exception e) {}
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e){

        }
        SwingUtilities.invokeLater(() -> new MusicFlow().setVisible(true));
    }

    // ========================================================
    // SCENE: LIBRARY
    // ========================================================
    class LibraryPanel extends JPanel {
        DefaultTableModel TableModel;
        MusicFlow frame;

        public LibraryPanel(MusicFlow frame) {
            this.frame = frame;
            setLayout(new BorderLayout());
            setBackground(COL_BG_MAIN);
            setBorder(new EmptyBorder(40, 40, 40, 40));
            JLabel title = new JLabel("Your Library");
            title.setFont(new Font("SansSerif", Font.BOLD, 32));
            title.setForeground(COL_TEXT);
            add(title, BorderLayout.NORTH);

            String[] cols = {"#", "TITLE", "ARTIST", "ALBUM", "PATH", ""};
            TableModel = new DefaultTableModel(cols, 0) {
                public boolean isCellEditable(int r, int c) {
                    return false;
                }
            };

            refreshData();

            JTable table = new JTable(TableModel);
            setupTableLogic(table, TableModel, this, null);
            JScrollPane scroll = new JScrollPane(table);
            scroll.getViewport().setBackground(COL_BG_MAIN);
            scroll.setBorder(null);
            add(scroll, BorderLayout.CENTER);
        }

        public void refreshData() {
            TableModel.setRowCount(0);
            try {
                File file = new File("musics.csv");
                if (file.exists()) {
                    BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
                    String line; int rowNum = 1;
                    while ((line = bufferedReader.readLine()) != null) {
                        String[] data = line.split(SPLIT_CHAR);
                        // Format: Title, Artist, Album, Duration, Path
                        if (data.length >= 5) {
                            TableModel.addRow(new Object[]{
                                    String.valueOf(rowNum++), data[0], data[1], data[2], data[4], "⋮"
                            });
                        } else if (data.length == 4) {
                            TableModel.addRow(new Object[]{ String.valueOf(rowNum++), data[0], data[1], data[2], data[3], "⋮"
                            });
                        }
                    } bufferedReader.close();
                }
            } catch (Exception e) {

            }
        }
    }

    // ========================================================
    // SCENE: PLAYLIST MANAGER (FULL FITUR)
    // ========================================================
    class PlaylistManagerPanel extends JPanel {
        private CardLayout PlayListLayout = new CardLayout();
        private JPanel PlayListContent = new JPanel(PlayListLayout);
        private MusicFlow mainFrame;
        private PlaylistGrid gridView;

        private ArrayList<PlaylistData> playlists = new ArrayList<>();
        private final String CSV_PLAYLIST = "data_playlists.csv";
        private final String SONG_DELIMITER = "||";

        class PlaylistData {
            String title, desc;
            Color color;
            ArrayList<String[]> songs = new ArrayList<>();

            public PlaylistData(String t, String d, Color c) {
                this.title = t; this.desc = d; this.color = c;
            }
        }

        public PlaylistManagerPanel(MusicFlow frame) {
            this.mainFrame = frame;
            setLayout(new BorderLayout());
            add(PlayListContent, BorderLayout.CENTER);

            loadPlaylists();

            gridView = new PlaylistGrid();
            PlayListContent.add(gridView, "GRID");
            PlayListLayout.show(PlayListContent, "GRID");
        }

        private void savePlaylists() {
            try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(CSV_PLAYLIST))) {
                for (PlaylistData playlistData : playlists) {
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append(playlistData.title).append(SPLIT_CHAR).append(playlistData.desc).append(SPLIT_CHAR);
                    for (int i = 0; i < playlistData.songs.size(); i++) {
                        String[] s = playlistData.songs.get(i);
                        // Simpan: Title,Artist,Album,Path
                        String path = (s.length > 4) ? s[4] : "";
                        stringBuilder.append(s[0]).append(SPLIT_CHAR).append(s[1]).append(SPLIT_CHAR)
                                .append(s[2]).append(SPLIT_CHAR).append(SPLIT_CHAR).append(path);
                        if(i < playlistData.songs.size()-1) stringBuilder.append(SONG_DELIMITER);
                    }
                    bufferedWriter.write(stringBuilder.toString());
                    bufferedWriter.newLine();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void loadPlaylists() {
            playlists.clear();
            File f = new File(CSV_PLAYLIST);
            if(!f.exists()) return;
            try (BufferedReader br = new BufferedReader(new FileReader(f))) {
                String line;
                while((line = br.readLine()) != null) {
                    String[] parts = line.split(SPLIT_CHAR, 3);
                    if(parts.length >= 2) {
                        String desc = (parts.length > 1) ? parts[1] : ""; // Ambil deskripsi
                        String songsStr = (parts.length > 2) ? parts[2] : "";

                        PlaylistData data = new PlaylistData(parts[0], desc, getRandomColor());

                        if(!songsStr.isEmpty()) {
                            String[] songsRaw = songsStr.split("\\|\\|");
                            for(String string : songsRaw) {
                                String[] det = string.split(SPLIT_CHAR);
                                if(det.length >= 4) data.songs.add(det);
                            }
                        }
                        playlists.add(data);
                    }
                }
            } catch (Exception e) {}
        }

        class PlaylistGrid extends JPanel {
            private JPanel gridContainer;

            public PlaylistGrid() {
                setLayout(new BorderLayout());
                setBackground(COL_BG_MAIN);
                setBorder(new EmptyBorder(40, 40, 40, 40));
                JPanel header = new JPanel(new BorderLayout());
                header.setBackground(COL_BG_MAIN);
                JLabel title = new JLabel("Your Playlists");
                title.setFont(new Font("SansSerif", Font.BOLD, 32));
                title.setForeground(COL_TEXT);
                PurpleButton btnCreate = new PurpleButton("+ Create Playlist");
                btnCreate.addActionListener(e -> showCreateDialog());

                header.add(title, BorderLayout.WEST);
                header.add(btnCreate, BorderLayout.EAST);
                add(header, BorderLayout.NORTH);

                gridContainer = new JPanel(new FlowLayout(FlowLayout.LEFT, 30, 30));
                gridContainer.setBackground(COL_BG_MAIN);
                refreshGrid();

                JScrollPane scroll = new JScrollPane(gridContainer);
                scroll.setBorder(null);
                scroll.getViewport().setBackground(COL_BG_MAIN);
                add(scroll, BorderLayout.CENTER);
            }

            public void refreshGrid() {
                gridContainer.removeAll();
                for(PlaylistData p : playlists) {
                    gridContainer.add(createCard(p));
                }
                gridContainer.revalidate();
                gridContainer.repaint();
            }

            private JPanel createCard(PlaylistData p) {
                JPanel card = new JPanel();
                card.setPreferredSize(new Dimension(220, 290));
                card.setBackground(COL_CARD);
                card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
                card.setBorder(new EmptyBorder(15, 15, 15, 15));
                card.setCursor(new Cursor(Cursor.HAND_CURSOR));

                JPanel cover = new JPanel(new GridBagLayout());
                cover.setPreferredSize(new Dimension(190, 180));
                cover.setBackground(p.color);
                JLabel icon = new JLabel("▶");
                icon.setForeground(Color.WHITE);
                icon.setFont(new Font("Segoe UI Symbol", Font.BOLD, 24));
                cover.add(icon);

                JPanel infoPanel = new JPanel(new BorderLayout());
                infoPanel.setBackground(COL_CARD);
                infoPanel.setMaximumSize(new Dimension(200, 50));
                JLabel lTitle = new JLabel(p.title);
                lTitle.setFont(new Font("SansSerif", Font.BOLD, 16));
                lTitle.setForeground(Color.WHITE);

                JButton btnMenu = new JButton("⋮");
                btnMenu.setForeground(Color.WHITE);
                btnMenu.setFont(new Font("Segoe UI Symbol", Font.BOLD, 18));
                btnMenu.setContentAreaFilled(false);
                btnMenu.setBorderPainted(false);
                btnMenu.setFocusPainted(false);

                JPopupMenu popup = new JPopupMenu();
                JMenuItem ren = new JMenuItem("Rename");
                JMenuItem del = new JMenuItem("Delete");

                ren.addActionListener(e -> showRenameDialog(p));
                del.addActionListener(e -> {
                    int c = JOptionPane.showConfirmDialog(this,
                            "Hapus playlist " + p.title + "?",
                            "Konfirmasi", JOptionPane.YES_NO_OPTION);
                    if(c == JOptionPane.YES_OPTION) {
                        playlists.remove(p);
                        savePlaylists();
                        refreshGrid(); }
                });
                popup.add(ren);
                popup.add(del);
                btnMenu.addActionListener(e -> popup.show(btnMenu, 0, btnMenu.getHeight()));

                infoPanel.add(lTitle, BorderLayout.WEST);
                infoPanel.add(btnMenu, BorderLayout.EAST);

                card.add(cover);
                card.add(Box.createVerticalStrut(15));
                card.add(infoPanel);
                card.addMouseListener(new MouseAdapter() {
                    public void mouseClicked(MouseEvent e) {
                        openDetail(p);
                    }
                });
                return card;
            }

            private void openDetail(PlaylistData p) {
                PlayListContent.add(new PlaylistDetail(p), "DETAIL_TEMP");
                PlayListLayout.show(PlayListContent, "DETAIL_TEMP");
            }
        }

        class PlaylistDetail extends JPanel {
            PlaylistData currentData;
            DefaultTableModel model;

            public PlaylistDetail(PlaylistData playlistData) {
                this.currentData = playlistData;
                setLayout(new BorderLayout());
                setBackground(COL_BG_MAIN);
                setBorder(new EmptyBorder(40, 40, 0, 40));

                JPanel header = new JPanel(new BorderLayout());
                header.setBackground(COL_BG_MAIN);
                JButton btnBack = new JButton("← Back to Playlists");
                btnBack.setForeground(COL_ACCENT);
                btnBack.setContentAreaFilled(false);
                btnBack.setBorderPainted(false);
                btnBack.setFont(new Font("SansSerif", Font.BOLD, 14));
                btnBack.setCursor(new Cursor(Cursor.HAND_CURSOR));
                btnBack.addActionListener(e -> {
                    gridView.refreshGrid();
                    PlayListLayout.show(PlayListContent, "GRID");
                });

                JPanel info = new JPanel();
                info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
                info.setBackground(COL_BG_MAIN);
                JLabel title = new JLabel(playlistData.title);
                title.setFont(new Font("SansSerif", Font.BOLD, 40));
                title.setForeground(Color.WHITE);
                JLabel desc = new JLabel(playlistData.desc.isEmpty() ? "No description" : playlistData.desc);
                desc.setForeground(COL_TEXT_SEC);
                info.add(title);
                info.add(desc);

                PurpleButton btnAddSong = new PurpleButton("+ Add Song");
                btnAddSong.setPreferredSize(new Dimension(120, 40));
                btnAddSong.addActionListener(e -> showAddSongDialog());

                JPanel top = new JPanel(new BorderLayout());
                top.setBackground(COL_BG_MAIN);
                top.add(btnBack, BorderLayout.WEST);
                JPanel bottom = new JPanel(new BorderLayout());
                bottom.setBackground(COL_BG_MAIN);
                bottom.setBorder(new EmptyBorder(20,0,20,0));
                bottom.add(info, BorderLayout.WEST);
                bottom.add(btnAddSong, BorderLayout.EAST);

                header.add(top, BorderLayout.NORTH);
                header.add(bottom, BorderLayout.CENTER);
                add(header, BorderLayout.NORTH);

                // [PERBAIKAN] Menambahkan Kolom PATH
                String[] cols = {"#", "TITLE", "ARTIST", "ALBUM", "PATH", ""};
                model = new DefaultTableModel(cols, 0) {
                    public boolean isCellEditable(int r, int c) {
                        return false;
                    }
                };
                loadSongs();

                JTable table = new JTable(model);
                setupTableLogic(table, model, this, (row) -> {
                    currentData.songs.remove(row);
                    savePlaylists();
                    loadSongs();
                });

                JScrollPane scroll = new JScrollPane(table);
                scroll.getViewport().setBackground(COL_BG_MAIN);
                scroll.setBorder(BorderFactory.createEmptyBorder(20,0,20,0));
                add(scroll, BorderLayout.CENTER);
            }

            private void loadSongs() {
                model.setRowCount(0);
                int no = 1;
                for(String[] s : currentData.songs) {
                    // Pastikan s[4] (path) ada. Data: Title, Artist, Album, Duration, Path
                    String path = (s.length > 4) ? s[4] : ((s.length > 3) ? s[3] : "");
                    model.addRow(new Object[]{ String.valueOf(no++), s[0], s[1], s[2], path, "⋮" });
                }
            }

            // [PERBAIKAN] Ukuran Dialog Add Song Dikecilkan
            private void showAddSongDialog() {
                JDialog d = new JDialog((Frame)SwingUtilities.getWindowAncestor(this), "Add Song", true);
                d.setSize(200, 100); // Ukuran lebih pas
                d.setLocationRelativeTo(this);
                d.setUndecorated(true);
                JPanel p = new JPanel(new BorderLayout());
                p.setBackground(new Color(30,30,30));
                p.setBorder(BorderFactory.createLineBorder(new Color(152, 14, 182)));

                JLabel lbl = new JLabel("Select Song from Library:");
                lbl.setForeground(Color.WHITE);
                lbl.setBorder(new EmptyBorder(15,15,5,15));

                JComboBox<String> combo = new JComboBox<>();
                ArrayList<String[]> libSongs = new ArrayList<>();

                try (BufferedReader bufferedReader = new BufferedReader(new FileReader("musics.csv"))) {
                    String line;
                    while((line = bufferedReader.readLine()) != null) {
                        String[] data = line.split(SPLIT_CHAR);
                        // Format Library: Title, Artist, Album, Duration, Path
                        if(data.length >= 4) {
                            libSongs.add(data);
                            combo.addItem(data[0] + " - " + data[1]);
                        }
                    }
                } catch(Exception e) {

                }

                PurpleButton btnAdd = new PurpleButton("Add");
                JButton btnCancel = new JButton("Cancel");
                btnCancel.setContentAreaFilled(false);
                btnCancel.setBorderPainted(false);
                btnCancel.setForeground(Color.GRAY);

                btnCancel.addActionListener(e -> d.dispose());
                btnAdd.addActionListener(e -> {
                    int idx = combo.getSelectedIndex();
                    if(idx >= 0) {
                        currentData.songs.add(libSongs.get(idx));
                        savePlaylists();
                        loadSongs();
                        d.dispose();
                    }
                });

                JPanel btnP = new JPanel(new FlowLayout(FlowLayout.RIGHT));
                btnP.setOpaque(false);
                btnP.add(btnCancel);
                btnP.add(btnAdd);
                p.add(lbl, BorderLayout.NORTH);
                p.add(combo, BorderLayout.CENTER);
                p.add(btnP, BorderLayout.SOUTH);
                d.add(p);
                d.setVisible(true);
            }
        }

        // [PERBAIKAN] Dialog Create/Rename dengan Description
        private void showCreateDialog() {
            showPlaylistDialog(null);
        }
        private void showRenameDialog(PlaylistData playlistData) {
            showPlaylistDialog(playlistData);
        }

        private void showPlaylistDialog(PlaylistData exist) {
            boolean isEdit = (exist != null);
            JDialog d = new JDialog((Frame)SwingUtilities.getWindowAncestor(this), isEdit ? "Rename" : "Create Playlist", true);
            d.setUndecorated(true);
            d.setSize(400, 280);
            d.setLocationRelativeTo(this);
            JPanel p = new JPanel();
            p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
            p.setBackground(new Color(30,30,30));
            p.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(60,60,60)),
                    new EmptyBorder(20,20,20,20)));

            JLabel l = new JLabel(isEdit ? "Rename Playlist" : "New Playlist Name");
            l.setForeground(Color.WHITE);
            l.setAlignmentX(LEFT_ALIGNMENT);
            JTextField textField = new JTextField();
            textField.setBackground(new Color(50,50,50));
            textField.setForeground(Color.WHITE);
            textField.setCaretColor(Color.WHITE);
            textField.setMaximumSize(new Dimension(400, 30));
            textField.setAlignmentX(LEFT_ALIGNMENT);

            JLabel lDesc = new JLabel("Description");
            lDesc.setForeground(Color.GRAY);
            lDesc.setAlignmentX(LEFT_ALIGNMENT);
            JTextArea ta = new JTextArea(3, 20);
            ta.setBackground(new Color(50,50,50));
            ta.setForeground(Color.WHITE);
            ta.setCaretColor(Color.WHITE);
            JScrollPane scroll = new JScrollPane(ta);
            scroll.setMaximumSize(new Dimension(400, 60));
            scroll.setAlignmentX(LEFT_ALIGNMENT);
            scroll.setBorder(null);

            if(isEdit) {
                textField.setText(exist.title);
                ta.setText(exist.desc);
            }

            PurpleButton btn = new PurpleButton("Save");
            JButton cancel = new JButton("Cancel");
            cancel.setContentAreaFilled(false);
            cancel.setBorderPainted(false);
            cancel.setForeground(Color.GRAY);

            cancel.addActionListener(e -> d.dispose());
            btn.addActionListener(e -> {
                if(!textField.getText().isEmpty()) {
                    if(isEdit) {
                        exist.title = textField.getText();
                        exist.desc = ta.getText();
                    }
                    else {
                        playlists.add(new PlaylistData(textField.getText(), ta.getText(), getRandomColor()));
                    }
                    savePlaylists();
                    if(gridView != null) gridView.refreshGrid();
                    d.dispose();
                }
            });

            p.add(l); p.add(Box.createVerticalStrut(5));
            p.add(textField);
            p.add(Box.createVerticalStrut(15));
            p.add(lDesc);
            p.add(Box.createVerticalStrut(5));
            p.add(scroll);
            p.add(Box.createVerticalStrut(20));
            JPanel bp = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            bp.setOpaque(false);
            bp.setAlignmentX(LEFT_ALIGNMENT);
            bp.add(cancel);
            bp.add(btn);
            p.add(bp);
            d.add(p);
            d.setVisible(true);
        }

        private Color getRandomColor() {
            Random random = new Random();
            return new Color(random.nextInt(100),
                    random.nextInt(100), random.nextInt(150) + 50);
        }
    }

    // --- TABLE LOGIC SHARED ---
    interface DeleteAction { 
        void onDelete(int row);
    }

    private void setupTableLogic(JTable table, DefaultTableModel model, Component parent, DeleteAction delAction) {

        table.setBackground(COL_BG_MAIN);
        table.setForeground(COL_TEXT);
        table.setRowHeight(55);
        table.setShowGrid(false);
        table.setSelectionBackground(new Color(35, 35, 35));
        table.setSelectionForeground(COL_TEXT);
        JTableHeader th = table.getTableHeader();
        th.setBackground(COL_BG_MAIN);
        th.setForeground(COL_TEXT_SEC);
        th.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0,
                new Color(50,50,50)));

        table.putClientProperty("hoveredRow", -1);
        table.addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseMoved(MouseEvent e) {
            int row = table.rowAtPoint(e.getPoint());
            if (row != (int)table.getClientProperty("hoveredRow"))
            {
                table.putClientProperty("hoveredRow", row);
                table.repaint();
            }}
        });
        table.addMouseListener(new MouseAdapter() {
            public void mouseExited(MouseEvent e) {
                table.putClientProperty("hoveredRow", -1);
                table.repaint();
            }
            public void mouseClicked(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                int col = table.columnAtPoint(e.getPoint());
                if (row != -1) {
                    if (col != model.getColumnCount() - 1) {
                        // Play Song logic
                        if(parent instanceof PlaylistManagerPanel.PlaylistDetail) {
                            PlaylistManagerPanel.PlaylistDetail pd = (PlaylistManagerPanel.PlaylistDetail) parent;
                            playFromPlaylist(pd.currentData.songs, row);
                        } else {
                            String t = model.getValueAt(row, 1).toString();
                            String a = model.getValueAt(row, 2).toString();
                            byte[] img = BackEnd.MusicManager.getRawCover(row);
                            playFromLibrary(row, t, a, (img!=null) ? new ImageIcon(img) : null);
                        }
                    } else {
                        // Show Menu
                        JPopupMenu popupMenu = new JPopupMenu();
                        JMenuItem DeleteSongs = new JMenuItem("Delete Song");
                        DeleteSongs.addActionListener(ev -> {
                            if(delAction != null) {
                                delAction.onDelete(row);
                            } else {
                                BackEnd.MusicManager.getDatabaseLagu().remove(row);
                                BackEnd.MusicManager.simpanDataKeCSV();
                                if(parent instanceof LibraryPanel) ((LibraryPanel)parent).refreshData();
                            }
                        });
                        popupMenu.add(DeleteSongs);
                        popupMenu.show(e.getComponent(), e.getX(), e.getY());
                    }
                }
            }
        });

        // Renderers
        table.getColumnModel().getColumn(0).setCellRenderer(new DefaultTableCellRenderer() {

            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSel, boolean hasFocus, int row, int col) {
                super.getTableCellRendererComponent(table, value, isSel, hasFocus, row, col);
                setHorizontalAlignment(CENTER);

                if (row == (int) table.getClientProperty("hoveredRow")) {
                    setText("▶");
                    setForeground(COL_ACCENT);
                    setFont(new Font("Segoe UI Symbol", Font.BOLD, 18));
                }
                else { setText(value.toString());
                    setForeground(COL_TEXT_SEC);
                    setFont(new Font("SansSerif", Font.PLAIN, 14));
                }
                return this;
            }
        });
        table.getColumnModel().getColumn(model.getColumnCount()-1).setCellRenderer(new DefaultTableCellRenderer() {

            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSel, boolean hasFocus, int row, int col) {
                super.getTableCellRendererComponent(table, value, isSel, hasFocus, row, col);
                setForeground(COL_TEXT_SEC);
                setFont(new Font("Segoe UI Symbol", Font.BOLD, 18));
                setHorizontalAlignment(CENTER);
                return this;
            }
        });
        table.getColumnModel().getColumn(0).setMaxWidth(50);
        table.getColumnModel().getColumn(model.getColumnCount()-1).setMaxWidth(50);
    }

    // ========================================================
    // SCENE: ADD MUSIC
    // ========================================================
    class AddMusicPanel extends JPanel {
        private JTextField txtTitle, txtArtist, txtAlbum, txtFilePath;
        private File selectedFile;
        public AddMusicPanel() {
            JPanel groupTitle = createInputGroup("Song Title *", "Enter song title");
            txtTitle = (JTextField) groupTitle.getComponent(1);
            JPanel groupArtist = createInputGroup("Artist *", "Enter artist name");
            txtArtist = (JTextField) groupArtist.getComponent(1);
            JPanel groupAlbum = createInputGroup("Album", "Enter album name");
            txtAlbum = (JTextField) groupAlbum.getComponent(1);

            txtFilePath = new JTextField("No file selected");
            txtFilePath.setEditable(false);
            txtFilePath.setBackground(COL_INPUT);
            txtFilePath.setForeground(Color.GRAY);
            txtFilePath.setBorder(BorderFactory.createCompoundBorder(new RoundedBorder(10),
                    new EmptyBorder(10, 15, 10, 15)));

            setLayout(new BorderLayout())
            ; setBackground(COL_BG_MAIN);
            JPanel content = new JPanel();
            content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
            content.setBackground(COL_BG_MAIN);
            content.setBorder(new EmptyBorder(40, 60, 40, 60));

            JLabel headerTitle = new JLabel("Add New Music");
            headerTitle.setFont(new Font("SansSerif", Font.BOLD, 32));
            headerTitle.setForeground(COL_TEXT);
            headerTitle.setAlignmentX(LEFT_ALIGNMENT);
            content.add(headerTitle);
            content.add(Box.createVerticalStrut(30));

            JPanel formContainer = new JPanel();
            formContainer.setLayout(new BoxLayout(formContainer, BoxLayout.Y_AXIS));
            formContainer.setBackground(COL_BG_MAIN);
            formContainer.setAlignmentX(LEFT_ALIGNMENT);

            JPanel inputGrid = new JPanel(new GridBagLayout());
            inputGrid.setBackground(COL_BG_MAIN);
            inputGrid.setAlignmentX(LEFT_ALIGNMENT);
            inputGrid.setMaximumSize(new Dimension(800, 150));
            GridBagConstraints Constraints = new GridBagConstraints();
            Constraints.fill = GridBagConstraints.HORIZONTAL;
            Constraints.insets = new Insets(10, 0, 10, 20);
            Constraints.weightx = 0.5;
            Constraints.gridx = 0;
            Constraints.gridy = 0;
            inputGrid.add(groupTitle, Constraints);
            Constraints.gridx = 1;
            inputGrid.add(groupArtist, Constraints);
            Constraints.gridx = 0;
            Constraints.gridy = 1;
            inputGrid.add(groupAlbum, Constraints);
            formContainer.add(inputGrid);
            formContainer.add(Box.createVerticalStrut(20));

            JPanel filePanel = new JPanel(new BorderLayout(10, 0));
            filePanel.setBackground(COL_BG_MAIN);
            filePanel.setMaximumSize(new Dimension(800, 45));
            filePanel.setAlignmentX(LEFT_ALIGNMENT);
            JButton btnBrowse = new JButton("Choose File");

            btnBrowse.addActionListener(e -> {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setFileFilter(new FileNameExtensionFilter("MP3 Files", "mp3"));

                if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                    selectedFile = fileChooser.getSelectedFile();
                    txtFilePath.setText(selectedFile.getAbsolutePath()); txtFilePath.setForeground(Color.WHITE);
                    try {
                        AudioFile f = AudioFileIO.read(selectedFile);
                        Tag tag = f.getTag();
                        if (tag != null) {
                            txtTitle.setText(tag.getFirst(org.jaudiotagger.tag.FieldKey.TITLE));
                            txtArtist.setText(tag.getFirst(org.jaudiotagger.tag.FieldKey.ARTIST));
                            txtAlbum.setText(tag.getFirst(org.jaudiotagger.tag.FieldKey.ALBUM));
                            txtTitle.setForeground(Color.WHITE);
                            txtArtist.setForeground(Color.WHITE);
                            txtAlbum.setForeground(Color.WHITE);
                        }
                    } catch (Exception ex) {}
                }
            });
            filePanel.add(txtFilePath, BorderLayout.CENTER);
            filePanel.add(btnBrowse, BorderLayout.EAST);
            formContainer.add(filePanel);
            formContainer.add(Box.createVerticalStrut(30));

            GradientButton btnAdd = new GradientButton("♫ Add to Library");
            JButton btnClear = new JButton("Clear");
            btnAdd.addActionListener(e -> {
                String titleText = txtTitle.getText(), a = txtArtist.getText(), al = txtAlbum.getText(), p = txtFilePath.getText();
                if (titleText.isEmpty() || selectedFile == null) {
                    JOptionPane.showMessageDialog(this, "Data tidak lengkap!");
                    return;
                }
                BackEnd.MusicManager.getDatabaseLagu().add(new BackEnd.Lagu(titleText, a, al, p));
                BackEnd.MusicManager.simpanDataKeCSV();
                JOptionPane.showMessageDialog(this, "Berhasil ditambahkan!");
            });
            JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
            btnPanel.setBackground(COL_BG_MAIN);
            btnPanel.add(btnAdd);
            btnPanel.add(btnClear);
            formContainer.add(btnPanel);
            content.add(formContainer);
            JScrollPane scroll = new JScrollPane(content);
            scroll.setBorder(null);
            add(scroll, BorderLayout.CENTER);
        }
        private JPanel createInputGroup(String label, String placeholder) {
            JPanel p = new JPanel(new BorderLayout(0, 5));
            p.setBackground(COL_BG_MAIN);
            JLabel l = new JLabel(label);
            l.setForeground(COL_TEXT_SEC);
            JTextField t = new JTextField(placeholder);
            t.setBackground(COL_INPUT);
            t.setForeground(Color.GRAY);
            t.setBorder(BorderFactory.createCompoundBorder(new RoundedBorder(10), new EmptyBorder(10, 15, 10, 15)));
            p.add(l, BorderLayout.NORTH);
            p.add(t, BorderLayout.CENTER);
            return p;
        }
    }

    // ========================================================
    // SCENE: PLAYER PANEL
    // ========================================================
    class PlayerPanel extends JPanel {
        private JLabel lblTitle, lblArtist, lblCover;
        private GradientStopButton barPlayBtn;
        private MusicFlow mainFrame;

        public PlayerPanel(MusicFlow frame) {
            this.mainFrame = frame;
            setLayout(new BorderLayout());
            setBackground(COL_BG_MAIN);
            JPanel centerPanel = new JPanel(new GridBagLayout());
            centerPanel.setOpaque(false);
            JPanel contentBox = new JPanel();
            contentBox.setLayout(new BoxLayout(contentBox, BoxLayout.Y_AXIS));
            contentBox.setOpaque(false);

            lblCover = new JLabel();
            lblCover.setAlignmentX(CENTER_ALIGNMENT);
            lblCover.setPreferredSize(new Dimension(300, 300));
            lblCover.setMaximumSize(new Dimension(300, 300));
            setPlaceholderCover();

            lblTitle = new JLabel("No Song Playing");
            lblTitle.setFont(new Font("SansSerif", Font.BOLD, 32));
            lblTitle.setForeground(Color.WHITE); lblTitle.setAlignmentX(CENTER_ALIGNMENT);
            lblArtist = new JLabel("Select from library");
            lblArtist.setForeground(COL_TEXT_SEC); lblArtist.setFont(new Font("SansSerif", Font.PLAIN, 18));
            lblArtist.setAlignmentX(CENTER_ALIGNMENT);

            contentBox.add(lblCover);
            contentBox.add(Box.createVerticalStrut(30));
            contentBox.add(lblTitle);
            contentBox.add(Box.createVerticalStrut(10));
            contentBox.add(lblArtist);
            contentBox.add(Box.createVerticalStrut(40));

            centerPanel.add(contentBox);
            add(centerPanel, BorderLayout.CENTER);
            add(createFullPlaybackBar(), BorderLayout.SOUTH);
        }

        public void setSongInfo(String title, String artist, ImageIcon coverIcon) {
            lblTitle.setText(title);
            lblArtist.setText(artist);
            if (coverIcon != null) {
                Image img = coverIcon.getImage().getScaledInstance(300, 300, Image.SCALE_SMOOTH);
                lblCover.setIcon(new ImageIcon(img));
            } else { setPlaceholderCover(); }
            repaint();
        }

        private void setPlaceholderCover() {
            lblCover.setIcon(new Icon() {
                public void paintIcon(Component c, Graphics g, int x, int y) {
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setPaint(new GradientPaint(0, 0, COL_CARD, 300, 300, COL_SIDEBAR));
                    g2.fillRoundRect(0, 0, 300, 300, 20, 20);
                    g2.setColor(COL_TEXT_SEC);
                    g2.drawString("No Cover", 120, 150);
                }
                public int getIconWidth() {
                    return 300;
                }
                public int getIconHeight() {
                    return 300;
                }
            });
        }

        private JPanel createFullPlaybackBar() {
            JPanel bar = new JPanel(new BorderLayout(0, 20));
            bar.setBackground(COL_CARD);
            bar.setBorder(new EmptyBorder(25, 40, 25, 40));
            JPanel progressPanel = new JPanel(new BorderLayout(15, 0));
            progressPanel.setOpaque(false);
            JPanel controls = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 0));
            controls.setOpaque(false);

            JButton btnPrev = createIconButton("⏮", 28);
            barPlayBtn = new GradientStopButton(60);
            JButton btnNext = createIconButton("⏭", 28);

            btnPrev.addActionListener(e -> mainFrame.playPrev());
            btnNext.addActionListener(e -> mainFrame.playNext());

            controls.add(btnPrev);
            controls.add(barPlayBtn);
            controls.add(btnNext);
            bar.add(progressPanel, BorderLayout.NORTH);
            bar.add(controls, BorderLayout.CENTER);
            return bar;
        }

        private JButton createIconButton(String icon, int size) {
            JButton btn = new JButton(icon);
            btn.setContentAreaFilled(false);
            btn.setBorderPainted(false);
            btn.setFocusPainted(false);
            btn.setForeground(COL_TEXT_SEC);
            btn.setFont(new Font("Segoe UI Symbol", Font.PLAIN, size));
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btn.addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) {
                    btn.setForeground(Color.WHITE);
                }
                public void mouseExited(MouseEvent e) {
                    btn.setForeground(COL_TEXT_SEC);
                }
            });
            return btn;
        }
    }

    // --- CUSTOM BUTTONS & STYLES ---
    class GradientStopButton extends JButton {
        private int size; public GradientStopButton(int size) {
            this.size = size;
            setPreferredSize(new Dimension(size, size));
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            addActionListener(e -> {
                BackEnd.MusicManager.stopLagu();
                repaint();
            });
        }

        protected void paintComponent(Graphics graphics) {
            Graphics2D g2 = (Graphics2D) graphics;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setPaint(new GradientPaint(0, 0, COL_ACCENT, getWidth(), getHeight(), COL_ACCENT_2));
            g2.fillOval(0, 0, getWidth(), getHeight());
            g2.setColor(Color.WHITE);
            int cx = getWidth() / 2;
            int cy = getHeight() / 2;
            int stopSize = size / 3;
            g2.fillRoundRect(cx - (stopSize / 2), cy - (stopSize / 2), stopSize, stopSize, 5, 5);
        }
    }

    class PurpleButton extends JButton {
        public PurpleButton(String text) {
            super(text);
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setForeground(Color.WHITE);
            setFont(new Font("SansSerif", Font.BOLD, 14));
            setCursor(new Cursor(Cursor.HAND_CURSOR));
        }

        protected void paintComponent(Graphics graphics) {
            Graphics2D g2 = (Graphics2D) graphics;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(COL_ACCENT);
            g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 15, 15));
            super.paintComponent(graphics);
        }
    }

    class SidebarButton extends JButton {
        String targetScene; boolean isActive;
        public SidebarButton(String text, String scene) {
            super(text);
            targetScene = scene;
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setForeground(COL_TEXT_SEC);
            setFont(new Font("SansSerif", Font.BOLD, 14));
            setHorizontalAlignment(SwingConstants.LEFT);
            setBorder(new EmptyBorder(10,20,10,10));
            setCursor(new Cursor(Cursor.HAND_CURSOR));
        }
        public void setActive(boolean active) {
            isActive = active;
            setForeground(active ? Color.WHITE : COL_TEXT_SEC);
            repaint();
        }
        protected void paintComponent(Graphics graphics) {
            if(isActive) {
                Graphics2D g2 = (Graphics2D) graphics;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(COL_ACCENT);
                g2.fill(new RoundRectangle2D.Double(0,0,getWidth(),getHeight(),15,15));
            }
            super.paintComponent(graphics);
        }
    }
    class IconSymbol implements Icon {
        Color color; int anInt;
        public IconSymbol(Color color, int s) {
            this.color =color; this.anInt =s;
        }
        public int getIconWidth() {
            return anInt;
        }
        public int getIconHeight() {
            return anInt;
        }
        public void paintIcon(Component component, Graphics graphics, int x, int y) {
            graphics.setColor(color);
            graphics.fillRoundRect(x, y, anInt, anInt, 5, 5);
            graphics.setColor(Color.WHITE);
            graphics.drawOval(x+5, y+5, anInt -10, anInt -10);
        }
    }
    
    class RoundedBorder implements Border {
        int anInt;
        RoundedBorder(int r) {
            this.anInt =r;
        }
        public Insets getBorderInsets(Component c) {
            return new Insets(anInt, anInt, anInt, anInt);
        }
        public boolean isBorderOpaque() {
            return true;
        }
        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {

        }
    }
    class GradientButton extends JButton {
        public GradientButton(String text) {
            super(text);
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setForeground(Color.WHITE);
            setFont(new Font("SansSerif", Font.BOLD, 15));
            setCursor(new Cursor(Cursor.HAND_CURSOR));
        }
        protected void paintComponent(Graphics graphics) {
            Graphics2D g2 = (Graphics2D) graphics;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setPaint(new GradientPaint(0,0, COL_ACCENT, getWidth(), 0, COL_ACCENT_2));
            g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 15, 15));
            super.paintComponent(graphics);
        }
    }
}