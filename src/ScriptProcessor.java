import java.io.*;

public class ScriptProcessor {
    private CommandExecutor commandExecutor;

    public void setCommandExecutor(CommandExecutor commandExecutor) {
        this.commandExecutor = commandExecutor;
    }

    public void processScript(File file) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }

                System.out.println("Executing: " + line);
                commandExecutor.execute(line);
            }
        } catch (IOException e) {
            System.err.println("Error processing bash script: " + e.getMessage());
        }
    }
}
