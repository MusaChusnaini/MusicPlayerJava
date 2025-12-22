package FrontEnd;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.plaf.basic.BasicProgressBarUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.io.File;
import java.util.ArrayList;

public class MusicFlow extends JFrame {

    // --- PALET WARNA ---
    public static final Color COL_BG_MAIN = new Color(18, 18, 18);
    public static final Color COL_SIDEBAR = new Color(10, 10, 10);
    public static final Color COL_ACCENT  = new Color(189, 0, 255); // Ungu Vivid
    public static final Color COL_ACCENT_2= new Color(255, 0, 128); // Pink
    public static final Color COL_CARD    = new Color(30, 30, 30);
    public static final Color COL_INPUT   = new Color(40, 40, 40);
    public static final Color COL_TEXT    = Color.WHITE;
    public static final Color COL_TEXT_SEC= new Color(170, 170, 170);

    // --- KOMPONEN UTAMA ---
    private CardLayout cardLayout = new CardLayout();
    private JPanel mainContentPanel = new JPanel(cardLayout);
    private java.util.List<SidebarButton> navButtons = new ArrayList<>();

    private PlayerPanel playerPanel;

    public MusicFlow() {
        setTitle("MusicFlow Premium");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        add(createSidebar(), BorderLayout.WEST);

        mainContentPanel.setBackground(COL_BG_MAIN);
        playerPanel = new PlayerPanel();

        mainContentPanel.add(playerPanel, "PLAYER");
        mainContentPanel.add(new LibraryPanel(this), "LIBRARY");
        mainContentPanel.add(new PlaylistManagerPanel(this), "PLAYLISTS");
        mainContentPanel.add(new AddMusicPanel(), "ADD_MUSIC");

        add(mainContentPanel, BorderLayout.CENTER);

        navigate("PLAYER");

        BackEnd.MusicManager.setOnSongChangeEvent(() -> {
            updatePlayerUI();
        });
    }

    // Method untuk update teks dan gambar di PlayerPanel
    private void updatePlayerUI() {
        int idx = BackEnd.MusicManager.getCurrentIndex();
        if (idx != -1) {
            BackEnd.Lagu lagu = BackEnd.MusicManager.getDatabaseLagu().get(idx);
            byte[] imgData = BackEnd.MusicManager.getRawCover(idx);
            ImageIcon icon = (imgData != null) ? new ImageIcon(imgData) : null;

            playerPanel.setSongInfo(lagu.getJudul(), lagu.getArtis(), icon);
        }
    }

    public void playSong(int index, String title, String artist,ImageIcon album) {
        BackEnd.MusicManager.playLagu(index);

        // Update tampilan PlayerPanel (Teks & Gambar)
        playerPanel.setSongInfo(title, artist, album);

        // Pindah ke tab PLAYER secara otomatis saat lagu diklik
        navigate("PLAYER");
    }

    private void navigate(String sceneName) {
        cardLayout.show(mainContentPanel, sceneName);

        // Jika user pindah ke tab LIBRARY, panggil fungsi refresh
        if (sceneName.equals("LIBRARY")) {
            // Cari LibraryPanel di dalam mainContentPanel dan panggil refreshData()
            for (Component comp : mainContentPanel.getComponents()) {
                if (comp instanceof LibraryPanel) {
                    ((LibraryPanel) comp).refreshData();
                }
            }
        }

        for (SidebarButton btn : navButtons) {
            btn.setActive(btn.targetScene.equals(sceneName));
        }
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

        // REVISI: Bagian Premium Features sudah dihapus dari sini.

        return sidebar;
    }

    public static void main(String[] args) {
        BackEnd.MusicManager.loadDataDariCSV();

        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }
        catch (Exception e){

        }
        SwingUtilities.invokeLater(() -> new MusicFlow().setVisible(true));
    }

    // ========================================================
    // SCENE 4: ADD MUSIC
    // ========================================================
    class AddMusicPanel extends JPanel {
        private JTextField txtTitle, txtArtist, txtAlbum, txtFilePath;
        private File selectedFile;
        public AddMusicPanel() {
            // 1. Inisialisasi Group Input dan ambil referensi JTextField-nya
            JPanel groupTitle = createInputGroup("Song Title *", "Enter song title");
            txtTitle = (JTextField) groupTitle.getComponent(1);

            JPanel groupArtist = createInputGroup("Artist *", "Enter artist name");
            txtArtist = (JTextField) groupArtist.getComponent(1);

            JPanel groupAlbum = createInputGroup("Album", "Enter album name");
            txtAlbum = (JTextField) groupAlbum.getComponent(1);

            // 2. Inisialisasi txtFilePath agar tidak NULL
            txtFilePath = new JTextField("No file selected");
            txtFilePath.setEditable(false);
            txtFilePath.setBackground(COL_INPUT);
            txtFilePath.setForeground(Color.GRAY);
            txtFilePath.setBorder(BorderFactory.createCompoundBorder(new RoundedBorder(10), new EmptyBorder(10, 15, 10, 15)));

            // --- Layouting ---
            setLayout(new BorderLayout());
            setBackground(COL_BG_MAIN);
            JPanel content = new JPanel();
            content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
            content.setBackground(COL_BG_MAIN);
            content.setBorder(new EmptyBorder(40, 60, 40, 60));

            // Header
            JLabel headerTitle = new JLabel("Add New Music");
            headerTitle.setFont(new Font("SansSerif", Font.BOLD, 32));
            headerTitle.setForeground(COL_TEXT);
            headerTitle.setAlignmentX(LEFT_ALIGNMENT);
            content.add(headerTitle);
            content.add(Box.createVerticalStrut(30));

            // Form Container
            JPanel formContainer = new JPanel();
            formContainer.setLayout(new BoxLayout(formContainer, BoxLayout.Y_AXIS));
            formContainer.setBackground(COL_BG_MAIN);
            formContainer.setAlignmentX(LEFT_ALIGNMENT);

            // Input Grid
            JPanel inputGrid = new JPanel(new GridBagLayout());
            inputGrid.setBackground(COL_BG_MAIN);
            inputGrid.setAlignmentX(LEFT_ALIGNMENT);
            inputGrid.setMaximumSize(new Dimension(800, 150));
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.insets = new Insets(10, 0, 10, 20);
            gbc.weightx = 0.5;

            gbc.gridx = 0; gbc.gridy = 0;
            inputGrid.add(groupTitle, gbc);
            gbc.gridx = 1;
            inputGrid.add(groupArtist, gbc);
            gbc.gridx = 0; gbc.gridy = 1;
            inputGrid.add(groupAlbum, gbc);

            formContainer.add(inputGrid);
            formContainer.add(Box.createVerticalStrut(20));

            // File Panel
            JPanel filePanel = new JPanel(new BorderLayout(10, 0));
            filePanel.setBackground(COL_BG_MAIN);
            filePanel.setMaximumSize(new Dimension(800, 45));
            filePanel.setAlignmentX(LEFT_ALIGNMENT);

            JButton btnBrowse = new JButton("Choose File");
            // ... (Style btnBrowse tetap sama) ...

            btnBrowse.addActionListener(e -> {
                JFileChooser fileChooser = new JFileChooser();
                if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                    selectedFile = fileChooser.getSelectedFile();
                    txtFilePath.setText(selectedFile.getAbsolutePath());
                    txtFilePath.setForeground(Color.WHITE);

                    try {
                        org.jaudiotagger.audio.AudioFile f = org.jaudiotagger.audio.AudioFileIO.read(selectedFile);
                        org.jaudiotagger.tag.Tag tag = f.getTag();
                        if (tag != null) {
                            txtTitle.setText(tag.getFirst(org.jaudiotagger.tag.FieldKey.TITLE));
                            txtArtist.setText(tag.getFirst(org.jaudiotagger.tag.FieldKey.ARTIST));
                            txtAlbum.setText(tag.getFirst(org.jaudiotagger.tag.FieldKey.ALBUM));
                            txtTitle.setForeground(Color.WHITE);
                            txtArtist.setForeground(Color.WHITE);
                            txtAlbum.setForeground(Color.WHITE);
                        }
                    } catch (Exception ex) { ex.printStackTrace(); }
                }
            });

            filePanel.add(txtFilePath, BorderLayout.CENTER);
            filePanel.add(btnBrowse, BorderLayout.EAST);
            formContainer.add(filePanel);
            formContainer.add(Box.createVerticalStrut(30));

            // Buttons
            GradientButton btnAdd = new GradientButton("♫ Add to Library");
            JButton btnClear = new JButton("Clear");

            btnAdd.addActionListener(e -> {
                String jLagu = txtTitle.getText();
                String nArtis = txtArtist.getText();
                String nAlbum = txtAlbum.getText();
                String pFile  = txtFilePath.getText();

                if (jLagu.isEmpty() || selectedFile == null) {
                    JOptionPane.showMessageDialog(this, "Data tidak lengkap!");
                    return;
                }

                BackEnd.MusicManager.getDatabaseLagu().add(new BackEnd.Lagu(jLagu, nArtis, nAlbum, pFile));
                BackEnd.MusicManager.simpanDataKeCSV();
                JOptionPane.showMessageDialog(this, "Berhasil ditambahkan!");
            });

            JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
            btnPanel.setBackground(COL_BG_MAIN);
            btnPanel.add(btnAdd); btnPanel.add(btnClear);
            formContainer.add(btnPanel);

            content.add(formContainer);
            JScrollPane scroll = new JScrollPane(content); scroll.setBorder(null);
            add(scroll, BorderLayout.CENTER);
        }

        private JPanel createInputGroup(String label, String placeholder, String rightText) {
            JPanel p = new JPanel(new BorderLayout(0, 5)); p.setBackground(COL_BG_MAIN);
            JLabel l = new JLabel(label); l.setForeground(COL_TEXT_SEC);
            JTextField t = new JTextField(placeholder);
            t.setBackground(COL_INPUT);
            t.setForeground(Color.GRAY);
            t.setBorder(BorderFactory.createCompoundBorder(new RoundedBorder(10), new EmptyBorder(10, 15, 10, 15)));
            p.add(l, BorderLayout.NORTH);
            p.add(t, BorderLayout.CENTER);

            if(rightText != null) {
                JPanel w = new JPanel(new BorderLayout());
                w.setBackground(COL_BG_MAIN);
                w.add(t, BorderLayout.CENTER);
                JLabel r = new JLabel(rightText);
                r.setForeground(Color.GRAY);
                r.setBorder(new EmptyBorder(0,10,0,0));
                w.add(r, BorderLayout.EAST);
                p.add(w, BorderLayout.CENTER);
            }
            return p;
        }
        private JPanel createInputGroup(String l, String p) {
            return createInputGroup(l, p, null);
        }
        private JPanel createFeatureCard(String icon, String title, String desc, Color c) {
            JPanel card = new JPanel();
            card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
            card.setBackground(COL_SIDEBAR);
            card.setBorder(BorderFactory.createLineBorder(new Color(50,50,50), 1, true));
            JLabel l1 = new JLabel(icon);
            l1.setFont(new Font("SansSerif", Font.PLAIN, 30));
            l1.setForeground(c);
            l1.setAlignmentX(CENTER_ALIGNMENT);
            JLabel l2 = new JLabel(title);

            l2.setFont(new Font("SansSerif", Font.BOLD, 16));
            l2.setForeground(Color.WHITE);
            l2.setAlignmentX(CENTER_ALIGNMENT);
            JLabel l3 = new JLabel(desc);
            l3.setFont(new Font("SansSerif", Font.PLAIN, 12));
            l3.setForeground(Color.GRAY);
            l3.setAlignmentX(CENTER_ALIGNMENT);
            card.add(Box.createVerticalGlue());
            card.add(l1);
            card.add(Box.createVerticalStrut(10));
            card.add(l2);
            card.add(Box.createVerticalStrut(5));
            card.add(l3);
            card.add(Box.createVerticalGlue());
            return card;
        }
    }

    // ========================================================
    // SCENE: PLAYER PANEL
    // ========================================================
    class PlayerPanel extends JPanel {
        private JLabel lblTitle, lblArtist, lblCover; // Tambahkan lblCover

        private GradientStopButton barPlayBtn;

        public PlayerPanel() {
            setLayout(new BorderLayout());
            setBackground(COL_BG_MAIN);

            JPanel centerPanel = new JPanel(new GridBagLayout());
            centerPanel.setOpaque(false);
            JPanel contentBox = new JPanel();
            contentBox.setLayout(new BoxLayout(contentBox, BoxLayout.Y_AXIS));
            contentBox.setOpaque(false);

            // --- KOMPONEN ALBUM ART ---
            lblCover = new JLabel();
            lblCover.setAlignmentX(CENTER_ALIGNMENT);
            lblCover.setPreferredSize(new Dimension(300, 300));
            lblCover.setMaximumSize(new Dimension(300, 300));
            // Set gambar default jika belum ada lagu
            setPlaceholderCover();


            lblTitle = new JLabel("No Song Playing");
            lblTitle.setFont(new Font("SansSerif", Font.BOLD, 32));
            lblTitle.setForeground(Color.WHITE);
            lblTitle.setAlignmentX(CENTER_ALIGNMENT);

            lblArtist = new JLabel("Select from library");
            lblArtist.setForeground(COL_TEXT_SEC);
            lblArtist.setFont(new Font("SansSerif", Font.PLAIN, 18));
            lblArtist.setAlignmentX(CENTER_ALIGNMENT);

            // --- SUSUNAN HIERARKI ---
            contentBox.add(lblCover); // 1. Gambar paling atas
            contentBox.add(Box.createVerticalStrut(30));
            contentBox.add(lblTitle); // 2. Judul
            contentBox.add(Box.createVerticalStrut(10));
            contentBox.add(lblArtist); // 3. Artis
            contentBox.add(Box.createVerticalStrut(40));

            centerPanel.add(contentBox);
            add(centerPanel, BorderLayout.CENTER);
            add(createFullPlaybackBar(), BorderLayout.SOUTH);
        }

        public void setSongInfo(String title, String artist, ImageIcon coverIcon) {
            lblTitle.setText(title);
            lblArtist.setText(artist);

            if (coverIcon != null) {
                // Resize gambar agar pas 300x300
                Image img = coverIcon.getImage().getScaledInstance(300, 300, Image.SCALE_SMOOTH);
                lblCover.setIcon(new ImageIcon(img));
            } else {
                setPlaceholderCover();
            }
            repaint();
        }

        private void setPlaceholderCover() {
            // Membuat kotak gradient sebagai pengganti jika tidak ada cover
            lblCover.setIcon(new Icon() {
                @Override
                public void paintIcon(Component c, Graphics g, int x, int y) {
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setPaint(new GradientPaint(0, 0, COL_CARD, 300, 300, COL_SIDEBAR));
                    g2.fillRoundRect(0, 0, 300, 300, 20, 20);
                    g2.setColor(COL_TEXT_SEC);
                    g2.drawString("No Cover", 120, 150);
                }
                @Override public int getIconWidth() { return 300; }
                @Override public int getIconHeight() { return 300; }
            });
        }

        private void updateUIFromBackend() {
            // 1. Dapatkan index lagu yang sedang aktif sekarang dari Backend
            int currentIndex = BackEnd.MusicManager.getCurrentIndex();

            if (currentIndex != -1) {
                // 2. Ambil objek lagu dari database Backend
                BackEnd.Lagu laguAktif = BackEnd.MusicManager.getDatabaseLagu().get(currentIndex);

                // 3. Ambil data gambar cover
                byte[] rawImg = BackEnd.MusicManager.getRawCover(currentIndex);
                ImageIcon coverIcon = (rawImg != null) ? new ImageIcon(rawImg) : null;

                // 4. Update tampilan PlayerPanel
                setSongInfo(laguAktif.getJudul(), laguAktif.getArtis(), coverIcon);
            }
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

            btnPrev.addActionListener(e -> {
                BackEnd.MusicManager.prevLagu();
                updateUIFromBackend();
            });
            btnNext.addActionListener(e -> {
                BackEnd.MusicManager.nextLagu();
                updateUIFromBackend();
            });

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
                } public void mouseExited(MouseEvent e) {
                    btn.setForeground(COL_TEXT_SEC);
                }
            });
            return btn;
        }
    }

    // ========================================================
    // SCENE LAINNYA
    // ========================================================
    class LibraryPanel extends JPanel {
        DefaultTableModel model;
        public LibraryPanel(MusicFlow frame) {
            setLayout(new BorderLayout());
            setBackground(COL_BG_MAIN);
            setBorder(new EmptyBorder(40, 40, 40, 40));

            JLabel title = new JLabel("Your Library");
            title.setFont(new Font("SansSerif", Font.BOLD, 32));
            title.setForeground(COL_TEXT);
            add(title, BorderLayout.NORTH);

            // Header Tabel
            String[] cols = {"#", "TITLE", "ARTIST", "ALBUM", "PATH", ""};

            // DefaultTableModel tanpa data awal (kosong)
            model = new DefaultTableModel(cols, 0) {
                @Override
                public boolean isCellEditable(int r, int c) {
                    return false;
                }
            };
            refreshData();
            // --- LOGIKA MEMBACA CSV ---
            try {
                File file = new File("musics.csv");
                if (file.exists()) {
                    java.io.BufferedReader br = new java.io.BufferedReader(new java.io.FileReader(file));
                    String line;
                    int rowNum = 1;
                    while ((line = br.readLine()) != null) {
                        // Split berdasarkan koma (sesuai format simpanBackend sebelumnya)
                        String[] data = line.split(",");
                        if (data.length >= 4) {
                            // Menambahkan baris ke model tabel: {No, Judul, Artis, Album, Path, Menu}
                            model.addRow(new Object[]{
                                    String.valueOf(rowNum++),
                                    data[0], // Judul
                                    data[1], // Artis
                                    data[2], // Album
                                    data[3], // Path (Ganti Duration jadi Path untuk dimainkan)
                                    "⋮"      // Menu icon
                            });
                        }
                    }
                    br.close();
                }
            } catch (Exception e) {
                System.err.println("Gagal memuat data library: " + e.getMessage());
            }

            JTable table = new JTable(model);

            // Modifikasi: Ambil Judul (col 1) dan Artis (col 2) saat diklik untuk dimainkan
            setupTableLogic(table, model, this, null);

            JScrollPane scroll = new JScrollPane(table);
            scroll.getViewport().setBackground(COL_BG_MAIN);
            scroll.setBorder(null);
            add(scroll, BorderLayout.CENTER);
        }
        public void refreshData() {
            model.setRowCount(0); // Hapus data lama di tabel
            try {
                File file = new File("musics.csv");
                if (file.exists()) {
                    java.io.BufferedReader br = new java.io.BufferedReader(new java.io.FileReader(file));
                    String line;
                    int rowNum = 1;
                    while ((line = br.readLine()) != null) {
                        String[] data = line.split(",");
                        if (data.length >= 4) {
                            model.addRow(new Object[]{
                                    String.valueOf(rowNum++),
                                    data[0], data[1], data[2], data[3], "⋮"
                            });
                        }
                    }
                    br.close();
                }
            } catch (Exception e) {
                System.err.println("Gagal refresh library: " + e.getMessage());
            }
        }
    }

    class PlaylistManagerPanel extends JPanel {
        private CardLayout plLayout = new CardLayout();
        private JPanel plContent = new JPanel(plLayout);
        private MusicFlow mainFrame;
        public PlaylistManagerPanel(MusicFlow frame) {
            this.mainFrame = frame;
            setLayout(new BorderLayout());
            add(plContent, BorderLayout.CENTER);
            plContent.add(new PlaylistGrid(), "GRID");
            plLayout.show(plContent, "GRID");
        }

        class PlaylistGrid extends JPanel {
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
                JPanel grid = new JPanel(new FlowLayout(FlowLayout.LEFT, 30, 30));
                grid.setBackground(COL_BG_MAIN);
                grid.add(createCard("Chill Vibes", "Relaxing beats", new Color(100, 50, 100), () -> openDetail("Chill Vibes", "Relaxing beats")
                ));
                grid.add(createCard("Workout Energy", "Power up!", new Color(50, 50, 100), () -> openDetail("Workout Energy", "Power up!")));
                add(grid, BorderLayout.CENTER);
            }
            private void openDetail(String t, String d) { plContent.add(new PlaylistDetail(t, d), "DETAIL_TEMP"); plLayout.show(plContent, "DETAIL_TEMP"); }
            private JPanel createCard(String title, String desc, Color color, Runnable onClick) {
                JPanel card = new JPanel(); card.setPreferredSize(new Dimension(220, 280)); card.setBackground(COL_CARD); card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS)); card.setBorder(new EmptyBorder(15, 15, 15, 15)); card.setCursor(new Cursor(Cursor.HAND_CURSOR));
                JPanel cover = new JPanel(new GridBagLayout()); cover.setPreferredSize(new Dimension(190, 190)); cover.setBackground(color); JLabel icon = new JLabel("▶"); icon.setForeground(Color.WHITE); icon.setFont(new Font("Segoe UI Symbol", Font.BOLD, 24)); cover.add(icon); card.add(cover); card.add(Box.createVerticalStrut(15));
                JLabel lTitle = new JLabel(title); lTitle.setFont(new Font("SansSerif", Font.BOLD, 16)); lTitle.setForeground(Color.WHITE); card.add(lTitle); card.addMouseListener(new MouseAdapter() { public void mouseClicked(MouseEvent e) { onClick.run(); } }); return card;
            }
        }
        class PlaylistDetail extends JPanel {
            public PlaylistDetail(String titleStr, String descStr) {
                setLayout(new BorderLayout());
                setBackground(COL_BG_MAIN);
                setBorder(new EmptyBorder(40, 40, 0, 40));
                JPanel header = new JPanel(new BorderLayout());
                header.setBackground(COL_BG_MAIN); JButton btnBack = new JButton("← Back to Playlists"); btnBack.setForeground(COL_ACCENT); btnBack.setContentAreaFilled(false); btnBack.setBorderPainted(false); btnBack.setFont(new Font("SansSerif", Font.BOLD, 14)); btnBack.setCursor(new Cursor(Cursor.HAND_CURSOR)); btnBack.addActionListener(e -> plLayout.show(plContent, "GRID"));
                JPanel info = new JPanel();
                info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
                info.setBackground(COL_BG_MAIN);
                JLabel title = new JLabel(titleStr);
                title.setFont(new Font("SansSerif", Font.BOLD, 40));
                title.setForeground(Color.WHITE);
                info.add(title);
                header.add(btnBack, BorderLayout.NORTH); header.add(info, BorderLayout.CENTER);
                add(header, BorderLayout.NORTH);
                String[] cols = {"#", "TITLE", "ARTIST", "ALBUM", "DURATION", ""};
                Object[][] data = {
                        {"1", "Song A", "Artist A", "Album A", "3:00", "⋮"}
                        , {"2", "Song B", "Artist B", "Album B", "4:20", "⋮"}
                };
                DefaultTableModel model = new DefaultTableModel(data, cols) {
                    public boolean isCellEditable(int r, int c) {
                        return false;
                    } };
                JTable table = new JTable(model);
                setupTableLogic(table, model, this, null);
                JScrollPane scroll = new JScrollPane(table);
                scroll.getViewport().setBackground(COL_BG_MAIN);
                scroll.setBorder(BorderFactory.createEmptyBorder(20,0,20,0));
                add(scroll, BorderLayout.CENTER);
            }
        }
        private void showCreateDialog() {
            JDialog dialog = new JDialog((Frame)SwingUtilities.getWindowAncestor(this), "Create Playlist", true);
            dialog.setUndecorated(true);
            dialog.setSize(400, 320);
            dialog.setLocationRelativeTo(this);
            JPanel p = new JPanel();
            p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
            p.setBackground(new Color(30,30,30));

            p.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(60,60,60)),
                    new EmptyBorder(25,25,25,25)));

            JLabel l = new JLabel("Create New Playlist");
            l.setForeground(Color.WHITE);
            l.setFont(new Font("SansSerif", Font.BOLD, 20));
            l.setAlignmentX(LEFT_ALIGNMENT);
            JLabel lName = new JLabel("Name");
            lName.setForeground(COL_TEXT_SEC);
            lName.setAlignmentX(LEFT_ALIGNMENT);
            JTextField tfName = new JTextField();
            tfName.setBackground(new Color(50,50,50));
            tfName.setForeground(Color.WHITE);
            tfName.setCaretColor(Color.WHITE);
            tfName.setBorder(BorderFactory.createEmptyBorder(8,10,8,10));
            tfName.setMaximumSize(new Dimension(400, 35));
            tfName.setAlignmentX(LEFT_ALIGNMENT);
            JLabel lDesc = new JLabel("De" + "scription");
            lDesc.setForeground(COL_TEXT_SEC);
            lDesc.setAlignmentX(LEFT_ALIGNMENT);
            JTextArea taDesc = new JTextArea(3, 20);
            taDesc.setBackground(new Color(50,50,50));
            taDesc.setForeground(Color.WHITE);
            taDesc.setCaretColor(Color.WHITE);
            taDesc.setBorder(BorderFactory.createEmptyBorder(8,10,8,10));
            JScrollPane scrollDesc = new JScrollPane(taDesc);
            scrollDesc.setBorder(null);
            scrollDesc.setMaximumSize(new Dimension(400, 80));
            scrollDesc.setAlignmentX(LEFT_ALIGNMENT);
            PurpleButton btn = new PurpleButton("Create");
            btn.setAlignmentX(LEFT_ALIGNMENT);
            btn.addActionListener(e->dialog.dispose());
            JButton cancel = new JButton("Cancel");
            cancel.setContentAreaFilled(false);
            cancel.setBorderPainted(false);
            cancel.setForeground(Color.GRAY);
            cancel.addActionListener(e->dialog.dispose());
            p.add(l);
            p.add(Box.createVerticalStrut(20));
            p.add(lName);
            p.add(Box.createVerticalStrut(5));
            p.add(tfName);
            p.add(Box.createVerticalStrut(15));
            p.add(lDesc);
            p.add(Box.createVerticalStrut(5));
            p.add(scrollDesc);
            p.add(Box.createVerticalStrut(25));
            JPanel btnP = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0,0));
            btnP.setOpaque(false);
            btnP.setAlignmentX(LEFT_ALIGNMENT);
            btnP.setMaximumSize(new Dimension(400, 40));
            btnP.add(cancel);
            btnP.add(btn);
            p.add(btnP);
            dialog.add(p);
            dialog.setVisible(true);
        }
    }

    interface TableAction { void onPlay(String t, String a); }
    private void setupTableLogic(JTable table, DefaultTableModel model, Component parent, TableAction action) {
        table.setBackground(COL_BG_MAIN);
        table.setForeground(COL_TEXT);
        table.setRowHeight(55);
        table.setShowGrid(false);
        table.setSelectionBackground(new Color(35, 35, 35));
        table.setSelectionForeground(COL_TEXT);
        JTableHeader th = table.getTableHeader();
        th.setBackground(COL_BG_MAIN);
        th.setForeground(COL_TEXT_SEC);
        th.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(50,50,50)));
        table.putClientProperty("hoveredRow", -1);
        table.addMouseMotionListener(new MouseMotionAdapter()
        { public void mouseMoved(MouseEvent e) {
            int row = table.rowAtPoint(e.getPoint());
            if (row != (int)table.getClientProperty("hoveredRow"))
            {
                table.putClientProperty("hoveredRow", row);
                table.repaint();
            }
        }});
        table.addMouseListener(new MouseAdapter() {
            public void mouseExited(MouseEvent e) {
                table.putClientProperty("hoveredRow", -1);
                table.repaint();
            }
            public void mouseClicked(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                int col = table.columnAtPoint(e.getPoint());

                if (row != -1) {
                    // Jika klik bukan pada kolom menu (titik tiga)
                    if (col != model.getColumnCount() - 1) {
                        // 1. Ambil data teks dari model tabel
                        String title = model.getValueAt(row, 1).toString();
                        String artist = model.getValueAt(row, 2).toString();

                        // 2. Ambil data gambar (ImageIcon) dari Backend berdasarkan index baris
                        byte[] rawImg = BackEnd.MusicManager.getRawCover(row);
                        ImageIcon albumIcon = null;
                        if (rawImg != null) {
                            albumIcon = new ImageIcon(rawImg);
                        }

                        // 3. Panggil playSong dengan 3 parameter: Judul, Artis, dan ImageIcon
                        playSong(row, title, artist, albumIcon);
                    } else {
                        showPopupMenu(e, row, model, parent);
                    }
                }
            } });
        table.getColumnModel().getColumn(0).setCellRenderer(new DefaultTableCellRenderer() { public Component getTableCellRendererComponent(JTable table, Object value, boolean isSel, boolean hasFocus, int row, int col) { super.getTableCellRendererComponent(table, value, isSel, hasFocus, row, col); setHorizontalAlignment(CENTER); if (row == (int) table.getClientProperty("hoveredRow")) { setText("▶"); setForeground(COL_ACCENT); setFont(new Font("Segoe UI Symbol", Font.BOLD, 18)); } else { setText(value.toString()); setForeground(COL_TEXT_SEC); setFont(new Font("SansSerif", Font.PLAIN, 14)); } return this; } });
        table.getColumnModel().getColumn(model.getColumnCount()-1).setCellRenderer(new DefaultTableCellRenderer()
        {
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSel, boolean hasFocus, int row, int col)
            {
                super.getTableCellRendererComponent(table, value, isSel, hasFocus, row, col);
                setForeground(COL_TEXT_SEC);
                setFont(new Font("Segoe UI Symbol", Font.BOLD, 18));
                setHorizontalAlignment(CENTER); return this;
            }
        });
        table.getColumnModel().getColumn(0).setMaxWidth(50);
        table.getColumnModel().getColumn(model.getColumnCount()-1).setMaxWidth(50);
    }
    private void showPopupMenu(MouseEvent e, int row, DefaultTableModel model, Component parent) {
        JPopupMenu popup = new JPopupMenu();
        // ... styling popup ...
        if (row == BackEnd.MusicManager.getCurrentIndex()) {
            BackEnd.MusicManager.stopLagu();
        }
        JMenuItem delItem = new JMenuItem("Delete Song");
        delItem.addActionListener(evt -> {
            // 1. Hapus dari ArrayList di Backend
            // Karena urutan tabel = urutan ArrayList
            BackEnd.MusicManager.getDatabaseLagu().remove(row);

            // 2. Simpan perubahan ke CSV agar permanen
            BackEnd.MusicManager.simpanDataKeCSV();

            // 3. Refresh tampilan LibraryPanel
            if (parent instanceof LibraryPanel) {
                ((LibraryPanel) parent).refreshData();
            }

            JOptionPane.showMessageDialog(parent, "Lagu berhasil dihapus!");
        });

        popup.add(delItem);
        popup.show(e.getComponent(), e.getX(), e.getY());
    }

    // --- CUSTOM BUTTONS ---
    class GradientStopButton extends JButton {

        private int size;
        public GradientStopButton(int size) {
            this.size = size;
            setPreferredSize(new Dimension(size, size));
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));

            // Listener untuk memanggil fungsi stop di Backend
            addActionListener(e -> {
                BackEnd.MusicManager.stopLagu();
                repaint();
            });
        }

        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Menggunakan palet warna aksen yang sama dengan tema MusicFlow
            g2.setPaint(new GradientPaint(0, 0, COL_ACCENT, getWidth(), getHeight(), COL_ACCENT_2));
            g2.fillOval(0, 0, getWidth(), getHeight());

            g2.setColor(Color.WHITE);
            int cx = getWidth() / 2;
            int cy = getHeight() / 2;

            // Menggambar Ikon STOP (Kotak di Tengah)
            // Ukuran kotak disesuaikan secara proporsional dengan ukuran button
            int stopSize = size / 3;
            int x = cx - (stopSize / 2);
            int y = cy - (stopSize / 2);

            // fillRoundRect memberikan kesan modern dengan sudut sedikit melengkung (arc 5, 5)
            g2.fillRoundRect(x, y, stopSize, stopSize, 5, 5);
        }
    }
    class PurpleButton extends JButton {
        public PurpleButton(String text)
        {
            super(text);
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setForeground(Color.WHITE);
            setFont(new Font("SansSerif", Font.BOLD, 14));
            setCursor(new Cursor(Cursor.HAND_CURSOR));
        }
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(COL_ACCENT);
            g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 15, 15));
            super.paintComponent(g);
        }
    }
    class SidebarButton extends JButton {
        String targetScene;
        boolean isActive;
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
        public void setActive(boolean b) {
            isActive = b;
            setForeground(b ? Color.WHITE : COL_TEXT_SEC);
            repaint();
        }
        protected void paintComponent(Graphics g) {
            if(isActive)
            {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(COL_ACCENT);
                g2.fill(new RoundRectangle2D.Double(0,0,getWidth(),getHeight(),15,15));
            }
            super.paintComponent(g);
        }
    }
    class IconSymbol implements Icon {
        Color c;
        int s;
        public IconSymbol(Color c, int s) {
            this.c=c;
            this.s=s;
        } public int getIconWidth() {
            return s;
        }
        public int getIconHeight() {
            return s;
        }
        public void paintIcon(Component cmp, Graphics g, int x, int y) {
            g.setColor(c);
            g.fillRoundRect(x, y, s, s, 5, 5);
            g.setColor(Color.WHITE);
            g.drawOval(x+5, y+5, s-10, s-10);
        }
    }
    class ArtPanel extends JPanel {
        int t, w, h;
        public ArtPanel(int t, int w, int h) {
            this.t=t;
            this.w=w;
            this.h=h;
            setPreferredSize(new Dimension(w,h));
            setBackground(COL_BG_MAIN);
        }
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D)g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int s = Math.min(getWidth(), getHeight());
            if (t==1) {
                g2.setPaint(new GradientPaint(0,0,new Color(100,50,150),s,s,new Color(255,100,100)));
                g2.fillRoundRect(0,0,s,s,20,20);
            } else if (t==2) {
                g2.setColor(new Color(20,20,50));
                g2.fillRoundRect(0,0,s,s,20,20);
                g2.setColor(Color.CYAN);
                g2.drawOval(10,10,s-20,s-20);
            } else {
                g2.setColor(new Color(240,220,100));
                g2.fillRoundRect(0,0,s,s,20,20);
                g2.setColor(Color.BLACK);
                g2.fillOval(5,5,s-10,s-10);
                g2.setColor(Color.RED);
                g2.fillOval(s/2-15,s/2-15,30,30);
            }
        }
    }
    class DashedBorder extends EmptyBorder {
        Color c;
        int t, a;
        public DashedBorder(Color c, int t, int a) {
            super(t,t,t,t);
            this.c=c;
            this.t=t;
            this.a=a;
        }
        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D)g.create();
            g2.setColor(this.c);
            g2.setStroke(new BasicStroke(t, 0, 0, 10, new float[]{10}, 0));
            g2.drawRoundRect(x+t/2, y+t/2, w-t, h-t, a, a); g2.dispose();
        }
    }
    class RoundedBorder implements Border {
        int r; RoundedBorder(int r) {
            this.r=r;
        }
        public Insets getBorderInsets(Component c) {
            return new Insets(r,r,r,r);
        } public boolean isBorderOpaque() {
            return true;
        }
        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {

        }
    }
    class GradientButton extends JButton {
        public GradientButton(String text)
        {
            super(text);
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setForeground(Color.WHITE);
            setFont(new Font("SansSerif", Font.BOLD, 15));
            setCursor(new Cursor(Cursor.HAND_CURSOR));
        }
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setPaint(new GradientPaint(0,0, COL_ACCENT, getWidth(), 0, COL_ACCENT_2));
            g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 15, 15));
            super.paintComponent(g);
        }
    }
}