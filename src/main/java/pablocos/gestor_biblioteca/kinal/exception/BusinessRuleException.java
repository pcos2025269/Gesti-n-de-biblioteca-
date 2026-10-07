package pablocos.gestor_biblioteca.kinal.exception;

/** Violacion de una regla de negocio o conflicto de estado. Se responde con 409. */
public class BusinessRuleException extends RuntimeException {

    public BusinessRuleException(String message) {
        super(message);
    }
}
