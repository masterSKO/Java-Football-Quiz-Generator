package com.ok.vs;

import java.io.*;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

class Question {
    private String questionText;
    private String[] options;
    private int correctAnswer;
    private int difficultyLevel;

    public Question(String questionText, String[] options, int correctAnswer, int difficultyLevel) {
        this.questionText = questionText;
        this.options = options;
        this.correctAnswer = correctAnswer;
        this.difficultyLevel = difficultyLevel;
    }

    public String getQuestionText() {
        return questionText;
    }

    public String[] getOptions() {
        return options;
    }

    public int getCorrectAnswer() {
        return correctAnswer;
    }

    public int getDifficultyLevel() {
        return difficultyLevel;
    }

    @Override
    public String toString() {
        return questionText + "|" + String.join("|", options) + "|" + correctAnswer + "|" + difficultyLevel;
    }
}

class Lifeline {
    private boolean audiencePollUsed = false;
    private boolean fiftyFiftyUsed = false;
    private boolean skipQuestionUsed = false;

    public boolean isAudiencePollAvailable() {
        return !audiencePollUsed;
    }

    public boolean isFiftyFiftyAvailable() {
        return !fiftyFiftyUsed;
    }

    public boolean isSkipQuestionAvailable() {
        return !skipQuestionUsed;
    }

    public int[] useAudiencePoll(Question question) {
        if (audiencePollUsed) return null;
        audiencePollUsed = true;
        
        int correct = question.getCorrectAnswer();
        int[] percentages = new int[4];
        int remaining = 100;
        
        percentages[correct] = ThreadLocalRandom.current().nextInt(40, 71);
        remaining -= percentages[correct];
        
        for (int i = 0; i < 4; i++) {
            if (i == correct) continue;
            if (remaining <= 0) {
                percentages[i] = 0;
                continue;
            }
            
            if (i == 3) {
                percentages[i] = remaining;
            } else {
                percentages[i] = ThreadLocalRandom.current().nextInt(0, remaining + 1);
                remaining -= percentages[i];
            }
        }
        
        return percentages;
    }

    public int[] useFiftyFifty(Question question) {
        if (fiftyFiftyUsed) return null;
        fiftyFiftyUsed = true;
        
        int correct = question.getCorrectAnswer();
        int toRemove1, toRemove2;
        
        do {
            toRemove1 = ThreadLocalRandom.current().nextInt(0, 4);
        } while (toRemove1 == correct);
        
        do {
            toRemove2 = ThreadLocalRandom.current().nextInt(0, 4);
        } while (toRemove2 == correct || toRemove2 == toRemove1);
        
        return new int[]{toRemove1, toRemove2};
    }

    public boolean useSkipQuestion() {
        if (skipQuestionUsed) return false;
        skipQuestionUsed = true;
        return true;
    }
}

class Quiz {
    private List<Question> questions;
    private int currentQuestionIndex;
    private int score;
    private Lifeline lifeline;
    private boolean gameOver;
    private int questionsRemaining;

    public Quiz(List<Question> questions) {
        this.questions = questions;
        this.currentQuestionIndex = 0;
        this.score = 0;
        this.lifeline = new Lifeline();
        this.gameOver = false;
        this.questionsRemaining = questions.size();
    }

    public Question getCurrentQuestion() {
        if (currentQuestionIndex < questions.size()) {
            return questions.get(currentQuestionIndex);
        }
        return null;
    }

    public boolean checkAnswer(int answer) {
        Question current = getCurrentQuestion();
        if (current != null && answer == current.getCorrectAnswer()) {
            score += current.getDifficultyLevel() * 10;
            currentQuestionIndex++;
            questionsRemaining--;
            return true;
        } else {
            gameOver = true;
            return false;
        }
    }

    public void skipQuestion() {
        currentQuestionIndex++;
        questionsRemaining--;
    }

    public boolean isQuizOver() {
        return gameOver || currentQuestionIndex >= questions.size();
    }

    public int getScore() {
        return score;
    }

    public Lifeline getLifeline() {
        return lifeline;
    }

    public int getTotalQuestions() {
        return questions.size();
    }

    public int getCurrentQuestionNumber() {
        return currentQuestionIndex + 1;
    }

    public int getQuestionsRemaining() {
        return questionsRemaining;
    }

    public boolean isGameOver() {
        return gameOver;
    }
}

class QuizManager {
    private static final String QUESTIONS_FILE = ".vscode/vs/football_questions.txt";
    
    public static List<Question> loadQuestions() throws IOException {
        List<Question> questions = new ArrayList<>();
        File file = new File(QUESTIONS_FILE);
        
        if (!file.exists()) {
            createSampleQuestionsFile();
        }
        
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length >= 6) {
                    String questionText = parts[0];
                    String[] options = Arrays.copyOfRange(parts, 1, 5);
                    int correctAnswer = Integer.parseInt(parts[5]);
                    int difficultyLevel = Integer.parseInt(parts[6]);
                    questions.add(new Question(questionText, options, correctAnswer, difficultyLevel));
                }
            }
        }
        
        // Sort questions by difficulty level
        questions.sort(Comparator.comparingInt(Question::getDifficultyLevel));
        return questions;
    }
    
    private static void createSampleQuestionsFile() throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(QUESTIONS_FILE))) {
            // Level 1 questions
            writer.println("Which country won the 2018 FIFA World Cup?|France|Croatia|Belgium|England|0|1");
            writer.println("Which player has won the most Ballon d'Or awards?|Lionel Messi|Cristiano Ronaldo|Michel Platini|Johan Cruyff|0|1");
            writer.println("Which club has won the most Champions League titles?|Real Madrid|AC Milan|Bayern Munich|Liverpool|0|1");
            
            // Level 2 questions
            writer.println("In which year did Barcelona win their first Champions League?|1992|2006|2009|2011|0|2");
            writer.println("Who is the all-time top scorer for the Brazilian national team?|Pele|Ronaldo|Neymar|Romario|2|2");
            writer.println("Which country hosted the 2014 FIFA World Cup?|Brazil|Argentina|Germany|South Africa|0|2");
            
            // Level 3 questions
            writer.println("Who was the top scorer of the 2006 FIFA World Cup?|Miroslav Klose|Thierry Henry|Ronaldo|Hernan Crespo|0|3");
            writer.println("Which English club is known as 'The Red Devils'?|Manchester United|Liverpool|Arsenal|Chelsea|0|3");
            writer.println("Who scored the 'Hand of God' goal?|Diego Maradona|Lionel Messi|Pele|Zinedine Zidane|0|3");
            
            // Level 4 questions
            writer.println("Which player holds the record for most goals in a single Champions League season?|Cristiano Ronaldo|Lionel Messi|Robert Lewandowski|Ruud van Nistelrooy|0|4");
            writer.println("Which country won the first ever FIFA World Cup in 1930?|Uruguay|Argentina|Brazil|Italy|0|4");
            writer.println("Who is the only player to have won the World Cup, Champions League, Ballon d'Or and the Golden Boot?|Ronaldo|Zinedine Zidane|Lionel Messi|Cristiano Ronaldo|0|4");
            
            // Level 5 questions
            writer.println("Which player has made the most appearances in the Premier League?|Gareth Barry|Ryan Giggs|Frank Lampard|James Milner|0|5");
            writer.println("Who was the manager of Arsenal's 'Invincibles' team?|Arsene Wenger|Alex Ferguson|Jose Mourinho|Carlo Ancelotti|0|5");
            writer.println("Which player scored the fastest hat-trick in Premier League history?|Sadio Mane|Robbie Fowler|Sergio Aguero|Harry Kane|0|5");
        }
    }
}

public class FootballQuiz {
    private static Scanner scanner = new Scanner(System.in);
    
    public static void main(String[] args) {
        try {
            System.out.println("Welcome to the Shivendra's Football com.ok.vs.Quiz!");
            System.out.println("Rules:");
            System.out.println("- 5 difficulty levels with 3 questions each");
            System.out.println("- Get 1 question wrong and the game is over");
            System.out.println("- You have 3 lifelines (each can be used once):");
            System.out.println("  1. Audience Poll");
            System.out.println("  2. 50:50");
            System.out.println("  3. Skip com.ok.vs.Question");
            
            List<Question> questions = QuizManager.loadQuestions();
            Quiz quiz = new Quiz(questions);
            
            while (!quiz.isQuizOver()) {
                displayQuestion(quiz);
                
                System.out.print("\nYour choice (1-4) or lifeline (5-Audience, 6-50:50, 7-Skip): ");
                int choice = getValidInput(1, 7);
                
                if (choice >= 1 && choice <= 4) {
                    boolean isCorrect = quiz.checkAnswer(choice - 1);
                    if (isCorrect) {
                        System.out.println("\n✅ Correct! Well done!");
                    } else {
                        Question current = quiz.getCurrentQuestion();
                        System.out.println("\n❌ Wrong! The correct answer was: " + 
                            (current.getCorrectAnswer() + 1) + " " +
                            current.getOptions()[current.getCorrectAnswer()]);
                        System.out.println("Game Over!");
                        break;
                    }
                    System.out.println("Current Score: " + quiz.getScore());
                    System.out.println("Press enter to continue...");
                    scanner.nextLine();
                } else {
                    handleLifeline(choice, quiz);
                }
            }
            
            displayFinalResults(quiz);
        } catch (IOException e) {
            System.out.println("Error loading questions: " + e.getMessage());
        }
    }
    
    private static void displayQuestion(Quiz quiz) {
        Question current = quiz.getCurrentQuestion();
        System.out.println("\n--------------------------------------------------");
        System.out.println("com.ok.vs.Question " + quiz.getCurrentQuestionNumber() + " of " + quiz.getTotalQuestions());
        System.out.println("Questions remaining to win: " + quiz.getQuestionsRemaining());
        System.out.println("Current Score: " + quiz.getScore());
        System.out.println("--------------------------------------------------");
        System.out.println(current.getQuestionText());
        
        String[] options = current.getOptions();
        for (int i = 0; i < options.length; i++) {
            System.out.println((i + 1) + " " + options[i]);
        }
        
        displayAvailableLifelines(quiz.getLifeline());
    }
    
    private static void displayAvailableLifelines(Lifeline lifeline) {
        System.out.println("\nAvailable Lifelines:");
        if (lifeline.isAudiencePollAvailable()) {
            System.out.println("5. Audience Poll");
        }
        if (lifeline.isFiftyFiftyAvailable()) {
            System.out.println("6. 50:50");
        }
        if (lifeline.isSkipQuestionAvailable()) {
            System.out.println("7. Skip com.ok.vs.Question");
        }
    }
    
    private static void handleLifeline(int choice, Quiz quiz) {
        Lifeline lifeline = quiz.getLifeline();
        Question current = quiz.getCurrentQuestion();
        
        switch (choice) {
            case 5: // Audience Poll
                if (lifeline.isAudiencePollAvailable()) {
                    int[] percentages = lifeline.useAudiencePoll(current);
                    System.out.println("\nAudience Poll Results:");
                    for (int i = 0; i < percentages.length; i++) {
                        System.out.println((i + 1) + " " + percentages[i] + "%");
                    }
                } else {
                    System.out.println("You've already used the Audience Poll lifeline!");
                }
                break;
                
            case 6: // 50:50
                if (lifeline.isFiftyFiftyAvailable()) {
                    int[] toRemove = lifeline.useFiftyFifty(current);
                    System.out.println("\n50:50 - Removing two wrong answers:");
                    String[] options = current.getOptions();
                    for (int i = 0; i < options.length; i++) {
                        if (i != toRemove[0] && i != toRemove[1]) {
                            System.out.println((i + 1) + " " + options[i]);
                        } else {
                            System.out.println((i + 1) + ". [removed]");
                        }
                    }
                } else {
                    System.out.println("You've already used the 50:50 lifeline!");
                }
                break;
                
            case 7: // Skip com.ok.vs.Question
                if (lifeline.isSkipQuestionAvailable()) {
                    lifeline.useSkipQuestion();
                    quiz.skipQuestion();
                    System.out.println("\ncom.ok.vs.Question skipped! Moving to the next one.");
                } else {
                    System.out.println("You've already used the Skip com.ok.vs.Question lifeline!");
                }
                break;
        }
        
        if (choice != 7) { // If not skip question, show question again
            System.out.println("\nPress enter to continue with the same question...");
            scanner.nextLine();
        }
    }
    
    private static int getValidInput(int min, int max) {
        while (true) {
            try {
                int input = scanner.nextInt();
                scanner.nextLine(); // consume newline
                if (input >= min && input <= max) {
                    return input;
                }
                System.out.print("Invalid input. Please enter a number between " + min + " and " + max + ": ");
            } catch (InputMismatchException e) {
                System.out.print("Invalid input. Please enter a number: ");
                scanner.nextLine(); // clear invalid input
            }
        }
    }
    
    private static void displayFinalResults(Quiz quiz) {
        System.out.println("\n\n==================================================");
        System.out.println("                  QUIZ COMPLETE!                  ");
        System.out.println("==================================================");
        System.out.println("Your final score: " + quiz.getScore());
        
        if (quiz.isGameOver()) {
            System.out.println("You got a question wrong and the game ended.");
        } else {
            System.out.println("Congratulations! You answered all questions correctly!");
        }
        
        int maxPossibleScore = 0;
        for (int i = 1; i <= 5; i++) {
            maxPossibleScore += i * 10 * 3; // 3 questions per level, each worth level*10 points
        }
        
        double percentage = (double) quiz.getScore() / maxPossibleScore * 100;
        System.out.printf("You scored %.1f%% of the maximum possible score!\n", percentage);
        
        if (percentage >= 80) {
            System.out.println("🏆 Outstanding performance! You're a football expert!");
        } else if (percentage >= 60) {
            System.out.println("👍 Great job! You know your football!");
        } else if (percentage >= 40) {
            System.out.println("👌 Not bad! You have decent football knowledge.");
        } else {
            System.out.println("🤔 Maybe watch more football matches?");
        }
    }
}