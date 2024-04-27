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
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class Hyperion extends JavaPlugin implements Listener {
    private final Map<UUID, Long> healingCooldowns = new HashMap<>();
    private static final int HEALING_COOLDOWN_SECONDS = 5;

    @Override
    public void onEnable() {
        getLogger().info("Hyperion has been enabled.");
        Bukkit.getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        getLogger().info("Hyperion has been disabled.");
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!isHyperionAttack(event)) {
            return;
        }

        Player player = event.getPlayer();
        teleportPlayer(player);

        int entitiesDamaged = damageNearbyEntities(player);
        notifyPlayerOfDamage(player, entitiesDamaged);

        if (canUseHealing(player)) {
            applyHealingEffects(player);
            setHealingCooldown(player);
            scheduleHealingCooldownReset(player);
        }
    }

    private boolean isHyperionAttack(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        return player.getInventory().getItemInMainHand().getType() == Material.IRON_SWORD
                && (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK);
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

        for (Entity entity : player.getNearbyEntities(range, range, range)) {
            if (entity instanceof LivingEntity && !(entity instanceof Player)) {
                double damage = ThreadLocalRandom.current().nextDouble(1, 1000);
                ((LivingEntity) entity).damage(damage);
                entitiesDamaged++;

                Location entityLocation = entity.getLocation();
                entityLocation.setDirection(player.getLocation().getDirection());
                entity.teleport(entityLocation);
            }
        }

        return entitiesDamaged;
    }

    private void notifyPlayerOfDamage(Player player, int entitiesDamaged) {
        double totalDamage = ThreadLocalRandom.current().nextDouble(1, 1000) * entitiesDamaged;
        String message = ChatColor.GRAY + "Your Implosion hit " + ChatColor.RED + entitiesDamaged
                + ChatColor.GRAY + " enemies for " + ChatColor.RED + String.format("%.2f", totalDamage)
                + ChatColor.GRAY + " damage.";
        player.sendMessage(message);
    }

    private boolean canUseHealing(Player player) {
        if (!healingCooldowns.containsKey(player.getUniqueId())) {
            return true;
        }

        long lastUseTime = healingCooldowns.get(player.getUniqueId());
        long cooldownTimeLeft = (lastUseTime / 1000 + HEALING_COOLDOWN_SECONDS) - (System.currentTimeMillis() / 1000);
        return cooldownTimeLeft <= 0;
    }

    private void applyHealingEffects(Player player) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 200, 20));
        player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 100, 20));

        World world = player.getWorld();
        world.playSound(player.getLocation(), Sound.ENTITY_ZOMBIE_VILLAGER_CURE, 1.0f, 1.0f);
        world.spawnParticle(Particle.EXPLOSION, player.getLocation(), 5);
    }

    private void setHealingCooldown(Player player) {
        healingCooldowns.put(player.getUniqueId(), System.currentTimeMillis());
    }

    private void scheduleHealingCooldownReset(Player player) {
        new BukkitRunnable() {
            @Override
            public void run() {
                healingCooldowns.remove(player.getUniqueId());
                player.sendMessage(ChatColor.GREEN + "Healing ability is now available!");
            }
        }.runTaskLater(this, HEALING_COOLDOWN_SECONDS * 20);
    }
}
