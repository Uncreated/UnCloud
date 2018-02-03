package com.uncreated.uncloud.Server.auth;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class User
{
	@Id
	@GeneratedValue
	@JsonIgnore
	Long id;
	String login;
	String password;

	public User()
	{
	}

	public User(String login, String password)
	{
		this.login = login;
		this.password = password;
	}

	public String getLogin()
	{
		return login;
	}

	public String getPassword()
	{
		return password;
	}
}
