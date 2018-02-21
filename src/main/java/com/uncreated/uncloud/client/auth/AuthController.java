package com.uncreated.uncloud.client.auth;

import com.google.gson.Gson;
import com.uncreated.uncloud.client.Controller;
import com.uncreated.uncloud.client.auth.view.AuthView;
import com.uncreated.uncloud.client.requests.RequestHandler;
import com.uncreated.uncloud.client.requests.RequestStatus;
import com.uncreated.uncloud.server.auth.User;

import java.util.HashMap;

public class AuthController
		extends Controller<AuthView>
{
	private Gson gson;

	private AuthInfBox authInfBox;
	private String selLogin;

	private boolean autoAuth = true;

	public AuthController(RequestHandler requestHandler)
	{
		super(requestHandler);

		this.gson = new Gson();
		this.requestHandler = requestHandler;
	}

	public String getSelLogin()
	{
		return selLogin;
	}

	@Override
	public synchronized void onAttach(AuthView authView)
	{
		super.onAttach(authView);

		String json = authView.getJsonAuthInf();
		try
		{
			authInfBox = gson.fromJson(json, AuthInfBox.class);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		if (authInfBox == null)
		{
			authInfBox = new AuthInfBox();
		}
		selLogin = authInfBox.getLastLogin();
		authView.setUsers(authInfBox.getMap().keySet());
		if (selLogin != null)
		{
			authView.selectUser(selLogin, autoAuth);
			autoAuth = false;
		}
	}

	public void selectUser(String login)
	{
		selLogin = login;
		view.selectUser(login, false);
	}

	public void auth()
	{
		if (selLogin == null)
		{
			view.onRequestIncorrect();
			return;
		}
		AuthInf authInf = authInfBox.getMap().get(selLogin);

		runThread(() ->
		{
			RequestStatus<String> requestStatus = requestHandler.auth(authInf.getAccessToken());
			if (!requestStatus.isOk())
			{
				auth(authInf.getUser());
			}
			else
			{
				authCall(requestStatus, authInf);
			}
		});
	}

	private void auth(User user)
	{
		AuthInf authInf = new AuthInf();
		authInf.setUser(user);
		RequestStatus<String> requestStatus = requestHandler.auth(authInf.getUser());
		authCall(requestStatus, authInf);
	}

	public void auth(String login, String password)
	{
		runThread(() ->
		{
			auth(new User(login, password));
		});
	}

	private void authCall(RequestStatus<String> requestStatus, AuthInf authInf)
	{
		call(() ->
		{
			if (requestStatus.isOk())
			{
				authInf.setAccessToken(requestStatus.getData());
				authInfBox.getMap().put(authInf.getUser().getLogin(), authInf);
				authInfBox.setLastLogin(authInf.getUser().getLogin());
				view.setJsonAuthInf(gson.toJson(authInfBox));
				view.onAuthOk();
			}
			else
			{
				view.onRequestIncorrect();
			}
		});
	}

	public void register(String login, String password)
	{
		runThread(() ->
		{
			RequestStatus requestStatus = requestHandler.register(login, password);
			call(() ->
			{
				if (requestStatus.isOk())
				{
					view.onRegisterOk();
				}
				else
				{
					view.onRequestIncorrect();
				}
			});
		});
	}

	private class AuthInfBox
	{
		private HashMap<String, AuthInf> authInfMap;
		private String lastLogin;

		AuthInfBox()
		{
			authInfMap = new HashMap<>();
		}

		HashMap<String, AuthInf> getMap()
		{
			return authInfMap;
		}

		void setMap(HashMap<String, AuthInf> authInfMap)
		{
			this.authInfMap = authInfMap;
		}

		String getLastLogin()
		{
			return lastLogin;
		}

		void setLastLogin(String lastLogin)
		{
			this.lastLogin = lastLogin;
		}
	}

	private class AuthInf
	{
		private User user;
		private String accessToken;

		AuthInf()
		{
		}

		User getUser()
		{
			return user;
		}

		void setUser(User user)
		{
			this.user = user;
		}

		String getAccessToken()
		{
			return accessToken;
		}

		void setAccessToken(String accessToken)
		{
			this.accessToken = accessToken;
		}
	}
}
