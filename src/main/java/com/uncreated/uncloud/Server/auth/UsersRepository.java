package com.uncreated.uncloud.Server.auth;

import org.springframework.data.repository.CrudRepository;

public interface UsersRepository extends CrudRepository<User, Long>
{
}
