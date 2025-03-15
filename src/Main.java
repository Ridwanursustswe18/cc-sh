import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Main {
    private static final Map<String, String> UNIX_TO_WINDOWS_COMMANDS = new HashMap<>();
    private static File currentDirectory = new File(System.getProperty("user.dir"));

    static {
        UNIX_TO_WINDOWS_COMMANDS.put("ls", "dir");
        UNIX_TO_WINDOWS_COMMANDS.put("pwd", "cd");
        UNIX_TO_WINDOWS_COMMANDS.put("cat", "type");
        UNIX_TO_WINDOWS_COMMANDS.put("grep", "findstr");
    }

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
                saveCommandToHistory(command);
                executeCommand(command.trim(), isWindows);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void saveCommandToHistory(String command) {
        try {
            String fileName = "ccsh_history.txt";
            File historyFile = new File(currentDirectory, fileName);
            if (!historyFile.exists()) {
                historyFile.createNewFile();
            }
            try (FileWriter fw = new FileWriter(historyFile, true);
                 BufferedWriter bw = new BufferedWriter(fw);
                 PrintWriter out = new PrintWriter(bw)) {
                out.println(command);
            }
        } catch (IOException e) {
            System.err.println("Error writing to history file: " + e.getMessage());
        }
    }
    private static void executeCommand(String command, boolean isWindows) {
        if (command.equals("history")) {
            if(!isWindows){
                executeShellCommand(command, false);
            }else {
                displayCommandHistory();
            }
            return;
        }
        if (command.startsWith("cd ")) {
            handleCdCommand(command.substring(2).trim());
            return;
        } else if (command.equals("cd")) {
            handleCdCommand("");
            return;
        }
        if (command.contains("|")) {
            handlePipedCommand(command, isWindows);
        } else {
            handleSingleCommand(command, isWindows);
        }
    }

    private static void handlePipedCommand(String command, boolean isWindows) {
        String[] pipeParts = command.split("\\|");
        List<String> translatedCommands = new ArrayList<>();

        for (String part : pipeParts) {
            String trimmedPart = part.trim();
            String[] cmdParts = trimmedPart.split("\\s+", 2);
            String originalCmd = cmdParts[0];
            String args = cmdParts.length > 1 ? cmdParts[1] : "";
            if (originalCmd.equals("wc")) {
                if (isWindows) {
                    translatedCommands.add(translateWcCommand(args));
                } else {
                    translatedCommands.add("wc " + args);
                }
            } else if (isWindows && UNIX_TO_WINDOWS_COMMANDS.containsKey(originalCmd)) {
                String translatedCmd = UNIX_TO_WINDOWS_COMMANDS.get(originalCmd);
                if (originalCmd.equals("ls")) {
                    args = translateLsArguments(args);
                }
                translatedCommands.add(translatedCmd + (args.isEmpty() ? "" : " " + args));
            } else {
                translatedCommands.add(trimmedPart);
            }
        }

        String translatedPipeline = String.join(" | ", translatedCommands);
        executeShellCommand(translatedPipeline, isWindows);
    }

    private static void handleSingleCommand(String command, boolean isWindows) {
        String[] cmdParts = command.split("\\s+", 2);
        String originalCmd = cmdParts[0];
        String args = cmdParts.length > 1 ? cmdParts[1] : "";
        if (originalCmd.equals("wc")) {
            if (isWindows) {
                command = translateWcCommand(args);
            } else {
                command = "wc " + args;
            }
        } else if (isWindows && UNIX_TO_WINDOWS_COMMANDS.containsKey(originalCmd)) {
            String translatedCmd = UNIX_TO_WINDOWS_COMMANDS.get(originalCmd);
            if (originalCmd.equals("ls")) {
                args = translateLsArguments(args);
            }
            command = translatedCmd + (args.isEmpty() ? "" : " " + args);
        }

        executeShellCommand(command, isWindows);
    }

    private static String translateWcCommand(String args) {
        if (args.contains("-l")) {
            return "find /c /v \"\"";
        } else if (args.contains("-c")) {
            return "cmd /c for %f in (.) do @echo %~zf";
        } else if (args.contains("-w")) {
            return "cmd /c for /f %f in ('type') do @echo %f | find /c /v \"\"";
        } else if (args.contains("-m")) {
            return "cmd /c for /f %f in ('type') do @echo %f | find /c /v \"\"";
        } else {
            return "find /c /v \"\"";
        }
    }

    private static void executeShellCommand(String command, boolean isWindows) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder();
            if (isWindows) {
                processBuilder.command("cmd.exe", "/c", command);
            } else {
                processBuilder.command("/bin/sh", "-c", command);
            }
            processBuilder.directory(currentDirectory);
            processBuilder.redirectErrorStream(true);

            Process process = processBuilder.start();

            Thread outputThread = new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(process.getInputStream()))) {
                    char[] buffer = new char[8192];
                    int bytesRead;
                    while ((bytesRead = reader.read(buffer)) != -1) {
                        System.out.print(new String(buffer, 0, bytesRead));
                    }
                } catch (IOException e) {
                    System.out.println("Error reading output: " + e.getMessage());
                }
            });
            outputThread.start();

            process.waitFor();
            outputThread.join();

        } catch (IOException | InterruptedException e) {
            System.out.println("Execution error: " + e.getMessage());
        }
    }

    private static void handleCdCommand(String path) {
        if (path.isEmpty()) {
            path = System.getProperty("user.home");
        }

        File newDir;
        if (path.equals("..")) {
            newDir = currentDirectory.getParentFile();
        } else {
            newDir = new File(currentDirectory, path);
        }

        if (newDir != null && newDir.exists() && newDir.isDirectory()) {
            currentDirectory = newDir;
            System.out.println("Changed directory to: " + currentDirectory.getAbsolutePath());
        } else {
            System.out.println("cd: " + path + ": No such directory");
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

        if (!windowsArgs.isEmpty()) {
            windowsArgs.setLength(windowsArgs.length() - 1);
        }

        return windowsArgs.toString();
    }
    private static class SignalHandler {
        public void setupSignalHandler() {
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("\nReceived interrupt signal (SIGINT) - shell will not exit.");

            }));
        }
    }
    private static void displayCommandHistory() {
        try {
            String fileName = "ccsh_history.txt";
            File historyFile = new File(currentDirectory, fileName);

            if (!historyFile.exists()) {
                System.out.println("No command history found.");
                return;
            }

            try (BufferedReader reader = new BufferedReader(new FileReader(historyFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.printf("%s\n", line);
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading history file: " + e.getMessage());
        }
    }
}