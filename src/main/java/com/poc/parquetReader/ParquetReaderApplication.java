package com.poc.parquetReader;

import com.poc.parquetReader.writer.CustomParquetWriter;
import org.apache.hadoop.fs.Path;
import org.apache.parquet.hadoop.metadata.CompressionCodecName;
import org.apache.parquet.schema.MessageType;
import org.apache.parquet.schema.MessageTypeParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

@SpringBootApplication
public class ParquetReaderApplication implements CommandLineRunner {

	@Value("${schema.filePath}")
	private String schemaFilePath;

	@Value("${output.directoryPath}")
	private String outputDirectoryPath;

	private static final Logger logger = LoggerFactory.getLogger(ParquetReaderApplication.class);


	public static void main(String[] args) {
		SpringApplication.run(ParquetReaderApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {

		List<List<String>> columns = getDataForFile();
		MessageType schema = getSchemaForParquetFile();
		CustomParquetWriter writer = getParquetWriter(schema);

		for (List<String> column : columns) {
			logger.info("Writing line: " + column.toArray());
			writer.write(column);
		}
		logger.info("Finished writing Parquet file.");

		writer.close();
	}

	private CustomParquetWriter getParquetWriter(MessageType schema) throws IOException {
		String outputFilePath = outputDirectoryPath + "/" + System.currentTimeMillis() + ".parquet";
		File outputParquetFile = new File(outputFilePath);
		Path path = new Path(outputParquetFile.toURI().toString());
		return new CustomParquetWriter(
				path, schema, false, CompressionCodecName.SNAPPY
		);
	}

	private MessageType getSchemaForParquetFile() throws IOException {
        Resource resource = new ClassPathResource("user.schema");
        File file = resource.getFile();
		String rawSchema = new String(Files.readAllBytes(file.toPath()));
		return MessageTypeParser.parseMessageType(rawSchema);
	}

	private List<List<String>> getDataForFile() {
		List<List<String>> data = new ArrayList<>();

		List<String> parquetFileItem1 = new ArrayList<>();
		parquetFileItem1.add("1");
		parquetFileItem1.add("Name1");
		parquetFileItem1.add("true");

		List<String> parquetFileItem2 = new ArrayList<>();
		parquetFileItem2.add("2");
		parquetFileItem2.add("Name2");
		parquetFileItem2.add("false");

		data.add(parquetFileItem1);
		data.add(parquetFileItem2);

		return data;
	}

}
