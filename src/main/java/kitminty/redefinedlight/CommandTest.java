package kitminty.redefinedlight;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import static net.minecraft.commands.Commands.argument;
import com.mojang.logging.LogUtils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;

public class CommandTest {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("kitminty").then(argument("player", StringArgumentType.string()).executes(context -> {
            final String name = StringArgumentType.getString(context, "player");
            final ServerPlayer player = context.getSource().getPlayerOrException();
            player.displayClientMessage(Component.literal(name), false);
            LOGGER.info("works");
            return 1;
        })));
    }
}
