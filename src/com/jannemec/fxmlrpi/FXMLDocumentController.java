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
import com.jannemec.sensors.TCS3200;

import com.jannemec.tools.MemCache;
import com.jannemec.tools.ActionListener;
import com.jannemec.tools.TimerTaskParent;
import com.jannemec.tools.Tools;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
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
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import com.pi4j.io.gpio.RaspiPin;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import javafx.event.EventHandler;

/**
 *
 * @author u935
 */
public class FXMLDocumentController implements Initializable, ActionListener {
    
    private Timeline timelineShow = null;
    private Timeline timelineLog = null;
    protected static SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
    protected static SimpleDateFormat formatWithDate = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
    protected static SimpleDateFormat formatCompact = new SimpleDateFormat("yyyyMMddHHmmss");
    protected com.jannemec.tools.MemCache mCache = null;
    protected com.jannemec.sensors.AM2321 am2321 = null;
    protected com.jannemec.sensors.BMP180 bmp180 = null;
    protected com.jannemec.sensors.TSL2561 tsl2561 = null;
    protected com.jannemec.sensors.RainSBX rainSBX = null;
    protected com.jannemec.sensors.MotionPIR motionPIR = null;
    protected com.jannemec.sensors.HCSR04 hcsr04 = null;
    protected com.jannemec.sensors.Button button1 = null;
    protected com.jannemec.sensors.TCS3200 tcs3200 = null;
    protected com.jannemec.sensors.Button button2 = null;
    protected RPiCamera rPiCamera = null;
    protected Session session = null;
    protected Properties setup = null;
    protected Tools tools = null;
    protected EventHandler<ActionEvent> eventShow = null;
    protected EventHandler<ActionEvent> eventLog = null;
    protected KeyFrame keyframeShow = null;
    protected KeyFrame keyframeLog = null;
    
    @FXML
    private TextField timeTextField;
    //@FXML
    //private ClockControl clockControl;
    //@FXML
    //private Label tempLabel;
    @FXML
    private TextField tempTextField;
    @FXML
    private TextField temp2TextField;
    //@FXML
    //private Label humidLabel;
    @FXML
    private TextField humidTextField;
    //@FXML
    //private Label pressLabel;
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
    private TextField distanceTextField;
    
    @FXML
    private javafx.scene.control.Button closeButton;
    
    
    
    //@FXML
    //private TextField colorTextField;
    
    //@FXML
    //private TextField colorDtTextField;
    
    @FXML
    private void handleCloseButtonAction(ActionEvent event) {
        System.out.println("You clicked me!");
        // get a handle to the stage
        Stage stage = (Stage) closeButton.getScene().getWindow();
        // do closing actions
        stage.close();
    }
    
    private static final Logger LOGGER = Logger.getLogger(FXMLDocumentController.class.getName());
    public Handler consoleHandler = null;
    public Handler fileHandler  = null;
    
    private Timer timerLog = null;
    private Timer timerShow = null;

    
    @FXML
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Logger setup
        Handler consoleHandler = null;
        Handler fileHandler  = null;
        try{
            //Creating consoleHandler and fileHandler
            consoleHandler = new ConsoleHandler();
            fileHandler  = new FileHandler("./javafxml.log");
            //Assigning handlers to LOGGER object
            LOGGER.addHandler(consoleHandler);
            LOGGER.addHandler(fileHandler);
            //Setting levels to handlers and LOGGER
            consoleHandler.setLevel(Level.ALL);
            fileHandler.setLevel(Level.ALL);
            LOGGER.setLevel(Level.ALL);
            LOGGER.config("Logger Configuration done.");
            //Console handler removed
            //LOGGER.removeHandler(consoleHandler);
            //LOGGER.log(Level.FINE, "Finer logged");
        }catch(IOException exception){
            LOGGER.log(Level.SEVERE, "Error occur in FileHandler.", exception);
        }
        //LOGGER.finer("Finest example on LOGGER handler completed.");

                
        // read Properties
        this.setup = new Properties();
	try {
            // load a properties file
            InputStream in = Tools.getPropertyFile(FXMLDocumentController.class, "properties/setup.properties");
            this.setup.load(in);          
	} catch (IOException ex) {
		//ex.printStackTrace();
	} finally {
		
	}
        this.mCache = new MemCache();
        this.mCache.setDelay(Integer.parseInt(setup.getProperty("general.refresh")) - 1);
        
        
        // Prepare date forated
        this.tempTextField.setText("waiting for init");
        
        //Establishing a session with required user details
        this.session = Session.getInstance(this.setup, new javax.mail.Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(setup.getProperty("mail.smtp.username"), setup.getProperty("mail.smtp.password"));
            }
        });
        this.tools = new Tools();

        try {
            this.tools.dbfConnect(this.setup.getProperty("dbf.connectionString").replace("currentDir", this.tools.getJarPath()));
            this.tools.dbfCreateTable();
            this.tools.dbfKlimaCreateTable();
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        } catch (UnsupportedEncodingException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
        
        
        
        
        // Temperature
        this.am2321 = new AM2321(this.mCache);

        this.bmp180 = new BMP180(mCache);
        // Setup altitude based on position
        this.bmp180.setSensorAltitude(350);  // For Jesenice, Central Bohemia, Czech Republic
        
        this.hcsr04 = new HCSR04(mCache);
        
        this.rainSBX = new RainSBX(mCache);
        this.rainSBX.setInteruptMode(true);
        this.rainSBX.setActionListener(this);
        
        //this.motionPIR = new MotionPIR(mCache);
        //this.motionPIR.setInteruptMode(true);
        //this.motionPIR.setActionListener(this);
        
        this.tsl2561 = new TSL2561(mCache);
        
        //this.button1 = new Button(mCache);
        //this.button1.setInteruptMode(true);
        //this.button1.setActionListener(this);
        
        this.tcs3200 = new TCS3200(mCache);
        //this.button2 = new Button(mCache, RaspiPin.GPIO_24);
        //this.button2.setInteruptMode(true);
        //this.button2.setActionListener(this);
        
        try {
            this.rPiCamera = new RPiCamera(setup.getProperty("camera.dir", "/home/vlk/Dokumenty/rpi/RPiGPIOFX/photos"));
            this.rPiCamera
                .setWidth(500).setHeight(500) // Set Camera to produce 500x500 images.
                .setBrightness(90)                // Adjust Camera's brightness setting.
                .setExposure(Exposure.AUTO)       // Set Camera's exposure.
                .setTimeout(2)                    // Set Camera's timeout.
                .setContrast(100) 			// Set maximum contrast
                .setSharpness(100)		    // Set maximum sharpness
                .setQuality(75)
                .setAddRawBayer(true)
                .turnOffPreview()            // Turn on image preview
		.setEncoding(Encoding.PNG);
        } catch (FailedToRunRaspistillException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        //} catch (UnsupportedEncodingException ex) {
        //    LOGGER.log(Level.SEVERE, null, ex);
        }

        try {
            this.showValues();
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
        System.out.println("App started!");
        //System.out.println("Log file: " + FXMLDocumentController.class.getClassLoader().getResource("logging.properties"));
        
        
        // Timer version
        this.timerLog = new Timer();
        this.timerShow = new Timer();
        
        this.taskShow.setParent(this);
        
        this.timerLog.schedule(this.taskLog, Long.parseLong(setup.getProperty("general.logrefresh")) * 100, Long.parseLong(setup.getProperty("general.logrefresh")) * 1000);
        this.timerShow.schedule(this.taskShow, Long.parseLong(setup.getProperty("general.refresh")) * 100, Long.parseLong(setup.getProperty("general.refresh")) * 1000);
        /**
         // TimeLine version
         
        this.eventShow = new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                try {
                    showValues();
                    //timelineShow.stop();
                    //timelineShow.playFromStart();
                } catch (Exception ex) {
                    LOGGER.log(Level.SEVERE, null, ex);
                }
            }
        };
        this.keyframeShow = new KeyFrame(Duration.seconds(Integer.parseInt(setup.getProperty("general.refresh"))), this.eventShow);
        this.timelineShow = new Timeline(Timeline.INDEFINITE, this.keyframeShow);
        this.timelineShow.setCycleCount(Timeline.INDEFINITE);
        
        this.eventLog = new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                try {
                    System.out.println("Log run");
                    logValues();
                } catch (Exception ex) {
                    LOGGER.log(Level.SEVERE, null, ex);
                }
            }
        };
        this.keyframeLog = new KeyFrame(Duration.seconds(Integer.parseInt(setup.getProperty("general.logrefresh"))), this.eventLog);
        this.timelineLog = new Timeline(Timeline.INDEFINITE, this.keyframeLog);
        this.timelineLog.setCycleCount(Timeline.INDEFINITE);
        
        this.timelineShow.playFromStart();
        this.timelineLog.playFromStart();
        */
        
        
        
        
        
        /*
        // Version with closures
        this.timelineShow = new Timeline();
        this.timelineShow.setAutoReverse(true);
        this.timelineShow.setCycleCount(Timeline.INDEFINITE);
        
        this.timelineLog = new Timeline();
        this.timelineLog.setAutoReverse(true);
        this.timelineLog.setCycleCount(Timeline.INDEFINITE);
        
        this.timelineShow.getKeyFrames().add(
            new KeyFrame(Duration.seconds(Integer.parseInt(setup.getProperty("general.refresh"))), (ActionEvent event) -> { 
                try {     
                    this.showValues();
                } catch (Exception ex) {
                    Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
                }
            } // KeyFrame event handler
        ));
        
        this.timelineShow.playFromStart();
        
        this.timelineLog.getKeyFrames().add(
            new KeyFrame(Duration.seconds(Integer.parseInt(setup.getProperty("general.logrefresh"))), (ActionEvent event) -> { 
                try {     
                    this.logValues();
                } catch (Exception ex) {
                    Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
                }
            } // KeyFrame event handler
        ));
        this.timelineLog.playFromStart();
        */
    }

    public TimerTaskParent taskShow = new TimerTaskParent() {
        @Override
        public void run() {
            try {
                this.getParent().showValues();
            } catch (Exception ex) {
                LOGGER.log(Level.SEVERE, null, ex);
            }
        }
    };
    
    public TimerTask taskLog = new TimerTask() {
        @Override
        public void run() {
            try {
                logValues();
            } catch (Exception ex) {
                LOGGER.log(Level.SEVERE, null, ex);
            }
        }
    };
    
    public void logValues() throws  Exception {
        // And we have to store values to dbf
        Date dt = new Date();
        this.tools.storeKlimaValues(dt, this.bmp180.getTemperature(), this.bmp180.getPressureAtSeaLevel(), this.am2321.getHumidity()
                , tsl2561.getFull(), tsl2561.getInfrared(), tsl2561.getVisible(), this.rainSBX.isRain()); 
    }
    
    public void showValues() throws  Exception {
        Date dt = new Date();
        //FXMLDocumentController.format = new SimpleDateFormat("HH:mm:ss");
        this.timeTextField.setText(FXMLDocumentController.format.format(dt));
        
        //System.out.println("aaa " + FXMLDocumentController.format.format(dt));
        
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
        /**/
        /*
        this.rainCheckBox.setSelected(this.rainSBX.isRain());
        if (this.rainSBX.getLastChangeDate() == null) {
            this.rainTextField.setText("---");
        } else {
            this.rainTextField.setText(FXMLDocumentController.format.format(this.rainSBX.getLastChangeDate()));
        };*/
    }

    @Override
    public void handleAction(Sensor sensor) {
    }
    
    public void no(Sensor sensor) {
        Date dt = new Date();
        System.out.println(FXMLDocumentController.formatCompact.format(dt) + " Signal from " + sensor.getClass().getName());
        if (sensor.getClass().getName().equals("com.jannemec.sensors.RainSBX")) {
            try {
                this.rainCheckBox.setSelected(this.rainSBX.isRain());
            } catch  (Exception ex) {
                LOGGER.log(Level.SEVERE, null, ex);
                //System.out.println(ex.getMessage());
            }
            //System.out.println("RAIN sensor signal");// + (this.rainSBX.isRain() ? "ANO" : "NE"));
            if (this.rainSBX.getLastChangeDate() == null) {
                this.rainTextField.setText("---");
            } else {
                this.rainTextField.setText(FXMLDocumentController.formatWithDate.format(this.rainSBX.getLastChangeDate()));
            }
        }
        
        if (sensor.getClass().getName().equals("com.jannemec.sensors.MotionPIR")) {
            try {
                this.motionCheckBox.setSelected(this.motionPIR.isMovement());
            } catch  (Exception ex) {
                LOGGER.log(Level.SEVERE, null, ex);
            }
            //System.out.println("RAIN sensor signal");// + (this.rainSBX.isRain() ? "ANO" : "NE"));
            if (this.motionPIR.getLastChangeDate() == null) {
                this.motionTextField.setText("---");
            } else {
                this.motionTextField.setText(FXMLDocumentController.formatWithDate.format(this.motionPIR.getLastChangeDate()));
            }
        }
        
        if (sensor.getClass().getName().equals("com.jannemec.sensors.Button")) {
            try {
                if (this.button2.isOn()) {
                    // Načtení barvy
                    String color = this.tcs3200.getColor();
                    //this.colorTextField.setText(color);
                    //this.colorDtTextField.setText(FXMLDocumentController.formatWithDate.format(this.motionPIR.getLastChangeDate()));
                }
                if (this.button1.isOn()) {
                    // Take a picture
                    if (!(this.rPiCamera == null)) {
                        
                        String filename = FXMLDocumentController.formatCompact.format(dt) + ".png";
                        System.out.println("Picture " + filename);
                        File image = this.rPiCamera.takeStill(filename, 650, 650);
			System.out.println("New PNG image saved to:\n\t" + image.getAbsolutePath());
                        //this.rPiCamera.takeStill(filename);
                        
                        // Send Mail with photo
                        try {
                            //Creating a Message object to set the email content
                            MimeMessage msg = new MimeMessage(session);
                            //Storing the comma seperated values to email addresses
                            String to = "jannemec@centrum.cz";
                            /*Parsing the String with defualt delimiter as a comma by marking the boolean as true and storing the email addresses in an array of InternetAddress objects*/
                            InternetAddress[] address = InternetAddress.parse(to, true);
                            //Setting the recepients from the address variable
                            msg.setRecipients(Message.RecipientType.TO, address);
                            String from = "jan.nemec@cscz.biz";
                            msg.setFrom(from);
                            String timeStamp = FXMLDocumentController.formatWithDate.format(new Date());
                            msg.setSubject("Photo from RPi : " + timeStamp);
                            msg.setSentDate(new Date());
                            msg.setText("The photo taken from RPi - according to action");
                            msg.setHeader("XPriority", "1");
                            MimeBodyPart messageBodyPart = new MimeBodyPart();
                            DataSource source = new FileDataSource(image.getAbsolutePath());
                            messageBodyPart.setDataHandler(new DataHandler(source));
                            messageBodyPart.setFileName(filename);
                            Multipart multipart = new MimeMultipart();
                            multipart.addBodyPart(messageBodyPart);
                            msg.setContent(multipart);
                            Transport.send(msg);
                            System.out.println("Mail has been sent successfully");
                        } catch (MessagingException mex) {
                            System.out.println("Unable to send an email" + mex);
                        }
                    }
                }
            } catch  (Exception ex) {
                LOGGER.log(Level.SEVERE, null, ex);
            }
        }
    }
}
