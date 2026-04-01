import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface NoteInterface extends Remote {
    int createNote(String title, String content) throws RemoteException;
    String getNote(int noteId) throws RemoteException;
    boolean updateNote(int noteId, String content) throws RemoteException;
    boolean deleteNote(int noteId) throws RemoteException;
    List<String> listNotes() throws RemoteException;
}