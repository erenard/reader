package manga.gui;

import java.awt.Container;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.SwingWorker;

import manga.OptionManager;
import manga.model.Album;
import manga.model.Page;

public class MainFrame extends JFrame implements KeyListener, ActionListener, WindowListener {

    private static final long serialVersionUID = 1L;
    private final Panel panel;
    private final JMenuBar menuBar;
    private final JMenu mangaMenu;
    private final JMenuItem openAlbum;
    private final JMenuItem closeAlbum;
    private final JMenuItem exit;
    private final JMenu jumpToMenu;
    private final JMenu languageMenu;
    private final JMenuItem frLanguage;
    private final JMenuItem enLanguage;
    public List<JMenuItem> menuItems;
    private String workingDirectory = "";
    private final Properties messages;

    private static final MainFrame instance = new MainFrame();
    public static MainFrame getInstance() {
        return instance;
    }
    
    private Album album;
    private Page page;

    private MainFrame() {
        //Chargement du fichier de langue
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("properties/messages.properties");
        messages = new Properties();
        try {
            messages.load(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //Menu Manga
        menuBar = new JMenuBar();
        mangaMenu = new JMenu();
        openAlbum = new JMenuItem();
        openAlbum.addActionListener(this);
        mangaMenu.add(openAlbum);
        closeAlbum = new JMenuItem();
        closeAlbum.addActionListener(this);
        mangaMenu.add(closeAlbum);
        exit = new JMenuItem();
        exit.addActionListener(this);
        mangaMenu.add(exit);
        menuBar.add(mangaMenu);
        //Menu Jump To
        jumpToMenu = new JMenu();
        menuItems = new ArrayList<>();
        menuBar.add(jumpToMenu);
        //Language
        languageMenu = new JMenu("Language");
        frLanguage = new JMenuItem("Fran�ais");
        frLanguage.addActionListener(this);
        languageMenu.add(frLanguage);
        enLanguage = new JMenuItem("English");
        enLanguage.addActionListener(this);
        languageMenu.add(enLanguage);
        menuBar.add(languageMenu);
        //Fenetre
        setSize(400, 400);
        Container container = getContentPane();
        panel = new Panel();
        container.add(panel);
        container.add(menuBar, "North");
        this.addKeyListener(this);
        this.addWindowListener(this);
        setLanguage(OptionManager.getInstance().getProperty(OptionManager.LANGUAGE));
        workingDirectory = loadWorkingDirectory();
    }

    private void setLanguage(String language) {
        mangaMenu.setText(messages.getProperty("Menu1." + language));
        openAlbum.setText(messages.getProperty("Menu1.1." + language));
        closeAlbum.setText(messages.getProperty("Menu1.2." + language));
        exit.setText(messages.getProperty("Menu1.3." + language));
        jumpToMenu.setText(messages.getProperty("Menu2." + language));
        setTitle(messages.getProperty("Title." + language));
        OptionManager.getInstance().setProperty(OptionManager.LANGUAGE, language);
    }

    private void openManga(Album album) {
        this.album = album;
        this.page = album.jumpTo(0);
        int pathLength = album.getCommonPath().length();
        List<Page> pages = album.getPages();
        for(Page page : pages) {
            JMenuItem menuItem = new JMenuItem(page.getAbsolutePath().substring(pathLength));
            menuItem.addActionListener(this);
            menuItems.add(menuItem);
            jumpToMenu.add(menuItem);
        }
    }

    private void closeManga() {
        album = null;
        page = null;
        jumpToMenu.removeAll();
        menuItems.clear();
        panel.setPage(null);
        refreshView();
    }

    private void refreshView() {
        panel.setPage(page);
        if (page != null) {
            setTitle(page.getCurrentFrame().getTitle());
        } else {
            setTitle(messages.getProperty("Title.EN"));
        }
        panel.repaint();
    }

    private String loadWorkingDirectory() {
        OptionManager optionManager = OptionManager.getInstance();
        return optionManager.getProperty(OptionManager.WORKING_DIRECTORY);
    }

    private void saveWorkingDirectory(String s) {
        OptionManager optionManager = OptionManager.getInstance();
        optionManager.setProperty(OptionManager.WORKING_DIRECTORY, s);
        optionManager.save();
    }

    private void closeWindow() {
        OptionManager.getInstance().save();
        System.exit(0);
    }

    //ActionListener

    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        if (source.equals(openAlbum)) {
            JFileChooser chooser = new JFileChooser(workingDirectory);
            chooser.setFileFilter(new ImageFileFilter());
            chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
            int result = chooser.showOpenDialog(null);
            if (result == JFileChooser.APPROVE_OPTION) {
                try {
                    closeManga();
                    File file = chooser.getSelectedFile();
                    saveWorkingDirectory(file.getAbsolutePath());
                    openManga(new Album(file));
                } catch (IOException ex) {
                    Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } else if (source.equals(closeAlbum)) {
            closeManga();
        } else if (source.equals(frLanguage)) {
            setLanguage("FR");
        } else if (source.equals(enLanguage)) {
            setLanguage("EN");
        } else if (source.equals(exit)) {
            closeWindow();
        } else {
            for (int i = 0; i < menuItems.size(); i++) {
                JMenuItem menuItem = menuItems.get(i);
                if (e.getSource().equals(menuItem)) {
                    page = album.jumpTo(i);
                    page.jumpTo(0);
                    break;
                }
            }
        }
        refreshView();
    }

    //KeyboardListener
    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
            page.next();
        } else if (e.getKeyCode() == KeyEvent.VK_LEFT) {
            page.previous();
        }
        refreshView();
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    //WindowListener

    @Override
    public void windowActivated(WindowEvent e) {
    }

    @Override
    public void windowClosed(WindowEvent e) {
    }

    @Override
    public void windowClosing(WindowEvent e) {
        closeWindow();
    }

    @Override
    public void windowDeactivated(WindowEvent e) {
    }

    @Override
    public void windowDeiconified(WindowEvent e) {
    }

    @Override
    public void windowIconified(WindowEvent e) {
    }

    @Override
    public void windowOpened(WindowEvent e) {
    }
}
