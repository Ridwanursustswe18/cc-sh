import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Main {
    private static final Map<String, String> UNIX_TO_WINDOWS_COMMANDS = new HashMap<>();

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

                    break;
                }

                executeCommand(command, isWindows);
            }
        }
    }

    private static void executeCommand(String command, boolean isWindows) {
        try {
            String[] commandParts = command.split("\\s+", 2);
            String commandName = commandParts[0];
            if (isWindows) {
                if (UNIX_TO_WINDOWS_COMMANDS.containsKey(commandName)) {
                    String windowsCommand = UNIX_TO_WINDOWS_COMMANDS.get(commandName);
                    if (commandParts.length > 1) {
                        command = windowsCommand + " " + commandParts[1];
                    } else {
                        command = windowsCommand;
                    }
                } else {
                    System.out.println("No such file or directory (os error 2)");
                    return;
                }
            }

            Process process;
            if (isWindows) {
                process = Runtime.getRuntime().exec("cmd.exe /c " + command);
            } else {
                process = Runtime.getRuntime().exec(new String[]{"/bin/sh", "-c", command});
            }

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                }
            }

        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}