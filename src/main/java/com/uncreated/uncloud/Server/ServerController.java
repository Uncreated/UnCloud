package com.uncreated.uncloud.Server;

import com.uncreated.uncloud.Server.auth.AuthService;
import com.uncreated.uncloud.Server.auth.Session;
import com.uncreated.uncloud.Server.auth.User;
import com.uncreated.uncloud.Server.auth.UsersRepository;
import com.uncreated.uncloud.Server.storage.FileTransfer;
import com.uncreated.uncloud.Server.storage.StorageService;
import com.uncreated.uncloud.Server.storage.UserFiles;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@RestController
@RequestMapping("/api")
public class ServerController
{
	private AuthService authService;
	private StorageService storageService;

	public ServerController(UsersRepository usersRepository)
	{
		authService = new AuthService(usersRepository);
		storageService = new StorageService();
	}

	@RequestMapping(value = "/register", method = POST, consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	public ResponseEntity<Answer> register(HttpEntity<User> httpEntity)
	{
		try
		{
			authService.register(httpEntity.getBody());
			return ResponseEntity.status(HttpStatus.OK).body(null);
		} catch (RequestException e)
		{
			return ResponseEntity.status(e.getHttpCode()).body(e.getErrorMsg());
		}
	}

	@RequestMapping(value = "/auth", method = POST, consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	public ResponseEntity<Answer> auth(HttpEntity<User> httpEntity)
	{
		try
		{
			User user = httpEntity.getBody();
			Session session = authService.login(user.getLogin(), user.getPassword());
			return ResponseEntity.status(HttpStatus.OK).body(session);
		} catch (RequestException e)
		{
			return ResponseEntity.status(e.getHttpCode()).body(e.getErrorMsg());
		}
	}

	@RequestMapping(value = "/files", method = GET)
	public ResponseEntity<Answer> files(HttpEntity<String> httpEntity)
	{
		try
		{
			String login = authService.getLogin(httpEntity);
			UserFiles userFiles = storageService.getUserFiles(login);
			return ResponseEntity.status(HttpStatus.OK).body(userFiles);
		} catch (RequestException e)
		{
			return ResponseEntity.status(e.getHttpCode()).body(e.getErrorMsg());
		}
	}

	@RequestMapping(value = "/file", method = GET)
	public ResponseEntity<Answer> getFile(@RequestParam(value = "path") String path,
										  @RequestParam(value = "part") Integer part,
										  HttpEntity<String> httpEntity)
	{
		try
		{
			String login = authService.getLogin(httpEntity);
			FileTransfer fileTransfer = storageService.getFile(login, path, part);
			return ResponseEntity.status(HttpStatus.OK).body(fileTransfer);
		} catch (RequestException e)
		{
			return ResponseEntity.status(e.getHttpCode()).body(e.getErrorMsg());
		}
	}

	@RequestMapping(value = "/file", method = RequestMethod.DELETE)
	public ResponseEntity<Answer> deleteFile(@RequestParam(value = "path") String path,
											 HttpEntity<String> httpEntity)
	{
		try
		{
			String login = authService.getLogin(httpEntity);
			storageService.removeFile(login, path);
			return ResponseEntity.status(HttpStatus.OK).body(null);
		} catch (RequestException e)
		{
			return ResponseEntity.status(e.getHttpCode()).body(e.getErrorMsg());
		}
	}

	@RequestMapping(value = "/file", method = POST, consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	public ResponseEntity<Answer> putFile(HttpEntity<FileTransfer> httpEntity)
	{
		try
		{
			String login = authService.getLogin(httpEntity);
			storageService.setFile(login, httpEntity.getBody());
			return ResponseEntity.status(HttpStatus.OK).body(null);
		} catch (RequestException e)
		{
			return ResponseEntity.status(e.getHttpCode()).body(e.getErrorMsg());
		}
	}
}
