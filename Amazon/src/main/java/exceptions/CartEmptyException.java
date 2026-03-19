package exceptions;

public class CartEmptyException extends Exception {

    public CartEmptyException(String msg){
        super(msg);
    } 
}