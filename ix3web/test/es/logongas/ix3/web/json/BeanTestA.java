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
public class BeanTestA {
    private BeanTestB prop1=new BeanTestB();
     private BeanTestB prop2=new BeanTestB();

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

    /**
     * @return the prop2
     */
    public BeanTestB getProp2() {
        return prop2;
    }

    /**
     * @param prop2 the prop2 to set
     */
    public void setProp2(BeanTestB prop2) {
        this.prop2 = prop2;
    }
    
}
