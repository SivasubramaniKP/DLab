
#include <stdio.h>
#include <winsock2.h>
#include <string.h>
#include <stdlib.h>

#pragma comment(lib, "ws2_32")
#define BUFFER_SIZE 1000
#define PORT 9090

int main() {
    WSADATA wsa;
    SOCKET client_socket;
    struct sockaddr_in server_addr;

    char buffer[BUFFER_SIZE];
    char server_ip[20];

    printf("Enter the server address");
    scanf("%s", server_ip);

    WSAStartup(MAKEWORD(2, 2), &wsa);
    
    client_socket = socket(AF_INET, SOCK_STREAM, 0);

    server_addr.sin_family = AF_INET;
    server_addr.sin_port = htons(PORT);
    server_addr.sin_addr.S_un.S_addr = inet_addr(server_ip);

    connect(client_socket, (struct sockaddr *)&server_addr, sizeof(server_addr));


    while(1) {
        printf("Enter 1 to Create a note\n");
        printf("Enter 2 to read all note\n");
        printf("Enter 3 to update a note\n");
        printf("Enter 4 to delete a note\n");

        int option;
        scanf("%d", &option);
        getchar();

        switch(option) {
            case 1: {
                char title[50];
                char description[100];
                char author[50];
                printf("Enter the title\n");
                fgets(title, 50, stdin);
                printf("Enter the description\n");
                fgets(description, 100, stdin);
                printf("Enter the author name\n");
                fgets(author, 50, stdin);

                title[strcspn(title, "\n")] = '\0';
                description[strcspn(title, "\n")] = '\0';
                author[strcspn(author, "\n")] = '\0';

                snprintf(buffer, BUFFER_SIZE, "CREATE '%s' '%s' '%s'", title, description, author);

                send(client_socket, buffer, BUFFER_SIZE, 0);

                memset(buffer, 0, BUFFER_SIZE);

                recv(client_socket, buffer, BUFFER_SIZE, 0);
                
                printf("%s\n", buffer);
                break;
            }
            case 0:
                strcpy(buffer, "GET");
                send(client_socket, buffer, BUFFER_SIZE, 0);
                printf("INSIDE GET CLIENT %s\n", buffer);

                memset(buffer, 0, BUFFER_SIZE);
                recv(client_socket, buffer, BUFFER_SIZE, 0);

                printf("%s\n", buffer);
        }
    }
    closesocket(client_socket);
    WSACleanup();
    return 0;


}

