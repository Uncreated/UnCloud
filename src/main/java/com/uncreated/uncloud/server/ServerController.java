package com.uncreated.uncloud.server;

import com.uncreated.uncloud.common.RequestException;
import com.uncreated.uncloud.common.filestorage.FileTransfer;
import com.uncreated.uncloud.common.filestorage.FolderNode;
import com.uncreated.uncloud.server.auth.AuthService;
import com.uncreated.uncloud.server.auth.Session;
import com.uncreated.uncloud.server.auth.User;
import com.uncreated.uncloud.server.auth.UsersRepository;
import com.uncreated.uncloud.server.storage.StorageService;
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
import static org.springframework.web.bind.annotation.RequestMethod.PUT;

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
	public ResponseEntity register(HttpEntity<User> httpEntity)
	{
		try
		{
			authService.register(httpEntity.getBody());
			return ResponseEntity.status(HttpStatus.OK).body(null);
		}
		catch (RequestException e)
		{
			return ResponseEntity.status(e.getHttpCode()).body(e.getErrorMsg());
		}
	}

	@RequestMapping(value = "/auth", method = POST, consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	public ResponseEntity authPost(HttpEntity<User> httpEntity)
	{
		try
		{
			User user = httpEntity.getBody();
			Session session = authService.auth(user.getLogin(), user.getPasswordHash());
			return ResponseEntity.status(HttpStatus.OK).body(session);
		}
		catch (RequestException e)
		{
			return ResponseEntity.status(e.getHttpCode()).body(e.getErrorMsg());
		}
	}

	@RequestMapping(value = "/auth", method = PUT, produces = APPLICATION_JSON_VALUE)
	public ResponseEntity authPut(HttpEntity<User> httpEntity)
	{
		try
		{
			Session session = authService.update(httpEntity);
			return ResponseEntity.status(HttpStatus.OK).body(session);
		}
		catch (RequestException e)
		{
			return ResponseEntity.status(e.getHttpCode()).body(e.getErrorMsg());
		}
	}

	@RequestMapping(value = "/files", method = GET, produces = APPLICATION_JSON_VALUE)
	public ResponseEntity files(HttpEntity httpEntity)
	{
		try
		{
			String login = authService.getLogin(httpEntity);
			FolderNode folderNode = storageService.getFiles(login);
			return ResponseEntity.status(HttpStatus.OK).body(folderNode);
		}
		catch (RequestException e)
		{
			return ResponseEntity.status(e.getHttpCode()).body(e);
		}
	}

	@RequestMapping(value = "/file", method = GET, produces = APPLICATION_JSON_VALUE)
	public ResponseEntity getFile(HttpEntity httpEntity,
								  @RequestParam(value = "path") String path,
								  @RequestParam(value = "part") Integer part)
	{
		try
		{
			String login = authService.getLogin(httpEntity);
			FileTransfer fileTransfer = storageService.getFilePart(login, path, part);
			return ResponseEntity.status(HttpStatus.OK).body(fileTransfer);
		}
		catch (RequestException e)
		{
			return ResponseEntity.status(e.getHttpCode()).body(e.getErrorMsg());
		}
	}

	@RequestMapping(value = "/file", method = RequestMethod.DELETE, produces = APPLICATION_JSON_VALUE)
	public ResponseEntity deleteFile(HttpEntity httpEntity,
									 @RequestParam(value = "path") String path)
	{
		try
		{
			String login = authService.getLogin(httpEntity);
			storageService.removeFile(login, path);
			return ResponseEntity.status(HttpStatus.OK).body(null);
		}
		catch (RequestException e)
		{
			return ResponseEntity.status(e.getHttpCode()).body(e.getErrorMsg());
		}
	}

	@RequestMapping(value = "/file", method = POST, consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	public ResponseEntity postFile(HttpEntity<FileTransfer> httpEntity)
	{
		try
		{
			String login = authService.getLogin(httpEntity);
			storageService.setFile(login, httpEntity.getBody());
			return ResponseEntity.status(HttpStatus.OK).body(null);
		}
		catch (RequestException e)
		{
			return ResponseEntity.status(e.getHttpCode()).body(e.getErrorMsg());
		}
	}

	@RequestMapping(value = "/folder", method = POST, produces = APPLICATION_JSON_VALUE)
	public ResponseEntity postFolder(HttpEntity httpEntity,
									 @RequestParam(value = "path") String path)
	{
		try
		{
			String login = authService.getLogin(httpEntity);
			storageService.createFolder(login, path);
			return ResponseEntity.status(HttpStatus.OK).body(null);
		}
		catch (RequestException e)
		{
			return ResponseEntity.status(e.getHttpCode()).body(e.getErrorMsg());
		}
	}
}
