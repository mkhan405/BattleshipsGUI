import java.util.ArrayList;
import java.util.HashMap;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.scene.text.Text;

public class GuiClient extends Application{
    private Text welcome, choose, nameError, prompt, remaining, selected, requiredBlocks, orientationSelected, error, currTurn, remainingEnemy;
    private Button onlineButton, botButton, verticalButton, horizontalButton, confirmButton, hitButton;
    private Button battleship, cruiser, submarine, carrier, destroyer, selectedShip = null;
    private GridPane playerBoatPane, enemyBoatPane;
    private TextField nameTextField, messageField = new TextField();
    private HBox buttonBox, middleHBox = new HBox(50);
    ListView<String> chatLog = new ListView<>();
    private VBox welcomeBox, boatSelectBox, orientationBox, mainVBox, topTextBox, gameBox, chatBox = new VBox(10, chatLog,messageField);
    private Message client = new Message();
    private ArrayList<Message> messages = new ArrayList<>();
    private HashMap<String, Scene> sceneMap;
    private Client clientConnection;
    private final int numColumns = 10, numRows = 10, cellSize = 30;
    private String currentOrientation = null, username, opponent, sessionID = null, firstPlayer, secondPlayer;
    private int remainingBoats = 5, boatSize;
    private Rectangle currChosenCell = null;
    private ArrayList<ArrayList<Integer>> boatCells, chosenCell;

    private String[] boatImages = {
            "shiphead.png",
            "shipmiddle.png",
            "shiptail.png",
            "shipheadvertical.png",
            "shipmiddlevertical.png",
            "shiptailvertical.png"
    };

    public static void main(String[] args) {
        launch(args);
    }
    @Override
    public void start(Stage primaryStage) throws Exception {
        clientConnection = new Client(data->
                Platform.runLater(()->{
                    if (data instanceof Message) {
                        Message message = (Message) data;

                        switch (message.type) {
                            case "user_registered":  // New user has joined the server
//                                messages.add(message);
                                choose.setText("Play with another player or with the AI");
                                welcomeBox.getChildren().remove(nameTextField);
                                welcomeBox.getChildren().add(buttonBox);
                                nameError.setText("");
                                break;
                            case "registration_error":  // username already exists
                                nameError.setText("Username already exists! Choose a different username");
                                break;
                            case "wait_for_opponent":
                                choose.setText("Waiting for opponent");
                                break;
                            case "start_session":
                                sessionID = message.content;

                                firstPlayer = sessionID.substring(sessionID.indexOf("p1=") + "p1=".length(), sessionID.indexOf("p2=") - 1);
                                secondPlayer = sessionID.substring(sessionID.indexOf("p2=") + "p2=".length());

                                sessionID = message.content.split(";")[0];

                                if (firstPlayer.equals(username)) {
                                    opponent = secondPlayer;
                                } else {
                                    opponent = firstPlayer;
                                }

                                boatPlace(primaryStage);
                                break;
                            case "indiv_message_ok":
                                chatLog.getItems().add(message.username + " to you: " + message.content);
                                break;
                            case "start_AI_session":
                                sessionID = message.content.split(";")[0];
                                boatPlace(primaryStage);
                                break;
                            case "wait_for_other_player_boats":
                                error.setText("Waiting for opponent...");
                                break;
                            case "start_game":
                                gamePlay(primaryStage);
                                break;
                            case "hit":
                                currChosenCell.setFill(Color.ORANGE);
                                try {Thread.sleep(2000);} catch (InterruptedException ex) {}
                                gameBox.getChildren().clear();
                                gameBox.getChildren().add(playerBoatPane);
                                currChosenCell = null;
                                break;
                            case "miss":
                                currChosenCell.setFill(Color.DARKGRAY);
                                try {Thread.sleep(2000);} catch (InterruptedException ex) {}
                                gameBox.getChildren().clear();
                                gameBox.getChildren().add(playerBoatPane);
                                currChosenCell = null;
                                break;
                            case "sink":
                                ArrayList<Integer> sinkCell = message.cells.get(0);
                                Rectangle targetCell = (Rectangle) getNodeFromGridPane(playerBoatPane, sinkCell.get(0), sinkCell.get(1));
                                System.out.println(sinkCell.get(0));
                                System.out.println(sinkCell.get(1));
                                targetCell.setFill(Color.INDIANRED);
                            case "continue":
                                try {Thread.sleep(2000);} catch (InterruptedException ex) {}

                                gameBox.getChildren().clear();
                                gameBox.getChildren().addAll(enemyBoatPane, hitButton);
                                break;
                        }

                    }
                })
        );

        clientConnection.start();

        primaryStage.setOnCloseRequest(t -> {
            Platform.exit();
            System.exit(0);
        });

        sceneMap = new HashMap<>();
        sceneMap.put("welcome", createWelcomePage());

        primaryStage.setScene(sceneMap.get("welcome"));
        primaryStage.setTitle("Battleships");
        primaryStage.show();
    }

    private Scene createWelcomePage() {
        welcome = new Text("Welcome to Battleships!");
        welcome.setStyle("-fx-font-size: 45; -fx-font-weight: bold; -fx-fill: white; -fx-font-family: Arial;");

        choose = new Text("Play with another player or with the AI");
        choose.setStyle("-fx-font-size: 20; -fx-font-weight: bold; -fx-fill: white; -fx-font-family: Arial; ");

        nameError = new Text();
        nameError.setStyle(" -fx-font-size: 16; -fx-font-weight: bold; -fx-fill: #8f1212; -fx-font-family: Arial;");

        onlineButton = new Button("Online");
        styleButton(onlineButton, "linear-gradient(#448aff, #005ecb)", "linear-gradient(#82b1ff, #447eff)");

        botButton = new Button("AI");
        styleButton(botButton, "linear-gradient(#f0bf2b, #d4a004)", "linear-gradient(#f7e35c, #edd428)");

        nameTextField = new TextField();
        nameTextField.setMaxWidth(300);
        nameTextField.setStyle("-fx-text-fill: black; -fx-font-size: 16; -fx-background-radius: 10; -fx-font-family: Arial; -fx-pref-width: 30px;");

        onlineButton.setOnAction(e -> {
            clientConnection.send(new Message("request_session","multiplayer",nameTextField.getText()));
            welcomeBox.getChildren().remove(buttonBox);
        });

        botButton.setOnAction(e -> {
            clientConnection.send(new Message("request_session","AI",nameTextField.getText()));
        });



        nameTextField.setOnAction(e->{
            if (nameTextField.getText().isEmpty()) {
                nameError.setText("Invalid Empty Username");
            }
            else {
                clientConnection.send(new Message("new_user",nameTextField.getText()));
                username = nameTextField.getText();
                nameError.setText("");
                nameTextField.clear();
            }
        });

        buttonBox = new HBox(40, onlineButton, botButton);
        buttonBox.setAlignment(Pos.CENTER);

        welcomeBox = new VBox(20, welcome, choose, nameError, nameTextField);
        welcomeBox.setAlignment(Pos.CENTER);

        BorderPane pane = new BorderPane(welcomeBox);
        pane.setStyle("-fx-background-color: #383838;");

        return new Scene(pane, 550, 550);
    }

    private Scene createBoatPlaceScene(){
        boatSelectBox = new VBox(20, remaining, carrier, battleship, cruiser, submarine, destroyer);
        boatSelectBox.setAlignment(Pos.CENTER);

        orientationBox = new VBox(20, selected, requiredBlocks, orientationSelected, verticalButton, horizontalButton);
        orientationBox.setAlignment(Pos.CENTER);

        messageField.setOnAction(e->{
            String content = messageField.getText();
            clientConnection.send(new Message("indiv_messsage", content, username, opponent));
            chatLog.getItems().add("You to " + opponent + ": " + content);
            messageField.setText("");
        });

        chatLog.setStyle("-fx-background-insets: 0; -fx-padding: 5; -fx-border-insets: 0; -fx-background-radius: 5; -fx-border-radius: 5; -fx-border-color: #e0e0e0; -fx-border-width: 1;");


        middleHBox.getChildren().add(playerBoatPane);
        middleHBox.getChildren().add(boatSelectBox);
        if(sessionID != null) {
            middleHBox.getChildren().add(chatBox);
        }

//        middleHBox = new HBox(50, playerBoatPane, boatSelectBox,  chatBox);
        middleHBox.setAlignment(Pos.CENTER);

        mainVBox = new VBox(75, prompt, middleHBox, error);
        mainVBox.setAlignment(Pos.CENTER);

        BorderPane pane = new BorderPane(mainVBox);
        pane.setPadding(new Insets( 20));
        pane.setStyle("-fx-background-color: Grey");

        BorderPane.setAlignment(prompt, Pos.CENTER);
        return new Scene(pane, 700, 700);
    }

    private void selectShip(Button ship) {
        error.setText("");
        if (selectedShip != null) {
            selectedShip.setDisable(false);
        }
        selectedShip = ship;
        ship.setDisable(true);

        boatSize = (int) selectedShip.getUserData();

        selected.setText("Selected: " + ship.getText().split(" - ")[0]);
        requiredBlocks.setText("Required Blocks: " + ship.getUserData());

        middleHBox.getChildren().remove(1);
        middleHBox.getChildren().add(1, orientationBox);
    }

    private void boatPlace(Stage primaryStage) {
        prompt = new Text("Place Your Boats, " + username);
        prompt.setStyle("-fx-font-size: 36; -fx-font-weight: bold; -fx-text-fill: black; -fx-font-family: Arial;");

        carrier = new Button("CARRIER - 5");
        battleship = new Button("BATTLESHIP - 4");
        cruiser = new Button("CRUISER - 3");
        submarine = new Button("SUBMARINE - 3");
        destroyer = new Button("DESTROYER - 2");

        // Length, Count of ship placement
        carrier.setUserData(5);
        battleship.setUserData(4);
        cruiser.setUserData(3);
        submarine.setUserData(3);
        destroyer.setUserData(2);

        carrier.setOnAction(e -> selectShip(carrier));
        battleship.setOnAction(e -> selectShip(battleship));
        cruiser.setOnAction(e -> selectShip(cruiser));
        submarine.setOnAction(e -> selectShip(submarine));
        destroyer.setOnAction(e -> selectShip(destroyer));

        styleRectangleButton(carrier);
        styleRectangleButton(battleship);
        styleRectangleButton(cruiser);
        styleRectangleButton(submarine);
        styleRectangleButton(destroyer);

        selected = new Text();
        requiredBlocks = new Text();
        orientationSelected = new Text("Select Orientation");
        error = new Text("");

        selected.setStyle("-fx-font-size: 16; -fx-font-weight: normal; -fx-fill: black; -fx-font-family: Arial; ");
        requiredBlocks.setStyle("-fx-font-size: 16; -fx-font-weight: normal; -fx-fill: black; -fx-font-family: Arial; ");
        orientationSelected.setStyle("-fx-font-size: 16; -fx-font-weight: normal; -fx-fill: black; -fx-font-family: Arial; ");
        error.setStyle(" -fx-font-size: 16; -fx-font-weight: bold; -fx-fill: #8f1212; -fx-font-family: Arial;");

        verticalButton = new Button("Vertical");
        horizontalButton = new Button("Horizontal");
        confirmButton = new Button("Confirm");
        styleRectangleButton(verticalButton);
        styleRectangleButton(horizontalButton);
        styleButton(confirmButton, "linear-gradient(#78c800, #558b2f)", "linear-gradient(#9eff56, #76d25b)");
        confirmButton.setOnAction(e -> {
            if (firstPlayer.equals(username)) {
                clientConnection.send(new Message("p1_boats", sessionID, username, boatCells));
            }
            else {
                clientConnection.send(new Message("p2_boats", sessionID, username, boatCells));
            }
        });


        verticalButton.setOnAction(e -> {
            currentOrientation = "Vertical";
            verticalButton.setDisable(true);
            horizontalButton.setDisable(false);
            orientationSelected.setText("Orientation Selected: Vertical");
            error.setText("");
        });
        horizontalButton.setOnAction(e ->{
            currentOrientation = "Horizontal";
            horizontalButton.setDisable(true);
            verticalButton.setDisable(false);
            orientationSelected.setText("Orientation Selected: Horizontal");
            error.setText("");
        });

        remaining = new Text("Remaining Boats: " + remainingBoats);
        remaining.setStyle("-fx-font-size: 16; -fx-font-weight: normal; -fx-fill: black; -fx-font-family: Arial; ");

        playerBoatPane = new GridPane();
        boatCells = new ArrayList<>();

        // Add row labels (1 to 10)
        for (int col = 0; col < numColumns; col++) {
            Label colLabel = new Label(Integer.toString(col + 1));
            colLabel.setStyle("-fx-font-size: 15; -fx-font-weight: bold; -fx-text-fill: black; -fx-font-family: Arial;");
            colLabel.setMinSize(cellSize, cellSize);
            colLabel.setAlignment(Pos.CENTER);
            playerBoatPane.add(colLabel, col + 1, 0); // Offset by one for the column labels
        }

        // Add column labels (A to J)
        char rowChar = 'A';
        for (int row = 0; row < numRows; row++) {
            Label rowLabel = new Label(String.valueOf((char)(rowChar + row)));
            rowLabel.setStyle("-fx-font-size: 15; -fx-font-weight: bold; -fx-text-fill: black; -fx-font-family: Arial;");
            rowLabel.setMinSize(cellSize, cellSize);
            rowLabel.setAlignment(Pos.CENTER);
            playerBoatPane.add(rowLabel, 0, row + 1); // Offset by one for the row labels
        }

        // Populate the grid
        for (int row = 1; row <= numRows; row++) {
            for (int col = 1; col <= numColumns; col++) {
                Rectangle cell = new Rectangle(cellSize, cellSize);
                cell.setStroke(Color.BLACK);
                cell.setFill(Color.TRANSPARENT);
                cell.setUserData(true);
                int finalRow = row; // Adjust for zero-based index
                int finalCol = col; // Adjust for zero-based index
                cell.setOnMouseClicked(event -> placeShip(finalRow, finalCol));
                playerBoatPane.add(cell, col, row); // The grid content starts from (1,1) due to labels
            }
        }

        sceneMap.put("prep", createBoatPlaceScene());
        primaryStage.setScene(sceneMap.get("prep"));
    }

    private void gamePlay(Stage primaryStage) {
        chosenCell = new ArrayList<>();

        currTurn = new Text("It's Your Turn!");
        currTurn.setStyle("-fx-font-size: 16; -fx-font-weight: normal; -fx-fill: black; -fx-font-family: Arial; ");

        remainingBoats = 5;
        remainingEnemy = new Text("Remaining Enemy Boats: " + remainingBoats);
        remainingEnemy.setStyle("-fx-font-size: 16; -fx-font-weight: normal; -fx-fill: black; -fx-font-family: Arial; ");

        hitButton = new Button("Hit!");
        hitButton.setAlignment(Pos.CENTER);
        styleButton(hitButton, "linear-gradient(#78c800, #558b2f)", "linear-gradient(#9eff56, #76d25b)");
        hitButton.setOnAction(e -> {
            if (currChosenCell == null) {
                return;
            }
            int col = GridPane.getColumnIndex(currChosenCell);
            int row = GridPane.getRowIndex(currChosenCell);

            chosenCell.clear();
            ArrayList<Integer> tempChosenCell = new ArrayList<>();
            tempChosenCell.add(col);
            tempChosenCell.add(row);
            chosenCell.add(tempChosenCell);

            clientConnection.send(new Message("turn", sessionID, username, chosenCell));
        });

        enemyBoatPane = new GridPane();
        enemyBoatPane.setAlignment(Pos.CENTER);

        // Add row labels (1 to 10)
        for (int col = 0; col < numColumns; col++) {
            Label colLabel = new Label(Integer.toString(col + 1));
            colLabel.setStyle("-fx-font-size: 15; -fx-font-weight: bold; -fx-text-fill: black; -fx-font-family: Arial;");
            colLabel.setMinSize(cellSize, cellSize);
            colLabel.setAlignment(Pos.CENTER);
            enemyBoatPane.add(colLabel, col + 1, 0); // Offset by one for the column labels
        }

        // Add column labels (A to J)
        char rowChar = 'A';
        for (int row = 0; row < numRows; row++) {
            Label rowLabel = new Label(String.valueOf((char)(rowChar + row)));
            rowLabel.setStyle("-fx-font-size: 15; -fx-font-weight: bold; -fx-text-fill: black; -fx-font-family: Arial;");
            rowLabel.setMinSize(cellSize, cellSize);
            rowLabel.setAlignment(Pos.CENTER);
            enemyBoatPane.add(rowLabel, 0, row + 1); // Offset by one for the row labels
        }

        // Populate the grid
        for (int row = 1; row <= numRows; row++) {
            for (int col = 1; col <= numColumns; col++) {
                Rectangle cell = new Rectangle(cellSize, cellSize);
                cell.setStroke(Color.BLACK);
                cell.setFill(Color.TRANSPARENT);
                cell.setUserData(true);
                int finalRow = row; // Adjust for zero-based index
                int finalCol = col; // Adjust for zero-based index
                cell.setOnMouseClicked(event -> {
                    if (currChosenCell != null) {
                        currChosenCell.setFill(Color.TRANSPARENT);
                    }
                    currChosenCell = (Rectangle) getNodeFromGridPane(enemyBoatPane, finalCol, finalRow);
                    currChosenCell.setFill(Color.DARKGRAY);
                });
                enemyBoatPane.add(cell, col, row); // The grid content starts from (1,1) due to labels
            }
        }

        sceneMap.put("game", createGamePlayScene());
        primaryStage.setScene(sceneMap.get("game"));
    }

    private Scene createGamePlayScene() {
        topTextBox = new VBox(15, currTurn, remainingEnemy);
        topTextBox.setAlignment(Pos.CENTER);
        topTextBox.setPadding(new Insets(10));

        gameBox = new VBox(enemyBoatPane);

        BorderPane pane = new BorderPane();
        pane.setPadding(new Insets( 20));
        pane.setStyle("-fx-background-color: grey");
        BorderPane.setAlignment(hitButton, Pos.CENTER);

        pane.setTop(topTextBox);
        pane.setCenter(gameBox);
        pane.setBottom(hitButton);

        return new Scene(pane, 700, 700);
    }

    private void placeShip(int row, int col) {
        // No ship selected
        if (selectedShip == null) {
            if (remainingBoats != 0) {
                error.setText("Must Choose Boat");
            }
            return;
        }

        if (currentOrientation == null) {
            error.setText("Must Choose Orientation");
            return;
        }

        int shipSize = (int) selectedShip.getUserData();

        if (currentOrientation.equals("Horizontal")) {
            // Ship doesn't fit
            if (col + shipSize > numColumns + 1) {
                error.setText("Out of Bounds");
                return;
            }

            for (int i = 0; i < shipSize; i++) {
                Rectangle targetCell = (Rectangle) getNodeFromGridPane(playerBoatPane, col + i, row);
                if(targetCell.getUserData().equals(false)) {
                    error.setText("Merge with another ship");
                    return;
                }
            }

            for (int i = 0; i < shipSize; i++) {
                int newCol = col + i;
                if (i == 0) {
                    addImageToGridPane(boatImages[0], newCol, row);
                }
                else if (i == shipSize - 1) {
                    addImageToGridPane(boatImages[2], newCol, row);
                }
                else {
                    addImageToGridPane(boatImages[1], newCol, row);
                }
                getNodeFromGridPane(playerBoatPane, newCol, row).setUserData(false);

                ArrayList<Integer> newBoatCells = new ArrayList<>();
                newBoatCells.add(newCol);
                newBoatCells.add(row);
                boatCells.add(newBoatCells);
            }
            // Vertical placement
        } else if(currentOrientation.equals("Vertical")){
            // Ship doesn't fit
            if (row + shipSize > numRows+1) {
                error.setText("Out of Bounds");
                return;
            }
            for (int i = 0; i < shipSize; i++) {
                Rectangle targetCell = (Rectangle) getNodeFromGridPane(playerBoatPane, col, row + i);
                if(targetCell.getUserData().equals(false)) {
                    error.setText("Merge with another ship");
                    return;
                }
            }
            for (int i = 0; i < shipSize; i++) {
                int newRow = row + i;
                if (i == 0) {
                    addImageToGridPane(boatImages[3], col, newRow);
                }
                else if (i == shipSize - 1) {
                    addImageToGridPane(boatImages[5], col, newRow);
                }
                else {
                    addImageToGridPane(boatImages[4], col, newRow);
                }
                getNodeFromGridPane(playerBoatPane, col, newRow).setUserData(false);

                ArrayList<Integer> newBoatCells = new ArrayList<>();
                newBoatCells.add(col);
                newBoatCells.add(newRow);
                boatCells.add(newBoatCells);
            }
        }

        selectedShip.setDisable(true);
        selectedShip = null;
        currentOrientation = null;

        horizontalButton.setDisable(false);
        verticalButton.setDisable(false);

        remaining.setText("Remaining Boats: " + (--remainingBoats));
        orientationSelected.setText("Orientation Selected");
        error.setText("");

        middleHBox.getChildren().remove(1);
        middleHBox.getChildren().add(1, boatSelectBox);

        if (remainingBoats == 0) {
            boatSelectBox.getChildren().add(confirmButton);
        }
    }

    private Node getNodeFromGridPane(GridPane gridPane, int col, int row) {
        for (Node node : gridPane.getChildren()) {
            if (GridPane.getColumnIndex(node) != null && GridPane.getRowIndex(node) != null &&
                    GridPane.getColumnIndex(node) == col && GridPane.getRowIndex(node) == row) {
                return node;
            }
        }
        return null;
    }

    private void styleRectangleButton(Button button){
//        int buttonX = 120;
//        int buttonY = 40;

        button.setStyle("-fx-font-size: 14px; " +
                "-fx-background-color: " + "linear-gradient(#73777d, #959aa1)" + "; " +
                "-fx-text-fill: black; " +
                "-fx-pref-width: 120px; " +
                "-fx-pref-height: 40px; " +
                "-fx-border-radius: 20; " +
                "-fx-background-radius: 20;");
        button.setEffect(new DropShadow(10, Color.BLACK));

        // Hover effect
        button.setOnMouseEntered(e -> button.setStyle("-fx-font-size: 14px; " +
                "-fx-background-color: " + "linear-gradient(#a2a4a6, #bbbdbf)" + "; " +
                "-fx-text-fill: black; " +
                "-fx-pref-width: 125px; " +  // Slightly larger on hover
                "-fx-pref-height: 45px; " +
                "-fx-border-radius: 20; " +
                "-fx-background-radius: 20; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.8), 10, 0, 0, 0);"));
        button.setOnMouseExited(e -> button.setStyle("-fx-font-size: 14px; " +
                "-fx-background-color: " + "linear-gradient(#73777d, #959aa1)" + "; " +
                "-fx-text-fill: black; " +
                "-fx-pref-width: 120px; " +
                "-fx-pref-height: 40px; " +
                "-fx-border-radius: 20; " +
                "-fx-background-radius: 20; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.6), 10, 0, 0, 0);"));

//        button.setStyle("-fx-font-size: 14; -fx-background-insets: 0; -fx-padding: 0; -fx-text-fill: black; -fx-font-family: Arial;");
//        button.setPrefWidth(buttonX);
//        button.setPrefHeight(buttonY);
//
//        Rectangle rectangle = new Rectangle(buttonX, buttonY);
//        button.setShape(rectangle);
//        button.setPadding(new Insets(10));
    }

    private void styleButton(Button button, String baseColor, String hoverColor) {
        button.setStyle("-fx-font-size: 15px; " +
                "-fx-background-color: " + baseColor + "; " +
                "-fx-text-fill: white; " +
                "-fx-pref-width: 100px; " +
                "-fx-pref-height: 20px; " +
                "-fx-border-radius: 20; " +
                "-fx-background-radius: 20;");
        button.setEffect(new DropShadow(10, Color.BLACK));

        // Hover effect
        button.setOnMouseEntered(e -> button.setStyle("-fx-font-size: 15px; " +
                "-fx-background-color: " + hoverColor + "; " +
                "-fx-text-fill: white; " +
                "-fx-pref-width: 110px; " +  // Slightly larger on hover
                "-fx-pref-height: 35px; " +
                "-fx-border-radius: 20; " +
                "-fx-background-radius: 20; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.8), 10, 0, 0, 0);"));
        button.setOnMouseExited(e -> button.setStyle("-fx-font-size: 15px; " +
                "-fx-background-color: " + baseColor + "; " +
                "-fx-text-fill: white; " +
                "-fx-pref-width: 100px; " +
                "-fx-pref-height: 20px; " +
                "-fx-border-radius: 20; " +
                "-fx-background-radius: 20; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.6), 10, 0, 0, 0);"));
    }

    private void addImageToGridPane(String imagePath, int column, int row) {
        // Create an image object
        Image image = new Image(imagePath);

        // Create an ImageView and set the image to it
        ImageView imageView = new ImageView(image);

        imageView.setFitHeight(cellSize);
        imageView.setFitWidth(cellSize);
        imageView.setPreserveRatio(true);

        // Add the ImageView to the gridpane at the specified column and row
        playerBoatPane.add(imageView, column, row);
    }

    private void removeImageFromGridPane(GridPane gridPane, int column, int row) {
        Node nodeToRemove = null;
        for (Node node : gridPane.getChildren()) {
            // Check the node's column and row position
            if (GridPane.getColumnIndex(node) != null && GridPane.getRowIndex(node) != null
                    && GridPane.getColumnIndex(node) == column && GridPane.getRowIndex(node) == row) {
                if (node instanceof ImageView) { // Additional check if you only want to remove ImageViews
                    nodeToRemove = node;
                    break;
                }
            }
        }

        if (nodeToRemove != null) {
            gridPane.getChildren().remove(nodeToRemove); // Remove the node from the grid
        }
    }
}
