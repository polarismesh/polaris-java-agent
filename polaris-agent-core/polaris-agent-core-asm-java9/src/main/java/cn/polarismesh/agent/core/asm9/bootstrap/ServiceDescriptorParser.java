package cn.polarismesh.agent.core.asm9.bootstrap;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ServiceDescriptorParser {


    public List<String> parse(InputStream inputStream) throws IOException {
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
        BufferedReader bufferedInputStream = new BufferedReader(inputStreamReader);
        Set<String> lineSet = new HashSet<>();
        String line;
        while((line = bufferedInputStream.readLine()) != null) {
            line = trim(line);
            if (line != null) {
                lineSet.add(line);
            }
        }
        return new ArrayList<>(lineSet);
    }


    private String trim(String line) {
        final int commentIndex = line.indexOf('#');
        if (commentIndex != -1) {
            line = line.substring(0, commentIndex);
        }
        line = line.trim();
        if (line.isEmpty()) {
            return null;
        }
        return line;
    }
}
