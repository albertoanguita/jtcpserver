package jacz.commengine.communication;

/**
 * Different communication error types
 */
public class CommError {

    public enum Type {
        WRITE_NON_SERIALIZABLE_OBJECT("Attempt to write a non-serializable object"),
        CLASS_CANNOT_BE_SERIALIZED("Unable to serialize a class when writing"),
        UNKNOWN_CLASS_RECEIVED("An unknown class was received"),
        IO_CHANNEL_FAILED_WRITING("IO channel failed when writing"),
        IO_CHANNEL_FAILED_READING("IO channel failed when reading");
//        IO_CHANNEL_FAILED_DISCONNECTING("IO channel failed when disconnecting");

        String str;

        Type(String str) {
            this.str = str;
        }
    }

    private final Type type;

    private final String reason;

    private final Exception e;

    CommError(Type type, Exception e) {
        this.type = type;
        reason = type.str + " (" + e.getMessage() + ")";
        this.e = e;
    }

    public Type getType() {
        return type;
    }

    public Exception getException() {
        return e;
    }

    @Override
    public String toString() {
        return reason;
    }
}
