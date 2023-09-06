import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.bytebuddy.implementation.bind.annotation.Morph;
import org.junit.jupiter.api.Test;
import searchengine.model.Page;
import searchengine.utils.Morphology;
import searchengine.utils.SiteParser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
public class MorphologyTest {



    @Test
    void analyseTest() throws IOException {


        String path = "src/test/resources/text.txt";
        Page page = Page.pageBuilder().code(200).content(Files.readString(Path.of(path))).build();

        val map = Morphology.analyse(page);
        int actual = map.get("остров");

        assertEquals(4, actual);
    }
}
