package com.example.demo.controller;

import com.example.demo.dto.AssetDTO;
import com.example.demo.entity.AssetType;
import com.example.demo.service.AssetService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AssetController.class)
class AssetControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AssetService assetService;

    // Helper method to create a dummy AssetDTO
    private AssetDTO createAssetDTO(Long id, String symbol, AssetType type) {
        AssetDTO dto = new AssetDTO();
        dto.setId(id);
        dto.setSymbol(symbol);
        dto.setName("Test Asset " + symbol);
        dto.setType(type);
        dto.setQuantity(new BigDecimal("10"));
        // Set required validation fields
        dto.setBuyPrice(new BigDecimal("100.00"));
        // Optional enriched fields
        dto.setCurrentPrice(new BigDecimal("105.00"));
        return dto;
    }

    // ---------------------------------------------------------------------
    @Nested
    @DisplayName("GET /api/assets")
    class GetAllAssetsTests {

        @Test
        @DisplayName("Given assets exist when getAllAssets then return list of assets")
        void givenAssetsExist_whenGetAllAssets_thenReturnListOfAssets() throws Exception {
            // GIVEN
            AssetDTO asset1 = createAssetDTO(1L, "AAPL", AssetType.STOCK);
            AssetDTO asset2 = createAssetDTO(2L, "BOND1", AssetType.BOND);
            List<AssetDTO> mockAssets = Arrays.asList(asset1, asset2);

            when(assetService.getAllAssets()).thenReturn(mockAssets);

            // WHEN & THEN
            mockMvc.perform(get("/api/assets")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.length()").value(2))
                    .andExpect(jsonPath("$.data[0].symbol").value("AAPL"));
        }

        @Test
        @DisplayName("Given no assets when getAllAssets then return empty list")
        void givenNoAssets_whenGetAllAssets_thenReturnEmptyList() throws Exception {
            // GIVEN
            when(assetService.getAllAssets()).thenReturn(Collections.emptyList());

            // WHEN & THEN
            mockMvc.perform(get("/api/assets")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").isEmpty());
        }
    }

    // ---------------------------------------------------------------------
    @Nested
    @DisplayName("GET /api/assets/{id}")
    class GetAssetByIdTests {

        @Test
        @DisplayName("Given existing asset ID when getAssetById then return asset")
        void givenExistingAssetId_whenGetAssetById_thenReturnAsset() throws Exception {
            // GIVEN
            Long assetId = 1L;
            AssetDTO asset = createAssetDTO(assetId, "GOOGL", AssetType.STOCK);

            when(assetService.getAssetById(assetId)).thenReturn(asset);

            // WHEN & THEN
            mockMvc.perform(get("/api/assets/{id}", assetId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.id").value(assetId))
                    .andExpect(jsonPath("$.data.symbol").value("GOOGL"));
        }
    }

    // ---------------------------------------------------------------------
    @Nested
    @DisplayName("GET /api/assets/type/{type}")
    class GetAssetsByTypeTests {

        @Test
        @DisplayName("Given valid asset type when getAssetsByType then return filtered assets")
        void givenValidAssetType_whenGetAssetsByType_thenReturnFilteredAssets() throws Exception {
            // GIVEN
            AssetType type = AssetType.STOCK;
            AssetDTO asset = createAssetDTO(1L, "TSLA", AssetType.STOCK);
            List<AssetDTO> mockAssets = Collections.singletonList(asset);

            when(assetService.getAssetsByType(type)).thenReturn(mockAssets);

            // WHEN & THEN
            mockMvc.perform(get("/api/assets/type/{type}", type)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data[0].type").value(type.toString()));
        }
    }

    // ---------------------------------------------------------------------
    @Nested
    @DisplayName("GET /api/assets/search")
    class SearchAssetsTests {

        @Test
        @DisplayName("Given search query when searchAssets then return matching assets")
        void givenSearchQuery_whenSearchAssets_thenReturnMatchingAssets() throws Exception {
            // GIVEN
            String query = "Tech";
            AssetDTO asset = createAssetDTO(1L, "MSFT", AssetType.STOCK);
            List<AssetDTO> mockAssets = Collections.singletonList(asset);

            when(assetService.searchAssets(query)).thenReturn(mockAssets);

            // WHEN & THEN
            mockMvc.perform(get("/api/assets/search")
                            .param("q", query)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data[0].symbol").value("MSFT"));
        }
    }

    // ---------------------------------------------------------------------
    @Nested
    @DisplayName("POST /api/assets")
    class CreateAssetTests {

        @Test
        @DisplayName("Given valid asset DTO when createAsset then return created asset")
        void givenValidAssetDTO_whenCreateAsset_thenReturnCreatedAsset() throws Exception {
            // GIVEN
            AssetDTO inputDto = createAssetDTO(null, "NVDA", AssetType.STOCK);
            AssetDTO createdDto = createAssetDTO(1L, "NVDA", AssetType.STOCK);

            when(assetService.createAsset(any(AssetDTO.class))).thenReturn(createdDto);

            // WHEN & THEN
            mockMvc.perform(post("/api/assets")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(inputDto)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.data.id").value(1L))
                    .andExpect(jsonPath("$.data.symbol").value("NVDA"));
        }

        @Test
        @DisplayName("Given invalid asset data when createAsset then return Bad Request")
        void givenInvalidAssetData_whenCreateAsset_thenReturnBadRequest() throws Exception {
            // GIVEN
            AssetDTO invalidDto = new AssetDTO(); // Empty DTO assuming @NotNull validations exist

            // WHEN & THEN
            mockMvc.perform(post("/api/assets")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidDto)))
                    // This assumes @Valid triggers 400. If no annotations exist on DTO fields, this might fail to 200/201.
                    // Adjust based on strict DTO validation rules.
                    .andExpect(status().isBadRequest());
        }
    }

    // ---------------------------------------------------------------------
    @Nested
    @DisplayName("PUT /api/assets/{id}")
    class UpdateAssetTests {

        @Test
        @DisplayName("Given valid update request when updateAsset then return updated asset")
        void givenValidUpdateRequest_whenUpdateAsset_thenReturnUpdatedAsset() throws Exception {
            // GIVEN
            Long assetId = 1L;
            AssetDTO updateDto = createAssetDTO(assetId, "AMZN", AssetType.STOCK);

            when(assetService.updateAsset(eq(assetId), any(AssetDTO.class))).thenReturn(updateDto);

            // WHEN & THEN
            mockMvc.perform(put("/api/assets/{id}", assetId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateDto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.symbol").value("AMZN"));
        }
    }

    // ---------------------------------------------------------------------
    @Nested
    @DisplayName("DELETE /api/assets/{id}")
    class DeleteAssetTests {

        @Test
        @DisplayName("Given existing asset ID when deleteAsset then return success")
        void givenExistingAssetId_whenDeleteAsset_thenReturnSuccess() throws Exception {
            // GIVEN
            Long assetId = 1L;
            doNothing().when(assetService).deleteAsset(assetId);

            // WHEN & THEN
            mockMvc.perform(delete("/api/assets/{id}", assetId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }
}

