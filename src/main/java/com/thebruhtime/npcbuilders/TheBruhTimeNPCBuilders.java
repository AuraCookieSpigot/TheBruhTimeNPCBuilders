package com.thebruhtime.npcbuilders;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCRegistry;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public class TheBruhTimeNPCBuilders extends JavaPlugin {

    private final List<Integer> builderNPCs = new ArrayList<>();

    private final String[] names = {
            "FoundationBruh",
            "WallBruh",
            "RoofBruh",
            "WindowBruh",
            "InsideBruh",
            "FinalBruh"
    };

    private final String[] roles = {
            "Foundation",
            "Walls",
            "Roof",
            "Windows and Doors",
            "Interior",
            "Details and Cleanup"
    };

    @Override
    public void onEnable() {
        getLogger().info("TheBruhTimeNPCBuilders has started!");

        if (!CitizensAPI.hasImplementation()) {
            getLogger().severe("Citizens is required!");
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        getLogger().info("TheBruhTimeNPCBuilders has stopped.");
    }

    @Override
    public boolean onCommand(
            CommandSender sender,
            Command command,
            String label,
            String[] args
    ) {
        if (!command.getName().equalsIgnoreCase("npcbuilder")) {
            return false;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        if (args.length == 0) {
            showHelp(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "spawnteam":
                spawnTeam(player);
                break;

            case "removeall":
                removeAll(player);
                break;

            case "list":
                listBuilders(player);
                break;

            case "build":
                if (args.length >= 2 && args[1].equalsIgnoreCase("house")) {
                    buildHouse(player);
                } else {
                    player.sendMessage(ChatColor.YELLOW + "Use /npcbuilder build house");
                }
                break;

            default:
                showHelp(player);
                break;
        }

        return true;
    }

    private void spawnTeam(Player player) {
        removeAllSilent();

        NPCRegistry registry = CitizensAPI.getNPCRegistry();
        Location center = player.getLocation();

        double[][] positions = {
                {-3, 0, 2},
                {0, 0, 2},
                {3, 0, 2},
                {-3, 0, -2},
                {0, 0, -2},
                {3, 0, -2}
        };

        for (int i = 0; i < 6; i++) {
            NPC npc = registry.createNPC(EntityType.PLAYER, names[i]);

            Location spawnLocation = center.clone().add(
                    positions[i][0],
                    positions[i][1],
                    positions[i][2]
            );

            npc.spawn(spawnLocation);

            npc.data().setPersistent("builder-role", roles[i]);
            npc.data().setPersistent("builder-number", i + 1);

            builderNPCs.add(npc.getId());
        }

        player.sendMessage(ChatColor.AQUA + "TheBruhTime builder team spawned!");
        player.sendMessage(ChatColor.GREEN + "All 6 builders are ready!");
    }

    private void buildHouse(Player player) {
        Location start = player.getLocation()
                .clone()
                .add(
                        player.getLocation()
                                .getDirection()
                                .normalize()
                                .multiply(8)
                );

        start.setY(
                player.getWorld()
                        .getHighestBlockYAt(
                                start.getBlockX(),
                                start.getBlockZ()
                        )
        );

        player.sendMessage(ChatColor.GOLD + "The builders are starting the house!");

        List<BlockPlacement> blocks = createHouseBlueprint(start);

        placeBlocksSlowly(player, blocks);
    }

    private List<BlockPlacement> createHouseBlueprint(Location start) {
        List<BlockPlacement> blocks = new ArrayList<>();

        World world = start.getWorld();

        int baseX = start.getBlockX();
        int baseY = start.getBlockY();
        int baseZ = start.getBlockZ();

        int width = 7;
        int depth = 7;
        int wallHeight = 4;

        // FLOOR
        for (int x = 0; x < width; x++) {
            for (int z = 0; z < depth; z++) {
                blocks.add(
                        new BlockPlacement(
                                world,
                                baseX + x,
                                baseY,
                                baseZ + z,
                                Material.OAK_PLANKS
                        )
                );
            }
        }

        // WALLS
        for (int y = 1; y <= wallHeight; y++) {
            for (int x = 0; x < width; x++) {
                blocks.add(
                        new BlockPlacement(
                                world,
                                baseX + x,
                                baseY + y,
                                baseZ,
                                Material.OAK_PLANKS
                        )
                );

                blocks.add(
                        new BlockPlacement(
                                world,
                                baseX + x,
                                baseY + y,
                                baseZ + depth - 1,
                                Material.OAK_PLANKS
                        )
                );
            }

            for (int z = 1; z < depth - 1; z++) {
                blocks.add(
                        new BlockPlacement(
                                world,
                                baseX,
                                baseY + y,
                                baseZ + z,
                                Material.OAK_PLANKS
                        )
                );

                blocks.add(
                        new BlockPlacement(
                                world,
                                baseX + width - 1,
                                baseY + y,
                                baseZ + z,
                                Material.OAK_PLANKS
                        )
                );
            }
        }

        // DOOR OPENING
        removePlacement(blocks, baseX + 3, baseY + 1, baseZ);
        removePlacement(blocks, baseX + 3, baseY + 2, baseZ);

        // WINDOWS
        removePlacement(blocks, baseX, baseY + 2, baseZ + 3);
        removePlacement(blocks, baseX + width - 1, baseY + 2, baseZ + 3);
        removePlacement(blocks, baseX + 3, baseY + 2, baseZ + depth - 1);

        blocks.add(
                new BlockPlacement(
                        world,
                        baseX,
                        baseY + 2,
                        baseZ + 3,
                        Material.GLASS
                )
        );

        blocks.add(
                new BlockPlacement(
                        world,
                        baseX + width - 1,
                        baseY + 2,
                        baseZ + 3,
                        Material.GLASS
                )
        );

        blocks.add(
                new BlockPlacement(
                        world,
                        baseX + 3,
                        baseY + 2,
                        baseZ + depth - 1,
                        Material.GLASS
                )
        );

        // ROOF
        for (int x = -1; x <= width; x++) {
            for (int z = -1; z <= depth; z++) {
                blocks.add(
                        new BlockPlacement(
                                world,
                                baseX + x,
                                baseY + wallHeight + 1,
                                baseZ + z,
                                Material.SPRUCE_PLANKS
                        )
                );
            }
        }

        // TORCH
        blocks.add(
                new BlockPlacement(
                        world,
                        baseX + 3,
                        baseY + 2,
                        baseZ + 3,
                        Material.TORCH
                )
        );

        return blocks;
    }

    private void removePlacement(
            List<BlockPlacement> blocks,
            int x,
            int y,
            int z
    ) {
        blocks.removeIf(
                placement ->
                        placement.x == x
                                && placement.y == y
                                && placement.z == z
        );
    }

    private void placeBlocksSlowly(
            Player player,
            List<BlockPlacement> blocks
    ) {
        final int[] index = {0};

        Bukkit.getScheduler().runTaskTimer(
                this,
                task -> {
                    if (index[0] >= blocks.size()) {
                        player.sendMessage(ChatColor.GREEN + "House complete!");
                        task.cancel();
                        return;
                    }

                    int blocksPerRun = 2;

                    for (int i = 0; i < blocksPerRun; i++) {
                        if (index[0] >= blocks.size()) {
                            break;
                        }

                        BlockPlacement placement = blocks.get(index[0]);
                        placement.place();
                        index[0]++;
                    }
                },
                0L,
                4L
        );
    }

    private void removeAll(Player player) {
        int removed = removeAllSilent();

        player.sendMessage(
                ChatColor.YELLOW
                        + "Removed "
                        + removed
                        + " builder NPCs."
        );
    }

    private int removeAllSilent() {
        NPCRegistry registry = CitizensAPI.getNPCRegistry();

        int removed = 0;

        for (Integer npcId : builderNPCs) {
            NPC npc = registry.getById(npcId);

            if (npc != null) {
                npc.destroy();
                removed++;
            }
        }

        builderNPCs.clear();

        return removed;
    }

    private void listBuilders(Player player) {
        player.sendMessage(ChatColor.GOLD + "=== TheBruhTime Builders ===");

        for (int i = 0; i < 6; i++) {
            player.sendMessage(
                    ChatColor.AQUA
                            + ""
                            + (i + 1)
                            + ". "
                            + names[i]
                            + ChatColor.WHITE
                            + " - "
                            + roles[i]
            );
        }
    }

    private void showHelp(Player player) {
        player.sendMessage(ChatColor.GOLD + "=== TheBruhTimeNPCBuilders ===");

        player.sendMessage(
                ChatColor.YELLOW
                        + "/npcbuilder spawnteam"
                        + ChatColor.WHITE
                        + " - Spawn all 6 builders"
        );

        player.sendMessage(
                ChatColor.YELLOW
                        + "/npcbuilder build house"
                        + ChatColor.WHITE
                        + " - Build a wooden house"
        );

        player.sendMessage(
                ChatColor.YELLOW
                        + "/npcbuilder removeall"
                        + ChatColor.WHITE
                        + " - Remove all builders"
        );

        player.sendMessage(
                ChatColor.YELLOW
                        + "/npcbuilder list"
                        + ChatColor.WHITE
                        + " - Show builder jobs"
        );
    }

    private static class BlockPlacement {

        private final World world;
        private final int x;
        private final int y;
        private final int z;
        private final Material material;

        public BlockPlacement(
                World world,
                int x,
                int y,
                int z,
                Material material
        ) {
            this.world = world;
            this.x = x;
            this.y = y;
            this.z = z;
            this.material = material;
        }

        public void place() {
            Block block = world.getBlockAt(x, y, z);
            block.setType(material);
        }
    }
}
