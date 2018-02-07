package com.uncreated.uncloud.Server;

import org.springframework.http.HttpStatus;

public class RequestException extends Exception
{
	private ErrorMsg errorMsg;
	private HttpStatus httpCode;


	public RequestException(String errorMsg)
	{
		this(errorMsg, HttpStatus.BAD_REQUEST);
	}

	public RequestException(String errorMsg, HttpStatus httpCode)
	{
		super(errorMsg);
		this.httpCode = httpCode;
		this.errorMsg = new ErrorMsg(errorMsg);

		System.out.println("RequestException: CODE=" + httpCode + "\n" + errorMsg);
	}

	ErrorMsg getErrorMsg()
	{
		return errorMsg;
	}

	HttpStatus getHttpCode()
	{
		return httpCode;
	}

	public class ErrorMsg implements Answer
	{
		String msg;

		public ErrorMsg()
		{
		}

		ErrorMsg(String msg)
		{
			this.msg = msg;
		}

		public String getMsg()
		{
			return msg;
		}
	}
}
