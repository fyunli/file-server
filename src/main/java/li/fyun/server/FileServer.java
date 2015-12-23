package li.fyun.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by fyunli on 15/12/23.
 */
public class FileServer {

    static Logger logger = LoggerFactory.getLogger(FileServer.class);

    private ExecutorService executorService;//线程池
    private int port;//监听端口
    private boolean quit = false;//退出
    private ServerSocket server;

    public FileServer(int port) {
        this.port = port;
        executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 50);
        logger.debug("file server started.");
    }

    public void quit() {
        this.quit = true;
        try {
            server.close();
            logger.debug("file server stoped.");
        } catch (IOException e) {
        }
    }

    public void start() throws Exception {
        server = new ServerSocket(port);
        while (!quit) {
            try {
                Socket socket = server.accept();
                executorService.execute(new SocketTask(socket));
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    public static void main(String[] args) {
        FileServer s = new FileServer(7878);
        try {
            s.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
