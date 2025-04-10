package es.uca.gamebox.exception;

public class ApiException extends RuntimeException{

    public ApiException(String message) {
        super(message);
        System.err.println("ApiException lanzada: " + message);
    }

    public ApiException() {
        super("An unexpected error occurred");
        System.err.println("ApiException lanzada, error desconocido");
    }

}
