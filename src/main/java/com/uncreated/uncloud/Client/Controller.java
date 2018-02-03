package com.uncreated.uncloud.Client;

import com.uncreated.uncloud.Server.RequestException;
import com.uncreated.uncloud.Server.auth.Session;
import com.uncreated.uncloud.Server.auth.User;
import com.uncreated.uncloud.Server.storage.FileTransfer;
import com.uncreated.uncloud.Server.storage.UserFiles;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import javax.validation.constraints.NotNull;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

public class Controller
{
	private static final String API_URL = "http://192.168.1.42:8080/api/";
	private static final RestTemplate restTemplate = new RestTemplate();

	Session session;

	public void register(String login, String password) throws RequestException
	{
		Request request = new Request("register", HttpMethod.POST);
		request.go(new User(login, password), String.class);
	}

	public Session auth(String login, String password) throws RequestException
	{
		Request request = new Request("auth", HttpMethod.POST);
		session = request.go(new User(login, password), Session.class);
		return session;
	}

	public UserFiles files() throws RequestException
	{
		Request request = new Request("files", HttpMethod.GET);
		return request.go("", UserFiles.class);
	}

	public FileTransfer getFile(String path, Integer part) throws RequestException
	{
		Request request = new Request("file", HttpMethod.GET);
		request.add("path", path);
		request.add("part", part.toString());
		return request.go("", FileTransfer.class);
	}

	public void removeFile(String path) throws RequestException
	{
		Request request = new Request("file", HttpMethod.DELETE);
		request.add("path", path);
		request.go("", String.class);
	}

	public void setFile(FileTransfer fileTransfer) throws RequestException
	{
		Request request = new Request("file", HttpMethod.POST);
		request.go(fileTransfer, String.class);
	}

	private class Request
	{
		String call;
		HttpMethod method;
		ArrayList<Param> params = new ArrayList<>();

		public Request(String call, HttpMethod method)
		{
			this.call = call;
			this.method = method;
		}

		private <REQ, RESP> RESP go(@NotNull REQ req, Class<RESP> tClass) throws RequestException
		{
			HttpHeaders httpHeaders = new HttpHeaders();
			if (session != null)
				httpHeaders.set("Authorization", session.getAccessToken());

			HttpEntity<REQ> entity = new HttpEntity<>(req, httpHeaders);
			try
			{
				return restTemplate.exchange(getUrl(), method, entity, tClass).getBody();
			} catch (HttpClientErrorException e)
			{
				throw new RequestException(e.getStatusText(), e.getStatusCode());
			}
		}

		public void add(String name, String value)
		{
			params.add(new Param(name, value));
		}

		public URI getUrl()
		{
			StringBuilder builder = new StringBuilder();
			builder.append(API_URL);
			builder.append(call);
			int szi = params.size();
			if (szi > 0)
			{
				builder.append("?");
				for (int i = 0; i < szi; i++)
				{
					Param param = params.get(i);
					builder.append(param.name);
					builder.append("=");
					builder.append(param.value);
					if (i != szi - 1)
						builder.append("&");
				}
			}
			try
			{
				return new URI(builder.toString());
			} catch (URISyntaxException e)
			{
				e.printStackTrace();
				return null;
			}
		}

		private class Param
		{
			String name;
			String value;

			public Param(String name, String value)
			{
				this.name = name;
				this.value = value;
			}
		}
	}
}
