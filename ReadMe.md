# Quick start

Для быстрого запуска проекта достаточно выполнить команду ниже. 
> [!IMPORTANT]
> При первом запуске отсутствуют некоторые файлы конфигурации (к примеру mail.properties). Их нужно добавить самостоятельно. 

После добавления необходимых файлов в дальнейшем для запуска достаточно просто выполнять данную команду:
```bash
./app.sh
```
После выполнения команды по умолчанию запускается Cloudflared Tunnel, в консоли выведется https ссылка (далее cloudflared-url) для доступа к api через интернет. 

## Для разработки (postgres, kafka, zoo, keycloak и ollama)
```bash
./app.sh -dev
```

## Для остановки всех докер контейнеров
```bash
./app.sh -down
```

### Документация к api находится по адресу: </br>
* Локально http://localhost:8181/swagger-ui/index.html
* Удаленно https://${cloudflared-url}/swagger-ui/index.html

### Логин пользователя
``` http request
POST /auth HTTP/1.1
Host: ${cloudflared-url:localhost:8181}
Content-Type: application/x-www-form-urlencoded

grant_type=password&client_id=my_client&username=${useruser}&password=${userpassword}
```

Далее при каждом запросе отправляем в header запроса
```http request
Authorization: Bearer eyJhbGciOi...UIiwia2lk
```

### Обновление refresh_token пользователя
``` http request
POST /auth HTTP/1.1
Host: ${cloudflared-url:localhost:8181}
Content-Type: application/x-www-form-urlencoded

grant_type=refresh_token&client_id=my_client&refresh_token=${refresh_token}
```

## Архитектура проекта
![Screenshot](https://github.com/A192747/VKR-Career-Development-Platform/blob/develop/Info/images/architecture.jpg)

### Модули основного сервиса (монолита)
![Screenshot](https://github.com/A192747/VKR-Career-Development-Platform/blob/develop/Info/images/modules.png)

### Процесс интервью
![Screenshot](https://github.com/A192747/VKR-Career-Development-Platform/blob/develop/Info/images/interview-process.jpg)