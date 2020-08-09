package net.playlegend.legendshowlog.bukkit;

import org.apache.commons.io.FileUtils;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

public class LegendShowLog extends JavaPlugin {

  private String logPath;
  private String postUrl;
  private String pasteDomain;

  @Override
  public void onLoad() {
    File configFile = new File(this.getDataFolder(), "config.yml");
    if (!configFile.exists()) {
      try {
        if (!configFile.getParentFile().isDirectory()) {
          configFile.getParentFile().mkdirs();
        }

        FileUtils.copyURLToFile(LegendShowLog.class.getResource("/config.yml"), configFile);
      } catch (IOException e) {
        this.getLogger().log(Level.SEVERE, "Can't save default config" + e);
      }
    }
    YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
    logPath = config.getString("showlog.log_path");
    postUrl = config.getString("showlog.post_url");
    pasteDomain = config.getString("showlog.paste_url");
  }

  @Override
  public void onEnable() {
    this.getCommand("showlog").setExecutor(new ShowLogCommand(logPath, postUrl, pasteDomain));
  }
}
