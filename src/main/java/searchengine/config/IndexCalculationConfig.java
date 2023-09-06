package searchengine.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import searchengine.repo.IndexRepository;
import searchengine.repo.LemmaRepository;
import searchengine.utils.IndexManager;

@Configuration
public class IndexCalculationConfig {

    @Autowired
    private IndexRepository indexRepository;

    @Autowired
    private LemmaRepository lemmaRepository;

    @Bean
    public IndexManager getIndexCalculation(){
        return new IndexManager(indexRepository, lemmaRepository);
    }
}
