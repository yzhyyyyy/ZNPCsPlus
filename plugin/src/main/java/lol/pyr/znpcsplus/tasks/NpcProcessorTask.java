package lol.pyr.znpcsplus.tasks;

import lol.pyr.znpcsplus.api.event.NpcDespawnEvent;
import lol.pyr.znpcsplus.api.event.NpcSpawnEvent;
import lol.pyr.znpcsplus.entity.EntityPropertyImpl;
import lol.pyr.znpcsplus.entity.EntityPropertyRegistryImpl;
import lol.pyr.znpcsplus.npc.NpcEntryImpl;
import lol.pyr.znpcsplus.npc.NpcImpl;
import lol.pyr.znpcsplus.npc.NpcRegistryImpl;
import lol.pyr.znpcsplus.user.User;
import lol.pyr.znpcsplus.user.UserManager;
import lol.pyr.znpcsplus.util.LookType;
import lol.pyr.znpcsplus.util.NpcLocation;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.NumberConversions;
import org.bukkit.util.Vector;

public class NpcProcessorTask extends BukkitRunnable {
    private final NpcRegistryImpl npcRegistry;
    private final EntityPropertyRegistryImpl propertyRegistry;
    private final UserManager userManager;

    public NpcProcessorTask(NpcRegistryImpl npcRegistry, EntityPropertyRegistryImpl propertyRegistry,UserManager userManager) {
        this.npcRegistry = npcRegistry;
        this.propertyRegistry = propertyRegistry;
        this.userManager = userManager;
    }

    public void run() {
        EntityPropertyImpl<Integer> viewDistanceProperty = propertyRegistry.getByName("view_distance", Integer.class); // Not sure why this is an Integer, but it is
        EntityPropertyImpl<LookType> lookProperty = propertyRegistry.getByName("look", LookType.class);
        EntityPropertyImpl<Double> lookDistanceProperty = propertyRegistry.getByName("look_distance", Double.class);
        EntityPropertyImpl<Boolean> permissionRequiredProperty = propertyRegistry.getByName("permission_required", Boolean.class);
        EntityPropertyImpl<Boolean> playerKnockbackProperty = propertyRegistry.getByName("player_knockback", Boolean.class);
        EntityPropertyImpl<String> playerKnockbackExemptPermissionProperty = propertyRegistry.getByName("player_knockback_exempt_permission", String.class);
        EntityPropertyImpl<Double> playerKnockbackDistanceProperty = propertyRegistry.getByName("player_knockback_distance", Double.class);
        EntityPropertyImpl<Double> playerKnockbackVerticalProperty = propertyRegistry.getByName("player_knockback_vertical", Double.class);
        EntityPropertyImpl<Double> playerKnockbackHorizontalProperty = propertyRegistry.getByName("player_knockback_horizontal", Double.class);
        EntityPropertyImpl<Integer> playerKnockbackCooldownProperty = propertyRegistry.getByName("player_knockback_cooldown", Integer.class);
        EntityPropertyImpl<Boolean> playerKnockbackSoundProperty = propertyRegistry.getByName("player_knockback_sound", Boolean.class);
        EntityPropertyImpl<Sound> playerKnockbackSoundNameProperty = propertyRegistry.getByName("player_knockback_sound_name", Sound.class);
        EntityPropertyImpl<Float> playerKnockbackSoundVolumeProperty = propertyRegistry.getByName("player_knockback_sound_volume", Float.class);
        EntityPropertyImpl<Float> playerKnockbackSoundPitchProperty = propertyRegistry.getByName("player_knockback_sound_pitch", Float.class);
        double lookDistance;
        boolean permissionRequired;
        boolean playerKnockback;
        String playerKnockbackExemptPermission = null;
        double playerKnockbackDistance = 0;
        double playerKnockbackVertical = 0;
        double playerKnockbackHorizontal = 0;
        int playerKnockbackCooldown = 0;
        boolean playerKnockbackSound = false;
        Sound playerKnockbackSoundName = null;
        float playerKnockbackSoundVolume = 0;
        float playerKnockbackSoundPitch = 0;
        for (NpcEntryImpl entry : npcRegistry.getProcessable()) {
            NpcImpl npc = entry.getNpc();
            if (!npc.isEnabled()) continue;

            double closestDist = Double.MAX_VALUE;
            Player closest = null;
            LookType lookType = npc.getProperty(lookProperty);
            lookDistance =  NumberConversions.square(npc.getProperty(lookDistanceProperty));
            permissionRequired = npc.getProperty(permissionRequiredProperty);
            playerKnockback = npc.getProperty(playerKnockbackProperty);
            if (playerKnockback) {
                playerKnockbackExemptPermission = npc.getProperty(playerKnockbackExemptPermissionProperty);
                playerKnockbackDistance = NumberConversions.square(npc.getProperty(playerKnockbackDistanceProperty));
                playerKnockbackVertical = npc.getProperty(playerKnockbackVerticalProperty);
                playerKnockbackHorizontal = npc.getProperty(playerKnockbackHorizontalProperty);
                playerKnockbackCooldown = npc.getProperty(playerKnockbackCooldownProperty);
                playerKnockbackSound = npc.getProperty(playerKnockbackSoundProperty);
                playerKnockbackSoundName = npc.getProperty(playerKnockbackSoundNameProperty);
                playerKnockbackSoundVolume = npc.getProperty(playerKnockbackSoundVolumeProperty);
                playerKnockbackSoundPitch = npc.getProperty(playerKnockbackSoundPitchProperty);
            }
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (!player.getWorld().equals(npc.getWorld())) {
                    if (npc.isVisibleTo(player)) npc.hide(player);
                    continue;
                }
                if (permissionRequired && !player.hasPermission("znpcsplus.npc." + entry.getId())) {
                    if (npc.isVisibleTo(player)) npc.hide(player);
                    continue;
                }
                double distance = player.getLocation().distanceSquared(npc.getBukkitLocation());

                // visibility
                boolean inRange = distance <= NumberConversions.square(npc.getProperty(viewDistanceProperty));
                if (!inRange && npc.isVisibleTo(player)) {
                    NpcDespawnEvent event = new NpcDespawnEvent(player, entry);
                    Bukkit.getPluginManager().callEvent(event);
                    if (!event.isCancelled()) npc.hide(player);
                }
                if (inRange) {
                    if (!npc.isVisibleTo(player)) {
                        NpcSpawnEvent event = new NpcSpawnEvent(player, entry);
                        Bukkit.getPluginManager().callEvent(event);
                        if (event.isCancelled()) continue;
                        npc.show(player);
                    }
                    if (distance < closestDist) {
                        closestDist = distance;
                        closest = player;
                    }
                    if (lookType.equals(LookType.PER_PLAYER) && lookDistance >= distance) {
                        NpcLocation expected = npc.getLocation().lookingAt(player.getLocation().add(0, -npc.getType().getHologramOffset(), 0));
                        if (!expected.equals(npc.getLocation())) npc.setHeadRotation(player, expected.getYaw(), expected.getPitch());
                    }

                    // player knockback
                    User user = userManager.get(player.getUniqueId());
                    if (playerKnockbackExemptPermission == null || !player.hasPermission(playerKnockbackExemptPermission)) {
                        if (playerKnockback && distance <= playerKnockbackDistance && user.canKnockback(playerKnockbackCooldown)) {
                            double x = npc.getLocation().getX() - player.getLocation().getX();
                            double z = npc.getLocation().getZ() - player.getLocation().getZ();
                            double angle = Math.atan2(z, x);
                            double knockbackX = -Math.cos(angle) * playerKnockbackHorizontal;
                            double knockbackZ = -Math.sin(angle) * playerKnockbackHorizontal;
                            player.setVelocity(player.getVelocity().add(new Vector(knockbackX, playerKnockbackVertical, knockbackZ)));
                            if (playerKnockbackSound)
                                player.playSound(player.getLocation(), playerKnockbackSoundName, playerKnockbackSoundVolume, playerKnockbackSoundPitch);
                        }
                    }
                }
            }
            // look property
            if (lookType.equals(LookType.CLOSEST_PLAYER)) {
                if (closest != null && lookDistance >= closestDist) {
                    NpcLocation expected = npc.getLocation().lookingAt(closest.getLocation().add(0, -npc.getType().getHologramOffset(), 0));
                    if (!expected.equals(npc.getLocation())) npc.setHeadRotation(expected.getYaw(), expected.getPitch());
                }
            }
        }
    }
}
