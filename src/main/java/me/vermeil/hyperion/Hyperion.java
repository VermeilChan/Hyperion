package me.vermeil.hyperion;

import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;

public class Hyperion extends JavaPlugin implements Listener, CommandExecutor {
    private final Map<UUID, Long> healingCooldowns = new HashMap<>();

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        Objects.requireNonNull(getCommand("givehyperion")).setExecutor(this);
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            return true;
        }

        Player player = (Player) sender;

        if (!player.isOp()) {
            return true;
        }

        ItemStack hyperionSword = giveHyperion();
        player.getInventory().addItem(hyperionSword);
        player.sendMessage(ChatColor.GREEN + "You have received the Hyperion");

        return true;
    }

    private ItemStack giveHyperion() {
        ItemStack hyperionSword = new ItemStack(Material.IRON_SWORD);
        ItemMeta meta = hyperionSword.getItemMeta();

        Objects.requireNonNull(meta).setDisplayName(ChatColor.LIGHT_PURPLE + "Shiny Heroic Hyperion " + ChatColor.GOLD + "✪✪✪✪" + ChatColor.RED + "➎");

        List<String> lore = Arrays.asList(
                ChatColor.GRAY + "Gear Score: " + ChatColor.LIGHT_PURPLE + "1184 " + ChatColor.DARK_GRAY + "(5000)",
                ChatColor.GRAY + "Damage: " + ChatColor.RED + "366 " + ChatColor.YELLOW + "(+30) " + ChatColor.DARK_GRAY + "(+2,125)",
                ChatColor.GRAY + "Strength: " + ChatColor.RED + "250 " + ChatColor.YELLOW + "(+30) " + ChatColor.GOLD + "[+5] " + ChatColor.BLUE + "(+50) " + ChatColor.DARK_GRAY + "(+1,468.75)",
                ChatColor.GRAY + "Crit Damage: " + ChatColor.RED + "+70% " + ChatColor.DARK_GRAY + "(+437.5%)",
                ChatColor.GRAY + "Bonus Attack Speed: " + ChatColor.RED + "+7% " + ChatColor.BLUE + "(+7% ) " + ChatColor.DARK_GRAY + "(+10.5%)",
                ChatColor.GRAY + "Intelligence: " + ChatColor.GREEN + "+634 " + ChatColor.BLUE + "(+125) " + ChatColor.LIGHT_PURPLE + "(+24) " + ChatColor.DARK_GRAY + "(+3,743.75)",
                ChatColor.GRAY + "Ferocity: " + ChatColor.GREEN + "+33 " + ChatColor.DARK_GRAY + "(+45)",
                ChatColor.DARK_PURPLE + "[" + ChatColor.AQUA + "✎" + ChatColor.DARK_PURPLE + "] " + ChatColor.DARK_PURPLE + "[" + ChatColor.AQUA + "⚔" + ChatColor.DARK_PURPLE + "]",
                "",
                ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "Ultimate Wise V" + ChatColor.BLUE + ", " + ChatColor.BLUE + "Champion X, Cleave VI",
                ChatColor.BLUE + "Critical VII, Cubism VI, Divine Gift III",
                ChatColor.BLUE + "Dragon Hunter V, Ender Slayer VII, Execute VI",
                ChatColor.BLUE + "Experience V, Fire Aspect III, First Strike IV",
                ChatColor.BLUE + "Giant Killer VII, Impaling III, Lethality VI",
                ChatColor.BLUE + "Looting V, Luck VII, Mana Steal III",
                ChatColor.BLUE + "Scavenger V, Smite VII, Smoldering V",
                ChatColor.BLUE + "Tabasco III, Thunderlord VII, Vampirism VI",
                ChatColor.BLUE + "Venomous VI",
                "",
                ChatColor.DARK_PURPLE + "◆ Music Rune III",
                "",
                ChatColor.GRAY + "Deals +" + ChatColor.RED + "50% " + ChatColor.GRAY + "damage to Withers.",
                ChatColor.GRAY + "Grants " + ChatColor.RED + "+1 ❁ Damage " + ChatColor.GRAY + "and " + ChatColor.GREEN + "+2 " + ChatColor.AQUA + "✎",
                ChatColor.AQUA + "Intelligence " + ChatColor.GRAY + "pre " + ChatColor.RED + "Catacombs " + ChatColor.GRAY + "level.",
                "",
                ChatColor.GREEN + "Scroll Abilities:",
                ChatColor.GOLD + "Ability: Wither Impact " + ChatColor.YELLOW + ChatColor.BOLD + "RIGHT CLICK",
                ChatColor.GRAY + "Teleport " + ChatColor.GREEN + "10 blocks" + ChatColor.GRAY + " ahead of you.",
                ChatColor.GRAY + "Then implode dealing " + ChatColor.RED + "36,647 " + ChatColor.GRAY + "damage",
                ChatColor.GRAY + "to nearby enemies. Also applies the",
                ChatColor.GRAY + "wither shield scroll ability reducing",
                ChatColor.GRAY + "damage taken and granting an",
                ChatColor.GRAY + "absorption shield for " + ChatColor.YELLOW + "5 " + ChatColor.GRAY + "seconds.",
                ChatColor.DARK_GRAY + "Mana Cost: " + ChatColor.DARK_AQUA + "150",
                "",
                ChatColor.LIGHT_PURPLE + "" + ChatColor.MAGIC + "A" + ChatColor.RESET + ChatColor.LIGHT_PURPLE + ChatColor.BOLD + " SHINY MYTHIC DUNGEON SWORD " + ChatColor.MAGIC + "A"
        );

        meta.setLore(lore);
        meta.addEnchant(Enchantment.UNBREAKING, 100 , true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        meta.setUnbreakable(true);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        hyperionSword.setItemMeta(meta);

        return hyperionSword;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        if (!isHyperion(item)) {
            return;
        }

        teleportPlayer(player);
        int entitiesDamaged = damageNearbyEntities(player);
        notifyPlayerOfDamage(player, entitiesDamaged);

        if (canUseHealing(player)) {
            applyHealingEffects(player);
            setHealingCooldown(player);
        }
    }

    private boolean isHyperion(ItemStack item) {
        if (item == null || item.getType() != Material.IRON_SWORD) {
            return false;
        }

        if (!item.hasItemMeta()) {
            return false;
        }

        ItemMeta meta = item.getItemMeta();
        if (!Objects.requireNonNull(meta).hasDisplayName()) {
            return false;
        }

        String expectedDisplayName = ChatColor.LIGHT_PURPLE + "Shiny Heroic Hyperion " + ChatColor.GOLD + "✪✪✪✪" + ChatColor.RED + "➎";

        return meta.getDisplayName().equals(expectedDisplayName);
    }

    private void teleportPlayer(Player player) {
        Vector direction = player.getLocation().getDirection().normalize();
        Location teleportDestination = findTeleportDestination(player, direction);
        player.teleport(teleportDestination);

        World world = player.getWorld();
        world.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 1.0f);
        world.spawnParticle(Particle.EXPLOSION, player.getLocation(), 5);
    }

    private Location findTeleportDestination(Player player, Vector direction) {
        Location teleportDestination = player.getLocation();
        for (double i = 0; i < 10; i += 0.5) {
            Location loc = player.getLocation().add(direction.clone().multiply(i));
            if (!loc.getBlock().isPassable()) {
                break;
            }
            teleportDestination = loc;
        }
        return teleportDestination;
    }

    private int damageNearbyEntities(Player player) {
        double range = 5.0;
        int entitiesDamaged = 0;
        double hyperionDamage = RandomDamage();

        for (Entity entity : player.getNearbyEntities(range, range, range)) {
            if (entity instanceof LivingEntity && !(entity instanceof Player)) {
                ((LivingEntity) entity).damage(hyperionDamage);
                entitiesDamaged++;
            }
        }

        return entitiesDamaged;
    }

    private double RandomDamage() {
        return 30000 + (Math.random() * (50000 - 30000));
    }

    private void notifyPlayerOfDamage(Player player, int entitiesDamaged) {
        if (entitiesDamaged > 0) {
            double totalDamage = entitiesDamaged * RandomDamage();
            String message = ChatColor.GRAY + "Your Implosion hit " + ChatColor.RED + entitiesDamaged + ChatColor.GRAY + " enemies for " + ChatColor.RED + String.format("%.2f", totalDamage) + ChatColor.GRAY + " damage.";
            player.sendMessage(message);
        }
    }

    private boolean canUseHealing(Player player) {
        Long cooldownEnd = healingCooldowns.get(player.getUniqueId());
        return cooldownEnd == null || System.currentTimeMillis() >= cooldownEnd;
    }

    private void applyHealingEffects(Player player) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, PotionEffect.INFINITE_DURATION, 20, true));
        player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 100, 10, true));
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 100, 5, true));

        World world = player.getWorld();
        world.playSound(player.getLocation(), Sound.ENTITY_ZOMBIE_VILLAGER_CURE, 1.0f, 1.0f);
        world.spawnParticle(Particle.EXPLOSION, player.getLocation(), 1);
    }

    private void setHealingCooldown(Player player) {
        healingCooldowns.put(player.getUniqueId(), System.currentTimeMillis() + 5000);

        new BukkitRunnable() {
            @Override
            public void run() {
                healingCooldowns.remove(player.getUniqueId());
            }
        }.runTaskLater(this, 5000 / 50);
    }
}
