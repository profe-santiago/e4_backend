package com.tickets.event_service.category;

import com.tickets.event_service.category.dto.CategoryRequest;
import com.tickets.event_service.category.dto.CategoryResponse;
import com.tickets.event_service.exception.CategoryNotFoundException;
import com.tickets.event_service.exception.DuplicateNameException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
@DisplayName("CategoryServiceImpl")
class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    private Category existingCategory;

    @BeforeEach
    void setUp() {
        existingCategory = new Category();
        existingCategory.setId(1L);
        existingCategory.setName("Música");
        existingCategory.setDescription("Conciertos y festivales");
    }

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("debe crear la categoría cuando el nombre no existe")
        void shouldCreate_whenNameIsUnique() {
            CategoryRequest request = buildRequest("Teatro", "Obras de teatro");
            given(categoryRepository.existsByNameIgnoreCase("Teatro")).willReturn(false);
            given(categoryRepository.save(any(Category.class))).willReturn(existingCategory);

            CategoryResponse response = categoryService.create(request);

            assertThat(response).isNotNull();
            then(categoryRepository).should().save(any(Category.class));
        }

        @Test
        @DisplayName("debe lanzar DuplicateNameException cuando el nombre ya existe")
        void shouldThrow_whenNameIsDuplicated() {
            CategoryRequest request = buildRequest("Música", null);
            given(categoryRepository.existsByNameIgnoreCase("Música")).willReturn(true);

            assertThatThrownBy(() -> categoryService.create(request))
                    .isInstanceOf(DuplicateNameException.class)
                    .hasMessageContaining("Música");

            then(categoryRepository).should(never()).save(any());
        }
    }

    @Nested
    @DisplayName("findById")
    class FindById {

        @Test
        @DisplayName("debe retornar la categoría cuando existe")
        void shouldReturn_whenFound() {
            given(categoryRepository.findById(1L)).willReturn(Optional.of(existingCategory));

            CategoryResponse response = categoryService.findById(1L);

            assertThat(response.getName()).isEqualTo("Música");
        }

        @Test
        @DisplayName("debe lanzar CategoryNotFoundException cuando no existe")
        void shouldThrow_whenNotFound() {
            given(categoryRepository.findById(99L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> categoryService.findById(99L))
                    .isInstanceOf(CategoryNotFoundException.class)
                    .hasMessageContaining("99");
        }
    }

    @Nested
    @DisplayName("findAll")
    class FindAll {

        @Test
        @DisplayName("debe retornar todas las categorías")
        void shouldReturnAll() {
            given(categoryRepository.findAll()).willReturn(List.of(existingCategory));

            List<CategoryResponse> result = categoryService.findAll();

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getName()).isEqualTo("Música");
        }
    }

    @Nested
    @DisplayName("update")
    class Update {

        @Test
        @DisplayName("debe actualizar la categoría cuando el nuevo nombre es único")
        void shouldUpdate_whenNameIsUnique() {
            CategoryRequest request = buildRequest("Teatro", "Nuevo desc");
            given(categoryRepository.findById(1L)).willReturn(Optional.of(existingCategory));
            given(categoryRepository.existsByNameIgnoreCase("Teatro")).willReturn(false);
            given(categoryRepository.save(any(Category.class))).willReturn(existingCategory);

            categoryService.update(1L, request);

            then(categoryRepository).should().save(any(Category.class));
        }

        @Test
        @DisplayName("debe lanzar CategoryNotFoundException cuando no existe")
        void shouldThrow_whenNotFound() {
            given(categoryRepository.findById(99L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> categoryService.update(99L, buildRequest("X", null)))
                    .isInstanceOf(CategoryNotFoundException.class);

            then(categoryRepository).should(never()).save(any());
        }
    }

    @Nested
    @DisplayName("delete")
    class Delete {

        @Test
        @DisplayName("debe eliminar la categoría cuando existe")
        void shouldDelete_whenExists() {
            given(categoryRepository.existsById(1L)).willReturn(true);

            categoryService.delete(1L);

            then(categoryRepository).should().deleteById(1L);
        }

        @Test
        @DisplayName("debe lanzar CategoryNotFoundException cuando no existe")
        void shouldThrow_whenNotFound() {
            given(categoryRepository.existsById(99L)).willReturn(false);

            assertThatThrownBy(() -> categoryService.delete(99L))
                    .isInstanceOf(CategoryNotFoundException.class);

            then(categoryRepository).should(never()).deleteById(any());
        }
    }

    private CategoryRequest buildRequest(String name, String description) {
        CategoryRequest req = new CategoryRequest();
        req.setName(name);
        req.setDescription(description);
        return req;
    }
}
