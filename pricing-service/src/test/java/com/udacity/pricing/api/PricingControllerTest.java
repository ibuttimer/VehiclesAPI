package com.udacity.pricing.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.udacity.pricing.config.Config;
import com.udacity.pricing.AbstractTest;
import com.udacity.pricing.domain.price.Price;
import com.udacity.pricing.domain.price.PriceRepository;
import org.assertj.core.util.Streams;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import static com.udacity.pricing.config.Config.*;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


class PricingControllerTest extends AbstractTest {

    @Autowired
    PricingController pricingController;

    @Autowired
    PriceRepository priceRepository;

    public List<Price> repositoryEntries;
    private static final int NUM_PRICES = 3;
    public static List<Price> PRICES = LongStream.range(0, NUM_PRICES)
            .mapToObj(i -> Price.of("Currency"+i, BigDecimal.valueOf((i+1)*1_000_000), i + 1))
            .collect(Collectors.toList());

    @BeforeAll
    public static void beforeAll() {
        AbstractTest.beforeAll();
    }

    @BeforeEach
    public void beforeEach() {
        clearRepository(priceRepository);
        repositoryEntries = Streams.stream(
                    priceRepository.saveAll(PRICES))
                .collect(Collectors.toList());
        assertEquals(PRICES.size(), repositoryEntries.size());
        for (int i = 0; i < PRICES.size(); i++) {
            Price base = PRICES.get(i);
            Price repo = repositoryEntries.get(i);
            assertEquals(base.getCurrency(), repo.getCurrency());
            assertEquals(base.getPrice(), repo.getPrice());
            assertEquals(base.getVehicleId(), repo.getVehicleId());
        }
    }

    @DisplayName("Get price by vehicle id")
    @Test
    public void getPrice() {
        ObjectMapper objectMapper = new ObjectMapper();

        repositoryEntries.forEach(p -> {
            try {
                String expected = objectMapper.writeValueAsString(p);

                mockMvc.perform(get(
                            getPriceByVehicleIdUrl(p.getVehicleId())))
                        .andExpect(status().isOk())
                        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                        .andExpect(content().json(expected));
            } catch (Exception e) {
                e.printStackTrace();
                fail();
            }
        });
    }

    @Transactional
    @DisplayName("Delete price by vehicle id")
    @Test
    public void deletePrice() {
        int initialSize = repositoryEntries.size();
        Price delete = repositoryEntries.remove( initialSize / 2);

        try {
            mockMvc.perform(delete(
                        getDeleteByVehicleIdUrl(delete.getVehicleId())))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(content().string("1"));
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }

        List<Price> remaining = Streams.stream(priceRepository.findAll())
                .collect(Collectors.toList());
        assertEquals(remaining.size(), initialSize - 1);
        assertFalse(remaining.contains(delete));
    }

    @DisplayName("Set new price by vehicle id")
    @Test
    public void newPrice() {
        Price original = repositoryEntries.get( repositoryEntries.size() / 2);
        AtomicReference<Price> update = new AtomicReference<>();
        ObjectMapper objectMapper = new ObjectMapper();
        String notExpected = null;
        try {
            notExpected = objectMapper.writeValueAsString(original);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            fail();
        }

        try {
            mockMvc.perform(get(
                        getNewPriceByVehicleIdUrl(original.getVehicleId())))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(content().string(doesNotContainString(notExpected)))
                    .andDo(mvcResult -> {
                        update.set(objectMapper.readValue(
                                mvcResult.getResponse().getContentAsString(), Price.class));
                    });
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }

        Price updated = update.get();
        if (updated != null) {
            // verify only vehicle id is the same
            assertEquals(original.getVehicleId(), updated.getVehicleId());
            assertNotEquals(original.getId(), updated.getId());
            assertNotEquals(original.getCurrency(), updated.getCurrency());
            assertNotEquals(original.getPrice(), updated.getPrice());

            // verify same number of price entities
            assertEquals(repositoryEntries.size(), priceRepository.count());
        } else {
            fail("Update did not occur");
        }
    }

    /**
     * Get the url for the price of the specified vehicle
     * @param vehicleId - id of vehicle
     * @return
     */
    private static String getUrlWithVehicleIdUrl(String url, Long vehicleId) {
        return Config.getUrl(url, Map.of("vehicleId", vehicleId));
    }

    /**
     * Get the url for the price of the specified vehicle
     * @param vehicleId - id of vehicle
     * @return
     */
    public static String getPriceByVehicleIdUrl(Long vehicleId) {
        return getUrlWithVehicleIdUrl(PRICING_GET_BY_VEHICLEID_URL, vehicleId);
    }

    /**
     * Get the url for the price of the specified vehicle
     * @param vehicleId - id of vehicle
     * @return
     */
    public static String getNewPriceByVehicleIdUrl(Long vehicleId) {
        return getUrlWithVehicleIdUrl(PRICING_GET_RANDOM_URL, vehicleId) + "&currency=NewCurrency";
    }

    /**
     * Get the url to delete the price of the specified vehicle
     * @param vehicleId - id of vehicle
     * @return
     */
    public static String getDeleteByVehicleIdUrl(Long vehicleId) {
        return getUrlWithVehicleIdUrl(PRICING_DELETE_BY_VEHICLEID_URL, vehicleId);
    }

    public static Matcher<String> doesNotContainString(String s) {
        return CoreMatchers.not(containsString(s));
    }

}