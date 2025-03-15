import java.io.*;
public class Main {


    public static void main(String[] args) {
        SignalHandler signalHandler = new SignalHandler();
        signalHandler.setupSignalHandler();
        boolean isWindows = System.getProperty("os.name").toLowerCase().contains("win");

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
            while (true) {
                System.out.print("ccsh> ");
                String command = reader.readLine();

                if (command == null || command.trim().isEmpty()) {
                    continue;
                }

                if (command.equals("exit") || command.equals("quit")) {
                    System.out.println("Exiting...");
                    break;
                }
                Command.saveCommandToHistory(command);
                Command.executeCommand(command.trim(), isWindows);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static class SignalHandler {
        public void setupSignalHandler() {
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("\nReceived interrupt signal (SIGINT) - shell will not exit.");

            }));
        }
    }

}