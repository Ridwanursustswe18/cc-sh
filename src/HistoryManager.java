import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class HistoryManager {
    private static final String HISTORY_FILE = System.getProperty("user.home") + File.separator + ".ccsh_history";
    private List<String> commandHistory;

    public HistoryManager() {
        commandHistory = new ArrayList<>();
        loadCommandHistory();
        Runtime.getRuntime().addShutdownHook(new Thread(this::saveCommandHistory));
    }

    public void addCommand(String command) {
        commandHistory.add(command);
    }

    public void displayHistory() {
        commandHistory.forEach(System.out::println);
    }

    private void loadCommandHistory() {
        File historyFile = new File(HISTORY_FILE);
        if (historyFile.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(historyFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    commandHistory.add(line);
                }
            } catch (IOException e) {
                System.err.println("Error loading history: " + e.getMessage());
            }
        }
    }

    private void saveCommandHistory() {
        try (PrintWriter out = new PrintWriter(HISTORY_FILE)) {
            commandHistory.forEach(out::println);
        } catch (IOException e) {
            System.err.println("Error saving history: " + e.getMessage());
        }
    }
}