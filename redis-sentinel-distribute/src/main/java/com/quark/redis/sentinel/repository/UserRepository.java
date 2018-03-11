package com.quark.redis.sentinel.repository;


import com.quark.redis.sentinel.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

/**
 * Created by Administrator on 2018/1/22/022.
 */
public interface UserRepository extends CrudRepository<User,Long> {

    Page<User> findAll(Pageable pageable);

    User findByNameAndId(String name, Long id);

    List<User>  findByName(String name);


}
