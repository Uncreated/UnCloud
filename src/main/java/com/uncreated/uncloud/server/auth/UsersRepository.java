package com.uncreated.uncloud.server.auth;

import org.springframework.data.repository.CrudRepository;

public interface UsersRepository
		extends CrudRepository<User, String>
{
}
