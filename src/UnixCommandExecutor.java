public class UnixCommandExecutor extends BaseCommandExecutor {

    public UnixCommandExecutor(CommandTranslator translator, DirectoryManager directoryManager,
                               ProcessRunner processRunner, ScriptProcessor scriptProcessor,
                               HistoryManager historyManager) {
        super(translator, directoryManager, processRunner, scriptProcessor, historyManager);
    }

    @Override
    protected void handleHistoryCommand() {
        processRunner.runProcess("history", false);
    }

    @Override
    protected void handlePipedCommand(String command) {
        processRunner.runProcess(command, false);
    }

    @Override
    protected void handleSingleCommand(String command) {
        processRunner.runProcess(command, false);
    }

    @Override
    protected void handleOutputRedirectionCommand(String command) {
        processRunner.runProcess(command, false);
    }
}