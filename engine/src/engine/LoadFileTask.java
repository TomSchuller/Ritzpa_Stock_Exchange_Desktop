package engine;

import dto.Item;
import engine.exception.*;
import engine.jaxb.schema.generated.RizpaStockExchangeDescriptor;
import engine.jaxb.schema.generated.RseItem;
import engine.jaxb.schema.generated.RseStock;
import engine.jaxb.schema.generated.RseUser;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

    public class LoadFileTask extends Task<Boolean> { // change to loadFileTask
        private final String fileName;
        private final Consumer<Map<String, Stock>> totalStocksMapDelegate; // number of stocks == totalWordsDelegate
        private final Consumer<List<String>> totalStocksNamesDelegate; // number of users == totalLinesDelegate
        private final Consumer<Map<String, User>> totalUsersMapDelegate; // number of stocks == totalWordsDelegate
        private final Consumer<List<String>> totalUsersNamesDelegate; // number of users == totalLinesDelegate

        private final int SLEEP_TIME = 100; // in histogram init to 0
        private final int PROGRESS_BAR_SLEEP_TIME = 500; // in histogram init to 0

        public LoadFileTask(String fileName, Consumer<Map<String, Stock>> totalStocksMapDelegate, Consumer<List<String>> totalStocksNamesDelegate, Consumer<Map<String, User>> totalUsersMapDelegate, Consumer<List<String>> totalUsersNamesDelegate) {
            this.fileName = fileName;
            this.totalStocksMapDelegate = totalStocksMapDelegate;
            this.totalStocksNamesDelegate = totalStocksNamesDelegate;
            this.totalUsersMapDelegate = totalUsersMapDelegate;
            this.totalUsersNamesDelegate = totalUsersNamesDelegate;
        }

        @Override
        protected Boolean call() throws Exception {
            Map<String, Stock> stocks;
            List<String> stocksNames;

            Map<String, User> users;
            List<String> userNames;

            // Fetching file
            try {
                updateProgress(0, 1);
                updateMessage("Fetching file...");
                Thread.sleep(PROGRESS_BAR_SLEEP_TIME);
                RizpaStockExchangeDescriptor stockz;
                try {
                    InputStream inputStream = new FileInputStream(new File(fileName));
                    stockz = deserializeFrom(inputStream);
                } catch (JAXBException ex) {
                    super.cancelled();
                    // ex.printStackTrace();
                    throw new Exception("ERROR! The XML file is corrupted" + System.lineSeparator());
                } catch (FileNotFoundException ex) {
                    // ex.printStackTrace();
                    super.cancelled();
                    throw new Exception("ERROR! Not found an XML file in " + fileName + System.lineSeparator());
                }
                // has stocks?

                updateProgress(1, 1);
                updateMessage("Successfully fetched file!");
                Thread.sleep(PROGRESS_BAR_SLEEP_TIME);

                // Fetching stocks
                int totalStocks = stockz.getRseStocks().getRseStock().size();
                int progressIndex = 0;
                updateProgress(progressIndex, totalStocks);
                updateMessage("Checking Valid Stocks in the file...");
                Thread.sleep(PROGRESS_BAR_SLEEP_TIME);
                ++progressIndex;

                stocks = new HashMap<>();
                stocksNames = new ArrayList<>();

                for (RseStock currStock : stockz.getRseStocks().getRseStock()) {
                    Stock newStock = new Stock(currStock.getRseSymbol(), currStock.getRseCompanyName(), currStock.getRsePrice());

                    if (stocks.containsKey(newStock.getSymbol())) {
                        Stock stock2 = stocks.get(newStock.getSymbol());
                        super.cancelled();
                        throw new ContainsSymbolException(newStock, stock2);
                    }
                    Stock stock2 = containsCompany(currStock.getRseCompanyName(), stocksNames, stocks);
                    if (stock2 != null) {
                        super.cancelled();
                        throw new ContainsCompanyException(newStock, stock2);
                    } else {
                        stocks.put(currStock.getRseSymbol(), newStock);
                        stocksNames.add(currStock.getRseSymbol());
                    }
                    updateMessage("Uploading " + currStock.getRseSymbol() + " stock to system!");
                    updateProgress(progressIndex, totalStocks);
                    ++progressIndex;
                    Thread.sleep(PROGRESS_BAR_SLEEP_TIME);
                }
                updateProgress(totalStocks, totalStocks);
                updateMessage("Successfully fetched stocks!");
                Thread.sleep(PROGRESS_BAR_SLEEP_TIME);
                // update total Stocks in UI
                Platform.runLater(
                        () -> totalStocksMapDelegate.accept(stocks)
                );
                Platform.runLater(
                        () -> totalStocksNamesDelegate.accept(stocksNames)
                );


                // Fetching users
                int totalUsers = stockz.getRseUsers().getRseUser().size();
                progressIndex = 0;
                updateProgress(progressIndex, totalUsers);
                updateMessage("Checking Valid Users in the file...");
                Thread.sleep(PROGRESS_BAR_SLEEP_TIME);
                ++progressIndex;

                users = new HashMap<>();
                userNames = new ArrayList<>();

                for (RseUser currUser : stockz.getRseUsers().getRseUser()) {
                    List<Item> newItems = new ArrayList<>();
                    for (RseItem currItem : currUser.getRseHoldings().getRseItem()) {

                        // If the stock doesn't exist in the system
                        if (!stocks.containsKey(currItem.getSymbol())) {
                            super.cancelled();
                            Item newItem = new Item(currItem.getSymbol(), currItem.getQuantity(), 0);
                            throw new MissingStockException(newItem, currUser.getName());
                        }

                        Item newItem = new Item(currItem.getSymbol(), currItem.getQuantity(), stocks.get(currItem.getSymbol()).getValue());

                        // If the stock already exists in the system
                        for (Item item : newItems) {
                            if (item.getSymbol().equals(newItem.getSymbol())) {
                                super.cancelled();
                                throw new ContainsItemException(newItem, item, currUser.getName());
                            }
                        }

                        newItems.add(newItem);
                    }
                    User newUser = new User(currUser.getName(), newItems);

                    if (users.containsKey(currUser.getName())) {
                        super.cancelled();
                        throw new ContainsUserException(newUser);
                    }

                    users.put(newUser.getName(), newUser);
                    userNames.add(newUser.getName());
                    updateMessage("Uploading " + currUser.getName() + " to system!");
                    updateProgress(progressIndex, totalUsers);
                    ++progressIndex;
                    Thread.sleep(PROGRESS_BAR_SLEEP_TIME);
                }

                updateProgress(totalUsers, totalUsers);
                updateMessage("Successfully fetched users!");
                Thread.sleep(PROGRESS_BAR_SLEEP_TIME);
                updateMessage("System is ready for use!");

                // update total Users in UI
                Platform.runLater(
                        () -> totalUsersMapDelegate.accept(users)
                );
                Platform.runLater(
                        () -> totalUsersNamesDelegate.accept(userNames)
                );

                Thread.sleep(SLEEP_TIME);

                updateMessage("Done...");
            }
            catch (InterruptedException ignored) { //catch sleep time
            }
            catch (IOException e) {
                e.printStackTrace();
            }

            return Boolean.TRUE;
        }

        private static RizpaStockExchangeDescriptor deserializeFrom(InputStream in) throws JAXBException {
            final String JAXB_XML_GAME_PACKAGE_NAME = "engine.jaxb.schema.generated";
            JAXBContext jc = JAXBContext.newInstance(JAXB_XML_GAME_PACKAGE_NAME);
            Unmarshaller u = jc.createUnmarshaller();
            return (RizpaStockExchangeDescriptor) u.unmarshal(in);
        }

        private Stock containsCompany(String companyName,List<String> stocksNames, Map<String,Stock> stocks) {
            for (String symbol : stocksNames) {
                if (stocks.get(symbol).getCompany().equals(companyName))
                    return stocks.get(symbol);
            }
            return null;
        }
    }

