package io.servertap.utils;

import io.servertap.ServerTapMain;
import io.servertap.api.v1.models.Player;
import io.servertap.utils.pluginwrappers.EconomyWrapper;

import java.net.InetSocketAddress;

public class JSONUtil {

    public static Player fromBukkitPlayer(org.bukkit.entity.Player player) {
        Player p = new Player();

        EconomyWrapper economyWrapper = ServerTapMain.instance.getExternalPluginWrapperRepo().getEconomyWrapper();
        if (economyWrapper.isAvailable()) {
            p.setBalance(economyWrapper.getPlayerBalance(player));
        }

        p.setUuid(player.getUniqueId().toString());
        p.setDisplayName(player.getDisplayName());

        InetSocketAddress playerAddress = player.getAddress();
        if (playerAddress != null) {
            p.setAddress(playerAddress.getHostString());
            p.setPort(playerAddress.getPort());
        }

        p.setExhaustion(player.getExhaustion());
        p.setExp(player.getExp());

        p.setWhitelisted(player.isWhitelisted());
        p.setBanned(player.isBanned());
        p.setOp(player.isOp());

        return p;
    }
}
