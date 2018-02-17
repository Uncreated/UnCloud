package com.uncreated.uncloud.server.auth;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class Session
{
	private String accessToken;
	private Long expiryDate;

	private String login;

	public Session()
	{
	}

	public Session(String accessToken, String login)
	{
		this.accessToken = accessToken;
		this.login = login;
		expiryDate = System.currentTimeMillis() + 86400000L;//day
	}

	public String getAccessToken()
	{
		return accessToken;
	}

	public Long getExpiryDate()
	{
		return expiryDate;
	}

	public String getLogin()
	{
		return login;
	}

	@JsonIgnore
	public boolean isExpired()
	{
		return System.currentTimeMillis() > expiryDate;
	}
}