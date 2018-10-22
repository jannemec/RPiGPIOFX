/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jannemec.fxmlrpi;

import com.jannemec.control.ClockControl;
import com.jannemec.sensors.AM2321;
import com.jannemec.sensors.BMP180;
import com.jannemec.sensors.TSL2561;
import com.jannemec.tools.MemCache;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.util.Duration;
import javafx.stage.Stage;

/**
 *
 * @author u935
 */
public class FXMLDocumentController implements Initializable {
    
    private Timeline timeline = null;
    protected SimpleDateFormat format = null;
    protected com.jannemec.tools.MemCache mCache = null;
    protected com.jannemec.sensors.AM2321 am2321 = null;
    protected com.jannemec.sensors.BMP180 bmp180 = null;
    protected com.jannemec.sensors.TSL2561 tsl2561;
    
    @FXML
    private ClockControl clockControl;
    @FXML
    private Label tempLabel;
    @FXML
    private TextField tempTextField;
    @FXML
    private TextField temp2TextField;
    @FXML
    private Label humidLabel;
    @FXML
    private TextField humidTextField;
    @FXML
    private Label pressLabel;
    @FXML
    private TextField pressTextField;
    @FXML
    private TextField press2TextField;
    @FXML
    private TextField altitudeTextField;
    
    @FXML
    private TextField lightTextField;
    @FXML
    private TextField lightVisibleTextField;
    @FXML
    private TextField lightInfraTextField;
    
    
    @FXML
    private javafx.scene.control.Button closeButton;
    
    @FXML
    private void handleCloseButtonAction(ActionEvent event) {
        System.out.println("You clicked me!");
        // get a handle to the stage
        Stage stage = (Stage) closeButton.getScene().getWindow();
        // do closing actions
        stage.close();
    }
    
    @FXML
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        this.mCache = new MemCache();
        
        this.tempTextField.setText("waiting for init");
        this.format = new SimpleDateFormat("HH:mm:ss");
        
        timeline = new Timeline();
        timeline.setCycleCount(Timeline.INDEFINITE);
        // Test hodiny
        /*
        timeline.getKeyFrames().add(
            new KeyFrame(Duration.seconds(1), (ActionEvent event) -> { 
                Date dt = new Date();
                this.tempTextField.setText(this.format.format(dt));     
            } // KeyFrame event handler
        ));
        timeline.playFromStart();
        */
        
        // Temperature
        this.am2321 = new AM2321(this.mCache);

        this.bmp180 = new BMP180(mCache);
        // Setup altitude based on position
        this.bmp180.setSensorAltitude(350);  // For Jesenice, Central Bohemia, Czech Republic
        
        
        this.tsl2561 = new TSL2561(mCache);
        try {
            this.showValues();
        } catch (Exception ex) {
            Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("App started!");
        timeline.getKeyFrames().add(
            new KeyFrame(Duration.seconds(30), (ActionEvent event) -> { 
                try {     
                    this.showValues();
                } catch (Exception ex) {
                    Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
                }
            } // KeyFrame event handler
        ));
        timeline.playFromStart();
    }

    private void showValues() throws  Exception {
        this.humidTextField.setText(String.format("%.0f", (double) this.am2321.getHumidity()));
        this.tempTextField.setText(String.format("%.1f", (double) this.am2321.getTemperature()));
        
        this.temp2TextField.setText(String.format("%.1f", (double) this.bmp180.getTemperature()));
        this.pressTextField.setText(String.format("%.0f", (double) this.bmp180.getPressure()));
        this.press2TextField.setText(String.format("%.0f", (double) this.bmp180.getPressureAtSeaLevel()));
        this.altitudeTextField.setText(String.format("%.0f", (double) this.bmp180.getAltitude()));
        
        this.lightTextField.setText(String.format("%.1f", (double) tsl2561.getFull()));
        this.lightVisibleTextField.setText(String.format("%.1f", (double) tsl2561.getVisible()));
        this.lightInfraTextField.setText(String.format("%.1f", (double) tsl2561.getInfrared()));
    }
    
}
