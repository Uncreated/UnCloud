package com.uncreated.uncloud.server.auth;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Entity
public class User
{
	@Id
	private String login;
	private byte[] passwordHash;

	public User()
	{
	}

	public User(String login, String password)
	{
		this.login = login;
		this.passwordHash = generatePasswordHash(password);
	}

	public String getLogin()
	{
		return login;
	}

	public byte[] getPasswordHash()
	{
		return passwordHash;
	}

	@JsonIgnore
	private static byte[] generatePasswordHash(String password)
	{
		try
		{
			//sha512(sha512(pass))
			byte[] passBytes = password.getBytes("UTF-8");
			byte[] sha512a = sha512(passBytes);
			byte[] sha512b = sha512(sha512a);
			return sha512b;
		}
		catch (NoSuchAlgorithmException | UnsupportedEncodingException e)
		{
			e.printStackTrace();
			return null;
		}
	}

	@JsonIgnore
	private static byte[] sha512(byte[] data) throws NoSuchAlgorithmException
	{
		return MessageDigest.getInstance("SHA-512").digest(data);
	}
}
