package com.friendlycafe.exception;

public class InvalidMailFormatException extends Exception{
	
	private static final long serialVersionUID = 1L;

	public InvalidMailFormatException(String message, Throwable cause) {
		super(message, cause);
	}


}
