package pablocos.gestor_biblioteca.kinal.exception;

/** Solicitud con datos incoherentes que no se pueden expresar con anotaciones de validacion. Se responde con 400. */
public class InvalidRequestException extends RuntimeException {

    public InvalidRequestException(String message) {
        super(message);
    }
}
