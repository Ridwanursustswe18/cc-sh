import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Command {
    private static final Map<String, String> UNIX_TO_WINDOWS_COMMANDS = new HashMap<>();
    static {
        UNIX_TO_WINDOWS_COMMANDS.put("ls", "dir");
        UNIX_TO_WINDOWS_COMMANDS.put("pwd", "cd");
        UNIX_TO_WINDOWS_COMMANDS.put("cat", "type");
        UNIX_TO_WINDOWS_COMMANDS.put("grep", "findstr");
        UNIX_TO_WINDOWS_COMMANDS.put("mkdir", "md");
        History.loadCommandHistory();
        Runtime.getRuntime().addShutdownHook(new Thread(History::saveCommandHistory));
    }
    private static File currentDirectory = new File(System.getProperty("user.dir"));
    public static void executeCommand(String command, boolean isWindows) {
        if (command.equals("history")) {
            if(!isWindows){
                executeShellCommand(command, false);
            }else {
                History.commandHistory.forEach(System.out::println);
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
        if(command.startsWith("bash ")){
            String filePath = currentDirectory.getAbsolutePath() + File.separator + command.substring(5).trim();
            File scriptFile = new File(filePath);
            if(scriptFile.exists() && scriptFile.isFile()) {
                processBashFile(scriptFile);
            } else {
                System.err.println("Error: Bash script file not found: " + filePath);
            }
            return;
        }
        if (command.contains("|")) {
            handlePipedCommand(command, isWindows);
        } else {
            handleSingleCommand(command, isWindows);
        }
    }

    public static void handlePipedCommand(String command, boolean isWindows) {
        String[] pipeParts = command.split("\\|");
        List<String> translatedCommands = new ArrayList<>();

        for (String part : pipeParts) {
            String trimmedPart = part.trim();
            String[] cmdParts = trimmedPart.split("\\s+", 2);
            String originalCmd = cmdParts[0];
            String args = cmdParts.length > 1 ? cmdParts[1] : "";
            if (originalCmd.equals("wc")) {
                if (isWindows) {
                    translatedCommands.add(TranslateCommand.translateWcCommand(args));
                } else {
                    translatedCommands.add("wc " + args);
                }
            } else if (isWindows && UNIX_TO_WINDOWS_COMMANDS.containsKey(originalCmd)) {
                String translatedCmd = UNIX_TO_WINDOWS_COMMANDS.get(originalCmd);
                if (originalCmd.equals("ls")) {
                    args = TranslateCommand.translateLsArguments(args);
                }
                translatedCommands.add(translatedCmd + (args.isEmpty() ? "" : " " + args));
            } else {
                translatedCommands.add(trimmedPart);
            }
        }

        String translatedPipeline = String.join(" | ", translatedCommands);
        executeShellCommand(translatedPipeline, isWindows);
    }

    public static void handleSingleCommand(String command, boolean isWindows) {
        String[] cmdParts = command.split("\\s+", 2);
        String originalCmd = cmdParts[0];
        String args = cmdParts.length > 1 ? cmdParts[1] : "";
        if (originalCmd.equals("wc")) {
            if (isWindows) {
                command = TranslateCommand.translateWcCommand(args);
            } else {
                command = "wc " + args;
            }
        } else if (isWindows && UNIX_TO_WINDOWS_COMMANDS.containsKey(originalCmd)) {
            String translatedCmd = UNIX_TO_WINDOWS_COMMANDS.get(originalCmd);
            if (originalCmd.equals("ls")) {
                args = TranslateCommand.translateLsArguments(args);
            }
            command = translatedCmd + (args.isEmpty() ? "" : " " + args);
        }

        executeShellCommand(command, isWindows);
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

    private static void processBashFile(File file) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }

                boolean isWindows = System.getProperty("os.name").toLowerCase().contains("win");
                if(line.contains("|")){
                    handlePipedCommand(line, isWindows);
                } else if (line.contains("cd")) {
                    String[] parts = line.split("\\s",2);
                    handleCdCommand(parts[1]);
                } else {
                    handleSingleCommand(line, isWindows);
                }
            }
        } catch (IOException e) {
            System.err.println("Error processing bash script: " + e.getMessage());
        }
    }}
