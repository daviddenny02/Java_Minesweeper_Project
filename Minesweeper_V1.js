"use strict";

// Required dependencies and imports
const fs = require('fs');
const readlineSync = require('readline-sync');

// Have fun!
class Minesweeper_V1 {

	// Class to hold the attributes of a tile on the gameBoard.
	// The playerBoard will be an array of these objects.
	static Tile = class {
		constructor() {
			this.revealed = false;		// True when the player reveals the tile.
			this.mine = false;			// True if there is a mine on the tile.
			this.flagged = false;		// True when the player flags the tile.
			this.surroundingMines = 0;	// Holds the number of mines surrounding the tile.
										//    Will be -1 if the tile is a mine itself.
		}
	}

	// Class to hold the attributes of each player on the leaderboard.
	static Player = class {
		constructor() {
			this.name = "AAA";			// Default name.
			this.bestTime = 9999;		// Default best time.
		}
	}

	// Main method is the highest-level method, running the main menu and playing the game.
	static main(args) {	
		// Initialize leaderboard file if it doesn't exist
		Minesweeper_V1.initializeLeaderboardFile();

		process.stdout.write("\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n");
		process.stdout.write("=======================\nMinesweeper (Version 1)\n=======================");
		process.stdout.write("\n\nMenu:\t1.) Play\n\t2.) Quit\n\t3.) Leaderboard\n\t4.) Change Difficulty\n\t5.) How To Play\n\nEnter Your Selection: ");
		
		let MenuSelection = readlineSync.question();
		let mineDifficulty = 10; // Default difficulty of 10 mines = normal
		
		while(MenuSelection.toLowerCase() !== "quit") {
			// Ensure the leaderboard is up-to-date.
			let leaderboard = Minesweeper_V1.readLeaderboard();
			MenuSelection = MenuSelection.toLowerCase();
			switch(MenuSelection) {
			
			// Parse the user's input from their MenuSelction.
			case "1":
			case "play":
				// Start the timer
				let startTime = process.hrtime.bigint();
				
                let gameResult = Minesweeper_V1.playMinesweeper(readlineSync, mineDifficulty); // Start the game!
                
                if(gameResult === 0) { // User quit mid-game
                	process.stdout.write("Quitting...\n");
                }
                if(gameResult === 1) { // User lost the game
                	process.stdout.write("==================\nGAME OVER YOU LOSE\n==================\n");
                }
                if(gameResult === 2) { // User won the game!
                	process.stdout.write("==================\nGAME OVER YOU WIN!\n==================\n");
                	// End the timer to store in the leaderboard
                	let endTime = process.hrtime.bigint();
                	let userTime = Number(endTime - startTime) / 1_000_000_000.0;
                	process.stdout.write("\nYou won in " + userTime + " seconds!");
                	process.stdout.write("\n\nEnter your name (3 letters): ");
                	let userName = readlineSync.question();
                	if(userName.length !== 3) {
                		while(userName.length !== 3) {
                			process.stdout.write("\nImproper name formatting! Please re-enter: ");
                			process.stdout.write("\nEnter your name (3 letters): ");
                        	userName = readlineSync.question();
                		}
                	}
                	leaderboard = Minesweeper_V1.addPlayer(userName.toUpperCase(), userTime, leaderboard);
                }
                break;
			case "2":
				MenuSelection = "quit";
				break;
			// The leaderboard option to print the current Leaderboard.
			case "3":
			case "leaderboard":
				// Ensure the leaderboard file exists to avoid an exception.
		        //let file = new fs.StatWatcher("leaderboard.txt");
		        if (!fs.existsSync("leaderboard.txt")) {
		        	console.log("\nThere is currently no recorded data.\nPlay some games!");
		            break;
		        }
		        // Else read the leaderboard.
                console.log("\nHere are the fasted recorded times:");
                leaderboard = Minesweeper_V1.readLeaderboard();
                
                process.stdout.write("\nRANK\tNAME\tTIME (s)");
                process.stdout.write("\n--------------------------\n");
                
                let i = 1;
                for (let playerToCompare of leaderboard) {
                	process.stdout.write(i + "\t" + playerToCompare.name + "\t" + String(playerToCompare.bestTime));
                	process.stdout.write("\n");
                	i++;
    	        }
                break;
            // The changeDifficulty option to change the difficulty if desired.
			case "4":
			case "change difficulty":
				mineDifficulty = Minesweeper_V1.changeDifficulty(readlineSync, mineDifficulty);
				break;
			case "5":
			case "how to play":
				Minesweeper_V1.printRules();
				break;
            // Default case to display unknown command.
            // This can be replaced with an exception throw/catch if desired.
            default:
            	console.log("\nUnknown Command");
            	break;
			}
			// Break completely if the user quits from the main menu.
			if(MenuSelection === "quit") {break;}
			process.stdout.write("\nMenu:\t1.) Play\n\t2.) Quit\n\t3.) Leaderboard\n\t4.) Change Difficulty\n\t5.) How To Play\n\nEnter Your Selection: ");
			MenuSelection = readlineSync.question();
		}
		process.stdout.write("\nQuitting...");
	}
	
	// This is the main method to play the game
	// Returns three seperate values for each end-game scenario:
	// 		Returns 0 if the user quit, or anther exception was encountered
	//		Returns 1 if the user lost
	//		Returns 2 if the user won
	static playMinesweeper(input, mineDifficulty) {

		console.log("\nGenerating New Board...\n");
        
		// Needed work variables.
        let gameOver = false;	// This variable determines if the game is still running.
        let devMode = false;	// This variable determines if devMode is enabled or disabled.
        let firstTurn = true;	// This variable determines if it is the user's first turn.
        let TileSelection;		// This variable stores the user command.
        
        // Create the gameBoard.
        // Our work array of objects is made from getBoardData().
        let currentGameBoard = Minesweeper_V1.createGameBoard(mineDifficulty);
        let boardData = Minesweeper_V1.getBoardData(currentGameBoard);
        
        // Loop through turns while the game is still active.
        while(gameOver === false) {
        	
        	// Check if the player has won before allowing the next turn.
        	if(Minesweeper_V1.checkForWin(boardData, mineDifficulty)) {
        		Minesweeper_V1.printFinalBoard(boardData);
        		process.stdout.write("\n");
        		return 2;
        	}
        	
        	// Reprint the updated playerBoard with the results of the previous turn.
        	// Display the options the player has on this turn.
        	// Also prints the boardData if devMode is enabled.
        	Minesweeper_V1.printPlayerBoard(boardData, devMode);
        	process.stdout.write("\nType in a coordinate to Reveal or\nFlag like this: \"RB6\" or \"FI8\".");
        	process.stdout.write("\nOr type \"Quit\" to exit to the menu.");
        	process.stdout.write("\nOr type \"Dev\" to toggle developer mode.\n\nReveal or flag a tile: ");
        	// Get the player's next turn!
        	TileSelection = input.question();
        	process.stdout.write("\n");
        	
        	// Parsing the command with a char array.
        	// Command will default to lowercase for ease of parsing.
        	TileSelection = TileSelection.toLowerCase();
        	let TileSelectionParser = TileSelection.split('');
        	
        	// This is the quit condition. It will simply exit the game, return a seperate integer.
        	if(TileSelection.toLowerCase() === "quit") {
        		return 0;
        	}
        	
        	// This is the devMode condition. It will toggle devMode to print the boardData when printing.
        	else if((TileSelection.toLowerCase() === "dev") || (TileSelection.toLowerCase() === "developer") ) {
        		process.stdout.write("Toggling developer mode...\n\n");
        		devMode = !devMode;
        	}
        	
        	// Check that the command is valid.
        	// Many of the following if statements simply check for invalid commands.
        	// Could replace this with exception throws and catches if desired.
        	else if((TileSelectionParser.length !== 3) || ((TileSelectionParser[0] !== 'f') && (TileSelectionParser[0] !== 'r'))) {
        		process.stdout.write("Invalid format for selection!\n\n");
        		continue;
        	}
        	// Check that the first tile coordinate is valid.
        	else if(!(TileSelectionParser[1] >= 'a' && TileSelectionParser[1] <= 'j')) {
        		process.stdout.write("Invalid format for selection!\n\n");
        		continue;
        	}
        	// Check that the second tile coordinate is valid.
        	else if(!(TileSelectionParser[2] >= '0' && TileSelectionParser[2] <= '9')) {
        		process.stdout.write("Invalid format for selection!\n\n");
        		continue;
        	}
        	else {
        		// If the code reaches here, then the player has entered a valid command!
        		// Reveal a tile.
        		if(TileSelectionParser[0] === 'r') {
        			// Prevent revealing a flagged tile.
        			// For reference, the "TileSelectionParser[2] - '0'" shown here simply converts the char to an int.
        			if(boardData[TileSelectionParser[2] - '0'][Minesweeper_V1.letterConverter(TileSelectionParser[1])].flagged === true) {
        				continue;
        			}
        			// This is the lose condition. If a player reveals a mine, they lose!
        			// Generate a new board if the player tripped a mine on their first turn.
        			if(boardData[TileSelectionParser[2] - '0'][Minesweeper_V1.letterConverter(TileSelectionParser[1])].mine === true) {
        				// Prevent a user from losing on their first turn.
        				if(firstTurn) {
        					process.stdout.write("You tripped a mine on your first turn!\nGenerating new board...\n\n");
        					while(boardData[TileSelectionParser[2] - '0'][Minesweeper_V1.letterConverter(TileSelectionParser[1])].mine === true) {
        						currentGameBoard = Minesweeper_V1.createGameBoard(mineDifficulty);
            			        boardData = Minesweeper_V1.getBoardData(currentGameBoard);
        					}
        				}
        				else {
        					Minesweeper_V1.printFinalBoard(boardData);
            				process.stdout.write("\n");
            				return 1;
        				}
        			}
        			boardData[TileSelectionParser[2] - '0'][Minesweeper_V1.letterConverter(TileSelectionParser[1])].revealed = true;
            		boardData = Minesweeper_V1.fillBlanks(boardData);
            		firstTurn = false;
        		}
        		// Flag a tile.
        		if(TileSelectionParser[0] === 'f') {
        			// Prevent flagging a revealed tile -- there's no point.
        			if(boardData[TileSelectionParser[2] - '0'][Minesweeper_V1.letterConverter(TileSelectionParser[1])].revealed === true) {
        				continue;
        			}
        			// Toggle the desired flag coordinate.
        			boardData[TileSelectionParser[2] - '0'][Minesweeper_V1.letterConverter(TileSelectionParser[1])].flagged =
        					!boardData[TileSelectionParser[2] - '0'][Minesweeper_V1.letterConverter(TileSelectionParser[1])].flagged;
        		}
        	}
        }      
        return 0;
	}
	
	// Method to print the rules of Minesweeper
	static printRules() {
		process.stdout.write("\n===========\nHOW TO PLAY\n===========\n");
		process.stdout.write("\nIn Minesweeper, the goal is to clear a grid of hidden");
		process.stdout.write("\ntiles without uncovering any mines. Each uncovered");
		process.stdout.write("\ntiles shows a number indicating how many mines are");
		process.stdout.write("\nadjacent to it, helping you deduce where mines are likely");
		process.stdout.write("\nlocated. Use this information to flag tiles you believe");
		process.stdout.write("\ncontain mines, which prevents accidentally uncovering them.");
		process.stdout.write("\nOnce you've flagged all the mines and uncovered the rest");
		process.stdout.write("\nof the grid, you win the game! If you uncover a mine,");
		process.stdout.write("\nthe game ends.");
		process.stdout.write("\n\nFor this implementation of the game, simply uncover");
		process.stdout.write("\nmines by entering the tile's coordinates when prompted!\n");
	}
	
	// Method to change the difficulty for the games.
	// Returns the number of mines corresponding to the set difficulty (5, 10, or 20 mines)
	static changeDifficulty(input, mineDifficulty) {
		if(mineDifficulty === 5) {
			process.stdout.write("\nThe difficulty is currently set to EASY.");
		}
		else if(mineDifficulty === 10) {
			process.stdout.write("\nThe difficulty is currently set to NORMAL.");
		}
		else if(mineDifficulty === 20) {
			process.stdout.write("\nThe difficulty is currently set to HARD.");
		}
		
		process.stdout.write("\n\nChange your difficulty here!\nHere are your options:\n");
		process.stdout.write("\nMenu:\t1.) Easy   (5 mines)\n\t2.) Normal (10 mines)\n\t3.) Hard   (20 mines)\n\nEnter Your Selection: ");
		
		let MenuSelection = input.question();
		MenuSelection = MenuSelection.toLowerCase();
		
		switch(MenuSelection) {
		case "1":
		case "easy":
			process.stdout.write("\nSetting the difficulty to EASY difficulty...\n");
			mineDifficulty = 5;
			break;
		case "2":
		case "normal":
			process.stdout.write("\nSetting the difficulty to NORMAL difficulty...\n");
			mineDifficulty = 10;
			break;
		case "3":
		case "hard":
			process.stdout.write("\nSetting the difficulty to HARD difficulty...\n");
			mineDifficulty = 20;
			break;
		default:
			process.stdout.write("\nUnknown difficulty entered. Not changing difficulty...\n");
			break;
		}
		return mineDifficulty;
	}
	
	// This method updates the current leaderboard ArrayList by reading the leaderboard.txt file.
	static readLeaderboard(){
		let leaderboard = [];
		
		// Ensure the leaderboard file exists to avoid an exception.
        if (!fs.existsSync("leaderboard.txt")) {
            return leaderboard;
        }
		
		// Let us print modified file
		try {
			let data = fs.readFileSync("leaderboard.txt", 'utf8');
			let lines = data.split("\n").filter(line => line !== "");
			for(let i = 0; i < lines.length; i += 2) {
				let playerInst = new Minesweeper_V1.Player();
				playerInst.name = lines[i];
				playerInst.bestTime = Number(lines[i+1]);
				leaderboard.push(playerInst);
			}
		}
		// Catch block to handle IO exceptions
		catch (e) {
			console.log("Exception Occurred" + e);
		}
		return leaderboard;
	}
	
	// This method adds a winning user's score to the leaderboard, and updates it in-file.
	// This method reads the current leaderboard ArrayList and writes to leaderboard.txt.
	// The Leaderboard is stored locally with the leaderboard.txt file in the programs default directory.
	static addPlayer(userName, userTime, leaderboard) {
    
		// Adding the player's winning data to the leaderboard.
		let playerInst = new Minesweeper_V1.Player();
		playerInst.name = userName;
		playerInst.bestTime = userTime;
		
		let i = 0;
		let added = false;  // Track if the player was added
	
		// Add a player at their specific, ordered index
		for (let playerToCompare of leaderboard) {
			if (playerInst.bestTime < playerToCompare.bestTime) {
				leaderboard.splice(i, 0, playerInst);
				added = true;
				break;
			}
			i++;
		}
	
		// If player is the slowest, add to the end
		if (!added) {
			leaderboard.push(playerInst);
		}
	
		let fileName = "leaderboard.txt";
		// Try block to check for exceptions
		try {
			let output = "";
			// Writing the updated leaderboard to the leaderboard file
			for (let playerToCompare of leaderboard) {
				output += playerToCompare.name + "\n";
				output += String(playerToCompare.bestTime) + "\n";
			}
			fs.writeFileSync(fileName, output, 'utf8');
			console.log("Leaderboard updated successfully!");
		}
		// Catch block to handle exceptions
		catch (e) {
			// Display message when error occurs
			console.log("Exception occurred while updating leaderboard: " + e);
		}
		return leaderboard;
	}

	static initializeLeaderboardFile() {
		if (!fs.existsSync("leaderboard.txt")) {
			try {
				fs.writeFileSync("leaderboard.txt", "", 'utf8');
				console.log("Created new leaderboard file.");
				return true;
			} catch (e) {
				console.log("Error creating leaderboard file: " + e);
				return false;
			}
		}
		return true; // File already exists
	}
	
	// Method to check for win. If 100 - mineDifficulty tiles are revealed,
	// and none are mines, then the user has won!
	static checkForWin(boardData, mineDifficulty) {
		let tileCounter = 0;
		for(let i = 0; i < 10; i++) {
			for(let j = 0; j < 10; j++) {
				if((boardData[i][j].revealed === true) && (boardData[i][j].mine === false)) {
					tileCounter++;
				}
			}
		}
		if(tileCounter === (100 - mineDifficulty)) {
			return true;
		}
		else {
			return false;
		}
	}
	
	// Method that checks if there are tiles to be automatically revealed.
	// If this returns false, then all blanks that should be revealed have
	// been revealed.
	// This can be replaced with a recursive check if desired.
	static checkBlanks(boardData){
		for(let i = 0; i < 10; i++) {
			for(let j = 0; j < 10; j++) {
				if(boardData[i][j].mine === true) {continue;} // Skip if a mine is to be checked
				
				//process.stdout.write("\nYIKESSS...\n");
				if((boardData[i][j].surroundingMines === 0) && (boardData[i][j].revealed === true)) { // A blank was encountered
					// Check surrounding tiles, increment mineCount if one is found, be wary of valid indicies
					if((i > 0) && (j > 0)) 	{if((boardData[i - 1][j - 1].mine === false) && (boardData[i - 1][j - 1].revealed === false)) 	{return false;}} // Check top left
					if (i > 0) 				{if((boardData[i - 1][j].mine === false) 	&& (boardData[i - 1][j].revealed === false)) 		{return false;}} // Check top
					if((i > 0) && (j < 9)) 	{if((boardData[i - 1][j + 1].mine === false) && (boardData[i - 1][j + 1].revealed === false)) 	{return false;}} // Check top right
					if (j > 0) 				{if((boardData[i][j - 1].mine === false) 	&& (boardData[i][j - 1].revealed === false)) 		{return false;}} // Check left
					if (j < 9) 				{if((boardData[i][j + 1].mine === false) 	&& (boardData[i][j + 1].revealed === false)) 		{return false;}} // Check right
					if((i < 9) && (j > 0)) 	{if((boardData[i + 1][j - 1].mine === false) && (boardData[i + 1][j - 1].revealed === false)) 	{return false;}} // Check bottom left
					if (i < 9) 				{if((boardData[i + 1][j].mine === false) 	&& (boardData[i + 1][j].revealed === false)) 		{return false;}} // Check bottom
					if((i < 9) && (j < 9)) 	{if((boardData[i + 1][j + 1].mine === false) && (boardData[i + 1][j + 1].revealed === false)) 	{return false;}} // Check bottom right
				}
			}
		}
		return true;
	}
	
	// Method to reveal tiles that should be automatically revealed.
static fillBlanks(boardData){
    // Make the numbers for the board, iterating through every blank tile
    while(Minesweeper_V1.checkBlanks(boardData) === false) {
        for(let i = 0; i < 10; i++) {
            for(let j = 0; j < 10; j++) {
                if(boardData[i][j].mine === true) {continue;} // Skip if a mine is to be checked
                
                if((boardData[i][j].surroundingMines === 0) && (boardData[i][j].revealed === true)) { // A blank was encountered
                    // Check and reveal all adjacent tiles
                    if((i > 0) && (j > 0))   {if(boardData[i - 1][j - 1].mine === false)   {boardData[i - 1][j - 1].revealed = true; boardData[i - 1][j - 1].flagged = false;}} // Check top left
                    if (i > 0)               {if(boardData[i - 1][j].mine === false)       {boardData[i - 1][j].revealed = true;     boardData[i - 1][j].flagged = false;}} // Check top
                    if((i > 0) && (j < 9))   {if(boardData[i - 1][j + 1].mine === false)   {boardData[i - 1][j + 1].revealed = true; boardData[i - 1][j + 1].flagged = false;}} // Check top right
                    if (j > 0)               {if(boardData[i][j - 1].mine === false)       {boardData[i][j - 1].revealed = true;     boardData[i][j - 1].flagged = false;}} // Check left
                    if (j < 9)               {if(boardData[i][j + 1].mine === false)       {boardData[i][j + 1].revealed = true;     boardData[i][j + 1].flagged = false;}} // Check right
                    if((i < 9) && (j > 0))   {if(boardData[i + 1][j - 1].mine === false)   {boardData[i + 1][j - 1].revealed = true; boardData[i + 1][j - 1].flagged = false;}} // Check bottom left
                    if (i < 9)               {if(boardData[i + 1][j].mine === false)       {boardData[i + 1][j].revealed = true;     boardData[i + 1][j].flagged = false;}} // Check bottom
                    if((i < 9) && (j < 9))   {if(boardData[i + 1][j + 1].mine === false)   {boardData[i + 1][j + 1].revealed = true; boardData[i + 1][j + 1].flagged = false;}} // Check bottom right
                }
            }
        }
    }
    return boardData;
}

	// Helper method to convert a letter coordinate to a board index.
	static letterConverter(letter) {
		return letter.charCodeAt(0) - 'a'.charCodeAt(0);
	}

	// Helper method to create the game board.
	// Returns a 10x10 array of characters.
	static createGameBoard(mineDifficulty) {
		// Create a 10x10 board filled with '-'
		let board = [];
		for(let i = 0; i < 10; i++){
			board[i] = [];
			for(let j = 0; j < 10; j++){
				board[i][j] = '-';
			}
		}
		// Place mines randomly based on mineDifficulty
		let minesPlaced = 0;
		while(minesPlaced < mineDifficulty) {
			let row = Math.floor(Math.random() * 10);
			let col = Math.floor(Math.random() * 10);
			if (board[row][col] !== '*') {
				board[row][col] = '*';
				minesPlaced++;
			}
		}
		return board;
	}

	// Helper method to get board data (Tile objects) from the game board.
	// Returns a 10x10 array of Tile objects with surrounding mine counts populated.
	static getBoardData(currentGameBoard) {
		let boardData = [];
		// Create Tile objects based on currentGameBoard
		for(let i = 0; i < 10; i++){
			boardData[i] = [];
			for(let j = 0; j < 10; j++){
				let tile = new Minesweeper_V1.Tile();
				if(currentGameBoard[i][j] === '*') {
					tile.mine = true;
					tile.surroundingMines = -1;
				} else {
					tile.mine = false;
					tile.surroundingMines = 0;
				}
				boardData[i][j] = tile;
			}
		}
		// Calculate surrounding mines for non-mine tiles
		for(let i = 0; i < 10; i++){
			for(let j = 0; j < 10; j++){
				if(boardData[i][j].mine === true) { continue; }
				let count = 0;
				for(let x = -1; x <= 1; x++){
					for(let y = -1; y <= 1; y++){
						let ni = i + x;
						let nj = j + y;
						if(ni >= 0 && ni < 10 && nj >= 0 && nj < 10 && boardData[ni][nj].mine === true) {
							count++;
						}
					}
				}
				boardData[i][j].surroundingMines = count;
			}
		}
		return boardData;
	}

	// Helper method to print the player board.
	static printPlayerBoard(boardData, devMode) {
		console.log("\n   A B C D E F G H I J");
		for(let i = 0; i < 10; i++){
			let rowStr = i + " ";
			for(let j = 0; j < 10; j++){
				let tile = boardData[i][j];
				if(tile.flagged) {
					rowStr += " F";
				} else if(tile.revealed) {
					if(tile.mine) {
						rowStr += " *";
					} else if(tile.surroundingMines === 0) {
						rowStr += " ■"; // Changed from "  " to " ·" to indicate a revealed blank tile
					} else {
						rowStr += " " + tile.surroundingMines;
					}
				} else {
					rowStr += " □";
				}
			}
			// If devMode is enabled, append actual underlying data
			if(devMode) {
				rowStr += "    ";
				for(let j = 0; j < 10; j++){
					let tile = boardData[i][j];
					if(tile.mine) {
						rowStr += " *";
					} else {
						rowStr += " " + tile.surroundingMines;
					}
				}
			}
			console.log(rowStr);
		}
	}

	// Helper method to print the final board.
	static printFinalBoard(boardData) {
		console.log("\n   A B C D E F G H I J");
		for(let i = 0; i < 10; i++){
			let rowStr = i + " ";
			for(let j = 0; j < 10; j++){
				let tile = boardData[i][j];
				if(tile.mine) {
					rowStr += " *";
				} else if(tile.revealed) {
					if(tile.surroundingMines === 0) {
						rowStr += " ■"; // Changed from "  " to " ·" to indicate a revealed blank tile
					} else {
						rowStr += " " + tile.surroundingMines;
					}
				} else {
					rowStr += " □";
				}
			}
			console.log(rowStr);
		}
	}
}

// Execute the main method.
Minesweeper_V1.main();

module.exports = Minesweeper_V1;