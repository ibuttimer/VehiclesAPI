package com.udacity.pricing.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.udacity.pricing.domain.price.Price;
import com.udacity.pricing.domain.price.PriceRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static com.udacity.pricing.api.PricingControllerTest.getDeleteByVehicleIdUrl;
import static com.udacity.pricing.api.PricingControllerTest.getPriceByVehicleIdUrl;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(PricingController.class)
@AutoConfigureMockMvc
class PricingControllerMockTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    PricingController pricingController;

    @MockBean
    PriceRepository priceRepository;

    @DisplayName("Get price by vehicle id")
    @Test
    public void getPrice() throws Exception {

        Long vehicleId = 1L;
        Price price = Price.of("EUR", BigDecimal.valueOf(12345), vehicleId);

        ObjectMapper objectMapper = new ObjectMapper();
        String expected = objectMapper.writeValueAsString(price);

        when(pricingController.getByVehicleId(vehicleId)).thenReturn(price);

        mockMvc.perform(get(
                    getPriceByVehicleIdUrl(vehicleId)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(expected));

        verify(pricingController, times(1)).getByVehicleId(vehicleId);
    }

    @DisplayName("Delete price by vehicle id")
    @Test
    public void deletePrice() throws Exception {

        Long vehicleId = 1L;

        when(pricingController.deleteByVehicleId(vehicleId)).thenReturn(1L);

        mockMvc.perform(delete(
                    getDeleteByVehicleIdUrl(vehicleId)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string("1"));


        verify(pricingController, times(1)).deleteByVehicleId(vehicleId);

    }
}