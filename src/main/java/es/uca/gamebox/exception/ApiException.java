package es.uca.gamebox.exception;

public class ApiException extends RuntimeException{

    public ApiException(String message) {
        super(message);
    }

    public ApiException() {
        super("An unexpected error occurred");
    }

}
