package hangman;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.List;
import java.util.ArrayList;


public class EvilHangmanGame implements IEvilHangmanGame {
    private Set<String> gameDictionary = new HashSet<String>();
    private SortedSet<Character> guessedLetters = new TreeSet<Character>();
    private Set<String> patterns = new HashSet<String>();
    private String fullPattern;
    private String currentPattern;
    private int length = 0;

    @Override
    public void startGame(File dictionary, int wordLength) throws IOException, EmptyDictionaryException {
        // read file contents into the fullDictionary set
        Set<String> fullDictionary = new HashSet<String>();
        Scanner scanner = new Scanner(dictionary);
        while (scanner.hasNext()) {
            fullDictionary.add(scanner.next());
        }
        if (fullDictionary.isEmpty()) {
            throw new EmptyDictionaryException();
        }

        // reset previous values and a create subset of words that are wordLength long
        patterns.clear();
        guessedLetters.clear();
        gameDictionary.clear();
        length = wordLength;
        for (String word : fullDictionary) {
            if (word.length() == wordLength) {
                gameDictionary.add(word);
            }
        }
        if (gameDictionary.isEmpty()) {
            throw new EmptyDictionaryException();
        }

        // set pattern to all "-"s
        StringBuilder defaultPattern = new StringBuilder();
        for (int i = 0 ; i < wordLength; i++) {
            defaultPattern.append("-");
        }
        currentPattern = defaultPattern.toString();
        fullPattern = defaultPattern.toString();
    }

    @Override
    public Set<String> makeGuess(char guess) throws GuessAlreadyMadeException {
        // make sure guess is lowercase
        String guessString = String.valueOf(guess);
        guessString = guessString.toLowerCase();
        guess = guessString.charAt(0);
        int noLetterIndex = -1;
        Set<String> noLetterSet = new HashSet<String>();

        // add guess to guessedLetters
        if (!guessedLetters.contains(guess)) {
            guessedLetters.add(guess);
        }
        else {
            throw new GuessAlreadyMadeException();
        }

        // reset currentPattern
        StringBuilder defaultPattern = new StringBuilder();
        for (int i = 0 ; i < length; i++) {
            defaultPattern.append("-");
        }
        currentPattern = defaultPattern.toString();

        // partition gameDictionary into groups base on guessed letter patterns
        patterns.clear();
        findPatterns(guess);
        List<Set<String>> partitions = new ArrayList<Set<String>>(patterns.size());
        for (int i = 0; i < patterns.size(); i++) {
            partitions.add(new HashSet<String>());
        }
        for (String word : gameDictionary) {
            StringBuilder sb = new StringBuilder();
            // find pattern for each word
            for (int i = 0; i < word.length(); i++) {
                if (word.charAt(i) != guess) {
                    sb.append("-");
                }
                else {
                    sb.append(guess);
                }
            }
            int index = 0;
            for (String p : patterns) {
                if (p.equals(sb.toString())) {
                    partitions.get(index).add(word);
                }
                if (numLettersInPattern(p) == 0) {
                    noLetterIndex = index;
                    noLetterSet = partitions.get(noLetterIndex);
                }
                index++;
            }
        }

        // choose largest set in partitions
        int currentIndex = 0;
        int maxIndex = 0;
        int max = 0;
        ArrayList<Integer> maxIndices = new ArrayList<Integer>();
        for (Set part : partitions) {
            if (part.size() > max) {
                max = part.size();
                maxIndex = currentIndex;
                maxIndices.clear();
                maxIndices.add(currentIndex);
            }
            else if (part.size() == max) {
                maxIndices.add(currentIndex);
            }
            currentIndex++;
        }

        // replace gameDictionary with the new set if only one max
        if (maxIndices.size() == 1) {
           gameDictionary = partitions.get(maxIndex);
        }

        // else choose the partition pattern with no letters in it
        else if (noLetterIndex > -1) {
            gameDictionary = noLetterSet;
        }

        // if there isn't a noLetterSet then choose the one with fewest letters
        else {
            // cut partitions that aren't on the maxIndices
            for (int j = 0; j < partitions.size(); j++) {
                if (!maxIndices.contains(j)) {
                    partitions.remove(j);
                }
            }
            // cut partitions that aren't on the maxIndices
            int k = 0;
            for (String p : patterns) {
                if (!maxIndices.contains(k)) {
                    patterns.remove(p);
                }
                k++;
            }

            // find fewest letters
            int cIndex = 0;
            int mIndex = 0;
            int min = 0;
            ArrayList<Integer> minIndices = new ArrayList<Integer>();
            for (String p : patterns) {
                if (min == 0) {
                    min = numLettersInPattern(p);
                    mIndex = cIndex;
                    minIndices.add(cIndex);
                }
                else if (numLettersInPattern(p) < min) {
                    min = numLettersInPattern(p);
                    mIndex = cIndex;
                    minIndices.clear();
                    minIndices.add(cIndex);
                }
                else if (numLettersInPattern(p) == min) {
                    minIndices.add(cIndex);
                }
                cIndex++;
            }
            if (minIndices.size() == 1) {
                gameDictionary = partitions.get(mIndex);
            }

            // If still unresolved, choose one with the rightmost guessed letter
            else {
                // cut partitions that aren't on the minIndices
                for (int j = 0; j < partitions.size(); j++) {
                    if (!minIndices.contains(j)) {
                        partitions.remove(j);
                    }
                }
                // cut partitions that aren't on the minIndices
                int m = 0;
                for (String p : patterns) {
                    if (!minIndices.contains(m)) {
                        patterns.remove(p);
                    }
                    m++;
                }

                if (rightmostGuessedLetterPattern(guess).size() == 1) {
                    int rIndex = rightmostGuessedLetterPattern(guess).get(0);
                    gameDictionary = partitions.get(rIndex);
                }

                // If still more than one group, choose one with the next rightmost letter
                else {
                    ArrayList<Integer> rightIndices = rightmostGuessedLetterPattern(guess);
                    // cut partitions that aren't on the rightIndices
                    for (int j = 0; j < partitions.size(); j++) {
                        if (!minIndices.contains(j)) {
                            partitions.remove(j);
                        }
                    }
                    // cut partitions that aren't on the rightIndices
                    int n = 0;
                    for (String p : patterns) {
                        if (!minIndices.contains(n)) {
                            patterns.remove(p);
                        }
                        n++;
                    }

                    gameDictionary = finalRight(partitions);
                }
            }
        }

        // set currentPattern and fullPattern
        String layout = gameDictionary.stream().findFirst().get();
        StringBuilder newFullPattern = new StringBuilder(fullPattern);
        StringBuilder newCurrentPattern = new StringBuilder(currentPattern);
        for (int i = 0; i < layout.length(); i++) {
            if (layout.charAt(i) == guess) {
                newFullPattern.setCharAt(i, guess);
                newCurrentPattern.setCharAt(i, guess);
            }
        }
        fullPattern = newFullPattern.toString();
        currentPattern = newCurrentPattern.toString();

        return gameDictionary;
    }

    @Override
    public SortedSet<Character> getGuessedLetters() {
        return guessedLetters;
    }

    public String getPattern() { return fullPattern; }

    private void findPatterns (char guessLetter) {
        for(String word : gameDictionary) {
            StringBuilder sb = new StringBuilder();
            // find pattern for each word
            for (int i = 0; i < word.length(); i++) {
                if (word.charAt(i) != guessLetter) {
                    sb.append("-");
                }
                else {
                    sb.append(guessLetter);
                }
            }
            // add pattern to the patterns set
            patterns.add(sb.toString());
        }
    }

    int numLettersInCurrentPattern() {
        int numLetters = 0;
        for (int i = 0; i < currentPattern.length(); i++) {
            if (currentPattern.charAt(i) != '-') {
                numLetters++;
            }
        }
        return numLetters;
    }

    private int numLettersInPattern(String pattern) {
        int numLetters = 0;
        for (int i = 0; i < pattern.length(); i++) {
            if (pattern.charAt(i) != '-') {
                numLetters++;
            }
        }
        return numLetters;
    }

    private ArrayList<Integer> rightmostGuessedLetterPattern(char guess) {
        int currentIndex = 0;
        int maxIndex = 0;
        int max = 0;
        ArrayList<Integer> maxIndices = new ArrayList<Integer>();
        for (String p : patterns) {
            if (p.lastIndexOf(guess) > max) {
                max = p.lastIndexOf(guess);
                maxIndex = currentIndex;
                maxIndices.clear();
                maxIndices.add(currentIndex);
            }
            else if (p.lastIndexOf(guess) == max) {
                maxIndices.add(currentIndex);
            }
            currentIndex++;
        }

        return maxIndices;
    }

    private Set<String> finalRight(List<Set<String>> partitions) {
        Set<String> newPartition = new HashSet<String>();
        for (int i = (length - 1); i >= 0; i--) {
            int patternIndex = 0;
            ArrayList<Integer> letterIndices = new ArrayList<Integer>();
            for (String p : patterns) {
                if (p.charAt(i) != '-') {
                    letterIndices.add(patternIndex);
                }
                patternIndex++;
            }

            // check if there was only 1 letter
            if (letterIndices.size() == 1)  {
                int finalIndex = letterIndices.get(0);
                newPartition = partitions.get(finalIndex);
                return newPartition;
            }
            // check if there was more than 1 but less than patterns.size()
            else if ((letterIndices.size() > 1) && (letterIndices.size() < patterns.size())) {
                int index = 0;
                for (String p : patterns) {
                    if (!letterIndices.contains(index)) {
                        patterns.remove(p);
                    }
                    index++;
                }
            }
        }
        return null;
    }
}
