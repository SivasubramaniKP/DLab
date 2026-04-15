
#include <winsock2.h>
#include <string.h>
#include <stdio.h>
#include <stdlib.h>

#define PORT 9090
#define BUFFERSIZE 256

int main() {
    WSADATA wsa;
    SOCKET client_socket;

    struct sockaddr_in server_addr;
    char buffer[BUFFERSIZE];
    char server_ip[20];

    WSAStartup(MAKEWORD(2, 2), &wsa);

    client_socket = socket(AF_INET, SOCK_STREAM, 0);

    printf("Enter the servere IP address\n");
    scanf("%s", server_ip);

    server_addr.sin_family = AF_INET;
    server_addr.sin_port = htons(PORT);
    server_addr.sin_addr.S_un.S_addr = inet_addr(server_ip);

    connect(client_socket, (struct sockaddr *)&server_addr, sizeof(server_addr));

    // fgets(buffer, BUFFERSIZE, stdin);

    const char *request = "5.0 + 6.0";

    strcpy(buffer, request);

    send(client_socket, buffer, strlen(buffer), 0);


    memset(buffer, 0, BUFFERSIZE);

    recv(client_socket, buffer, BUFFERSIZE, 0);

    printf("Result = %s\n", buffer);

    closesocket(client_socket);

    WSACleanup();

    return 0;
}