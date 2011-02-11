/**
 *  Title: <p>
 *
 *  Description: <p>
 *
 *  Copyright: Copyright (c) <p>
 *
 *  Company: <p>
 *
 *  @author
 *
 *@version 0.9community */
package org.jini.projects.athena.exception;

/**
 *  Base Exception generated by <CODE>PooledConnection</CODE> s or the <CODE>
 *  ConnectionPool</CODE> during initialization
 *@author     calum
 *
 */
public class PooledConnectionException extends Exception {
    String message;


    /**
     *  Default Constructor
     *
     *@since
     */
    public PooledConnectionException() {
    }


    /**
     *  Creates an exception with the given message
     *
     *@param  s  Exception message
     *@since
     */
    public PooledConnectionException(String s) {
        message = s;
    }


    /**
     *  Gets the message for this exception
     *
     *@return    Exception message
     *@since
     */
    public String getMessage() {
        return message;
    }

}
