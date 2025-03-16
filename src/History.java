import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class History {
    private static final String HISTORY_FILE = System.getProperty("user.home") + File.separator + ".ccsh_history";
    public static List<String> commandHistory = new ArrayList<>();
    public static void loadCommandHistory() {
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

    public static void saveCommandHistory() {
        try (PrintWriter out = new PrintWriter(HISTORY_FILE)) {
            commandHistory.forEach(out::println);
        } catch (IOException e) {
            System.err.println("Error saving history: " + e.getMessage());
        }
    }

    public static void saveCommandToHistory(String command) {
        commandHistory.add(command);
    }
}
