package com.agtinternational.wordCount.impl;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.agtinternational.wordCount.entities.BigFile;
import com.agtinternational.wordCount.entities.GeneralFile;
import com.agtinternational.wordCount.entities.Word;

public class CountWords {

	private static final String ONLY_LETTERS_REGEX = "\\p{L}+";
	private static final String CHARSET_ISO = "ISO-8859-1";

	public static void main(String args[]) throws IOException {

		String fileName = "/home/guel/agt/testdata/a/Berlin.txt";
		String folder = "/home/guel/agt/testdata";
		readFolders(folder);
		// readFile(fileName);
	}

	private static void readFolders(String folder) throws IOException {
		try (Stream<Path> paths = Files.walk(Paths.get(folder))) {
			paths.forEach(filePath -> {
				if (Files.isDirectory(filePath))
					System.out.println("Directory " + filePath);

				if (Files.isRegularFile(filePath)) {
					// System.out.println("File" + filePath);
					try {

						Object file = readFile(filePath.toString());
						if ( file instanceof BigFile) {
							BigFile bigFile = (BigFile) file;
							System.out.println("es big file "+bigFile.getName()+" words: "+bigFile.getWordCount());
						}
						if (file instanceof GeneralFile) {
							GeneralFile generalFile = (GeneralFile) file;
							System.out.println("es small file "+generalFile.getName()+" words: "+generalFile.getWordCount());
						}

					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			});
		}
	}

	private static void countInLine(String line, Map<String, Long> result) {
		// System.out.println("result entrada " + result);
		Map<String, Long> counter = Arrays.asList(line.split(",|\\s+|\\.")).stream().map(String::toLowerCase)
				.filter(word -> word.length() > 0).filter(word -> word.matches(ONLY_LETTERS_REGEX))
				.collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

		counter.forEach((key, value) -> result.merge(key, value, Long::sum));

		// System.out.println(counter);
		// System.out.println("result " + result);
		// System.out.println("total of words " + sum + "\n \n");
	}

	private static Object readFile(String fileName) throws IOException {

		Map<String, Long> result = new HashMap<>();
		Files.lines(Paths.get(fileName), Charset.forName(CHARSET_ISO)).filter(line -> line.length() > 0)
				.forEach(String -> {
					countInLine(String, result);
				});

		long sumWords = result.values().stream().mapToLong(Long::longValue).sum();
		System.out.println(sumWords);
		if (sumWords > 1000) {
			BigFile file = new BigFile();
			file.setName(fileName);
			file.setWordCount(sumWords);
			List<Word> words = new ArrayList();
			Map<String, Long> wordsFiltered = result.entrySet().stream().filter(map -> map.getValue() > 40)

					.collect(Collectors.toMap(p -> p.getKey(), p -> p.getValue()));
			wordsFiltered.forEach((k, v) -> {
				Word word = new Word(k, v);
				words.add(word);
			});
			file.setWords(words);

			System.out.println("@@@@@@@@@" + wordsFiltered);

			return file;
		}
		// System.out.println(sumWords);
		// System.out.println(result);
		GeneralFile file = new GeneralFile();
		file.setName(fileName);
		file.setWordCount(sumWords);

		return file;

	}

}
