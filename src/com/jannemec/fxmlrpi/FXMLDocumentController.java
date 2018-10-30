/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jannemec.fxmlrpi;

import com.hopding.jrpicam.RPiCamera;
import com.hopding.jrpicam.enums.Encoding;
import com.hopding.jrpicam.enums.Exposure;
import com.hopding.jrpicam.exceptions.FailedToRunRaspistillException;

import com.jannemec.control.ClockControl;
import com.jannemec.sensors.AM2321;
import com.jannemec.sensors.BMP180;
import com.jannemec.sensors.Button;
import com.jannemec.sensors.MotionPIR;
import com.jannemec.sensors.TSL2561;
import com.jannemec.sensors.HCSR04;
import com.jannemec.sensors.RainSBX;
import com.jannemec.sensors.Sensor;

import com.jannemec.tools.MemCache;
import com.jannemec.tools.ActionListener;
import java.io.File;

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
import javafx.scene.control.CheckBox;

/**
 *
 * @author u935
 */
public class FXMLDocumentController implements Initializable, ActionListener {
    
    private Timeline timeline = null;
    protected SimpleDateFormat format = null;
    protected SimpleDateFormat formatWithDate = null;
    protected SimpleDateFormat formatCompact = null;
    protected com.jannemec.tools.MemCache mCache = null;
    protected com.jannemec.sensors.AM2321 am2321 = null;
    protected com.jannemec.sensors.BMP180 bmp180 = null;
    protected com.jannemec.sensors.TSL2561 tsl2561 = null;
    protected com.jannemec.sensors.RainSBX rainSBX = null;
    protected com.jannemec.sensors.MotionPIR motionPIR = null;
    protected com.jannemec.sensors.HCSR04 hcsr04 = null;
    protected com.jannemec.sensors.Button button1 = null;
    protected RPiCamera rPiCamera = null;
    
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
    private CheckBox rainCheckBox;
    @FXML
    private TextField rainTextField;
    
    @FXML
    private CheckBox motionCheckBox;
    @FXML
    private TextField motionTextField;
    
    
    @FXML
    private javafx.scene.control.Button closeButton;
    
    @FXML
    private TextField distanceTextField;
    
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
        this.formatWithDate = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
        this.formatCompact = new SimpleDateFormat("yyyyMMddHHmmss");
        
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
        
        this.hcsr04 = new HCSR04(mCache);
        
        this.rainSBX = new RainSBX(mCache);
        this.rainSBX.setInteruptMode(true);
        this.rainSBX.setActionListener(this);
        
        this.motionPIR = new MotionPIR(mCache);
        this.motionPIR.setInteruptMode(true);
        this.motionPIR.setActionListener(this);
        
        this.tsl2561 = new TSL2561(mCache);
        
        this.button1 = new Button(mCache);
        this.button1.setInteruptMode(true);
        this.button1.setActionListener(this);
        
        try {
            this.rPiCamera = new RPiCamera("/home/vlk/Dokumenty/rpi/RPiGPIOFX/photos");
            this.rPiCamera
                .setWidth(500).setHeight(500) // Set Camera to produce 500x500 images.
                .setBrightness(75)                // Adjust Camera's brightness setting.
                .setExposure(Exposure.AUTO)       // Set Camera's exposure.
                .setTimeout(2)                    // Set Camera's timeout.
                .setAddRawBayer(true)
                .turnOffPreview()            // Turn on image preview
		.setEncoding(Encoding.JPG);
        } catch (FailedToRunRaspistillException e) {
        }
        // create listener on button
        
        
        
        
        
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
        
        this.distanceTextField.setText(String.format("%.1f", (double) (hcsr04.getDistance() * 1000)));
        /*
        this.rainCheckBox.setSelected(this.rainSBX.isRain());
        if (this.rainSBX.getLastChangeDate() == null) {
            this.rainTextField.setText("---");
        } else {
            this.rainTextField.setText(this.format.format(this.rainSBX.getLastChangeDate()));
        };*/
    }

    @Override
    public void handleAction(Sensor sensor) {
        Date dt = new Date();
        System.out.println(this.formatCompact.format(dt) + " Signal from " + sensor.getClass().getName());
        if (sensor.getClass().getName().equals("com.jannemec.sensors.RainSBX")) {
            try {
                this.rainCheckBox.setSelected(this.rainSBX.isRain());
            } catch  (Exception e) {
                
            }
            //System.out.println("RAIN sensor signal");// + (this.rainSBX.isRain() ? "ANO" : "NE"));
            if (this.rainSBX.getLastChangeDate() == null) {
                this.rainTextField.setText("---");
            } else {
                this.rainTextField.setText(this.formatWithDate.format(this.rainSBX.getLastChangeDate()));
            }
        }
        
        if (sensor.getClass().getName().equals("com.jannemec.sensors.MotionPIR")) {
            try {
                this.motionCheckBox.setSelected(this.motionPIR.isMovement());
            } catch  (Exception e) {
                
            }
            //System.out.println("RAIN sensor signal");// + (this.rainSBX.isRain() ? "ANO" : "NE"));
            if (this.motionPIR.getLastChangeDate() == null) {
                this.motionTextField.setText("---");
            } else {
                this.motionTextField.setText(this.formatWithDate.format(this.motionPIR.getLastChangeDate()));
            }
        }
        
        if (sensor.getClass().getName().equals("com.jannemec.sensors.Button")) {
            try {
                if (this.button1.isOn()) {
                    // Take a picture
                    if (!(this.rPiCamera == null)) {
                        
                        String filename = this.formatCompact.format(dt) + ".jpg";
                        System.out.println("Picture " + filename);
                        File image = this.rPiCamera.takeStill(filename, 650, 650);
			System.out.println("New PNG image saved to:\n\t" + image.getAbsolutePath());
                        //this.rPiCamera.takeStill(filename);
                    }
                }
            } catch  (Exception e) {
                System.out.println("Error " + e.getMessage());
            }
        }
    }
}
