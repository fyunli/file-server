package li.fyun.client;

import com.google.gson.Gson;
import li.fyun.Base64;
import li.fyun.ResponseMessage;
import li.fyun.StreamUtils;
import li.fyun.UploadHeader;

import java.io.*;
import java.net.Socket;

/**
 * Created by fyunli on 15/12/23.
 */
public class UploadClient {

    static final int BUFFER_SIZE = 1024;
    static final Gson GSON = new Gson();

    String server;
    int port;

    public UploadClient(String server, int port) {
        this.server = server;
        this.port = port;
    }

    public void upload(File file) throws IOException {
        if (file == null && file.isDirectory() && file.isDirectory()) {
            throw new IOException("invalid file.");
        }

        Socket socket = new Socket(server, port);
        OutputStream outStream = socket.getOutputStream();

        String socketHeader = getSocketHeader(file);

        outStream.write(socketHeader.getBytes());

        PushbackInputStream pushbackInputStream = new PushbackInputStream(socket.getInputStream());

        String responseJson = StreamUtils.readLine(pushbackInputStream);
        System.out.println(responseJson);
        ResponseMessage responseMessage = GSON.fromJson(responseJson, ResponseMessage.class);

        Long breakPosition = responseMessage.getPosition();
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

    protected String getSocketHeader(File file) throws UnsupportedEncodingException {
        UploadHeader uploadHeader = new UploadHeader();
        uploadHeader.setContentLength(file.length());
        uploadHeader.setFilename(file.getName());
        uploadHeader.setSourceId(Base64.encode(file.getAbsolutePath().getBytes("utf-8")));
        return GSON.toJson(uploadHeader) + "\r\n";
    }

}
