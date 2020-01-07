package xnetter.sock.marshal;

public class MarshalException  extends RuntimeException {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8901182323722825267L;

	public MarshalException(String message) {
        super(message);
    }

    public MarshalException(String message, Throwable cause) {
        super(message, cause);
    }

    public MarshalException(Throwable cause) {
        super(cause);
    }

    public MarshalException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
