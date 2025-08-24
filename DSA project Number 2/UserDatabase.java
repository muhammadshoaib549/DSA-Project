import java.io.*;

import java.util.HashMap;

import java.util.LinkedList; // Used for recent logins history (LinkedList).

import java.util.List;

import java.util.ArrayList; // For returning lists.


// Manages all user accounts, including signup, login, logout, and deletion.
// It uses a HashMap for quick user lookup, a LinkedList for recent logins, and a BST for sorted usernames.
public class UserDatabase {

    // HashMap to store all user accounts.
    // Key: Username (String), Value: User object. Provides O(1) average-case lookup.
    public HashMap<String, User> userDatabase = new HashMap<>();


    // NEW DSA FEATURE: LinkedList to store the usernames of recently logged-in users.
    // LinkedList is efficient for adding/removing from ends, suitable for a fixed-size history.
    private LinkedList<String> recentLogins;
    private static final int MAX_RECENT_LOGINS = 3; // UPDATED: Maximum number of recent logins to track set to 3.

    // NEW DSA FEATURE: Binary Search Tree (BST) to store all usernames in alphabetical order.
    // Allows for efficient retrieval of all usernames in a sorted manner.
    private BinarySearchTree usernameBST;

    // Constructor: Initializes data structures and loads existing data from files.
    public UserDatabase() {
        recentLogins = new LinkedList<>(); // Initialize the LinkedList for recent logins.
        usernameBST = new BinarySearchTree(); // Initialize the BST for sorted usernames.
        loadDataFromFile(); // Load user accounts from file and populate BST.

        Message.loadDataFromFile(userDatabase); // Load messages and link them to user objects.
    }

    // Loads user account data from the accounts file.
    // Populates the userDatabase HashMap and the usernameBST.

    public void loadDataFromFile() {
        // Here We Will Use the String Line For  the Retrieving of the Data Instead of the Serialization that's Why used the BufferedInput And Buffered outpu

        String line;
        userDatabase.clear(); // Clear existing users in memory before loading new data.
        usernameBST.clear();  // Clear the BST to rebuild it with fresh data.

        try (BufferedReader reader = new BufferedReader(new FileReader(Config.ACCOUNTS_FILE))) {
            String username = null, password = null, email = null;
            // Read file line by line, parsing user details.
            while ((line = reader.readLine()) != null) {

                if (line.startsWith("Username:")) {
                    // Holding the Begging Index of the User Naame
                    username = line.substring(9).trim();

                } else if (line.startsWith("Password:")) {
                    password = line.substring(9).trim();
                } else if (line.startsWith("Email:")) {
                    email = line.substring(6).trim();
                }
                // Once all three pieces of information for a user are read, create a User object.
                if (username != null && password != null && email != null) {
                    User user = new User(username, password, email);
                    userDatabase.put(username, user);   // Add user to the HashMap.
                    usernameBST.insert(username);       // Add username to the BST for sorted access.
                    // Reset variables for the next user.
                    username = null;
                    password = null;
                    email = null;
                }
            }
        }
        catch (FileNotFoundException e) {
            System.err.println("Accounts file not found. A new one will be created upon signup.");
        }
        catch (IOException e) {
            System.err.println("Error reading accounts file: " + e.getMessage());
        }
    }

    // Rewrites the entire accounts file based on the current in-memory userDatabase.
    // This is crucial after operations like user deletion.
    public void rewriteAccountsFile() {
        // 'false' in FileWriter means overwrite the file.
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(Config.ACCOUNTS_FILE, false))) {
            // Since Configure Class Holding the Names of the Data Base As the Static That's Why We Used it As the Class Name Pointing Accounts File

            for (User user : userDatabase.values()) { // Iterate through all users in the HashMap.
                writer.write("Username:" + user.getUsername());
                writer.newLine();
                writer.write("Password:" + user.getPassword());
                writer.newLine();
                writer.write("Email:" + user.getEmail());
                writer.newLine();
            }

        } catch (IOException e) {
            // this output stream is used to display error messages
            System.err.println("Error rewriting accounts file: " + e.getMessage());
        }
    }

    // Handles user signup (registration).
    public boolean signup(String username, String password, String email) {
        if (userDatabase.containsKey(username)) { // Check if username already exists.
            System.out.println("Username already taken.");
            return false;
        } else {
            User newUser = new User(username, password, email);
            userDatabase.put(username, newUser); // Add new user to HashMap.
            usernameBST.insert(username);       // Add new username to BST.
            addAccountsToFile(username, password, email); // Persist the new account to file.
            System.out.println("User registered successfully!");
            return true;
        }
    }

    // Appends a new user account to the accounts file.
    public void addAccountsToFile(String username, String password, String email) {
        // 'true' in FileWriter means append to the file.
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(Config.ACCOUNTS_FILE, true))) {
            writer.write("Username:" + username);
            writer.newLine();

            writer.write("Password:" + password);

            writer.newLine();

            writer.write("Email:" + email);

            writer.newLine();

        } catch (IOException e) {
            System.err.println("Error writing new account to file: " + e.getMessage());
        }
    }

    // Handles user login.
    public User login(String username, String password) {
        User user = userDatabase.get(username); // Retrieve user from HashMap.
        if (user != null && user.getPassword().equals(password)) {
            user.setOnline(true); // Set user status to online.

            // NEW DSA FEATURE (LinkedList): Add user to recent logins.
            if (recentLogins.contains(username)) {
                recentLogins.remove(username); // Remove if already present to move it to the end (most recent).
            }
            recentLogins.addLast(username); // Add to the end (most recent).
            // Maintain the fixed size for recent logins.
            if (recentLogins.size() > MAX_RECENT_LOGINS) {
                recentLogins.removeFirst(); // Remove the oldest login if the limit is exceeded.
            }

            System.out.println("Login successful!");
            return user;
        } else {
            System.out.println("Invalid username or password.");
            return null;
        }
    }

    // Logs out a user (sets their online status to false).
    public void logout(User user) {
        if (user != null) {
            user.setOnline(false);
            System.out.println(user.getUsername() + " has been logged out.");
        }
    }

    // Deletes a user account from the system.
    public boolean deleteUser(String username) {
        if (userDatabase.containsKey(username)) {
            userDatabase.remove(username); // Remove from in-memory HashMap.
            usernameBST.delete(username); // Remove from BST.

            rewriteAccountsFile(); // Rewrite accounts file to remove the deleted user.

            // Re-load and rewrite messages to ensure the deleted user's messages are also removed from file.
            Message.loadDataFromFile(userDatabase);
            Message.rewriteMessagesFile(userDatabase);

            // Also remove from recent logins if the deleted user was there.
            recentLogins.remove(username);

            System.out.println("Account '" + username + "' deleted successfully.");
            return true;
        } else {
            System.out.println("User '" + username + "' not found.");
            return false;
        }

    }

    // NEW DSA FEATURE (LinkedList): Returns a list of recently logged-in usernames.
    // Returns a new ArrayList to prevent external modification of the internal LinkedList.
    public List<String> getRecentLogins()
    {

        return new ArrayList<>(recentLogins);

    }

    // NEW DSA FEATURE (BST): Returns all usernames sorted alphabetically using the BST's inorder traversal.
    public List<String> getAllUsernamesSorted() {
        return usernameBST.inorderTraversal();
    }

    // Retrieves a User object by username from the database HashMap.
    public User getUser(String username) {
        return userDatabase.get(username);
    }

    // Returns the entire user database HashMap.
    public HashMap<String, User> getUserDatabase() {
        return userDatabase;
    }
}