import java.io.*;

public class ProcessRunner {
    private DirectoryManager directoryManager;

    public ProcessRunner(DirectoryManager directoryManager) {
        this.directoryManager = directoryManager;
    }

    public void runProcess(String command, boolean isWindows) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder();
            if (isWindows) {
                processBuilder.command("cmd.exe", "/c", command);
            } else {
                processBuilder.command("/bin/sh", "-c", command);
            }
            processBuilder.directory(directoryManager.getCurrentDirectory());
            processBuilder.redirectErrorStream(true);

            Process process = processBuilder.start();

            Thread outputThread = new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(process.getInputStream()))) {
                    char[] buffer = new char[8192];
                    int bytesRead;
                    while ((bytesRead = reader.read(buffer)) != -1) {
                        System.out.print(new String(buffer, 0, bytesRead));
                    }
                } catch (IOException e) {
                    System.out.println("Error reading output: " + e.getMessage());
                }
            });
            outputThread.start();

            process.waitFor();
            outputThread.join();

        } catch (IOException | InterruptedException e) {
            System.out.println("Execution error: " + e.getMessage());
        }
    }
}