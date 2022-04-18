package com.sample;

import org.jsoup.Jsoup;

import java.io.FileWriter;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

public class Main {

    private static final String GOOGLE_SEARCH_LINK = "https://www.google.com/search?hl=en&gl=us&q=";

    private static String reasonCompany;
    private static ArrayList<String> currentOwners = new ArrayList<>();

    private static String readFile(String path) throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, StandardCharsets.UTF_8);
    }

    private static long filterDesiredCompanies(String from, String to, String done) throws IOException {
        String content = readFile(from);
        String[] lines = content.split(System.getProperty("line.separator"));
        FileWriter writer;
        FileWriter writer2;
        long startTime = System.currentTimeMillis();
        for (String company : lines) {
            reasonCompany = "";
            writer = new FileWriter(to, true);
            writer2 = new FileWriter(done, true);
            if (isDesiredCompany(company)) {
                if (!reasonCompany.isEmpty())
                    writer.append(">> ").append(company).append(" -> ").append(reasonCompany).append("\n");
                else writer.append(company).append("\n");
            }
            writer2.append(company).append("\n");
            writer.close();
            writer2.close();
            reasonCompany = "";
            currentOwners.clear();
        }
        return System.currentTimeMillis() - startTime;
    }

    private static boolean isDesiredCompany(String company) {
        if(currentOwners.isEmpty()) System.out.println("Researching \"" + company + "\" ...");
        else System.out.println(">> Researching \"" + company + "\" ...");
        String doc = "";
        try {
            doc = Jsoup.connect(GOOGLE_SEARCH_LINK + URLEncoder.encode(company, "UTF-8")).get().toString();
            // Encoder is for spaces and other special characters to go into the URL
        } catch (Exception e) {
            System.err.println("\nRequest rate limit is reached, remove checked companies from companies, then change VPN or wait and try again");
            System.exit(1);
        }

        String[] keywords = {"UK", "British", "United Kingdom", "Britain"};
        if (Arrays.stream(keywords).anyMatch(doc::contains)) return true; // returns true if doc contains any element of "keywords"
        // case sensitive
        //if (Arrays.stream(Arrays.asList(keywords).stream().map(String::toUpperCase).toArray(String[]::new)).anyMatch(doc.toUpperCase()::contains))
        // case insensitive

        String[] words = {"Parent organization", "Owner"};
        String[] htmlWords = new String[words.length];
        for (int i = 0; i < words.length; i++) htmlWords[i] = ">" + words[i];

        if (Arrays.stream(htmlWords).anyMatch(doc::contains)) {
            ArrayList<String> parentOrganizations = parentOrganizationsOf(doc, words);
            for (String s : currentOwners) parentOrganizations.remove(s);
            currentOwners.addAll(parentOrganizations);
            for (String organization : parentOrganizations) {
                if (isDesiredCompany(organization)) {
                    if (reasonCompany.equals("")) reasonCompany = organization;
                    return true;
                }
            }
        }
        return false;
    }

    private static ArrayList<String> parentOrganizationsOf(String htmlFile, String[] words) {
        String[] lines = htmlFile.split(System.getProperty("line.separator"));
        HashSet<String> tempSet = new HashSet<>();
        for (String word : words) {
            String line = "";
            for (String l : lines) if (l.contains(">" + word)) line = l; // finding HTML line containing desired word
            String tempString1 = Jsoup.parse(line).body().text();
            String[] result = new String[0];

            if (!line.isEmpty() && (tempString1.contains(word + ": ") || tempString1.contains(word + "s: "))) { // s: for plural of a word
                result = tempString1.substring(tempString1.indexOf(':') + 2).replaceAll("\\(.*?\\)", "")
                        .replaceAll("\\(.*?", "").split(","); // removing anything that contains brackets
            }
            for (int i = 0; i < result.length; i++) result[i] = result[i].trim();
            tempSet.addAll(new ArrayList<>(Arrays.asList(result)));
        }
        return new ArrayList<>(tempSet);
    }

    public static void main(String[] args) throws IOException {
        String from = "companies.txt";
        String to = "filtered_companies.txt";
        String done = "checked_companies.txt";
        System.out.println("Required time: " + filterDesiredCompanies(from, to, done) / 1000. + "s");
    }
}
