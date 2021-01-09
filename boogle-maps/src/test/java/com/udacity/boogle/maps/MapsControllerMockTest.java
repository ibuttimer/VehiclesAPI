package com.udacity.boogle.maps;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.udacity.boogle.service.AddressService;
import com.udacity.boogle.service.VehicleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.StringUtils;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;

import static com.udacity.boogle.config.Config.MAPS_GET_URL;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MapsController.class)
public class MapsControllerMockTest {

    private static final long CAR_ID = 1L;
    private static final double LAT_0 = 0.0;
    private static final double LON_0 = 0.0;

    @Autowired
    MockMvc mvc;

    @MockBean
    AddressService addressService;

    @MockBean
    VehicleService vehicleService;

    @BeforeEach
    void setUp() {
    }

    @DisplayName("Get address for new car")
    @Test
    public void createAddress() throws Exception {

        Address address = new Address("777 Brockton Avenue", "Abington", "MA", "2351");

        ObjectMapper objectMapper = new ObjectMapper();
        String expected = objectMapper.writeValueAsString(address);

        Mockito.when(addressService.getAddress(LAT_0, LON_0, CAR_ID)).thenReturn(address);


        mvc.perform(
            get(MapsControllerTest.getMapUri(MAPS_GET_URL, LAT_0, LON_0, CAR_ID)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(mvcResult -> {
                    String content = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
                    assertValidAddress(
                            objectMapper.readValue(content, Address.class));
                })
                .andExpect(content().json(expected));
    }

    @DisplayName("Delete address allocation")
    @Test
    public void deleteAddress() throws Exception {

        long result = 1L;
        Mockito.when(addressService.deleteAddress(CAR_ID)).thenReturn(result);

        mvc.perform(
            delete(MapsControllerTest.getDeleteUri(MAPS_GET_URL, CAR_ID)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string(Long.toString(result)));
    }


    private void assertValidAddress(Address address) {
        assertFalse(StringUtils.isBlank(address.getAddress()));
        assertFalse(StringUtils.isBlank(address.getCity()));
        assertFalse(StringUtils.isBlank(address.getState()));
        assertFalse(StringUtils.isBlank(address.getZip()));
    }

}