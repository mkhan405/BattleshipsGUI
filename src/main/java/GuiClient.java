
import java.util.ArrayList;
import java.util.HashMap;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
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
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.scene.text.Text;

public class GuiClient extends Application{
    private Text welcomeText, promptText, errorText;
    private Button onlineButton, botButton, backButton;
    private TextField nameTextField;
    private VBox mainVBox;
    private Message client = new Message();
    private ArrayList<String> allUsers = new ArrayList<>();
    private ArrayList<Message> userNames = new ArrayList<>();
    private String username;
    private VBox otherUsers = new VBox(10);
    private HashMap<String, Scene> sceneMap;
    private Client clientConnection;

    public static void main(String[] args) {
        launch(args);
    }
    @Override
    public void start(Stage primaryStage) throws Exception {
        clientConnection = new Client(data->{
            Platform.runLater(()->{
                if (data instanceof ArrayList) {
                    userNames = (ArrayList<Message>) data;
                    for (Message s: userNames){
                        if (!s.username.equals(client.username)){
                            Button userButton = new Button(s.username);
                            styleRectangleButton(userButton);
                            userButton.setStyle("-fx-background-color: white;");
                            otherUsers.getChildren().addAll(userButton);
                        }
                    }
                }
                else {
                    Message message = (Message) data;
                    if (message.type.equals("registration_error")){
                        mainVBox.getChildren().remove(errorText);
                        errorText.setText("Username already exists! Choose a different username");
                        errorText.setStyle("-fx-text-fill: red; -fx-font-size: 16; -fx-font-family: Arial");
                        mainVBox.getChildren().add(errorText);
                    }
                    // New user has joined the server
                    else if (message.type.equals("user_registered")){
                        if(!allUsers.contains(message.username)) {
                            Button newUserButton = new Button(message.username);
                            styleRectangleButton(newUserButton);
                            newUserButton.setStyle("-fx-background-color: white;");
                            otherUsers.getChildren().add(newUserButton);
                            userNames.add(message);
                            boatPlace(primaryStage);
                        }
                    }
                    else {
                        boatPlace(primaryStage);
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
        welcomeText.setStyle("-fx-font-size: 45; -fx-font-weight: bold; -fx-text-fill: white; -fx-font-family: Arial;");

        promptText = new Text("Play with another player or with the AI");
        promptText.setStyle("-fx-font-size: 20; -fx-font-weight: bold; -fx-text-fill: white; -fx-font-family: Arial;");

        errorText = new Text();
        errorText.setStyle(" -fx-font-size: 16; -fx-font-weight: bold; -fx-text-fill: red; -fx-font-family: Arial;");

        onlineButton = new Button("Online");
        styleButton(onlineButton, "linear-gradient(#448aff, #005ecb)", "linear-gradient(#82b1ff, #447eff)");

        botButton = new Button("AI");
        styleButton(botButton, "linear-gradient(#f0bf2b, #d4a004)", "linear-gradient(#f7e35c, #edd428)");

        backButton = new Button("Back");
        styleButton(backButton, "linear-gradient(#ff5252, #c50e29)", "linear-gradient(#ff8a80, #ff5252)");

        HBox buttonBox = new HBox(40, onlineButton, botButton);
        buttonBox.setAlignment(Pos.CENTER);

        nameTextField = new TextField();
        nameTextField.setStyle("-fx-text-fill: black; -fx-font-size: 16; -fx-background-radius: 10; -fx-font-family: Arial; -fx-pref-width: 30px;");
        nameTextField.setAlignment(Pos.CENTER);

        onlineButton.setOnAction(e -> {
            promptText.setText("You're playing online! What's your username?");
            buttonBox.getChildren().clear();
            buttonBox.getChildren().add(backButton);
            mainVBox.getChildren().add(1, nameTextField);
        });

        botButton.setOnAction(e -> {
            promptText.setText("You're playing with the AI! What's your username?");
            buttonBox.getChildren().clear();
            buttonBox.getChildren().add(backButton);
            mainVBox.getChildren().add(1, nameTextField);
        });

        backButton.setOnAction(e -> {
            promptText.setText("Play with another player or with the AI");
            mainVBox.getChildren().remove(nameTextField);
            buttonBox.getChildren().clear();
            buttonBox.getChildren().add(onlineButton);
            buttonBox.getChildren().add(botButton);
        });

        nameTextField.setOnAction(e->{
            if (nameTextField.getText().isEmpty()) {
                errorText.setText("Invalid Empty Username");
            }
            else {
                System.out.println("Sending this username: " + client.username);
                clientConnection.send(new Message("new_user",nameTextField.getText()));
                errorText.setText("");
                nameTextField.clear();
            }
        });

        mainVBox = new VBox(20, promptText, errorText, buttonBox);
        mainVBox.setAlignment(Pos.CENTER);

        BorderPane pane = new BorderPane();
        pane.setPadding(new Insets(70));
        pane.setTop(welcomeText);
        BorderPane.setAlignment(welcomeText, Pos.CENTER);
        pane.setCenter(mainVBox);
        pane.setStyle("-fx-background-color: #383838;");

        return new Scene(pane, 600, 600);
    }

    public Scene createBoatPlaceScene(Label promptLabel, GridPane boatPane){ //bug if no users to display add error message

        Button carrier = new Button("CARRIER (5)");
        Button battleship = new Button("BATTLESHIP (4)");
        Button cruiser = new Button("CRUISER (3)");
        Button submarine = new Button("SUBMARINE (3)");
        Button destroyer = new Button("DESTROYER (2)");

        styleRectangleButton(carrier);
        styleRectangleButton(battleship);
        styleRectangleButton(cruiser);
        styleRectangleButton(submarine);
        styleRectangleButton(destroyer);

        Text remaining = new Text("Remaining Boats - 5");

        VBox ships = new VBox(10,remaining,carrier,battleship,cruiser,submarine,destroyer);
        ships.setAlignment(Pos.CENTER);
        HBox h1 = new HBox(200, boatPane,ships);
        h1.setAlignment(Pos.CENTER);
        VBox v1 = new VBox(25,promptLabel,h1);
        v1.setAlignment(Pos.CENTER);

        BorderPane pane = new BorderPane();
        pane.setPadding(new Insets( 20));
        pane.setStyle("-fx-background-color: Grey");

        pane.setCenter(v1);
        BorderPane.setAlignment(promptLabel, Pos.CENTER);
        return new Scene(pane, 700, 500);
    }

    public void boatPlace(Stage primaryStage) {
        Label promptLabel = new Label("Place Your Boats");
        promptLabel.setStyle("-fx-font-size: 45; -fx-font-weight: bold; -fx-text-fill: black; -fx-font-family: Arial;");

        GridPane boatPane = new GridPane();

        int numColumns = 10;
        int numRows = 10;
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
                cell.setOnMouseClicked(event -> {
                    Rectangle source = (Rectangle) event.getSource();
                    if (source.getFill() == Color.TRANSPARENT) {
                        source.setFill(Color.DARKGRAY);
                    } else {
                        source.setFill(Color.TRANSPARENT);
                    }
                });
                boatPane.add(cell, col, row); // The grid content starts from (1,1) due to labels
            }
        }

        sceneMap.put("prep", createBoatPlaceScene(promptLabel, boatPane));
        primaryStage.setScene(sceneMap.get("prep"));
    }

    private Button styleRectangleButton(Button button){
        int buttonX = 120;
        int buttonY = 40;

        button.setStyle("-fx-font-size: 14; -fx-background-insets: 0; -fx-padding: 0; -fx-text-fill: black; -fx-font-family: Arial;");
        button.setPrefWidth(buttonX);
        button.setPrefHeight(buttonY);

        Rectangle rectangle = new Rectangle(buttonX, buttonY);
        button.setShape(rectangle);
        button.setPadding(new Insets(10));

        return button;
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


