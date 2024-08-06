package org.example.uelearner;

/*
 *  Copyright (c) 2016 Joeri de Ruiter
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */


import de.learnlib.acex.analyzers.AcexAnalyzers;
import de.learnlib.algorithms.ttt.mealy.TTTLearnerMealy;
import de.learnlib.api.algorithm.LearningAlgorithm;
import de.learnlib.api.logging.LearnLogger;
import de.learnlib.api.oracle.EquivalenceOracle;
import de.learnlib.api.query.DefaultQuery;
import de.learnlib.counterexamples.AcexLocalSuffixFinder;
import de.learnlib.filter.cache.mealy.MealyCacheOracle;
import de.learnlib.filter.statistic.Counter;
import de.learnlib.filter.statistic.oracle.MealyCounterOracle;
import de.learnlib.oracle.equivalence.CheckingEQOracle;
import de.learnlib.oracle.equivalence.MealyRandomWordsEQOracle;
import de.learnlib.oracle.equivalence.MealyWMethodEQOracle;
import de.learnlib.oracle.equivalence.MealyWpMethodEQOracle;
import de.learnlib.oracle.membership.SULOracle;
import net.automatalib.words.Word;
import net.automatalib.words.WordBuilder;
import de.learnlib.util.statistics.SimpleProfiler;
import net.automatalib.automata.transducers.MealyMachine;
import net.automatalib.serialization.dot.GraphDOT;
import net.automatalib.words.impl.SimpleAlphabet;
import org.example.uelearner.ue.LTEUEConfig;
import org.example.uelearner.ue.UESUL;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Random;
import java.util.Scanner;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.SimpleFormatter;

import static org.example.uelearner.tgbot.NotificationBot.sendToTelegram;
import static org.example.uelearner.ue.UESUL.*;

import java.io.*;
import java.lang.reflect.Field;

import de.learnlib.algorithms.ttt.base.*;

import java.util.*;

import static java.lang.Thread.sleep;
import static org.example.uelearner.ue.UESUL.runProcess;

/**
 * @author Joeri de Ruiter (joeri@cs.ru.nl)
 */
public class Learner {
    static LearningConfig config;
    boolean combine_query = false;
    SimpleAlphabet<String> alphabet;
    StateLearnerSUL<String, String> sul;
    SULOracle<String, String> memOracle;
    LogOracle.MealyLogOracle<String, String> logMemOracle;

    MealyCounterOracle<String, String> statsMemOracle;
    MealyCacheOracle<String, String> cachedMemOracle;

    boolean LearnUE = false;
    MealyCounterOracle<String, String> statsCachedMemOracle;
    LearningAlgorithm<MealyMachine<?, String, ?, String>, String, Word<String>> learningAlgorithm;

    SULOracle<String, String> eqOracle;
    LogOracle.MealyLogOracle<String, String> logEqOracle;

    MealyCounterOracle<String, String> statsEqOracle;
    MealyCacheOracle<String, String> cachedEqOracle;
    MealyCounterOracle<String, String> statsCachedEqOracle;
    EquivalenceOracle<MealyMachine<?, String, ?, String>, String, Word<String>> equivalenceAlgorithm;

    EquivalenceOracle<MealyMachine<?, String, ?, String>, String, Word<String>> checkingCEAlgorithm;

    public List<List<String>> StoredCEs = new ArrayList<>();
    public List<Word<String>> WordCEs = new ArrayList<>();

    public Learner(LearningConfig config) throws Exception {
        this.config = config;

        loadCE();
        System.out.println("All CE loaded!");

        // Create output directory if it doesn't exist
        Path path = Paths.get(config.output_dir);
        if (Files.notExists(path)) {
            Files.createDirectories(path);
        }

        configureLogging(config.output_dir);

        LearnLogger log = LearnLogger.getLogger(Learner.class.getSimpleName());

        if (config.type == LearningConfig.TYPE_LTEUE) {
            log.info("Using UE SUL");
            sul = new UESUL(new LTEUEConfig(config));
            alphabet = ((UESUL) sul).getAlphabet();
            loadLearningAlgorithm(config.learning_algorithm, alphabet, sul);
            loadEquivalenceAlgorithm(config.eqtest, alphabet, sul);
        }
    }

    public void loadCE(){
        String file_name = "./CEStore/input";
        try (BufferedReader br = new BufferedReader(new FileReader(file_name))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.contains("INFO")) {
                    line = line.split("/")[0].split("\\[")[1].replaceAll("\\|", " ");
                    //System.out.println(line);
                    List<String> split_line = Arrays.asList(line.split("\\s+"));
                    StoredCEs.add(split_line);
                    WordCEs.add(generateCEWord(split_line));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Word<String> generateCEWord(List<String> StoredCEs) {

        int length = StoredCEs.size();
        final WordBuilder<String> result = new WordBuilder<>(length);

        for (int j = 0; j < length; ++j) {
            String sym = StoredCEs.get(j);
            result.append(sym);
        }

        return (Word<String>) result.toWord();
    }

    public static void writeDotModel(MealyMachine<?, String, ?, String> model, SimpleAlphabet<String> alphabet, String filename) throws IOException, InterruptedException {
        // Write output to dot-file
        File dotFile = new File(filename);
        PrintStream psDotFile = new PrintStream(dotFile);
        GraphDOT.write(model, alphabet, psDotFile);
        psDotFile.close();

        // Convert .dot to .pdf
        Runtime.getRuntime().exec("dot -Tpdf -O " + filename);
    }

    public int countLines(String filePath) throws IOException {
        int linesCount = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            while (reader.readLine() != null) {
                linesCount++;
            }
        }
        return linesCount;
    }

    public void writeToFile(String message) throws IOException {
        String filename = "query_statistics.log";
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename, true))) {
            writer.write(message);
            writer.newLine();
        }
    }

    public void RawlogStatistic() {
        String filePath1 = "learning_queries.log";
        String filePath2 = "equivalence_queries.log";
        try {
            int LQ_num = countLines(filePath1);
            int EQ_num = countLines(filePath2);
            String message = "Learning queries: " + LQ_num + " Equivalence queries: " + EQ_num;
            System.out.println(message);
//            System.out.println("Learning queries: " + String.valueOf(LQ_num) + " Equivalence queries: " + String.valueOf(EQ_num));
            writeToFile(message);
            if(config.tgbot_enable == 1){
                sendToTelegram(config.device + "Learning queries: " + String.valueOf(LQ_num) + " Equivalence queries: " + String.valueOf(EQ_num));
            }

        } catch (IOException e) {
            System.out.println("Error reading the file: " + e.getMessage());
        }
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.err.println("Invalid number of parameters");
            System.exit(-1);
        }



        // temp solution for logging.
//        runProcess(false, "echo \"%s\" | sudo -S rm -rf learning_queries.log", pass);
//        runProcess(false, "echo \"%s\" | sudo -S rm -rf equivalence_queries.log", pass);
//        runProcess(false, "echo \"%s\" | sudo -S rm -rf query_statistics.log", pass);

        try {
            LearningConfig config = new LearningConfig(args[0]);
            String pass = config.password;
            runProcess(false, String.format("echo \"%s\" | sudo -S rm -rf learning_queries.log", pass));
            runProcess(false, String.format("echo \"%s\" | sudo -S rm -rf equivalence_queries.log", pass));
            runProcess(false, String.format("echo \"%s\" | sudo -S rm -rf query_statistics.log", pass));

            System.out.println("Loaded Learning Config correctly");
            System.out.println(config.log_executor_active);
            System.out.println(config.device);

            Learner learner = new Learner(config);
            learner.learn();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loadLearningAlgorithm(String algorithm, SimpleAlphabet<String> alphabet, StateLearnerSUL<String, String> sul) throws Exception {

        // Create the membership oracle
        //memOracle = new SULOracle<String, String>(sul);
        // Add a logging oracle
        logMemOracle = new LogOracle.MealyLogOracle<String, String>(sul, LearnLogger.getLogger("learning_queries"), combine_query);


        // Count the number of queries actually sent to the SUL
        statsMemOracle = new MealyCounterOracle<String, String>(logMemOracle, "membership queries to SUL");


        // Use cache oracle to prevent double queries to the SUL
        //cachedMemOracle = MealyCacheOracle.createDAGCacheOracle(alphabet, statsMemOracle);
        // Count the number of queries to the cache
        statsCachedMemOracle = new MealyCounterOracle<String, String>(statsMemOracle, "membership queries to cache");


        if (algorithm.toLowerCase().equals("ttt")) {
            AcexLocalSuffixFinder suffixFinder = new AcexLocalSuffixFinder(AcexAnalyzers.BINARY_SEARCH_BWD, true, "Analyzer");
            learningAlgorithm = new TTTLearnerMealy<String, String>(alphabet, statsCachedMemOracle, AcexAnalyzers.BINARY_SEARCH_BWD);
        } else {
            throw new Exception("Unknown learning algorithm " + config.learning_algorithm);
        }

    }

    public void loadEquivalenceAlgorithm(String algorithm, SimpleAlphabet<String> alphabet, StateLearnerSUL<String, String> sul) throws Exception {
        //eqOracle = new SULOracle<String, String>(sul);
        // Add a logging oracle
        logEqOracle = new LogOracle.MealyLogOracle<String, String>(sul, LearnLogger.getLogger("equivalence_queries"), combine_query);
        // Add an oracle that counts the number of queries
        statsEqOracle = new MealyCounterOracle<String, String>(logEqOracle, "equivalence queries to SUL");
        // Use cache oracle to prevent double queries to the SUL
        //cachedEqOracle = MealyCacheOracle.createDAGCacheOracle(alphabet, statsEqOracle);
        // Count the number of queries to the cache
        statsCachedEqOracle = new MealyCounterOracle<String, String>(statsEqOracle, "equivalence queries to cache");

        // Instantiate the selected equivalence algorithm
        switch (algorithm.toLowerCase()) {
            case "wmethod":
                equivalenceAlgorithm = new MealyWMethodEQOracle<String, String>(statsCachedEqOracle, config.max_depth);
                break;

            case "modifiedwmethod":
                equivalenceAlgorithm = new ModifiedWMethodEQOracle.MealyModifiedWMethodEQOracle<String, String>(config.max_depth, statsCachedEqOracle);
                break;

            case "wpmethod":
                equivalenceAlgorithm = new MealyWpMethodEQOracle<String, String>(statsCachedEqOracle, config.max_depth);
                checkingCEAlgorithm = new CheckingEQOracle<>(statsCachedEqOracle,1, WordCEs);
                break;

            case "randomwords":
                equivalenceAlgorithm = new MealyRandomWordsEQOracle<String, String>(statsCachedEqOracle, config.min_length, config.max_length, config.nr_queries, new Random(config.seed));
                break;

            default:
                throw new Exception("Unknown equivalence algorithm " + config.eqtest);
        }
    }


    public void learn() throws IOException, InterruptedException {

        LearnLogger log = LearnLogger.getLogger(Learner.class.getSimpleName());

        log.info("Using learning algorithm " + learningAlgorithm.getClass().getSimpleName());
        log.info("Using equivalence algorithm " + equivalenceAlgorithm.getClass().getSimpleName());


        log.info("Starting learning");

        SimpleProfiler.start("Total time");

        boolean learning = true;
        Counter round = new Counter("Rounds", "");

        round.increment();
        log.logPhase("Starting round " + round.getCount());
        SimpleProfiler.start("Learning");
        learningAlgorithm.startLearning();
        SimpleProfiler.stop("Learning");


        MealyMachine<?, String, ?, String> hypothesis = learningAlgorithm.getHypothesisModel();
        while (learning) {
            // Write outputs
            writeDotModel(hypothesis, alphabet, config.output_dir + "/hypothesis_" + round.getCount() + ".dot");

            String dot_filename = config.output_dir + "/hypothesis_" + round.getCount() + ".dot";

            RawlogStatistic();

//             Output statistics each round --mainly for testing CE feeding
            log.info("+++++++++++++++++++++++++++++++++++++++++++++++++++");
            log.info(SimpleProfiler.getResults());
            log.info(round.getSummary());
            log.info(statsMemOracle.getStatisticalData().getSummary());
            log.info(statsCachedMemOracle.getStatisticalData().getSummary());
            log.info(statsEqOracle.getStatisticalData().getSummary());
            log.info(statsCachedEqOracle.getStatisticalData().getSummary());
            log.info("States in this hypothesis: " + hypothesis.size());

            if(Learner.check_complete(dot_filename, config.final_symbol)){
                if(config.tgbot_enable == 1){
                    sendToTelegram("Learning complete: " + config.final_symbol + " found in " + dot_filename);
                }
                System.out.println("\n\nLearning complete.");
                System.out.println(config.final_symbol + " found in " + dot_filename);
                if(config.DIKEUE_compare == 1){
                    UESUL.kill_eNodeb();
                    UESUL.kill_core();
                    System.exit(0);
                }

            }



            // Search counter-example
            SimpleProfiler.start("Searching for counter-example");
            DefaultQuery<String, Word<String>> counterExample = checkingCEAlgorithm.findCounterExample(hypothesis, alphabet); //open this line and comment out the next line, and also open "if(counterExample == null){" branch to enable CE feeding
//            DefaultQuery<String, Word<String>> counterExample = equivalenceAlgorithm.findCounterExample(hypothesis, alphabet);
            if(counterExample == null){
                counterExample = equivalenceAlgorithm.findCounterExample(hypothesis, alphabet);
            }

            SimpleProfiler.stop("Searching for counter-example");

            if (counterExample == null) {
                // No counter-example found, so done learning
                learning = false;

                // Write outputs
                writeDotModel(hypothesis, alphabet, config.output_dir + "/learnedModel.dot");

                dot_filename = config.output_dir + "/learnedModel.dot";
                if(Learner.check_complete(dot_filename, config.final_symbol)){
                    System.out.println("\n\nLearning complete.");
                    System.out.println(config.final_symbol + "found in " + dot_filename);
                    if(config.DIKEUE_compare == 1){ // For AE purpose
                        UESUL.kill_eNodeb();
                        UESUL.kill_core();
                        System.exit(0);
                    }
//                    System.exit(0);
                }
            } else {
                // Counter example found, update hypothesis and continue learning
                log.logCounterexample("Counter-example found: " + counterExample);
                //TODO Add more logging
                round.increment();
                log.logPhase("Starting round " + round.getCount());


                try{
                    SimpleProfiler.start("Learning");
                    learningAlgorithm.refineHypothesis(counterExample);
                    SimpleProfiler.stop("Learning");
                } catch (Exception e) {
                    if(config.tgbot_enable == 1){
                        sendToTelegram(e.toString() + config.device + " at counter example: " + counterExample.toString());
                    }
                    e.printStackTrace();
                    kill_core();
                    kill_eNodeb();
                    kill_uecontroller();
                    runProcess(false, "python3 ./UEController/UEResetter.py");
                    sleep(1000);
                    System.exit(1);
                }


                hypothesis = learningAlgorithm.getHypothesisModel();
            }

            logEqOracle.count = 0;
        }

        SimpleProfiler.stop("Total time");


        // Output statistics
        log.info("-------------------------------------------------------");
        log.info(SimpleProfiler.getResults());
        log.info(round.getSummary());
        log.info(statsMemOracle.getStatisticalData().getSummary());
        log.info(statsCachedMemOracle.getStatisticalData().getSummary());
        log.info(statsEqOracle.getStatisticalData().getSummary());
        log.info(statsCachedEqOracle.getStatisticalData().getSummary());
        log.info("States in final hypothesis: " + hypothesis.size());
    }

    public static boolean check_complete(String filename, String final_symbol) throws FileNotFoundException {
        final_symbol = final_symbol.trim();

        if(final_symbol.equalsIgnoreCase("")){
            return false;
        }

        File myObj = new File(filename);
        Scanner myReader = new Scanner(myObj);
        while (myReader.hasNextLine()) {
            String data = myReader.nextLine();
            System.out.println(data);

            if (data.contains("/")){
                String [] parts = data.split("/");
                if (parts.length < 2){
                    return false;
                }
                if (parts[1].trim().replaceAll("\"];", "").equalsIgnoreCase(final_symbol)){
                    return true;
                }

            }
        }
        return false;
    }

    public void configureLogging(String output_dir) throws SecurityException, IOException {
        MyLearnLogger loggerLearnlib = MyLearnLogger.getLogger("de.learnlib");
        loggerLearnlib.setLevel(Level.ALL);
        FileHandler fhLearnlibLog = new FileHandler(output_dir + "/learnlib.log");
        loggerLearnlib.addHandler(fhLearnlibLog);
        fhLearnlibLog.setFormatter(new SimpleFormatter());

        MyLearnLogger loggerLearner = MyLearnLogger.getLogger(Learner.class.getSimpleName());
        loggerLearner.setLevel(Level.ALL);
        FileHandler fhLearnerLog = new FileHandler(output_dir + "/learner.log");
        loggerLearner.addHandler(fhLearnerLog);
        fhLearnerLog.setFormatter(new SimpleFormatter());
        loggerLearner.addHandler(new ConsoleHandler());

        MyLearnLogger loggerLearningQueries = MyLearnLogger.getLogger("learning_queries");
        loggerLearningQueries.setLevel(Level.ALL);
        FileHandler fhLearningQueriesLog = new FileHandler(output_dir + "/learning_queries.log");
        loggerLearningQueries.addHandler(fhLearningQueriesLog);
        fhLearningQueriesLog.setFormatter(new SimpleFormatter());
        loggerLearningQueries.addHandler(new ConsoleHandler());

        MyLearnLogger loggerEquivalenceQueries = MyLearnLogger.getLogger("equivalence_queries");
        loggerEquivalenceQueries.setLevel(Level.ALL);
        FileHandler fhEquivalenceQueriesLog = new FileHandler(output_dir + "/equivalence_queries.log");
        loggerEquivalenceQueries.addHandler(fhEquivalenceQueriesLog);
        fhEquivalenceQueriesLog.setFormatter(new SimpleFormatter());
        loggerEquivalenceQueries.addHandler(new ConsoleHandler());
    }
}

