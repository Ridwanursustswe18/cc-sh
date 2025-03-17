import java.io.*;

public class Main {
    public static void main(String[] args) {
        DirectoryManager directoryManager = new DirectoryManager();
        CommandTranslator commandTranslator = new CommandTranslator();
        ProcessRunner processRunner = new ProcessRunner(directoryManager);
        HistoryManager historyManager = new HistoryManager();
        ScriptProcessor scriptProcessor = new ScriptProcessor();

        boolean isWindows = System.getProperty("os.name").toLowerCase().contains("win");

        CommandExecutor commandExecutor;
        if (isWindows) {
            commandExecutor = new WindowsCommandExecutor(
                    commandTranslator, directoryManager, processRunner, scriptProcessor, historyManager);
        } else {
            commandExecutor = new UnixCommandExecutor(
                    commandTranslator, directoryManager, processRunner, scriptProcessor, historyManager);
        }
        scriptProcessor.setCommandExecutor(commandExecutor);
        setupSignalHandler(historyManager);
        runCommandLoop(commandExecutor);
    }

    private static void runCommandLoop(CommandExecutor commandExecutor) {
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

                commandExecutor.execute(command.trim());
            }
        } catch (IOException e) {
            System.err.println("Error reading input: " + e.getMessage());
        }
    }

    private static void setupSignalHandler(HistoryManager historyManager) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\nShutting down ccsh...");
        }));
    }
}