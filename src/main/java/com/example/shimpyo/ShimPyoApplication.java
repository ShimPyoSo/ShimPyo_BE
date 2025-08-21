package com.example.shimpyo;

//import com.example.shimpyo.domain.search.indexing.AcTermIndexer;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@RequiredArgsConstructor
public class ShimPyoApplication {//implements CommandLineRunner {
//    private final AcTermIndexer acTermIndexer;

    public static void main(String[] args) {
        SpringApplication.run(ShimPyoApplication.class, args);
    }

//    @Override
//    public void run(String... args) {
//        acTermIndexer.reindexTourists(); // ▶ 이제 파라미터 없이 전체 색인 OK
//    }
}
