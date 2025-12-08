package engine;

import java.util.ArrayList;
import java.util.List;

public class MessageLog {
    private static final int MAX_MESSAGES = 5;
    private List<String> messages;

    public MessageLog() {
        messages = new ArrayList<>();
    }

    public void add(String message) {
        messages.add(message);
        if (messages.size() > MAX_MESSAGES) {
            messages.remove(0);
        }
    }

    public List<String> getMessages() {
        return messages;
    }

    public void clear() {
        messages.clear();
    }
}