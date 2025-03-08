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

                if (command.equals("exit") ) {
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
        String translatedCommand = originalCommandName;
        String translatedArguments = arguments;

        if (isWindows) {
            if (UNIX_TO_WINDOWS_COMMANDS.containsKey(originalCommandName)) {
                translatedCommand = UNIX_TO_WINDOWS_COMMANDS.get(originalCommandName);
                if (originalCommandName.equals("ls")) {
                    translatedArguments = translateLsArguments(arguments);
                }
            } else {
                System.out.println("No such file or directory (os error 2)");
                return;
            }
            command = translatedCommand + (translatedArguments.isEmpty() ? "" : " " + translatedArguments);
        }

        Process process;
        try {
            if (isWindows) {
                process = Runtime.getRuntime().exec("cmd.exe /c " + command);
            } else {
                try {
                    process = Runtime.getRuntime().exec(new String[]{"/bin/sh", "-c", command});
                } catch (IOException e) {
                    System.out.println("No such file or directory (os error 2)");
                    return;
                }
            }
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