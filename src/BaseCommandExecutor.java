import java.io.File;

public abstract class BaseCommandExecutor implements CommandExecutor {
    protected CommandTranslator translator;
    protected DirectoryManager directoryManager;
    protected ProcessRunner processRunner;
    protected ScriptProcessor scriptProcessor;
    protected HistoryManager historyManager;

    public BaseCommandExecutor(CommandTranslator translator, DirectoryManager directoryManager,
                               ProcessRunner processRunner, ScriptProcessor scriptProcessor,
                               HistoryManager historyManager) {
        this.translator = translator;
        this.directoryManager = directoryManager;
        this.processRunner = processRunner;
        this.scriptProcessor = scriptProcessor;
        this.historyManager = historyManager;
    }


    @Override
    public String execute(String command) {
        historyManager.addCommand(command);

        if (command.equals("history")) {
            handleHistoryCommand();
            return command;
        }

        if (command.startsWith("cd ") || command.equals("cd")) {
            handleCdCommand(command);
            return command;
        }

        if (command.startsWith("bash ")) {
            handleBashCommand(command);
            return command;
        }

        if (command.contains("|")) {
            handlePipedCommand(command);
            return command;
        }
            handleSingleCommand(command);

        return command;
    }

    protected abstract void handleHistoryCommand();
    protected abstract void handlePipedCommand(String command);
    protected abstract void handleSingleCommand(String command);
    protected abstract void handleOutputRedirectionCommand(String command);
    protected void handleCdCommand(String command) {
        String path = command.equals("cd") ? "" : command.substring(3).trim();
        directoryManager.changeDirectory(path);
    }

    protected void handleBashCommand(String command) {
        String scriptName = command.substring(5).trim();
        File scriptFile = new File(directoryManager.getCurrentDirectory(), scriptName);
        if (scriptFile.exists() && scriptFile.isFile()) {
            scriptProcessor.processScript(scriptFile);
        } else {
            System.err.println("Error: Bash script file not found: " + scriptFile.getAbsolutePath());
        }
    }
}