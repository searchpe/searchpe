package io.searchpe;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class Batchtest {

    public static void main(String[] args) throws IOException {
        File file = new File("/home/admin/git/searchpe/Working directory/unzipFolder/padron_reducido_ruc.txt");

        Map<Integer, Integer> contador = new HashMap<>();

        boolean exists = file.exists();
        if (exists) {
            Stream<String> lines = Files.lines(file.toPath(), Charset.forName("ISO-8859-1"));

            lines.forEach(line -> {
                if (line.contains("||")) {
                    System.out.println(line);
                }

                int count = line.length() - line.replace("|", "").length();
//                Integer length = line.split("\\|").length;
                if (!contador.containsKey(count)) {
                    contador.put(count, 0);
                }

                Integer current = contador.get(count);
                contador.put(count, current + 1);

            });
        }

        for (Map.Entry<Integer, Integer> entry : contador.entrySet()) {
            System.out.println(entry.getKey() + ":" + entry.getValue());
        }
    }
}
