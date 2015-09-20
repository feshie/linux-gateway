package org.mountainsensing.fetcher;

import java.io.IOException;
import java.net.URI;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;

/**
 * CoapException. Represents a CoAP communication issue on a given URI, using a given method.
 */
public class CoapException extends IOException {

    public static final long serialVersionUID = 1;

    /**
     * CoAP Methods.
     */
    public static enum Method {
        GET(),
        POST(),
        DELETE()
    }

    /**
     * The URI on which an error occurred.
     */
    private final URI uri;

    /**
     * The response received.
     */
    private final CoapResponse response;

    /**
     * The method which was used on the URI.
     */
    private final Method method;

    /**
     * Create a new OperationException.
     * @param uri The URI on which an error occurred.
     * @param method The method used on the URI.
     * @param response The CoAP response received. NULL if no response was received.
     */
    public CoapException(URI uri, Method method, CoapResponse response) {
        this(uri, method, response, null, null);
    }

    /**
     * Create a new OperationException.
     * @param uri The URI on which an error occurred.
     * @param method The method used on the URI.
     * @param response The CoAP response received. NULL if no response was received.
     * @param message A message describing the exception.
     */
    public CoapException(URI uri, Method method, CoapResponse response, String message) {
        this(uri, method, response, message, null);
    }

    /**
     * Create a new OperationException.
     * @param uri The URI on which an error occurred.
     * @param method The method used on the URI.
     * @param response The CoAP response received. NULL if no response was received.
     * @param cause The cause of this exception.
     */
    public CoapException(URI uri, Method method, CoapResponse response, Exception cause) {
        this(uri, method, response, null, cause);
    }

    /**
     * Create a new OperationException.
     * @param uri The URI on which an error occurred.
     * @param method The method used on the URI.
     * @param response The CoAP response received. NULL if no response was received.
     * @param message A message describing the exception.
     * @param cause The cause of this exception.
     */
    public CoapException(URI uri, Method method, CoapResponse response, String message, Exception cause) {
        super(message, cause);
        this.uri = uri;
        this.method = method;
        this.response = response;
    }

    /**
     * Get the URI.
     * @return The URI on which an error occurred.
     */
    public URI getURI() {
        return uri;
    }

    /**
     * Get the method.
     * @return  The Method which was used on the URI.
     */
    public Method getMethod() {
        return method;
    }

    /**
     * Get the ResponseCode received.
     * @return The ResponseCode received at the URL. NULL if no response was received.
     */
    public ResponseCode getCode() {
        return response == null ? null : response.getCode();
    }

    @Override
    public String getMessage() {
        return super.getMessage() + ". Got CoAP response " + getCode() + " using " + getMethod() + " on " + getURI();
    }
}