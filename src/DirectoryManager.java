import java.io.File;

public class DirectoryManager {
    private File currentDirectory;

    public DirectoryManager() {
        currentDirectory = new File(System.getProperty("user.dir"));
    }

    public File getCurrentDirectory() {
        return currentDirectory;
    }

    public void changeDirectory(String path) {
        if (path.isEmpty()) {
            path = System.getProperty("user.home");
        }

        File newDir;
        if (path.equals("..")) {
            newDir = currentDirectory.getParentFile();
        } else {
            newDir = new File(currentDirectory, path);
        }

        if (newDir != null && newDir.exists() && newDir.isDirectory()) {
            currentDirectory = newDir;
            System.out.println("Changed directory to: " + currentDirectory.getAbsolutePath());
        } else {
            System.out.println("cd: " + path + ": No such directory");
        }
    }
}
