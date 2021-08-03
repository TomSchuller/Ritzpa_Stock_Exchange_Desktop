package engine;

import java.io.Serializable;
import java.util.*;

public class Stock implements Serializable {
    private final String symbol;
    private final String company;
    private int value;
    /// Make it generic
    private final TreeSet<Transaction> purchases;
    private final TreeSet<Transaction> sells;
    private final List<Transaction> allTransactions;


    class SellsComparator implements Comparator<Transaction>, Serializable {
        @Override
        public int compare(Transaction s1, Transaction s2) {
            if (s1.getValue() > s2.getValue())
                return 1;
            else if (s1.getValue() < s2.getValue())
                return -1;
            else {
                return (s1.getTimeStamp().compareTo(s2.getTimeStamp()) ); // by dates compare them
                // 13:51:22:123  -- 13:51:22:183
            }
            //return 0;
        }
    }

    class PurchasesComparator implements Comparator<Transaction>, Serializable {
        @Override
        public int compare(Transaction s1, Transaction s2) {
            if (s1.getValue() < s2.getValue())
                return 1;
            else if (s1.getValue() > s2.getValue())
                return -1;
            else {
                return (s1.getTimeStamp().compareTo(s2.getTimeStamp()) ); // by dates compare them
                // 13:51:22:123  -- 13:51:22:183
            }
            //return 0;
        }
    }

    public Stock(String newSymbol, String newCompany, int newValue){
        this.symbol = newSymbol;
        this.company = newCompany;
        this.value = newValue;
        this.purchases = new TreeSet<>(new PurchasesComparator()); // max
        this.sells = new TreeSet<>(new SellsComparator()); // min
        this.allTransactions = new LinkedList<>();
    }

    public int getTotalCycle() {
        int sum = 0;
        for (Transaction transaction : allTransactions) {
            sum += transaction.getCycle();
        }
        return sum;
    }

    public String getSymbol() { return symbol; }
    public String getCompany() { return company; }
    public int getValue() { return value; }
    public TreeSet<Transaction> getPurchases() { return purchases; }
   // public Queue<Transaction> get_sells() { return _sells; }
    public TreeSet<Transaction> getSells() { return sells; }
    public List<Transaction> getCompletedTransactions() { return allTransactions; }

    @Override
    public String toString() {
        return "Symbol: " + symbol +
                ", Company: " + company +
                ", Current value: " + value +
                ", Transactions cycle: " + this.getTotalCycle() +
                ", All transactions: " + allTransactions.size();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Stock stock = (Stock) o;
        return value == stock.value && Objects.equals(symbol, stock.symbol) && Objects.equals(company, stock.company) && Objects.equals(purchases, stock.purchases) && Objects.equals(sells, stock.sells) && Objects.equals(allTransactions, stock.allTransactions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(symbol, company, value, purchases, sells, allTransactions);
    }

    public void setValue(int value) {
        this.value = value;
    }
}
