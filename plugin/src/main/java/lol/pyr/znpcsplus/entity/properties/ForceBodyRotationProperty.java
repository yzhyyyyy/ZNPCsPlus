package lol.pyr.znpcsplus.entity.properties;

import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import lol.pyr.znpcsplus.ZNpcsPlusBootstrap;
import lol.pyr.znpcsplus.entity.PacketEntity;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Map;

public class ForceBodyRotationProperty extends DummyProperty<Boolean> {
    private final ZNpcsPlusBootstrap plugin;

    public ForceBodyRotationProperty(ZNpcsPlusBootstrap plugin) {
        super("force_body_rotation", false);
        this.plugin = plugin;
    }

    @Override
    public void apply(Player player, PacketEntity entity, boolean isSpawned, Map<Integer, EntityData> properties) {
        if (entity.getProperty(this)) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> entity.swingHand(player, false), 2L);
            Bukkit.getScheduler().runTaskLater(plugin, () -> entity.swingHand(player, false), 6L);
        }
    }
}
