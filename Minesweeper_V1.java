// CSE-1325 Project
// Due 28 November 2024
// Project Topic: Java Minesweeper

// Team Members:
// 	Hilary Mbotchak
// 	David Denny
// 	David Madrigal

package Minesweeper_V1;

import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

// Have fun!
public class Minesweeper_V1 
{
	// Class to hold the attributes of a tile on the gameBoard.
	// The playerBoard will be an array of these objects.
	public static class Tile{
		boolean revealed = false;		// True when the player reveals the tile.
		boolean mine = false;			// True if there is a mine on the tile.
		boolean flagged = false;		// True when the player flags the tile.
		int		surroundingMines = 0;	// Holds the number of mines surrounding the tile.
										//    Will be -1 if the tile is a mine itself.
	}
	
	// Class to hold the attributes of each player on the leaderboard.
	public static class Player{
		String name = "AAA";			// Default name.
		double bestTime = 9999;			// Default best time.
	}
	
	// Main method is the highest-level method, running the main menu and playing the game.
	public static void main(String args[])
	{	
		System.out.print("\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n");
		System.out.print("=======================\nMinesweeper (Version 1)\n=======================");
		System.out.print("\n\nMenu:\t1.) Play\n\t2.) Quit\n\t3.) Leaderboard\n\t4.) Change Difficulty\n\t5.) How To Play\n\nEnter Your Selection: ");
		
		Scanner input = new Scanner(System.in);
		String MenuSelection = input.next();
		int mineDifficulty = 10; // Default difficulty of 10 mines = normal
		
		while(!MenuSelection.equalsIgnoreCase("Quit")) {
			// Ensure the leaderboard is up-to-date.
			ArrayList<Player> leaderboard = readLeaderboard();
			MenuSelection.toLowerCase();
			switch(MenuSelection) {
			
			// Parse the user's input from their MenuSelction.
			case "1":
			case "play":
				// Start the timer
				long startTime = System.nanoTime();
				
                int gameResult = playMinesweeper(input, mineDifficulty); // Start the game!
                
                if(gameResult == 0) { // User quit mid-game
                	System.out.print("Quitting...\n");
                }
                if(gameResult == 1) { // User lost the game
                	System.out.print("==================\nGAME OVER YOU LOSE\n==================\n");
                }
                if(gameResult == 2) { // User won the game!
                	System.out.print("==================\nGAME OVER YOU WIN!\n==================\n");
                	// End the timer to store in the leaderboard
                	long endTime = System.nanoTime();
                	double userTime = (endTime - startTime) / 1_000_000_000.0;
                	System.out.print("\nYou won in " + userTime + " seconds!");
                	System.out.print("\n\nEnter your name (3 letters): ");
                	String userName = input.next();
                	if(userName.length() != 3) {
                		while(userName.length() != 3) {
                			System.out.print("\nImproper name formatting! Please re-enter: ");
                			System.out.print("\nEnter your name (3 letters): ");
                        	userName = input.next();
                		}
                	}
                	leaderboard = addPlayer(userName.toUpperCase(), userTime, leaderboard);
                }
                break;
			case "2":
				MenuSelection = "quit";
				break;
			// The leaderboard option to print the current Leaderboard.
			case "3":
			case "leaderboard":
				// Ensure the leaderboard file exists to avoid an exception.
		        File file = new File("leaderboard.txt");
		        if (!file.exists()) {
		        	System.out.println("\nThere is currently no recorded data.\nPlay some games!");
		            break;
		        }
		        // Else read the leaderboard.
                System.out.println("\nHere are the fasted recorded times:");
                leaderboard = readLeaderboard();
                
                System.out.print("\nRANK\tNAME\tTIME (s)");
                System.out.print("\n--------------------------\n");
                
                int i = 1;
                for (Player playerToCompare : leaderboard) {
                	System.out.print(i + "\t" + playerToCompare.name + "\t" + String.valueOf(playerToCompare.bestTime));
                	System.out.print("\n");
                	i++;
    	        }
                break;
            // The changeDifficulty option to change the difficulty if desired.
			case "4":
			case "change difficulty":
				mineDifficulty = changeDifficulty(input, mineDifficulty);
				break;
			case "5":
			case "how to play":
				printRules();
				break;
            // Default case to display unknown command.
            // This can be replaced with an exception throw/catch if desired.
            default:
            	System.out.println("\nUnknown Command");
            	break;
			}
			// Break completely if the user quits from the main menu.
			if(MenuSelection.equals("quit")) {break;}
			System.out.print("\nMenu:\t1.) Play\n\t2.) Quit\n\t3.) Leaderboard\n\t4.) Change Difficulty\n\t5.) How To Play\n\nEnter Your Selection: ");
			MenuSelection = input.next();
		}
		System.out.print("\nQuitting...");
		input.close();
	}
	
	// This is the main method to play the game
	// Returns three seperate values for each end-game scenario:
	// 		Returns 0 if the user quit, or anther exception was encountered
	//		Returns 1 if the user lost
	//		Returns 2 if the user won
	public static int playMinesweeper(Scanner input, int mineDifficulty) {

		System.out.println("\nGenerating New Board...\n");
        
		// Needed work variables.
        boolean gameOver = false;	// This variable determines if the game is still running.
        boolean devMode = false;	// This variable determines if devMode is enabled or disabled.
        boolean firstTurn = true;	// This variable determines if it is the user's first turn.
        String TileSelection;		// This variable stores the user command.
        
        // Create the gameBoard.
        // Our work array of objects is made from getBoardData().
        char[][] currentGameBoard = createGameBoard(mineDifficulty);
        Tile[][] boardData = getBoardData(currentGameBoard);
        
        // Loop through turns while the game is still active.
        while(gameOver == false) {
        	
        	// Check if the player has won before allowing the next turn.
        	if(checkForWin(boardData, mineDifficulty)) {
        		printFinalBoard(boardData);
        		System.out.print("\n");
        		return 2;
        	}
        	
        	// Reprint the updated playerBoard with the results of the previous turn.
        	// Display the options the player has on this turn.
        	// Also prints the boardData if devMode is enabled.
        	printPlayerBoard(boardData, devMode);
        	System.out.print("\nType in a coordinate to Reveal or\nFlag like this: \"RB6\" or \"FI8\".");
        	System.out.print("\nOr type \"Quit\" to exit to the menu.");
        	System.out.print("\nOr type \"Dev\" to toggle developer mode.\n\nReveal or flag a tile: ");
        	// Get the player's next turn!
        	TileSelection = input.next();
        	System.out.print("\n");
        	
        	// Parsing the command with a char array.
        	// Command will default to lowercase for ease of parsing.
        	TileSelection = TileSelection.toLowerCase();
        	char[] TileSelectionParser = TileSelection.toCharArray();
        	
        	// This is the quit condition. It will simply exit the game, return a seperate integer.
        	if(TileSelection.equalsIgnoreCase("quit")) {
        		return 0;
        	}
        	
        	// This is the devMode condition. It will toggle devMode to print the boardData when printing.
        	else if((TileSelection.equalsIgnoreCase("dev")) || (TileSelection.equalsIgnoreCase("developer")) ) {
        		System.out.print("Toggling developer mode...\n\n");
        		devMode = !devMode;
        	}
        	
        	// Check that the command is valid.
        	// Many of the following if statements simply check for invalid commands.
        	// Could replace this with exception throws and catches if desired.
        	else if((TileSelectionParser.length != 3) || ((TileSelectionParser[0] != 'f') && (TileSelectionParser[0] != 'r'))) {
        		System.out.print("Invalid format for selection!\n\n");
        		continue;
        	}
        	// Check that the first tile coordinate is valid.
        	else if(!(TileSelectionParser[1] >= 'a' && TileSelectionParser[1] <= 'j')) {
        		System.out.print("Invalid format for selection!\n\n");
        		continue;
        	}
        	// Check that the second tile coordinate is valid.
        	else if(!(TileSelectionParser[2] >= '0' && TileSelectionParser[2] <= '9')) {
        		System.out.print("Invalid format for selection!\n\n");
        		continue;
        	}
        	else {
        		// If the code reaches here, then the player has entered a valid command!
        		// Reveal a tile.
        		if(TileSelectionParser[0] == 'r') {
        			// Prevent revealing a flagged tile.
        			// For reference, the "TileSelectionParser[2] - '0'" shown here simply converts the char to an int.
        			if(boardData[(TileSelectionParser[2] - '0')][letterConverter(TileSelectionParser[1])].flagged == true) {
        				continue;
        			}
        			// This is the lose condition. If a player reveals a mine, they lose!
        			// Generate a new board if the player tripped a mine on their first turn.
        			if(boardData[(TileSelectionParser[2] - '0')][letterConverter(TileSelectionParser[1])].mine == true) {
        				// Prevent a user from losing on their first turn.
        				if(firstTurn) {
        					System.out.print("You tripped a mine on your first turn!\nGenerating new board...\n\n");
        					while(boardData[(TileSelectionParser[2] - '0')][letterConverter(TileSelectionParser[1])].mine == true) {
        						currentGameBoard = createGameBoard(mineDifficulty);
            			        boardData = getBoardData(currentGameBoard);
        					}
        				}
        				else {
        					printFinalBoard(boardData);
            				System.out.print("\n");
            				return 1;
        				}
        			}
        			boardData[(TileSelectionParser[2] - '0')][letterConverter(TileSelectionParser[1])].revealed = true;
            		boardData = fillBlanks(boardData);
            		firstTurn = false;
        		}
        		// Flag a tile.
        		if(TileSelectionParser[0] == 'f') {
        			// Prevent flagging a revealed tile -- there's no point.
        			if(boardData[(TileSelectionParser[2] - '0')][letterConverter(TileSelectionParser[1])].revealed == true) {
        				continue;
        			}
        			// Toggle the desired flag coordinate.
        			boardData[(TileSelectionParser[2] - '0')][letterConverter(TileSelectionParser[1])].flagged =
        					!boardData[(TileSelectionParser[2] - '0')][letterConverter(TileSelectionParser[1])].flagged;
        		}
        	}
        }      
        return 0;
	}
	
	// Method to print the rules of Minesweeper
	public static void printRules() {
		System.out.print("\n===========\nHOW TO PLAY\n===========\n");
		System.out.print("\nIn Minesweeper, the goal is to clear a grid of hidden");
		System.out.print("\ntiles without uncovering any mines. Each uncovered");
		System.out.print("\ntiles shows a number indicating how many mines are");
		System.out.print("\nadjacent to it, helping you deduce where mines are likely");
		System.out.print("\nlocated. Use this information to flag tiles you believe");
		System.out.print("\ncontain mines, which prevents accidentally uncovering them.");
		System.out.print("\nOnce you've flagged all the mines and uncovered the rest");
		System.out.print("\nof the grid, you win the game! If you uncover a mine,");
		System.out.print("\nthe game ends.");
		System.out.print("\n\nFor this implementation of the game, simply uncover");
		System.out.print("\nmines by entering the tile's coordinates when prompted!\n");
	}
	
	// Method to change the difficulty for the games.
	// Returns the number of mines corresponding to the set difficulty (5, 10, or 20 mines)
	public static int changeDifficulty(Scanner input, int mineDifficulty) {
		if(mineDifficulty == 5) {
			System.out.print("\nThe difficulty is currently set to EASY.");
		}
		else if(mineDifficulty == 10) {
			System.out.print("\nThe difficulty is currently set to NORMAL.");
		}
		else if(mineDifficulty == 20) {
			System.out.print("\nThe difficulty is currently set to HARD.");
		}
		
		System.out.print("\n\nChange your difficulty here!\nHere are your options:\n");
		System.out.print("\nMenu:\t1.) Easy   (5 mines)\n\t2.) Normal (10 mines)\n\t3.) Hard   (20 mines)\n\nEnter Your Selection: ");
		
		String MenuSelection = input.next();
		MenuSelection = MenuSelection.toLowerCase();
		
		switch(MenuSelection) {
		case "1":
		case "easy":
			System.out.print("\nSetting the difficulty to EASY difficulty...\n");
			mineDifficulty = 5;
			break;
		case "2":
		case "normal":
			System.out.print("\nSetting the difficulty to NORMAL difficulty...\n");
			mineDifficulty = 10;
			break;
		case "3":
		case "hard":
			System.out.print("\nSetting the difficulty to HARD difficulty...\n");
			mineDifficulty = 20;
			break;
		default:
			System.out.print("\nUnknown difficulty entered. Not changing difficulty...\n");
			break;
		}
		return mineDifficulty;
	}
	
	// This method updates the current leaderboard ArrayList by reading the leaderboard.txt file.
	public static ArrayList<Player> readLeaderboard(){
		ArrayList<Player> leaderboard = new ArrayList<>();
		
		// Ensure the leaderboard file exists to avoid an exception.
        File file = new File("leaderboard.txt");
        if (!file.exists()) {
            return leaderboard;
        }
		
		// Let us print modified file
		try {
			BufferedReader in = new BufferedReader(new FileReader("leaderboard.txt"));
			String readerString;
			// Till there is content in string
			// condition holds true
			while ((readerString = in.readLine()) != null) {
				//System.out.println(readerString);
				Player playerInst = new Player();
				playerInst.name = readerString;
				playerInst.bestTime = Double.valueOf(in.readLine());
				leaderboard.add(playerInst);
			}
			in.close();
		}
		// Catch block to handle IO exceptions
		catch (IOException e) {
			System.out.println("Exception Occurred" + e);
		}
		return leaderboard;
	}
	
	// This method adds a winning user's score to the leaderboard, and updates it in-file.
	// This method reads the current leaderboard ArrayList and writes to leaderboard.txt.
	// The Leaderboard is stored locally with the leaderboard.txt file in the programs default directory.
	public static ArrayList<Player> addPlayer(String userName, double userTime, ArrayList<Player> leaderboard) {
		
		// Adding the player's winning data to the leaderboard.
		Player playerInst = new Player();
		playerInst.name = userName;
		playerInst.bestTime = userTime;
		
		int i = 0;
		boolean added = false;  // Track if the player was added

		// Add a player at their specific, ordered index
		for (Player playerToCompare : leaderboard) {
		    if (playerInst.bestTime < playerToCompare.bestTime) {
		        leaderboard.add(i, playerInst);
		        added = true;
		        break;
		    }
		    i++;
		}

		// If player is the slowest, add to the end
		if (!added) {
		    leaderboard.add(playerInst);
		}

		String fileName = "leaderboard.txt";
		// Try block to check for exceptions
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(fileName));
			// Writing the updated leaderboard to the leaderboard file
			for (Player playerToCompare : leaderboard) {
				out.write(playerToCompare.name);
				out.write("\n");
				out.write(String.valueOf(playerToCompare.bestTime));
				out.write("\n");
	        }
			out.close();
		}
		
		// Catch block to handle exceptions
		catch (IOException e) {
			// Display message when error occurs
			System.out.println("Exception Occurred" + e);
		}
		return leaderboard;
	}
	
	// Method to check for win. If 100 - mineDifficulty tiles are revealed,
	// and none are mines, then the user has won!
	public static boolean checkForWin(Tile boardData[][], int mineDifficulty) {
		int tileCounter = 0;
		for(int i = 0; i < 10; i++) {
			for(int j = 0; j < 10; j++) {
				if((boardData[i][j].revealed == true) && (boardData[i][j].mine == false)) {
					tileCounter++;
				}
			}
		}
		if(tileCounter == (100 - mineDifficulty)) {
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
	public static boolean checkBlanks(Tile boardData[][]){
		for(int i = 0; i < 10; i++) {
			for(int j = 0; j < 10; j++) {
				if(boardData[i][j].mine == true) {continue;} // Skip if a mine is to be checked
				
				if((boardData[i][j].surroundingMines == 0) && (boardData[i][j].revealed == true)) { // A blank was encountered
					// Check surrounding tiles, increment mineCount if one is found, be wary of valid indicies
					if((i > 0) && (j > 0)) 	{if((boardData[i - 1][j - 1].mine == false) && (boardData[i - 1][j - 1].revealed == false)) 	{return false;}} // Check top left
					if (i > 0) 				{if((boardData[i - 1][j].mine == false) 	&& (boardData[i - 1][j].revealed == false)) 		{return false;}} // Check top
					if((i > 0) && (j < 9)) 	{if((boardData[i - 1][j + 1].mine == false) && (boardData[i - 1][j + 1].revealed == false)) 	{return false;}} // Check top right
					if (j > 0) 				{if((boardData[i][j - 1].mine == false) 	&& (boardData[i][j - 1].revealed == false)) 		{return false;}} // Check left
					if (j < 9) 				{if((boardData[i][j + 1].mine == false) 	&& (boardData[i][j + 1].revealed == false)) 		{return false;}} // Check right
					if((i < 9) && (j > 0)) 	{if((boardData[i + 1][j - 1].mine == false) && (boardData[i + 1][j - 1].revealed == false)) 	{return false;}} // Check bottom left
					if (i < 9) 				{if((boardData[i + 1][j].mine == false) 	&& (boardData[i + 1][j].revealed == false)) 		{return false;}} // Check bottom
					if((i < 9) && (j < 9)) 	{if((boardData[i + 1][j + 1].mine == false) && (boardData[i + 1][j + 1].revealed == false)) 	{return false;}} // Check bottom right
				}
			}
		}
		return true;
	}
	
	// Method to reveal tiles that should be automatically revealed.
	public static Tile[][] fillBlanks(Tile boardData[][]){
		// Make the numbers  for the board, iterating through every blank tile
		while(checkBlanks(boardData) == false) {
			for(int i = 0; i < 10; i++) {
				for(int j = 0; j < 10; j++) {
					if(boardData[i][j].mine == true) {continue;} // Skip if a mine is to be checked
					
					if((boardData[i][j].surroundingMines == 0) && (boardData[i][j].revealed == true)) { // A blank was encountered
						if((i > 0) && (j > 0)) 	{if(boardData[i - 1][j - 1].mine == false) 	{boardData[i - 1][j - 1].revealed = true;	boardData[i - 1][j - 1].flagged = false;}} // Check top left
						if (i > 0) 				{if(boardData[i - 1][j].mine == false) 		{boardData[i - 1][j].revealed = true;		boardData[i - 1][j].flagged = false;}} // Check top
						if((i > 0) && (j < 9)) 	{if(boardData[i - 1][j + 1].mine == false) 	{boardData[i - 1][j + 1].revealed = true;	boardData[i - 1][j + 1].flagged = false;}} // Check top right
						if (j > 0) 				{if(boardData[i][j - 1].mine == false) 		{boardData[i][j - 1].revealed = true;		boardData[i][j - 1].flagged = false;}} // Check left
						if (j < 9) 				{if(boardData[i][j + 1].mine == false) 		{boardData[i][j + 1].revealed = true;		boardData[i][j + 1].flagged = false;}} // Check right
						if((i < 9) && (j > 0)) 	{if(boardData[i + 1][j - 1].mine == false) 	{boardData[i + 1][j - 1].revealed = true;	boardData[i + 1][j - 1].flagged = false;}} // Check bottom left
						if (i < 9) 				{if(boardData[i + 1][j].mine == false) 		{boardData[i + 1][j].revealed = true;		boardData[i + 1][j].flagged = false;}} // Check bottom
						if((i < 9) && (j < 9)) 	{if(boardData[i + 1][j + 1].mine == false) 	{boardData[i + 1][j + 1].revealed = true;	boardData[i + 1][j + 1].flagged = false;}} // Check bottom right
					}
				}
			}
		}	
		return(boardData);
	}
	
	// Method to convert letter to number for command.
	// This is used to get the column coordinate from the player.
	// For example, RG5 reveals gameBoard[5][6] tile.
	public static int letterConverter(char letter) {
		if(letter == 'a') {return 0;}
		if(letter == 'b') {return 1;}
		if(letter == 'c') {return 2;}
		if(letter == 'd') {return 3;}
		if(letter == 'e') {return 4;}
		if(letter == 'f') {return 5;}
		if(letter == 'g') {return 6;}
		if(letter == 'h') {return 7;}
		if(letter == 'i') {return 8;}
		if(letter == 'j') {return 9;}
		return -1;
	}
	
	// This method prints the final game board when the player
	// either wins or loses the current game.
	public static void printFinalBoard(Tile boardData[][])
	{
		System.out.print("   ===================\n");
		System.out.print("       Final Board    \n");
		System.out.print("   ===================\n");
		System.out.print("   A B C D E F G H I J\n");
		System.out.print("   ___________________\n");
        for (int i = 0; i < boardData.length; i++){
            // Loop through all elements of current row
        	System.out.print(i + " |");
            for (int j = 0; j < boardData[i].length; j++){
               	if		(boardData[i][j].surroundingMines == 0) {System.out.print("■ ");}
               	else if	(boardData[i][j].mine == true) {System.out.print("X ");}
                else 	{System.out.print(boardData[i][j].surroundingMines + " ");}
            }
            System.out.print("\n");
        }
    }
	
	// Method to print the playerBoard, which is an object array.
	// It prints what the player is allowed to see, including devMode.
	public static void printPlayerBoard(Tile boardData[][], boolean devMode)
    {
		if(devMode) {
			System.out.print("   ===================\t     ===================\n");
			System.out.print("      Player Board:   \t       Developer Board: \n");
			System.out.print("   ===================\t     ===================\n");
		}
        // Loop through all rows
		System.out.print("   A B C D E F G H I J");
		if(devMode) {
			System.out.print("\t     A B C D E F G H I J");
		}
		System.out.print("\n   ___________________");
		if(devMode) {
			System.out.print("\t     ___________________");
		}
		System.out.print("\n");
        for (int i = 0; i < boardData.length; i++){
            // Loop through all elements of current row
        	System.out.print(i + " |");
            for (int j = 0; j < boardData[i].length; j++){
            	if(boardData[i][j].flagged == true) {
            		System.out.print("X ");
            	}
            	else if(boardData[i][j].revealed) {
                	if		(boardData[i][j].surroundingMines == 0) {System.out.print("■ ");}
                	else if	(boardData[i][j].mine == true) {System.out.print("M ");}
                	else 	{System.out.print(boardData[i][j].surroundingMines + " ");}
                }
                else {
                	System.out.print("□ ");
                }
            }
            
            // Display the boardData if devMdoe is enabled.
            if(devMode) {
            	// Loop through all elements of current row
            	System.out.print("\t  " + i + " |");
                for (int j = 0; j < boardData[i].length; j++){
                	if		(boardData[i][j].surroundingMines == 0) {System.out.print("■ ");}
                   	else if	(boardData[i][j].mine == true) {System.out.print("X ");}
                    else 	{System.out.print(boardData[i][j].surroundingMines + " ");}
                }
            } 
            System.out.print("\n");
        }
    }

	// Takes the currentGameBoard, and creates a 2D array of
	// tile objects. Converts char array to object array.
	public static Tile[][] getBoardData(char currentGameBoard[][]){
		Tile[][] boardData = new Tile[10][10];
		for(int i = 0; i < 10; i++) {
			for(int j = 0; j < 10; j++) {
				boardData[i][j] = new Tile();
				
				if(currentGameBoard[i][j] == 'M') {
					boardData[i][j].mine = true;
					boardData[i][j].surroundingMines = -1;
				}
				else if(currentGameBoard[i][j] == '■') {
					boardData[i][j].surroundingMines = 0;
				}
				else {
					// Tile has surrounding mines
					boardData[i][j].surroundingMines = (currentGameBoard[i][j] - '0');
				}
			}
		}
		return boardData;
	}
	
	//	Method to establish the gameBoard, which is a charArray.
	//	This charArray will also be printed when devMode is on.
	public static char[][] createGameBoard(int mineDifficulty)
	{	
		//	LEGEND:
		//	M = Mine
		//	■ = Blank Space, no immediate mines
		//	1-8 = Blank Tile with 1-8 mines around it
		
		// Create an array to represent our gameBoard
		char[][] currentGameBoard = new char[10][10];
				
		// Clear the gameBoard
		for(int i = 0; i < 10; i++) {
			for(int j = 0; j < 10; j++) {
				currentGameBoard[i][j] = '■';
			}
		}
		
		// Randomly place mineDifficulty mines
		// create instance of Random class
        Random rand = new Random();
		int randomRow, randomCol;
		int numMines = 0;
		
		while(numMines < mineDifficulty) { // mineDifficulty mines to be placed
			randomRow = rand.nextInt(10);
			randomCol = rand.nextInt(10);
			// Make sure the space to place the mine is blank before placing
			if(currentGameBoard[randomRow][randomCol] == '■') {
				currentGameBoard[randomRow][randomCol] = 'M';
				numMines++; // Increment the number of mines
			}
		}
		
		// Make the numbers  for the board, iterating through every blank tile
		for(int i = 0; i < 10; i++) {
			for(int j = 0; j < 10; j++) {
				int mineCounter = 0; // Clear the mineCounter before checking a new tile
				if(currentGameBoard[i][j] == 'M') {continue;} // Skip if a mine is to be checked
				
				// Check surrounding tiles, increment mineCount if one is found, be wary of valid indicies
				if((i > 0) && (j > 0)) 	{if(currentGameBoard[i - 1][j - 1] == 'M') 	{mineCounter++;}} // Check top left
				if (i > 0) 				{if(currentGameBoard[i - 1][j] == 'M') 		{mineCounter++;}} // Check top
				if((i > 0) && (j < 9)) 	{if(currentGameBoard[i - 1][j + 1] == 'M') 	{mineCounter++;}} // Check top right
				if (j > 0) 				{if(currentGameBoard[i][j - 1] == 'M') 		{mineCounter++;}} // Check left
				if (j < 9) 				{if(currentGameBoard[i][j + 1] == 'M') 		{mineCounter++;}} // Check right
				if((i < 9) && (j > 0)) 	{if(currentGameBoard[i + 1][j - 1] == 'M') 	{mineCounter++;}} // Check bottom left
				if (i < 9) 				{if(currentGameBoard[i + 1][j] == 'M') 		{mineCounter++;}} // Check bottom
				if((i < 9) && (j < 9)) 	{if(currentGameBoard[i + 1][j + 1] == 'M') 	{mineCounter++;}} // Check bottom right
				
				// Update the currentGameBoard as needed
				if(mineCounter > 0) {
					if(mineCounter == 1) {currentGameBoard[i][j] = '1';}
					else if(mineCounter == 2) {currentGameBoard[i][j] = '2';}
					else if(mineCounter == 3) {currentGameBoard[i][j] = '3';}
					else if(mineCounter == 4) {currentGameBoard[i][j] = '4';}
					else if(mineCounter == 5) {currentGameBoard[i][j] = '5';}
					else if(mineCounter == 6) {currentGameBoard[i][j] = '6';}
					else if(mineCounter == 7) {currentGameBoard[i][j] = '7';}
					else if(mineCounter == 8) {currentGameBoard[i][j] = '8';}
				}
			}
		}	
		return currentGameBoard;
	}
}
