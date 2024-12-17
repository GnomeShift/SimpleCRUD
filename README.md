<h1>
<p align="center">
<a href="https://github.com/GnomeShift/SimpleCRUD" target="_blank" rel="noopener noreferrer">SimpleCRUD</a>
</p>
</h1>

<p align="center">
<a href="README.md">🇷🇺 Русский</a>
</p>

## 🚀Быстрая навигация
* [Обзор](#обзор)
    * [Функции](#функции)
* [Установка](#установка)
* [Конфигурация](#конфигурация)

# 🌐Обзор
**SimpleCRUD** - это приложение для управления данными в БД SQLite.

> [!WARNING]
> Данная версия приложения - демонстрационная!

## ⚡Функции
* Просмотр данных.
* Добавление данных.
* Редактирование данных.
* Удаление данных.

# ⬇️Установка
Клонируйте репозиторий:
```bash
git clone https://github.com/GnomeShift/SimpleCRUD.git
```
Соберите файл ```.jar``` с помощью Maven:
```bash
mvn clean install
```
Запустите собранный файл:
```bash
java -jar SimpleCRUD-1.0-SNAPSHOT.jar
```

## ⚙️Конфигурация
1. Создайте файл БД SQLite с названием ```database```  в папке с приложением.
2. Запустите скрипт создания структуры БД.
   * Для Windows: ```create_structure_win.cmd```
   * Для Linux/MacOS: ```create_structure_bash.sh```