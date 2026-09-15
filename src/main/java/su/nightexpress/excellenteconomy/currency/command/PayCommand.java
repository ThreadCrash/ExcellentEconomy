package su.nightexpress.excellenteconomy.currency.command;

import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;
import su.nightexpress.excellenteconomy.EconomyPlugin;
import su.nightexpress.excellenteconomy.api.currency.ExcellentCurrency;
import su.nightexpress.excellenteconomy.command.CommandArguments;
import su.nightexpress.excellenteconomy.command.currency.CurrencyCommand;
import su.nightexpress.excellenteconomy.config.Lang;
import su.nightexpress.excellenteconomy.config.Perms;
import su.nightexpress.excellenteconomy.currency.CurrencyManager;
import su.nightexpress.excellenteconomy.user.CoinsUser;
import su.nightexpress.excellenteconomy.user.UserManager;
import su.nightexpress.nightcore.commands.Arguments;
import su.nightexpress.nightcore.commands.builder.LiteralNodeBuilder;
import su.nightexpress.nightcore.commands.context.CommandContext;
import su.nightexpress.nightcore.commands.context.ParsedArguments;
import su.nightexpress.nightcore.core.config.CoreLang;

public class PayCommand implements CurrencyCommand {

    private final EconomyPlugin   plugin;
    private final CurrencyManager manager;
    private final UserManager     userManager;

    public PayCommand(@NonNull EconomyPlugin plugin, @NonNull CurrencyManager manager,
                      @NonNull UserManager userManager) {
        this.plugin = plugin;
        this.manager = manager;
        this.userManager = userManager;
    }

    @Override
    public boolean isFallback() {
        return false;
    }

    @Override
    public void build(@NonNull LiteralNodeBuilder builder, @NonNull ExcellentCurrency currency) {
        builder
            .playerOnly()
            .permission(Perms.COMMAND_CURRENCY_SEND)
            .description(Lang.COMMAND_CURRENCY_SEND_DESC)
            .withArguments(
                Arguments.playerName(CommandArguments.PLAYER),
                CommandArguments.positiveAmount(currency)
            );
    }

    @Override
    public boolean execute(@NonNull CommandContext context, @NonNull ParsedArguments arguments,
                           @NonNull ExcellentCurrency currency) {
        Player sender = context.getPlayerOrThrow();
        String targetName = arguments.getString(CommandArguments.PLAYER);
        double amount = arguments.getDouble(CommandArguments.AMOUNT);

        this.userManager.loadByNameAsync(targetName).thenAccept(opt -> {
            this.plugin.runTask(() -> {
                CoinsUser targetUser = opt.orElse(null);
                if (targetUser == null) {
                    currency.sendPrefixed(CoreLang.ERROR_INVALID_PLAYER, context.getSender());
                    return;
                }

                this.manager.send(sender, targetUser, currency, amount);
            });
        });
        return true;
    }
}
