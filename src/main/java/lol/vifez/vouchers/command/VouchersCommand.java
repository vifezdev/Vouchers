package lol.vifez.vouchers.command;

import lol.vifez.vouchers.VouchersPlugin;
import lol.vifez.vouchers.manager.VoucherManager;
import lol.vifez.vouchers.manager.VoucherManager.Voucher;
import lol.vifez.vouchers.util.CC;
import lol.vifez.volt.api.CommandBase;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.stream.Collectors;

public class VouchersCommand extends CommandBase {

    private final VoucherManager voucherManager;
    private final VouchersPlugin plugin;

    public VouchersCommand(VouchersPlugin plugin, VoucherManager voucherManager) {
        this.voucherManager = voucherManager;
        this.plugin = plugin;

        aliases("voucher", "vouchers");
        description("Voucher management");
        permission("vouchers.manage");

        defaultHandler(this::handleHelp);

        sub("create", this::handleCreate);
        sub("delete", this::handleDelete);
        sub("give", this::handleGive);
        sub("setIcon", this::handleSetIcon);
        sub("setCommand", this::handleSetCommand);
        sub("addBroadcast", this::handleAddBroadcast);
        sub("setLore", this::handleSetLore);
        sub("list", this::handleList);
    }

    private boolean checkArgs(CommandSender sender, String[] args, int required, String usage) {
        if (args.length < required) {
            sender.sendMessage(CC.translate("&cUsage: " + usage));
            return true;
        }
        return false;
    }

    private Voucher getVoucherOrSendError(CommandSender sender, String name) {
        Voucher voucher = voucherManager.getVoucher(name.toUpperCase(Locale.ROOT));
        if (voucher == null) {
            sender.sendMessage(CC.translate("&cVoucher not found."));
            return null;
        }
        return voucher;
    }

    private void handleHelp(CommandSender sender, String[] args) {
        sender.sendMessage(" ");
        sender.sendMessage(CC.translate("&b&lVouchers commands"));
        sender.sendMessage(" ");
        sender.sendMessage(CC.translate("&7* &b/voucher create <voucher> &7- &fCreate a voucher"));
        sender.sendMessage(CC.translate("&7* &b/voucher delete <voucher> &7- &fDelete a voucher"));
        sender.sendMessage(CC.translate("&7* &b/voucher give <user> <voucher> &7- &fGive a user a voucher"));
        sender.sendMessage(CC.translate("&7* &b/voucher setIcon <voucher> &7- &fSet a voucher's item to item in hand"));
        sender.sendMessage(CC.translate("&7* &b/voucher setCommand <voucher> <command> &7- &fSet voucher's command"));
        sender.sendMessage(CC.translate("&7* &b/voucher addBroadcast <voucher> <message> &7- &fAdd broadcast message"));
        sender.sendMessage(CC.translate("&7* &b/voucher setLore <voucher> <line1|line2|...> &7- &fSet voucher lore (separate lines with |)"));
        sender.sendMessage(CC.translate("&7* &b/voucher list &7- &fList all vouchers"));
        sender.sendMessage(" ");
    }

    private void handleCreate(CommandSender sender, String[] args) {
        if (checkArgs(sender, args, 1, "/voucher create <voucher>")) return;

        String name = args[0].toUpperCase(Locale.ROOT);

        if (voucherManager.createVoucher(name)) {
            sender.sendMessage(CC.translate("&aVoucher '" + name + "' created."));
        } else {
            sender.sendMessage(CC.translate("&cVoucher already exists."));
        }
    }

    private void handleDelete(CommandSender sender, String[] args) {
        if (checkArgs(sender, args, 1, "/voucher delete <voucher>")) return;

        String name = args[0].toUpperCase(Locale.ROOT);

        if (voucherManager.deleteVoucher(name)) {
            sender.sendMessage(CC.translate("&cVoucher '" + name + "' deleted."));
        } else {
            sender.sendMessage(CC.translate("&cVoucher not found."));
        }
    }

    private void handleGive(CommandSender sender, String[] args) {
        if (checkArgs(sender, args, 2, "/voucher give <user> <voucher>")) return;

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(CC.translate("&cPlayer not found."));
            return;
        }

        Voucher voucher = getVoucherOrSendError(sender, args[1]);
        if (voucher == null) return;

        ItemStack item = voucherManager.buildVoucherItem(voucher.getName());
        target.getInventory().addItem(item);

        String voucherName = voucher.getName();
        sender.sendMessage(CC.translate("&aGave " + target.getName() + " a voucher: " + voucherName));
        target.sendMessage(CC.translate("&aYou have received a voucher: " + voucherName));
    }

    private void handleSetIcon(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(CC.translate("&cOnly players can use this command."));
            return;
        }
        if (checkArgs(sender, args, 1, "/voucher setIcon <voucher>")) return;

        Player player = (Player) sender;
        ItemStack handItem = player.getItemInHand();
        if (handItem == null || handItem.getType() == Material.AIR) {
            player.sendMessage(CC.translate("&cYou must be holding an item in your hand."));
            return;
        }

        Voucher voucher = getVoucherOrSendError(player, args[0]);
        if (voucher == null) return;

        voucher.setItem(handItem.getType());
        voucherManager.saveVouchers();
        player.sendMessage(CC.translate("&aSet voucher " + voucher.getName() + " icon to " + handItem.getType().name()));
    }

    private void handleSetCommand(CommandSender sender, String[] args) {
        if (checkArgs(sender, args, 2, "/voucher setCommand <voucher> <command>")) return;

        Voucher voucher = getVoucherOrSendError(sender, args[0]);
        if (voucher == null) return;

        String command = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        voucher.setActions(Collections.singletonList(command));

        voucherManager.saveVouchers();
        sender.sendMessage(CC.translate("&aSet command for voucher " + voucher.getName()));
    }

    private void handleAddBroadcast(CommandSender sender, String[] args) {
        if (checkArgs(sender, args, 2, "/voucher addBroadcast <voucher> <message>")) return;

        Voucher voucher = getVoucherOrSendError(sender, args[0]);
        if (voucher == null) return;

        String broadcastMessage = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        List<String> broadcasts = new ArrayList<>(voucher.getBroadcast());
        broadcasts.add(broadcastMessage);
        voucher.setBroadcast(broadcasts);

        voucherManager.saveVouchers();
        sender.sendMessage(CC.translate("&aAdded broadcast message for voucher " + voucher.getName()));
    }

    private void handleSetLore(CommandSender sender, String[] args) {
        if (checkArgs(sender, args, 2, "/voucher setLore <voucher> <line1|line2|...>")) return;

        Voucher voucher = getVoucherOrSendError(sender, args[0]);
        if (voucher == null) return;

        String loreRaw = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        List<String> lore = Arrays.stream(loreRaw.split("\\|"))
                .map(CC::translate)
                .collect(Collectors.toList());

        voucher.setLore(lore);
        voucherManager.saveVouchers();
        sender.sendMessage(CC.translate("&aSet lore for voucher " + voucher.getName()));
    }

    private void handleList(CommandSender sender, String[] args) {
        sender.sendMessage(CC.translate("&b&lAvailable vouchers:"));
        voucherManager.getVouchers().keySet().stream()
                .sorted()
                .map(name -> CC.translate("&7- &b" + name))
                .forEach(sender::sendMessage);
    }
}