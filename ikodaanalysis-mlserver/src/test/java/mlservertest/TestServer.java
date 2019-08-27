package mlservertest;




import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;


@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class TestServer
{




        @Autowired
        private MockMvc mockMvc;

        @Test
        public void homePage() throws Exception
        {
            // N.B. jsoup can be useful for asserting HTML content
            mockMvc.perform(get("/plaintext"))
                    .andExpect(content().string(containsString(" ")));
         }

}
