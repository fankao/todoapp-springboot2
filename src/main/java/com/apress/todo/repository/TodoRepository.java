package com.apress.todo.repository;

import org.springframework.data.repository.CrudRepository;

import com.apress.todo.domain.Todo;

public interface TodoRepository extends CrudRepository<Todo, String>{
	
}
