package me.vermeil.hyperion;

import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;

public class HyperionEventListener implements Listener {
    private final Map<UUID, Long> healingCooldowns = new HashMap<>();

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!isRightClick(event.getAction())) {
            return;
        }

        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        if (!HyperionBuilder.isHyperion(item)) {
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

    private boolean isRightClick(Action action) {
        return action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK;
    }

    private void teleportPlayer(Player player) {
        Vector direction = player.getLocation().getDirection().normalize();
        Location teleportDestination = findTeleportDestination(player, direction);
        player.teleport(teleportDestination);

        World world = player.getWorld();
        Location playerLocation = player.getLocation();
        world.playSound(playerLocation, Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 1.0f);
        world.spawnParticle(Particle.EXPLOSION, playerLocation, 5);
    }

    private Location findTeleportDestination(Player player, Vector direction) {
        Location startLocation = player.getLocation();
        Location teleportDestination = startLocation.clone();
        for (double i = 0; i < 10.0; i += 0.5) {
            Location currentLocation = startLocation.clone().add(direction.clone().multiply(i));
            if (!currentLocation.getBlock().isPassable()) {
                break;
            }
            teleportDestination = currentLocation;
        }
        return teleportDestination;
    }

    private int damageNearbyEntities(Player player) {
        double damage = RandomDamage();
        double range = 5.0;
        int entitiesDamaged = 0;

        for (Entity entity : player.getNearbyEntities(range, range, range)) {
            if (entity instanceof LivingEntity && !(entity instanceof Player)) {
                ((LivingEntity) entity).damage(damage);
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
        player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, PotionEffect.INFINITE_DURATION, 20));
        player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 100, 10));
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 100, 1));

        World world = player.getWorld();
        Location playerLocation = player.getLocation();
        world.playSound(playerLocation, Sound.ENTITY_ZOMBIE_VILLAGER_CURE, 1.0f, 1.0f);
        world.spawnParticle(Particle.EXPLOSION, playerLocation, 1);
    }

    private void setHealingCooldown(Player player) {
        healingCooldowns.put(player.getUniqueId(), System.currentTimeMillis() + 5000);

        new BukkitRunnable() {
            @Override
            public void run() {
                healingCooldowns.remove(player.getUniqueId());
            }
        }.runTaskLater(JavaPlugin.getPlugin(Hyperion.class), 5000 / 50);
    }
}
