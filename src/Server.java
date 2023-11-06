import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class Server {
    private static final int PORT = 12345;
    private static ConcurrentHashMap<String, PrintWriter> connectedClients = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Servidor MSP en ejecución en el puerto " + PORT);

            while (true) {
                Socket clienteSocket = serverSocket.accept();
                new ClienteHandler(clienteSocket).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static class ClienteHandler extends Thread {
        private Socket clienteSocket;
        private PrintWriter out;
        private String usuario;

        public ClienteHandler(Socket socket) {
            this.clienteSocket = socket;
        }

        public void run() {
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(clienteSocket.getInputStream()));
                out = new PrintWriter(clienteSocket.getOutputStream(), true);

                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    if (inputLine.startsWith("CONNECT ")) {
                        handleConnectCommand(inputLine);
                    } else if (inputLine.startsWith("DISCONNECT ")) {
                        handleDisconnectCommand(inputLine);
                    } else if (inputLine.equals("LIST")) {
                        sendConnectedClientsList();
                    } else if (inputLine.startsWith("SEND ")) {
                        handleMessageCommand(inputLine);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                disconnectClient();
            }
        }

        private void handleConnectCommand(String inputLine) {
            String[] partes = inputLine.split(" ");
            if (partes.length == 2) {
                usuario = partes[1];
                connectedClients.put(usuario, out);
                System.out.println(usuario + " se ha conectado.");
                out.println("Conectado con éxito.");
            } else {
                out.println("Comando CONNECT incorrecto.");
            }
        }

        private void handleDisconnectCommand(String inputLine) {
            String[] partes = inputLine.split(" ");
            if (partes.length == 2 && partes[1].equals(usuario)) {
                disconnectClient();
            } else {
                out.println("Comando DISCONNECT incorrecto.");
            }
        }

        private void handleMessageCommand(String inputLine) {
            if (usuario == null) {
                out.println("Debes conectarte antes de enviar mensajes.");
                return;
            }

            String[] partes = inputLine.split("@");
            if (partes.length == 2) {
                String recipient = partes[1];
                String message = inputLine.substring(inputLine.indexOf('#') + 1, inputLine.indexOf('@'));
                if (connectedClients.containsKey(recipient)) {
                    PrintWriter recipientWriter = connectedClients.get(recipient);
                    recipientWriter.println(usuario + ": " + message);
                } else {
                    out.println("El usuario " + recipient + " no está conectado.");
                }
            } else {
                out.println("Comando SEND incorrecto.");
            }
        }

        private void sendConnectedClientsList() {
            out.println("Usuarios conectados: " + String.join(", ", connectedClients.keySet()));
        }

        private void disconnectClient() {
            if (usuario != null) {
                connectedClients.remove(usuario);
                System.out.println(usuario + " se ha desconectado.");
            }
            try {
                clienteSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
