// Git Hub must be
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

// Basically For the Restriction of the gmail id which is the Built-in the Java
import java.util.regex.Pattern;

// The main application class for the chat application.
// Handles user interaction, menu navigation, and orchestrates calls to UserDatabase and Message classes.
public class ChatApp {

    // Static instance of Message to perform message-related operations.
    static Message messageObj = new Message(null, null, null);
    // Static variable to hold the currently logged-in user.
    static User loginUser;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);         // Scanner for user input.
        UserDatabase userDatabase = new UserDatabase(); // Manages user accounts and DSA features.

        // Main application loop. Continues until user chooses to exit.
        while (true) {
            // Display login/signup menu if no user is currently logged in.
            if (loginUser == null) {
                System.out.println("\n--- Welcome to WhatsApp! ---");
                System.out.println("How Can I Assist You Today?");
                System.out.println("1. Sign Up");
                System.out.println("2. Login");
                System.out.println("3. Exit");
                System.out.print("Please choose an option: ");

                int choice = getValidatedInput(scanner); // Get validated integer input from user.

                switch (choice) {
                    case 1: // Sign Up
                        System.out.print("Enter a new Username: ");
                        String username = scanner.nextLine();

                        // Check if username already exists.
                        if (userDatabase.getUserDatabase().containsKey(username)) {
                            System.out.println("Username '" + username + "' is already taken. Please choose a different one.");
                            break;
                        }

                        // Validate password strength.
                        String password;
                        while (true) {
                            System.out.print("Enter password (at least 4 characters): ");
                            password = scanner.nextLine();
                            if (password.length() < 4) {
                                System.out.println("Your password is too weak. Please enter a password with at least 4 characters.");
                            } else {
                                break;
                            }
                        }

                        // Validate email format.
                        String email;
                        while (true) {
                            System.out.print("Enter Email address: ");
                            email = scanner.nextLine();
                            if (!isValidEmail(email)) {
                                System.out.println("Invalid Email Address. Please enter a valid email (e.g., user@example.com).");
                            } else {
                                break;
                            }
                        }

                        userDatabase.signup(username, password, email); // Attempt to register the new user.
                        break;

                    case 2: // Login
                        System.out.print("Enter username: ");
                        String loginUsername = scanner.nextLine();
                        System.out.print("Enter password: ");
                        String loginPassword = scanner.nextLine();
                        loginUser = userDatabase.login(loginUsername, loginPassword); // Attempt to log in the user.
                        break;

                    case 3: // Exit
                        System.out.println("Exiting the application. Goodbye!");
                        scanner.close(); // Close the scanner before exiting.
                        return; // Terminate the program.

                    default:
                        System.out.println("Invalid option. Please try again.");
                }
            } else {
                // Display logged-in user menu.
                System.out.println("\n--- Logged-In Menu (" + loginUser.getUsername() + ") ---");
                System.out.println("1. Send Message");
                System.out.println("2. View Chats");
                System.out.println("3. View Messages with a User (Means Receivers Name");
                System.out.println("4. View Online Users");
                System.out.println("5. Search Messages (by keyword)");
                System.out.println("6. Delete Account");
                System.out.println("7. View Recent Logins");             // (LinkedList) that Will Traverse Over the Recent Users
                System.out.println("8. View Last N Messages (Reverse Order)"); // That Will Use Stack here First in First Out
                System.out.println("9. View All Users (Alphabetical)"); // (Binary Search Tree)
                System.out.println("10. Logout");
                System.out.println("11. Exit");
                System.out.print("Choose an option: ");

                int choice = getValidatedInput(scanner); // Get validated integer input.

                switch (choice) {
                    case 1: // Send Message
                        System.out.print("Enter receiver's username: ");
                        String receiverName = scanner.nextLine();

                        User receiverUser = userDatabase.getUser(receiverName); // Get recipient's User object.

                        if (receiverUser == null) {
                            System.out.println("User with username '" + receiverName + "' not found.");
                            break;
                        }

                        if (receiverUser.equals(loginUser)) {
                            System.out.println("You cannot send messages to yourself.");
                            break;
                        }

                        System.out.print("Enter your message: ");
                        String messageContent = scanner.nextLine();
                        messageObj.sendMessage(loginUser, receiverUser, messageContent); // Send the message.
                        break;

                    case 2: // View Chats
                        System.out.println("\n--- Your Chats ---");
                        ArrayList<String> chats = messageObj.getChats(loginUser); // Get list of chat partners.
                        if (chats.isEmpty()) {
                            System.out.println("No chats available. Send a message to start one!");
                        } else {
                            System.out.println("You have chats with:");
                            for (String chatUser : chats) {
                                // Display online status of chat partners.
                                System.out.println("- " + chatUser + " (" + (userDatabase.getUser(chatUser).isOnline() ? "Online" : "Offline") + ")");
                            }
                        }
                        break;

                    case 3: // View Messages with a User
                        System.out.print("Enter username to view messages: ");
                        String chatUsername = scanner.nextLine();
                        User chatUser = userDatabase.getUser(chatUsername); // Get the chat partner's User object.
                        if (chatUser == null) {
                            System.out.println("User with username '" + chatUsername + "' not found.");
                            break;
                        }
                        System.out.println("\n--- Chat with " + chatUsername + " ---");
                        ArrayList<Message> messages = messageObj.getMessages(loginUser, chatUser); // Get all messages, chronologically sorted.
                        if (messages.isEmpty()) {
                            System.out.println("No messages with " + chatUsername + ".");
                        } else {
                            for (Message message : messages) {
                                message.displayMessage(); // Display each message.
                            }
                        }
                        break;

                    case 4: // View Online Users
                        System.out.println("\n--- Online Users ---");
                        boolean foundOnlineUser = false;
                        // Iterate through all users to find who is online (excluding the current user).
                        for (User user : userDatabase.getUserDatabase().values()) {
                            if (user.isOnline() && !user.equals(loginUser)) {
                                System.out.println("- " + user.getUsername());
                                foundOnlineUser = true;
                            }
                        }
                        if (!foundOnlineUser) {
                            System.out.println("No other users are currently online.");
                        }
                        break;

                    case 5: // Search Messages (by keyword)
                        System.out.print("Enter keyword to search for: ");
                        String keyword = scanner.nextLine().toLowerCase(); // Convert keyword to lowercase for case-insensitive search.
                        System.out.println("\n--- Search Results for '" + keyword + "' ---");
                        boolean foundMessages = false;
                        // Iterate through all users and their messages to find matching content.
                        for (User user : userDatabase.getUserDatabase().values()) {
                            // Check sent messages.
                            for (String recipient : user.getSentMessages().keySet()) {
                                // Only show messages if the sender or receiver is the logged-in user.
                                if (user.equals(loginUser) || userDatabase.getUser(recipient).equals(loginUser)) {
                                    for (Message msg : user.getSentMessages().get(recipient)) {
                                        if (msg.getContent().toLowerCase().contains(keyword)) {
                                            System.out.print("[FROM YOU] "); // Indicate if message was sent by current user.
                                            msg.displayMessage();
                                            foundMessages = true;
                                        }
                                    }
                                }
                            }
                            // Check received messages.
                            for (String sender : user.getReceivedMessages().keySet()) {
                                if (user.equals(loginUser) || userDatabase.getUser(sender).equals(loginUser)) {
                                    for (Message msg : user.getReceivedMessages().get(sender)) {
                                        if (msg.getContent().toLowerCase().contains(keyword)) {
                                            System.out.print("[TO YOU] "); // Indicate if message was received by current user.
                                            msg.displayMessage();
                                            foundMessages = true;
                                        }
                                    }
                                }
                            }
                        }
                        if (!foundMessages) {
                            // Key Word is the Reciever Name Here
                            System.out.println("No messages found containing '" + keyword + "'.");

                        }
                        break;

                    case 6: // Delete Account
                        System.out.println("\n--- Delete Account ---");
                        System.out.println("Are you sure you want to delete your account? This action cannot be undone.");
                        System.out.print("Type your username ('" + loginUser.getUsername() + "') to confirm: ");
                        String confirmUsername = scanner.nextLine();

                        if (confirmUsername.equals(loginUser.getUsername())) {
                            userDatabase.deleteUser(loginUser.getUsername()); // Delete user account.
                            loginUser = null; // Log out the user after deletion.
                        } else {
                            System.out.println("Confirmation failed. Account not deleted.");
                        }
                        break;

                    case 7: // NEW FEATURE: View Recent Logins (Uses LinkedList in UserDatabase)
                        System.out.println("\n--- Recent Logins ---");
                        List<String> recentLogins = userDatabase.getRecentLogins();
                        if (recentLogins.isEmpty()) {
                            System.out.println("No recent logins to display.");
                        } else {
                            System.out.println("Last " + recentLogins.size() + " logins (most recent first):");
                            // Display in reverse order of login (most recent first).
                            for (int i = recentLogins.size() - 1; i >= 0; i--) {
                                System.out.println("- " + recentLogins.get(i));
                            }
                        }
                        break;

                    case 8: // NEW FEATURE: View Last N Messages (Reverse Order) (Uses Stack in Message)
                        System.out.print("Enter username to view messages: ");
                        String chatUserForStack = scanner.nextLine();
                        User targetChatUser = userDatabase.getUser(chatUserForStack);

                        if (targetChatUser == null) {
                            System.out.println("User with username '" + chatUserForStack + "' not found.");
                            break;
                        }

                        System.out.print("Enter number of last messages to view: ");
                        int numMessages = getValidatedInput(scanner);

                        System.out.println("\n--- Last " + numMessages + " messages with " + chatUserForStack + " (Newest First) ---");
                        // Call the new method that uses a Stack to get messages in reverse order.
                        ArrayList<Message> lastNMessages = messageObj.getMessagesReverseChronological(loginUser, targetChatUser, numMessages);

                        if (lastNMessages.isEmpty()) {
                            System.out.println("No messages to display or less than " + numMessages + " messages with " + chatUserForStack + ".");
                        } else {
                            for (Message message : lastNMessages) {
                                message.displayMessage();
                            }
                        }
                        break;

                    case 9: // NEW FEATURE: View All Users (Alphabetical) (Uses Binary Search Tree in UserDatabase)
                        System.out.println("\n--- All Registered Users (Alphabetical Order) ---");
                        List<String> allSortedUsers = userDatabase.getAllUsernamesSorted(); // Get usernames from BST.
                        if (allSortedUsers.isEmpty()) {
                            System.out.println("No registered users found.");
                        } else {
                            for (String usernameInOrder : allSortedUsers) {
                                User user = userDatabase.getUser(usernameInOrder); // Retrieve full user object for online status.
                                System.out.println("- " + user.getUsername() + " (" + (user.isOnline() ? "Online" : "Offline") + ")");
                            }
                        }
                        break;

                    case 10: // Logout
                        userDatabase.logout(loginUser); // Log out the current user.
                        loginUser = null; // Clear the logged-in user.
                        break;

                    case 11: // Exit
                        if (loginUser != null) {
                            userDatabase.logout(loginUser); // Log out before exiting if a user is logged in.
                        }
                        System.out.println("Exiting from the Chat App. Goodbye!");
                        scanner.close(); // Close the scanner.
                        return; // Terminate the program.

                    default:
                        System.out.println("Invalid option. Please try again.");
                }
            }
        }
    }

    // Helper method to get validated integer input from the user.
    private static int getValidatedInput(Scanner scanner) {
        while (true) {
            try {
                return Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.print("Invalid input. Please enter a number: ");
            }
        }
    }

    // Helper method to validate email format using a regular expression.
    private static boolean isValidEmail(String email) {
        // Simple regex for basic email validation.
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        Pattern pattern = Pattern.compile(emailRegex);
        return email != null && pattern.matcher(email).matches();
    }
}
