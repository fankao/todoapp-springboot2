package com.apress.todo.controller;

import com.apress.todo.domain.Todo;
import com.apress.todo.domain.TodoBuilder;
import com.apress.todo.repository.CommonRepository;
import com.apress.todo.validation.TodoValidationError;
import com.apress.todo.validation.TodoValidationErrorBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.validation.Valid;
import java.net.URI;

@RestController
@RequestMapping("/api")
public class TodoController {
    private CommonRepository<Todo> todoRepository;

    public TodoController(CommonRepository<Todo> todoCommonRepository) {
        this.todoRepository = todoCommonRepository;
    }

    @GetMapping("/todo")
    public ResponseEntity<Iterable<Todo>> getToDos() {
        return ResponseEntity.ok(todoRepository.findAll());
    }

    @GetMapping("/todo/{id}")
    public ResponseEntity<Todo> getToDoById(@PathVariable("id") String id) {
        return ResponseEntity.ok(todoRepository.findById(id));
    }

    @PatchMapping("/todo/{id}")
    public ResponseEntity<Todo> setCompleted(@PathVariable String id) {
        Todo result = todoRepository.findById(id);
        result.setCompleted(true);
        todoRepository.save(result);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .buildAndExpand(result.getId()).toUri();
        return ResponseEntity.ok().header("Location", location.toString()).build();
    }

    @RequestMapping(value = "/todo",method = {RequestMethod.PUT,RequestMethod.POST})
    public ResponseEntity<?> createTodo(@Valid @RequestBody Todo todo, Errors errors){
        if(errors.hasErrors()){
            return ResponseEntity.badRequest().body(TodoValidationErrorBuilder.fromBindingError(errors));
        }
        Todo result = todoRepository.save(todo);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().
                path("/{id}")
                .buildAndExpand(result.getId()).toUri();
        return ResponseEntity.created(location).build();

    }

    @DeleteMapping("/todo/{id}")
    public ResponseEntity<Todo> deleteTodo(@PathVariable("id") String id) {
        todoRepository.delete(TodoBuilder.create().withId(id).build());
        return ResponseEntity.noContent().build();
    }
    @DeleteMapping("/todo")
    public ResponseEntity<Todo> deleteTodo(@RequestBody Todo todo) {
        todoRepository.delete(todo);
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public TodoValidationError handleException(Exception e){
        return new TodoValidationError(e.getMessage());
    }


}
