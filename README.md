<h1 align="center">Search engine</h1>
<h2>Описание:</h2>
<h4>Цель:</h4>
Даннный проект выполнен в качестве <b>итогового проекта</b> по обучающему курсу "Java-разработчик"
для закрепления пройденного материала.
<h4>Используемый стек:</h4>
Spring Boot, Spring Data JPA, MySQL, для морфологического анализа используется библиотека LuceneMorphology

<h4>Принцип работы:</h4>

1. В конфигурационном файле перед запуском приложения задаются адреса сайтов, по которым движок должен
осуществлять поиск.<br>
2. Поисковый движок обходит все страницы заданных сайтов и индексиует их
(создает так называемый индекс) так, чтобы потом находить наиболее релевантные страницы по любому
поисковому запросу.<br>
3. Пользователь присылает запрос через API движка. Запрос — это набор слов, по которым нужно найти страницы
сайта.<br>
4. Запрос определённым образом трансформируется в список слов, переведённых в базовую форму(лемму).
Например, для существительных — именительный падеж, единственное число.<br>
5. В индексе ищутся страницы, на которых встречаются все эти слова.<br>
6. Результаты поиска ранжируются, сортируются и отдаются пользователю.

<h2>Веб - интерфейс</h2>
Приложение имеет веб-интерфейс, который собрежит 3 вкладки:

<h4>DASHBOARD</h4>
<img src="https://raw.githubusercontent.com/artemkhal/searchengine/master/Dashboard.png" alt="DASHBOARD"/><br><br>
На вкладке DASHBOARD отображается статистика по сервису :
<li>Количество сайтов указанных в файле конфигурации</li>
<li>Количество найденных лемм</li>
<li>Количество индексов</li>
<li>Статистика по каждому сайту</li>


<h4>MANAGEMENT</h4>
<img src="https://raw.githubusercontent.com/artemkhal/searchengine/master/Management.png" alt="MANAGEMENT"/><br><br>
На вкладке MANAGEMENT представлена возможность начать/остановить индексацию, добавить отдельную страницу
для индексации(переиндексации), если она находится в пределах индексируемых сайтов.


<h4>SEARCH</h4>
<img src="https://raw.githubusercontent.com/artemkhal/searchengine/master/Search.png" alt="SEARCH"/><br><br>
На данной вкладке можно получить список веб-страниц содержащих ваш поисковый запрос,
а так же выбрать конкретный сайт для поиска запроса

<h2>Запуск:</h2>
Перед запуском приложения убедитесь, что у вас установлена база данных MySql
и пользователь под которым вы будете использовать базу данных имеет привелегии на внесение изменений.<br>

Для успешного запуска и использования приложения:
<ol>
<li>Скачайте из этого репозитория папку launch, она находится в корне проекта</li>
<li>В файле конфигурации application.yaml введите сервер порт, логи, пароль, путь к схеме SQL и перечень сайтов для индексации(см. подсказки в application.yaml)</li>
<li>Откройте терменал и введите команду <code>
java -jar /путь/к/папке/launch/SearchEngine-1.0-SNAPSHOT.jar
</code><br>
"/путь/к/папке" необходимо изменить на актуальный для вас</li>

</ol>

Если все сделано правильно, то в терминале будут появятся сообщения о старте приложения. Можно заходить на http://localhost:(указанный вами сервер порт)<br><br>
