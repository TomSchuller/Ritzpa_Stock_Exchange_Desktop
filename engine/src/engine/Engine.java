package engine;


import dto.StockDTO;
import dto.TransactionDTO;
import dto.UserDTO;
import java.util.List;

public interface Engine  {
    /**
     *
     * @param pathname
     */
    LoadFileTask load(String pathname); /// NEEDS TO BE OBJECT

    /**
     *List<StockDTO>
     */
    List<String> getStocksNames();

    /**
     *
     * @param SYMBOL
     */
    StockDTO getStock(String SYMBOL); // stockDTO

    UserDTO getUser(String name);

    List<String> getUserNames();

    List<TransactionDTO> order(int lmtMkr, int type, String stockSymbol, int amount, int price, String userName) throws IllegalArgumentException;

    void startThread(LoadFileTask task);
}
