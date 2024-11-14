// Server.java
package application;

import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    public static void main(String[] args) {
        int port = 8080;
        if (isPortAvailable(port)) {
            System.out.println("Port " + port + " is available.");
            try (ServerSocket serverSocket = new ServerSocket(port)) {
                DatabaseConnection dbConnection = new DatabaseConnection();
                System.out.println("Server started on port " + port);
                while (true) {
                    Socket clientSocket = serverSocket.accept();
                    ServerHandler handler = new ServerHandler(clientSocket, dbConnection);
                    new Thread(handler).start();
                }
            } catch (Exception e) {
                System.out.println("failed to start server");
            }
        } else {
            System.out.println("Port " + port + " is already in use.");
        }
    }
    public static boolean isPortAvailable(int port) {
        try (ServerSocket _ = new ServerSocket(port)) {
            return true; // Port is available
        } catch (Exception e) {
            return false; // Port is in use
        }
    }
}
