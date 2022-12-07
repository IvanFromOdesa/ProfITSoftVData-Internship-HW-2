package org.example.task_one;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Reading and writing the data without any external library.
 * Formatting of the input file perceived.
 * Using BufferedReader and BufferedWriter for buffering
 * and less memory usage.
 */
public class TaskOne {

    private static final Pattern patternSurname = Pattern.compile("surname\\s*=\\s*\"([^\\x00-\\x7F[\\w]]+\")\\s");
    private static final Pattern patternName = Pattern.compile("\\b(name\\s*=\\s*)(\"[^\\x00-\\x7F[\\w]]+)\"");
    public static File concatenateNameSurnameXML(String path) {

        File file = new File("output_1.xml");
        Matcher matcherSurname;
        StringBuilder sb = new StringBuilder();
        String s;

        try(BufferedReader reader = new BufferedReader(
                    new FileReader(path), 16 * 1024); // optimal size of buffer
            // BufferedReader's faster and more efficient than Scanner as we can set
            // the size of buffer and BufferedReader reads the data directly without parsing it
            // Taking advantages from both BufferedWriter (buffering, memory-efficient)
            // and PrintWriter (println()) - decorator pattern
            PrintWriter out = new PrintWriter(new BufferedWriter
                    (new FileWriter(file.getAbsolutePath())))) {

            while ((s = reader.readLine()) != null) {
                if(s.contains(">")) {
                    if(sb.length() != 0) {
                        sb.append(s);
                        if(s.matches("\\R*<\\/persons>")) { // if there are escape chars
                                                                // at the end of the file
                            out.println(sb);
                            break;
                        }
                        matcherSurname = patternSurname.matcher(sb);
                        if(!matcherSurname.find()) throw new IllegalStateException("Unexpected error");
                        replaceInitials(matcherSurname, out);
                        sb.setLength(0);
                    } else { // if it contains > and sb.length() == 0 -
                            // all attributes are within one line or the line
                            // is either <persons> or </persons>
                        matcherSurname = patternSurname.matcher(s);
                        if(!matcherSurname.find()) { // If the line does not contain "surname" attribute
                            out.println(s);
                            continue;
                        }
                        replaceInitials(matcherSurname, out);
                    }
                } else sb.append(s).append(System.lineSeparator()); // if the line does not contain
                                                                    // the closing >, append till it does
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;
    }

    private static void replaceInitials(Matcher matcherSurname, PrintWriter out) {
        String surname = matcherSurname.group(1);
        // Deleting the "surname" from the person
        String s = matcherSurname.replaceAll("");
        Matcher matcherName = patternName.matcher(s);
        if(!matcherName.find()) throw new IllegalStateException("Unexpected error");

        // Putting name and surname into the "name"
        out.println(matcherName.replaceAll(matcherName.group(1)
                + matcherName.group(2) + " " + surname));
    }
}
