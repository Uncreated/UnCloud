package com.uncreated.uncloud.client.auth.view;

import com.uncreated.uncloud.client.View;

import java.util.Set;

public interface AuthView
		extends View
{
	String getJsonAuthInf();

	void setJsonAuthInf(String json);

	void setUsers(Set<String> logins);

	void selectUser(String login, boolean autoAuth);

	//response
	void onAuthOk();

	void onRegisterOk();

	void onRequestTimeout();

	void onRequestIncorrect();
}
