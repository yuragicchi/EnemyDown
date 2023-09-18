package plugin.enemydown;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import plugin.enemydown.command.EnemyDownCommand;

public final class Main extends JavaPlugin{

    @Override
    public void onEnable() {
        EnemyDownCommand enemyDownCommand = new EnemyDownCommand();
        Bukkit.getPluginManager().registerEvents(enemyDownCommand, this); //enemyDownCommandをイベントリスナーとして登録する
        getCommand("enemyDown").setExecutor(enemyDownCommand); //getCommand("enemyDown")を使用して、"enemyDown"という名前のコマンドを取得し、そのコマンドの実行をenemyDownCommandに委任する
    }

}
