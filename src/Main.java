import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
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

                if (command.equals("exit") || command.equals("quit")) {
                    System.out.println("Exiting...");
                    break;
                }

                executeCommand(command, isWindows);
            }
        }
    }

    private static void executeCommand(String command, boolean isWindows) {
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
}