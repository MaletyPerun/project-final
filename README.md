## [REST API](http://localhost:8080/doc)

## Концепция:

- Spring Modulith
    - [Spring Modulith: достигли ли мы зрелости модульности](https://habr.com/ru/post/701984/)
    - [Introducing Spring Modulith](https://spring.io/blog/2022/10/21/introducing-spring-modulith)
    - [Spring Modulith - Reference documentation](https://docs.spring.io/spring-modulith/docs/current-SNAPSHOT/reference/html/)

```
  url: jdbc:postgresql://localhost:5432/jira
  username: jira
  password: JiraRush
```

- Есть 2 общие таблицы, на которых не fk
    - _Reference_ - справочник. Связь делаем по _code_ (по id нельзя, тк id привязано к окружению-конкретной базе)
    - _UserBelong_ - привязка юзеров с типом (owner, lead, ...) к объекту (таска, проект, спринт, ...). FK вручную будем
      проверять

## Аналоги

- https://java-source.net/open-source/issue-trackers

## Тестирование

- https://habr.com/ru/articles/259055/

Список выполненных задач:
...

2) Удалить социальные сети: vk, yandex
3) Вынести чувствительную информацию
4) Использовалась in memory БД (H2)
6) Сделать рефакторинг метода com.javarush.jira.bugtracking.attachment.FileUtil#upload
7) Добавления тегов к задаче:
```
 - com.javarush.jira.bugtracking.task.TaskController
 - com.javarush.jira.bugtracking.task.TaskService
```
8) Добавить подсчет времени сколько задача находилась в работе и тестировании:
```
 - com.javarush.jira.bugtracking.task.ActivityService#timeInWork
 - com.javarush.jira.bugtracking.task.ActivityService#timeInTest
```
Вызов методов в 
```
com.javarush.jira.bugtracking.task.TaskUIController#showEditForm
```
9) Написать Dockerfile для основного сервера
10) Написать docker-compose файл для запуска контейнера сервера вместе с БД и nginx
11) Добавить локализацию минимум на двух языках для шаблонов писем (mails) и стартовой страницы index.html

В проекте используюутся 2 активных профиля: prod и test.

- prod для сборки проекта без тестирвания.
- test для тестирования всего приложения. 