package engine;

import dto.Item;
import dto.StockDTO;
import dto.TransactionDTO;
import dto.UserDTO;

import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;

public class EngineImpl implements Engine, Serializable {
    private Map<String, Stock> stocks;
    private List<String> stocksNames;

    private Map<String, User> users;
    private List<String> userNames;

    @Override
    public LoadFileTask load(String pathname) {
        //create consumers
        Consumer<Map<String, Stock>> totalStocksMapDelegate = tw -> this.stocks = tw;

        Consumer<List<String>> totalStocksNamesMapDelegate = ta -> this.stocksNames = ta;

        Consumer<Map<String, User>> totalUsersMapDelegate = tg -> this.users = tg;

        Consumer<List<String>> totalUsersNamesMapDelegate = ow -> this.userNames = ow;

        return new LoadFileTask(pathname,totalStocksMapDelegate, totalStocksNamesMapDelegate, totalUsersMapDelegate,totalUsersNamesMapDelegate);

    }

    @Override
    public void startThread(LoadFileTask task) { new Thread(task).start();}

    @Override
    public List<String> getStocksNames() {
        return stocksNames;
    }

    @Override
    public StockDTO getStock(String stockSymbol) {
        String name = findStockByName(stockSymbol);
        if (name == null) {
            return null;
        }

        Stock stock = stocks.get(name);

        List<TransactionDTO> allTransactions = new ArrayList<>();
        List<TransactionDTO> purchases = new ArrayList<>();
        List<TransactionDTO> sells = new ArrayList<>();

        for (Transaction tr : stock.getPurchases()) {
            purchases.add(new TransactionDTO(tr.getValue(), tr.getQuantity(), tr.getCycle(), tr.getTimeStamp(), tr.getTypeLmtMkt(), tr.getBuyerName(), tr.getSellerName()));
        }
        for (Transaction tr : stock.getSells()) {
            sells.add(new TransactionDTO(tr.getValue(), tr.getQuantity(), tr.getCycle(), tr.getTimeStamp(), tr.getTypeLmtMkt(), tr.getBuyerName(), tr.getSellerName()));
        }
        for (Transaction tr : stock.getCompletedTransactions()) {
            allTransactions.add(new TransactionDTO(tr.getValue(), tr.getQuantity(), tr.getCycle(), tr.getTimeStamp(), tr.getTypeLmtMkt(), tr.getBuyerName(), tr.getSellerName()));
        }
        return new StockDTO(stock.getSymbol(), stock.getCompany(), stock.getValue(), stock.getTotalCycle(), purchases, sells, allTransactions);
    }

    @Override
    public List<TransactionDTO> order(int lmtMkt, int type, String stockSymbol, int amount, int price, String userName) throws IllegalArgumentException {
        String name = findStockByName(stockSymbol);
        if (name == null) {
            throw new IllegalArgumentException("ERROR! " + stockSymbol + " doesn't exist!");
        }
        // name can't be null if im here
        Stock stock = stocks.get(name);

        Transaction transaction;

        if (type == 1) {
            transaction = new Transaction(price, amount, type, lmtMkt, userName, null);  // add TR type, LMT MKR enums
        }
        else if (type == 2) {
            transaction = new Transaction(price, amount, type, lmtMkt, null, userName);  // add TR type, LMT MKR enums
        }
        else { //its saved for deals closed
            throw new IllegalArgumentException("ERROR! You've entered " + type + ", an invalid type of stocks." + System.lineSeparator() + "You can only enter 1 to buy or 2 to sell the stock.");
        }


        if (lmtMkt == 1) {
            return limit(transaction, stock, false);
        } else { // lmt_mkr == 2 MKR
            return mkt(transaction, stock);
        }
    }


    public List<TransactionDTO> mkt(Transaction transaction, Stock stock) {
        int price;
        if(transaction.getOrder() == 1) { // to buy
            if(stock.getSells().size() == 0) {
                price = stock.getValue();
            }
            else {
                //price = stock.get_sells().peek().get_value();
                price = stock.getSells().first().getValue();
            }
        }
        else { // i think its 2 to sell
            if(stock.getPurchases().size() == 0) {
                price = stock.getValue();
            }
            else {
             //   price = stock.get_purchases().peek().get_value();
                price = stock.getPurchases().first().getValue();
            }
        }
        transaction.setValue(price);
        return limit(transaction, stock, true);
    }


    public List<TransactionDTO> limit(Transaction transaction, Stock stock, boolean isMkt) throws IllegalArgumentException {
        List<TransactionDTO>dealsMade = new ArrayList<>();
        List<Transaction>SameUserList = new ArrayList<>();

        if(transaction.getOrder() == 1) { // we want to buy
            int size = stock.getSells().size();
            for(int i = 0; i < size; ++i) { // i=0, i<sells.size
                Transaction sell = stock.getSells().first();

                // check if the sell offer isn't from the same user- if it is skip to next
                if(transaction.getBuyerName().equals(sell.getSellerName())) {
                    //skip next person
                    SameUserList.add(sell);
                    stock.getSells().remove(sell);
                }
                else if(sell.getValue() > transaction.getValue() && !isMkt) { // too high cant buy
                        stock.getPurchases().add(transaction);
                        return dealsMade;
                }
                else { // the price is good for us
                    if(transaction.getQuantity() == sell.getQuantity()) { // full deal
                        Transaction deal = new Transaction(sell.getValue(), sell.getQuantity(), 3, transaction.getTypeLmtMkt(), transaction.getBuyerName(), sell.getSellerName()); // change 3
                        TransactionDTO made = new TransactionDTO(deal.getValue(), deal.getQuantity(), deal.getCycle() ,deal.getTimeStamp(), deal.getTypeLmtMkt(), deal.getBuyerName(), deal.getSellerName());
                        dealsMade.add(made);
                        stock.getCompletedTransactions().add(deal);
                        stock.setValue(deal.getValue());// after every deal update stock val
                        updateUsers(deal.getBuyerName(), deal.getSellerName(), deal.getQuantity(), stock.getSymbol(), stock.getValue());

                        stock.getSells().remove(sell);
                        return dealsMade;
                    }
                    else if(transaction.getQuantity() < sell.getQuantity()) { // i buy less then you sell
                        //pop to sell and add to green
                        Transaction deal = new Transaction(sell.getValue(), transaction.getQuantity(), 3, transaction.getTypeLmtMkt(), transaction.getBuyerName(), sell.getSellerName()); // change 3
                        TransactionDTO made = new TransactionDTO(deal.getValue(), deal.getQuantity(), deal.getCycle() ,deal.getTimeStamp(), deal.getTypeLmtMkt(), deal.getBuyerName(), deal.getSellerName());
                        dealsMade.add(made);
                        stock.getCompletedTransactions().add(deal);
                        stock.setValue(deal.getValue());// after every deal update stock val
                        updateUsers(deal.getBuyerName(), deal.getSellerName(), deal.getQuantity(), stock.getSymbol(), stock.getValue());
                        sell.setQuantity(sell.getQuantity() - transaction.getQuantity());
                        return dealsMade;
                    }
                    else { // i buy all you sell, but i want to buy more
                        Transaction deal = new Transaction(sell.getValue(), sell.getQuantity(), 3, transaction.getTypeLmtMkt(), transaction.getBuyerName(), sell.getSellerName()); // change 3
                        TransactionDTO made = new TransactionDTO(deal.getValue(), deal.getQuantity(), deal.getCycle() ,deal.getTimeStamp(), deal.getTypeLmtMkt(), deal.getBuyerName(), deal.getSellerName());
                        dealsMade.add(made);
                        stock.getCompletedTransactions().add(deal);
                        stock.setValue(deal.getValue());// after every deal update stock val
                        updateUsers(deal.getBuyerName(), deal.getSellerName(), deal.getQuantity(), stock.getSymbol(), stock.getValue());
                        transaction.setQuantity(transaction.getQuantity()-sell.getQuantity());
                        stock.getSells().remove(sell);
                    }
                }
            } // end of loop
            if(transaction.getQuantity() > 0) {
                if(isMkt) {
                    transaction.setValue(stock.getValue()); // update transaction value by last deal
                }
                stock.getPurchases().add(transaction);
            }
            if(SameUserList.size() >0) {
                //return the moves transactions fro, same user
                for(Transaction tr : SameUserList) {
                    stock.getSells().add(tr);
                }
            }
        } // type 1

        else { //  type 2 i want to sell
            int size = stock.getPurchases().size();
            for(int i = 0; i < size; ++i) { // i=0; i<sells.size
                Transaction purchase = stock.getPurchases().first(); //peek();

                if(transaction.getSellerName().equals(purchase.getBuyerName())) {
                    //skip next person
                    SameUserList.add(purchase);
                    stock.getPurchases().remove(purchase);
                }
                else if(purchase.getValue() < transaction.getValue() && !isMkt) { // too low won't sell
                    // add to waiting list
                    stock.getSells().add(transaction);
                    // update user real quantity of the stock
                    updateRealQuantity(transaction.getSellerName(),stock.getSymbol(), transaction.getQuantity());
                    // return the deals list
                    return dealsMade;
                }
                else { // the price is good for us
                    if(transaction.getQuantity() == purchase.getQuantity()) { // full deal
                        Transaction deal = new Transaction(purchase.getValue(), purchase.getQuantity(), 3, transaction.getTypeLmtMkt(), purchase.getBuyerName(), transaction.getSellerName()); // change 3
                        TransactionDTO made = new TransactionDTO(deal.getValue(), deal.getQuantity(), deal.getCycle() ,deal.getTimeStamp(), deal.getTypeLmtMkt(), deal.getBuyerName(), deal.getSellerName());
                        dealsMade.add(made);
                        stock.getCompletedTransactions().add(deal);
                        stock.setValue(deal.getValue());// after every deal update stock val
                        updateUsers(deal.getBuyerName(), deal.getSellerName(), deal.getQuantity(), stock.getSymbol(), stock.getValue());
                        updateRealQuantity(transaction.getSellerName(),stock.getSymbol(), deal.getQuantity());
                        stock.getPurchases().remove(purchase);
                        return dealsMade;
                    }
                    else if (transaction.getQuantity() < purchase.getQuantity()) { // i sell less then you want to buy
                        Transaction deal = new Transaction(purchase.getValue(), transaction.getQuantity(), 3, transaction.getTypeLmtMkt(), purchase.getBuyerName(), transaction.getSellerName()); // change 3
                        TransactionDTO made = new TransactionDTO(deal.getValue(), deal.getQuantity(), deal.getCycle() ,deal.getTimeStamp(), deal.getTypeLmtMkt(), deal.getBuyerName(), deal.getSellerName());
                        dealsMade.add(made);
                        stock.getCompletedTransactions().add(deal);
                        stock.setValue(deal.getValue());// after every deal update stock val
                        updateUsers(deal.getBuyerName(), deal.getSellerName(), deal.getQuantity(), stock.getSymbol(), stock.getValue());
                        updateRealQuantity(transaction.getSellerName(),stock.getSymbol(), deal.getQuantity());
                        purchase.setQuantity(purchase.getQuantity() - transaction.getQuantity());
                        return dealsMade;
                    }
                    else { // i sell all you want to buy, but i want to sell more
                        Transaction deal = new Transaction(purchase.getValue(), purchase.getQuantity(), 3, transaction.getTypeLmtMkt(), purchase.getBuyerName(), transaction.getSellerName()); // change 3
                        TransactionDTO made = new TransactionDTO(deal.getValue(), deal.getQuantity(), deal.getCycle() ,deal.getTimeStamp(), deal.getTypeLmtMkt(), deal.getBuyerName(), deal.getSellerName());
                        dealsMade.add(made);
                        stock.getCompletedTransactions().add(deal);
                        stock.setValue(deal.getValue()); // after every deal update stock val
                        updateUsers(deal.getBuyerName(), deal.getSellerName(), deal.getQuantity(), stock.getSymbol(), stock.getValue());
                        updateRealQuantity(transaction.getSellerName(),stock.getSymbol(), deal.getQuantity());
                        transaction.setQuantity(transaction.getQuantity()-purchase.getQuantity());
                        stock.getPurchases().remove(purchase);
                    }
                }
            } // end of loop
            if(transaction.getQuantity() > 0) {
                if(isMkt) {
                    transaction.setValue(stock.getValue()); // update transaction value by last deal
                }
                stock.getSells().add(transaction);
                updateRealQuantity(transaction.getSellerName(),stock.getSymbol(), transaction.getQuantity());
            }
            if(SameUserList.size() >0) {
                //return the moves transactions fro, same user
                for(Transaction tr : SameUserList) {
                    stock.getPurchases().add(tr);
                }
            }
        } // end type 2

        return dealsMade;
    } //

    private void updateRealQuantity(String sellerName, String symbol, int quantity) {
        //users.get(transaction.getSellerName()).getItems().get(stock.getSymbol()).setQuantity();
        for (String userName : userNames) {
            if (userName.equals(sellerName)) {
                for (Item item : users.get(sellerName).getItems()) {
                    if (item.getSymbol().equals(symbol)) {
                        item.setRealQuantity(item.getRealQuantity() - quantity);
                        // if (item.getRalQuantity() == 0) maybe do something ?
                        break;
                    }
                }
            }
        }
    }

    private void updateUsers(String buyerName, String sellerName, int quantity, String symbol, int value) {
        boolean buyerHasStock = false;

        for (String userName : userNames) {
            if (userName.equals(buyerName)) {
                for (Item item : users.get(buyerName).getItems()) {
                    if (item.getSymbol().equals(symbol)) {
                        buyerHasStock = true;
                        item.setQuantity(item.getQuantity() + quantity);
                        item.setRealQuantity(item.getRealQuantity() + quantity);
                        item.setValue(value);
                        if (item.getQuantity() == 0) {
                            users.get(buyerName).getItems().remove(item);
                        }
                        break;
                    }
                }

                if (!buyerHasStock) {
                    Item newStock = new Item(symbol, quantity, value);
                    users.get(buyerName).getItems().add(newStock);
                }
            }

            else if (userName.equals(sellerName)) {
                for (Item item : users.get(sellerName).getItems()) {
                    if (item.getSymbol().equals(symbol)) {
                        item.setQuantity(item.getQuantity() - quantity); // didnt add real quantity here cuz it already happened\
                        item.setValue(value);
                        if (item.getQuantity() == 0) {
                            users.get(sellerName).getItems().remove(item);
                        }
                        break;
                    }
                }
            }
            // Update all other users with stock new value
            else {
                for (Item item : users.get(userName).getItems()) {
                    if (item.getSymbol().equals(symbol)) {
                        item.setValue(value);
                        break;
                    }
                }
            }
        }


    }

    @Override
    public UserDTO getUser(String name) {
        if (!userNames.contains(name)) {
            return null; // handle problem
        }
        return new UserDTO(name, users.get(name).getItems());
    }

    @Override
    public List<String> getUserNames() {
        return userNames;
    }

    public String findStockByName(String SYMBOL){
        for (String str: stocksNames) {
            if (SYMBOL.compareToIgnoreCase(str) == 0) return str;
        }
        return null;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EngineImpl engine = (EngineImpl) o;
        return Objects.equals(stocks, engine.stocks) && Objects.equals(stocksNames, engine.stocksNames);
    }

    @Override
    public int hashCode() {
        return Objects.hash(stocks, stocksNames);
    }

    @Override
    public String toString() {
        return "Engine{" +
                "stocks=" + stocks +
                ", names=" + stocksNames +
                '}';
    }
}
