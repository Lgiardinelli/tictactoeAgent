package fr.univlille.model;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class BoardTest {

    private Board board;

    @BeforeEach
    void setUp() {
        board = new Board();
    }

    @Test
    void constructor_initializesNewGameInProgress() {
        assertNull(board.getWinner());
        assertEquals(Player.X, board.getCurrentTurn());
        assertEquals("IN_PROGRESS", board.getState().name());
        assertTrue(board.isInProgressMode());
        assertFalse(board.isInFinishedMode());
        assertBoardCellsAreEmpty();
    }

    @Test
    void restart_clearsBoardAndResetsTurnToX() {
        board.mark(0, 0);
        board.mark(0, 1);

        board.restart();

        assertNull(board.getWinner());
        assertEquals(Player.X, board.getCurrentTurn());
        assertEquals("IN_PROGRESS", board.getState().name());
        assertTrue(board.isInProgressMode());
        assertFalse(board.isInFinishedMode());
        assertBoardCellsAreEmpty();
    }

    @Test
    void mark_validMove_placesTokenAndAlternatesTurn() {
        board.mark(1, 1);

        assertEquals(Player.X, readCellValue(1, 1));
        assertEquals(Player.O, board.getCurrentTurn());
        assertNull(board.getWinner());
        assertEquals("IN_PROGRESS", board.getState().name());
    }

    @ParameterizedTest
    @MethodSource("validMoves")
    void mark_acceptsAnyValidCoordinate(int row, int col) {
        board.mark(row, col);

        assertEquals(Player.X, readCellValue(row, col));
        assertEquals(Player.O, board.getCurrentTurn());
        assertNull(board.getWinner());
        assertEquals("IN_PROGRESS", board.getState().name());
    }

    private static Stream<Arguments> validMoves() {
        return Stream.of(
            Arguments.of(0, 0),
            Arguments.of(0, 1),
            Arguments.of(0, 2),
            Arguments.of(1, 0),
            Arguments.of(1, 1),
            Arguments.of(1, 2),
            Arguments.of(2, 0),
            Arguments.of(2, 1),
            Arguments.of(2, 2)
        );
    }

    @Test
    void mark_invalidMove_outOfBounds_doesNothing() {
        board.mark(-1, 0);
        board.mark(0, -1);
        board.mark(3, 0);
        board.mark(0, 3);

        assertBoardCellsAreEmpty();
        assertEquals(Player.X, board.getCurrentTurn());
        assertEquals("IN_PROGRESS", board.getState().name());
        assertNull(board.getWinner());
    }

    @Test
    void mark_invalidMove_onAlreadyPlayedCell_doesNothing() {
        board.mark(0, 0);

        board.mark(0, 0);

        assertEquals(Player.X, readCellValue(0, 0));
        assertEquals(Player.O, board.getCurrentTurn());
        assertEquals("IN_PROGRESS", board.getState().name());
        assertNull(board.getWinner());
    }

    @Test
    void mark_whenPlayerWins_setsWinnerAndFinishedState() {
        board.mark(0, 0);
        board.mark(1, 0);
        board.mark(0, 1);
        board.mark(1, 1);

        board.mark(0, 2);

        assertEquals(Player.X, board.getWinner());
        assertEquals(Player.X, board.getCurrentTurn());
        assertEquals("FINISHED", board.getState().name());
        assertTrue(board.isInFinishedMode());
        assertFalse(board.isInProgressMode());
    }

    @Test
    void mark_publicWinPatterns_detectsEveryWinningLine() {
        board.mark(0, 0);
        board.mark(1, 0);
        board.mark(0, 1);
        board.mark(1, 1);
        board.mark(0, 2);
        assertWinner(Player.X, 0, 2);

        board = new Board();
        board.mark(0, 0);
        board.mark(0, 1);
        board.mark(1, 0);
        board.mark(1, 1);
        board.mark(2, 0);
        assertWinner(Player.X, 2, 0);

        board = new Board();
        board.mark(0, 0);
        board.mark(0, 1);
        board.mark(1, 1);
        board.mark(1, 0);
        board.mark(2, 2);
        assertWinner(Player.X, 2, 2);

        board = new Board();
        board.mark(0, 2);
        board.mark(0, 0);
        board.mark(1, 1);
        board.mark(1, 0);
        board.mark(2, 0);
        assertWinner(Player.X, 2, 0);
    }

    @Test
    void mark_whenGameAlreadyFinished_ignoresFurtherMoves() {
        board.mark(0, 0);
        board.mark(1, 0);
        board.mark(0, 1);
        board.mark(1, 1);
        board.mark(0, 2);

        board.mark(2, 2);

        assertEquals(Player.X, board.getWinner());
        assertEquals("FINISHED", board.getState().name());
        assertNull(readCellValue(2, 2));
        assertEquals(Player.X, board.getCurrentTurn());
    }

    @Test
    void setCurrentTurn_updatesTurn() {
        board.setCurrentTurn(Player.O);

        assertEquals(Player.O, board.getCurrentTurn());
    }

    @Test
    void setState_viaReflection_acceptsEnumValue() throws Exception {
        Class<?> gameStateClass = Class.forName("fr.univlille.model.Board$GameState");
        Object finishedState = Enum.valueOf((Class<? extends Enum>) gameStateClass, "FINISHED");
        Method setState = Board.class.getDeclaredMethod("setState", gameStateClass);
        setState.setAccessible(true);

        setState.invoke(board, finishedState);

        assertEquals("FINISHED", board.getState().name());
        assertTrue(board.isInFinishedMode());
    }

    @Test
    void privateHelpers_detectOutOfBoundsAndOccupiedCells() throws Exception {
        Method isOutOfBounds = Board.class.getDeclaredMethod("isOutOfBounds", int.class);
        isOutOfBounds.setAccessible(true);
        assertTrue((boolean) isOutOfBounds.invoke(board, -1));
        assertTrue((boolean) isOutOfBounds.invoke(board, 3));
        assertFalse((boolean) isOutOfBounds.invoke(board, 0));
        assertFalse((boolean) isOutOfBounds.invoke(board, 2));

        Method isCellValueAlreadySet = Board.class.getDeclaredMethod("isCellValueAlreadySet", int.class, int.class);
        isCellValueAlreadySet.setAccessible(true);
        assertFalse((boolean) isCellValueAlreadySet.invoke(board, 0, 0));

        board.mark(0, 0);
        assertTrue((boolean) isCellValueAlreadySet.invoke(board, 0, 0));
    }

    @Test
    void privateHelper_detectsHorizontalVerticalAndDiagonalWins() throws Exception {
        Method isWinningMoveByPlayer = Board.class.getDeclaredMethod("isWinningMoveByPlayer", Player.class, int.class, int.class);
        isWinningMoveByPlayer.setAccessible(true);

        setCellValue(0, 0, Player.X);
        setCellValue(0, 1, Player.X);
        setCellValue(0, 2, Player.X);
        assertTrue((boolean) isWinningMoveByPlayer.invoke(board, Player.X, 0, 2));

        clearBoard();
        setCellValue(0, 1, Player.O);
        setCellValue(1, 1, Player.O);
        setCellValue(2, 1, Player.O);
        assertTrue((boolean) isWinningMoveByPlayer.invoke(board, Player.O, 2, 1));

        clearBoard();
        setCellValue(0, 0, Player.X);
        setCellValue(1, 1, Player.X);
        setCellValue(2, 2, Player.X);
        assertTrue((boolean) isWinningMoveByPlayer.invoke(board, Player.X, 2, 2));

        clearBoard();
        setCellValue(0, 2, Player.O);
        setCellValue(1, 1, Player.O);
        setCellValue(2, 0, Player.O);
        assertTrue((boolean) isWinningMoveByPlayer.invoke(board, Player.O, 2, 0));
    }

    @Test
    void privateHelper_flipCurrentTurn_switchesBetweenPlayers() throws Exception {
        Method flipCurrentTurn = Board.class.getDeclaredMethod("flipCurrentTurn");
        flipCurrentTurn.setAccessible(true);

        assertEquals(Player.X, board.getCurrentTurn());
        flipCurrentTurn.invoke(board);
        assertEquals(Player.O, board.getCurrentTurn());
        flipCurrentTurn.invoke(board);
        assertEquals(Player.X, board.getCurrentTurn());
    }

    @Test
    void isValid_rejectsFinishedGameAndOccupiedCell() throws Exception {
        Method isValid = Board.class.getDeclaredMethod("isValid", int.class, int.class);
        isValid.setAccessible(true);

        assertTrue((boolean) isValid.invoke(board, 0, 0));
        board.mark(0, 0);
        assertFalse((boolean) isValid.invoke(board, 0, 0));

        board.mark(1, 0);
        board.mark(0, 1);
        board.mark(1, 1);
        board.mark(0, 2);
        assertFalse((boolean) isValid.invoke(board, 2, 2));
    }

    private void assertWinner(Player expectedWinner, int row, int col) {
        assertEquals(expectedWinner, board.getWinner());
        assertEquals("FINISHED", board.getState().name());
        assertEquals(expectedWinner, board.getCurrentTurn());
        assertEquals(expectedWinner, readCellValue(row, col));
    }

    private void assertBoardCellsAreEmpty() {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                assertNull(readCellValue(row, col), "Cell [" + row + "," + col + "] should be empty");
            }
        }
    }

    private void clearBoard() {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                setCellValue(row, col, null);
            }
        }
    }

    private Player readCellValue(int row, int col) {
        try {
            Field cellsField = Board.class.getDeclaredField("cells");
            cellsField.setAccessible(true);
            Cell[][] cells = (Cell[][]) cellsField.get(board);
            return cells[row][col].getValue();
        } catch (ReflectiveOperationException e) {
            throw new AssertionError("Unable to read cell value", e);
        }
    }

    private void setCellValue(int row, int col, Player value) {
        try {
            Field cellsField = Board.class.getDeclaredField("cells");
            cellsField.setAccessible(true);
            Cell[][] cells = (Cell[][]) cellsField.get(board);
            cells[row][col].setValue(value);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError("Unable to set cell value", e);
        }
    }
}
