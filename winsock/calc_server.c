
#include <winsock2.h>
#include <stdio.h>
#include <string.h>
#include <stdlib.h>

#pragma comment(lib, "ws2_32.lib")

#define PORT 9090

#define BUFFER_SIZE 256

double calculate(double arg1, double arg2, char operator) {
    double res;
    switch (operator)
    {
        case '+':
            res = arg1 + arg2;
            break;
        case '-':
            res = arg1 - arg2;
            break;
        case '*':
            res = arg1 * arg2;
            break;
        case '/':
            if (arg2 != 0)
                res = arg1/arg2;
            else    
                res = 0;
    default:
        res = 0;
    }
    return res;
}

void handle_request(char *request, char *response) {
    double arg1, arg2, result;
    char operator;

    if (sscanf(request, "%lf %c %lf", &arg1, &operator, &arg2) == 3) {
        sprintf(response, "%.2lf %c %.2lf %.2lf", arg1, operator, arg2, calculate(arg1, arg2, operator));

    } else {
        sprintf(response, "ERROR Invalid format");
    }
}

int main() {
    WSADATA wsa;
    SOCKET server_socket, client_socket;

    struct sockaddr_in server_addr, client_addr;
    int client_addr_in = sizeof(client_addr);
    char buffer[BUFFER_SIZE];
    char response[BUFFER_SIZE];

    if(WSAStartup(MAKEWORD(2, 2), &wsa) != 0) {
        perror("Startup");
        exit(1);
    }



    server_socket = socket(AF_INET, SOCK_STREAM, 0);

    server_addr.sin_family = AF_INET;
    server_addr.sin_port = htons(PORT);
    server_addr.sin_addr.S_un.S_addr = INADDR_ANY;

    bind(server_socket, (struct sockaddr *)&server_addr, sizeof(server_addr));

    listen(server_socket, 5);

    while (1) {
        client_socket = accept(server_socket, (struct sockaddr *)&client_addr, &client_addr_in);

        while(1) {
            memset(buffer, 0, BUFFER_SIZE);
            memset(response, 0, BUFFER_SIZE);

            int recv_size = recv(client_socket, buffer, BUFFER_SIZE, 0);
            if(recv_size <= 0) {
                break;
            }

            buffer[recv_size] = '\0';
            printf("Received %s\n", buffer);

            if(strncmp(buffer, "EXIT", 4) == 0){
                printf("Client Request exit\n");
                return 0;
            }

            handle_request(buffer, response);

            send(client_socket, response, strlen(response), 0);
            printf("Sent %s\n", response);
        }
        closesocket(client_socket);
    }

    closesocket(server_socket);
    WSACleanup();
    printf("Server shuts down\n");

}

