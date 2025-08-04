package ohhell.network;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/// Echo server to use for testing
class EchoServer implements Runnable {

    private final int port;
    private Thread thread;

    public EchoServer(int port) {
        this.port = port;
    }

    public void run() {

        try (
                ServerSocket serverSocket =
                        new ServerSocket(port);
                Socket clientSocket = serverSocket.accept();
                OutputStream out = clientSocket.getOutputStream();
                InputStream in = clientSocket.getInputStream();
        ) {
            byte[] buffer = new byte[1024];
            int numBytes;
            while ((numBytes = in.read(buffer)) > 0) {
                out.write(buffer, 0, numBytes);
                // 0 means shutdown server
                if (buffer[numBytes - 1] == 0 || buffer[numBytes - 2] == 0) {
                    break;
                }
            }
        } catch (IOException e) {
            System.err.println("Exception caught when trying to listen on port "
                    + port + " or listening for a connection");
            System.err.println(e.getMessage());
        }
    }

    public void start() {
        thread = new Thread(this);

        thread.start();
    }

    public boolean wait(int timeout) {
        try {
            thread.join(timeout);
            return true;
        } catch (InterruptedException e) {
            return false;
        }
    }
}
