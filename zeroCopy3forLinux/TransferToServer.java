//package sendfile;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

public class TransferToServer {
  ServerSocketChannel listener = null;
  String directoryPath = "./sendfile";

  public static void main(String[] args) {
    TransferToServer dns = new TransferToServer();
    dns.mySetup();
    dns.sendfile();
  }

  protected void mySetup() {
    InetSocketAddress listenAddr = new InetSocketAddress(12345);

    try {
      listener = ServerSocketChannel.open();
      ServerSocket ss = listener.socket();
      ss.setReuseAddress(true);
      ss.bind(listenAddr);
      System.out.println("Listening on port : " + listenAddr.toString());
    } catch (IOException e) {
      System.out.println("Failed to bind, is port : " + listenAddr.toString()
          + " already in use ? Error Msg : " + e.getMessage());
      e.printStackTrace();
    }

  }

  public void sendfile() {
    try {
      while (true) {
        // Establish the connection
        SocketChannel conn = listener.accept();
        System.out.println("Accepted : " + conn);
        conn.configureBlocking(true);

        // List files in the directory
        File directory = new File(directoryPath);
        File[] files = directory.listFiles();
        List<String> fileList;
        fileList = new ArrayList<>();
        for (File file : files) {
          if (file.isFile()) {
            fileList.add(file.getName());
          }
        }

        // Send the list of file names to the client
        String response = String.join("\n", fileList) + "\n";
        ByteBuffer buffer = ByteBuffer.wrap(response.getBytes());
        conn.write(buffer);

        // Ask for the file name
        // Set up a ByteBuffer to receive the data
        buffer = ByteBuffer.allocate(1024); // Adjust the buffer size as needed
        // Read data from the SocketChannel into the ByteBuffer
        int bytesRead = conn.read(buffer);
        String selectedFileName = "";
        if (bytesRead > 0) {
          buffer.flip(); // Switch to read mode
          selectedFileName = StandardCharsets.UTF_8.decode(buffer).toString();
        }
        File selectedFile = new File(directoryPath, selectedFileName);
        long fsize = selectedFile.length();
        System.out.println("Sending " + selectedFileName + " to client: total byte " + fsize);

        // Send file length
        buffer = ByteBuffer.allocate(16);
        buffer.putLong(fsize);
        buffer.flip();
        conn.write(buffer);

        // Send the selected file to the client
        FileChannel fc = new FileInputStream(selectedFile).getChannel();
        long start = System.currentTimeMillis();
        long totalSent = 0;
        while (totalSent < fsize) {
          long sent = fc.transferTo(totalSent, selectedFile.length() - totalSent, conn);
          totalSent += sent;
        }

        // Displayed time taken
        System.out.println("total bytes transferred--" + totalSent + " and time taken in MS--"
            + (System.currentTimeMillis() - start));
        fc.close();
      }

    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
