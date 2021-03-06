package io.subutai.core.environment.api.exception;


/**
 * Thrown in process of environment destruction if some conditions don't apply to destruction process needs
 */
public class EnvironmentDestructionException extends Exception
{
    /**
     * Constructs a new exception with the specified cause and a detail message of <tt>(cause==null ? null :
     * cause.toString())</tt> (which typically contains the class and detail message of <tt>cause</tt>).
     *
     * @param cause the cause (which is saved for later retrieval by the {@link #getCause()} method).  (A <tt>null</tt>
     * value is permitted, and indicates that the cause is nonexistent or unknown.)
     */
    public EnvironmentDestructionException( final Throwable cause )
    {
        super( cause );
    }


    /**
     * Constructs a new exception with the specified detail message.  The cause is not initialized, and may subsequently
     * be initialized by a call to {@link #initCause}.
     *
     * @param message the detail message. The detail message is saved for later retrieval by the {@link #getMessage()}
     * method.
     */
    public EnvironmentDestructionException( final String message )
    {
        super( message );
    }
}
