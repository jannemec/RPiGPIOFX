/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jannemec.fxmlrpi;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 *
 * @author u935
 * java -cp ~/Dokumenty/rpi/RPiGPIOFX/RPiGPIOFX.jar com.jannemec.fxmlrpi.RPiGPIOFX
 * java -cp RPiGPIOFX.jar com.jannemec.fxmlrpi.RPiGPIOFX
 * java -Djava.ext.dirs=~/Dokumenty/rpi/armv6hf-sdk/rt/lib/ext -cp RPiGPIOFX.jar com.jannemec.fxmlrpi.RPiGPIOFX
 * java -Djava.ext.dirs=~/Dokumenty/rpi/armv6hf-sdk/rt/lib/ext -jar RPiGPIOFX.jar
 * gksu "java -jar RPiGPIOFX.jar"
 */
public class RPiGPIOFX extends Application {
    
    @Override
    public void start(Stage stage) throws Exception {
        
        Parent root = FXMLLoader.load(getClass().getResource("FXMLDocument.fxml"));
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) throws Exception {
        launch(args);
    }
    
}
