package com.app.controller;

import com.app.dto.RouteDto;
import com.app.dto.VehicleDto;
import com.app.dto.requests.RouteStatusRequest;
import com.app.model.RouteStatus;
import com.app.security.CustomUserDetailService;
import com.app.security.JWTGenerator;
import com.app.security.SecurityUtil;
import com.app.service.RouteService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = RouteController.class)
@AutoConfigureMockMvc(addFilters = false)
public class RouteControllerTests {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RouteService routeService;
    @MockBean
    private SecurityUtil securityUtil;
    @MockBean
    private JWTGenerator jwtGenerator;
    @MockBean
    private AuthenticationManager authenticationManager;
    @MockBean
    private CustomUserDetailService customUserDetailService;

    private Page<RouteDto> routesDto;
    private RouteDto routeDto;
    private VehicleDto vehicleDto;
    private RouteStatusRequest routeRequest;

    @BeforeEach
    void setUp() {
        vehicleDto = VehicleDto.builder().id(1L).licensePlate("1234").model("test_model")
                .manufacturer("test_manufacturer").productionYear(1999).fuelCapacity(new BigDecimal(2))
                .averageConsumption(new BigDecimal(3)).mileage(4).lastMaintenance(new Date())
                .createdAt(Instant.now()).userId(2L).build();
        routeDto = RouteDto.builder().vehicleDto(vehicleDto).id(1L).distance(new BigDecimal(200)).estimatedTime(30)
                .startTime(new Date()).endTime(new Date()).status(RouteStatus.ACTIVE).createdAt(Instant.now())
                .waypoints(new ArrayList<>()).userId(2L).build();
        routesDto = new PageImpl<>(List.of(routeDto));
        routeRequest = new RouteStatusRequest(1L, RouteStatus.FINISHED);
    }

    @Test
    void givenLoggedInUser_whenGetVehicleRoutes_thenReturnOk() throws Exception {
        given(securityUtil.getSessionUser()).willReturn("John");
        given(routeService.getVehicleRoutes(anyString(), anyString(), anyInt())).willReturn(routesDto);

        mockMvc.perform(get("/api/route")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("licensePlate", "1234"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.body.routeDtoList[0].distance").value("200"))
                .andExpect(jsonPath("$.body.routeDtoList[0].estimatedTime").value("30"))
                .andExpect(jsonPath("$.body.routeDtoList[0].userId").value("2"));
    }

    @Test
    void givenLoggedInUserAndInvalidRoute_whenGetVehicleRoutes_thenReturnNotFound() throws Exception {
        given(securityUtil.getSessionUser()).willReturn("John");
        given(routeService.getVehicleRoutes(anyString(), anyString(), anyInt())).willReturn(Page.empty());

        mockMvc.perform(get("/api/route")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("licensePlate", "9999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("No routes found"));
    }

    @Test
    void givenNonLoggedInUser_whenGetVehicleRoutes_thenReturnUnauthorized() throws Exception {
        given(securityUtil.getSessionUser()).willReturn(null);

        mockMvc.perform(get("/api/route")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Not logged in"));
    }

    @Test
    void givenLoggedInUser_whenSearchRoutes_thenReturnOk() throws Exception {
        given(securityUtil.getSessionUser()).willReturn("John");
        given(routeService.searchRoute(anyString(), anyInt(), anyMap())).willReturn(routesDto);

        mockMvc.perform(post("/api/route/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("licensePlate", "1234"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.body.routeDtoList[0].distance").value("200"))
                .andExpect(jsonPath("$.body.routeDtoList[0].estimatedTime").value("30"))
                .andExpect(jsonPath("$.body.routeDtoList[0].userId").value("2"));
    }

    @Test
    void givenLoggedInUserAndInvalidRoute_whenSearchRoutes_thenReturnNotFound() throws Exception {
        given(securityUtil.getSessionUser()).willReturn("John");
        given(routeService.searchRoute(anyString(), anyInt(), anyMap())).willReturn(Page.empty());

        mockMvc.perform(post("/api/route/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("licensePlate", "9999"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("No routes found"));
    }

    @Test
    void givenNonLoggedInUser_whenSearchRoutes_thenReturnUnauthorized() throws Exception {
        given(securityUtil.getSessionUser()).willReturn(null);

        mockMvc.perform(post("/api/route/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("licensePlate", "9999"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Not logged in"));
    }

    @Test
    void givenLoggedInUser_whenAddRoute_thenReturnCreated() throws Exception {
        given(securityUtil.getSessionUser()).willReturn("John");
        given(routeService.addRoute(anyString(), any(RouteDto.class))).willReturn(routeDto);

        mockMvc.perform(post("/api/route")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(routeDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Route added successfully"))
                .andExpect(jsonPath("$.body.distance").value(200))
                .andExpect(jsonPath("$.body.estimatedTime").value(30));
    }

    @Test
    void givenLoggedInUserAndInvalidRoute_whenAddRoute_thenReturnBadRequest() throws Exception {
        routeDto.setDistance(null);
        BindingResult bindingResult = mock(BindingResult.class);
        given(securityUtil.getSessionUser()).willReturn("John");
        given(bindingResult.hasErrors()).willReturn(true);
        given(bindingResult.getAllErrors()).willReturn(List.of(new ObjectError("distance", "Distance cannot be null")));

        mockMvc.perform(post("/api/route")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(routeDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Distance cannot be null"));
    }

    @Test
    void givenNonLoggedInUser_whenAddRoute_thenReturnUnauthorized() throws Exception {
        given(securityUtil.getSessionUser()).willReturn(null);

        mockMvc.perform(post("/api/route")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(routeDto)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Not logged in"));
    }

    @Test
    void givenLoggedInUser_whenDeleteRoute_thenReturnOk() throws Exception {
        given(securityUtil.getSessionUser()).willReturn("John");
        given(routeService.deleteRoute(anyString(), anyLong())).willReturn(true);

        mockMvc.perform(delete("/api/route")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Route deleted successfully"));
    }

    @Test
    void givenLoggedInUserAndInvalidRoute_whenDeleteRoute_thenReturnNotFound() throws Exception {
        given(securityUtil.getSessionUser()).willReturn("John");
        given(routeService.deleteRoute(anyString(), anyLong())).willReturn(false);

        mockMvc.perform(delete("/api/route")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("id", "2"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Route not found"));
    }

    @Test
    void givenNonLoggedInUser_whenDeleteRoute_thenReturnUnauthorized() throws Exception {
        given(securityUtil.getSessionUser()).willReturn(null);

        mockMvc.perform(delete("/api/route")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("id", "1"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Not logged in"));
    }

    @Test
    void givenLoggedInUser_whenEditRoute_thenReturnOk() throws Exception {
        given(securityUtil.getSessionUser()).willReturn("John");
        given(routeService.editRoute(anyString(), any(RouteStatusRequest.class))).willReturn(true);

        mockMvc.perform(put("/api/route")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(routeRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Successful route update"));
    }

    @Test
    void givenLoggedInUserAndInvalidRoute_whenEditRoute_thenReturnBadRequest() throws Exception {
        given(securityUtil.getSessionUser()).willReturn("John");
        given(routeService.editRoute(anyString(), any(RouteStatusRequest.class))).willReturn(false);

        mockMvc.perform(put("/api/route")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(routeRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Unsuccessful route update"));
    }

    @Test
    void givenLoggedInUserAndInvalidIdRoute_whenEditRoute_thenReturnBadRequest() throws Exception {
        given(securityUtil.getSessionUser()).willReturn("John");
        given(routeService.editRoute(anyString(), any(RouteStatusRequest.class))).willReturn(false);
        routeRequest.setId(null);

        mockMvc.perform(put("/api/route")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(routeRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Insert selected route id"));
    }

    @Test
    void givenLoggedInUserAndInvalidStatusRoute_whenEditRoute_thenReturnBadRequest() throws Exception {
        given(securityUtil.getSessionUser()).willReturn("John");
        given(routeService.editRoute(anyString(), any(RouteStatusRequest.class))).willReturn(false);
        routeRequest.setStatus(null);

        mockMvc.perform(put("/api/route")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(routeRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Insert new route status"));
    }

    @Test
    void givenNonLoggedInUser_whenEditRoute_thenReturnUnauthorized() throws Exception {
        given(securityUtil.getSessionUser()).willReturn(null);

        mockMvc.perform(put("/api/route")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(routeRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Not logged in"));
    }

}
