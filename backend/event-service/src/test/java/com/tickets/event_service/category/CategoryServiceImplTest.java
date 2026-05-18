package com.tickets.event_service.category;

import com.tickets.event_service.category.application.CreateCategoryUseCase;
import com.tickets.event_service.category.application.DeleteCategoryUseCase;
import com.tickets.event_service.category.application.GetCategoryByIdUseCase;
import com.tickets.event_service.category.application.ListCategoriesUseCase;
import com.tickets.event_service.category.application.UpdateCategoryUseCase;
import com.tickets.event_service.category.application.dto.CategoryCommand;
import com.tickets.event_service.category.domain.Category;
import com.tickets.event_service.category.domain.CategoryRepository;
import com.tickets.event_service.exception.CategoryNotFoundException;
import com.tickets.event_service.exception.DuplicateNameException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
@DisplayName("Category UseCases")
class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    private Category existingCategory;

    @BeforeEach
    void setUp() {
        existingCategory = new Category(1L, "Música", "Conciertos y festivales");
    }

    @Nested
    @DisplayName("CreateCategoryUseCase")
    class Create {

        private CreateCategoryUseCase useCase;

        @BeforeEach
        void init() {
            useCase = new CreateCategoryUseCase(categoryRepository);
        }

        @Test
        @DisplayName("debe crear la categoría cuando el nombre no existe")
        void shouldCreate_whenNameIsUnique() {
            CategoryCommand command = new CategoryCommand("Teatro", "Obras de teatro");
            given(categoryRepository.existsByNameIgnoreCase("Teatro")).willReturn(false);
            given(categoryRepository.save(any(Category.class))).willReturn(existingCategory);

            Category result = useCase.execute(command);

            assertThat(result).isNotNull();
            then(categoryRepository).should().save(any(Category.class));
        }

        @Test
        @DisplayName("debe lanzar DuplicateNameException cuando el nombre ya existe")
        void shouldThrow_whenNameIsDuplicated() {
            CategoryCommand command = new CategoryCommand("Música", null);
            given(categoryRepository.existsByNameIgnoreCase("Música")).willReturn(true);

            assertThatThrownBy(() -> useCase.execute(command))
                    .isInstanceOf(DuplicateNameException.class)
                    .hasMessageContaining("Música");

            then(categoryRepository).should(never()).save(any());
        }
    }

    @Nested
    @DisplayName("GetCategoryByIdUseCase")
    class FindById {

        private GetCategoryByIdUseCase useCase;

        @BeforeEach
        void init() {
            useCase = new GetCategoryByIdUseCase(categoryRepository);
        }

        @Test
        @DisplayName("debe retornar la categoría cuando existe")
        void shouldReturn_whenFound() {
            given(categoryRepository.findById(1L)).willReturn(Optional.of(existingCategory));

            Category result = useCase.execute(1L);

            assertThat(result.getName()).isEqualTo("Música");
        }

        @Test
        @DisplayName("debe lanzar CategoryNotFoundException cuando no existe")
        void shouldThrow_whenNotFound() {
            given(categoryRepository.findById(99L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> useCase.execute(99L))
                    .isInstanceOf(CategoryNotFoundException.class)
                    .hasMessageContaining("99");
        }
    }

    @Nested
    @DisplayName("ListCategoriesUseCase")
    class FindAll {

        private ListCategoriesUseCase useCase;

        @BeforeEach
        void init() {
            useCase = new ListCategoriesUseCase(categoryRepository);
        }

        @Test
        @DisplayName("debe retornar todas las categorías")
        void shouldReturnAll() {
            given(categoryRepository.findAll()).willReturn(List.of(existingCategory));

            List<Category> result = useCase.execute();

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getName()).isEqualTo("Música");
        }
    }

    @Nested
    @DisplayName("UpdateCategoryUseCase")
    class Update {

        private UpdateCategoryUseCase useCase;

        @BeforeEach
        void init() {
            useCase = new UpdateCategoryUseCase(categoryRepository);
        }

        @Test
        @DisplayName("debe actualizar la categoría cuando el nuevo nombre es único")
        void shouldUpdate_whenNameIsUnique() {
            CategoryCommand command = new CategoryCommand("Teatro", "Nuevo desc");
            given(categoryRepository.findById(1L)).willReturn(Optional.of(existingCategory));
            given(categoryRepository.existsByNameIgnoreCase("Teatro")).willReturn(false);
            given(categoryRepository.save(any(Category.class))).willReturn(existingCategory);

            useCase.execute(1L, command);

            then(categoryRepository).should().save(any(Category.class));
        }

        @Test
        @DisplayName("debe lanzar CategoryNotFoundException cuando no existe")
        void shouldThrow_whenNotFound() {
            given(categoryRepository.findById(99L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> useCase.execute(99L, new CategoryCommand("X", null)))
                    .isInstanceOf(CategoryNotFoundException.class);

            then(categoryRepository).should(never()).save(any());
        }
    }

    @Nested
    @DisplayName("DeleteCategoryUseCase")
    class Delete {

        private DeleteCategoryUseCase useCase;

        @BeforeEach
        void init() {
            useCase = new DeleteCategoryUseCase(categoryRepository);
        }

        @Test
        @DisplayName("debe eliminar la categoría cuando existe")
        void shouldDelete_whenExists() {
            given(categoryRepository.existsById(1L)).willReturn(true);

            useCase.execute(1L);

            then(categoryRepository).should().deleteById(1L);
        }

        @Test
        @DisplayName("debe lanzar CategoryNotFoundException cuando no existe")
        void shouldThrow_whenNotFound() {
            given(categoryRepository.existsById(99L)).willReturn(false);

            assertThatThrownBy(() -> useCase.execute(99L))
                    .isInstanceOf(CategoryNotFoundException.class);

            then(categoryRepository).should(never()).deleteById(any());
        }
    }
}
