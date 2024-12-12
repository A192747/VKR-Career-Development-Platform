# Quick start
```bash
./start.sh
```

## Для разработки (postgres, kafka, zoo и  keycloak)
```bash
./start.sh -dev
```

## Для остановки всех докер контейнеров
```bash
./start.sh -down-all
```

### Документация к api находится по адресу: </br>
http://localhost/api/v1/swagger-ui/index.html

### Архитектура проекта
![Screenshot](https://github.com/A192747/VKR-Career-Development-Platform/blob/develop/Info/images/architecture.jpg)

### Логин пользователя
В будущем ссылка будет изменена
``` http request
POST /realms/my_realm/protocol/openid-connect/token HTTP/1.1
Host: localhost:8180
Content-Type: application/x-www-form-urlencoded

grant_type=password&client_id=my_client&username=useruser&password=userpasswordd
```

Далее при каждом запросе отправляем в header запроса
```http request
Authorization: Bearer eyJhbGciOi...UIiwia2lk
```
