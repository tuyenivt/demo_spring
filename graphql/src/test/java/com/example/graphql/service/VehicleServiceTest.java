package com.example.graphql.service;

import com.example.graphql.dto.input.CreateVehicleInput;
import com.example.graphql.dto.input.UpdateVehicleInput;
import com.example.graphql.dto.pagination.ConnectionInput;
import com.example.graphql.dto.pagination.PageInput;
import com.example.graphql.entity.Student;
import com.example.graphql.entity.Vehicle;
import com.example.graphql.enums.VehicleType;
import com.example.graphql.exception.ResourceNotFoundException;
import com.example.graphql.exception.ValidationException;
import com.example.graphql.repository.StudentRepository;
import com.example.graphql.repository.VehicleRepository;
import com.example.graphql.util.CursorUtils;
import com.example.graphql.validation.VehicleValidator;
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
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VehicleServiceTest {

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private VehicleValidator vehicleValidator;

    @InjectMocks
    private VehicleService vehicleService;

    private Vehicle testVehicle;
    private Student testStudent;
    private UUID testVehicleId;
    private UUID testStudentId;

    @BeforeEach
    void setUp() {
        testVehicleId = UUID.randomUUID();
        testStudentId = UUID.randomUUID();

        testStudent = Student.builder()
                .name("John Doe")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .build();
        testStudent.setId(testStudentId);

        testVehicle = Vehicle.builder()
                .type(VehicleType.CAR)
                .student(testStudent)
                .build();
        testVehicle.setId(testVehicleId);
        testVehicle.setCreatedAt(OffsetDateTime.now());
        testVehicle.setUpdatedAt(OffsetDateTime.now());
    }

    @Test
    void create_shouldMapInputToEntityCorrectly() {
        var input = new CreateVehicleInput(VehicleType.MOTORCYCLE, testStudentId);
        var savedVehicle = Vehicle.builder()
                .type(VehicleType.MOTORCYCLE)
                .student(testStudent)
                .build();
        savedVehicle.setId(UUID.randomUUID());

        when(studentRepository.findById(testStudentId)).thenReturn(Optional.of(testStudent));
        when(vehicleRepository.save(any(Vehicle.class))).thenReturn(savedVehicle);

        var result = vehicleService.create(input);

        assertThat(result).isNotNull();
        assertThat(result.getType()).isEqualTo(VehicleType.MOTORCYCLE);
        assertThat(result.getStudent()).isEqualTo(testStudent);

        verify(vehicleValidator).validateCreate(input);
        verify(studentRepository).findById(testStudentId);
        verify(vehicleRepository).save(any(Vehicle.class));
    }

    @Test
    void create_shouldCreateVehicleWithoutStudent() {
        var input = new CreateVehicleInput(VehicleType.BICYCLE, null);
        var savedVehicle = Vehicle.builder()
                .type(VehicleType.BICYCLE)
                .student(null)
                .build();
        savedVehicle.setId(UUID.randomUUID());

        when(vehicleRepository.save(any(Vehicle.class))).thenReturn(savedVehicle);

        var result = vehicleService.create(input);

        assertThat(result).isNotNull();
        assertThat(result.getType()).isEqualTo(VehicleType.BICYCLE);
        assertThat(result.getStudent()).isNull();

        verify(vehicleValidator).validateCreate(input);
        verify(studentRepository, never()).findById(any());
        verify(vehicleRepository).save(any(Vehicle.class));
    }

    @Test
    void create_shouldThrowResourceNotFoundExceptionForInvalidStudentId() {
        var invalidStudentId = UUID.randomUUID();
        var input = new CreateVehicleInput(VehicleType.CAR, invalidStudentId);

        when(studentRepository.findById(invalidStudentId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> vehicleService.create(input))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Student")
                .hasMessageContaining(invalidStudentId.toString());

        verify(vehicleValidator).validateCreate(input);
        verify(studentRepository).findById(invalidStudentId);
        verify(vehicleRepository, never()).save(any());
    }

    @Test
    void update_shouldThrowResourceNotFoundExceptionForMissingId() {
        var missingId = UUID.randomUUID();
        var input = new UpdateVehicleInput(missingId, VehicleType.TRUCK, null);

        when(vehicleRepository.findById(missingId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> vehicleService.update(input))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Vehicle")
                .hasMessageContaining(missingId.toString());

        verify(vehicleValidator).validateUpdate(input);
        verify(vehicleRepository).findById(missingId);
        verify(vehicleRepository, never()).save(any());
    }

    @Test
    void update_shouldOnlyUpdateProvidedFields() {
        var input = new UpdateVehicleInput(testVehicleId, VehicleType.TRUCK, null);

        when(vehicleRepository.findById(testVehicleId)).thenReturn(Optional.of(testVehicle));
        when(vehicleRepository.save(any(Vehicle.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = vehicleService.update(input);

        assertThat(result.getType()).isEqualTo(VehicleType.TRUCK);
        assertThat(result.getStudent()).isEqualTo(testStudent); // unchanged

        verify(vehicleValidator).validateUpdate(input);
        verify(vehicleRepository).save(any(Vehicle.class));
    }

    @Test
    void findPage_shouldReturnPagedResults() {
        var pageInput = new PageInput(0, 10);
        var pageable = PageRequest.of(0, 10);
        var page = new PageImpl<>(List.of(testVehicle), pageable, 1);

        when(vehicleRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class), any(Pageable.class)))
                .thenReturn(page);

        var result = vehicleService.findPage(pageInput, null, null);

        assertThat(result).isNotNull();
        assertThat(result.content()).hasSize(1);
        assertThat(result.pageInfo().totalElements()).isEqualTo(1);
        assertThat(result.pageInfo().hasNext()).isFalse();

        verify(vehicleRepository).findAll(any(org.springframework.data.jpa.domain.Specification.class), any(Pageable.class));
    }

    @Test
    void findConnection_shouldHandleEmptyResults() {
        var connectionInput = new ConnectionInput(10, null, null, null);

        when(vehicleRepository.count(any(org.springframework.data.jpa.domain.Specification.class)))
                .thenReturn(0L);

        var result = vehicleService.findConnection(connectionInput, null, null);

        assertThat(result).isNotNull();
        assertThat(result.edges()).isEmpty();
        assertThat(result.totalCount()).isEqualTo(0);
        assertThat(result.pageInfo().hasNextPage()).isFalse();
    }

    @Test
    void findConnection_shouldEncodeCursorsCorrectly() {
        var connectionInput = new ConnectionInput(10, null, null, null);
        var page = new PageImpl<>(List.of(testVehicle));

        when(vehicleRepository.count(any(org.springframework.data.jpa.domain.Specification.class)))
                .thenReturn(1L);
        when(vehicleRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class), any(Pageable.class)))
                .thenReturn(page);

        var result = vehicleService.findConnection(connectionInput, null, null);

        assertThat(result.edges()).hasSize(1);
        var edge = result.edges().get(0);
        assertThat(edge.cursor()).isNotNull();

        // Verify cursor can be decoded
        var decodedId = CursorUtils.decode(edge.cursor());
        assertThat(decodedId).isEqualTo(testVehicleId);
    }

    @Test
    void createAll_shouldValidateVehicleLimitPerStudent() {
        var input1 = new CreateVehicleInput(VehicleType.CAR, testStudentId);
        var input2 = new CreateVehicleInput(VehicleType.TRUCK, testStudentId);
        var inputs = List.of(input1, input2);

        when(vehicleRepository.countByStudentId(testStudentId)).thenReturn(4); // already has 4 vehicles

        assertThatThrownBy(() -> vehicleService.createAll(inputs))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("exceed vehicle limit");

        verify(vehicleValidator, times(2)).validateCreate(any());
        verify(vehicleRepository).countByStudentId(testStudentId);
        verify(vehicleRepository, never()).saveAll(any());
    }

    @Test
    void createAll_shouldAllowBulkCreateWhenWithinLimit() {
        var input1 = new CreateVehicleInput(VehicleType.CAR, testStudentId);
        var input2 = new CreateVehicleInput(VehicleType.BICYCLE, testStudentId);
        var inputs = List.of(input1, input2);

        var vehicle1 = Vehicle.builder().type(VehicleType.CAR).student(testStudent).build();
        var vehicle2 = Vehicle.builder().type(VehicleType.BICYCLE).student(testStudent).build();
        vehicle1.setId(UUID.randomUUID());
        vehicle2.setId(UUID.randomUUID());

        when(vehicleRepository.countByStudentId(testStudentId)).thenReturn(2); // has 2, adding 2 more = 4 total (< 5)
        when(studentRepository.findById(testStudentId)).thenReturn(Optional.of(testStudent));
        when(vehicleRepository.saveAll(any())).thenReturn(List.of(vehicle1, vehicle2));

        var result = vehicleService.createAll(inputs);

        assertThat(result).hasSize(2);

        verify(vehicleValidator, times(2)).validateCreate(any());
        verify(vehicleRepository).countByStudentId(testStudentId);
        verify(vehicleRepository).saveAll(any());
    }

    @Test
    void findAll_shouldUsePaginationToLimitResults() {
        when(vehicleRepository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(testVehicle)));

        var result = vehicleService.findAll(50);

        assertThat(result).hasSize(1);
        verify(vehicleRepository).findAll(eq(PageRequest.of(0, 50)));
    }
}
