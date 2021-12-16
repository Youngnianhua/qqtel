package com.nianhua;

import com.nianhua.model.QqTel;
import org.apache.ibatis.exceptions.PersistenceException;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DuplicateKeyException;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.nianhua.util.StringUtil.subString;

@SpringBootTest
class QqtelApplicationTests {

    @Test
    void contextLoads() throws IOException {
        Document gbk = Jsoup.parse(new URL("https://chaqbang.com/").openStream(), "GBK", "https://chaqbang.com/");
        System.out.println(gbk);
    }

}
