
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
    private Text welcomeText, promptText, errorText, username, promptLabel, remaining, t1,t2,t3, errorT1;
    private Button onlineButton, botButton, backButton, vertical, horizontal;
    private Button battleship, cruiser, submarine, carrier, destroyer;
    private GridPane boatPane;
    private TextField nameTextField;
    private HBox buttonBox, h1, h2;
    private VBox welcomeVBox, ships,orientation, v1;
    private Message client = new Message();
    private ArrayList<String> allUsers = new ArrayList<>();
    private ArrayList<Message> messages = new ArrayList<>();
    private HashMap<String, Scene> sceneMap;
    private Client clientConnection;
    private final int numColumns = 10, numRows = 10;
    private String currentOrientation = null;
    private Button selectedShip = null;


    public static void main(String[] args) {
        launch(args);
    }
    @Override
    public void start(Stage primaryStage) throws Exception {
        clientConnection = new Client(data->{
            Platform.runLater(()->{
                if (data instanceof Message) {
                    Message message = (Message) data;

                    if (message.type.equals("user_registered")){ // New user has joined the server
                        messages.add(message);
//                        username.setText(nameTextField.getText());
                        nameTextField.clear();
                        boatPlace(primaryStage);
                    }
                    else if (message.type.equals("registration_error")){ // username already exists
                        username.setText("");
                        errorText.setText("Username already exists! Choose a different username");
                    }

                }
            });
        });

        clientConnection.start();

        primaryStage.setOnCloseRequest(t -> {
            Platform.exit();
            System.exit(0);
        });

        sceneMap = new HashMap<String, Scene>();
        sceneMap.put("welcome", createWelcomePage());

        primaryStage.setScene(sceneMap.get("welcome"));
        primaryStage.setTitle("Battleships");
        primaryStage.show();
    }

    public Scene createWelcomePage() {
        welcomeText = new Text("Welcome to Battleships!");
        welcomeText.setStyle("-fx-font-size: 45; -fx-font-weight: bold; -fx-fill: white; -fx-font-family: Arial;");

        promptText = new Text("Play with another player or with the AI");
        promptText.setStyle("-fx-font-size: 20; -fx-font-weight: bold; -fx-fill: white; -fx-font-family: Arial; ");

        errorText = new Text();
        errorText.setStyle(" -fx-font-size: 16; -fx-font-weight: bold; -fx-fill: red; -fx-font-family: Arial;");

        onlineButton = new Button("Online");
        styleButton(onlineButton, "linear-gradient(#448aff, #005ecb)", "linear-gradient(#82b1ff, #447eff)");

        botButton = new Button("AI");
        styleButton(botButton, "linear-gradient(#f0bf2b, #d4a004)", "linear-gradient(#f7e35c, #edd428)");

        backButton = new Button("Back");
        styleButton(backButton, "linear-gradient(#ff5252, #c50e29)", "linear-gradient(#ff8a80, #ff5252)");

        nameTextField = new TextField();
        nameTextField.setMaxWidth(300);
        nameTextField.setStyle("-fx-text-fill: black; -fx-font-size: 16; -fx-background-radius: 10; -fx-font-family: Arial; -fx-pref-width: 30px;");

        onlineButton.setOnAction(e -> {
            promptText.setText("You're playing online! What's your username?");
            buttonBox.getChildren().clear();
            buttonBox.getChildren().add(backButton);
            welcomeVBox.getChildren().add(1, nameTextField);
        });

        botButton.setOnAction(e -> {
            promptText.setText("You're playing with the AI! What's your username?");
            buttonBox.getChildren().clear();
            buttonBox.getChildren().add(backButton);
            welcomeVBox.getChildren().add(1, nameTextField);
        });

        backButton.setOnAction(e -> {
            promptText.setText("Play with another player or with the AI");
            welcomeVBox.getChildren().remove(nameTextField);
            buttonBox.getChildren().clear();
            buttonBox.getChildren().add(onlineButton);
            buttonBox.getChildren().add(botButton);
            errorText.setText("");
        });

        username = new Text();
        username.setStyle("-fx-font-size: 20; -fx-font-weight: bold; -fx-text-fill: black; -fx-font-family: Arial;");


        nameTextField.setOnAction(e->{
            if (nameTextField.getText().isEmpty()) {
                errorText.setText("Invalid Empty Username");
            }
            else {
                clientConnection.send(new Message("new_user",nameTextField.getText()));
                username.setText(nameTextField.getText());
                errorText.setText("");
                nameTextField.clear();
            }
        });

        buttonBox = new HBox(40, onlineButton, botButton);
        buttonBox.setAlignment(Pos.CENTER);

        welcomeVBox = new VBox(20, welcomeText,promptText, errorText, buttonBox);
        welcomeVBox.setAlignment(Pos.CENTER);

        BorderPane pane = new BorderPane(welcomeVBox);
        pane.setStyle("-fx-background-color: #383838;");

        return new Scene(pane, 550, 550);
    }

    public Scene createBoatPlaceScene(){ //bug if no users to display add error message
        promptLabel = new Text("Place Your Boats");
        promptLabel.setStyle("-fx-font-size: 36; -fx-font-weight: bold; -fx-text-fill: black; -fx-font-family: Arial;");

        carrier = new Button("CARRIER (5)");
        battleship = new Button("BATTLESHIP (4)");
        cruiser = new Button("CRUISER (3)");
        submarine = new Button("SUBMARINE (3)");
        destroyer = new Button("DESTROYER (2)");

        t1 = new Text("Selected Boat");
        t2 = new Text("Required Blocks");
        t3 = new Text("Orientation Selected");
        errorT1 = new Text("");
        t1.setStyle("-fx-font-size: 16; -fx-font-weight: lighter; -fx-fill: black; -fx-font-family: Arial; ");
        t2.setStyle("-fx-font-size: 16; -fx-font-weight: lighter; -fx-fill: black; -fx-font-family: Arial; ");
        t3.setStyle("-fx-font-size: 16; -fx-font-weight: lighter; -fx-fill: black; -fx-font-family: Arial; ");
        errorT1.setStyle(" -fx-font-size: 16; -fx-font-weight: bold; -fx-fill: red; -fx-font-family: Arial;");

        vertical = new Button("Vertical");
        horizontal = new Button("Horizontal");

        styleRectangleButton(carrier);styleRectangleButton(battleship);styleRectangleButton(cruiser);styleRectangleButton(submarine);styleRectangleButton(destroyer);styleRectangleButton(vertical);styleRectangleButton(horizontal);

        remaining = new Text("Remaining Boats - 5");
        remaining.setStyle("-fx-font-size: 16; -fx-font-weight: lighter; -fx-fill: black; -fx-font-family: Arial; ");


        ships = new VBox(20,remaining,carrier,battleship,cruiser,submarine,destroyer);
        ships.setAlignment(Pos.CENTER);
        orientation = new VBox(20,t1,t2,t3,vertical,horizontal,errorT1);
        orientation.setAlignment(Pos.CENTER);
        h1 = new HBox(100, boatPane,ships);
        h1.setAlignment(Pos.CENTER);
        h2 = new HBox(50, promptLabel, username);
        h2.setAlignment(Pos.CENTER);
        v1 = new VBox(25,h2,h1);
        v1.setAlignment(Pos.CENTER);


        carrier.setUserData(5); // Length, Count of ship placement
        battleship.setUserData(4);
        cruiser.setUserData(3);
        submarine.setUserData(3);
        destroyer.setUserData(2);

        carrier.setOnAction(e -> selectShip(carrier));
        battleship.setOnAction(e -> selectShip(battleship));
        cruiser.setOnAction(e -> selectShip(cruiser));
        submarine.setOnAction(e -> selectShip(submarine));
        destroyer.setOnAction(e -> selectShip(destroyer));

        vertical.setOnAction(e -> {
            currentOrientation = "Vertical";
            errorT1.setText("");
            t3.setText("Orientation Selected: Vertical");
        });
        horizontal.setOnAction(e ->{
            currentOrientation = "Horizontal";
            errorT1.setText("");
            t3.setText("Orientation Selected: Horizontal");
        });

        BorderPane pane = new BorderPane(v1);
        pane.setPadding(new Insets( 20));
        pane.setStyle("-fx-background-color: Grey");

        BorderPane.setAlignment(promptLabel, Pos.CENTER);
        return new Scene(pane, 700, 700);
    }

    private void selectShip(Button ship) {
        if (selectedShip != null) {
            selectedShip.setDisable(false);
        }
        selectedShip = ship;
        ship.setDisable(true);
        h1.getChildren().remove(1);
        h1.getChildren().add(1, orientation);
    }


    public void boatPlace(Stage primaryStage) {
        boatPane = new GridPane();

        int cellSize = 30;

        // Add row labels (1 to 10)
        for (int col = 0; col < numColumns; col++) {
            Label colLabel = new Label(Integer.toString(col + 1));
            colLabel.setStyle("-fx-font-size: 15; -fx-font-weight: bold; -fx-text-fill: black; -fx-font-family: Arial;");
            colLabel.setMinSize(cellSize, cellSize);
            colLabel.setAlignment(Pos.CENTER);
            boatPane.add(colLabel, col+1, 0); // Offset by one for the column labels
        }

        // Add column labels (A to J)
        char rowChar = 'A';
        for (int row = 0; row < numRows; row++) {
            Label rowLabel = new Label(String.valueOf((char)(rowChar + row)));
            rowLabel.setStyle("-fx-font-size: 15; -fx-font-weight: bold; -fx-text-fill: black; -fx-font-family: Arial;");
            rowLabel.setMinSize(cellSize, cellSize);
            rowLabel.setAlignment(Pos.CENTER);
            boatPane.add(rowLabel, 0, row+1); // Offset by one for the row labels
        }

        // Populate the grid
        for (int row = 1; row <= numRows; row++) {
            for (int col = 1; col <= numColumns; col++) {
                Rectangle cell = new Rectangle(cellSize, cellSize);
                cell.setStroke(Color.BLACK);
                cell.setFill(Color.TRANSPARENT);
                cell.setUserData(true);
                final int finalRow = row; // Adjust for zero-based index
                final int finalCol = col; // Adjust for zero-based index
                cell.setOnMouseClicked(event -> {
                    placeShip(finalRow, finalCol, cell);

                });
                boatPane.add(cell, col, row); // The grid content starts from (1,1) due to labels
            }
        }

        sceneMap.put("game", createBoatPlaceScene());
        primaryStage.setScene(sceneMap.get("game"));
    }

    private void placeShip(int row, int col, Rectangle cell) {
        if (selectedShip == null) return; // No ship selected
        if (currentOrientation == null) {
            errorT1.setText("Must Choose Orientation");
            return;
        }

        int shipSize = (int) selectedShip.getUserData();

        if (currentOrientation.equals("Horizontal")) {
            if (col + shipSize > numColumns+1) {
                errorT1.setText("Out of Bounds");
                return;
            } // Ship doesn't fit
            for (int i = 0; i < shipSize; i++) {
                Rectangle targetCell = (Rectangle) getNodeFromGridPane(boatPane, col + i, row);
                if(targetCell.getUserData().equals(false)) {
                    errorT1.setText("Merge with another ship");
                    return;
                }
            }
            for (int i = 0; i < shipSize; i++) {
                Rectangle targetCell = (Rectangle) getNodeFromGridPane(boatPane, col + i, row);
                targetCell.setFill(Color.DARKGRAY);
                targetCell.setUserData(false);
            }
            errorT1.setText("");
        } else if(currentOrientation.equals("Vertical")){ // Vertical placement
            if (row + shipSize > numRows+1) {
                errorT1.setText("Out of Bounds");
                return;
            } // Ship doesn't fit
            for (int i = 0; i < shipSize; i++) {
                Rectangle targetCell = (Rectangle) getNodeFromGridPane(boatPane, col, row + i);
                if(targetCell.getUserData().equals(false)) {
                    errorT1.setText("Merge with another ship");
                    return;
                }
            }
            for (int i = 0; i < shipSize; i++) {
                Rectangle targetCell = (Rectangle) getNodeFromGridPane(boatPane, col, row + i);
                targetCell.setFill(Color.DARKGRAY);
                targetCell.setUserData(false);
            }
            errorT1.setText("");
        }

        selectedShip.setDisable(true);
        selectedShip = null;
        currentOrientation = null;
        t3.setText("Orientation Selected: ");

        h1.getChildren().remove(1);
        h1.getChildren().add(1, ships);
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
        int buttonX = 120;
        int buttonY = 40;

        button.setStyle("-fx-font-size: 14; -fx-background-insets: 0; -fx-padding: 0; -fx-text-fill: black; -fx-font-family: Arial;");
        button.setPrefWidth(buttonX);
        button.setPrefHeight(buttonY);

        Rectangle rectangle = new Rectangle(buttonX, buttonY);
        button.setShape(rectangle);
        button.setPadding(new Insets(10));

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

    public void addImageToGridPane(GridPane gridPane, String imagePath, int column, int row) {
        // Create an image object
        Image image = new Image(imagePath);

        // Create an ImageView and set the image to it
        ImageView imageView = new ImageView(image);

        // You might want to adjust the size of the image view to fit the cell
        imageView.setFitWidth(gridPane.getColumnConstraints().get(column).getPrefWidth());
        imageView.setFitHeight(gridPane.getRowConstraints().get(row).getPrefHeight());
        imageView.setPreserveRatio(true);

        // Add the ImageView to the gridpane at the specified column and row
        gridPane.add(imageView, column, row);
    }
}
