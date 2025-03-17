import java.util.ArrayList;
import java.util.List;

public class WindowsCommandExecutor extends BaseCommandExecutor {

    public WindowsCommandExecutor(CommandTranslator translator, DirectoryManager directoryManager,
                                  ProcessRunner processRunner, ScriptProcessor scriptProcessor,
                                  HistoryManager historyManager) {
        super(translator, directoryManager, processRunner, scriptProcessor, historyManager);
    }

    @Override
    protected void handleHistoryCommand() {
        historyManager.displayHistory();
    }

    @Override
    protected void handlePipedCommand(String command) {
        String[] pipeParts = command.split("\\|");
        List<String> translatedCommands = new ArrayList<>();

        for (String part : pipeParts) {
            translatedCommands.add(translator.translateCommand(part.trim(), true));
        }

        String translatedPipeline = String.join(" | ", translatedCommands);
        processRunner.runProcess(translatedPipeline, true);
    }

    @Override
    protected void handleSingleCommand(String command) {
        String translatedCommand = translator.translateCommand(command, true);
        processRunner.runProcess(translatedCommand, true);
    }
}