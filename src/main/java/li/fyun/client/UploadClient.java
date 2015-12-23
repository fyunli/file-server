package li.fyun.client;

import com.google.gson.Gson;
import li.fyun.StreamUtils;
import li.fyun.UploadHeader;
import sun.misc.BASE64Encoder;

import java.io.*;
import java.net.Socket;

/**
 * Created by fyunli on 15/12/23.
 */
public class UploadClient {

    static final String SERVER = "192.168.1.80";
    static final int PORT = 7878;
    static final int BUFFER_SIZE = 1024;
    static final Gson GSON = new Gson();

    public UploadClient() {

    }

    public void upload(File file) throws IOException {
        if (file == null && file.isDirectory() && file.isDirectory()) {
            throw new IOException("invalid file.");
        }

        Socket socket = new Socket(SERVER, PORT);
        OutputStream outStream = socket.getOutputStream();

        String socketHeader = getSocketHeader(file);

        outStream.write(socketHeader.getBytes());

        PushbackInputStream pushbackInputStream = new PushbackInputStream(socket.getInputStream());

        String response = StreamUtils.readLine(pushbackInputStream);
        System.out.println(response);

        Long breakPosition = getBreakPosition(response);
        uploadFile(outStream, file, breakPosition);

        outStream.close();
        pushbackInputStream.close();
        socket.close();
        System.out.println("upload successfully.");
    }

    private void uploadFile(OutputStream outStream, File file, Long breakPosition) throws IOException {
        RandomAccessFile fileOutStream = new RandomAccessFile(file, "r");
        fileOutStream.seek(Long.valueOf(breakPosition));
        byte[] buffer = new byte[BUFFER_SIZE];
        int len = -1;
        while ((len = fileOutStream.read(buffer)) != -1) {
            outStream.write(buffer, 0, len);
        }
        fileOutStream.close();
    }

    private long getBreakPosition(String response) throws IOException {
        long position = 0;
        if (response == null || "".equals(response)) {
            return position;
        }

        String[] items = response.split(";");
        if (items.length != 2) {
            return position;
        }

        try {
            position = Long.valueOf(items[1].substring(items[1].indexOf("=") + 1));
        } finally {
            return position;
        }
    }

    protected String getSocketHeader(File file) throws UnsupportedEncodingException {
        UploadHeader uploadHeader = new UploadHeader();
        uploadHeader.setContentLength(file.length());
        uploadHeader.setFilename(file.getName());
        uploadHeader.setSourceId(new BASE64Encoder().encode(file.getAbsolutePath().getBytes("utf-8")));
        return GSON.toJson(uploadHeader);
    }

}
