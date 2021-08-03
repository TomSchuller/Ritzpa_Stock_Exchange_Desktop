package ui;

import dto.Item;
import dto.UserDTO;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class UserTabController {
    @FXML ComboBox<String> stocksComboBox;
    @FXML RadioButton sellRadioButton;
    @FXML RadioButton buyRadioButton;
    @FXML RadioButton LMTRadioButton;
    @FXML RadioButton MKTRadioButton;
    @FXML Slider quantitySlider;
    @FXML TextField quantityTextField;
    @FXML Slider priceSlider;
    @FXML TextField priceTextField;
    @FXML HBox enterPriceHBox;
    @FXML Label enterPriceLabel;
    @FXML Button submitButton;
    @FXML Label userSumPrice;
    @FXML VBox stocksBox;
    @FXML StackPane userTabPane;
    @FXML TableView<Item> userTable;
    @FXML VBox mainSceneVBox;
    @FXML ScrollPane tradeScrollPane;

    @FXML StackedBarChart<String, Double> gainLossChart;
    @FXML StackedAreaChart<Integer, Integer> earningsChart;
    @FXML PieChart investingsChart;

    @FXML XYChart.Series<Integer, Integer> earnings;
    @FXML XYChart.Series<String, Double> stock;

    @FXML TableColumn<Item, String> symbolColumn;
    @FXML TableColumn<Item, String> quantityColumn;
    @FXML TableColumn<Item, String> currentValueColumn;

    @FXML private RitzpaMainSceneController ritzpaMainSceneController;
    private Map<String, Integer> itemsLastValues;
    private static final int SLIDER_MAX = 1000;
    @FXML private ToggleGroup buyOrSell;
    @FXML private ToggleGroup lmtOrMkt;

    @FXML
    public void initialize() {
        itemsLastValues = new HashMap<>();

        symbolColumn.setCellValueFactory(new PropertyValueFactory<>("symbol"));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        currentValueColumn.setCellValueFactory(new PropertyValueFactory<>("value"));

        earnings = new XYChart.Series<>();
        stock = new XYChart.Series<>();

        quantityTextField.textProperty().bind(Bindings.format("%.0f",quantitySlider.valueProperty()));
        priceTextField.textProperty().bind(Bindings.format("%.0f",priceSlider.valueProperty()));

        buyOrSell = new ToggleGroup();
        buyOrSell.getToggles().add(buyRadioButton);
        buyOrSell.getToggles().add(sellRadioButton);

        lmtOrMkt = new ToggleGroup();
        lmtOrMkt.getToggles().add(LMTRadioButton);
        lmtOrMkt.getToggles().add(MKTRadioButton);

        enterPriceHBox.visibleProperty().bind(LMTRadioButton.selectedProperty());
        enterPriceLabel.visibleProperty().bind(LMTRadioButton.selectedProperty());
    }

    ObservableList<Item> getItemList(List<Item> userItems) {
        return FXCollections.observableArrayList(userItems);
    }


     public void updateUserCharts(List<Item> items, String stockSymbolOrder) {
        Map<String, Double> lastPercent = new HashMap<>();
        int gainLossSize = gainLossChart.getData().size();
        // If the chart is not empty - get the last percentages
        if (gainLossSize != 0) {
            for (XYChart.Series<String, Double> stockSeries : gainLossChart.getData()){
                XYChart.Data<String, Double> stockData = stockSeries.getData().get(0);
                String xValueSymbol = stockData.getXValue();
                Double yValuePercent = stockData.getYValue();
                lastPercent.put(xValueSymbol, yValuePercent);
            }
        }

         AtomicReference<Integer> userSum = new AtomicReference<>(0);
         AtomicReference<XYChart.Series<String, Double>> gainLossSeries = new AtomicReference<>(new XYChart.Series<>());
         AtomicReference<XYChart.Series<Integer, Integer>> earningsChartSeries = new AtomicReference<>(new XYChart.Series<>());
         AtomicReference<Double> change = new AtomicReference<>(0.0);

         int earningsChartSize = 0;

         if (earningsChart.getData().size() != 0) earningsChartSize = earningsChart.getData().get(0).getData().size();
         else {
             earningsChart.getData().add(earningsChartSeries.get());
         }
         int investingChartSize = investingsChart.getData().size();

        // Delete pie chart
         investingsChart.getData().remove(0, investingChartSize);

         // Delete Gain Loss chart
         gainLossChart.getData().remove(0, gainLossSize);


         items.forEach((item) -> {
             // Gain Loss Chart
             gainLossSeries.set(new XYChart.Series<>());
             // calculate change
             // if the item is not new - only update
             if (itemsLastValues.get(item.getSymbol()) != null) {
                 // if the current item has been involved in a transaction then change percent
                 if (item.getSymbol().equals(stockSymbolOrder)) change.set(calculateChange(itemsLastValues.get(item.getSymbol()).doubleValue(), item.getValue().doubleValue()));
                 // else if it's not a new item and it hasn't been involved in a transaction - put its last percent
                 else if (lastPercent.containsKey(item.getSymbol())) change.set(lastPercent.get(item.getSymbol()));
             }
             else {
                 change.set(0.0);
             }

             gainLossSeries.get().getData().add(new XYChart.Data<>(item.getSymbol(), change.get()));
             gainLossChart.getData().add(gainLossSeries.get());


             // Pie Chart
             investingsChart.getData().add(new PieChart.Data(item.getSymbol(), item.getQuantity()));

             // Total Money chart
             userSum.updateAndGet(v -> v + (item.getValue() * item.getQuantity()));
         });

         // Add new user Sum - only if has changed
         if (!userSum.get().toString().equals(userSumPrice.getText())) {
             earningsChart.getData().get(0).getData().add(new XYChart.Data<>(earningsChartSize, userSum.get()));

             // Set user sum label
             userSumPrice.setText(userSum.toString());
         }

         // Update last values
         getLastValues();
     }

    public Double calculateChange(Double lastValue, Double currValue) {
        double newValue;
        // (300-275):275*100 =
        newValue = ((currValue-lastValue) /lastValue )*100;
        return newValue;

        /*if (currValue.equals(lastValue)) newValue = 0.0; // no change - 0%
        else if (currValue > lastValue) newValue = (currValue/lastValue)*100; // gain - calculate percent
        else newValue = (lastValue/currValue)*(-100); // loss - calculate negative percent*/

    }

    private void getLastValues() {
        itemsLastValues.clear();
        userTable.getItems().forEach((item -> itemsLastValues.put(item.getSymbol(), item.getValue())));
    }

    public void setMainSceneController(RitzpaMainSceneController ritzpa){
        if (ritzpaMainSceneController == null) this.ritzpaMainSceneController = ritzpa;
    }

    public void setMainSceneController(RitzpaMainSceneController ritzpa, UserDTO user) {
        // Initialize
        if (ritzpaMainSceneController == null) {
            setMainSceneController(ritzpa);
            userTable.setItems(getItemList(user.getItems()));
            updateUserCharts(user.getItems(), null);
        }
    }

    public void updateUser(UserDTO user, String stockSymbolOrder) {
        updateUserCharts(user.getItems(), stockSymbolOrder);
        userTable.getItems().remove(0, userTable.getItems().size());
        userTable.setItems(getItemList(user.getItems()));
    }

    public void showTrade(Boolean show) {
        tradeScrollPane.setVisible(show);
        mainSceneVBox.setVisible(!show);
    }

    public void addStocks(List<String> stocksNames) {
        ObservableList<String> list = FXCollections.observableArrayList(stocksNames);
        stocksComboBox.setItems(list);
    }

    @FXML
    void handleCommitTrade() {
        try {
            ritzpaMainSceneController.addTransaction(this.getLMTOrMKT(), this.getBuyOrSell(), this.getStockSymbol(), this.getPrice(), this.getQuantity());
        } catch (IllegalArgumentException ex) {
            ritzpaMainSceneController.setSystemMessage(ex.getMessage());
        }
    }

    public int getBuyOrSell() {
        if (!sellRadioButton.isSelected() && !buyRadioButton.isSelected()){
            throw new IllegalArgumentException("ERROR! Please choose order of transaction: Buy or Sell!");
        }
        RadioButton selectedRadioButton = (RadioButton) buyOrSell.getSelectedToggle();
        String toggleGroupValue = selectedRadioButton.getText();
        if (toggleGroupValue.equals("Buy")) {
            return 1;
        }
        else {
            return 2;
        }
    }

    public int getLMTOrMKT() {
        if (!MKTRadioButton.isSelected() && !LMTRadioButton.isSelected()){
            throw new IllegalArgumentException("ERROR! Please choose type of transaction: LMT or MKT!");
        }
        RadioButton selectedRadioButton = (RadioButton) lmtOrMkt.getSelectedToggle();
        String toggleGroupValue = selectedRadioButton.getText();
        if (toggleGroupValue.equals("LMT")) {
            return 1;
        }
        else {
            return 2;
        }
    }

    // return the slider choice
    public int getQuantity() { /////////////////////////////////////////////// have problem! - get value return double
        return (int) quantitySlider.getValue();
    }

    // return the slider choice
    public int getPrice() { /////////////////////////////////////////////// have problem! - get value return double
        return (int) priceSlider.getValue();
    }

    // return the combobox choice
    public String getStockSymbol() {
        if (stocksComboBox.getSelectionModel().isEmpty()){
            throw new IllegalArgumentException("ERROR! Please choose a stock!");
        }
        return stocksComboBox.getValue();
    }

    public void clearForm() {
        buyRadioButton.selectedProperty().set(false);
        sellRadioButton.selectedProperty().set(false);
        LMTRadioButton.selectedProperty().set(false);
        MKTRadioButton.selectedProperty().set(false);

        quantitySlider.adjustValue(1);
        priceSlider.adjustValue(1);

        stocksComboBox.getSelectionModel().clearSelection();
    }

    public void buyRadioButtonSelected() {
        stocksComboBox.getItems().remove(0, stocksComboBox.getItems().size() );

        for (Tab tab: ritzpaMainSceneController.stocksTabPane.getTabs()) {
            stocksComboBox.getItems().add(tab.getText());
        }

        // Choose Max
        quantitySlider.setMax(SLIDER_MAX);
        // Reset Quantity
        quantitySlider.setValue(1.0);
    }

    public void sellRadioButtonSelected() {
        stocksComboBox.getItems().remove(0, stocksComboBox.getItems().size());
        List<String> res = ritzpaMainSceneController.getUserAvailableStocks();
        for (String symbol: res) {
            stocksComboBox.getItems().add(symbol);
        }

        // If we made a combobox selection
        if (!stocksComboBox.getSelectionModel().isEmpty()){
            int quantity = ritzpaMainSceneController.getStockRealQuantity(stocksComboBox.getSelectionModel().getSelectedItem());
            if (quantity != -1) quantitySlider.setMax(quantity);
        }

        // Reset Quantity
        quantitySlider.setValue(1.0);
    }

    public void handleComboBoxSelection() {
        if (sellRadioButton.isSelected()){
            int quantity = ritzpaMainSceneController.getStockRealQuantity(stocksComboBox.getSelectionModel().getSelectedItem());
            if (quantity != -1) quantitySlider.setMax(quantity);
        }
    }

    public void handleExitButton() {
        ritzpaMainSceneController.returnUser();
    }
}
