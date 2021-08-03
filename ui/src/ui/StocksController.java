package ui;

import dto.StockDTO;
import dto.TransactionDTO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.text.SimpleDateFormat;
import java.util.Date;

//this.timeStamp = new SimpleDateFormat("HH:mm:ss:SSS").format(new Date());
public class StocksController {
    @FXML private RitzpaMainSceneController ritzpaMainSceneController;
    @FXML LineChart<String, Number> stockValueChart;
    @FXML Label stockCompanyLabel;
    @FXML Label currentValueLabel;
    @FXML Label totalCycleLabel;

    @FXML TableView<TransactionDTO> sellsTable;
    @FXML TableView<TransactionDTO> purchaseTable;
    @FXML TableView<TransactionDTO> madeTable;

    @FXML TableColumn<TransactionDTO, String> buyTimeStampColumn;
    @FXML TableColumn<TransactionDTO, String> buyTypeColumn;
    @FXML TableColumn<TransactionDTO, String> buyQuantityColumn;
    @FXML TableColumn<TransactionDTO, String> buyPriceColumn;
    @FXML TableColumn<TransactionDTO, String> buyUserColumn;

    @FXML TableColumn<TransactionDTO, String> madeTimeStampColumn;
    @FXML TableColumn<TransactionDTO, String> madeTypeColumn;
    @FXML TableColumn<TransactionDTO, String> madeQuantityColumn;
    @FXML TableColumn<TransactionDTO, String> madePriceColumn;
    @FXML TableColumn<TransactionDTO, String> madeSellerColumn;
    @FXML TableColumn<TransactionDTO, String> madeBuyerColumn;

    @FXML TableColumn<TransactionDTO, String> saleTimeStampColumn;
    @FXML TableColumn<TransactionDTO, String> saleTypeColumn;
    @FXML TableColumn<TransactionDTO, String> saleQuantityColumn;
    @FXML TableColumn<TransactionDTO, String> salePriceColumn;
    @FXML TableColumn<TransactionDTO, String> saleUserColumn;

    @FXML Label transactionsMadeLabel;
    @FXML Label sellsMadeLabel;
    @FXML Label purchasesMadeLabel;

    @FXML
    public void initialize() {
        buyPriceColumn.setCellValueFactory(new PropertyValueFactory<>("value"));
        buyTypeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        buyQuantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        buyTimeStampColumn.setCellValueFactory(new PropertyValueFactory<>("timeStamp"));
        buyUserColumn.setCellValueFactory(new PropertyValueFactory<>("buyerName"));

        salePriceColumn.setCellValueFactory(new PropertyValueFactory<>("value"));
        saleTypeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        saleQuantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        saleTimeStampColumn.setCellValueFactory(new PropertyValueFactory<>("timeStamp"));
        saleUserColumn.setCellValueFactory(new PropertyValueFactory<>("sellerName"));

        madePriceColumn.setCellValueFactory(new PropertyValueFactory<>("value"));
        madeTypeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        madeQuantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        madeTimeStampColumn.setCellValueFactory(new PropertyValueFactory<>("timeStamp"));
        madeBuyerColumn.setCellValueFactory(new PropertyValueFactory<>("buyerName"));
        madeSellerColumn.setCellValueFactory(new PropertyValueFactory<>("sellerName"));
    }

    public StocksController() {
    }

    ObservableList<TransactionDTO> getSellsList(String stockName) {
        return FXCollections.observableArrayList(ritzpaMainSceneController.getEngine().getStock(stockName).getSells());
    }

    ObservableList<TransactionDTO> getPurchasesList(String stockName) {
        return FXCollections.observableArrayList(ritzpaMainSceneController.getEngine().getStock(stockName).getPurchases());
    }

    ObservableList<TransactionDTO> getMadeList(String stockName) {
        return FXCollections.observableArrayList(ritzpaMainSceneController.getEngine().getStock(stockName).getTransactions());
    }

    public void updateStockChart(StockDTO stock, Integer dealsMadeSize) {
        sellsTable.setItems(getSellsList(stock.getSymbol()));
        purchaseTable.setItems(getPurchasesList(stock.getSymbol()));
        madeTable.setItems(getMadeList(stock.getSymbol()));

        sellsTable.setVisible(!sellsTable.getItems().isEmpty());
        sellsMadeLabel.setVisible(sellsTable.getItems().isEmpty());

        purchaseTable.setVisible(!purchaseTable.getItems().isEmpty());
        purchasesMadeLabel.setVisible(purchaseTable.getItems().isEmpty());

        madeTable.setVisible(!madeTable.getItems().isEmpty());
        transactionsMadeLabel.setVisible(madeTable.getItems().isEmpty());

        stockCompanyLabel.setText(stock.getCompany());
        totalCycleLabel.setText(stock.getTotalCycle().toString() + "$");
        currentValueLabel.setText(stock.getValue().toString() + "$");
        int size;
        // Initialize
        if (stockValueChart.getData().size() == 0) {
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            stockValueChart.getData().add(series);
            String initialTime = new SimpleDateFormat("HH:mm:ss:SSS").format(new Date());
            XYChart.Data<String, Number> data = new XYChart.Data<>(initialTime, stock.getValue().doubleValue());
            stockValueChart.getData().get(0).getData().add(0, data);
        }
        // Just update
        else {
            int transactionSize = stock.getTransactions().size()-dealsMadeSize;
            int allTransactionSize = stock.getTransactions().size();
            // Get size of existing series
            size = stockValueChart.getData().get(0).getData().size();

            for (int i = transactionSize; i < allTransactionSize; ++i) {
                // update only if value has changed
                //if (!stockValueChart.getData().get(0).getData().get(size - 1).getYValue().equals(stock.getValue())) {
                String dateString = stock.getTransactions().get(i).getTimeStamp();
                XYChart.Data<String, Number> data = new XYChart.Data<>(dateString, stock.getTransactions().get(i).getValue());
                stockValueChart.getData().get(0).getData().add(size, data);
                ++size;
                //}
            }
        }
    }


    public void setMainSceneController(RitzpaMainSceneController ritzpa) {
        if (ritzpaMainSceneController == null) this.ritzpaMainSceneController = ritzpa;
    }

    public void setMainSceneController(RitzpaMainSceneController ritzpa, StockDTO stock) {
        if (ritzpaMainSceneController == null) setMainSceneController(ritzpa);
//        sellsTable.setItems(getSellsList(stock.getSymbol()));
//        purchaseTable.setItems(getPurchasesList(stock.getSymbol()));
//        madeTable.setItems(getMadeList(stock.getSymbol()));
//
//        sellsTable.setVisible(!sellsTable.getItems().isEmpty());
//        sellsMadeLabel.setVisible(sellsTable.getItems().isEmpty());
//
//        purchaseTable.setVisible(!purchaseTable.getItems().isEmpty());
//        purchasesMadeLabel.setVisible(purchaseTable.getItems().isEmpty());
//
//        madeTable.setVisible(!madeTable.getItems().isEmpty());
//        transactionsMadeLabel.setVisible(madeTable.getItems().isEmpty());

        updateStockChart(stock, 0);
    }
}
