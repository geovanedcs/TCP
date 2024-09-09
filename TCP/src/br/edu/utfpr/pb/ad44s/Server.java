package br.edu.utfpr.pb.ad44s.server;

import java.io.*;
import java.net.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class Server {
    private static final int PORT = 1234;
    private static CopyOnWriteArrayList<ClientHandler> clients = new CopyOnWriteArrayList<>();

    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(PORT);
            System.out.println("Servidor está aguardando conexões na porta " + PORT);

            // Accept incoming connections
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Novo cliente conectado: " + clientSocket);

                // Create a new client handler for the connected client
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                clients.add(clientHandler);
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Broadcast a message to all clients except the sender
    public static void broadcast(String message, ClientHandler sender) {
        for (ClientHandler client : clients) {
            if (client != sender) {
                client.sendMessage(message);
            }
        }
    }

    // Internal class to handle client connections
    private static class ClientHandler implements Runnable {
        private Socket clientSocket;
        private PrintWriter out;
        private BufferedReader in;
        private String Username; // Use Username consistently

        // Constructor
        public ClientHandler(Socket socket) {
            this.clientSocket = socket;

            try {
                // Create input and output streams for communication
                out = new PrintWriter(clientSocket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Run method to handle client communication
        @Override
        public void run() {
            try {
                // Get the username from the client
                Username = getUsername();
                System.out.println("Usuário " + Username + " conectado.");

                out.println("Bem vindo ao trabalho final de AD44s, " + Username + "!"); 
                out.println("Digite sua mensagem:");
                String inputLine;

                // Continue receiving messages from the client
                while ((inputLine = in.readLine()) != null) {
                    System.out.println("[" + Username + "]: " + inputLine); 

                    // Broadcast the message to all clients
                    broadcast("[" + Username + "]: " + inputLine, this);
                }

                // Remove the client handler from the list
                clients.remove(this);

                // Close the input and output streams and the client socket
                in.close();
                out.close();
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Get the username from the client
        private String getUsername() throws IOException {
            out.println("Digite seu apelido:");
            return in.readLine();
        }

        public void sendMessage(String message) {
            out.println(message);
            out.println("Digite sua mensagem:");
        }
    }
}