import java.io.*;
import java.net.*;

public class cliente {
    public static void main(String[] args) {
        final String serverAddress = "localhost";
        final int serverPort = 12345;

        // Solicita al usuario el nombre de usuario al que desea conectarse.
        String username = getUserInput("Introduce tu nombre de usuario: ");

        try (Socket socket = new Socket(serverAddress, serverPort);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            // Proceso de conexión
            out.println("CONNECT " + username);
            String response = in.readLine();
            System.out.println(response);

            // Ciclo de interacción
            while (true) {
                String userInput = getUserInput("> ");
                out.println(userInput);

                if (userInput.equals("DISCONNECT " + username)) {
                    break;
                }

                response = in.readLine();
                if (response != null) {
                    System.out.println("Servidor: " + response);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String getUserInput(String prompt) {
        System.out.print(prompt);
        BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
        try {
            return stdIn.readLine();
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }
}