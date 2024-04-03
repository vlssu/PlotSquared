/*
 * PlotSquared, a land and world management plugin for Minecraft.
 * Copyright (C) IntellectualSites <https://intellectualsites.com>
 * Copyright (C) IntellectualSites team and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.plotsquared.core.command;

import com.google.inject.Injector;
import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.configuration.Settings;
import com.plotsquared.core.configuration.caption.TranslatableCaption;
import com.plotsquared.core.location.Location;
import com.plotsquared.core.permissions.Permission;
import com.plotsquared.core.player.ConsolePlayer;
import com.plotsquared.core.player.MetaDataAccess;
import com.plotsquared.core.player.PlayerMetaDataKeys;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.PlotArea;
import com.plotsquared.core.plot.world.SinglePlotArea;
import com.plotsquared.core.util.EconHandler;
import com.plotsquared.core.util.PlotExpression;
import com.plotsquared.core.util.task.RunnableVal2;
import com.plotsquared.core.util.task.RunnableVal3;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * PlotSquared command class.
 */
@CommandDeclaration(command = "plot",
        aliases = {"plots", "p", "plotsquared", "plot2", "p2", "ps", "2", "plotme", "plotz", "ap"})
public class MainCommand extends Command {

    private static final Logger LOGGER = LogManager.getLogger("PlotSquared/" + MainCommand.class.getSimpleName());

    private static MainCommand instance;
    public Help help;
    public Toggle toggle;

    private MainCommand() {
        super(null, true);
        instance = this;
    }

    public static MainCommand getInstance() {
        if (instance == null) {
            instance = new MainCommand();

            final Injector injector = PlotSquared.platform().injector();
            final List<Class<? extends Command>> commands = new LinkedList<>();
            commands.add(Caps.class);
            commands.add(Buy.class);
            if (Settings.Web.LEGACY_WEBINTERFACE) {
                LOGGER.warn("Legacy webinterface is used. Please note that it will be removed in future.");
            }
            commands.add(Load.class);
            commands.add(Confirm.class);
            commands.add(Template.class);
            commands.add(Download.class);
            commands.add(Setup.class);
            commands.add(Area.class);
            commands.add(DebugSaveTest.class);
            commands.add(DebugLoadTest.class);
            commands.add(CreateRoadSchematic.class);
            commands.add(DebugAllowUnsafe.class);
            commands.add(RegenAllRoads.class);
            commands.add(Claim.class);
            commands.add(Auto.class);
            commands.add(HomeCommand.class);
            commands.add(Visit.class);
            commands.add(Set.class);
            commands.add(Clear.class);
            commands.add(Delete.class);
            commands.add(Trust.class);
            commands.add(Add.class);
            commands.add(Leave.class);
            commands.add(Deny.class);
            commands.add(Remove.class);
            commands.add(Info.class);
            commands.add(Near.class);
            commands.add(ListCmd.class);
            commands.add(Debug.class);
            commands.add(SchematicCmd.class);
            commands.add(PluginCmd.class);
            commands.add(Purge.class);
            commands.add(Reload.class);
            commands.add(Merge.class);
            commands.add(DebugPaste.class);
            commands.add(Unlink.class);
            commands.add(Kick.class);
            commands.add(Inbox.class);
            commands.add(Comment.class);
            commands.add(DatabaseCommand.class);
            commands.add(Swap.class);
            commands.add(Music.class);
            commands.add(DebugRoadRegen.class);
            commands.add(DebugExec.class);
            commands.add(FlagCommand.class);
            commands.add(Target.class);
            commands.add(Move.class);
            commands.add(Condense.class);
            commands.add(Copy.class);
            commands.add(Trim.class);
            commands.add(Done.class);
            commands.add(Continue.class);
            commands.add(Middle.class);
            commands.add(Grant.class);
            commands.add(Owner.class);
            commands.add(Desc.class);
            commands.add(Biome.class);
            commands.add(Alias.class);
            commands.add(SetHome.class);
            commands.add(Cluster.class);
            commands.add(DebugImportWorlds.class);
            commands.add(Backup.class);

            if (Settings.Ratings.USE_LIKES) {
                commands.add(Like.class);
                commands.add(Dislike.class);
            } else {
                commands.add(Rate.class);
            }

            for (final Class<? extends Command> command : commands) {
                try {
                    injector.getInstance(command);
                } catch (final Exception e) {
                    LOGGER.error("Failed to register command {}", command.getCanonicalName());
                    e.printStackTrace();
                }
            }

            // Referenced commands
            instance.toggle = injector.getInstance(Toggle.class);
            instance.help = new Help(instance);
        }
        return instance;
    }

    public static boolean onCommand(final PlotPlayer<?> player, String... args) {
        final EconHandler econHandler = PlotSquared.platform().econHandler();
        if (args.length >= 1 && args[0].contains(":")) {
            String[] split2 = args[0].split(":");
            if (split2.length == 2) {
                // Ref: c:v, this will push value to the last spot in the array
                // ex. /p h:2 SomeUsername
                // > /p h SomeUsername 2
                String[] tmp = new String[args.length + 1];
                tmp[0] = split2[0];
                tmp[args.length] = split2[1];
                if (args.length >= 2) {
                    System.arraycopy(args, 1, tmp, 1, args.length - 1);
                }
                args = tmp;
            }
        }
        try {
            getInstance().execute(player, args, new RunnableVal3<>() {
                @Override
                public void run(final Command cmd, final Runnable success, final Runnable failure) {
                    if (cmd.hasConfirmation(player)) {
                        CmdConfirm.addPending(player, cmd.getUsage(), () -> {
                            PlotArea area = player.getApplicablePlotArea();
                            if (area != null && econHandler.isEnabled(area) && !player.hasPermission(Permission.PERMISSION_ADMIN_BYPASS_ECON)) {
                                PlotExpression priceEval =
                                        area.getPrices().get(cmd.getFullId());
                                double price = priceEval != null ? priceEval.evaluate(0d) : 0d;
                                if (econHandler.getMoney(player) < price) {
                                    if (failure != null) {
                                        failure.run();
                                    }
                                    return;
                                }
                            }
                            if (success != null) {
                                success.run();
                            }
                        });
                        return;
                    }
                    PlotArea area = player.getApplicablePlotArea();
                    if (area != null && econHandler.isEnabled(area) && !player.hasPermission(Permission.PERMISSION_ADMIN_BYPASS_ECON)) {
                        PlotExpression priceEval = area.getPrices().get(cmd.getFullId());
                        double price = priceEval != null ? priceEval.evaluate(0d) : 0d;
                        if (price != 0d && econHandler.getMoney(player) < price) {
                            if (failure != null) {
                                failure.run();
                            }
                            return;
                        }
                    }
                    if (success != null) {
                        success.run();
                    }
                }
            }, new RunnableVal2<>() {
                @Override
                public void run(Command cmd, CommandResult result) {
                    // Post command stuff!?
                }
            }).thenAccept(result -> {
                // TODO: Something with the command result
            });
        } catch (CommandException e) {
            e.perform(player);
        }
        // Always true
        return true;
    }

    @Override
    public CompletableFuture<Boolean> execute(
            final PlotPlayer<?> player, String[] args,
            RunnableVal3<Command, Runnable, Runnable> confirm,
            RunnableVal2<Command, CommandResult> whenDone
    ) {
        // Optional command scope //
        Location location = null;
        Plot plot = null;
        boolean tp = false;
        if (args.length >= 2) {
            PlotArea area = player.getApplicablePlotArea();
            Plot newPlot = Plot.fromString(area, args[0]);
            if (newPlot != null && (player instanceof ConsolePlayer || newPlot.getArea()
                    .equals(area) || player.hasPermission(Permission.PERMISSION_ADMIN)
                    || player.hasPermission(Permission.PERMISSION_ADMIN_AREA_SUDO))
                    && !newPlot.isDenied(player.getUUID())) {
                final Location newLoc;
                if (newPlot.getArea() instanceof SinglePlotArea) {
                    newLoc = newPlot.isLoaded() ? newPlot.getCenterSynchronous() : Location.at("", 0, 0, 0);
                } else {
                    newLoc = newPlot.getCenterSynchronous();
                }
                if (player.canTeleport(newLoc)) {
                    // Save meta
                    try (final MetaDataAccess<Location> locationMetaDataAccess
                                 = player.accessTemporaryMetaData(PlayerMetaDataKeys.TEMPORARY_LOCATION)) {
                        location = locationMetaDataAccess.get().orElse(null);
                        locationMetaDataAccess.set(newLoc);
                    }
                    try (final MetaDataAccess<Plot> plotMetaDataAccess
                                 = player.accessTemporaryMetaData(PlayerMetaDataKeys.TEMPORARY_LAST_PLOT)) {
                        plot = plotMetaDataAccess.get().orElse(null);
                        plotMetaDataAccess.set(newPlot);
                    }
                    tp = true;
                } else {
                    player.sendMessage(TranslatableCaption.of("border.denied"));
                    return CompletableFuture.completedFuture(false);
                }
                // Trim command
                args = Arrays.copyOfRange(args, 1, args.length);
            }
            if (args.length >= 2 && !args[0].isEmpty() && args[0].charAt(0) == '-') {
                if ("f".equals(args[0].substring(1))) {
                    confirm = new RunnableVal3<>() {
                        @Override
                        public void run(Command cmd, Runnable success, Runnable failure) {
                            if (area != null && PlotSquared.platform().econHandler().isEnabled(area)) {
                                PlotExpression priceEval =
                                        area.getPrices().get(cmd.getFullId());
                                double price = priceEval != null ? priceEval.evaluate(0d) : 0d;
                                if (price != 0d
                                        && PlotSquared.platform().econHandler().getMoney(player) < price) {
                                    if (failure != null) {
                                        failure.run();
                                    }
                                    return;
                                }
                            }
                            if (success != null) {
                                success.run();
                            }
                        }
                    };
                    args = Arrays.copyOfRange(args, 1, args.length);
                } else {
                    player.sendMessage(TranslatableCaption.of("errors.invalid_command_flag"));
                    return CompletableFuture.completedFuture(false);
                }
            }
        }
        try {
            super.execute(player, args, confirm, whenDone);
        } catch (CommandException e) {
            throw e;
        } catch (Throwable e) {
            e.printStackTrace();
            String message = e.getMessage();
            if (message != null) {
                player.sendMessage(
                        TranslatableCaption.of("errors.error"),
                        TagResolver.resolver("value", Tag.inserting(Component.text(message)))
                );
            } else {
                player.sendMessage(
                        TranslatableCaption.of("errors.error_console"));
            }
        }
        // Reset command scope //
        if (tp && !(player instanceof ConsolePlayer)) {
            try (final MetaDataAccess<Location> locationMetaDataAccess
                         = player.accessTemporaryMetaData(PlayerMetaDataKeys.TEMPORARY_LOCATION)) {
                if (location == null) {
                    locationMetaDataAccess.remove();
                } else {
                    locationMetaDataAccess.set(location);
                }
            }
            try (final MetaDataAccess<Plot> plotMetaDataAccess
                         = player.accessTemporaryMetaData(PlayerMetaDataKeys.TEMPORARY_LAST_PLOT)) {
                if (plot == null) {
                    plotMetaDataAccess.remove();
                } else {
                    plotMetaDataAccess.set(plot);
                }
            }
        }
        return CompletableFuture.completedFuture(true);
    }

    @Override
    public boolean canExecute(PlotPlayer<?> player, boolean message) {
        return true;
    }

}
