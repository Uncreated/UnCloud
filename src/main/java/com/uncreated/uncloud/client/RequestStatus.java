package com.uncreated.uncloud.client;

public class RequestStatus<T>
{
	private boolean ok;
	private String msg;
	private T data;

	RequestStatus(boolean ok, String msg)
	{
		this.ok = ok;
		this.msg = msg;
	}

	T getData()
	{
		return data;
	}

	RequestStatus<T> setData(T data)
	{
		this.data = data;
		return this;
	}

	RequestStatus(boolean ok)
	{
		this(ok, "");
	}

	public boolean isOk()
	{
		return ok;
	}

	public String getMsg()
	{
		return msg;
	}
}
