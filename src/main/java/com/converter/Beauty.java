package com.converter;

import org.hibernate.jdbc.util.BasicFormatterImpl;

public class Beauty {
    public static String toDO(String unBeauty){
        String sql = (new BasicFormatterImpl()).format(unBeauty);
        String[] lines = sql.split("\n");

        StringBuilder formattedText = new StringBuilder();
        for (String line : lines) {
            if (line.trim().equals("")) {
                continue;
            }
            if (line.startsWith("    ")) {
                formattedText.append(line.substring(4));
            } else {
                formattedText.append(line);
            }
            formattedText.append('\n');
        }
        return formattedText.toString();
    }
}
