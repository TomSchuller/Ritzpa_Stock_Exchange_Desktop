package engine.exception;

import engine.Stock;

public class ContainsSymbolException extends Exception {
    private final String companyTry;
    private final String companyExists;
    private final String symbol;
    private final String EXCEPTION_MESSAGE;

    public ContainsSymbolException(Stock stockTry, Stock stockExists) {
        companyTry = stockTry.getCompany();
        companyExists = stockExists.getCompany();
        symbol = stockTry.getSymbol();
        EXCEPTION_MESSAGE = "ERROR! You've tried to insert the stock: " + symbol + " by " + companyTry + ", but a similar stock already exists with the same symbol: "
                + symbol + " by " + companyExists + ". Please rename the symbol of one of the stocks.";
    }

    @Override
    public String getMessage() {
        return EXCEPTION_MESSAGE;
    }
}