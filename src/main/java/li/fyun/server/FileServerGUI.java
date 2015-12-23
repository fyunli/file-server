package li.fyun.server;

import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

/**
 * Created by fyunli on 15/12/23.
 */
public class FileServerGUI extends JFrame {//服务端程序入口
    private FileServer s = new FileServer(7878);
    private JLabel label;

    public FileServerGUI(String title) {
        super(title);

        this.addWindowListener(new WindowListener() {
            public void windowOpened(WindowEvent e) {
                new Thread(new Runnable() {
                    public void run() {
                        try {
                            s.start();
                        } catch (Exception e) {
                            //e.printStackTrace();
                        }
                    }
                }).start();
            }

            public void windowIconified(WindowEvent e) {
            }

            public void windowDeiconified(WindowEvent e) {
            }

            public void windowDeactivated(WindowEvent e) {
            }

            public void windowClosing(WindowEvent e) {
                s.quit();
                System.exit(0);
            }

            public void windowClosed(WindowEvent e) {
            }

            public void windowActivated(WindowEvent e) {
            }
        });

        label = new JLabel("服务器已启动", SwingConstants.CENTER);
        add(label, BorderLayout.CENTER);
    }

    public static void main(String[] args) {
        FileServerGUI window = new FileServerGUI("文件上传服务端");
        window.setSize(255, 96);
        window.setVisible(true);
    }

}