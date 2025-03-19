import java.io.*;
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

    @Override
    protected void handleOutputRedirectionCommand(String command) {
        String[] commandParts = command.split(">", 2);
        String cmdToExecute = commandParts[0].trim();
        String outputPath = commandParts[1].trim();
        File outputFile = new File(directoryManager.getCurrentDirectory(), outputPath);
        try {
            ProcessBuilder processBuilder = new ProcessBuilder();
            processBuilder.command("cmd.exe", "/c",execute(cmdToExecute));
            processBuilder.directory(directoryManager.getCurrentDirectory());
            Process process = processBuilder.start();
            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append(System.lineSeparator());
                }
            }
            try (PrintWriter writer = new PrintWriter(outputFile)) {
                writer.write(output.toString());
            }
        } catch (IOException e) {
            System.err.println("Error with redirection: " + e.getMessage());
        }

    }
}