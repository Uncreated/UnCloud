package com.uncreated.uncloud.client;

import com.uncreated.uncloud.common.RequestException;
import com.uncreated.uncloud.common.filestorage.FNode;
import com.uncreated.uncloud.common.filestorage.FileTransfer;
import com.uncreated.uncloud.common.filestorage.FolderNode;
import com.uncreated.uncloud.server.auth.Session;
import com.uncreated.uncloud.server.auth.User;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

class RequestHandler
{
	private static final int TRY_COUNT = 3;
	private static final String API_URL = "http://192.168.1.43:8080/api/";
	private static final RestTemplate restTemplate = new RestTemplate();

	private Session session;

	RequestStatus register(String login, String password)
	{
		Request request = new Request("register", HttpMethod.POST);
		try
		{
			request.go(new User(login, password), String.class);
			return new RequestStatus(true);
		}
		catch (RequestException e)
		{
			e.printStackTrace();
			return new RequestStatus(false, e.getMessage());
		}
	}

	RequestStatus auth(String login, String password)
	{
		Request request = new Request("auth", HttpMethod.POST);
		try
		{
			session = request.go(new User(login, password), Session.class);
			return new RequestStatus(true);
		}
		catch (RequestException e)
		{
			e.printStackTrace();
			return new RequestStatus(false, e.getMessage());
		}

	}

	RequestStatus<FolderNode> files()
	{
		Request request = new Request("files", HttpMethod.GET);
		try
		{
			FolderNode folderNode = request.go(FolderNode.class);
			return new RequestStatus<FolderNode>(true).setData(folderNode);
		}
		catch (RequestException e)
		{
			e.printStackTrace();
			return new RequestStatus<FolderNode>(false, e.getMessage());
		}
	}

	RequestStatus<FileTransfer> downloadFilePart(String path, Integer part)
	{
		Request request = new Request("file", HttpMethod.GET);
		request.add("part", part.toString());
		request.add("path", path);
		try
		{
			FileTransfer fileTransfer = request.go(FileTransfer.class);
			return new RequestStatus<FileTransfer>(true).setData(fileTransfer);
		}
		catch (RequestException e)
		{
			e.printStackTrace();
			return new RequestStatus<FileTransfer>(false, e.getMessage());
		}
	}

	RequestStatus<FNode> removeFile(String path)
	{
		Request request = new Request("file", HttpMethod.DELETE);
		request.add("path", path);
		try
		{
			request.go(String.class);
			return new RequestStatus<>(true);
		}
		catch (RequestException e)
		{
			e.printStackTrace();
			return new RequestStatus<>(false, e.getMessage());
		}
	}

	RequestStatus setFile(FileTransfer fileTransfer)
	{
		Request request = new Request("file", HttpMethod.POST);
		try
		{
			request.go(fileTransfer, String.class);
			return new RequestStatus(true);
		}
		catch (RequestException e)
		{
			e.printStackTrace();
			return new RequestStatus(false, e.getMessage());
		}
	}

	RequestStatus createFolder(String path)
	{
		Request request = new Request("folder", HttpMethod.POST);
		request.add("path", path);
		try
		{
			request.go(FNode.class);
			return new RequestStatus(true);
		}
		catch (RequestException e)
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

		Request(String call, HttpMethod method)
		{
			this.call = call;
			this.method = method;
		}

		private <RESP> RESP go(Class<RESP> tClass) throws RequestException
		{
			return go("", tClass);
		}

		private <RESP, REQ> RESP go(REQ req, Class<RESP> tClass) throws RequestException
		{
			for (int i = 0; i < TRY_COUNT; i++)
			{
				try
				{
					return goTry(req, tClass);
				}
				catch (RequestException e)
				{
					e.printStackTrace();
					if (i == TRY_COUNT - 1)
					{
						throw e;
					}
				}
			}
			return null;
		}

		private <RESP, REQ> RESP goTry(REQ req, Class<RESP> tClass) throws RequestException
		{
			try
			{
				HttpHeaders httpHeaders = new HttpHeaders();

				MediaType mediaType = new MediaType("application", "json", StandardCharsets.UTF_8);
				httpHeaders.setContentType(mediaType);
				if (session != null)
				{
					httpHeaders.set("Authorization", session.getAccessToken());
				}

				HttpEntity<REQ> entity = new HttpEntity<>(req, httpHeaders);
				try
				{
					return restTemplate.exchange(getUrl(), method, entity, tClass).getBody();
				}
				catch (HttpClientErrorException e)
				{
					throw new RequestException(e.getStatusText(), e.getStatusCode());
				}
			}
			catch (ResourceAccessException e)
			{
				throw new RequestException("Connection timed out", null);
			}
		}

		void add(String name, String value)
		{
			params.add(new Param(name, value));
		}

		URI getUrl()
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
					try
					{
						String value = URLEncoder.encode(param.value, "UTF-8");
						builder.append(param.name);
						builder.append("=");
						builder.append(value);
						if (i != szi - 1)
						{
							builder.append("&");
						}
					}
					catch (UnsupportedEncodingException e)
					{
						e.printStackTrace();
					}
				}
			}
			try
			{
				return new URI(builder.toString());
			}
			catch (URISyntaxException e)
			{
				e.printStackTrace();
				return null;
			}
		}

		private class Param
		{
			String name;
			String value;

			Param(String name, String value)
			{
				this.name = name;
				this.value = value;
			}
		}
	}
}
