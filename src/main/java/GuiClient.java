
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
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.w3c.dom.Text;

public class GuiClient extends Application{
    private TextField nameTextField;
    private HashMap<String, Scene> sceneMap;
    private Client clientConnection;
    private Label errorLabel = new Label();
    private VBox mainVBox;
    private Message client = new Message();
    private ArrayList<String> allUsers = new ArrayList<>();
    private ArrayList<Message> userNames = new ArrayList<>();
    private String username;
    private VBox otherUsers = new VBox(10);

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
                        if (!s.userName.equals(client.userName)){
                            Button userButton = new Button(s.userName);
                            styleRectangleButton(userButton);
                            userButton.setStyle("-fx-background-color: white;");
                            otherUsers.getChildren().addAll(userButton);
                        }
                    }
                }
                else {
                    Message message = (Message) data;
                    if (message.messageType.equals("Duplicate Username")){
                        mainVBox.getChildren().remove(errorLabel);
                        errorLabel.setText("Username already exists! Choose a different username");
                        errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 16; -fx-font-family: Arial");
                        mainVBox.getChildren().add(errorLabel);
                    }
                    // New user has joined the server
                    else if (message.messageType.equals("New User")){
                        if(!allUsers.contains(message.userName)) {
                            Button newUserButton = new Button(message.userName);
                            styleRectangleButton(newUserButton);
                            newUserButton.setStyle("-fx-background-color: white;");
                            otherUsers.getChildren().add(newUserButton);
                            userNames.add(message);
                        }
                    }
                    else {
                        boatPlace(primaryStage);
                    }
                }
            });
        });

        clientConnection.start();

        nameTextField = new TextField();
        nameTextField.setOnAction(e->{
            client.userName = nameTextField.getText();
            if (client.userName.isEmpty()) {
                mainVBox.getChildren().remove(errorLabel);
                errorLabel.setText("Invalid Empty Username");
                errorLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: red; -fx-font-size: 16;");
                mainVBox.getChildren().add(errorLabel);
            }
            else {
                System.out.println("Sending this username: " + client.userName);
                username = client.userName;
                clientConnection.send(client);
                nameTextField.clear();
            }
        });


        sceneMap = new HashMap<String, Scene>();
        sceneMap.put("welcome", createWelcomePage());

        primaryStage.setOnCloseRequest(t -> {
            Platform.exit();
            System.exit(0);
        });

        primaryStage.setScene(sceneMap.get("welcome"));
        primaryStage.setTitle("Battleships");
        primaryStage.show();
    }

    public Scene createWelcomePage() {
        Label welcomeLabel = new Label("Welcome to Battleships!");
        welcomeLabel.setStyle("-fx-font-size: 45; -fx-font-weight: bold; -fx-text-fill: white; -fx-font-family: Arial;");

        Label promptLabel = new Label("Play with another player or with the AI");
        promptLabel.setStyle("-fx-font-size: 20; -fx-font-weight: bold; -fx-text-fill: white; -fx-font-family: Arial;");

        Button onlineButton = new Button("Online");
        onlineButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-background-radius: 20 20 20 20; -fx-font-size: 20; -fx-font-family: Arial; -fx-pref-width: 125px;");

        Button botButton = new Button("AI");
        botButton.setStyle("-fx-background-color: #ffae00; -fx-text-fill: white; -fx-background-radius: 20 20 20 20; -fx-font-size: 20; -fx-font-family: Arial; -fx-pref-width: 125px;");

        HBox buttonBox = new HBox(40, onlineButton, botButton);
        buttonBox.setAlignment(Pos.CENTER);

        nameTextField.setStyle("-fx-text-fill: black; -fx-font-size: 16; -fx-background-radius: 10; -fx-font-family: Arial; -fx-pref-width: 30px;");
        nameTextField.setAlignment(Pos.CENTER);

        Button backButton = new Button("Back");
        backButton.setStyle("-fx-background-color: #810000; -fx-text-fill: white; -fx-background-radius: 20 20 20 20; -fx-font-size: 20; -fx-font-family: Arial; -fx-pref-width: 125px;");

        onlineButton.setOnAction(e -> {
            promptLabel.setText("You're playing online! What's your username?");
            buttonBox.getChildren().clear();
            buttonBox.getChildren().add(backButton);
            mainVBox.getChildren().add(1, nameTextField);
        });

        botButton.setOnAction(e -> {
            promptLabel.setText("You're playing with the AI! What's your username?");
            buttonBox.getChildren().clear();
            buttonBox.getChildren().add(backButton);
            mainVBox.getChildren().add(1, nameTextField);
        });

        backButton.setOnAction(e -> {
            promptLabel.setText("Play with another player or with the AI");
            mainVBox.getChildren().remove(nameTextField);
            buttonBox.getChildren().clear();
            buttonBox.getChildren().add(onlineButton);
            buttonBox.getChildren().add(botButton);
        });

        mainVBox = new VBox(30, promptLabel, buttonBox);
        mainVBox.setAlignment(Pos.CENTER);

        BorderPane pane = new BorderPane();
        pane.setPadding(new Insets(70));
        pane.setTop(welcomeLabel);
        BorderPane.setAlignment(welcomeLabel, Pos.CENTER);
        pane.setCenter(mainVBox);
        pane.setStyle("-fx-background-color: #383838;");
        return new Scene(pane, 700, 700);
    }

    public Scene createBoatPlaceScene(Label promptLabel, GridPane boatPane){ //bug if no users to display add error message
        BorderPane pane = new BorderPane();
        pane.setPadding(new Insets( 20));
        pane.setStyle("-fx-background-color: Grey");

        pane.setTop(promptLabel);
        pane.setCenter(boatPane);
        BorderPane.setAlignment(promptLabel, Pos.CENTER);
        BorderPane.setAlignment(boatPane, Pos.CENTER);

        return new Scene(pane, 900, 900);
    }

    public void boatPlace(Stage primaryStage){
        Label promptLabel = new Label("Place Your Boats");
        promptLabel.setStyle("-fx-font-size: 45; -fx-font-weight: bold; -fx-text-fill: black; -fx-font-family: Arial;");

        GridPane boatPane = new GridPane();
        int numColumns = 10;
        int numRows = 10;
        int cellSize = 30;

        for (int row = 0; row < numRows; row++) {
            for (int col = 0; col < numColumns; col++) {
                Rectangle cell = new Rectangle(cellSize, cellSize);
                cell.setStroke(Color.BLACK);
                cell.setFill(Color.TRANSPARENT);
                cell.setOnMouseClicked(event -> {
                    Rectangle source = (Rectangle) event.getSource();
                    if (source.getFill() == Color.TRANSPARENT) {
                        source.setFill(Color.DARKGRAY);
                    }
                    else {
                        source.setFill(Color.TRANSPARENT);
                    }

                });

                boatPane.add(cell, col, row);
            }
        }
        boatPane.setGridLinesVisible(true);

        sceneMap.put("prep", createBoatPlaceScene(promptLabel, boatPane));
        primaryStage.setScene(sceneMap.get("prep"));
    }

    private Button styleRectangleButton(Button button){
        int buttonX = 120;
        int buttonY = 60;

        button.setStyle("-fx-font-size: 14; -fx-background-insets: 0; -fx-padding: 0; -fx-text-fill: black; -fx-font-family: Arial;");
        button.setPrefWidth(buttonX);
        button.setPrefHeight(buttonY);

        Rectangle rectangle = new Rectangle(buttonX, buttonY);
        button.setShape(rectangle);
        button.setPadding(new Insets(10));

        return button;
    }

}
