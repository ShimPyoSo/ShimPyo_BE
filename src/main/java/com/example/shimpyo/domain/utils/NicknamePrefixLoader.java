package com.example.shimpyo.domain.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Getter
@Component
public class NicknamePrefixLoader {
    private static List<String> prefixes = new ArrayList<>();

    @PostConstruct
    public void loadPrefixes() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        InputStream is = getClass().getResourceAsStream("/prefixes.json");
        JsonNode root = mapper.readTree(is);
        prefixes = new ArrayList<>();
        root.get("prefixes").forEach(node -> prefixes.add(node.asText()));
    }

    public static String generateNickNames() {
        Random random = new Random();
        random.setSeed(System.currentTimeMillis());
        int randNum = random.nextInt(10000);
        StringBuilder sb = new StringBuilder(Integer.toString(randNum));
        while (sb.length() < 6) {
            sb.insert(0, 0);
        }
        sb.insert(0, prefixes.get(random.nextInt(115)));
        return sb.toString();
    }
}
