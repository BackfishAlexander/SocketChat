import java.io.*;
import java.util.ArrayList;

/**
 * ChatFilter.java
 *
 * Loads censored words from a text file.
 * Censored words will be replaced by asterisks
 *
 * @author abackfis@purdue.edu, hmohite@purdue.edu
 * @version 04/26/2020
 */
public class ChatFilter {

    File badWordsFile;

    public ChatFilter(String badWordsFileName) {
        try {
            System.out.println("Loading filter from file: " + badWordsFileName);

        } catch (Exception e) {
            System.out.println("Unable to load file: " + badWordsFileName);
        }
        this.badWordsFile = new File(badWordsFileName);
        if (!badWordsFile.canRead())
            System.out.println("Unable to load file: " + badWordsFileName);
    }

    public ChatFilter() {
    }

    /*private class BadWord {
        String word;
        int location;

        public BadWord(String word, int location) {
            this.word = word;
            this.location = location;
        }
    }*/

    public String filter(String msg) {
        if (badWordsFile == null || !badWordsFile.canRead())
            return msg;
        //ArrayList<BadWord> censors = new ArrayList<>();
        try {
            badWordsFile.createNewFile();
            //System.out.println("Looking for file named: " + badWordsFile.getName());
            FileReader fr = new FileReader(this.badWordsFile);
            BufferedReader br = new BufferedReader(fr);

            String nextLine;
            while ((nextLine = br.readLine()) != null) {
                if (msg.contains(nextLine)) {
                    String hashString = "";
                    for (int i = 0; i < nextLine.length(); i++) {
                        hashString += "*";
                    }
                    //int k = msg.toLowerCase().indexOf(nextLine.toLowerCase());
                    //censors.add(new BadWord(nextLine.toLowerCase(), k));
                    msg = msg.replaceAll(nextLine, hashString);
                }
            }
            //This all destroyed the program, so we will just go with case-insensitivity
            /*StringBuilder censoredMessage = new StringBuilder();
            int[][] zones = new int[censors.size()][2];
            for (int i = 0; i < zones.length; i++) {
                BadWord word = censors.get(i);
                zones[i][0] = word.location;
                zones[i][1] = word.location + word.word.length();
            }
            for (int i = 0; i < msg.length(); i++) {
                for (int j = 0; j < zones.length; j++) {
                    int max = zones[j][1] + 1;
                    int min = zones[j][0];

                    if (i < min && i > max)
                        censoredMessage.append(msg.charAt(i));
                    else
                        censoredMessage.append("*");
                }
            }
            */
            fr.close();
            br.close();
            return msg;
            //return censoredMessage.toString();
        } catch (Exception e) {
            System.out.println("Error reading from filter file.");
        }
        return msg;
    }
}