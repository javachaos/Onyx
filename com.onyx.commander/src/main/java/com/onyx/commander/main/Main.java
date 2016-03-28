package com.onyx.commander.main;

import java.io.IOException;
import java.net.URL;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    public static void main(String[] args) {
	launch("");
    }

    @Override
    public void start(Stage stage) throws Exception {
	Parent p = null;
	ClassLoader classLoader = Main.class.getClassLoader();
	URL uri = classLoader.getResource( "commander.fxml" );
	try {
	    p = FXMLLoader.load(uri);
	} catch (IOException e) {
	    e.printStackTrace();
	}
	
	Scene s = new Scene(p);
	stage.setScene(s);
	stage.show();
    }

}
