import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class NoteServer extends UnicastRemoteObject implements NoteInterface {
    
    private ConcurrentHashMap<Integer, Note> notes;
    private int nextId;
    
    // Constructor
    public NoteServer() throws RemoteException {
        super();
        notes = new ConcurrentHashMap<>();
        nextId = 1;
        
        // Add some sample notes
        addSampleNotes();
        
        System.out.println("Note server initialized with " + notes.size() + " sample notes");
    }
    
    private void addSampleNotes() {
        Note note1 = new Note(nextId++, "Welcome", "Welcome to Java RMI Note App!\nYou can create, view, update and delete notes.", "admin");
        Note note2 = new Note(nextId++, "RMI Notes", "Java RMI allows remote method invocation between JVMs.\nKey concepts: Stubs, Skeletons, Registry", "admin");
        
        notes.put(note1.getId(), note1);
        notes.put(note2.getId(), note2);
    }
    
    @Override
    public int createNote(String title, String content) throws RemoteException {
        System.out.println("Creating note: " + title);
        
        Note note = new Note(nextId++, title, content, "current_user");
        notes.put(note.getId(), note);
        
        System.out.println("Note created with ID: " + note.getId());
        return note.getId();
    }
    
    @Override
    public String getNote(int noteId) throws RemoteException {
        System.out.println("Fetching note ID: " + noteId);
        
        Note note = notes.get(noteId);
        if (note != null) {
            return note.getFullDetails();
        } else {
            return "Note not found with ID: " + noteId;
        }
    }
    
    @Override
    public boolean updateNote(int noteId, String content) throws RemoteException {
        System.out.println("Updating note ID: " + noteId);
        
        Note note = notes.get(noteId);
        if (note != null) {
            note.setContent(content);
            note.setTimestamp(new java.util.Date());
            System.out.println("Note updated successfully");
            return true;
        }
        
        System.out.println("Note not found for update");
        return false;
    }
    
    @Override
    public boolean deleteNote(int noteId) throws RemoteException {
        System.out.println("Deleting note ID: " + noteId);
        
        Note removed = notes.remove(noteId);
        if (removed != null) {
            System.out.println("Note deleted successfully");
            return true;
        }
        
        System.out.println("Note not found for deletion");
        return false;
    }
    
    @Override
    public List<String> listNotes() throws RemoteException {
        System.out.println("Listing all notes");
        
        List<String> noteList = new ArrayList<>();
        for (Note note : notes.values()) {
            noteList.add(note.toString());
        }
        
        if (noteList.isEmpty()) {
            noteList.add("No notes available");
        }
        
        return noteList;
    }
    
    // Main method to start server
    public static void main(String[] args) {
        try {
            // Create RMI registry on port 1099
            Registry registry = LocateRegistry.createRegistry(1099);
            
            // Create server instance
            NoteServer server = new NoteServer();
            
            // Bind server to registry
            registry.rebind("NoteService", server);
            
            System.out.println("==========================================");
            System.out.println("Java RMI Note Server is running...");
            System.out.println("Registry bound to port: 1099");
            System.out.println("Service name: NoteService");
            System.out.println("==========================================");
            System.out.println("Server IP: " + java.net.InetAddress.getLocalHost().getHostAddress());
            System.out.println("Waiting for client connections...");
            
        } catch (Exception e) {
            System.err.println("Server exception: " + e.toString());
            e.printStackTrace();
        }
    }
}