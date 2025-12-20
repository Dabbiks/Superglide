package com.dabbiks.superglide.game.world;


import com.dabbiks.superglide.ConsoleLogger;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.transform.AffineTransform; // POPRAWNY IMPORT
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockTypes;
import org.bukkit.Location;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Random;

import static com.dabbiks.superglide.Superglide.plugin;

public class SchematicPaster {

    private static File schematicsFolder;
    private static Random random;

    public SchematicPaster() {
        schematicsFolder = new File(plugin.getDataFolder(), "schematics");
        if (!schematicsFolder.exists()) {
            schematicsFolder.mkdirs();
        }
        random = new Random();
    }

    public static void pasteSchematic(String fileName, Location location) {
        File file = new File(schematicsFolder, fileName);

        if (!file.exists()) {
            ConsoleLogger.warning(ConsoleLogger.Type.WORLD_GENERATOR, "Missing schematic file");
            return;
        }

        ClipboardFormat format = ClipboardFormats.findByFile(file);
        if (format == null) return;

        try (ClipboardReader reader = format.getReader(new FileInputStream(file))) {
            Clipboard clipboard = reader.read();

            clipboard.getRegion().forEach(vec -> {
                BaseBlock block = clipboard.getFullBlock(vec);
                if (block.getBlockType() == BlockTypes.BEDROCK) {
                    clipboard.setBlock(vec, BlockTypes.AIR.getDefaultState().toBaseBlock());
                }
            });

            ClipboardHolder holder = new ClipboardHolder(clipboard);

            int rotateTimes = random.nextInt(4);
            holder.setTransform(new AffineTransform().rotateY(rotateTimes * 90));

            try (EditSession editSession = WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(location.getWorld()))) {
                Operation operation = holder
                        .createPaste(editSession)
                        .to(BlockVector3.at(location.getX(), location.getY(), location.getZ()))
                        .ignoreAirBlocks(true)
                        .build();

                Operations.complete(operation);
            } catch (WorldEditException e) {
                e.printStackTrace();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}