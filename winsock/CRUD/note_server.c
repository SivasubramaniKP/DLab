
#include <winsock2.h>
#include <string.h>
#include <stdio.h>
#include <stdlib.h>

int note_id = 0;

typedef struct {
    int note_id;
    char note_title[50];
    char note_description[100];
    char author[50];
} Note;

Note* create_note(char *note_title, char *note_description, char *author) {
    Note *newNote = (Note *)malloc(sizeof(Note));
    newNote->note_id = note_id++;
    strcpy(newNote->author, author);
    strcpy(newNote->note_title, note_title);
    strcpy(newNote->note_description, note_description);
    return newNote;
}

void print_note(Note *note){
    printf("Note ID = %d\n", note->note_id);
    printf("Note title = %s\n", note->note_title);
    printf("Note description = %s\n", note->note_description);
    printf("Note author = %s\n", note->author);
}

#define DB_SIZE 100
Note* DB[DB_SIZE];
int DB_INDEX = 0;

void print_all_notes() {
    for(int i = 0; i < DB_INDEX; i++){
        print_note(DB[i]);
    }
    printf("\n");
}

void handle_create_request(const char *request, char *response){
    char title[50];
    char description[100];
    char author[50];

    sscanf(request, "CREATE '%49[^']' '%99[^']' '%49[^']'", title, description, author);
    Note* newNote = create_note(title, description, author);
    
    if (DB_INDEX < DB_SIZE) {
        DB[DB_INDEX++] = newNote;
    }

    strcpy(response, "200 OK");
}

void handle_get_request(const char *request, char *response) {
    char out[1000] = ""; // Initialize to an empty string
    
    if (DB_INDEX == 0) {
        strcpy(response, "No notes found.");
        return;
    }

    for(int i = 0; i < DB_INDEX; i++) {
        char temp[256];
        // FIX: Changed %s to %d for note_id
        sprintf(temp, "ID: %d | Title: %s | Author: %s\n", 
                DB[i]->note_id, 
                DB[i]->note_title, 
                DB[i]->author);
        
        // Ensure we don't overflow the 'out' buffer
        if (strlen(out) + strlen(temp) < 999) {
            strcat(out, temp);
        }
    }
    strcpy(response, out);
}

void handle_update_request(const char *request, char *response){
    /*
        UPDATE <id> <new_title> <new_description> <new_author>
    */

    int note_id;
    char title[50];
    char description[100];
    char author[50];
    sscanf(request, "UPDATE <%d> <%49[^>]> <%99[^>]> <%49[^>]>", &note_id, title, description, author);
    int found = 0;
    for (int i = 0; i < DB_INDEX; i++){ 
        if(DB[i]->note_id == note_id) {
            found = 1;
            strcpy(DB[i]->note_title, title);
            strcpy(DB[i]->note_description, description);
            strcpy(DB[i]->author, author);
        }
    }
    if(!found) {
        strcpy(response, "Note id not found");
    } else strcpy(response, "Update succesful");
}

void handle_delete_request(const char *request, char *response){
    /*
        DELETE <id> 
    */
   int note_id;
   sscanf(request, "DELETE <%d>", &note_id);
   int array_index = -1;
   for(int i = 0; i < DB_INDEX; i++) {
    if (DB[i]->note_id == note_id) {
        array_index = i;
    }
   }

   if (array_index != -1) {
        free(DB[array_index]);
        for(int i = array_index; i < DB_INDEX - 1; i++) {
            DB[i] = DB[i+1];
        }
        DB_INDEX -= 1;

        strcpy(response, "Delete successfull");
   } else {
        strcpy(response, "Note id not found");
    }
}


#define PORT 9090
#define BUFFER_SIZE 1000

int main() {
    WSADATA wsa;
    SOCKET server_socket, client_socket;

    struct sockaddr_in server_addr, client_addr;
    int client_len = sizeof(client_addr);

    server_addr.sin_family = AF_INET;
    server_addr.sin_port = htons(PORT);
    server_addr.sin_addr.S_un.S_addr = INADDR_ANY;

    server_socket = socket(AF_INET, SOCK_STREAM, 0);

    bind(server_socket, (struct sockaddr *)&server_addr, sizeof(server_addr));
    
    listen(server_socket, 5);

    while(1) {
        client_socket = accept(server_socket, (struct sockaddr *)&client_addr, &client_len);

        while(1) {
            char buffer[BUFFER_SIZE];
            char response[BUFFER_SIZE];

            int recv_size = recv(client_socket, buffer, BUFFER_SIZE - 1, 0);
            if (recv_size <= 0) {
                break;
            }

            buffer[recv_size] = '\0';

            if (strncmp(buffer, "EXIT", 4) == 0) {
                printf("Client has requested to exit");
                break;
            }

            char action[20];
            sscanf(buffer, "%s", action);

            if (strcmp(action, "CREATE") == 0) {
                handle_create_request(buffer, response);
            } else if (strcmp(action, "UPDATE") == 0){
                handle_update_request(buffer, response);
            } else if (strcmp(action, "DELETE") == 0 ) {
                handle_delete_request(buffer, response);
            } else if (strcmp(action, "GET") == 0) {
                handle_get_request(buffer, response);
            } 
            else {
                strcpy(response, "ACTION NOT FOUND");
            }

            send(client_socket, response, strlen(response), 0);
        }
        closesocket(client_socket);
    }
    closesocket(server_socket);
    WSACleanup();
    printf("Server is closing down");
    
    return 0;
}

