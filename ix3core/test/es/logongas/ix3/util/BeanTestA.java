/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.logongas.ix3.util;

/**
 *
 * @author logongas
 */
public class BeanTestA {
    private BeanTestB prop1=new BeanTestB();

    /**
     * @return the prop1
     */
    public BeanTestB getProp1() {
        return prop1;
    }

    /**
     * @param prop1 the prop1 to set
     */
    public void setProp1(BeanTestB prop1) {
        this.prop1 = prop1;
    }
    
    public int getA() {
        return 0;
    };
    public int getA(int i) {
        return 0;
    };    
    
}
