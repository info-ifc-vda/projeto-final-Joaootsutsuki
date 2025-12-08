package engine;

import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.NonBlockingReader;

public class Input {
    private static Terminal terminal;
    private static NonBlockingReader reader;

    static {
        try {
            terminal = TerminalBuilder.builder().system(true).build();
            terminal.enterRawMode();
            reader = terminal.reader();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static char getKey() {
        try {
            int c = reader.read();
            return (char) c;
        } catch (Exception e) {
            return ' ';
        }
    }

    public static void cleanup() {
        try {
            if (terminal != null) {
                terminal.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}