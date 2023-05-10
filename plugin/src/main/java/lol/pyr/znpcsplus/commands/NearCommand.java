package lol.pyr.znpcsplus.commands;

import lol.pyr.director.adventure.command.CommandContext;
import lol.pyr.director.adventure.command.CommandHandler;
import lol.pyr.director.common.command.CommandExecutionException;
import lol.pyr.znpcsplus.npc.NpcEntryImpl;
import lol.pyr.znpcsplus.npc.NpcRegistryImpl;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;

import java.util.stream.Collectors;

public class NearCommand implements CommandHandler {
    @Override
    public void run(CommandContext context) throws CommandExecutionException {
        Player player = context.ensureSenderIsPlayer();
        double radius = Math.pow(context.parse(Integer.class), 2);

        String npcs = NpcRegistryImpl.get().allModifiable().stream()
                        .filter(entry -> entry.getNpc().getLocation().toBukkitLocation(entry.getNpc().getWorld()).distanceSquared(player.getLocation()) < radius)
                        .map(NpcEntryImpl::getId)
                        .collect(Collectors.joining(", "));

        if (npcs.length() == 0) context.halt(Component.text("There are no npcs within " + ((int) radius) + " blocks around you.", NamedTextColor.RED));
        context.send(Component.text("All NPCs that are within " + radius + " blocks from you:", NamedTextColor.GREEN).appendNewline()
                .append(Component.text(npcs, NamedTextColor.GREEN)));

    }
}
