#include <winsock2.h>
#include <stdio.h>
#pragma comment(lib, "ws2_32.lib")

#define PORT 9090
#define BUFFER_SIZE 1024

void handle_command(char *cmd, char *response) {
    if (strcmp(cmd, "ADD") == 0) {
        strcpy(response, "Note added (simulated)\n");
    }
    else if (strcmp(cmd, "LIST") == 0) {
        strcpy(response, "1. Shopping List\n2. Meeting Notes\n");
    }
    else if (strcmp(cmd, "HELLO") == 0) {
        strcpy(response, "Server: Hello from Windows Server!\n");
    }
    else {
        strcpy(response, "Unknown command\n");
    }
}

int main() {
    WSADATA wsa;
    SOCKET server_socket, client_socket;
    struct sockaddr_in server, client;
    int c, recv_size;
    char buffer[BUFFER_SIZE];
    char response[BUFFER_SIZE];
    
    // Initialize Winsock
    WSAStartup(MAKEWORD(2,2), &wsa);
    
    // Create socket
    server_socket = socket(AF_INET, SOCK_STREAM, 0);
    
    // Prepare server address
    server.sin_family = AF_INET;
    server.sin_addr.s_addr = INADDR_ANY;  // Listen on all interfaces
    server.sin_port = htons(PORT);
    
    // Bind
    bind(server_socket, (struct sockaddr*)&server, sizeof(server));
    
    // Listen
    listen(server_socket, 3);
    
    printf("Windows Note Server started on port %d\n", PORT);
    printf("Waiting for connection...\n");
    
    // Accept connection
    c = sizeof(struct sockaddr_in);
    client_socket = accept(server_socket, (struct sockaddr*)&client, &c);
    printf("Client connected!\n");
    
    // Communication loop
    while(1) {
        // Receive command
        recv_size = recv(client_socket, buffer, BUFFER_SIZE, 0);
        if(recv_size <= 0) break;
        
        buffer[recv_size] = '\0';
        printf("Received: %s\n", buffer);
        
        // Handle command
        handle_command(buffer, response);
        
        // Send response
        send(client_socket, response, strlen(response), 0);
    }
    
    closesocket(client_socket);
    closesocket(server_socket);
    WSACleanup();
    return 0;
}