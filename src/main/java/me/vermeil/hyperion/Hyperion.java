package me.vermeil.hyperion;

import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class Hyperion extends JavaPlugin implements Listener {
    private static final Random random = new Random();
    private final Map<UUID, Long> healingCooldowns = new HashMap<>();

    @Override
    public void onEnable() {
        getLogger().info("Hyperion has been enabled.");
        Bukkit.getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        getLogger().info("Hyperion has been disabled.");
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (!player.getInventory().getItemInMainHand().getType().equals(Material.IRON_SWORD)) {
            return;
        }

        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Vector direction = player.getLocation().getDirection().normalize();

        Location teleportDestination = player.getLocation();
        for (double i = 0; i < 10; i += 0.5) {
            Location loc = player.getLocation().add(direction.clone().multiply(i));
            if (!loc.getBlock().isPassable()) {
                break;
            }
            teleportDestination = loc;
        }

        player.teleport(teleportDestination);

        World world = player.getWorld();
        world.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 1.0f);
        world.spawnParticle(Particle.EXPLOSION_LARGE, player.getLocation(), 10);

        double range = 5.0;
        int entitiesDamaged = 0;
        double totalDamage = 0;
        for (Entity entity : player.getNearbyEntities(range, range, range)) {
            if (entity instanceof LivingEntity && !(entity instanceof Player)) {
                Location entityLocation = entity.getLocation();
                entityLocation.setDirection(player.getLocation().getDirection());
                entity.teleport(entityLocation);
                double damage = random.nextInt(71) + 5;
                ((LivingEntity) entity).damage(damage);
                entitiesDamaged++;
                totalDamage += damage;
            }
        }

        player.sendMessage(ChatColor.GRAY + "Your Implosion hit " + ChatColor.RED + entitiesDamaged + ChatColor.GRAY + " enemies for " + ChatColor.RED + String.format("%.1f", totalDamage) + ChatColor.GRAY + " damage.");

        int healingCooldownSeconds = 5;
        if (healingCooldowns.containsKey(player.getUniqueId())) {
            long cooldownTimeLeft = ((healingCooldowns.get(player.getUniqueId()) / 1000) + healingCooldownSeconds) - (System.currentTimeMillis() / 1000);
            if (cooldownTimeLeft > 0) {
                return;
            }
        }

        player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 200, 5));
        player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 200, 20));
        player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 200, 20));

        healingCooldowns.put(player.getUniqueId(), System.currentTimeMillis());
        world.playSound(player.getLocation(), Sound.ENTITY_ZOMBIE_VILLAGER_CURE, 1.0f, 1.0f);
        world.spawnParticle(Particle.EXPLOSION_LARGE, player.getLocation(), 10);
        new BukkitRunnable() {
            @Override
            public void run() {
                healingCooldowns.remove(player.getUniqueId());
                player.sendMessage(ChatColor.GREEN + "Healing ability is now available!");
            }
        }.runTaskLater(this, healingCooldownSeconds * 20);
    }
}
