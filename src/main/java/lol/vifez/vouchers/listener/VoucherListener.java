package lol.vifez.vouchers.listener;

import lol.vifez.vouchers.VouchersPlugin;
import lol.vifez.vouchers.manager.VoucherManager;
import lol.vifez.vouchers.manager.VoucherManager.Voucher;
import lol.vifez.vouchers.util.CC;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class VoucherListener implements Listener {

    private final VoucherManager voucherManager;
    private final VouchersPlugin plugin;

    public VoucherListener(VouchersPlugin plugin, VoucherManager voucherManager) {
        this.plugin = plugin;
        this.voucherManager = voucherManager;
    }

    @EventHandler
    public void onPlayerUseVoucher(PlayerInteractEvent event) {
        switch (event.getAction()) {
            case RIGHT_CLICK_AIR:
            case RIGHT_CLICK_BLOCK:
                break;
            default:
                return;
        }

        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        if (item == null || !item.hasItemMeta()) return;

        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) return;

        String displayName = meta.getDisplayName();

        voucherManager.getVouchers().values().stream()
                .filter(voucher -> voucher.getItem() == item.getType())
                .filter(voucher -> {
                    String expectedName = CC.translate(
                            (voucher.getItemName() != null && !voucher.getItemName().isEmpty())
                                    ? voucher.getItemName()
                                    : voucherManager.getVoucherItemNameFormat().replace("<voucher>", voucher.getName())
                    );
                    return displayName.equals(expectedName);
                })
                .findFirst()
                .ifPresent(voucher -> {
                    event.setCancelled(true);
                    redeemVoucher(player, voucher, item);
                });
    }

    private void redeemVoucher(Player player, Voucher voucher, ItemStack item) {
        String playerName = player.getName();

        voucher.getActions().forEach(action -> {
            String cmd = action.replace("%user%", playerName).replace("%username%", playerName);
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
        });

        voucher.getBroadcast().forEach(broadcast -> {
            String msg = broadcast.replace("%user%", playerName).replace("%username%", playerName);
            Bukkit.broadcastMessage(CC.translate(msg));
        });

        if (item.getAmount() > 1) {
            item.setAmount(item.getAmount() - 1);
        } else {
            player.getInventory().removeItem(item);
        }

        player.sendMessage(CC.translate("&aVoucher redeemed successfully!"));
    }
}