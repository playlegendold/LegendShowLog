package net.playlegend.legendshowlog.bukkit;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.input.ReversedLinesFileReader;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class ShowLogCommand implements CommandExecutor {

  private static final long MAX_FILE_LENGTH = 5 * 1024 * 1024L;
  private static final ExecutorService executorService = Executors.newSingleThreadExecutor();
  private static final Gson gson = new Gson();
  
  private final String logPath;
  private final String postUrl;
  private final String pasteUrl;

  @Override
  public boolean onCommand(final @NotNull CommandSender sender, final @NotNull Command command,
                           final @NotNull String label, final @NotNull String[] args) {
    executorService.submit(() -> {
      File logFile = new File(this.logPath);
      if (!logFile.exists()) {
        sender.sendMessage(ChatColor.RED + "Logfile not found (" + this.logPath + ")");
        return;
      }

      if (!logFile.isFile()) {
        sender.sendMessage(ChatColor.RED + "Logfile is not a file");
        return;
      }

      try {
        byte[] data;
        if (args.length == 0) {
          if (logFile.length() > MAX_FILE_LENGTH) {
            sender.sendMessage(ChatColor.GRAY + "Log file is too big use " + ChatColor.GOLD + "/showlog <lines>");
            return;
          }
          data = Files.readAllBytes(logFile.toPath());
        } else if (args.length == 1) {
          data = this.readBottomLines(logFile, Integer.parseInt(args[0]));
        } else {
          sender.sendMessage(ChatColor.GRAY + "Usage: " + ChatColor.GOLD + "/showlog <lines>");
          return;
        }

        sender.sendMessage(ChatColor.GRAY + "Log: " + ChatColor.GOLD + this.pasteUrl
            + this.postToHastebin(data));
      } catch (NumberFormatException ex) {
        sender.sendMessage(ChatColor.GRAY + "Usage: " + ChatColor.GOLD + "/showlog <lines>");
      } catch (Exception ex) {
        sender.sendMessage(ChatColor.RED + "An error occurred while reading the log file (" + ex.getMessage() + ")");
      }
    });

    return true;
  }

  private byte[] readBottomLines(final File file, final int lines) throws IOException {
    try (ReversedLinesFileReader reader = new ReversedLinesFileReader(file, StandardCharsets.UTF_8)) {
      byte[][] buffer = new byte[lines][];
      int linesToRead = lines;

      String line;
      int currentLine = 0;
      while ((line = reader.readLine()) != null && currentLine++ < lines) {
        byte[] lineBytes = line.getBytes(StandardCharsets.UTF_8);
        buffer[--linesToRead] = lineBytes;
      }

      int unreadLines = lines - linesToRead;
      int bufferLen = Arrays.stream(buffer).skip(linesToRead).mapToInt(b -> b.length).sum();
      byte[] returnArray = new byte[bufferLen + unreadLines];
      int returnArrayIndex = 0;
      for (int i = linesToRead; i < buffer.length; i++) {
        byte[] lineArray = buffer[i];
        System.arraycopy(buffer[i], 0, returnArray, returnArrayIndex, lineArray.length);
        returnArrayIndex += lineArray.length;
        returnArray[returnArrayIndex++] = '\n';
      }

      return returnArray;
    }
  }

  private String postToHastebin(final byte[] data) throws IOException {
    URL url = new URL(this.postUrl);
    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
    urlConnection.setDoOutput(true);
    urlConnection.setRequestMethod("POST");

    try (OutputStream outputStream = urlConnection.getOutputStream()) {
      outputStream.write(data);
    }

    urlConnection.connect();

    if (urlConnection.getResponseCode() != 200) {
      throw new IOException("Server returned not 200 response code");
    }

    try (InputStreamReader inputStreamReader = new InputStreamReader(urlConnection.getInputStream());
         BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {
      String result = bufferedReader.lines().collect(Collectors.joining("\n"));

      JsonObject response = gson.fromJson(result, JsonObject.class);

      return response.get("key").getAsString();
    }
  }
}
