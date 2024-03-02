import java.io.*;
import java.net.*;

public class FileClient {
    String serverAddress = "127.0.0.1"; // Server IP address
    int serverPort = 12345; // Server port
    String downloadDirectory = "./download"; // Directory to save downloaded files

    public FileClient() {
        try {
            Socket socket = new Socket(serverAddress, serverPort);

            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            // Request the file list
            System.out.println("File list:");
            String fileName;
            while (!(fileName = in.readLine()).equals("END_OF_LIST")) {
                System.out.println(fileName);
            }

            // Request a specific file
            System.out.print("Enter the name of the file you want to download: ");
            BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));
            String selectedFileName = userInput.readLine();
            out.println(selectedFileName);

            // Receive the file content
            InputStream fileInputStream = socket.getInputStream();
            File downloadedFile = new File(downloadDirectory, selectedFileName);
            long start = System.currentTimeMillis();
            FileOutputStream fileOutputStream = new FileOutputStream(downloadedFile);
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                fileOutputStream.write(buffer, 0, bytesRead);
            }
            fileOutputStream.close();
            // Display time taken
			System.out.println(" time taken in MS--" + (System.currentTimeMillis() - start));
            System.out.println("File downloaded successfully.");

            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    public static void main(String[] args) {
        new FileClient();
    }
}
