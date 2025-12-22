package FrontEnd;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
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
import java.util.ArrayList;

public class MusicFlow extends JFrame {

    // --- WARNA TEMA ---
    public static final Color COL_BG_MAIN = new Color(18, 18, 18);
    public static final Color COL_SIDEBAR = new Color(10, 10, 10);
    public static final Color COL_ACCENT  = new Color(189, 0, 255); // Ungu Vivid
    public static final Color COL_ACCENT_2= new Color(255, 0, 128); // Pink
    public static final Color COL_CARD    = new Color(30, 30, 30);
    public static final Color COL_TEXT    = Color.WHITE;
    public static final Color COL_TEXT_SEC= new Color(170, 170, 170);

    // --- KOMPONEN UTAMA ---
    private CardLayout cardLayout = new CardLayout();
    private JPanel mainContentPanel = new JPanel(cardLayout);
    private java.util.List<SidebarButton> navButtons = new ArrayList<>();

    private PlayerPanel playerPanel;

    public MusicFlow() {
        setTitle("MusicFlow Premium");
        setSize(1200, 800); // Sedikit lebih tinggi untuk bar bawah
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

        navigate("LIBRARY");
    }

    public void playSong(String title, String artist) {
        playerPanel.setSongInfo(title, artist);
        // navigate("PLAYER"); // Aktifkan jika ingin otomatis pindah ke Player saat klik lagu
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
            sidebar.add(btn);
            sidebar.add(Box.createVerticalStrut(10));
        }

        sidebar.add(Box.createVerticalGlue());
        JPanel pPanel = new JPanel(new GridLayout(2,1)); pPanel.setOpaque(false);
        JLabel p1 = new JLabel("Premium Features");
        p1.setForeground(Color.WHITE); p1.setFont(new Font("SansSerif", Font.BOLD, 12));
        JLabel p2 = new JLabel("Unlimited songs");
        p2.setForeground(new Color(255,255,255,180));
        p2.setFont(new Font("SansSerif", Font.PLAIN, 10));
        pPanel.add(p1); pPanel.add(p2);

        JPanel badge = new JPanel(new BorderLayout()) {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D)g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0,0, COL_ACCENT, getWidth(), getHeight(), COL_ACCENT_2);
                g2.setPaint(gp);
                g2.fillRoundRect(0,0,getWidth(),getHeight(), 15, 15);
            }
        };
        badge.setOpaque(false);
        badge.setBorder(new EmptyBorder(15,15,15,15));
        badge.setMaximumSize(new Dimension(200, 70));
        badge.add(pPanel, BorderLayout.CENTER);
        badge.setAlignmentX(LEFT_ALIGNMENT);
        sidebar.add(badge);
        return sidebar;
    }

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception e){}
        SwingUtilities.invokeLater(() -> new MusicFlow().setVisible(true));
    }

    // ========================================================
    // SCENE: PLAYER PANEL (DIPERBAIKI TOTAL)
    // ========================================================
    class PlayerPanel extends JPanel {
        private JLabel lblTitle, lblArtist;
        private GradientPlayButton mainPlayBtn; // Tombol Besar di Tengah
        private GradientPlayButton barPlayBtn;  // Tombol Kecil di Bar Bawah

        public PlayerPanel() {
            // Menggunakan BorderLayout agar bisa menaruh Bar di bawah (South)
            setLayout(new BorderLayout());
            setBackground(COL_BG_MAIN);

            // --- BAGIAN TENGAH (INFO LAGU & TOMBOL BESAR) ---
            JPanel centerPanel = new JPanel(new GridBagLayout()); // Untuk centering vertikal/horizontal
            centerPanel.setOpaque(false);

            JPanel contentBox = new JPanel();
            contentBox.setLayout(new BoxLayout(contentBox, BoxLayout.Y_AXIS));
            contentBox.setOpaque(false);

            // Tombol Play Besar Gradient (Ikon diperbaiki)
            mainPlayBtn = new GradientPlayButton(100); // Ukuran 100
            mainPlayBtn.setAlignmentX(CENTER_ALIGNMENT);

            lblTitle = new JLabel("No Song Playing");
            lblTitle.setFont(new Font("SansSerif", Font.BOLD, 32));
            lblTitle.setForeground(Color.WHITE);
            lblTitle.setAlignmentX(CENTER_ALIGNMENT);

            lblArtist = new JLabel("Select from library");
            lblArtist.setForeground(COL_TEXT_SEC);
            lblArtist.setFont(new Font("SansSerif", Font.PLAIN, 18));
            lblArtist.setAlignmentX(CENTER_ALIGNMENT);

            contentBox.add(mainPlayBtn);
            contentBox.add(Box.createVerticalStrut(40));
            contentBox.add(lblTitle);
            contentBox.add(Box.createVerticalStrut(10));
            contentBox.add(lblArtist);

            centerPanel.add(contentBox);
            add(centerPanel, BorderLayout.CENTER);

            // --- BAGIAN BAWAH (FULL PLAYBACK BAR) ---
            add(createFullPlaybackBar(), BorderLayout.SOUTH);
        }

        public void setSongInfo(String title, String artist) {
            lblTitle.setText(title);
            lblArtist.setText(artist);
            // Set kedua tombol jadi state 'Playing' (icon Pause)
            mainPlayBtn.setPlaying(true);
            barPlayBtn.setPlaying(true);
            repaint();
        }

        private JPanel createFullPlaybackBar() {
            JPanel bar = new JPanel(new BorderLayout(0, 20)); // Gap vertikal antar elemen bar
            bar.setBackground(COL_CARD); // Warna latar bar sedikit lebih terang
            bar.setBorder(new EmptyBorder(25, 40, 25, 40)); // Padding besar

            // 1. Progress Bar & Waktu
            JPanel progressPanel = new JPanel(new BorderLayout(15, 0));
            progressPanel.setOpaque(false);
            JLabel lblStart = new JLabel("0:45");
            lblStart.setForeground(COL_TEXT_SEC);
            JLabel lblEnd = new JLabel("4:20");
            lblEnd.setForeground(COL_TEXT_SEC);

            JProgressBar progressBar = new JProgressBar();
            progressBar.setValue(35);
            progressBar.setPreferredSize(new Dimension(100, 8));
            progressBar.setForeground(COL_ACCENT);
            progressBar.setBackground(new Color(50,50,50));
            progressBar.setBorderPainted(false);
            // Trik kecil untuk menghilangkan border default JProgressBar di beberapa L&F
            progressBar.setUI(new BasicProgressBarUI()
            {
                protected Color getSelectionBackground() {
                    return COL_ACCENT;
                }
                protected Color getSelectionForeground() {
                    return COL_ACCENT;
                }
            });

            progressPanel.add(lblStart, BorderLayout.WEST);
            progressPanel.add(progressBar, BorderLayout.CENTER);
            progressPanel.add(lblEnd, BorderLayout.EAST);

            // 2. Kontrol (Shuffle, Prev, Play, Next, Repeat)
            JPanel controls = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 0));
            controls.setOpaque(false);

            JButton btnShuffle = createIconButton("🔀", 18);
            JButton btnPrev = createIconButton("⏮", 28);
            barPlayBtn = new GradientPlayButton(60); // Tombol Play Gradient Kecil (Ukuran 60)
            JButton btnNext = createIconButton("⏭", 28);
            JButton btnRepeat = createIconButton("🔁", 18);

            controls.add(btnShuffle);
            controls.add(btnPrev);
            controls.add(barPlayBtn);
            controls.add(btnNext);
            controls.add(btnRepeat);

            bar.add(progressPanel, BorderLayout.NORTH);
            bar.add(controls, BorderLayout.CENTER);

            return bar;
        }

        private JButton createIconButton(String icon, int size) {
            JButton btn = new JButton(icon);
            btn.setContentAreaFilled(false); btn.setBorderPainted(false); btn.setFocusPainted(false);
            btn.setForeground(COL_TEXT_SEC);
            btn.setFont(new Font("Segoe UI Symbol", Font.PLAIN, size)); // Gunakan font yang support simbol
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btn.addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { btn.setForeground(Color.WHITE); }
                public void mouseExited(MouseEvent e) { btn.setForeground(COL_TEXT_SEC); }
            });
            return btn;
        }
    }

    // ========================================================
    // CUSTOM COMPONENTS (DIPERBAIKI ICONS NYA)
    // ========================================================

    // 1. TOMBOL PLAY GRADIENT (HASIL PERBAIKAN)
    class GradientPlayButton extends JButton {
        private boolean isPlaying = false;
        private int size;

        public GradientPlayButton(int size) {
            this.size = size;
            setPreferredSize(new Dimension(size, size));
            setContentAreaFilled(false); setFocusPainted(false); setBorderPainted(false); setCursor(new Cursor(Cursor.HAND_CURSOR));
            // Sinkronisasi klik: jika tombol ini diklik, ubah state-nya sendiri
            addActionListener(e -> { isPlaying = !isPlaying; repaint(); });
        }
        public void setPlaying(boolean b) { isPlaying = b; repaint(); }

        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g; g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Gambar Lingkaran Gradient
            g2.setPaint(new GradientPaint(0, 0, COL_ACCENT, getWidth(), getHeight(), COL_ACCENT_2));
            g2.fillOval(0, 0, getWidth(), getHeight());

            // Gambar Ikon Putih (Lebih Presisi & Halus)
            g2.setColor(Color.WHITE);
            int cx = getWidth() / 2;
            int cy = getHeight() / 2;

            if (isPlaying) {
                // Ikon PAUSE (Dua Batang Rounded)
                int barW = size / 7;
                int barH = size / 3;
                int gap = size / 10;
                g2.fillRoundRect(cx - barW - gap/2, cy - barH/2, barW, barH, barW/2, barW/2);
                g2.fillRoundRect(cx + gap/2,           cy - barH/2, barW, barH, barW/2, barW/2);
            } else {
                // Ikon PLAY (Segitiga Rounded)
                Path2D p = new Path2D.Double();
                double iconR = size / 3.5; // Radius ikon
                double offset = size / 20.0; // Geser sedikit ke kanan agar terlihat tengah secara visual

                // Koordinat segitiga yang lebih halus
                p.moveTo(cx - iconR + offset, cy - iconR);
                p.lineTo(cx + iconR + offset, cy);
                p.lineTo(cx - iconR + offset, cy + iconR);
                p.closePath();

                // Gunakan stroke tebal dengan join rounded agar sudutnya tidak tajam
                g2.setStroke(new BasicStroke((float)size/15, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.fill(p);
            }
        }
    }

    // ========================================================
    // SCENE LAINNYA (LIBRARY, PLAYLIST, ADD MUSIC - TETAP AMAN)
    // ========================================================
    class LibraryPanel extends JPanel {
        public LibraryPanel(MusicFlow frame) {
            setLayout(new BorderLayout());
            setBackground(COL_BG_MAIN); setBorder(new EmptyBorder(40, 40, 40, 40));
            JLabel title = new JLabel("Your Library");
            title.setFont(new Font("SansSerif", Font.BOLD, 32));
            title.setForeground(COL_TEXT);
            add(title, BorderLayout.NORTH);
            String[] cols = {"#", "TITLE", "ARTIST", "ALBUM", "DURATION", ""};
            Object[][] data = {
                    {"1", "Midnight Dreams", "Luna Echo", "Nocturnal", "4:05", "⋮"}, {"2", "Electric Sunrise", "Nova Beats", "Dawn", "3:18", "⋮"}, {"3", "Vintage Vibes", "Retro Soul", "Classic", "3:43", "⋮"}, {"4", "Ocean Waves", "Coastal Dreams", "Blue Horizon", "4:27", "⋮"}, {"5", "City Lights", "Urban Symphony", "Metropolitan", "3:21", "⋮"}
            };
            DefaultTableModel model = new DefaultTableModel(data, cols) {
                public boolean isCellEditable(int r, int c) { return false;
                }
            };
            JTable table = new JTable(model);
            setupTableLogic(table, model, this, (t, a) -> frame.playSong(t, a));
            JScrollPane scroll = new JScrollPane(table); scroll.getViewport().setBackground(COL_BG_MAIN);
            scroll.setBorder(null);
            add(scroll, BorderLayout.CENTER);
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
                setLayout(new BorderLayout()); setBackground(COL_BG_MAIN);
                setBorder(new EmptyBorder(40, 40, 40, 40));
                JPanel header = new JPanel(new BorderLayout());
                header.setBackground(COL_BG_MAIN); JLabel title = new JLabel("Your Playlists");
                title.setFont(new Font("SansSerif", Font.BOLD, 32)); title.setForeground(COL_TEXT);
                PurpleButton btnCreate = new PurpleButton("+ Create Playlist");
                btnCreate.addActionListener(e -> showCreateDialog()); header.add(title, BorderLayout.WEST);
                header.add(btnCreate, BorderLayout.EAST);
                add(header, BorderLayout.NORTH);
                JPanel grid = new JPanel(new FlowLayout(FlowLayout.LEFT, 30, 30));
                grid.setBackground(COL_BG_MAIN);
                grid.add(createCard("Chill Vibes", "Relaxing beats", new Color(100, 50, 100), () -> openDetail("Chill Vibes", "Relaxing beats")));
                grid.add(createCard("Workout Energy", "Power up!", new Color(50, 50, 100), () -> openDetail("Workout Energy", "Power up!")));
                add(grid, BorderLayout.CENTER);
            }
            private void openDetail(String t, String d) { plContent.add(new PlaylistDetail(t, d), "DETAIL_TEMP");
                plLayout.show(plContent, "DETAIL_TEMP");
            }
            private JPanel createCard(String title, String desc, Color color, Runnable onClick) {
                JPanel card = new JPanel();
                card.setPreferredSize(new Dimension(220, 280));
                card.setBackground(COL_CARD);
                card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
                card.setBorder(new EmptyBorder(15, 15, 15, 15));
                card.setCursor(new Cursor(Cursor.HAND_CURSOR));
                JPanel cover = new JPanel(new GridBagLayout());
                cover.setPreferredSize(new Dimension(190, 190));
                cover.setBackground(color); JLabel icon = new JLabel("▶");
                icon.setForeground(Color.WHITE);
                icon.setFont(new Font("Segoe UI Symbol", Font.BOLD, 24));
                cover.add(icon);
                card.add(cover);
                card.add(Box.createVerticalStrut(15));
                JLabel lTitle = new JLabel(title);
                lTitle.setFont(new Font("SansSerif", Font.BOLD, 16));
                lTitle.setForeground(Color.WHITE);
                card.add(lTitle);
                card.addMouseListener(new MouseAdapter() {
                    public void mouseClicked(MouseEvent e) { onClick.run(); } });
                return card;
            }
        }
        class PlaylistDetail extends JPanel {
            public PlaylistDetail(String titleStr, String descStr) {
                setLayout(new BorderLayout());
                setBackground(COL_BG_MAIN);
                setBorder(new EmptyBorder(40, 40, 0, 40));
                JPanel header = new JPanel(new BorderLayout());
                header.setBackground(COL_BG_MAIN); JButton btnBack = new JButton("← Back to Playlists");
                btnBack.setForeground(COL_ACCENT);
                btnBack.setContentAreaFilled(false);
                btnBack.setBorderPainted(false);
                btnBack.setFont(new Font("SansSerif", Font.BOLD, 14));
                btnBack.setCursor(new Cursor(Cursor.HAND_CURSOR));
                btnBack.addActionListener(e -> plLayout.show(plContent, "GRID"));
                JPanel info = new JPanel();
                info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
                info.setBackground(COL_BG_MAIN);
                JLabel title = new JLabel(titleStr);
                title.setFont(new Font("SansSerif", Font.BOLD, 40));
                title.setForeground(Color.WHITE); info.add(title);
                header.add(btnBack, BorderLayout.NORTH);
                header.add(info, BorderLayout.CENTER);
                add(header, BorderLayout.NORTH);
                String[] cols = {"#", "TITLE", "ARTIST", "ALBUM", "DURATION", ""};
                Object[][] data = {
                        {"1", "Song A", "Artist A", "Album A", "3:00", "⋮"}, {"2", "Song B", "Artist B", "Album B", "4:20", "⋮"}};
                DefaultTableModel model = new DefaultTableModel(data, cols) {
                    public boolean isCellEditable(int r, int c)
                    {
                        return false;
                    }
                }; JTable table = new JTable(model);
                setupTableLogic(table, model, this, (t, a) -> mainFrame.playSong(t, a));
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
            p.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(60,60,60)), new EmptyBorder(25,25,25,25)));
            JLabel l = new JLabel("Create New Playlist");
            l.setForeground(Color.WHITE);
            l.setFont(new Font("SansSerif", Font.BOLD, 20));
            l.setAlignmentX(LEFT_ALIGNMENT);
            JLabel lName = new JLabel("Name"); lName.setForeground
                    (COL_TEXT_SEC); lName.setAlignmentX(LEFT_ALIGNMENT);
                    JTextField tfName = new JTextField();
                    tfName.setBackground(new Color(50,50,50)); tfName.setForeground(Color.WHITE);
                    tfName.setCaretColor(Color.WHITE);
                    tfName.setBorder(BorderFactory.createEmptyBorder(8,10,8,10));
                    tfName.setMaximumSize(new Dimension(400, 35));
                    tfName.setAlignmentX(LEFT_ALIGNMENT);
            JLabel lDesc = new JLabel("Description");
            lDesc.setForeground(COL_TEXT_SEC);
            lDesc.setAlignmentX(LEFT_ALIGNMENT);
            JTextArea taDesc = new JTextArea(3, 20);
            taDesc.setBackground(new Color(50,50,50));
            taDesc.setForeground(Color.WHITE); taDesc.setCaretColor(Color.WHITE);
            taDesc.setBorder(BorderFactory.createEmptyBorder(8,10,8,10));
            JScrollPane scrollDesc = new JScrollPane(taDesc);
            scrollDesc.setBorder(null);
            scrollDesc.setMaximumSize(new Dimension(400, 80));
            scrollDesc.setAlignmentX(LEFT_ALIGNMENT);
            PurpleButton btn = new PurpleButton("Create");
            btn.setAlignmentX(LEFT_ALIGNMENT);
            btn.addActionListener(e->dialog.dispose()); JButton cancel = new JButton("Cancel");
            cancel.setContentAreaFilled(false);
            cancel.setBorderPainted(false);
            cancel.setForeground(Color.GRAY);
            cancel.addActionListener(e->dialog.dispose());
            p.add(l); p.add(Box.createVerticalStrut(20));
            p.add(lName); p.add(Box.createVerticalStrut(5));
            p.add(tfName); p.add(Box.createVerticalStrut(15));
            p.add(lDesc); p.add(Box.createVerticalStrut(5));
            p.add(scrollDesc); p.add(Box.createVerticalStrut(25));
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

    class AddMusicPanel extends JPanel {
        public AddMusicPanel() {
            setLayout(new BorderLayout());
            setBackground(COL_BG_MAIN);
            JPanel content = new JPanel();
            content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
            content.setBackground(COL_BG_MAIN);
            content.setBorder(new EmptyBorder(40, 60, 40, 60));
            JLabel title = new JLabel("Add New Music");
            title.setFont(new Font("SansSerif", Font.BOLD, 32));
            title.setForeground(COL_TEXT);
            title.setAlignmentX(LEFT_ALIGNMENT);
            content.add(title);
            content.add(Box.createVerticalStrut(30));
            JPanel form = new JPanel(new GridLayout(2, 2, 20, 20));
            form.setBackground(COL_BG_MAIN);
            form.setMaximumSize(new Dimension(800, 140));
            form.setAlignmentX(LEFT_ALIGNMENT);
            form.add(createInput("Song Title"));
            form.add(createInput("Artist"));
            form.add(createInput("Album"));
            form.add(createInput("Duration"));
            content.add(form);
            content.add(Box.createVerticalStrut(30));
            JPanel upload = new JPanel();
            upload.setBackground(COL_BG_MAIN);
            upload.setBorder(BorderFactory.createDashedBorder(Color.GRAY, 2, 5, 5, true)); upload.setMaximumSize(new Dimension(800, 100)); upload.add(new JLabel("Drag & Drop Audio File Here"){{setForeground(Color.GRAY); setFont(new Font("SansSerif", Font.BOLD, 14));}}); upload.setAlignmentX(LEFT_ALIGNMENT); content.add(upload); content.add(Box.createVerticalStrut(30));
            PurpleButton btnAdd = new PurpleButton("Add to Library");
            btnAdd.setAlignmentX(LEFT_ALIGNMENT);
            content.add(btnAdd);
            add(content, BorderLayout.CENTER);
        }
        private JPanel createInput(String label)
        { JPanel p = new JPanel(new BorderLayout());
            p.setBackground(COL_BG_MAIN);
            JLabel l = new JLabel(label);
            l.setForeground(COL_TEXT_SEC);
            JTextField t = new JTextField();
            t.setBackground(new Color(40,40,40));
            t.setForeground(Color.WHITE);
            t.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
            p.add(l, BorderLayout.NORTH);
            p.add(t, BorderLayout.CENTER);
            return p;
        }
    }

    interface TableAction { void onPlay(String t, String a); }
    private void setupTableLogic(JTable table, DefaultTableModel model, Component parent, TableAction action)
    {
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
    {
        public void mouseMoved(MouseEvent e)

    { int row = table.rowAtPoint(e.getPoint());
        if (row != (int)table.getClientProperty("hoveredRow"))
        { table.putClientProperty("hoveredRow", row);
            table.repaint(); } } });
        table.addMouseListener(new MouseAdapter() {
            public void mouseExited(MouseEvent e)
            { table.putClientProperty("hoveredRow", -1);
                table.repaint();
            } public void mouseClicked(MouseEvent e)
            { int row = table.rowAtPoint(e.getPoint());
                int col = table.columnAtPoint(e.getPoint());
                if (row != -1) { if (col == model.getColumnCount() - 1) showPopupMenu(e, row, model, parent);
                    else if (action != null)
                        action.onPlay(model.getValueAt(row, 1).toString(), model.getValueAt(row, 2).toString());
                }
            }
        });
        table.getColumnModel().getColumn(0).setCellRenderer(new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSel, boolean hasFocus, int row, int col)
            {
                super.getTableCellRendererComponent(table, value, isSel, hasFocus, row, col);
                setHorizontalAlignment(CENTER);
                if (row == (int)
                        table.getClientProperty("hoveredRow"))
                { setText("▶");
                    setForeground(COL_ACCENT);
                    setFont(new Font("Segoe UI Symbol", Font.BOLD, 18));
                } else { setText(value.toString());
                    setForeground(COL_TEXT_SEC);
                    setFont(new Font("SansSerif", Font.PLAIN, 14));
                } return this;
            }
        });
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
    private void showPopupMenu(MouseEvent e, int row, DefaultTableModel model, Component parent)
    {
        JPopupMenu popup = new JPopupMenu();
        popup.setBackground(new Color(40, 40, 40));
        popup.setBorder(BorderFactory.createLineBorder(new Color(60,60,60)));
        JMenuItem delItem = new JMenuItem("Delete Song");
        delItem.setBackground(new Color(40, 40, 40));
        delItem.setForeground(new Color(255, 80, 80));
        delItem.setBorder(new EmptyBorder(10, 20, 10, 20));
        delItem.addActionListener(evt -> model.removeRow(row)); popup.add(delItem);
        popup.show(e.getComponent(), e.getX(), e.getY());
    }

    class PurpleButton extends JButton
    { public PurpleButton(String text)
    {
        super(text);
        setContentAreaFilled(false);
        setFocusPainted(false);
        setBorderPainted(false);
        setForeground(Color.WHITE);
        setFont(new Font("SansSerif", Font.BOLD, 14));
        setCursor(new Cursor(Cursor.HAND_CURSOR));
    } protected void paintComponent(Graphics g)
    {
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
        public SidebarButton(String text, String scene)
        {
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
}