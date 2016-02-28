# Othello-14

CONTENTS OF README
----------------------------------------
1. Author information
2. Introduction
3. How to compile and run the project
4. Designs
5. Game flow
6. Limitation


AUTHOR INFORMATION
----------------------------------------
NAME: XIE, YAN
UID : 3035237262

NAME: YI, SHUIHAN
UID : 3035237793


INTRODUCTION
----------------------------------------
FILE LASTEST EDITTED: 2015/12/05
GAME NAME: Star Wars Othello

This android application project is an online game called "Othello" or "Reversi". Each game involves two players: "White" and "Black". The "Black" player (player who first login/wait in the room for matching) goes first. And then each player put chess on the board in turns. When neither of the players cannot put chess any more, or the whole chessboard is full, game’s over. Who’s winning is determined by the final score of each player. The game result will be recorded in the personal record.


HOW TO COMPILE AND RUN THE PROJECT
----------------------------------------
The project is compiled on the Android Studio 1.4. And user can run the APK file on AVD Nexus S (with resolution 800x480, API Level 23). 



DESIGNS
----------------------------------------
1.	Layouts Design
2.	Database Design
3.	Server Design

Layouts Design

The game includes five pages/layouts: 
1.	Main Page
2.	Login Page
3.	Record Page
4.	Matching Page
5.	Game Page

- Main Page, includes authors’ name and UID number, and a "CLICK TO CONTINUE" button links to the Login Page.

- Login Page, player can type his/her name in the text field. After input the name, the player can choose to click “RECORDS” or “START MATCHING” buttons. “RECORDS” button links Record Page. “START MATCHING” button links to Matching Page. If the player did not type any name, he/she cannot enter to these pages.

- Record Page, includes every game record for specific player’s name. Information includes “Name”, “Opposite”, “Result”, “Turn”, “Scores”.

- Matching Page, this page can show player who is already waiting in the room. The first player who logs in need to wait for the second player enter. When the second player entered, it will automatically jump to the Game Page.

- Game Page, mainly includes the game chess board, and other buttons for different gaming functions.

Details for Game Page: 

- "NEW GAME" Button: Start a new game for both players (reset the board).

- “SURRENDER” Button: Player can choose to give up the game, loss result will be recorded.

- "HINT" Button: Click to show the next available steps on the board (hints).

- Chessboard: Player can put chess on the empty cells on the board. If it’s not your turn, you will be not able to put any chess.

- "Turn": Shows “White” or “Black” turn in this round.

- "Number": Counts the number of each chess on the board.

- “Countdown Clock”: Shows how many seconds left for your turn.


Database Design

We create three tables to manage this game data: game table, players table, status table.
Game table contains two matched players account information and current game condition information. Looks like:
Id
Player1
Player2
Step
X
Y
New
Surrender
Time
End
Turn
Num
Char[]
Char[]
Num
Num
Num
Bool
Bool
Bool
Bool
Bool

Status table maintained the matching queue. When the first player came. He/she is waiting for the matching opponent. When the second player came into this game. Match this game, write players information in the game table to start a new game, then pop the two players out of the matching queue. 
Players table recorded the results of each game. Players can check their historical matches result.

PHP Server Design
 
PHP server mainly focus on responding for the request from the application and connect with the database. According to different request types (action name and parameters), server gave the different results. 
Action = select: Select player’s information to record activity.
Action = ready: Touch the button Start Matching.
Action = status: Match the game.
Action = newgame: Create a new game table to start a matching game.
Action = newgamebtn: Response for the click on the new game button.
Action = game: Record the step information and update data in the game table.
Action = gamelistener: Check the game table to listen to the opponent action.
Action = endgame: Set the end game information.


GAME FLOW
-----------------------------------------
When a player touching the Matching button, PHP server responded this request by the status information to keep the player waiting for the opponent or start the game. When two players matching the game, the game flow would jump into the Game page automatically. The black turn is the first came player. When the current turn player putting a chess on the chessboard, application will send the position information to the server. Server uses this game condition information to update the dataset. Each player executes a listener thread all the time to listen to the opponent actions. When the player received a new action from the opponent, he/she would update his/her chessboard. At the end of this game. Players would see the game result by a posted dialog, server would put the game result records into the database.

LIMITATION
-----------------------------------------
Unusual quit or termination is not considered in this game

