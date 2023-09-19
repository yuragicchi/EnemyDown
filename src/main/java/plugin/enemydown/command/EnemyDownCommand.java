package plugin.enemydown.command;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import plugin.enemydown.Main;
import plugin.enemydown.data.PlayerScore;

import java.util.*;

//EnemyDownCommandクラスが CommandExecutor と Listener という2つのインタフェースを実装している
public class EnemyDownCommand implements CommandExecutor, Listener {  //コマンドの実行とイベントのリスニングが可能になる。

    private Main main;
    private List<PlayerScore> playerScoreList = new ArrayList<>();


    public EnemyDownCommand(Main main) {
        this.main = main;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(sender instanceof Player player){
            PlayerScore nowPlayer = getPlayerScore(player);
            nowPlayer.setGameTime(20);

            World world = player.getWorld();   //ワールドの情報を変数で定義

            initPlayerStatus(player);  //プレイヤーの初期状態を設定

            Bukkit.getScheduler().runTaskTimer(main, Runnable ->{
                if(nowPlayer.getGameTime() <= 0) {
                    Runnable.cancel();
                    player.sendTitle("ゲームが終了しました。",
                            nowPlayer.getPlayerName()+"合計"+nowPlayer.getScore()+"点！",
                            0,60,0);
                    nowPlayer.setScore(0);
                    List<Entity> nearbyEnemies = player.getNearbyEntities(50, 0, 50);
                    for(Entity enemy : nearbyEnemies){
                        switch (enemy.getType()) {
                            case ZOMBIE, SKELETON, SPIDER -> enemy.remove();
                        }
                    }
                    return;
                }
                world.spawnEntity(getEnemySpawnLocation(player, world), getEnemy());
                nowPlayer.setGameTime(nowPlayer.getGameTime()-5);
            },0,5*20);  //マイクラは20チップで1秒
        }
        return false;
    }



    @EventHandler
    public void onEntityDeath(EntityDeathEvent e){
        LivingEntity enemy = e.getEntity();
        Player player = enemy.getKiller();  //死亡したエンティティのキラー（プレイヤーによって殺されたエンティティ）を取得
        if (Objects.isNull(player) || playerScoreList.isEmpty()) {
            return;
        }
        for(PlayerScore playerScore : playerScoreList){
            if(playerScore.getPlayerName().equals(player.getName())){
                int point = switch (enemy.getType()) {
                    case ZOMBIE, SPIDER -> 10;
                    case SKELETON -> 20;
                    default -> 0;
                };
                playerScore.setScore(playerScore.getScore() + point);
                player.sendMessage("敵を倒した！"+"現在のスコアは"+ playerScore.getScore() +"点！");
            }
        }
    }

    /**
     * 現在実行しているプレイヤーのスコア情報を取得する。
     *
     * @param player  コマンドを実行したプレイヤー
     * @return  現在実行しているプレイヤーのスコア情報
     */
    private PlayerScore getPlayerScore(Player player) {
        if(playerScoreList.isEmpty()){
            return addNewPlayer(player);
        }else{
            for(PlayerScore playerScore : playerScoreList){
                if(!playerScore.getPlayerName().equals(player.getName())){
                    return addNewPlayer(player);
                }else{
                    return playerScore;
                }
            }
        }
        return null;
    }

    /**
     * 新規のプレイヤー情報をリストに追加します。
     *
     * @param player　コマンドを実行したプレイヤー
     * @return 　新規プレイヤー
     */
    private PlayerScore addNewPlayer(Player player) {
        PlayerScore newPlayer = new PlayerScore();
        newPlayer.setPlayerName(player.getName());
        playerScoreList.add(newPlayer);
        return newPlayer;
    }

    /**
     * ゲームを始める前にプレイヤーの状態を設定する。
     * 体力と空腹度を最大にして、装備はネザライト一式になる。
     *
     * @param player　コマンドを実行したプレイヤー
     */

    private void initPlayerStatus(Player player) {
        player.setHealth(20);   //HPをマックスにする
        player.setFoodLevel(20);  //満腹にする

        PlayerInventory inventory = player.getInventory(); //インベントリの情報を取得
        inventory.setHelmet(new ItemStack(Material.NETHERITE_HELMET));
        inventory.setChestplate(new ItemStack(Material.NETHERITE_CHESTPLATE));
        inventory.setLeggings(new ItemStack(Material.NETHERITE_LEGGINGS));
        inventory.setBoots(new ItemStack(Material.NETHERITE_BOOTS));
        inventory.setItemInMainHand(new ItemStack(Material.NETHERITE_SWORD));
    }


    /**
     * 敵の出現エリアを取得する。
     * 出現エリアはX軸とZ軸は自分の位置からランダムでプラス-10〜9の値が設定される。
     * Y軸はプレイヤーと同じ位置になる。
     *
     * @param player　コマンドを実行したプレイヤー
     * @param world　コマンドを実行したプレイヤーが所属するワールド
     * @return  敵の出現場所
     */
    private Location getEnemySpawnLocation(Player player, World world) {
        Location playerLocation = player.getLocation();   //プレイヤーの位置を変数で定義

        int randomX = new SplittableRandom().nextInt(20)-10;
        int randomZ = new SplittableRandom().nextInt(20)-10;

        double x = playerLocation.getX()+randomX;
        double y = playerLocation.getY();
        double z = playerLocation.getZ()+randomZ;

        return new Location(world, x, y, z);  //スポーン位置を表すLocationオブジェクトを作成して返す
    }

    /**
     * ランダムで敵を取得する。
     * @return  敵
     */
    private EntityType getEnemy() {
        List<EntityType> enemylist = List.of(EntityType.ZOMBIE,EntityType.SKELETON,EntityType.SPIDER);
        return enemylist.get(new SplittableRandom().nextInt(enemylist.size()));  //0,1をランダムに生成し、返す
    }
}
