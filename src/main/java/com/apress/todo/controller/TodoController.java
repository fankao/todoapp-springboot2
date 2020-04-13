package com.apress.todo.controller;

import java.net.URI;
import java.util.Optional;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.apress.todo.domain.Todo;
import com.apress.todo.domain.TodoBuilder;
import com.apress.todo.repository.TodoRepository;
import com.apress.todo.validation.TodoValidationError;
import com.apress.todo.validation.TodoValidationErrorBuilder;

@RestController
@RequestMapping("/api")
public class TodoController {
	private TodoRepository todoRepository;

	@Autowired
	public TodoController(TodoRepository todoRepository) {
		this.todoRepository = todoRepository;
	}

	/**
	 * Lấy danh sách todo
	 * 
	 * @return ResponseEntity<Iterable<Todo>>
	 */
	@GetMapping("/todo")
	public ResponseEntity<Iterable<Todo>> getToDos() {
		return ResponseEntity.ok(todoRepository.findAll());
	}

	/**
	 * Lấy danh sách todo theo Id
	 * 
	 * @param id
	 * @return ResponseEntity<Todo>
	 */
	@GetMapping("/todo/{id}")
	public ResponseEntity<Todo> getToDoById(String id) {
		Optional<Todo> toDo = todoRepository.findById(id);
		if (toDo.isPresent()) {
			return ResponseEntity.ok(toDo.get());
		}
		return ResponseEntity.notFound().build();
	}

	/**
	 * Cập nhật trạng thái completed cho todo
	 * 
	 * @param id
	 * @return Xuất ra thông tin todo kèm đường dẫn lưu trong header
	 */
	@PatchMapping("/todo/{id}")
	public ResponseEntity<?> setCompleted(@PathVariable String id) {
		// Lấy Todo cần hoàn thành

		Optional<Todo> toDo = todoRepository.findById(id);
		// nếu todo không tồn tại thì xuất ra trạng thái not found
		if (!toDo.isPresent()) {
			return ResponseEntity.notFound().build();
		}

		// ngược lại thì cập nhật trạng thái completed cho todo
		Todo result = toDo.get();
		result.setCompleted(true);
		todoRepository.save(result);

		// Tạo URI lưu thông tin cho todo
		URI location = ServletUriComponentsBuilder.fromCurrentRequest().buildAndExpand(result.getId()).toUri();

		// Xuất ra thông tin todo kèm đường dẫn lưu trong header
		return ResponseEntity.ok().header("Location", location.toString()).build();

	}

	/**
	 * Tạo mới hoặc cập nhật thông tin cho todo
	 * 
	 * @param todo
	 * @param errors
	 * @return
	 */
	@RequestMapping(value = "/todo", method = { RequestMethod.POST, RequestMethod.PUT })
	public ResponseEntity<?> createToDo(@Valid @RequestBody Todo todo, Errors errors) {

		// nếu xuất hiện lỗi khi validation Request todo
		if (errors.hasErrors()) {
			// thì xuất ra trạng thái Bad Request kèm thông tin lỗi
			return ResponseEntity.badRequest().body(TodoValidationErrorBuilder.fromBindingError(errors));
		}

		// Lưu todo
		Todo result = todoRepository.save(todo);

		// tạo đường dẫn cho todo
		URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(result.getClass())
				.toUri();

		return ResponseEntity.created(location).build();

	}

	/**
	 * Xoá todo theo id
	 * @param id
	 * @return
	 */
	@DeleteMapping("/todo/{id}")
	public ResponseEntity<Todo> deleteToDo(@PathVariable String id) {
		todoRepository.delete(TodoBuilder.create().withId(id).build());
		return ResponseEntity.noContent().build();
	}

	/**
	 * Xoá todo
	 * @param id
	 * @return
	 */
	@DeleteMapping("/todo")
	public ResponseEntity<Todo> deleteToDo(@RequestBody Todo toDo) {
		todoRepository.delete(toDo);
		return ResponseEntity.noContent().build();
	}


	/**
	 * Xuất ra thông tin exception trong quá trình xử lý
	 * @param exception
	 * @return
	 */
	@ExceptionHandler
	@ResponseStatus(value = HttpStatus.BAD_REQUEST)
	public TodoValidationError handleException(Exception exception) {
		return new TodoValidationError(exception.getMessage());
	}

}
