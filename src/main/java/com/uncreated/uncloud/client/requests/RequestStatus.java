package com.uncreated.uncloud.client.requests;

public class RequestStatus<T>
{
	private boolean ok;
	private String msg;
	private T data;

	public RequestStatus(boolean ok, String msg)
	{
		this.ok = ok;
		this.msg = msg;
	}

	public T getData()
	{
		return data;
	}

	public RequestStatus<T> setData(T data)
	{
		this.data = data;
		return this;
	}

	public RequestStatus(boolean ok)
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
