import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);

        if (args.length == 1) {
            System.out.print(args[0] + "> ");
            String command = scanner.nextLine();

            String os = System.getProperty("os.name").toLowerCase();
            Process process;

            if(os.contains("win")) {
                if(command.equals("ls")){
                    command = "dir";
                }
                process = Runtime.getRuntime().exec("cmd.exe /c " + command);
            } else {
                process = Runtime.getRuntime().exec(command);
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
            reader.close();
        } else {
            System.out.println("No command line arguments found.");
        }
        scanner.close();
    }
}
