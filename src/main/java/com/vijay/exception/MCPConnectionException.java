package com.vijay.exception;

/**
 * Exception for MCP (Model Context Protocol) connection related errors
 */
public class MCPConnectionException extends ChatServiceException {
    
    private final String serverName;
    
    public MCPConnectionException(String serverName, String message) {
        super("MCP_CONNECTION_ERROR", message, 503);
        this.serverName = serverName;
    }
    
    public MCPConnectionException(String serverName, String message, Throwable cause) {
        super("MCP_CONNECTION_ERROR", message, 503, cause);
        this.serverName = serverName;
    }
    
    public MCPConnectionException(String serverName, String errorCode, String message, int httpStatus) {
        super(errorCode, message, httpStatus);
        this.serverName = serverName;
    }
    
    public MCPConnectionException(String serverName, String errorCode, String message, int httpStatus, Throwable cause) {
        super(errorCode, message, httpStatus, cause);
        this.serverName = serverName;
    }
    
    public String getServerName() {
        return serverName;
    }
}
