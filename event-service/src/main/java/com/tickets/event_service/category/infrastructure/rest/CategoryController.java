package com.tickets.event_service.category.infrastructure.rest;

import com.tickets.event_service.category.application.*;
import com.tickets.event_service.category.infrastructure.rest.dto.CategoryRequest;
import com.tickets.event_service.category.infrastructure.rest.dto.CategoryResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
@Tag(name = "Categories")
public class CategoryController {

    private final CreateCategoryUseCase createCategory;
    private final GetCategoryByIdUseCase getCategoryById;
    private final ListCategoriesUseCase listCategories;
    private final UpdateCategoryUseCase updateCategory;
    private final DeleteCategoryUseCase deleteCategory;
    private final CategoryRestMapper mapper;

    public CategoryController(CreateCategoryUseCase createCategory,
                               GetCategoryByIdUseCase getCategoryById,
                               ListCategoriesUseCase listCategories,
                               UpdateCategoryUseCase updateCategory,
                               DeleteCategoryUseCase deleteCategory,
                               CategoryRestMapper mapper) {
        this.createCategory = createCategory;
        this.getCategoryById = getCategoryById;
        this.listCategories = listCategories;
        this.updateCategory = updateCategory;
        this.deleteCategory = deleteCategory;
        this.mapper = mapper;
    }

    @GetMapping
    @Operation(summary = "Listar todas las categorías")
    public List<CategoryResponse> findAll() {
        return listCategories.execute().stream()
                .map(mapper::toResponse)
                .toList();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener categoría por ID")
    public CategoryResponse findById(@PathVariable Long id) {
        return mapper.toResponse(getCategoryById.execute(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Crear categoría (solo ADMIN)")
    public CategoryResponse create(@Valid @RequestBody CategoryRequest request) {
        return mapper.toResponse(createCategory.execute(mapper.toCommand(request)));
    }

    @PutMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Actualizar categoría (solo ADMIN)")
    public CategoryResponse update(@PathVariable Long id,
                                    @Valid @RequestBody CategoryRequest request) {
        return mapper.toResponse(updateCategory.execute(id, mapper.toCommand(request)));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Eliminar categoría (solo ADMIN)")
    public void delete(@PathVariable Long id) {
        deleteCategory.execute(id);
    }
}
