import java.io.*;
// For the Setting of the Timing Format
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

/*
ArrayList<Message> validMessages → to temporarily store messages read from the file.

ArrayList<String> getChats(User currUser) → to collect usernames you've chatted with.

ArrayList<Message> getMessages(...) → returns list of all messages between two users.

ArrayList<Message> getMessagesReverseChronological(...) → stores reversed messages using Stack.
 */
import java.util.HashMap;
// Using the Linked List fOR fETCHING The Data From the FILE and Reading to the File
import java.util.LinkedList;
// This is for the Queue Of the Messages In the Order of the FIFO
import java.util.Queue;

import java.util.Stack; // Imported for the new "View Last N Messages (Reverse Order)" feature.

// The Message class represents a single chat message between two users.
// It handles message creation, saving to file, loading from file, and various display options.
public class Message {

    private User sender;             // The user who sent the message.
    private User receiver;           // The user who received the message.
    private String content;          // The actual text content of the message.
    private LocalDateTime timestamp; // The exact date and time the message was sent.

    // Constructor to create a new Message object.
    public Message(User sender, User receiver, String content) {
        this.sender = sender;
        this.receiver = receiver;
        this.content = content;
        this.timestamp = LocalDateTime.now(); // Automatically sets the current time upon creation.
    }

    // Saves a single message to the messages file. Appends to the end of the file.
    public void saveMessageToFile() {
        // Using try-with-resources ensures the writer is closed automatically.

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(Config.MESSAGES_FILE, true))) { // 'true' means append mode.
            writer.write("From: " + sender.getUsername());
            writer.newLine();
            writer.write("To: " + receiver.getUsername());
            writer.newLine();
            writer.write("Message: " + content);
            writer.newLine();
            writer.write("Timestamp: " + getFormattedDate() + " " + getFormattedTime());
            writer.newLine();
            writer.newLine(); // Add an empty line for readability between messages in the file.
        } catch (IOException e) {
            System.err.println("Error saving message to file: " + e.getMessage());
        }
    }

    // Loads all messages from the messages file into the appropriate user's message queues.
    // This method is static as it operates on the global set of users.
    public static void loadDataFromFile(HashMap<String, User> users) {
        ArrayList<Message> validMessages = new ArrayList<>(); // Temporarily holds messages read from file.

        try (BufferedReader reader = new BufferedReader(new FileReader(Config.MESSAGES_FILE))) {
            String line;
            String senderName = null, receiverName = null, messageContent = null, timestampStr = null;

            // Read the file line by line, parsing message details.
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("From: ")) {
                    senderName = line.substring(6).trim();
                } else if (line.startsWith("To: ")) {
                    receiverName = line.substring(4).trim();
                } else if (line.startsWith("Message: ")) {
                    messageContent = line.substring(9).trim();
                } else if (line.startsWith("Timestamp: ")) {
                    timestampStr = line.substring(11).trim();

                    // Once all parts of a message are read, create a Message object.
                    if (senderName != null && receiverName != null && messageContent != null && timestampStr != null) {
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm a");
                        LocalDateTime timestamp = LocalDateTime.parse(timestampStr, formatter);

                        User sender = users.get(senderName);    // Retrieve sender User object from map.
                        User receiver = users.get(receiverName); // Retrieve receiver User object from map.

                        // Only add message if both sender and receiver users exist in the system.
                        if (sender != null && receiver != null) {
                            Message message = new Message(sender, receiver, messageContent);
                            message.timestamp = timestamp; // Set the actual timestamp from the file.
                            validMessages.add(message);
                        }
                        // Reset temporary variables for the next message.
                        senderName = null;
                        receiverName = null;
                        messageContent = null;
                        timestampStr = null;
                    }
                }
            }

            // Clear existing messages for all users before repopulating to prevent duplicates on reload.
            for (User user : users.values()) {
                user.getSentMessages().clear();
                user.getReceivedMessages().clear();
            }

            // Distribute the loaded messages to the correct user's sent and received message queues.
            for (Message message : validMessages) {
                String sName = message.getSender().getUsername();
                String rName = message.getReceiver().getUsername();

                User senderUser = users.get(sName);
                User receiverUser = users.get(rName);

                if (senderUser != null && receiverUser != null) {
                    // Add message to sender's 'sent' queue for the recipient.
                    senderUser.getSentMessages().putIfAbsent(rName, new LinkedList<>());
                    senderUser.getSentMessages().get(rName).add(message);

                    // Add message to receiver's 'received' queue from the sender.
                    receiverUser.getReceivedMessages().putIfAbsent(sName, new LinkedList<>());
                    receiverUser.getReceivedMessages().get(sName).add(message);
                }
            }

        } catch (FileNotFoundException e) {
            System.out.println("Messages file not found. A new one will be created upon sending messages.");
        } catch (IOException e) {
            System.err.println("Error loading messages from file: " + e.getMessage());
        }
    }

    // Rewrites the entire messages file based on the current in-memory message queues.
    // This is useful after operations like user deletion, to ensure file consistency.
    public static void rewriteMessagesFile(HashMap<String, User> users) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(Config.MESSAGES_FILE, false))) { // 'false' means overwrite.
            ArrayList<Message> allMessagesToWrite = new ArrayList<>();
            // Using a HashMap to ensure each unique message is written only once, even if present in both sent/received queues.
            HashMap<String, Message> uniqueMessages = new HashMap<>();

            // Collect all messages from all users' sent message queues.
            for (User user : users.values()) {
                for (Queue<Message> messagesQueue : user.getSentMessages().values()) {
                    for (Message message : messagesQueue) {
                        // Create a unique key for the message based on its attributes.
                        String messageKey = message.getSender().getUsername() + message.getReceiver().getUsername() +
                                message.getContent() + message.getTimestamp().toString();
                        if (!uniqueMessages.containsKey(messageKey)) {
                            uniqueMessages.put(messageKey, message);
                            allMessagesToWrite.add(message);
                        }
                    }
                }
            }

            // Sort all messages chronologically by timestamp before writing to file.
            // This ensures the file maintains a consistent, ordered history.
            allMessagesToWrite.sort((m1, m2) -> m1.getTimestamp().compareTo(m2.getTimestamp()));

            // Write each unique, sorted message to the file.
            for (Message message : allMessagesToWrite) {
                writer.write("From: " + message.getSender().getUsername());
                writer.newLine();
                writer.write("To: " + message.getReceiver().getUsername());
                writer.newLine();
                writer.write("Message: " + message.getContent());
                writer.newLine();
                writer.write("Timestamp: " + message.getFormattedDate() + " " + message.getFormattedTime());
                writer.newLine();
                writer.newLine(); // Add an empty line for separation.

            }
        } catch (IOException e) {
            System.err.println("Error rewriting messages file: " + e.getMessage());
        }
    }

    // --- Getters for Message attributes ---
    public LocalDateTime getTimestamp()

    {
        return timestamp;
    }

    public User getSender() {
        return sender;
    }

    public User getReceiver() {
        return receiver;
    }

    public String getContent() {
        return content;
    }

    // Formats the message timestamp to show time (e.g., "hh:mm AM/PM").
    public String getFormattedTime() {
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm a");
        return timestamp.format(timeFormatter);
    }

    // Formats the message timestamp to show date (e.g., "dd-MM-yyyy").
    public String getFormattedDate() {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        return timestamp.format(dateFormatter);
    }

    // Displays the message content along with sender, receiver, and timestamp to the console.
    public void displayMessage() {
        System.out.println(sender.getUsername() + " to " + receiver.getUsername() + ": " + content);
        System.out.println("[" + getFormattedTime() + " | " + getFormattedDate() + "]");
        System.out.println();
    }

    // Sends a message from one user to another. Adds it to both sender's sent queue and receiver's received queue.
    public void sendMessage(User sender, User receiver, String content) {
        Message message = new Message(sender, receiver, content);

        // Add message to sender's sent message queue for the specific receiver.
        sender.getSentMessages().putIfAbsent(receiver.getUsername(), new LinkedList<>());
        sender.getSentMessages().get(receiver.getUsername()).add(message);

        // Add message to receiver's received message queue from the specific sender.
        receiver.getReceivedMessages().putIfAbsent(sender.getUsername(), new LinkedList<>());
        receiver.getReceivedMessages().get(sender.getUsername()).add(message);

        message.displayMessage();    // Display the message immediately after sending.
        message.saveMessageToFile(); // Persist the message to file.
    }

    // Retrieves a list of usernames that the current user has chatted with (either sent or received messages).
    public ArrayList<String> getChats(User currUser) {
        ArrayList<String> chats = new ArrayList<>();

        // Add all distinct recipients from sent messages.
        for (String receiverName : currUser.getSentMessages().keySet()) {
            if (!chats.contains(receiverName)) { // Avoid adding duplicates.
                chats.add(receiverName);
            }
        }

        // Add all distinct senders from received messages.
        for (String senderName : currUser.getReceivedMessages().keySet()) {
            if (!chats.contains(senderName)) { // Avoid adding duplicates.
                chats.add(senderName);
            }
        }
        return chats;
    }

    // Retrieves all messages between the current user and another specified user, sorted chronologically.
    public ArrayList<Message> getMessages(User currUser, User user2) {
        ArrayList<Message> messages = new ArrayList<>();

        // Add messages sent by currUser to user2.
        if (currUser.getSentMessages().containsKey(user2.getUsername())) {
            Queue<Message> sentQueue = currUser.getSentMessages().get(user2.getUsername());
            messages.addAll(sentQueue);
        }

        // Add messages received by currUser from user2.
        if (currUser.getReceivedMessages().containsKey(user2.getUsername())) {
            Queue<Message> receivedQueue = currUser.getReceivedMessages().get(user2.getUsername());
            messages.addAll(receivedQueue);
        }

        // Sort all collected messages by their timestamp to ensure chronological order.
        messages.sort((m1, m2) -> m1.getTimestamp().compareTo(m2.getTimestamp()));
        return messages;
    }

    // NEW DSA FEATURE: Retrieves the last 'count' messages between two users in reverse chronological order (newest first).
    // This demonstrates the use of a Stack (LIFO - Last-In, First-Out).
    public ArrayList<Message> getMessagesReverseChronological(User currUser, User user2, int count) {
        ArrayList<Message> allMessages = getMessages(currUser, user2); // First, get all messages in chronological order.
        Stack<Message> messageStack = new Stack<>(); // Initialize a Stack.

        // Push the most recent 'count' messages onto the stack.
        // We iterate from the end of the chronologically sorted list backwards.
        for (int i = allMessages.size() - 1; i >= 0 && count > 0; i--, count--) {
            messageStack.push(allMessages.get(i));
        }

        ArrayList<Message> reversedMessages = new ArrayList<>();
        // Pop messages from the stack. Due to LIFO, they will be in reverse chronological order (newest first).
        while (!messageStack.isEmpty()) {
            reversedMessages.add(messageStack.pop());
        }

        return reversedMessages;

    }
}