package com.app.controller;

import com.app.dto.VehicleDto;
import com.app.dto.requests.VehicleUpdateRequest;
import com.app.security.CustomUserDetailService;
import com.app.security.JWTGenerator;
import com.app.security.SecurityUtil;
import com.app.service.VehicleService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.data.domain.Page;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(controllers = VehicleController.class)
@AutoConfigureMockMvc(addFilters = false)
public class VehicleControllerTests {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private VehicleService vehicleService;
    @MockBean
    private SecurityUtil securityUtil;
    @MockBean
    private JWTGenerator jwtGenerator;
    @MockBean
    private AuthenticationManager authenticationManager;
    @MockBean
    private CustomUserDetailService customUserDetailService;

    private Page<VehicleDto> vehiclesDto;
    private VehicleDto vehicleDto;
    private VehicleUpdateRequest vehicleRequest;

    @BeforeEach
    void setUp() {
        vehicleDto = VehicleDto.builder().id(1L).licensePlate("1234").model("test_model")
                .manufacturer("test_manufacturer").productionYear(1999).fuelCapacity(new BigDecimal(2))
                .averageConsumption(new BigDecimal(3)).mileage(4).lastMaintenance(new Date())
                .createdAt(Instant.now()).userId(2L).build();
        vehiclesDto = new PageImpl<>(List.of(vehicleDto));
        vehicleRequest = new VehicleUpdateRequest(1L, new BigDecimal(5), 5);
    }

    @Test
    void givenLoggedInUser_whenGetAllVehicles_thenReturnOk() throws Exception {
        given(securityUtil.getSessionUser()).willReturn("John");
        given(vehicleService.getAllVehicles(anyString(), anyInt())).willReturn(vehiclesDto);

        mockMvc.perform(get("/api/vehicle")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.body.vehicleDtoList[0].licensePlate").value("1234"));
    }

    @Test
    void givenNonLoggedInUser_whenGetAllVehicles_thenReturnUnauthorized() throws Exception {
        given(securityUtil.getSessionUser()).willReturn(null);

        mockMvc.perform(get("/api/vehicle")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Not logged in"));
    }

    @Test
    void givenLoggedInUser_whenSearchVehicles_thenReturnOk() throws Exception {
        given(securityUtil.getSessionUser()).willReturn("John");
        given(vehicleService.searchVehicles(anyString(), anyInt(), anyMap())).willReturn(vehiclesDto);

        mockMvc.perform(post("/api/vehicle/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("licensePlate", "1234"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.body.vehicleDtoList[0].licensePlate").value("1234"));
    }

    @Test
    void givenNonLoggedInUser_whenSearchVehicles_thenReturnUnauthorized() throws Exception {
        given(securityUtil.getSessionUser()).willReturn(null);

        mockMvc.perform(post("/api/vehicle/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("licensePlate", "1234"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Not logged in"));
    }

    @Test
    void givenNonLoggedInUserWithoutVehicles_whenSearchVehicles_thenReturnUnauthorized() throws Exception {
        given(securityUtil.getSessionUser()).willReturn("John");
        given(vehicleService.searchVehicles(anyString(), anyInt(), anyMap())).willReturn(Page.empty());

        mockMvc.perform(post("/api/vehicle/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("licensePlate", "1234"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("No vehicles found"));
    }

    @Test
    void givenLoggedInUser_whenAddVehicle_thenReturnCreated() throws Exception {
        given(securityUtil.getSessionUser()).willReturn("John");
        given(vehicleService.addVehicle(anyString(), any(VehicleDto.class))).willReturn(vehicleDto);

        mockMvc.perform(post("/api/vehicle")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(vehicleDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.body.licensePlate").value("1234"));
    }

    @Test
    void givenNonLoggedInUser_whenAddVehicle_thenReturnUnAuthorized() throws Exception {
        given(securityUtil.getSessionUser()).willReturn(null);

        mockMvc.perform(post("/api/vehicle")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(vehicleDto)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Not logged in"));
    }

    @Test
    void givenLoggedInUserAndInvalidVehicle_whenAddVehicle_thenReturnBadRequest() throws Exception {
        vehicleDto.setLicensePlate(null);
        BindingResult bindingResult = mock(BindingResult.class);
        given(securityUtil.getSessionUser()).willReturn("John");
        given(bindingResult.hasErrors()).willReturn(true);
        given(bindingResult.getAllErrors()).willReturn(List.of(new ObjectError("licensePlate", "License plate is required")));
        given(vehicleService.addVehicle(anyString(), any(VehicleDto.class))).willReturn(vehicleDto);

        mockMvc.perform(post("/api/vehicle")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(vehicleDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("License plate is required"));
    }

    @Test
    void givenLoggedInUser_whenDeleteVehicle_thenReturnOk() throws Exception {
        given(securityUtil.getSessionUser()).willReturn("John");
        given(vehicleService.deleteVehicle(anyString(), anyString())).willReturn(true);

        mockMvc.perform(delete("/api/vehicle")
                        .param("licensePlate", vehicleDto.getLicensePlate()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Vehicle deleted successfully"));
    }

    @Test
    void givenLoggedInUserAndInvalidVehicle_whenDeleteVehicle_thenReturnNotFound() throws Exception {
        given(securityUtil.getSessionUser()).willReturn("John");
        given(vehicleService.deleteVehicle(anyString(), anyString())).willReturn(false);

        mockMvc.perform(delete("/api/vehicle")
                        .param("licensePlate", "9999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Vehicle not found"));
    }

    @Test
    void givenNonLoggedInUser_whenDeleteVehicle_thenReturnNotFound() throws Exception {
        given(securityUtil.getSessionUser()).willReturn(null);

        mockMvc.perform(delete("/api/vehicle")
                        .param("licensePlate", "9999"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Not logged in"));
    }

    @Test
    void givenLoggedInUser_whenEditVehicleAfterRoute_thenReturnOk() throws Exception {
        given(securityUtil.getSessionUser()).willReturn("John");
        given(vehicleService.editVehicleAfterRoute(anyString(), any(VehicleUpdateRequest.class))).willReturn(true);

        mockMvc.perform(put("/api/vehicle/route")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(vehicleRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Successfully updated vehicle"));
    }

    @Test
    void givenLoggedInUserAndInvalidVehicle_whenEditVehicleAfterRoute_thenReturnBadRequest() throws Exception {
        given(securityUtil.getSessionUser()).willReturn("John");
        given(vehicleService.editVehicleAfterRoute(anyString(), any(VehicleUpdateRequest.class))).willReturn(false);

        mockMvc.perform(put("/api/vehicle/route")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(vehicleRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Unsuccessful vehicle update"));
    }

    @Test
    void givenLoggedInUserAndInvalidAvgConsumptionVehicle_whenEditVehicleAfterRoute_thenReturnBadRequest() throws Exception {
        given(securityUtil.getSessionUser()).willReturn("John");
        given(vehicleService.editVehicleAfterRoute(anyString(), any(VehicleUpdateRequest.class))).willReturn(false);
        vehicleRequest.setAverageConsumption(null);

        mockMvc.perform(put("/api/vehicle/route")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(vehicleRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Insert average consumption"));
    }

    @Test
    void givenLoggedInUserAndInvalidIdVehicle_whenEditVehicleAfterRoute_thenReturnBadRequest() throws Exception {
        given(securityUtil.getSessionUser()).willReturn("John");
        given(vehicleService.editVehicleAfterRoute(anyString(), any(VehicleUpdateRequest.class))).willReturn(false);
        vehicleRequest.setId(null);

        mockMvc.perform(put("/api/vehicle/route")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(vehicleRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Insert selected vehicle id"));
    }

    @Test
    void givenLoggedInUserAndInvalidMileageVehicle_whenEditVehicleAfterRoute_thenReturnBadRequest() throws Exception {
        given(securityUtil.getSessionUser()).willReturn("John");
        given(vehicleService.editVehicleAfterRoute(anyString(), any(VehicleUpdateRequest.class))).willReturn(false);
        vehicleRequest.setMileage(null);

        mockMvc.perform(put("/api/vehicle/route")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(vehicleRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Insert new mileage"));
    }

    @Test
    void givenNonLoggedInUser_whenEditVehicleAfterRoute_thenReturnUnauthorized() throws Exception {
        given(securityUtil.getSessionUser()).willReturn(null);

        mockMvc.perform(put("/api/vehicle/route")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(vehicleRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Not logged in"));
    }

}
