package com.app.service;

import com.app.dto.FuelHistoryDto;
import com.app.model.FuelHistory;
import com.app.repository.FuelHistoryRepo;
import com.app.repository.UserRepo;
import com.app.utils.FuelHistoryDtoToFuelHistory;
import com.app.utils.FuelHistoryToFuelHistoryDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class FuelHistoryService {

    private final FuelHistoryRepo fuelHistoryRepo;
    private final FuelHistoryDtoToFuelHistory fuelHistoryDtoConverter;
    private final FuelHistoryToFuelHistoryDto fuelHistoryConverter;

    public FuelHistoryService(FuelHistoryRepo fuelHistoryRepo, FuelHistoryDtoToFuelHistory fuelHistoryDtoConverter, FuelHistoryToFuelHistoryDto fuelHistoryConverter, UserRepo userRepo) {
        this.fuelHistoryRepo = fuelHistoryRepo;
        this.fuelHistoryDtoConverter = fuelHistoryDtoConverter;
        this.fuelHistoryConverter = fuelHistoryConverter;
    }

    private Pageable getPage(Integer page) {
        int pageNo = page < 1 ? 0 : page - 1;
        return PageRequest.of(pageNo, 10);
    }

    public Page<FuelHistoryDto> getAllHistories(Long vehicleId, Integer page) {
        Page<FuelHistory> fuelHistoryPage = fuelHistoryRepo.findByVehicleId(vehicleId, getPage(page));
        if (fuelHistoryPage.isEmpty()) return Page.empty();
        return fuelHistoryPage.map(fuelHistoryConverter::convert);
    }

    public FuelHistoryDto addFuelHistory(FuelHistoryDto fuelHistoryDto) {
        fuelHistoryDto.setCreatedAt(Instant.now());
        FuelHistory fuelHistory = fuelHistoryDtoConverter.convert(fuelHistoryDto);
        if (fuelHistory == null) return null;
        fuelHistoryRepo.save(fuelHistory);
        return fuelHistoryConverter.convert(fuelHistory);
    }
}
