package lol.pyr.znpcsplus.hologram;

import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import lol.pyr.znpcsplus.api.entity.EntityProperty;
import lol.pyr.znpcsplus.entity.EntityPropertyRegistryImpl;
import lol.pyr.znpcsplus.packets.PacketFactory;
import lol.pyr.znpcsplus.util.NpcLocation;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

public class HologramText extends HologramLine<Component> {

    private static final Component BLANK  = Component.text("%blank%");

    public HologramText(EntityPropertyRegistryImpl propertyRegistry, PacketFactory packetFactory, NpcLocation location, Component text) {
        super(text, packetFactory, EntityTypes.ARMOR_STAND, location);
        addProperty(propertyRegistry.getByName("name"));
        addProperty(propertyRegistry.getByName("invisible"));
    }

    @Override
    public void show(Player player) {
        if (!getValue().equals(BLANK)) {
            super.show(player);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getProperty(EntityProperty<T> key) {
        if (key.getName().equalsIgnoreCase("invisible")) return (T) Boolean.TRUE;
        if (key.getName().equalsIgnoreCase("name")) return (T) getValue();
        return super.getProperty(key);
    }

    @Override
    public boolean hasProperty(EntityProperty<?> key) {
        return key.getName().equalsIgnoreCase("name") || key.getName().equalsIgnoreCase("invisible");
    }
}
