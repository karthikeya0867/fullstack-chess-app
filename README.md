# Fullstack Chess App

## Description

A fullstack chess application implementing a complete **local player mode**.  
Backend built with Spring Boot, frontend with React, and SVG assets for chess pieces.  
This project supports local player-vs-player gameplay with fully integrated game logic.

![WARNING](https://img.shields.io/badge/WARNING-red)

> ⚠️ This is my first fullstack project, and is still in development phase.  
> Expect bugs and missing features and implementations if you are using this now — I'm working on improvements , Optimisations and new features incrementally.  
>  **First commit made on 15-07-2025** with basic local gameplay implementation.
> Once this Project is completed & Production Ready This Warning will be removed

## Features

- Local player mode: two players can play on the same device
- Responsive chessboard UI with React
- Backend chess rules and validation in Spring Boot
- SVG-based chess piece rendering

## Installation

1. Clone the repo:

   ```bash
   git clone https://github.com/karthikeya0867/fullstack-chess-app.git
   ```

2. Starting Front End :
   first install [node.js](https://nodejs.org/en/download) and then
   To Start FrontEnd
   ```bash
       cd Chess-FrontEnd
       npm install
       npm run dev
   ```
   This will start front end
   you can visit [frontEnd-URL](localhost:5173/) to see front end then
3. Starting Back End:
   If You Have Apache Maven

   ```
   cd Chess-BackEnd/demo
   mvn spring-boot:run
   ```

   Else Follow The installation process from [Apache-Maven](https://maven.apache.org/install.html) or Run This Instead

   ```bash
   cd Chess-BackEnd/demo
   ./mvnw spring-boot:run
   ```

   This will start tomcat on port 8080 you dont have to visit anything the front end will handle all API calls just run this commands and

   Play chess locally against a friend on the same device

![WIP](https://img.shields.io/badge/status-in--progress-orange)

- Random players : MultiPlayer Games Using WebSockets
- Computer GamePlay : Adding Vs AI using Stockfish API
- Move Undo & History
- Authorization and Authentication is Yet To Be Implemented So Login and Signup are nothing but UI Gimmicks
- Connect A database to hold user data
