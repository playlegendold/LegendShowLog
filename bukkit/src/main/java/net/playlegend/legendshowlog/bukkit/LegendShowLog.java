package net.playlegend.legendshowlog.bukkit;

import org.bukkit.plugin.java.JavaPlugin;

public class LegendShowLog extends JavaPlugin {

  public static final String LOG_PATH = "logs/latest.log";
  public static final String POST_URL = "https://paste.playlegend.net/documents";
  public static final String PASTE_DOMAIN = "https://paste.playlegend.net/";

  @Override
  public void onEnable() {
    this.getCommand("showlog").setExecutor(new ShowLogCommand());
  }
}
