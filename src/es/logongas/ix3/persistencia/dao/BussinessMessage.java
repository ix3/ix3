package es.logongas.ix3.persistencia.dao;

public class BussinessMessage {
    private final String propertyName;
    private final String message;

    public BussinessMessage(String propertyName, String message) {
        this.propertyName = propertyName;
        this.message = message;
    }



    @Override
    public String toString() {
        if (propertyName!=null) {
            return "'"+propertyName+ "'-"+message;
        } else {
            return message;
        }
    }
    

    public String getPropertyName() {
        return propertyName;
    }


    public String getMessage() {
        return message;
    }


}
