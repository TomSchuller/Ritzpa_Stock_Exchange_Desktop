package dto;

import java.util.List;

public class StockDTO implements AdvancedStockDTO, BasicStockDTO {
    private final String symbol;
    private final String company;
    private final int value;
    private final int totalCycle;
    private final int transactionsSize;
    private List<TransactionDTO> purchases;
    private List<TransactionDTO> sells;
    private List<TransactionDTO> allTransactions;

    public StockDTO(String newSymbol, String newCompany, int newValue, int newTotalCycle, int newTransactionSize) {
        symbol = newSymbol;
        company = newCompany;
        value = newValue;
        totalCycle = newTotalCycle;
        transactionsSize = newTransactionSize;
    }

    public StockDTO(String newSymbol, String newCompany, int newValue, int newTotalCycle, List<TransactionDTO> newPurchases, List<TransactionDTO> newSells, List<TransactionDTO> newAllTransactions) {
        this(newSymbol, newCompany, newValue, newTotalCycle, newAllTransactions.size());
        purchases = newPurchases;
        sells = newSells;
        allTransactions = newAllTransactions;
    }

    @Override
    public String getSymbol() { return symbol; }

    @Override
    public String getCompany() { return company; }

    @Override
    public Integer getValue() { return value; }

    @Override
    public Integer getTotalCycle() { return totalCycle; }

    @Override
    public List<TransactionDTO> getPurchases() { return purchases; }

    @Override
    public List<TransactionDTO> getSells() { return sells; }

    @Override
    public List<TransactionDTO> getTransactions() { return allTransactions; }
}
