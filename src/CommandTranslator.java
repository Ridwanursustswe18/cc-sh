import java.util.HashMap;
import java.util.Map;

public class CommandTranslator {
    private final Map<String, String> unixToWindowsCommands;

    public CommandTranslator() {
        unixToWindowsCommands = new HashMap<>();
        unixToWindowsCommands.put("ls", "dir");
        unixToWindowsCommands.put("pwd", "cd");
        unixToWindowsCommands.put("cat", "type");
        unixToWindowsCommands.put("grep", "findstr");
        unixToWindowsCommands.put("mkdir", "md");
    }

    public String translateCommand(String command, boolean toWindows) {
        if (!toWindows) {
            return command;
        }

        String[] cmdParts = command.split("\\s+", 2);
        String originalCmd = cmdParts[0];
        String args = cmdParts.length > 1 ? cmdParts[1] : "";

        if (originalCmd.equals("wc")) {
            return translateWcCommand(args);
        } else if (unixToWindowsCommands.containsKey(originalCmd)) {
            String translatedCmd = unixToWindowsCommands.get(originalCmd);
            if (originalCmd.equals("ls")) {
                args = translateLsArguments(args);
            }
            return translatedCmd + (args.isEmpty() ? "" : " " + args);
        }

        return command;
    }

    public static String translateLsArguments(String unixArgs) {
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
    public static String translateWcCommand(String args) {
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

}