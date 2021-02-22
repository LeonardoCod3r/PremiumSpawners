package centralworks.lib;

import com.google.common.collect.Maps;
import lombok.Setter;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.HashMap;
import java.util.Locale;

public class BalanceFormatter {

    @Setter
    private static HashMap<String, Double> format = Maps.newHashMap();
    @Setter
    private static String[] suffix;

    public static double getMultiplier(String txt) {
        txt = txt.replaceAll("\\d", "").replace(" ", "");
        for (String key : format.keySet()) {
            if (key.equalsIgnoreCase(txt)) {
                return format.get(key);
            }
        }
        return 1.0;
    }

    public static Double getValue(String txt) {
        txt = txt.replaceAll("\\D", "").replace(" ", "");
        return Double.valueOf(txt);
    }

    private static double getMultipliedValue(String txt) {
        return getValue(txt) * getMultiplier(txt);
    }

    public static Double get(String[] s) {
        double value = 0.0;
        for (String vls : s) {
            value += getMultipliedValue(vls);
        }
        return value;
    }

    public static String format(Double value) {
        int index = 0;
        while (value / 1000.0 >= 1.0) {
            value /= 1000.0;
            ++index;
        }
//        for (index = 0; value / 1000.0 >= 1.0; value /= 1000.0, ++index) ;
        final DecimalFormat decimalFormat = new DecimalFormat("#.##");
        decimalFormat.setDecimalFormatSymbols(new DecimalFormatSymbols(new Locale("pt", "BR")));
        try {
            decimalFormat.setRoundingMode(RoundingMode.UNNECESSARY);
            return decimalFormat.format(value) + suffix[index];
        } catch (Exception e) {
            decimalFormat.setRoundingMode(RoundingMode.HALF_EVEN);
            return decimalFormat.format(value) + suffix[index];
        }
    }

}
