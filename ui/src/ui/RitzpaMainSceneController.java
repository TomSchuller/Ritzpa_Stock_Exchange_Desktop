package ui;

import dto.Item;
import dto.TransactionDTO;
import dto.UserDTO;
import engine.Engine;
import engine.EngineImpl;
import engine.LoadFileTask;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.FileSystems;
import java.util.*;
import java.util.List;

import static ui.CommonResourcesPaths.*;

public class RitzpaMainSceneController {
    @FXML VBox mainVbox;
    @FXML TabPane usersTabPane;
    @FXML TabPane stocksTabPane;
    @FXML ScrollPane loadScrollPane;
    @FXML HBox toolBarHBox;
    @FXML Label systemMessagesLabel;
    @FXML MenuItem loadXmlFile;

    @FXML private Label progressPercentLabel;
    @FXML private Label taskMessageLabel;
    @FXML private ProgressBar taskProgressBar;

    private Engine ritzpa;
    private String currentDirectory;
    private final Map<String, UserTabController> usersControllers;
    private final Map<String, StocksController> stocksControllers;
    private final SimpleBooleanProperty isFileCollected;


    @FXML
    public void initialize() {
        currentDirectory = FileSystems.getDefault().getPath(".").toString();
        toolBarHBox.visibleProperty().bind(isFileCollected);
    }

    public RitzpaMainSceneController() {
        usersControllers = new HashMap<>();
        stocksControllers = new HashMap<>();
        isFileCollected = new SimpleBooleanProperty(false);
    }

    @FXML
    public void loadFile() {
        loadXmlFile.setDisable(true);
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("XML Files", "*.xml"));

        fileChooser.setInitialDirectory(new File(currentDirectory));
        File selectedFile = fileChooser.showOpenDialog(new Stage());

        // If user didn't choose file
        if (selectedFile != null) {
            currentDirectory = selectedFile.getPath().substring(0, selectedFile.getPath().lastIndexOf("\\"));
            setSystemMessage("Loading file");
            // clear screen
            handleShowProgressBar();

            // If this is the first time
            if (ritzpa == null) {
                ritzpa = new EngineImpl();
                LoadFileTask task = ritzpa.load(selectedFile.getPath());

                Runnable onFinish = () -> { // what to do when the task ended
                    isFileCollected.set(true); //is loaded ended
                    InitializeUsersTabPane();
                    InitializeStocksTabPane();
                    handleShowUsers();
                    setSystemMessage(selectedFile.getName() + " loaded successfully!");
                    loadXmlFile.setDisable(false);
                };

                // bind them to controller
                this.bindTaskToUIComponents(task, onFinish);

                ritzpa.startThread(task);

                // read if i need setOnsuccess/ exception/ cancelled
                task.setOnFailed(e -> {
                    Throwable exc = task.getException();

                    //init progress bar
                    this.taskMessageLabel.textProperty().unbind();
                    this.progressPercentLabel.textProperty().unbind();
                    this.taskProgressBar.progressProperty().unbind();

                    this.taskMessageLabel.setText("");
                    this.progressPercentLabel.setText("0");

                    // add small delay so u can see the problem

                    handleShowUsers();
                    setSystemMessage(exc.getMessage());

                    loadXmlFile.setDisable(false);
                    //init ritzpa
                    ritzpa = null;
                });
            }

            // If not first time
            else {
                Engine temp = new EngineImpl();
                temp.load(selectedFile.getPath());
                LoadFileTask task = temp.load(selectedFile.getPath());

                Runnable onFinish = () -> { // what to do when the task ended
                    //init components
                    usersTabPane.getTabs().clear();
                    stocksTabPane.getTabs().clear();
                    ritzpa = temp;
                    isFileCollected.set(true); //is loaded ended
                    InitializeUsersTabPane();
                    InitializeStocksTabPane();
                    handleShowUsers();
                    setSystemMessage(selectedFile.getName() + " loaded successfully!");
                    loadXmlFile.setDisable(false);
                };

                // bind them to controller
                this.bindTaskToUIComponents(task, onFinish);

                temp.startThread(task);

                // read if i need setOnsuccess/ exception/ cancelled
                task.setOnFailed(e -> {
                    Throwable exc = task.getException();

                    //init progress bar
                    this.taskMessageLabel.textProperty().unbind();
                    this.progressPercentLabel.textProperty().unbind();
                    this.taskProgressBar.progressProperty().unbind();

                    this.taskMessageLabel.setText("");
                    this.progressPercentLabel.setText("0");
                    loadXmlFile.setDisable(false);
                    // add small delay so u can see the problem

                    handleShowUsers();
                    setSystemMessage(exc.getMessage());
                });
            }
        }
        else {
            loadXmlFile.setDisable(false);
        }
    }


    public void InitializeUsersTabPane() { //// **** CHANGE **** ////
        try {
            for (String userName : ritzpa.getUserNames()) {
                FXMLLoader fxmlLoader = new FXMLLoader();
                URL url = getClass().getResource(USER_FXML_INCLUDE_RESOURCE);
                fxmlLoader.setLocation(url);

                Node newUserNode = fxmlLoader.load();
                UserTabController newUserController = fxmlLoader.getController();

                newUserController.setMainSceneController(this, ritzpa.getUser(userName));
                usersControllers.put(userName, newUserController);

                Tab newTab = new Tab(userName);
                newTab.setContent(newUserNode);
                usersTabPane.getTabs().add(newTab);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void InitializeStocksTabPane() {
        try {
            for (String stockName : ritzpa.getStocksNames()) {
                FXMLLoader fxmlLoader = new FXMLLoader();
                URL url = getClass().getResource(STOCKS_FXML_INCLUDE_RESOURCE);
                fxmlLoader.setLocation(url);

                Node newStockNode = fxmlLoader.load();
                StocksController newStockController = fxmlLoader.getController();
                newStockController.setMainSceneController(this, ritzpa.getStock(stockName));
                stocksControllers.put(stockName, newStockController);
                Tab newTab = new Tab(stockName);
                newTab.setContent(newStockNode);
                stocksTabPane.getTabs().add(newTab);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void handleShowUsers() {
        setSystemMessage("Showing Users!");
        stocksTabPane.setVisible(false);
        //stocksTabPane.setDisable(true);

        loadScrollPane.setVisible(false);
       // loadScrollPane.setDisable(true);

        usersTabPane.setVisible(true);
       // usersTabPane.setDisable(false);
    }

    public void handleShowStocks() {
        setSystemMessage("Showing Stocks!");
        usersTabPane.setVisible(false);
        //usersTabPane.setDisable(true);

        loadScrollPane.setVisible(false);
        //loadScrollPane.setDisable(true);

        stocksTabPane.setVisible(true);
        //stocksTabPane.setDisable(false);
    }

    public void handleShowProgressBar() {
        stocksTabPane.setVisible(false);
        //stocksTabPane.setDisable(true);

        usersTabPane.setVisible(false);
        //usersTabPane.setDisable(true);

        loadScrollPane.setVisible(true);
        //loadScrollPane.setDisable(false);

    }

    public void setUsersTabPane(String symbol) {
        usersControllers.forEach((user, controller) -> controller.updateUser(ritzpa.getUser(user), symbol));
    }

    public void setStocksTabPane(String symbol, Integer dealsMadeSize) {
        stocksControllers.get(symbol).updateStockChart(ritzpa.getStock(symbol), dealsMadeSize);
    }

    public void handleCommitTrade() {
        String userName = usersTabPane.getSelectionModel().getSelectedItem().getText();
        int userTabIndex = usersTabPane.getSelectionModel().getSelectedIndex();
        // Get trade scene
        usersControllers.get(userName).addStocks(ritzpa.getStocksNames());
        // Disable all other tabs
        handleShowUsers();
        setSystemMessage("Showing Trade Request!");
        disableEnableOtherTabs(userTabIndex, true);
        // Get user and enable show the trade scene
        usersControllers.get(userName).showTrade(true);

    }

    private void disableEnableOtherTabs(int selectedIndex, boolean disable) { //// **** CHANGE **** ////
        int index = 0;
        for (Tab tab : usersTabPane.getTabs()) {
            if (index != selectedIndex) {
                tab.setDisable(disable);
            }
            ++index;
        }
    }

    /* TODO:
    FRIDAY - ??:
    3. ComboBox no items - write on comboBox "No items" (if size == 0)

    SATURDAY -?s?:
    1. readME -- 1 hours
    2. Finishing touches...


    CSS - - 2 css and 1 default -- 1-2 day for each
        table- lots of elems i dont know, there is explanation here: https://edencoding.com/style-tableview-javafx/
        chart from https://docs.oracle.com/javafx/2/charts/css-styles.htm
        bug- when click of a stock paints it white cant see.. check about it

        upgrade- change the loading bar change
        upgrade- the table slider looks way cool copy too all styles

        change the tabs menu bar color (all tabs)
     */

    public void addTransaction(int type, int order, String stockSymbol, int price, int amount) { //// **** CHANGE **** ////
        String userName = usersTabPane.getSelectionModel().getSelectedItem().getText();
        String baseMessage = "Hey " + userName + ", ";
        String otherMessage;
        //Make new order
        List<TransactionDTO> dealsMade =  ritzpa.order(type, order, stockSymbol, amount, price, userName);
        // Update Users
        // Update stocks
        setStocksTabPane(stockSymbol, dealsMade.size());
        if(dealsMade.size() > 0) { // deal made
            setUsersTabPane(stockSymbol);
        }
        // Clear Commit Trade Page
        usersControllers.get(userName).clearForm();
        // Return other user tabs
        disableEnableOtherTabs(usersTabPane.getSelectionModel().getSelectedIndex(), false);
        usersTabPane.setDisable(false);
        // Return user page
        usersControllers.get(userName).showTrade(false);
        /// Pending buy or sell
        if (dealsMade.size() == 0 ) {
            if (order == 1) {
                // Successfully committed a purchase order! Please check the updated GOOGL pending purchases tab!
                otherMessage = "Successfully committed a purchase order! Please check the updated " + stockSymbol + " pending purchases tab!";
            }

            else {
                otherMessage = "Successfully committed a sell order! Please check the updated " + stockSymbol + " pending sells tab!";
            }
        }
        /// Full deal
        else if (isFullDeal(dealsMade, amount)) {
            otherMessage = "Successfully committed a full transaction! Please check the updated "  + stockSymbol + " transactions made tab!";
        }
        /// Partial deal
        else {
            if (order == 1){
                otherMessage = "Successfully committed a partial transaction! Please check the updated "  + stockSymbol + " pending purchases and transactions made tab!";
            }
            else {
                otherMessage = "Successfully committed a partial transaction! Please check the updated "  + stockSymbol + " pending sells and transactions made tab!";
            }
        }
        setSystemMessage(baseMessage + otherMessage);
    }

    public boolean isFullDeal(List<TransactionDTO> dealsMade, int quantity) {
        int sum = quantity;
        for (TransactionDTO deal : dealsMade) {
            sum -= deal.getQuantity();
        }
        return (sum == 0);
    }

    public List<String> getUserAvailableStocks() {
        String userName = usersTabPane.getSelectionModel().getSelectedItem().getText();
        List<String> res = new ArrayList<>();

        UserDTO user = ritzpa.getUser(userName);
        for (Item item : user.getItems()) {
            res.add(item.getSymbol());
        }
        return res;
    }

    public int getStockRealQuantity(String stockName) {
        for (Item item : ritzpa.getUser(usersTabPane.getSelectionModel().getSelectedItem().getText()).getItems()) {
            if (item.getSymbol().equals(stockName)) {
                return item.getRealQuantity();
            }
        }
        return -1;
    }


    public void setSystemMessage(String message) {
        systemMessagesLabel.setText(message);
    }

    public void returnUser() {
        String userName = usersTabPane.getSelectionModel().getSelectedItem().getText();
        // Clear Commit Trade Page
        usersControllers.get(userName).clearForm();
        // Return other user tabs
        disableEnableOtherTabs(usersTabPane.getSelectionModel().getSelectedIndex(), false);
        usersTabPane.setDisable(false);
        // Return user page
        usersControllers.get(userName).showTrade(false);
        // Set user message
        setSystemMessage(userName + " canceled Commit Trade operation!");
    }

    public Engine getEngine() {
        return ritzpa;
    }

    private void bindTaskToUIComponents(Task<Boolean> aTask, Runnable onFinish) {
        // task message
        taskMessageLabel.textProperty().bind(aTask.messageProperty());

        // task progress bar
        taskProgressBar.progressProperty().bind(aTask.progressProperty());

        // task percent label
        progressPercentLabel.textProperty().bind(
                Bindings.concat(
                        Bindings.format(
                                "%.0f",
                                Bindings.multiply(
                                        aTask.progressProperty(),
                                        100)),
                        " %"));

        // task cleanup upon finish
        aTask.valueProperty().addListener((observable, oldValue, newValue) -> onTaskFinished(Optional.ofNullable(onFinish)));
    }

    public void onTaskFinished(Optional<Runnable> onFinish) {
        this.taskMessageLabel.textProperty().unbind();
        this.progressPercentLabel.textProperty().unbind();
        this.taskProgressBar.progressProperty().unbind();
        onFinish.ifPresent(Runnable::run);
    }

    public void styleDefault(ActionEvent actionEvent) {
       Scene scene = usersTabPane.getScene();
       scene.getStylesheets().clear();
       scene.getStylesheets().add(getClass().getResource(STYLE1).toExternalForm()); // using by class loader
    }

    public void styleDarkMode(ActionEvent actionEvent) {
        Scene scene = usersTabPane.getScene();
        scene.getStylesheets().clear();
        scene.getStylesheets().add(getClass().getResource(STYLE2).toExternalForm()); // using by class loader
    }

    public void styleSmurfs(ActionEvent actionEvent) {
        Scene scene = usersTabPane.getScene();
        scene.getStylesheets().clear();
        scene.getStylesheets().add(getClass().getResource(STYLE3).toExternalForm()); // using by class loader
    }

    public void about(ActionEvent actionEvent) {
        File file = new File("ui/src/ui/resources/readme.pdf");
        if (Desktop.isDesktopSupported()) {
            new Thread(() -> {
                try {
                    Desktop.getDesktop().open(file);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }
}
