package com.nk.translator;

import java.io.FileInputStream;

import com.google.api.gax.core.CredentialsProvider;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.Credentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.translate.v3.LocationName;
import com.google.cloud.translate.v3.TranslateTextRequest;
import com.google.cloud.translate.v3.TranslateTextResponse;
import com.google.cloud.translate.v3.Translation;
import com.google.cloud.translate.v3.TranslationServiceClient;
import com.google.cloud.translate.v3.TranslationServiceSettings;

import javafx.application.Application;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class Main extends Application
{
	private Button buttonParse;
	private Button buttonReset;
	private TextArea textArea;
	private FlowPane flowPane;
	private ScrollPane scrollPane;
	private HBox hbox1;
	private Font font = Font.font("Arial", 28);
	private TextField textfield;
	private VBox root;
	private TranslationServiceClient client = null;

	public static void main()
	{
		launch();
	}

	@Override
	public void start(Stage stage) throws Exception
	{
		// get this json from your google cloud consolse
		String credentialsJson = "C://creds.json";
		Credentials credentials = ServiceAccountCredentials.fromStream(new FileInputStream(credentialsJson));
		CredentialsProvider credentialsProvider = FixedCredentialsProvider.create(credentials);
		TranslationServiceSettings settings = TranslationServiceSettings.newBuilder().setCredentialsProvider(credentialsProvider).build();
		client = TranslationServiceClient.create(settings);
		buttonParse = new Button("Translate");
		buttonParse.setOnMouseClicked(btnClicked());
		buttonReset = new Button("Reset");
		buttonReset.setDisable(true);
		buttonReset.setOnMouseClicked(new EventHandler<MouseEvent>()
		{
			@Override
			public void handle(MouseEvent event)
			{
				flowPane.getChildren().clear();
				root.getChildren().remove(scrollPane);
				root.getChildren().add(textArea);
				buttonReset.setDisable(true);
				buttonParse.setDisable(false);
				textArea.setText("");
				textArea.requestFocus();
			}
		});
		textfield = new TextField();
		textfield.setMinWidth(400);
		textfield.setFont(font);
		hbox1 = new HBox(10);
		hbox1.getChildren().addAll(buttonParse, buttonReset, textfield);
		root = new VBox(10);
		root.setPadding(new Insets(10, 10, 10, 10));
		Scene scene = new Scene(root, 1400, 720);
		textArea = new TextArea();
		textArea.setPrefWidth(1100);
		textArea.setPrefHeight(600);
		textArea.setFont(font);
		textArea.setWrapText(true);
		textArea.setText("Enter text here");
		flowPane = new FlowPane(Orientation.HORIZONTAL, 10, 5);
		scrollPane = new ScrollPane(flowPane);
		flowPane.setPadding(new Insets(10, 10, 10, 10));
		flowPane.prefWidthProperty().bind(scrollPane.widthProperty().subtract(40));
		flowPane.prefHeightProperty().bind(scrollPane.heightProperty().subtract(10));
		flowPane.setBackground(Background.fill(Paint.valueOf("white")));
		scrollPane.prefWidthProperty().bind(root.widthProperty().subtract(10));
		scrollPane.prefHeightProperty().bind(root.heightProperty().subtract(10));
		root.getChildren().add(hbox1);
		root.getChildren().add(textArea);
		stage.setScene(scene);
		stage.show();
	}

	private EventHandler<? super MouseEvent> btnClicked()
	{
		return new EventHandler<Event>()
		{
			@Override
			public void handle(Event event)
			{
				root.getChildren().remove(textArea);
				root.getChildren().add(scrollPane);
				buttonReset.setDisable(false);
				buttonParse.setDisable(true);
				String[] splitted = textArea.getText().replace("\n", " ").split(" ");
				for (String somestr : splitted)
				{
					Label someButton = new Label(somestr);
					someButton.setFont(font);
					someButton.setOnMouseClicked(new EventHandler<MouseEvent>()
					{
						@Override
						public void handle(MouseEvent event)
						{
							String tttt = ((Text) event.getTarget()).getText();
							textfield.setText(translateText(tttt));
						}
					});
					someButton.setOnMouseEntered(new EventHandler<MouseEvent>()
					{
						@Override
						public void handle(MouseEvent event)
						{
							((Label) event.getTarget()).setStyle("-fx-background-color: yellow");
						}
					});
					someButton.setOnMouseExited(new EventHandler<MouseEvent>()
					{
						@Override
						public void handle(MouseEvent event)
						{
							((Label) event.getTarget()).setStyle("-fx-background-color: none");
						}
					});
					flowPane.getChildren().add(someButton);
				}
			}
		};
	}

	public String translateText(String text)
	{
		// you might want to change these
		String projectId = "translationproject-439019";
		String sourceLanguage = "fr";
		String targetLanguage = "en";
		String result = "";
		TranslateTextRequest request = TranslateTextRequest.newBuilder().setParent(LocationName.of(projectId, "global").toString()).setMimeType("text/plain").setSourceLanguageCode(sourceLanguage).setTargetLanguageCode(targetLanguage).addContents(text).build();
		TranslateTextResponse response = client.translateText(request);
		for (Translation translation : response.getTranslationsList())
		{
			result += translation.getTranslatedText() + ", ";
		}
		return result;
	}
}
