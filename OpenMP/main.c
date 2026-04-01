#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <omp.h>

#define MAX_NOTES 100
#define MAX_TITLE_LEN 100
#define MAX_CONTENT_LEN 500
#define MAX_TAGS 5
#define MAX_TAG_LEN 20

typedef struct {
    int id;
    char title[MAX_TITLE_LEN];
    char content[MAX_CONTENT_LEN];
    char tags[MAX_TAGS][MAX_TAG_LEN];
    int num_tags;
    int is_archived;
} Note;

typedef struct {
    Note notes[MAX_NOTES];
    int count;
    int next_id;
} NoteApp;

// Initialize the application
void init_app(NoteApp *app) {
    app->count = 0;
    app->next_id = 1;
}

// Add a new note
int add_note(NoteApp *app, const char *title, const char *content) {
    if (app->count >= MAX_NOTES) {
        return -1; // No space
    }
    
    Note *note = &app->notes[app->count];
    note->id = app->next_id++;
    strncpy(note->title, title, MAX_TITLE_LEN - 1);
    strncpy(note->content, content, MAX_CONTENT_LEN - 1);
    note->num_tags = 0;
    note->is_archived = 0;
    
    app->count++;
    return note->id;
}

// Add tags to a note
void add_tags(Note *note, const char *tags[], int num_tags) {
    if (num_tags > MAX_TAGS) num_tags = MAX_TAGS;
    
    for (int i = 0; i < num_tags; i++) {
        if (note->num_tags < MAX_TAGS) {
            strncpy(note->tags[note->num_tags], tags[i], MAX_TAG_LEN - 1);
            note->num_tags++;
        }
    }
}

// Parallel search for notes containing keywords
void search_notes_parallel(NoteApp *app, const char *keyword, int *results, int *result_count) {
    *result_count = 0;
    int local_results[MAX_NOTES];
    int local_count = 0;
    
    #pragma omp parallel
    {
        int private_results[MAX_NOTES];
        int private_count = 0;
        
        #pragma omp for
        for (int i = 0; i < app->count; i++) {
            if (!app->notes[i].is_archived) {
                // Search in title and content
                if (strstr(app->notes[i].title, keyword) != NULL ||
                    strstr(app->notes[i].content, keyword) != NULL) {
                    private_results[private_count++] = app->notes[i].id;
                }
            }
        }
        
        #pragma omp critical
        {
            for (int i = 0; i < private_count; i++) {
                local_results[local_count++] = private_results[i];
            }
        }
    }
    
    // Copy results to output array
    for (int i = 0; i < local_count && i < MAX_NOTES; i++) {
        results[i] = local_results[i];
    }
    *result_count = local_count;
}

// Parallel search by tags
void search_by_tags_parallel(NoteApp *app, const char *tag, int *results, int *result_count) {
    *result_count = 0;
    int local_results[MAX_NOTES];
    int local_count = 0;
    
    #pragma omp parallel
    {
        int private_results[MAX_NOTES];
        int private_count = 0;
        
        #pragma omp for
        for (int i = 0; i < app->count; i++) {
            if (!app->notes[i].is_archived) {
                // Check if note has the tag
                for (int j = 0; j < app->notes[i].num_tags; j++) {
                    if (strcmp(app->notes[i].tags[j], tag) == 0) {
                        private_results[private_count++] = app->notes[i].id;
                        break;
                    }
                }
            }
        }
        
        #pragma omp critical
        {
            for (int i = 0; i < private_count; i++) {
                local_results[local_count++] = private_results[i];
            }
        }
    }
    
    for (int i = 0; i < local_count && i < MAX_NOTES; i++) {
        results[i] = local_results[i];
    }
    *result_count = local_count;
}

// Parallel bulk archive operation
void archive_notes_parallel(NoteApp *app, const int *note_ids, int num_ids) {
    #pragma omp parallel for
    for (int i = 0; i < num_ids; i++) {
        for (int j = 0; j < app->count; j++) {
            if (app->notes[j].id == note_ids[i]) {
                app->notes[j].is_archived = 1;
                break;
            }
        }
    }
}

// Parallel word count across all notes
void count_words_parallel(NoteApp *app, int *total_words) {
    *total_words = 0;
    
    #pragma omp parallel for reduction(+:total_words[0])
    for (int i = 0; i < app->count; i++) {
        if (!app->notes[i].is_archived) {
            int words = 1; // Count first word
            char *content = app->notes[i].content;
            
            for (int j = 0; content[j] != '\0'; j++) {
                if (content[j] == ' ' || content[j] == '\n' || content[j] == '\t') {
                    words++;
                }
            }
            *total_words += words;
        }
    }
}

// Display a single note
void display_note(Note *note) {
    printf("\n=== Note ID: %d ===\n", note->id);
    printf("Title: %s\n", note->title);
    printf("Content: %s\n", note->content);
    printf("Tags: ");
    for (int i = 0; i < note->num_tags; i++) {
        printf("%s ", note->tags[i]);
    }
    printf("\nStatus: %s\n", note->is_archived ? "Archived" : "Active");
    printf("===================\n");
}

// Display all notes
void display_all_notes(NoteApp *app, int show_archived) {
    printf("\n=== ALL NOTES ===\n");
    for (int i = 0; i < app->count; i++) {
        if (show_archived || !app->notes[i].is_archived) {
            display_note(&app->notes[i]);
        }
    }
}

int main() {
    NoteApp app;
    init_app(&app);
    
    // Add sample notes
    printf("Adding sample notes...\n");
    int id1 = add_note(&app, "Meeting Notes", "Discuss Q4 goals and project timeline. Important meeting at 2PM.");
    int id2 = add_note(&app, "Shopping List", "Milk, Eggs, Bread, Coffee, Vegetables");
    int id3 = add_note(&app, "Project Ideas", "Implement AI features using OpenMP for parallel processing");
    int id4 = add_note(&app, "OpenMP Tips", "Remember to use reduction clauses and critical sections");
    
    // Add tags
    const char *tags1[] = {"work", "meeting"};
    const char *tags2[] = {"personal", "shopping"};
    const char *tags3[] = {"work", "project", "ai"};
    const char *tags4[] = {"work", "programming", "openmp"};
    
    add_tags(&app.notes[0], tags1, 2);
    add_tags(&app.notes[1], tags2, 2);
    add_tags(&app.notes[2], tags3, 3);
    add_tags(&app.notes[3], tags4, 3);
    
    printf("Added 4 notes successfully!\n");
    
    // DEMO 1: Parallel Search by keyword
    printf("DEMO 1: Parallel Search by Keyword\n");
    
    int search_results[MAX_NOTES];
    int result_count;
    
    double start = omp_get_wtime();
    search_notes_parallel(&app, "project", search_results, &result_count);
    double end = omp_get_wtime();
    
    printf("Searching for notes containing 'project'...\n");
    printf("Found %d note(s) in %.6f seconds:\n", result_count, end - start);
    for (int i = 0; i < result_count; i++) {
        for (int j = 0; j < app.count; j++) {
            if (app.notes[j].id == search_results[i]) {
                printf("  - %s\n", app.notes[j].title);
                break;
            }
        }
    }
    
    // DEMO 2: Parallel search by tag
    printf("DEMO 2: Parallel Search by Tag\n");
    
    start = omp_get_wtime();
    search_by_tags_parallel(&app, "work", search_results, &result_count);
    end = omp_get_wtime();
    
    printf("Searching for notes tagged 'work'...\n");
    printf("Found %d note(s) in %.6f seconds:\n", result_count, end - start);
    for (int i = 0; i < result_count; i++) {
        for (int j = 0; j < app.count; j++) {
            if (app.notes[j].id == search_results[i]) {
                printf("  - %s\n", app.notes[j].title);
                break;
            }
        }
    }
    
    // DEMO 3: Parallel word count
    printf("DEMO 3: Parallel Word Count\n");
    
    int total_words;
    start = omp_get_wtime();
    count_words_parallel(&app, &total_words);
    end = omp_get_wtime();
    
    printf("Total words across all active notes: %d\n", total_words);
    printf("Word count completed in %.6f seconds\n", end - start);
    
    // DEMO 4: Parallel bulk archive
    printf("DEMO 4: Parallel Bulk Archive Operation\n");
    
    int to_archive[] = {id2, id4};
    printf("Archiving notes with IDs: %d, %d\n", to_archive[0], to_archive[1]);
    
    start = omp_get_wtime();
    archive_notes_parallel(&app, to_archive, 2);
    end = omp_get_wtime();
    
    printf("Archive operation completed in %.6f seconds\n", end - start);
    
    // Show all notes after archive
    display_all_notes(&app, 1);
    
    // DEMO 5: Parallel processing with different thread counts
    printf("DEMO 5: Performance Comparison\n");
    
    int thread_counts[] = {1, 2, 4};
    for (int t = 0; t < 3; t++) {
        omp_set_num_threads(thread_counts[t]);
        
        start = omp_get_wtime();
        for (int i = 0; i < 1000; i++) {
            search_notes_parallel(&app, "meeting", search_results, &result_count);
        }
        end = omp_get_wtime();
        
        printf("Threads: %d | 1000 searches: %.6f seconds\n", 
               thread_counts[t], end - start);
    }
    
    printf("\n=== Demonstration Complete ===\n");
    printf("OpenMP features demonstrated:\n");
    printf("  - Parallel for loops\n");
    printf("  - Critical sections\n");
    printf("  - Reduction clauses\n");
    printf("  - Parallel performance optimization\n");
    
    return 0;
}