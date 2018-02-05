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
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import javax.validation.constraints.NotNull;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

public class RequestHandler
{
	private static final String API_URL = "http://192.168.1.42:8080/api/";
	private static final RestTemplate restTemplate = new RestTemplate();

	Session session;

	public RequestStatus register(String login, String password)
	{
		Request request = new Request("register", HttpMethod.POST);
		try
		{
			request.go(new User(login, password), String.class);
			return new RequestStatus(true);
		} catch (RequestException e)
		{
			e.printStackTrace();
			return new RequestStatus(false, e.getMessage());
		}
	}

	public RequestStatus auth(String login, String password)
	{
		Request request = new Request("auth", HttpMethod.POST);
		try
		{
			session = request.go(new User(login, password), Session.class);
			return new RequestStatus(true);
		} catch (RequestException e)
		{
			e.printStackTrace();
			return new RequestStatus(false, e.getMessage());
		}

	}

	public RequestStatus<UserFiles> files()
	{
		Request request = new Request("files", HttpMethod.GET);
		try
		{
			UserFiles userFiles = request.go("", UserFiles.class);
			return new RequestStatus<UserFiles>(true).setData(userFiles);
		} catch (RequestException e)
		{
			e.printStackTrace();
			return new RequestStatus<UserFiles>(false, e.getMessage());
		}
	}

	public RequestStatus<FileTransfer> getFile(String path, Integer part)
	{
		Request request = new Request("file", HttpMethod.GET);
		request.add("path", path);
		request.add("part", part.toString());
		try
		{
			FileTransfer fileTransfer = request.go("", FileTransfer.class);
			return new RequestStatus<FileTransfer>(true).setData(fileTransfer);
		} catch (RequestException e)
		{
			e.printStackTrace();
			return new RequestStatus<FileTransfer>(false, e.getMessage());
		}
	}

	public RequestStatus removeFile(String path)
	{
		Request request = new Request("file", HttpMethod.DELETE);
		request.add("path", path);
		try
		{
			request.go("", String.class);
			return new RequestStatus(true);
		} catch (RequestException e)
		{
			e.printStackTrace();
			return new RequestStatus(false, e.getMessage());
		}
	}

	public RequestStatus setFile(FileTransfer fileTransfer)
	{
		Request request = new Request("file", HttpMethod.POST);
		try
		{
			request.go(fileTransfer, String.class);
			return new RequestStatus(true);
		} catch (RequestException e)
		{
			e.printStackTrace();
			return new RequestStatus(false, e.getMessage());
		}
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
			try
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
			} catch (ResourceAccessException e)
			{
				throw new RequestException("Connection timed out", null);
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
