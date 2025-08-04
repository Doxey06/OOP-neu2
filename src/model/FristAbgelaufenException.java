package model;

/**
 * Exception für abgelaufene Fristen
 */
public class FristAbgelaufenException extends Exception {
    public FristAbgelaufenException(String message) {
        super(message);
    }
}
