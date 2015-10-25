/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.logongas.ix3.web.json;

/**
 *
 * @author logongas
 */
public class BeanTestC {
    
    
    private int propReadOnly;
    private int propWriteOnly;
    private int prop;
    private boolean propBooleanReadOnly;

    
    public BeanTestC() {
    }

    public BeanTestC(int propReadOnly, int propWriteOnly, int prop, boolean propBooleanReadOnly) {
        this.propReadOnly = propReadOnly;
        this.propWriteOnly = propWriteOnly;
        this.prop = prop;
        this.propBooleanReadOnly = propBooleanReadOnly;
    }



    /**
     * @return the propReadOnly
     */
    public int getPropReadOnly() {
        return propReadOnly;
    }

    /**
     * @param propWriteOnly the propWriteOnly to set
     */
    public void setPropWriteOnly(int propWriteOnly) {
        this.propWriteOnly = propWriteOnly;
    }

    /**
     * @return the prop
     */
    public int getProp() {
        return prop;
    }

    /**
     * @param prop the prop to set
     */
    public void setProp(int prop) {
        this.prop = prop;
    }

    /**
     * @return the propBooleanReadOnly
     */
    public boolean isPropBooleanReadOnly() {
        return propBooleanReadOnly;
    }

    
}
