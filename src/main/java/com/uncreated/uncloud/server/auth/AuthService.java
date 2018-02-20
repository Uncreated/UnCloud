package com.uncreated.uncloud.server.auth;

import com.uncreated.uncloud.common.RequestException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;

public class AuthService
{
	private static final String ABC = "abcdefghijklmnopqrstuvwxyz";
	private final Random rand = new Random();
	private final UsersRepository usersRepository;
	private HashMap<String, Session> sessions = new HashMap<>();

	public AuthService(UsersRepository usersRepository)
	{
		this.usersRepository = usersRepository;

		User user = new User("admin", "123");
		usersRepository.save(user);
	}

	private String getLogin(String accessToken) throws RequestException
	{
		Session session = sessions.get(accessToken);
		if (session != null)
		{
			return session.getLogin();
		}

		throw new RequestException("Not authorized", HttpStatus.UNAUTHORIZED);
	}

	public String getLogin(HttpEntity<?> httpEntity) throws RequestException
	{
		return getLogin(getAccessToken(httpEntity));
	}

	public Session update(HttpEntity<?> httpEntity) throws RequestException
	{
		Session session = sessions.get(getAccessToken(httpEntity));
		if (session != null && !session.isExpired())
		{
			sessions.remove(session);
			session = generateAccessToken(session.getLogin());
			sessions.put(session.getAccessToken(), session);
			return session;
		}
		throw new RequestException("Token expired", HttpStatus.UNAUTHORIZED);
	}

	private String getAccessToken(HttpEntity<?> httpEntity)
	{
		String accessToken = httpEntity.getHeaders().getFirst("Authorization");
		System.out.println("Request: AccessToken=(" + accessToken + ")");
		return accessToken;
	}

	private User getUser(String login)
	{
		if (usersRepository.exists(login))
		{
			return usersRepository.findOne(login);
		}
		return null;
	}

	public void register(User newUser) throws RequestException
	{
		User user = getUser(newUser.getLogin());
		if (user != null)
		{
			throw new RequestException("Login already exists");
		}

		usersRepository.save(newUser);
	}

	public Session auth(String login, byte[] passwordHash) throws RequestException
	{
		User user = getUser(login);
		if (user != null)
		{
			if (Arrays.equals(user.getPasswordHash(), passwordHash))
			{
				Session session = generateAccessToken(login);
				sessions.put(session.getAccessToken(), session);
				return session;
			}

			throw new RequestException("Incorrect password");
		}

		throw new RequestException("Incorrect auth");
	}

	private Session generateAccessToken(String login)
	{
		String accessToken;

		do
		{
			accessToken = makeToken();
		}
		while (sessions.containsKey(accessToken));

		System.out.println("Response: AccessToken=(" + accessToken + ")");
		return new Session(accessToken, login);
	}

	private String makeToken()
	{
		StringBuilder builder = new StringBuilder(21);
		for (int i = 0; i < 20; i++)
		{
			builder.append(ABC.charAt(rand.nextInt(26)));
		}
		return builder.toString();
	}
}
