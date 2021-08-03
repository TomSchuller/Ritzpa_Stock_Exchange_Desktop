package engine;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

public class Transaction implements Serializable {
    private int value;
    private int quantity;
    private final String timeStamp;
    private final int order; // 1 buy, 2 sell1
    private final int lmtMkt; // 1 LMT, 2 MKR
    private final String buyerName;
    private final String sellerName;

    public Transaction(int newValue, int newQuantity, int newOrder, int lmtMkt, String newBuyerName, String newSellerName) {
        if(newQuantity <= 0) {
            throw new IllegalArgumentException("ERROR! You've entered " + newQuantity + ", an invalid quantity of stocks." + System.lineSeparator() + "You can only enter a number greater than 0.");
        }
        if(newOrder != 1 && newOrder != 2 && newOrder != 3) {
            throw new  IllegalArgumentException("ERROR! You've entered " + newOrder + ", an invalid type of stocks." + System.lineSeparator() + "You can only enter 1 to buy or 2 to sell the stock.");
        }
        if(newValue <= 0) {
            throw new IllegalArgumentException("ERROR! You've entered " + newValue + ", an invalid value of stock." + System.lineSeparator() +"You can only enter a number greater than 0.");
        }
        if(lmtMkt  != 1 && lmtMkt != 2) {
            throw new IllegalArgumentException("ERROR! You've entered " + lmtMkt + ", an invalid type of transaction." + System.lineSeparator() +"You can only enter a 1 to LMT and 2 to MKR (to to tom about change).");
        }
        this.value = newValue;
        this.quantity = newQuantity;
        this.timeStamp = new SimpleDateFormat("HH:mm:ss:SSS").format(new Date());
        this.order = newOrder;
        this.lmtMkt = lmtMkt;
        this.buyerName = newBuyerName;
        this.sellerName = newSellerName;
    }

    public int getCycle() { return quantity * value; }
    public int getValue() { return value; }
    public int getQuantity() { return quantity; }
    public String getTimeStamp() { return timeStamp; }
    public int getOrder() { return order; }
    public int getTypeLmtMkt() { return lmtMkt; }

    @Override
    public String toString() {
        return "(Transaction) " + "Sell value: " + value +
                " Date: " + timeStamp +
                ", Stocks sold: " + quantity +
                ", Transaction value: " + getCycle();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Transaction that = (Transaction) o;
        return value == that.value && quantity == that.quantity && Objects.equals(timeStamp, that.timeStamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, quantity, timeStamp);
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
    public void setValue(int value) {
        this.value = value;
    }

    public String getBuyerName() {
        return buyerName;
    }

    public String getSellerName() {
        return sellerName;
    }
}
