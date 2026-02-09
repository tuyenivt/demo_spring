package com.example.graphql.service;

import com.example.graphql.dto.input.CreateStudentInput;
import com.example.graphql.dto.input.UpdateStudentInput;
import com.example.graphql.dto.pagination.ConnectionInput;
import com.example.graphql.dto.pagination.PageInput;
import com.example.graphql.entity.Student;
import com.example.graphql.exception.ResourceNotFoundException;
import com.example.graphql.repository.StudentRepository;
import com.example.graphql.util.CursorUtils;
import com.example.graphql.validation.StudentValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudentServiceTest {

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private StudentValidator studentValidator;

    @InjectMocks
    private StudentService studentService;

    private Student testStudent;
    private UUID testStudentId;

    @BeforeEach
    void setUp() {
        testStudentId = UUID.randomUUID();
        testStudent = Student.builder()
                .name("John Doe")
                .address("123 Main St")
                .dateOfBirth(LocalDate.of(2000, 1, 1))
                .vehicles(Collections.emptyList())
                .build();
        testStudent.setId(testStudentId);
        testStudent.setCreatedAt(OffsetDateTime.now());
        testStudent.setUpdatedAt(OffsetDateTime.now());
    }

    @Test
    void create_shouldMapInputToEntityCorrectly() {
        var input = new CreateStudentInput("Jane Smith", "456 Oak Ave", "1999-05-15");
        var savedStudent = Student.builder()
                .name(input.name())
                .address(input.address())
                .dateOfBirth(LocalDate.parse(input.dateOfBirth()))
                .vehicles(Collections.emptyList())
                .build();
        savedStudent.setId(UUID.randomUUID());

        when(studentRepository.save(any(Student.class))).thenReturn(savedStudent);

        var result = studentService.create(input);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Jane Smith");
        assertThat(result.getAddress()).isEqualTo("456 Oak Ave");
        assertThat(result.getDateOfBirth()).isEqualTo(LocalDate.of(1999, 5, 15));

        verify(studentValidator).validateCreate(input);
        verify(studentRepository).save(any(Student.class));
    }

    @Test
    void findById_shouldReturnStudentWhenExists() {
        when(studentRepository.findById(testStudentId)).thenReturn(Optional.of(testStudent));

        var result = studentService.findById(testStudentId.toString());

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(testStudentId);
        assertThat(result.getName()).isEqualTo("John Doe");

        verify(studentRepository).findById(testStudentId);
    }

    @Test
    void findById_shouldThrowResourceNotFoundExceptionWhenNotExists() {
        when(studentRepository.findById(testStudentId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> studentService.findById(testStudentId.toString()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Student")
                .hasMessageContaining(testStudentId.toString());

        verify(studentRepository).findById(testStudentId);
    }

    @Test
    void update_shouldThrowResourceNotFoundExceptionForMissingId() {
        var missingId = UUID.randomUUID();
        var input = new UpdateStudentInput(missingId, "Updated Name", null, null);

        when(studentRepository.findById(missingId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> studentService.update(input))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Student")
                .hasMessageContaining(missingId.toString());

        verify(studentValidator).validateUpdate(input);
        verify(studentRepository).findById(missingId);
        verify(studentRepository, never()).save(any());
    }

    @Test
    void update_shouldOnlyUpdateProvidedFields() {
        var input = new UpdateStudentInput(testStudentId, "Updated Name", null, null);

        when(studentRepository.findById(testStudentId)).thenReturn(Optional.of(testStudent));
        when(studentRepository.save(any(Student.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = studentService.update(input);

        assertThat(result.getName()).isEqualTo("Updated Name");
        assertThat(result.getAddress()).isEqualTo("123 Main St"); // unchanged
        assertThat(result.getDateOfBirth()).isEqualTo(LocalDate.of(2000, 1, 1)); // unchanged

        verify(studentValidator).validateUpdate(input);
        verify(studentRepository).save(any(Student.class));
    }

    @Test
    void findPage_shouldReturnPagedResults() {
        var pageInput = new PageInput(0, 10);
        var pageable = PageRequest.of(0, 10);
        var page = new PageImpl<>(List.of(testStudent), pageable, 1);

        when(studentRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class), any(Pageable.class)))
                .thenReturn(page);

        var result = studentService.findPage(pageInput, null, null);

        assertThat(result).isNotNull();
        assertThat(result.content()).hasSize(1);
        assertThat(result.pageInfo().totalElements()).isEqualTo(1);
        assertThat(result.pageInfo().hasNext()).isFalse();

        verify(studentRepository).findAll(any(org.springframework.data.jpa.domain.Specification.class), any(Pageable.class));
    }

    @Test
    void findConnection_shouldHandleEmptyResults() {
        var connectionInput = new ConnectionInput(10, null, null, null);

        when(studentRepository.count(any(org.springframework.data.jpa.domain.Specification.class)))
                .thenReturn(0L);

        var result = studentService.findConnection(connectionInput, null, null);

        assertThat(result).isNotNull();
        assertThat(result.edges()).isEmpty();
        assertThat(result.totalCount()).isZero();
        assertThat(result.pageInfo().hasNextPage()).isFalse();
    }

    @Test
    void findConnection_shouldEncodeCursorsCorrectly() {
        var connectionInput = new ConnectionInput(10, null, null, null);
        var page = new PageImpl<>(List.of(testStudent));

        when(studentRepository.count(any(org.springframework.data.jpa.domain.Specification.class)))
                .thenReturn(1L);
        when(studentRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class), any(Pageable.class)))
                .thenReturn(page);

        var result = studentService.findConnection(connectionInput, null, null);

        assertThat(result.edges()).hasSize(1);
        var edge = result.edges().getFirst();
        assertThat(edge.cursor()).isNotNull();

        // Verify cursor can be decoded
        var decodedId = CursorUtils.decode(edge.cursor());
        assertThat(decodedId).isEqualTo(testStudentId);
    }

    @Test
    void createAll_shouldBulkCreateMultipleStudents() {
        var input1 = new CreateStudentInput("Student 1", "Addr 1", "2000-01-01");
        var input2 = new CreateStudentInput("Student 2", "Addr 2", "2001-02-02");
        var inputs = List.of(input1, input2);

        var student1 = Student.builder().name("Student 1").address("Addr 1").dateOfBirth(LocalDate.of(2000, 1, 1)).build();
        var student2 = Student.builder().name("Student 2").address("Addr 2").dateOfBirth(LocalDate.of(2001, 2, 2)).build();
        student1.setId(UUID.randomUUID());
        student2.setId(UUID.randomUUID());

        when(studentRepository.saveAll(any())).thenReturn(List.of(student1, student2));

        var result = studentService.createAll(inputs);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("Student 1");
        assertThat(result.get(1).getName()).isEqualTo("Student 2");

        verify(studentValidator, times(2)).validateCreate(any());
        verify(studentRepository).saveAll(any());
    }

    @Test
    void findAll_shouldUsePaginationToLimitResults() {
        when(studentRepository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(testStudent)));

        var result = studentService.findAll(50);

        assertThat(result).hasSize(1);
        verify(studentRepository).findAll(eq(PageRequest.of(0, 50)));
    }
}
