package com.example.Client2;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class WriteCSV {

    public void WriteToCSV(String fileName, List<PostRecord> postRecordList) throws IOException {

      FileWriter fileWriter = new FileWriter(fileName + ".csv");
      try {
        fileWriter.write("Start Time, Request Type, Latency, Response Code\n");
        for (PostRecord record : postRecordList) {
          fileWriter.write(record.toString());
          fileWriter.write("\n");
        }
        fileWriter.flush();
        fileWriter.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
}
