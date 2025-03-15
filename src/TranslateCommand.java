public class TranslateCommand {
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
