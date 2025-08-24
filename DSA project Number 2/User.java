
import java.util.HashMap;
import java.util.LinkedList; // Explicitly import LinkedList to highlight its use
import java.util.Queue;

public class User {
/*
Hash Map named SentMessages may Receiver name is a key And the Data is the Queue of  the Messages
 */
    /*
    Hashmap jis ka name ReceivedMessages ha Us may Key ha Sender name ha Aur Data Queue of the Messages Hain
     */
    private String name;
    private String password;
    private String email;
    private boolean isOnline;

    // Sent messages: HashMap where key is receiver's username, value is a Queue of Messages.
    // The Queue is implemented using LinkedList, demonstrating FIFO (First-In, First-Out) order for chat history.
    private HashMap<String, Queue<Message>> sentMessages;

    // Received messages: HashMap where key is sender's username, value is a Queue of Messages.
    // The Queue is implemented using LinkedList, demonstrating FIFO order for chat history.
    private HashMap<String, Queue<Message>> receivedMessages;

    public User(String name, String password, String email) {
        this.name = name;
        this.password = password;
        this.email = email;
        this.isOnline = false; // Initially offline
        this.sentMessages = new HashMap<>();
        this.receivedMessages = new HashMap<>();
    }

    public HashMap<String, Queue<Message>> getSentMessages() {

        return sentMessages;
    }

    public HashMap<String, Queue<Message>> getReceivedMessages()
    {
        return receivedMessages;
    }

    public String getUsername() {
        return name;
    }

    public void setUsername(String username) {
        this.name = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isOnline() {
        return isOnline;
    }

    public void setOnline(boolean online) {
        isOnline = online;
    }
}
