package engine.exception;

import dto.Item;

public class MissingStockException extends Exception {
    private final String symbolTry;
    private final String EXCEPTION_MESSAGE;

    public MissingStockException(Item stockTry, String userName) {
        symbolTry = stockTry.getSymbol();
        EXCEPTION_MESSAGE = "ERROR! You've tried to insert the stock: " + symbolTry + " to " + userName + ", but no such stock exists in the system! Please rename the symbol of the stock.";
    }

    @Override
    public String getMessage() {
        return EXCEPTION_MESSAGE;
    }
}
