package engine;

import java.util.ArrayList;
import java.util.List;

public class MessageLog {

    private static class Msg {
        String text;
        boolean fixed;

        Msg(String text, boolean fixed) {
            this.text = text;
            this.fixed = fixed;
        }
    }

    private List<Msg> messages = new ArrayList<>();
    private static final int MAX_TEMP_MESSAGES = 5;

    public void add(String msg) {
        messages.add(new Msg(msg, false));
        enforceLimit();
    }

    public void addFixed(String msg) {
        messages.add(new Msg(msg, true));
    }

    private void enforceLimit() {
        long tempCount = messages.stream().filter(m -> !m.fixed).count();

        while (tempCount > MAX_TEMP_MESSAGES) {
            for (int i = 0; i < messages.size(); i++) {
                if (!messages.get(i).fixed) {
                    messages.remove(i);
                    tempCount--;
                    break;
                }
            }
        }
    }

    public List<String> getMessages() {
        return messages.stream().map(m -> m.text).toList();
    }

    public void clearNonFixed() {
        messages.removeIf(m -> !m.fixed);
    }
}