package centralworks.lib;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import java.lang.reflect.Method;
import java.util.List;

public class BookViewer {

    private static boolean initialised;
    private static Method getHandle;
    private static Method openBook;

    static {
        try {
            getHandle = ReflectionUtils.getMethod("CraftPlayer", ReflectionUtils.PackageType.CRAFTBUKKIT_ENTITY, "getHandle");
            openBook = ReflectionUtils.getMethod("EntityPlayer", ReflectionUtils.PackageType.MINECRAFT_SERVER, "openBook", ReflectionUtils.PackageType.MINECRAFT_SERVER.getClass("ItemStack"));
            initialised = true;
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
            initialised = false;
        }
    }

    public static boolean isInitialised() {
        return initialised;
    }

    /**
     * Open a "Virtual" Book ItemStack.
     *
     * @param i Book ItemStack.
     * @param p Player that will open the book.
     * @return "true" if work
     */
    public static boolean openBook(ItemStack i, Player p) {
        if (!initialised) return false;
        final ItemStack held = p.getItemInHand();
        try {
            p.setItemInHand(i);
            sendPacket(i, p);
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
            initialised = false;
        }
        p.setItemInHand(held);
        return initialised;
    }

    private static void sendPacket(ItemStack i, Player p) throws ReflectiveOperationException {
        openBook.invoke(getHandle.invoke(p), getItemStack(i));
    }

    public static Object getItemStack(ItemStack item) {
        try {
            Method asNMSCopy = ReflectionUtils.getMethod(ReflectionUtils.PackageType.CRAFTBUKKIT_INVENTORY.getClass("CraftItemStack"), "asNMSCopy", ItemStack.class);
            return asNMSCopy.invoke(ReflectionUtils.PackageType.CRAFTBUKKIT_INVENTORY.getClass("CraftItemStack"), item);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Set the pages of the book in JSON format.
     *
     * @param metadata BookMeta of the Book ItemStack.
     * @param pages    Each page to be added to the book.
     */
    @SuppressWarnings("unchecked")
    public static void setPages(BookMeta metadata, List<String> pages) {
        List<Object> p;
        Object page;
        try {
            p = (List<Object>) ReflectionUtils.getField(ReflectionUtils.PackageType.CRAFTBUKKIT_INVENTORY.getClass("CraftMetaBook"), true, "pages").get(metadata);
            for (String text : pages) {
                page = ReflectionUtils.invokeMethod(ReflectionUtils.PackageType.MINECRAFT_SERVER.getClass("IChatBaseComponent$ChatSerializer").newInstance(), "a", text);
                p.add(page);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
