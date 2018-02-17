package com.uncreated.uncloud.common;

import org.springframework.http.HttpStatus;

public class RequestException
		extends Exception
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

	public ErrorMsg getErrorMsg()
	{
		return errorMsg;
	}

	public HttpStatus getHttpCode()
	{
		return httpCode;
	}

	public class ErrorMsg
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
