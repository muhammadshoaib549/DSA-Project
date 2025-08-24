// Array List is A generic type of Data Structure
// List is Also a dATA Structure But here We Can Store Only 1 type of A Data S......
import java.util.ArrayList;
// for the operation on user names

import java.util.List;
// For Storing the Usernames


public class BinarySearchTree {

    private BSTNode root;

    public BinarySearchTree()

    {

        this.root = null;
    }

    // Inserts a username into the BST
    public void insert(String username)

    {
        root = insertRecursive(root, username);
    }
// Using the Recursive Function For the Addition of the Names In the BST
    private BSTNode insertRecursive(BSTNode current, String username) {
        if (current == null) {
            return new BSTNode(username);
        }


        // Compare usernames to decide whether to go left or right
        int cmp = username.compareTo(current.username);

        if (cmp < 0) {

            current.left = insertRecursive(current.left, username);
        }

        else if (cmp > 0) {
            current.right = insertRecursive(current.right, username);
        }
        // If cmp == 0, username already exists, do nothing (BSTs typically don't store duplicates)
        return current;
    }

    // Deletes a username from the BST
    public void delete(String username) {
        root = deleteRecursive(root, username);
    }

    private BSTNode deleteRecursive(BSTNode current, String username) {
        if (current == null) {
            return null; // Username not found
        }

        int cmp = username.compareTo(current.username);
        // if the User Val is Less then the 0 then it must be On the Left hand Side of the Tree
        if (cmp < 0) {
            current.left = deleteRecursive(current.left, username);
        } else if (cmp > 0) {
            current.right = deleteRecursive(current.right, username);
        }

        else {
            // Node to be deleted found
            if (current.left == null && current.right == null) {
                return null; // Case 1: No children
            } else if (current.left == null) {
                return current.right; // Case 2: Only right child
            } else if (current.right == null) {
                return current.left; // Case 2: Only left child
            } else {
                // Case 3: Two children - find the smallest in the right subtree (inorder successor)
                String smallestValue = findSmallestValue(current.right);
                current.username = smallestValue; // Replace current node's value with inorder successor
                current.right = deleteRecursive(current.right, smallestValue); // Delete the inorder successor
            }
        }
        return current;
    }

    // Helper to find the smallest value in a subtree (used for deletion with two children)
    private String findSmallestValue(BSTNode root) {
        return root.left == null ? root.username : findSmallestValue(root.left);
    }

    // Performs an inorder traversal to get all usernames in alphabetical order
    public List<String> inorderTraversal() {
        List<String> sortedUsernames = new ArrayList<>();
        inorderRecursive(root, sortedUsernames);
        return sortedUsernames;
    }

    // Inorder Printing
    /*
    left
    Node
    Right

     */
    private void inorderRecursive(BSTNode node, List<String> sortedUsernames) {
        if (node != null) {
            inorderRecursive(node.left, sortedUsernames);
            sortedUsernames.add(node.username);
            inorderRecursive(node.right, sortedUsernames);
        }
    }

    // Clears the BST (resets it to an empty tree)
    public void clear() {
        root = null;
    }
}