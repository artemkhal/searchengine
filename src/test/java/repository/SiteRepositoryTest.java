package repository;

import lombok.val;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureDataJpa;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import searchengine.model.Site;
import searchengine.model.Status;
import searchengine.repo.SiteRepository;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(classes = SiteRepository.class)
//@RunWith(SpringRunner.class)
//@SpringBootTest
//@DataJpaTest
//@JdbcTest
@TestPropertySource(locations = "classpath:src/test/resources/applicationTest.yaml")
@Import(SiteRepository.class)
@AutoConfigureDataJpa
public class SiteRepositoryTest {

    @Autowired
    private SiteRepository repository;
    private Site site;

    private final String TEST_NAME = "test";
    private final String TEST_URL = "https://test.test/";
    private final Status TEST_STATUS = Status.FAILED;


    {
        val testSite = Site.siteBuilder()
                .name(TEST_NAME)
                .url(TEST_URL)
                .status(TEST_STATUS)
                .statusTime(LocalDateTime.now())
                .build();
        site = repository.save(testSite);
    }

    @Test
    public void findByNameTest(){
        val actual = repository.findByName(TEST_NAME);
        assertEquals(site, actual);
    }

    @Test
    public void findByUrlTest(){
        val actual = repository.findByUrl(TEST_URL);
        assertEquals(site, actual);
    }

    @org.junit.Test
    public void findByStatusTest(){
        val actual = repository.findByStatus(TEST_STATUS);
        assertEquals(site, actual);
    }
}
