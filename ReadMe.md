# Quick start

Для быстрого запуска проекта достаточно выполнить команду ниже. 
> [!IMPORTANT]
> При первом запуске отсутствуют некоторые файлы конфигурации (к примеру mail.properties). Их нужно добавить самостоятельно. 

После добавления необходимых файлов в дальнейшем для запуска достаточно просто выполнять данную команду:
```bash
./start.sh
```

## Для разработки (postgres, kafka, zoo, keycloak и Ollama)
```bash
./start.sh -dev
```

## Для остановки всех докер контейнеров (не удаляя Ollama)
Если вы хотите завершить работу контейнеров, но при этом не хотите ожидать длительной загрузки Ollama при следующем запуске, то достаточно выполнить следующую команду:
```bash
./start.sh -down
```

## Для остановки всех докер контейнеров
> [!WARNING]
> После выполнения данной команды будут отчищены все образы! Данная команда необходима, если нужно пересобрать все контейнеры.

```bash
./start.sh -down-all
```

### Документация к api находится по адресу: </br>
http://localhost:80/swagger-ui/index.html

### Архитектура проекта
![Screenshot](https://github.com/A192747/VKR-Career-Development-Platform/blob/develop/Info/images/architecture.jpg)

### Логин пользователя
``` http request
POST /auth HTTP/1.1
Host: localhost:80
Content-Type: application/x-www-form-urlencoded

grant_type=password&client_id=my_client&username=useruser&password=userpassword
```

Далее при каждом запросе отправляем в header запроса
```http request
Authorization: Bearer eyJhbGciOi...UIiwia2lk
```
