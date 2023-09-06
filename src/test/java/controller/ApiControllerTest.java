package controller;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.client.match.JsonPathRequestMatchers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import searchengine.controllers.ApiController;
import searchengine.services.IndexingService;
import searchengine.services.StatisticsService;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;


//@RunWith(SpringRunner.class)
@WebMvcTest(ApiController.class)
public class ApiControllerTest {

//    @Autowired
    MockMvc mockMvc;

    @MockBean
    IndexingService indexingService;

    @MockBean
    StatisticsService statisticsService;



    @Test
    public void startIndexingTest() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.get("/api/startIndexing/")
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(content().string("Start indexing"));

    }



}
