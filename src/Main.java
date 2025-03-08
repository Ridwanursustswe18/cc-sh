import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Main {
    private static final Map<String, String> UNIX_TO_WINDOWS_COMMANDS = new HashMap<>();
    private static File currentDirectory = new File(System.getProperty("user.dir"));

    static {
        UNIX_TO_WINDOWS_COMMANDS.put("ls", "dir");
        UNIX_TO_WINDOWS_COMMANDS.put("pwd", "cd");
        UNIX_TO_WINDOWS_COMMANDS.put("cat", "type");
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java Main <prompt-name>");
            return;
        }

        String promptName = args[0];
        boolean isWindows = System.getProperty("os.name").toLowerCase().contains("win");

        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                System.out.print(promptName + "> ");
                String command = scanner.nextLine().trim();

                if (command.isEmpty()) {
                    continue;
                }

                if (command.equals("exit")) {
                    System.out.println("Exiting...");
                    break;
                }

                executeCommand(command, isWindows);
            }
        }
    }

    private static void executeCommand(String command, boolean isWindows) {
        String[] commandParts = command.split("\\s+", 2);
        String originalCommandName = commandParts[0];
        String arguments = commandParts.length > 1 ? commandParts[1] : "";
        if (originalCommandName.equals("cd")) {
            handleCdCommand(arguments);
            return;
        }

        String translatedCommand = originalCommandName;
        String translatedArguments = arguments;

        if (isWindows) {
            if (UNIX_TO_WINDOWS_COMMANDS.containsKey(originalCommandName)) {
                translatedCommand = UNIX_TO_WINDOWS_COMMANDS.get(originalCommandName);
                // Translate arguments for specific commands
                if (originalCommandName.equals("ls")) {
                    translatedArguments = translateLsArguments(arguments);
                }
                // Add other command argument translations here as needed
            } else {
                System.out.println("No such file or directory (os error 2)");
                return;
            }
            command = translatedCommand + (translatedArguments.isEmpty() ? "" : " " + translatedArguments);
        }

        Process process;
        try {
            ProcessBuilder processBuilder = new ProcessBuilder();
            if (isWindows) {
                processBuilder.command("cmd.exe", "/c", command);
            } else {
                processBuilder.command("/bin/sh", "-c", command);
            }
            processBuilder.directory(currentDirectory); // Set the working directory
            process = processBuilder.start();

            try {
                process.waitFor();
            } catch (InterruptedException e) {
                System.out.println("Command execution interrupted.");
                return;
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                }
            }

        } catch (IOException e) {
            System.out.println("No such file or directory (os error 2)");
        }
    }

    private static void handleCdCommand(String path) {
        if (path.isEmpty()) {
            path = System.getProperty("user.home");
        }
        File newDirectory;
        if (path.equals("..")) {
            newDirectory = currentDirectory.getParentFile();
        } else {
            newDirectory = new File(currentDirectory, path);
        }

        if (newDirectory != null && newDirectory.exists() && newDirectory.isDirectory()) {
            currentDirectory = newDirectory;
            System.out.println("Changed directory to: " + currentDirectory.getAbsolutePath());
        } else {
            System.out.println("cd: " + path + ": No such file or directory");
        }
    }

    private static String translateLsArguments(String unixArgs) {
        StringBuilder windowsArgs = new StringBuilder();

        if (unixArgs.contains("-a") || unixArgs.contains("-A")) {
            windowsArgs.append("/A ");
        }
        if (unixArgs.contains("-l")) {
            windowsArgs.append("/Q ");
        }
        if (unixArgs.contains("-t")) {
            windowsArgs.append("/O-D ");
        }
        if (unixArgs.contains("-r")) {
            windowsArgs.append("/O-N ");
        }
        if (unixArgs.contains("-R")) {
            windowsArgs.append("/S ");
        }
        if (unixArgs.contains("-S")) {
            windowsArgs.append("/O-S ");
        }
        if (unixArgs.contains("-d")) {
            windowsArgs.append("/A:D ");
        }

        if (windowsArgs.length() > 0) {
            windowsArgs.setLength(windowsArgs.length() - 1);
        }

        return windowsArgs.toString();
    }
}