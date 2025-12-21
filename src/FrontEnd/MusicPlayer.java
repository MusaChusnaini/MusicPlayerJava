package FrontEnd;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class MusicPlayer extends JFrame {

    // --- 1. CONTROLLER / MAIN FRAME ---

    private CardLayout cardLayout = new CardLayout();
    private JPanel mainContentPanel = new JPanel(cardLayout);

    // Database Sementara (Mock Data)
    private List<Playlist> playlists = new ArrayList<>();

    // Referensi ke Views
    private LibraryView libraryView;
    private DetailView detailView;
    private CreateView createView;
    private PlayerView playerView;

    public MusicPlayer() {
        initMockData();
        setupUI();
    }

    private void setupUI() {
        setTitle("GUI"); // Judul Window
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Inisialisasi Views
        libraryView = new LibraryView(this);
        detailView = new DetailView(this);
        createView = new CreateView(this);
        playerView = new PlayerView(this);

        // Tambahkan Views ke CardLayout
        mainContentPanel.add(libraryView, "LIBRARY");
        mainContentPanel.add(detailView, "DETAIL");
        mainContentPanel.add(createView, "CREATE");
        mainContentPanel.add(playerView, "PLAYER");

        // Sidebar Navigation
        add(new Sidebar(this), BorderLayout.WEST);
        add(mainContentPanel, BorderLayout.CENTER);

        showLibrary(); // Tampilan awal
    }

    // --- NAVIGATION METHODS ---

    public void showLibrary() {
        libraryView.refreshData(playlists);
        cardLayout.show(mainContentPanel, "LIBRARY");
    }

    public void showDetail(Playlist p) {
        detailView.setPlaylist(p);
        cardLayout.show(mainContentPanel, "DETAIL");
    }

    public void showCreate() {
        cardLayout.show(mainContentPanel, "CREATE");
    }

    public void showPlayer(Song s) {
        playerView.setSong(s);
        cardLayout.show(mainContentPanel, "PLAYER");
    }

    public void addPlaylist(Playlist p) {
        playlists.add(p);
        showLibrary();
    }

    private void initMockData() {
        Playlist p1 = new Playlist("Lagu Coding", "Teman begadang");
        p1.addSong(new Song("Lo-Fi Beats", "Chill Cow", "3:20"));
        p1.addSong(new Song("Focus Flow", "Brain.fm", "5:00"));
        playlists.add(p1);

        Playlist p2 = new Playlist("Gym Hype", "Biar kuat");
        p2.addSong(new Song("Stronger", "Kanye West", "4:10"));
        playlists.add(p2);
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        SwingUtilities.invokeLater(() -> new MusicPlayer().setVisible(true));
    }


    // ==========================================
    // 2. MODELS (DATA ENTITIES)
    // ==========================================

    static class Song {
        private String title, artist, duration;

        public Song(String title, String artist, String duration) {
            this.title = title;
            this.artist = artist;
            this.duration = duration;
        }
        public String getTitle() { return title; }
        public String getArtist() { return artist; }
        public String getDuration() { return duration; }
    }

    static class Playlist {
        private String name, description;
        private List<Song> songs = new ArrayList<>();

        public Playlist(String name, String description) {
            this.name = name;
            this.description = description;
        }

        public void addSong(Song s) { songs.add(s); }
        public String getName() { return name; }
        public String getDescription() { return description; }
        public List<Song> getSongs() { return songs; }
        public int getSongCount() { return songs.size(); }
    }


    // ==========================================
    // 3. VIEWS (TAMPILAN / SCENES)
    // ==========================================

    // --- VIEW 1: Library ---
    static class LibraryView extends JPanel {
        private MusicPlayer controller;
        private JPanel gridPanel;

        public LibraryView(MusicPlayer controller) {
            this.controller = controller;
            setLayout(new BorderLayout());
            setBackground(new Color(18, 18, 18));

            JLabel title = new JLabel("Daftar Playlist");
            title.setFont(new Font("SansSerif", Font.BOLD, 24));
            title.setForeground(Color.WHITE);
            title.setBorder(new EmptyBorder(20,20,20,20));
            add(title, BorderLayout.NORTH);

            gridPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 20));
            gridPanel.setBackground(new Color(18, 18, 18));
            add(gridPanel, BorderLayout.CENTER);
        }

        public void refreshData(List<Playlist> playlists) {
            gridPanel.removeAll();
            for (Playlist p : playlists) {
                JPanel card = createCard(p);
                gridPanel.add(card);
            }
            gridPanel.revalidate();
            gridPanel.repaint();
        }

        private JPanel createCard(Playlist p) {
            JPanel card = new JPanel();
            card.setPreferredSize(new Dimension(160, 200));
            card.setBackground(new Color(40, 40, 40));
            card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
            card.setBorder(new EmptyBorder(10,10,10,10));

            JLabel lblName = new JLabel(p.getName());
            lblName.setForeground(Color.WHITE);
            lblName.setFont(new Font("SansSerif", Font.BOLD, 14));

            JLabel lblCount = new JLabel(p.getSongCount() + " Lagu");
            lblCount.setForeground(Color.GRAY);

            JPanel cover = new JPanel();
            cover.setBackground(Color.GRAY);
            cover.setMaximumSize(new Dimension(140, 100));

            card.add(cover);
            card.add(Box.createVerticalStrut(10));
            card.add(lblName);
            card.add(lblCount);

            card.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseClicked(java.awt.event.MouseEvent evt) {
                    controller.showDetail(p);
                }
            });
            return card;
        }
    }

    // --- VIEW 2: Detail Playlist ---
    static class DetailView extends JPanel {
        private MusicPlayer controller;
        private JLabel titleLabel, descLabel;
        private DefaultTableModel tableModel;
        private JTable table;

        public DetailView(MusicPlayer controller) {
            this.controller = controller;
            setLayout(new BorderLayout());
            setBackground(new Color(18, 18, 18));

            JPanel header = new JPanel();
            header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
            header.setBackground(new Color(40, 40, 40));
            header.setBorder(new EmptyBorder(20, 20, 20, 20));

            titleLabel = new JLabel("Title");
            titleLabel.setFont(new Font("SansSerif", Font.BOLD, 30));
            titleLabel.setForeground(Color.WHITE);

            descLabel = new JLabel("Desc");
            descLabel.setForeground(Color.LIGHT_GRAY);

            JButton btnBack = new JButton("Kembali");
            btnBack.addActionListener(e -> controller.showLibrary());

            header.add(btnBack);
            header.add(Box.createVerticalStrut(10));
            header.add(titleLabel);
            header.add(descLabel);
            add(header, BorderLayout.NORTH);

            tableModel = new DefaultTableModel(new String[]{"#", "Judul", "Artis", "Durasi"}, 0);
            table = new JTable(tableModel);
            table.setRowHeight(30);
            table.setBackground(new Color(18,18,18));
            table.setForeground(Color.WHITE);

            table.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseClicked(java.awt.event.MouseEvent evt) {
                    int row = table.getSelectedRow();
                    if (row != -1) {
                        String title = (String) tableModel.getValueAt(row, 1);
                        String artist = (String) tableModel.getValueAt(row, 2);
                        controller.showPlayer(new Song(title, artist, "00:00"));
                    }
                }
            });

            add(new JScrollPane(table), BorderLayout.CENTER);
        }

        public void setPlaylist(Playlist p) {
            titleLabel.setText(p.getName());
            descLabel.setText(p.getDescription());
            tableModel.setRowCount(0);
            int i = 1;
            for (Song s : p.getSongs()) {
                tableModel.addRow(new Object[]{i++, s.getTitle(), s.getArtist(), s.getDuration()});
            }
        }
    }

    // --- VIEW 3: Create Playlist ---
    static class CreateView extends JPanel {
        private MusicPlayer controller;
        private JTextField nameField;
        private JTextArea descArea;

        public CreateView(MusicPlayer controller) {
            this.controller = controller;
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setBackground(new Color(18, 18, 18));
            setBorder(new EmptyBorder(50, 100, 50, 100));

            JLabel title = new JLabel("Buat Playlist Baru");
            title.setForeground(Color.WHITE);
            title.setFont(new Font("SansSerif", Font.BOLD, 20));
            title.setAlignmentX(CENTER_ALIGNMENT);

            nameField = new JTextField();
            nameField.setMaximumSize(new Dimension(400, 30));

            descArea = new JTextArea(5, 20);
            descArea.setMaximumSize(new Dimension(400, 100));

            JButton btnSave = new JButton("Simpan");
            btnSave.setAlignmentX(CENTER_ALIGNMENT);
            btnSave.addActionListener(e -> saveAction());

            JButton btnCancel = new JButton("Batal");
            btnCancel.setAlignmentX(CENTER_ALIGNMENT);
            btnCancel.addActionListener(e -> controller.showLibrary());

            add(title);
            add(Box.createVerticalStrut(30));
            add(new JLabel("Nama:") {{ setForeground(Color.WHITE); }});
            add(nameField);
            add(Box.createVerticalStrut(10));
            add(new JLabel("Deskripsi:") {{ setForeground(Color.WHITE); }});
            add(new JScrollPane(descArea) {{ setMaximumSize(new Dimension(400, 100)); }});
            add(Box.createVerticalStrut(20));
            add(btnSave);
            add(Box.createVerticalStrut(10));
            add(btnCancel);
        }

        private void saveAction() {
            String name = nameField.getText();
            if (!name.isEmpty()) {
                Playlist newP = new Playlist(name, descArea.getText());
                controller.addPlaylist(newP);
                nameField.setText("");
                descArea.setText("");
                JOptionPane.showMessageDialog(this, "Berhasil dibuat!");
            }
        }
    }

    // --- VIEW 4: Player ---
    static class PlayerView extends JPanel {
        private MusicPlayer controller;
        private JLabel lblTitle, lblArtist;

        public PlayerView(MusicPlayer controller) {
            this.controller = controller;
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setBackground(new Color(10, 10, 10));
            setBorder(new EmptyBorder(50, 50, 50, 50));

            lblTitle = new JLabel("Title");
            lblTitle.setFont(new Font("SansSerif", Font.BOLD, 24));
            lblTitle.setForeground(Color.WHITE);
            lblTitle.setAlignmentX(CENTER_ALIGNMENT);

            lblArtist = new JLabel("Artist");
            lblArtist.setForeground(Color.GRAY);
            lblArtist.setAlignmentX(CENTER_ALIGNMENT);

            JButton btnMinimize = new JButton("Minimize");
            btnMinimize.setAlignmentX(CENTER_ALIGNMENT);
            btnMinimize.addActionListener(e -> controller.showLibrary());

            JPanel art = new JPanel();
            art.setBackground(Color.DARK_GRAY);
            art.setMaximumSize(new Dimension(200, 200));
            art.setPreferredSize(new Dimension(200, 200));
            art.setAlignmentX(CENTER_ALIGNMENT);

            add(art);
            add(Box.createVerticalStrut(20));
            add(lblTitle);
            add(lblArtist);
            add(Box.createVerticalStrut(30));
            add(btnMinimize);
        }

        public void setSong(Song s) {
            lblTitle.setText(s.getTitle());
            lblArtist.setText(s.getArtist());
        }
    }

    // --- COMPONENT: Sidebar ---
    static class Sidebar extends JPanel {
        public Sidebar(MusicPlayer controller) {
            setBackground(Color.BLACK);
            setPreferredSize(new Dimension(200, 600));
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setBorder(new EmptyBorder(20, 10, 20, 10));

            JLabel brand = new JLabel("GUI");
            brand.setForeground(Color.WHITE);
            brand.setFont(new Font("SansSerif", Font.BOLD, 30));

            JButton btnLib = new JButton("Library");
            btnLib.addActionListener(e -> controller.showLibrary());

            JButton btnCreate = new JButton("Buat Playlist");
            btnCreate.addActionListener(e -> controller.showCreate());

            add(brand);
            add(Box.createVerticalStrut(40));
            add(btnLib);
            add(Box.createVerticalStrut(10));
            add(btnCreate);
        }
    }
}