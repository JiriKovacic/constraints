package com.kovacic.neo4j.schema;

/**
 * Created by Jirka on 11. 4. 2016.
 */
public class IntegrityConstraintViolationException extends Exception {
    private String exMessage;

    public IntegrityConstraintViolationException(String exMessage)
    {
        this.exMessage = exMessage;
    }

    public String getMessage()
    {
        return exMessage;
    }
}
