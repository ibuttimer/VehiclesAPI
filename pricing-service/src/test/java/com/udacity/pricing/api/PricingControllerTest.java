package com.udacity.pricing.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.udacity.pricing.AbstractTest;
import com.udacity.pricing.config.Config;
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
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import static com.udacity.pricing.config.Config.*;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.hateoas.MediaTypes.HAL_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


class PricingControllerTest extends AbstractTest {

    @Autowired
    PriceRepository priceRepository;

    public List<Price> repositoryEntries;
    private static final int NUM_PRICES = 3;
    public static List<Price> PRICES = LongStream.range(0, NUM_PRICES)
            .mapToObj(i -> Price.of("Currency"+i, getIndexPrice((int) i), i + 1))
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
                // TODO update to use jsonPath()
                mockMvc.perform(get(
                            getPriceByVehicleIdUrl(p.getVehicleId())))
                        .andExpect(status().isOk())
                        .andExpect(content().contentType(HAL_JSON))
                        .andExpect(mvcResult -> {
                            String body = mvcResult.getResponse().getContentAsString();
                            assertTrue(body.contains(p.getPrice().toString()));
                            assertTrue(body.contains(p.getCurrency()));
                            assertTrue(body.contains(p.getVehicleId().toString()));
                        });
            } catch (Exception e) {
                e.printStackTrace();
                fail();
            }
        });
    }

    @DisplayName("Post price")
    @Test
    public void postPrice() {
        ObjectMapper objectMapper = new ObjectMapper();
        Price newPrice = Price.of("€", getIndexPrice(NUM_PRICES + 1), (long) (NUM_PRICES + 4));

        try {
            mockMvc.perform(post(
                        PRICING_POST_URL).content(
                                objectMapper.writeValueAsString(newPrice)))
                    .andExpect(status().isCreated());
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @DisplayName("Get count")
    @Test
    public void getCount() {
        try {
            mockMvc.perform(get(
                        PRICING_COUNT_URL))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(HAL_JSON))
                    .andExpect(content().string(Integer.toString(NUM_PRICES)));
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @Transactional
    @DisplayName("Delete price by vehicle id")
    @Test
    public void deletePrice() {
        int initialSize = repositoryEntries.size();
        Price delete = repositoryEntries.remove( initialSize / 2);

        try {
            mockMvc.perform(get(
                        getDeleteByVehicleIdUrl(delete.getVehicleId())))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(HAL_JSON))
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


    public static BigDecimal getIndexPrice(int index) {
        return BigDecimal.valueOf((index+1)* 1_000_000L);
    }

    /**
     * Utility class for mapping server response
     */
    static class LinkedPrice extends Price {

        Links _links;

        public LinkedPrice() {
            super();
        }

        public Links get_links() {
            return _links;
        }

        public void set_links(Links _links) {
            this._links = _links;
        }

        static class Links {
            Link self;
            Link price;

            public Link getSelf() {
                return self;
            }

            public void setSelf(Link self) {
                this.self = self;
            }

            public Link getPrice() {
                return price;
            }

            public void setPrice(Link price) {
                this.price = price;
            }
        }
        static class Link {
            String href;

            public String getHref() {
                return href;
            }
            public void setHref(String href) {
                this.href = href;
            }
        }
    }
}