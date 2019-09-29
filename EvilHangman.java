package hangman;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import java.util.Set;

public class EvilHangman {

    public static void main(String[] args) {

        // read in file and numbers
        File inFile = new File(args[0]);
        int wordLength = Integer.parseInt(args[1]);
        int numGuesses = Integer.parseInt(args[2]);

        // run the game
        EvilHangmanGame game = new EvilHangmanGame();
        try {
            game.startGame(inFile, wordLength);
            try {
                // prompt player for guess until numGuesses is 0
                for (int i = numGuesses; i > 0; i--) {
                    // print game info so far
                    System.out.println("You have " + i + " guesses left");
                    System.out.print("Used letters: ");
                    StringBuilder letters = new StringBuilder();
                    for (Character c : game.getGuessedLetters()) {
                        letters.append(c).append(" ");
                    }
                    letters.append("\n");
                    System.out.print(letters.toString());
                    System.out.println("Word: " + game.getPattern());

                    // get guess and run through makeGuess()
                    System.out.println("Enter guess: ");
                    Scanner input = new Scanner(System.in);
                    String sLetter = input.nextLine();
                    // check for valid char
                    while (sLetter.equals("") || (sLetter.equals(" ")) || (!sLetter.matches("^[a-zA-Z]*$")) || (sLetter.matches("[0-9.]*")) || (sLetter.length() > 1)) {
                        System.out.println("Invalid input");
                        System.out.println("Enter guess: ");
                        sLetter = input.nextLine();
                    }
                    sLetter = sLetter.toLowerCase();
                    char letter = sLetter.charAt(0);
                    // check that the letter hasn't already been guessed
                    while (game.getGuessedLetters().contains(letter)) {
                        System.out.println("You already used that letter");
                        System.out.println("Enter guess: ");
                        sLetter = input.nextLine();
                        while (sLetter.equals("") || (sLetter.equals(" ")) || (!sLetter.matches("^[a-zA-Z]*$")) || (sLetter.matches("[0-9.]*")) || (sLetter.length() > 1)) {
                            System.out.println("Invalid input");
                            System.out.println("Enter guess: ");
                            sLetter = input.nextLine();
                        }
                        sLetter = sLetter.toLowerCase();
                        letter = sLetter.charAt(0);
                    }
                    // if there is no letter print "Sorry, there are no <letter>'s
                    Set<String> word = game.makeGuess(letter);
                    int patternLetters = game.numLettersInCurrentPattern();
                    if ((word == null) || (patternLetters == 0)) {
                        System.out.println("Sorry, there are no " + letter + "'s");
                    }
                    else {
                        if (patternLetters > 1) {
                            System.out.println("Yes there are " + patternLetters + " " + letter + "'s");
                        }
                        else {
                            System.out.println("Yes there is " + patternLetters + " " + letter);
                        }
                        i++;
                    }

                    // if down to one word then print "You win!" and display the word
                    if (word.size() == 1) {
                        String finalWord = new String();
                        for (String w : word) {
                            finalWord = w;
                        }
                        System.out.println("\nYou win!");
                        System.out.print("The word was: ");
                        System.out.print(finalWord);
                        break;
                    }
                    // if the player used their last guess, print "You lose!" and display a word
                    if (i == 1) {
                        String finalWord = word.stream().findFirst().get();
                        System.out.println("\nYou lose!");
                        System.out.print("The word was: ");
                        System.out.print(finalWord);
                        break;
                    }

                    System.out.print("\n");
                }
            }
            catch (GuessAlreadyMadeException e) {
                System.out.println("You already used that letter");
                e.getCause();
            }
        }
        catch (EmptyDictionaryException e) {
            System.out.println("The dictionary provided is empty");
            e.getCause();
        }
        catch (IOException e) {
            System.out.println("Something was missing from input");
            e.getCause();
        }
    }
}
