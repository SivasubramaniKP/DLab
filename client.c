// note_client.c - Windows Host (User Interface)
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <winsock2.h>
#include <windows.h>

#pragma comment(lib, "ws2_32.lib")

#define PORT 9090
#define BUFFER_SIZE 4096

void display_menu() {
    printf("\n=== NOTE-TAKING APPLICATION ===\n");
    printf("1. List all notes\n");
    printf("2. Add new note\n");
    printf("3. View note\n");
    printf("4. Delete note\n");
    printf("5. Search notes\n");
    printf("6. Help\n");
    printf("7. Exit\n");
    printf("===============================\n");
    printf("Choice: ");
}

void send_command(SOCKET sock, char *command) {
    send(sock, command, strlen(command), 0);
    
    char response[BUFFER_SIZE];
    memset(response, 0, BUFFER_SIZE);
    recv(sock, response, BUFFER_SIZE, 0);
    
    printf("\n%s\n", response);
}

int main() {
    WSADATA wsaData;
    SOCKET client_socket;
    struct sockaddr_in server_addr;
    char buffer[BUFFER_SIZE];
    int choice;
    
    // Initialize Winsock
    if (WSAStartup(MAKEWORD(2, 2), &wsaData) != 0) {
        printf("WSAStartup failed. Error Code: %d\n", WSAGetLastError());
        return 1;
    }
    
    // Create socket
    if ((client_socket = socket(AF_INET, SOCK_STREAM, 0)) == INVALID_SOCKET) {
        printf("Socket creation failed. Error Code: %d\n", WSAGetLastError());
        WSACleanup();
        return 1;
    }
    
    // Configure server address (Localhost due to port forwarding)
    server_addr.sin_family = AF_INET;
    server_addr.sin_port = htons(PORT);
    server_addr.sin_addr.s_addr = inet_addr("192.168.0.29"); // Localhost
    
    // Connect to server
    printf("Connecting to Note Server at 127.0.0.1:%d...\n", PORT);
    if (connect(client_socket, (struct sockaddr*)&server_addr, sizeof(server_addr)) < 0) {
        printf("Connection failed. Error Code: %d\n", WSAGetLastError());
        printf("Make sure:\n");
        printf("1. Linux server is running\n");
        printf("2. VirtualBox port forwarding is set: 127.0.0.1:%d -> Guest:%d\n", PORT, PORT);
        closesocket(client_socket);
        WSACleanup();
        return 1;
    }
    
    printf("Connected to Note Server on Linux guest!\n");
    
    // Main application loop
    while (1) {
        display_menu();
        scanf("%d", &choice);
        getchar(); // Clear newline
        
        switch (choice) {
            case 1: // LIST
                send_command(client_socket, "LIST");
                break;
                
            case 2: // ADD
                printf("Enter title: ");
                fgets(buffer, 100, stdin);
                buffer[strcspn(buffer, "\n")] = 0; // Remove newline
                
                char title[100];
                strcpy(title, buffer);
                
                printf("Enter content: ");
                fgets(buffer, 500, stdin);
                buffer[strcspn(buffer, "\n")] = 0;
                
                char command[700];
                sprintf(command, "ADD %s | %s", title, buffer);
                send_command(client_socket, command);
                break;
                
            case 3: // VIEW
                printf("Enter note ID: ");
                fgets(buffer, 10, stdin);
                buffer[strcspn(buffer, "\n")] = 0;
                sprintf(command, "VIEW %s", buffer);
                send_command(client_socket, command);
                break;
                
            case 4: // DELETE
                printf("Enter note ID to delete: ");
                fgets(buffer, 10, stdin);
                buffer[strcspn(buffer, "\n")] = 0;
                sprintf(command, "DELETE %s", buffer);
                send_command(client_socket, command);
                break;
                
            case 5: // SEARCH
                printf("Enter search keyword: ");
                fgets(buffer, 100, stdin);
                buffer[strcspn(buffer, "\n")] = 0;
                sprintf(command, "SEARCH %s", buffer);
                send_command(client_socket, command);
                break;
                
            case 6: // HELP
                send_command(client_socket, "HELP");
                break;
                
            case 7: // EXIT
                send_command(client_socket, "EXIT");
                closesocket(client_socket);
                WSACleanup();
                printf("Goodbye!\n");
                return 0;
                
            default:
                printf("Invalid choice. Please try again.\n");
        }
        
        printf("\nPress Enter to continue...");
        getchar();
        
        // Clear screen for better UI
        system("cls");
    }
    
    return 0;
}