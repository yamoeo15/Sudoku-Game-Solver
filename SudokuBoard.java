/* 

Emmanuel Yamoah
Dr. Thomas 
10/17/2017

						Sudoku algorithm 

Utilizing stack implementation and complex indexing, this algorithm traverses a sudoku board submits an entry into the GUI interface, and backchecks to change values to ensure a correct puzzle. 






/**
 *
 *  This class implements the SudokuBoard and methods that work on the board.
 *  In an MVC/MVP design this would be considered the "model" class.
 *
 */

import javax.swing.*;
import java.io.BufferedReader;
import java.io.*;
import java.util.*;

import static java.lang.Character.getNumericValue;

public class SudokuBoard extends Observable {

    public static final short BOARD_SIZE = 9; // Size of the board 9x9
    public static boolean solved = false;     // used to indicate when a soln found

    // the "board" is the most important instance field
    private byte[][] board;

    // constructor
    public SudokuBoard(String filename) {
        // allocate space for the "board"
        board = new byte[BOARD_SIZE][BOARD_SIZE];
        try {
            // open the puzzle file for reading a board setup
            File fileObj = new File(filename);
            FileReader fileReader = new FileReader(fileObj);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            StringBuffer stringBuffer = new StringBuffer();
            String line;
            // create a single String containing entire file contents
            while ((line = bufferedReader.readLine()) != null) {
                stringBuffer.append(line);
            }
            String input = new String(stringBuffer);

            // parse input String and populate array
            // Note that a '_' in the input file is stored as a 0
            String[] values = input.split(",");
            for (int i = 0; i < values.length; i++) {
                int indexRow = i / BOARD_SIZE;
                int indexCol = i % BOARD_SIZE;
                values[i] = values[i].trim();
                if (values[i].equals("_"))
                    values[i] = "0";
                setCellValue(indexRow, indexCol, Byte.parseByte(values[i]));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        setChanged();  // to update GUI
    }

    // Scan a board column to make sure there are no duplicates,
    // but allow 0's (empty cells)
    public boolean columnOK(int col) {
        int[] counts = new int[BOARD_SIZE + 1];
        for (int row = 0; row < BOARD_SIZE; row++) {
            byte entry = board[row][col];
            counts[entry]++;
            if (entry != 0 && counts[entry] > 1)
                return false;
        }
        return true;
    }

    // Scan a board row to make sure there are no duplicates,
    // but allow 0's (empty cells)
    public boolean rowOK(int row) {
        int[] counts = new int[BOARD_SIZE + 1];
        for (int col = 0; col < BOARD_SIZE; col++) {
            byte entry = board[row][col];
            counts[entry]++;
            if (entry != 0 && counts[entry] > 1)
                return false;
        }
        return true;
    }

    // Scan a 3 x 3 board subArray to make sure there are no duplicates,
    // but allow 0's (empty cells)
    public boolean subArrayOK(int row, int col) {
        int[] counts = new int[BOARD_SIZE + 1];
        int rowStart = (row / 3) * 3;
        int colStart = (col / 3) * 3;

        for (row = rowStart; row < rowStart + 3; row++)
            for (col = colStart; col < colStart + 3; col++) {
                byte entry = board[row][col];
                counts[entry]++;
                if (entry != 0 && counts[entry] > 1)
                    return false;
            }
        return true;
    }

    // check to see if all squares have been filled (are non-zero)
    public boolean boardComplete() {
        for (int row = 0; row < BOARD_SIZE; row++)
            for (int col = 0; col < BOARD_SIZE; col++)
                if (board[row][col] == 0)
                    return false;
        return boardLegal();
    }

    // check board for legal entries
    //            but allow for empty (0-valued) cells
    public boolean boardLegal() {
        for (int row = 0; row < BOARD_SIZE; row++)
            if (!rowOK(row)) return false;
        for (int col = 0; col < BOARD_SIZE; col++)
            if (!columnOK(col)) return false;
        for (int rowBlock = 0; rowBlock < 3; rowBlock++)
            for (int colBlock = 0; colBlock < 3; colBlock++)
                if (!subArrayOK(rowBlock * 3, colBlock * 3))
                    return false;
        return true;
    }

    // set cell value and notify GUI that change has been made
    public void setCellValue(int row, int col, byte c) {
        if (c >= 0 && c <= 9) {
            board[row][col] = c;
            setChanged();
            notifyObservers(new Cell(row, col, board[row][col]));
        }
    }

    /**
     * utility method for generating printable String representation
     * of board
     */
    public String toString() {
        StringBuffer result = new StringBuffer();
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                result = result.append(board[row][col] + " ");
            }
            result = result.append("\n");
        }
        result = result.append("\n\n");
        return result.toString();
    }

    // simple accessor
    public byte getCellValue(int row, int col) {
        return board[row][col];
    }

    // driver for solving a puzzle based on current state
    // uses a SwingWorker to handle threading issues
    public void solvePuzzle() {
        SwingWorker sw = new SwingWorker<Void, Void>() {
            protected Void doInBackground() {
                solvePuzzle(0, 0);
                return null;
            }
        };
        sw.execute();
    }

    // ******************** EDIT ME *******************************
    // The method for solving a puzzle starting at [row=0][col=0],
    public void solvePuzzle(int row, int col) {

        Stack<Cell> backtrack = new Stack<>();                                                      //create stack
        int i = 0;
        int colIndx = 0, rowIndx = 0, indexInt = 1;                                                 //initialize indices
        char val = 1;
        boolean check1, subArraybool = false, backCheckbool = false;                                 //initialize booleans for check

        for (rowIndx = 0; rowIndx < BOARD_SIZE; rowIndx++) {                                         //loop rows
            for (colIndx = 0; colIndx < BOARD_SIZE; colIndx++) {                                      //loop columns
                if (subArraybool && (colIndx > 0)) colIndx = colIndx - 1;                             // in case that column index updates
                if (board[rowIndx][colIndx] == 0 || backCheckbool) {                                //if to check if certain postion is empty or is backcheck bool is true;
                    if (!subArraybool) indexInt = 1;                                                 //nested for to check if subarray boolean is false
                    subArraybool = check1 = backCheckbool = false;
                    for (i = indexInt; i <= 9; i++) {                                               //iterate through number 1-9
                        setCellValue(rowIndx, colIndx, (byte) i);                                   //change cell to value of i
                        if (columnOK(colIndx) && rowOK(rowIndx) && subArrayOK(rowIndx, colIndx)) { //if statement to check for conflicts
                            backtrack.push(new Cell(rowIndx, colIndx, (byte) i));                   //pushing specific cell onto stsack
                            check1 = true;                                                          //check to see if backtracking is needed
                            break;                                                                  //break if no valid value can be found
                        }
                        else {
                            setCellValue(rowIndx, colIndx, (byte) 0);
                        }
                    }

                    if (!check1) {                                                                  // in case of no value being found
                        while (true) {

                            Cell poppedCell = backtrack.pop();                                      //then proceed to pop from the stack
                            rowIndx = poppedCell.getRow();                                           //get row and column data
                            colIndx = poppedCell.getCol();
                            val = poppedCell.getvalue();
                            indexInt = getNumericValue(val);


                            subArraybool = backCheckbool = true;                                    //set backcheck and subarray bool to true
                            if (indexInt != 9) {
                                break;
                            }

                        }
                        indexInt++;                                                                 //increment index
                    }
                }
            }
        }
        return;
    }
}