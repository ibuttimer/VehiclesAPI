package com.udacity.boogle.maps;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.udacity.boogle.config.Config;
import com.udacity.boogle.service.AddressService;
import com.udacity.boogle.service.VehicleService;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.udacity.boogle.config.Config.MAPS_GET_URL;
import static com.udacity.boogle.maps.MapsController.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class MapsControllerTest extends AbstractTest {

    private static final long CAR_ID = 1L;
    private static final double LAT_0 = 0.0;
    private static final double LON_0 = 0.0;
    private static final double LAT_1 = 1.0;
    private static final double LON_1 = 1.0;

    @Autowired
    AddressService addressService;

    @Autowired
    AddressRecordRepository addressRecordRepository;

    @Autowired
    VehicleService vehicleService;

    @Autowired
    VehicleRepository vehicleRepository;

    @BeforeAll
    public static void beforeAll() {
        AbstractTest.beforeAll();
    }

    static final int NUM_ADDRESSES = 5;
    static final String ADDRESS_LINE_TEMPLATE = "%d Test St.";
    static final String CITY_TEMPLATE = "TestCity%d";
    static final String STATE_TEMPLATE = "State%d";
    static final String ZIP_TEMPLATE = "%d-%d";

    static final List<Address> ADDRESSES = IntStream.range(0, NUM_ADDRESSES)
            .mapToObj(i -> new Address(
                String.format(ADDRESS_LINE_TEMPLATE, i),
                String.format(CITY_TEMPLATE, i),
                String.format(STATE_TEMPLATE, i),
                String.format(ZIP_TEMPLATE, i, i)
        )).collect(Collectors.toList());

    List<AddressRecord> repositoryAddressRecords;
    List<String> addressJson;

    ObjectMapper objectMapper;

    @BeforeEach
    public void beforeEach() {
        objectMapper = new ObjectMapper();
        addressJson = Lists.newArrayList();

        clearRepository(addressRecordRepository);
        clearRepository(vehicleRepository);
        repositoryAddressRecords = addressService.saveAllAddresses(ADDRESSES);
        assertEquals(ADDRESSES.size(), repositoryAddressRecords.size());
        IntStream.range(0, ADDRESSES.size())
                .forEach(i -> {
                    Address address = repositoryAddressRecords.get(i).getAddress();
                    assertEquals(address, ADDRESSES.get(i));
                    try {
                        addressJson.add(objectMapper.writeValueAsString(address));
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                        fail();
                    }
                });
    }

    @DisplayName("Get address for new car")
    @Test
    public void createAddress() throws Exception {

        getAddressAndVerify(MAPS_GET_URL, LAT_0, LON_0, CAR_ID);
    }

    Address getAddressAndVerify(String baseUrl, double lat, double lon, long id) throws Exception {
        AtomicReference<Address> response = new AtomicReference<>();
        mockMvc.perform(
            get(getMapUri(baseUrl, lat, lon, id)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(mvcResult -> {
                    // expecting random address response, check its valid and in the list of possible addresses
                    String content = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
                    Address address = assertValidAddress(
                            objectMapper.readValue(content, Address.class));
                    assertEquals(1, addressJson.stream()
                            .filter(content::equals)
                            .count());

                    response.set(address);
                });
        return response.get();
    }

    @DisplayName("Address changes only when location changes")
    @Test
    public void changeAddress() throws Exception {

        Address initial = getAddressAndVerify(MAPS_GET_URL, LAT_0, LON_0, CAR_ID);
        Address noChange = getAddressAndVerify(MAPS_GET_URL, LAT_0, LON_0, CAR_ID);
        assertEquals(initial, noChange);
        Address moved = getAddressAndVerify(MAPS_GET_URL, LAT_1, LON_1, CAR_ID);
        assertNotEquals(initial, moved);
    }

    @DisplayName("Address not returned if already assigned")
    @Test
    public void noAddressDuplication() throws Exception {

        assignAllAndVerify();
    }

    void assignAllAndVerify() throws Exception {

        List<Address> assigned = Lists.newArrayList();

        // verify all assigned
        IntStream.range((int) CAR_ID, repositoryAddressRecords.size() + (int) CAR_ID)
                .forEach(i -> {
                    try {
                        Address address = getAddressAndVerify(MAPS_GET_URL, i, i, i);
                        assertFalse(assigned.contains(address));
                        assigned.add(address);
                    } catch (Exception e) {
                        e.printStackTrace();
                        fail();
                    }
                });

        // verify no assignment once all allocated
        int outOfRange = repositoryAddressRecords.size() + 10;
        mockMvc.perform(
            get(getMapUri(MAPS_GET_URL, outOfRange, outOfRange, outOfRange)))
                .andExpect(status().isNotFound());

    }

    @DisplayName("Delete address allocation")
    @Test
    public void deleteAddress() throws Exception {

        getAddressAndVerify(MAPS_GET_URL, LAT_0, LON_0, CAR_ID);

        mockMvc.perform(
            delete(getDeleteUri(MAPS_GET_URL, CAR_ID)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string("1"));

        mockMvc.perform(
            delete(getDeleteUri(MAPS_GET_URL, CAR_ID)))
                .andExpect(status().isNotFound());

        assignAllAndVerify();
    }


    private Address assertValidAddress(Address address) {
        assertFalse(StringUtils.isBlank(address.getAddress()));
        assertFalse(StringUtils.isBlank(address.getCity()));
        assertFalse(StringUtils.isBlank(address.getState()));
        assertFalse(StringUtils.isBlank(address.getZip()));
        return address;
    }

    public static URI getMapUri(String baseUrl, double lat, double lon, long id) {
        URI uri = null;
        try {
            uri = new URI(
                    Config.getUrl(baseUrl, Map.of(LATITUDE_PARAM, lat, LONGITUDE_PARAM, lon, VEHICLE_ID_PARAM, id)));
        } catch (URISyntaxException e) {
            e.printStackTrace();
            fail();
        }
        return uri;
    }

    public static URI getDeleteUri(String baseUrl, long id) {
        URI uri = null;
        try {
            uri = new URI(
                    Config.getUrl(baseUrl, Map.of(VEHICLE_ID_PARAM, id)));
        } catch (URISyntaxException e) {
            e.printStackTrace();
            fail();
        }
        return uri;
    }

}