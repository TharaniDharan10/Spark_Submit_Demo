package com.example.Spark_Submit_Demo;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import scala.Tuple2;

import java.util.Arrays;

/**
 * WordCount application using Apache Spark with HDFS integration
 *
 * This application reads text files from HDFS, counts word occurrences,
 * and writes the results back to HDFS.
 *
 * @author Spark Example
 * @version 0.0.1
 */
public class WordCount {

	public static void main(String[] args) {

		// Check if input and output paths are provided
		if (args.length < 2) {
			System.err.println("Usage: WordCount <input_path> <output_path>");
			System.err.println("Example: WordCount hdfs://localhost:50000/word hdfs://localhost:50000/wordoutput2");
			System.exit(1);
		}

		String inputPath = args[0];
		String outputPath = args[1];

		System.out.println("Starting WordCount application...");
		System.out.println("Input path: " + inputPath);
		System.out.println("Output path: " + outputPath);

		// Create Spark configuration
		SparkConf conf = new SparkConf()
				.setAppName("WordCount");

		// Create Java Spark Context
		JavaSparkContext sc = new JavaSparkContext(conf);

		try {
			// Read the input file from HDFS
			System.out.println("Reading input from HDFS...");
			JavaRDD<String> textFile = sc.textFile(inputPath);

			// Perform word count operation
			System.out.println("Processing word count...");
			JavaPairRDD<String, Integer> counts = textFile
					.flatMap(line -> Arrays.asList(line.split("\\s+")).iterator())  // Split line into words
					.filter(word -> !word.isEmpty())  // Filter out empty strings
					.mapToPair(word -> new Tuple2<>(word, 1))  // Map each word to (word, 1)
					.reduceByKey((a, b) -> a + b);  // Sum up counts for each word

			// Save the result to HDFS
			System.out.println("Saving results to HDFS...");
			counts.saveAsTextFile(outputPath);

			System.out.println("=================================");
			System.out.println("Word count completed successfully!");
			System.out.println("Output saved to: " + outputPath);
			System.out.println("=================================");

		} catch (Exception e) {
			System.err.println("Error during word count: " + e.getMessage());
			e.printStackTrace();
			System.exit(1);
		} finally {
			// Stop the Spark context
			sc.stop();
		}
	}
}