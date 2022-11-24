package org.example;

import org.example.task_one.TaskOne;
import org.example.task_two.TaskTwo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Stream;

public class Main {
    public static void main(String[] args) throws IOException {
        taskOne();
        taskTwo(true); // input is JSON, output - XML
        taskTwo(false); // input is XML, output - JSON
    }

    private static void taskOne() throws IOException {
        // Get the absolute path of the input file in resources folder
        URL res = Main.class.getClassLoader().getResource("info_1.xml");
        assert res != null; // Suggested by IDE
        File input = null;
        try {
            input = Paths.get(res.toURI()).toFile();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        assert input != null; // Suggested by IDE
        Date start = new Date();
        File output = TaskOne.concatenateNameSurnameXML(input.getAbsolutePath());
        Date end = new Date();
        System.out.println("Time taken: " + (end.getTime() - start.getTime()) + "ms"); // time taken

        // Check the output file
        try(BufferedReader reader = new BufferedReader(new FileReader(output))) {
            reader.lines().forEach(System.out::println);
        }
    }

    private static void taskTwo(boolean isJSON) throws IOException {

        String format = "xml";
        if(isJSON) format = "json";

        List<File> fileList = new ArrayList<>();
        // Get all the JSON/XML files from the moving_violations directory
        try (Stream<Path> paths = Files.walk(Paths.get(
                "src/main/resources/moving_violations/" + format))) {
            String finalFormat = format;
            paths.filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith("." + finalFormat))
                    .forEach(path -> fileList.add(new File(String.valueOf(path))));
        }
        Date start = new Date();
        File output = TaskTwo.overallViolationsStats(fileList);
        Date end = new Date();
        System.out.println("Time taken: " + (end.getTime() - start.getTime()) + "ms"); // time taken
        // Check the output file
        try(BufferedReader reader = new BufferedReader(new FileReader(output))) {
            reader.lines().forEach(System.out::println);
        }
    }
}