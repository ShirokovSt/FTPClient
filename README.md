# FTPClient
Клиент-серверное приложение, используеюшее протокол FTP, сервер взят с https://github.com/pReya/ftpServer.git. Данное приложение работает как на Windows, так и на Linux.
# Инструкция по сборке
Для того, чтобы собрать проект с нуля, требуются следующие действия:
1. Для сборки сервера и клиента:
* Прописать команду для создания CLASS файлов: **javac -sourcepath src/main -d bin src/main/FTPProtocol/*.java src/main/Main.java**
* Прописать команду для создания JAR файлов:
  - для сервера: **jar -cmf server_manifest/manifest.mf Server.jar -C bin .**
  - для клиента: **jar -cmf client_manifest/manifest.mf FTPClient.jar -C bin .**
2. Для сборки тестов:
* Прописать команду для создания CLASS файлов: 
  - для Windows: **[javac -cp .;lib/* -d bin src/test/FTPProtocol/*.java]**
  - для Linux: **[javac -cp .:lib/* -d bin src/test/FTPProtocol/*.java]**
* Прописать команду для создания JAR файлов: **jar -cmf test_manifest/manifest.mf FTPTest.jar -C bin .**

Сборку проводить не обязательно, необходимые JAR файлы уже есть в главном каталоге.
# Инструкция по запуска приложения
Сервер запускается из командной строки главного каталога командой: **java -jar Server.jar**
Клиент запускается аналогично, но другой командой: **java -jar FTPClient.jar [name] [password] [ip]**
На данный момент приложение поддерживает одного пользователя с параметрами: name - comp4621, password - network, ip - ip-адресс сервера
# Инструкция по работе с приложением
После авторизации, клиенту выводится следующее сообщение:
***
![image](https://user-images.githubusercontent.com/62287381/187708995-8950a0f6-5d9c-4961-9408-0fd10a638d2e.png)
***
Здесь он должен выбрать, в каком режиме будет происходить обмен данными: 0 - в пассивном, 1 - в активном.
После выбора режима, у пользователя появляется следующее меню:
***
![image](https://user-images.githubusercontent.com/62287381/187709496-b0c60019-f560-4c91-8ea2-23332f6971d9.png)
***
Здесь можно выбрать действия, которые можно делать пользователю:
* getList - получить имена всех студентов
* getSt <id> - получить информацию о студенте, по id
* addSt <name> - добавить студента с именем name (id генерируется автоматически)
* delSt <id> - удалить студента по id
* exit - выход из программы

Ниже приведены примеры работы команд:
* пример работы getList
***
![image](https://user-images.githubusercontent.com/62287381/187873368-84e60b66-8fd1-4a00-ab04-dd82247641a7.png)
***
* пример работы getSt <id>
***
![image](https://user-images.githubusercontent.com/62287381/187710889-bebfe146-909e-4c9e-9749-d91b531c6ce5.png)
***
* пример работы addSt <name>
***
![image](https://user-images.githubusercontent.com/62287381/187711062-7d943cf7-d48b-4741-a3cf-ae14173eef8c.png)
***
* getList после addSt Rahim
***
![image](https://user-images.githubusercontent.com/62287381/187873641-5520e61e-253a-4263-b849-98c143528f90.png)
***
* пример работы delSt <id>
***
![image](https://user-images.githubusercontent.com/62287381/187711553-7373c8f3-ca89-475c-902a-c31574acf3fe.png)
***
* getList после delSt 1
***
![image](https://user-images.githubusercontent.com/62287381/187874195-d7a69011-e140-4216-8340-20d380e7e2f7.png)
***
* пример работы exit
***
![image](https://user-images.githubusercontent.com/62287381/187711794-f662b858-e60d-4abb-8c87-5f224523561d.png)
***

# Инструкция по запуску тестов
Запуск тестов происходит с помощью команды: **java -jar FTPTest.jar**
Тесты проверяют все основные методы: getList, getSt [id], addSt [name], delSt [id] и exit.
Для этого отправляется на сервер специальный файл testJson.txt, с тестовым списком студентов. 
* getList - проверяется файл, возвращаемый с сервера, равен ли он исходному списку или нет
* getSt <id> - проверяется правильное ли имя студента вернулось по id
* addSt <name> - происходит добавление на сервер данного студента, потом получение файла с сервера, а в даном файле проверяется наличие данного студента
* delSt <id> - происходит удаление на сервере данного студента, потом получение файла с сервера, а в даном файле проверяется, есть ли этот студент
* exit - проверяется закрыт ли сокет после выхода или нет

