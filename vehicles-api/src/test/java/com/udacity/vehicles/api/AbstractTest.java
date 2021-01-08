package com.udacity.vehicles.api;

import com.google.common.collect.Lists;
import com.udacity.vehicles.VehiclesApiApplication;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
                classes = VehiclesApiApplication.class,
                properties = {
                    "job.autorun.enabled=false" // don't run preload of addresses into database
                })
@AutoConfigureMockMvc
public abstract class AbstractTest implements ITestResource {

    @Autowired
    protected MockMvc mockMvc;

    @LocalServerPort
    private Integer port;

    protected ResourceBundle bundle;

    public AbstractTest() {
        bundle = getResourceBundle("test");
    }

    @Override
    public ResourceBundle getResourceBundle() {
        return bundle;
    }


    @Disabled
    @Test
    void printSystemProperties() {
        //remove @Disabled to see System properties
        System.getProperties().forEach((key, value) -> System.out.println(key+" - "+value));
    }

    @Disabled
    @Test
    void printEnvironmentProperties() {
        // Remove @Disabled to see environment properties
        System.getenv().forEach((key, value) -> System.out.println(key+" - "+value));
    }

    /**
     * Load a resource file
     * @param filename - resource file name
     * @return file contents as byte array
     */
    protected byte[] loadFileBytes(String filename) {
        ClassLoader classLoader = getClass().getClassLoader();
        byte[] fileBytes = new byte[0];

        try (InputStream inputStream = classLoader.getResourceAsStream(filename)) {
            fileBytes = Objects.requireNonNull(inputStream).readAllBytes();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fileBytes;
    }

    /**
     * Load a resource file
     * @param inputStream - file input stream
     * @return file contents as byte array
     */
    protected byte[] loadFileBytes(InputStream inputStream) {
        byte[] fileBytes = new byte[0];

        try {
            if (inputStream != null) {
                fileBytes = inputStream.readAllBytes();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fileBytes;
    }

    /**
     * Load a resource file
     * @param filename - resource file name
     * @return file contents as list of strings
     */
    protected List<String> loadFileLines(String filename) {
        ClassLoader classLoader = getClass().getClassLoader();
        List<String> lines = Lists.newArrayList();

        try (InputStream inputStream = classLoader.getResourceAsStream(filename)) {
            lines = loadFileLines(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return lines;
    }

    /**
     * Load a resource file
     * @param inputStream - file input stream
     * @return file contents as list of strings
     */
    protected List<String> loadFileLines(InputStream inputStream) {
        List<String> lines = Lists.newArrayList();

        try (InputStreamReader streamReader = new InputStreamReader(Objects.requireNonNull(inputStream), StandardCharsets.UTF_8);
             BufferedReader reader = new BufferedReader(streamReader)) {

            lines = reader.lines().collect(Collectors.toList());

        } catch (IOException e) {
            e.printStackTrace();
        }
        return lines;
    }


    @BeforeAll
    public static void beforeAll() {
        // no-op
    }

    @AfterAll
    public static void afterAll() {
        // no-op
    }

    protected String getUrl(String path,
                            String query,
                            String fragment) {
        URI uri = null;
        try {
            uri = new URI("http", null, "localhost", port, path, query, fragment);
        } catch (URISyntaxException e) {
            e.printStackTrace();
            fail();
        }
        return uri.toString();
    }

    protected String getUrl(String path) {
        return getUrl(path, null, null);
    }

    /**
     * Pause test for specified time
     * @param timeout – the length of time to sleep in milliseconds
     * @throws InterruptedException – see {@link Thread()#pause(int)}
     */
    protected void pause(int timeout) throws InterruptedException {
        Thread.sleep(timeout);
    }

    /**
     * Pause test for specified time
     * @param timeout – resource key for the length of time to sleep in milliseconds
     * @throws InterruptedException – see {@link Thread()#pause(int)}
     */
    protected void pause(String timeout) throws InterruptedException {
        pause(timeout, 1);
    }

    /**
     * Pause test for specified time
     * @param timeout – resource key for the length of time to sleep in milliseconds
     * @param multiplier - number of multiples of timeout to wait
     * @throws InterruptedException – see {@link Thread()#pause(int)}
     */
    protected void pause(String timeout, int multiplier) throws InterruptedException {
        pause(getResourceInt(timeout) * multiplier);
    }

    /**
     * Pause test
     * @throws InterruptedException – see {@link Thread()#pause(int)}
     */
    protected void pause() throws InterruptedException {
        pause("defaultEoTTimeout");
    }

    protected void clearRepository(CrudRepository<?, ? extends Number> repository) {
        repository.deleteAll();
        assertEquals(0, repository.count(),() -> "Repository not empty: " + repository.getClass().getSimpleName());
        assertFalse(repository.findAll().iterator().hasNext(), () -> "Repository not empty: " + repository.getClass().getSimpleName());
    }

    protected void clearRepository(JpaRepository<?, ? extends Number> repository) {
        repository.deleteAll();
        assertEquals(0, repository.count(),() -> "Repository not empty: " + repository.getClass().getSimpleName());
        assertFalse(repository.findAll().iterator().hasNext(), () -> "Repository not empty: " + repository.getClass().getSimpleName());
    }

}
