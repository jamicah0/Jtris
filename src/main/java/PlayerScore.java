package main.java;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.ArrayList;

public class PlayerScore {
    String name;
    int score;

    public PlayerScore(String name, int score) {
        this.name = name;
        this.score = score;
    }

    public String getName() {
        return name;
    }

    public int getScore() {
        return score;
    }

    public String getScoreAsString() {
        return String.format("%08d" , score);
    }

    public static void saveScore(ArrayList<PlayerScore> scores) {
        BufferedWriter writer;

        try {
            String jarPath = Paths.get(PlayerScore.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParent().toString();
            writer = new BufferedWriter(new FileWriter(jarPath + "\\scores.txt"));
            for (PlayerScore score : scores) {

                writer.write(score.getName() + ";" + score.getScore() + "\n");
            }

            writer.close();

        } catch (IOException e) {
            System.out.printf("Error saving scores: %s\n", e.getMessage());
        } catch (URISyntaxException e) {
            System.out.println("Error getting path to scores.txt");
        }
    }

    public static ArrayList<PlayerScore> readScores() {
        ArrayList<PlayerScore> scores = new ArrayList<>();
        BufferedReader reader = null;

        try {
            String jarPath = Paths.get(PlayerScore.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParent().toString();
            reader = new BufferedReader(new FileReader(jarPath + "\\scores.txt"));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isEmpty()) {
                    continue;
                }
                String[] parts = line.split(";");
                scores.add(new PlayerScore(parts[0], Integer.parseInt(parts[1])));
            }
            reader.close();
        } catch (IOException ignored) {
        } catch (URISyntaxException e) {
            System.out.println("Error getting path to scores.txt");
        }

        return scores;
    }

}
