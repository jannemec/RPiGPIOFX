/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jannemec.tools;

import com.jannemec.fxmlrpi.FXMLDocumentController;
import java.util.TimerTask;

/**
 *
 * @author u935
 */
public abstract class TimerTaskParent extends TimerTask {
    protected FXMLDocumentController parent = null;
    public void setParent(FXMLDocumentController parent) {
        this.parent = parent;
    }
    public FXMLDocumentController getParent() {
        return(this.parent);
    }
}
