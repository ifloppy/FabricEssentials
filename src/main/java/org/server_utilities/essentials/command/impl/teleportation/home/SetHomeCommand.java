package org.server_utilities.essentials.command.impl.teleportation.home;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import org.server_utilities.essentials.command.Properties;
import org.server_utilities.essentials.command.util.OptionalOfflineTargetCommand;
import org.server_utilities.essentials.storage.EssentialsDataStorage;
import org.server_utilities.essentials.storage.UserDataStorage;
import org.server_utilities.essentials.storage.util.Location;
import org.server_utilities.essentials.teleportation.Home;

import java.util.List;
import java.util.Optional;

public class SetHomeCommand extends OptionalOfflineTargetCommand {

    private static final SimpleCommandExceptionType ALREADY_EXISTS = new SimpleCommandExceptionType(new TranslatableComponent("text.fabric-essentials.command.sethome.already_exists"));
    private static final String NAME = "name";

    public SetHomeCommand() {
        super(Properties.create("sethome").permission("sethome"));
    }

    @Override
    protected void register(LiteralArgumentBuilder<CommandSourceStack> literal) {
        RequiredArgumentBuilder<CommandSourceStack, String> name = Commands.argument(NAME, StringArgumentType.string());
        registerOptionalArgument(name);
        literal.then(name);
    }

    @Override
    protected int onSelf(CommandContext<CommandSourceStack> ctx, ServerPlayer sender) throws CommandSyntaxException {
        return setHome(ctx, StringArgumentType.getString(ctx, NAME), sender.getGameProfile(), true);
    }

    @Override
    protected int onOther(CommandContext<CommandSourceStack> ctx, ServerPlayer sender, GameProfile target) throws CommandSyntaxException {
        return setHome(ctx, StringArgumentType.getString(ctx, NAME), target, false);
    }

    private int setHome(CommandContext<CommandSourceStack> ctx, String name, GameProfile target, boolean self) throws CommandSyntaxException {
        ServerPlayer serverPlayer = ctx.getSource().getPlayerOrException();
        EssentialsDataStorage dataStorage = getEssentialsDataStorage(ctx);
        UserDataStorage userData = dataStorage.getUserData(target.getId());
        Optional<Home> optional = userData.getHome(name);
        if (optional.isEmpty()) {
            List<Home> homes = userData.getHomes();
            Home newHome = new Home(name, new Location(serverPlayer));
            homes.add(newHome);
            ctx.getSource().sendSuccess(self ? new TranslatableComponent("text.fabric-essentials.command.sethome.self", name) :
                    new TranslatableComponent("text.fabric-essentials.command.sethome.other", name, target.getName()), false);
            return 1;
        } else {
            throw ALREADY_EXISTS.create();
        }
    }
}
