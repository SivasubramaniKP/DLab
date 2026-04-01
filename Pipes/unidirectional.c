// unidirectional_pipe.c
// Parent writes notes, child reads and displays

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <sys/types.h>
#include <time.h>

#define MAX_NOTE_LENGTH 256
#define BUFFER_SIZE 1024

typedef struct {
    char content[MAX_NOTE_LENGTH];
    time_t timestamp;
} Note;

int main() {
    int pipefd[2];
    pid_t pid;
    
    printf("\n=== UNIDIRECTIONAL PIPE ===\n");
    printf("Parent → Child (One Way)\n");
    printf("==========================\n\n");
    
    // Create pipe
    if (pipe(pipefd) == -1) {
        perror("pipe");
        exit(EXIT_FAILURE);
    }
    
    pid = fork();
    
    if (pid == -1) {
        perror("fork");
        exit(EXIT_FAILURE);
    }
    
    if (pid == 0) { 
        // CHILD PROCESS - READER ONLY
        close(pipefd[1]); // Close write end
        
        Note note;
        int note_count = 0;
        
        printf("[CHILD] Note Viewer: Waiting for notes...\n");
        printf("----------------------------------------\n");
        
        // Read notes from pipe
        while (read(pipefd[0], &note, sizeof(Note)) > 0) {
            note_count++;
            printf("[CHILD] Received Note #%d\n", note_count);
            printf("         Time: %s", ctime(&note.timestamp));
            printf("         Content: %s\n", note.content);
            printf("----------------------------------------\n");
        }
        
        printf("[CHILD] Total notes received: %d\n", note_count);
        printf("[CHILD] Viewer closed.\n");
        
        close(pipefd[0]);
        exit(0);
        
    } else { 
        // PARENT PROCESS - WRITER ONLY
        close(pipefd[0]); // Close read end
        
        Note notes[] = {
            {"Team meeting at 3 PM", time(NULL)},
            {"Submit project report", time(NULL)},
            {"Call client for feedback", time(NULL)},
            {"Review pull requests", time(NULL)},
            {"Plan weekend hackathon", time(NULL)}
        };
        int num_notes = sizeof(notes) / sizeof(Note);
        
        printf("[PARENT] Note Creator: Sending %d notes...\n\n", num_notes);
        
        // Write notes to pipe
        for (int i = 0; i < num_notes; i++) {
            notes[i].timestamp = time(NULL);
            printf("[PARENT] Sending note %d: \"%s\"\n", i+1, notes[i].content);
            write(pipefd[1], &notes[i], sizeof(Note));
            sleep(1); // Delay between sends
        }
        
        printf("\n[PARENT] All notes sent. Closing.\n");
        close(pipefd[1]);
        wait(NULL); // Wait for child
    }
    
    return 0;
}