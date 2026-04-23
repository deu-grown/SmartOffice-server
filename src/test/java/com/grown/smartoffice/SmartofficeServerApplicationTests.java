package com.grown.smartoffice;

import com.grown.smartoffice.support.AbstractContainerTest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class SmartofficeServerApplicationTests extends AbstractContainerTest {

    @Test
    void contextLoads() {
    }
}
