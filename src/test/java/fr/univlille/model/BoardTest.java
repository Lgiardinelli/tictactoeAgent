package fr.univlille.model;

import junit.framework.TestCase;

import java.lang.reflect.Field;

import static fr.univlille.model.Player.O;
import static fr.univlille.model.Player.X;

/**
 * @author Léo Giardinelli
 */
public class BoardTest extends TestCase {

    private Board board;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        board = new Board();
    }

    public void testNewBoardStartsInProgressWithXTurn() throws Exception {
        assertEquals(Board.GameState.IN_PROGRESS, board.getState());
        assertTrue(board.isInProgressMode());
        assertFalse(board.isInFinishedMode());
        assertEquals(X, board.getCurrentTurn());
        assertNull(board.getWinner());
        assertAllCellsEmpty();
    }

    public void testRestartResetsBoardStateAndTurn() throws Exception {
        setCell(0, 0, X);
        setCell(0, 1, O);
        setCell(1, 1, X);
        board.setCurrentTurn(O);
        board.setState(Board.GameState.FINISHED);

        board.restart();

        assertEquals(Board.GameState.IN_PROGRESS, board.getState());
        assertTrue(board.isInProgressMode());
        assertFalse(board.isInFinishedMode());
        assertEquals(X, board.getCurrentTurn());
        assertNull(board.getWinner());
        assertAllCellsEmpty();
    }

    public void testMarkPlacesCurrentPlayerTokenAndFlipsTurnAfterNonWinningMove() throws Exception {
        board.mark(0, 0);

        assertEquals(X, getCellValue(0, 0));
        assertEquals(Board.GameState.IN_PROGRESS, board.getState());
        assertNull(board.getWinner());
        assertEquals(O, board.getCurrentTurn());
    }

    public void testMarkDoesNothingWhenCellAlreadyPlayed() throws Exception {
        board.mark(0, 0);
        board.mark(0, 0);

        assertEquals(X, getCellValue(0, 0));
        assertEquals(O, board.getCurrentTurn());
        assertEquals(Board.GameState.IN_PROGRESS, board.getState());
    }

    public void testMarkDoesNothingWhenMoveIsOutOfBounds() throws Exception {
        board.mark(-1, 0);
        board.mark(0, -1);
        board.mark(3, 0);
        board.mark(0, 3);
        board.mark(3, 3);

        assertEquals(X, board.getCurrentTurn());
        assertAllCellsEmpty();
        assertEquals(Board.GameState.IN_PROGRESS, board.getState());
    }

    public void testMarkDoesNothingWhenGameAlreadyFinished() throws Exception {
        board.mark(0, 0);
        board.mark(1, 0);
        board.mark(0, 1);
        board.mark(1, 1);
        board.mark(0, 2);

        board.mark(2, 2);

        assertEquals(X, board.getWinner());
        assertTrue(board.isInFinishedMode());
        assertEquals(Board.GameState.FINISHED, board.getState());
        assertEquals(X, getCellValue(0, 0));
        assertEquals(X, getCellValue(0, 1));
        assertEquals(X, getCellValue(0, 2));
        assertNull(getCellValue(2, 2));
    }

    public void testMarkCreatesHorizontalWin() throws Exception {
        board.setCurrentTurn(X);
        setCell(0, 0, X);
        setCell(0, 1, X);

        board.mark(0, 2);

        assertEquals(X, board.getWinner());
        assertEquals(Board.GameState.FINISHED, board.getState());
        assertTrue(board.isInFinishedMode());
        assertEquals(X, board.getCurrentTurn());
    }

    public void testMarkCreatesVerticalWin() throws Exception {
        board.setCurrentTurn(X);
        setCell(0, 0, X);
        setCell(1, 0, X);

        board.mark(2, 0);

        assertEquals(X, board.getWinner());
        assertEquals(Board.GameState.FINISHED, board.getState());
        assertEquals(X, board.getCurrentTurn());
    }

    public void testMarkCreatesMainDiagonalWin() throws Exception {
        board.setCurrentTurn(X);
        setCell(0, 0, X);
        setCell(1, 1, X);

        board.mark(2, 2);

        assertEquals(X, board.getWinner());
        assertEquals(Board.GameState.FINISHED, board.getState());
        assertEquals(X, board.getCurrentTurn());
    }

    public void testMarkCreatesAntiDiagonalWin() throws Exception {
        board.setCurrentTurn(X);
        setCell(0, 2, X);
        setCell(1, 1, X);

        board.mark(2, 0);

        assertEquals(X, board.getWinner());
        assertEquals(Board.GameState.FINISHED, board.getState());
        assertEquals(X, board.getCurrentTurn());
    }

    public void testManualSettersAndStatePredicatesWork() {
        board.setCurrentTurn(O);
        assertEquals(O, board.getCurrentTurn());

        board.setState(Board.GameState.FINISHED);
        assertEquals(Board.GameState.FINISHED, board.getState());
        assertTrue(board.isInFinishedMode());
        assertFalse(board.isInProgressMode());

        board.setState(Board.GameState.IN_PROGRESS);
        assertEquals(Board.GameState.IN_PROGRESS, board.getState());
        assertTrue(board.isInProgressMode());
        assertFalse(board.isInFinishedMode());
    }

    private void assertAllCellsEmpty() throws Exception {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                assertNull("Cell [" + row + ", " + col + "] should be empty", getCellValue(row, col));
            }
        }
    }

    private void setCell(int row, int col, Player value) throws Exception {
        Cell[][] cells = getCells();
        cells[row][col].setValue(value);
    }

    private Player getCellValue(int row, int col) throws Exception {
        return getCells()[row][col].getValue();
    }

    private Cell[][] getCells() throws Exception {
        Field cellsField = Board.class.getDeclaredField("cells");
        cellsField.setAccessible(true);
        return (Cell[][]) cellsField.get(board);
    }
}
