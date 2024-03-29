//package sendfile;

import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;

public class TransferToClient {
	String downloadDirectory = "./download";

	public static void main(String[] args) throws IOException {
		TransferToClient sfc = new TransferToClient();
		sfc.readData();
	}

	private void readData() {
		try {
			// Connect to server
			String host = "127.0.0.1";
			int port = 12345;
			SocketAddress sad = new InetSocketAddress(host, port);
			SocketChannel sc = SocketChannel.open();
			sc.connect(sad);
			sc.configureBlocking(true);

			// Request the file list
			ByteBuffer buffer = ByteBuffer.allocate(1024);
			int bytesRead = sc.read(buffer);
			System.out.println(bytesRead);
			if (bytesRead > 0) {
				buffer.flip();
				String fileList = StandardCharsets.UTF_8.decode(buffer).toString();
				System.out.println("Files on the server:\n" + fileList);
			}

			// Ask for the file name
			System.out.print("Enter the name of the file you want to download: ");
			BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));
			String selectedFileName = userInput.readLine();
			// Convert the string to bytes using UTF-8 encoding
			byte[] selectedFileBytes = selectedFileName.getBytes(StandardCharsets.UTF_8);
			// Write the byte data to the SocketChannel
			buffer = ByteBuffer.wrap(selectedFileBytes);
			sc.write(buffer);
			// Ensure all data is sent
			sc.finishConnect();

			// Read input file length
			buffer = ByteBuffer.allocate(16);
			sc.read(buffer);
			buffer.flip();
			long fsize = buffer.getLong();

			// Receive the file content
			File outputFile = new File(downloadDirectory, selectedFileName);
			FileChannel fileChannel = new FileOutputStream(outputFile).getChannel();
			long start = System.currentTimeMillis();
			long totalReceived = 0;
			while (totalReceived < fsize) {
				long received = fileChannel.transferFrom(sc, totalReceived, fsize - totalReceived);
				if (received <= 0) {
					break;
				}
				totalReceived += received;
			}

			// Display time taken
			System.out.println("total bytes received--" + totalReceived + " and time taken in MS--"
					+ (System.currentTimeMillis() - start));
			fileChannel.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
