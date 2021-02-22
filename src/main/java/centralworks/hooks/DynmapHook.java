package centralworks.hooks;

import centralworks.lib.BalanceFormatter;
import centralworks.spawners.models.Spawner;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.dynmap.bukkit.DynmapPlugin;
import org.dynmap.markers.Marker;
import org.dynmap.markers.MarkerAPI;
import org.dynmap.markers.MarkerSet;

public class DynmapHook {

    @Getter
    private static MarkerAPI markerAPI;
    @Getter
    private static MarkerSet markerSet;

    public void boot() {
        markerAPI = ((DynmapPlugin) Bukkit.getPluginManager().getPlugin("dynmap")).getMarkerAPI();
        markerSet = markerAPI.createMarkerSet("spawners_view_plugin", "Visibilidade dos geradores", markerAPI.getMarkerIcons(), false);
    }

    public void view(Spawner spawner) {
        if (markerAPI == null) return;
        final Location location = spawner.getLocation();
        final Marker marker;
        if (getMarkerSet().findMarker(spawner.getLocSerialized()) != null)
            marker = getMarkerSet().findMarker(spawner.getLocSerialized());
        else marker = getMarkerSet().createMarker(
                spawner.getLocSerialized(),
                "Gerador de " + spawner.getEntityName(),
                location.getWorld().getName(),
                location.getX(),
                location.getY(),
                location.getZ(),
                getMarkerAPI().getMarkerIcon("star"),
                false
        );
        marker.setDescription("Dono: " + spawner.getOwner() + "<br/> Pilha: " + BalanceFormatter.format(spawner.getAmount()) + "<br/> Valor: " + BalanceFormatter.format(spawner.getPriceAll()));
    }

    public void hide(Spawner spawner) {
        if (markerAPI == null) return;
        if (getMarkerSet().findAreaMarker(spawner.getLocSerialized()) != null) {
            getMarkerSet().findAreaMarker(spawner.getLocSerialized()).deleteMarker();
        }
    }

    public void shutdown() {
        if (markerAPI != null) getMarkerSet().deleteMarkerSet();
    }

}
