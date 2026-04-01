import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;
import java.util.Scanner;

public class NoteClient {
    
    private NoteInterface noteService;
    private Scanner scanner;
    
    public NoteClient(String serverIP) {
        scanner = new Scanner(System.in);
        
        try {
            // Get RMI registry from server
            Registry registry = LocateRegistry.getRegistry(serverIP, 1099);
            
            // Look up remote service
            noteService = (NoteInterface) registry.lookup("NoteService");
            
            System.out.println("Connected to RMI Note Server at " + serverIP);
            
        } catch (Exception e) {
            System.err.println("Client exception: " + e.toString());
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    private void displayMenu() {
        System.out.println("\n===== JAVA RMI NOTE APPLICATION =====");
        System.out.println("1. Create a new note");
        System.out.println("2. View a note");
        System.out.println("3. Update a note");
        System.out.println("4. Delete a note");
        System.out.println("5. List all notes");
        System.out.println("6. Exit");
        System.out.print("Choice: ");
    }
    
    private void createNote() {
        try {
            System.out.print("Enter title: ");
            String title = scanner.nextLine();
            
            System.out.print("Enter content (single line): ");
            String content = scanner.nextLine();
            
            int noteId = noteService.createNote(title, content);
            System.out.println("Note created successfully! ID: " + noteId);
            
        } catch (Exception e) {
            System.err.println("Error creating note: " + e.getMessage());
        }
    }
    
    private void viewNote() {
        try {
            System.out.print("Enter note ID: ");
            int noteId = Integer.parseInt(scanner.nextLine());
            
            String note = noteService.getNote(noteId);
            System.out.println("\n" + note);
            
        } catch (Exception e) {
            System.err.println("Error viewing note: " + e.getMessage());
        }
    }
    
    private void updateNote() {
        try {
            System.out.print("Enter note ID: ");
            int noteId = Integer.parseInt(scanner.nextLine());
            
            System.out.print("Enter new content: ");
            String content = scanner.nextLine();
            
            boolean success = noteService.updateNote(noteId, content);
            if (success) {
                System.out.println("Note updated successfully!");
            } else {
                System.out.println("Note not found!");
            }
            
        } catch (Exception e) {
            System.err.println("Error updating note: " + e.getMessage());
        }
    }
    
    private void deleteNote() {
        try {
            System.out.print("Enter note ID: ");
            int noteId = Integer.parseInt(scanner.nextLine());
            
            boolean success = noteService.deleteNote(noteId);
            if (success) {
                System.out.println("Note deleted successfully!");
            } else {
                System.out.println("Note not found!");
            }
            
        } catch (Exception e) {
            System.err.println("Error deleting note: " + e.getMessage());
        }
    }
    
    private void listNotes() {
        try {
            List<String> notes = noteService.listNotes();
            System.out.println("\n----- Available Notes -----");
            for (String note : notes) {
                System.out.println(note);
            }
            
        } catch (Exception e) {
            System.err.println("Error listing notes: " + e.getMessage());
        }
    }
    
    public void run() {
        while (true) {
            displayMenu();
            
            try {
                int choice = Integer.parseInt(scanner.nextLine());
                
                switch (choice) {
                    case 1:
                        createNote();
                        break;
                    case 2:
                        viewNote();
                        break;
                    case 3:
                        updateNote();
                        break;
                    case 4:
                        deleteNote();
                        break;
                    case 5:
                        listNotes();
                        break;
                    case 6:
                        System.out.println("Goodbye!");
                        scanner.close();
                        System.exit(0);
                        break;
                    default:
                        System.out.println("Invalid choice. Please try again.");
                }
                
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }
    }
    
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java NoteClient <server_ip>");
            System.exit(1);
        }
        
        NoteClient client = new NoteClient(args[0]);
        client.run();
    }
}