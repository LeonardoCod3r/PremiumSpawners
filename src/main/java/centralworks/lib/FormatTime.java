package centralworks.lib;

import java.util.concurrent.TimeUnit;

public class FormatTime {

    private final long tempo;

    public FormatTime(long tempo) {
        this.tempo = tempo;
    }

    public String format() {
        if (tempo == 0) return "0 segundos";
        final long dias = TimeUnit.MILLISECONDS.toDays(tempo);
        final long horas = TimeUnit.MILLISECONDS.toHours(tempo) - (dias * 24);
        final long minutos = TimeUnit.MILLISECONDS.toMinutes(tempo) - (TimeUnit.MILLISECONDS.toHours(tempo) * 60);
        final long segundos = TimeUnit.MILLISECONDS.toSeconds(tempo) - (TimeUnit.MILLISECONDS.toMinutes(tempo) * 60);
        final StringBuilder sb = new StringBuilder();
        if (dias > 0)
            sb.append(dias).append(dias == 1 ? " dia" : " dias");
        if (horas > 0)
            sb.append(dias > 0 ? (minutos > 0 ? ", " : " e ") : "").append(horas).append(horas == 1 ? " hora" : " horas");
        if (minutos > 0)
            sb.append(dias > 0 || horas > 0 ? (segundos > 0 ? ", " : " e ") : "").append(minutos).append(minutos == 1 ? " minuto" : " minutos");
        if (segundos > 0)
            sb.append(dias > 0 || horas > 0 || minutos > 0 ? " e " : (sb.length() > 0 ? ", " : "")).append(segundos).append(segundos == 1 ? " segundo" : " segundos");
        final String s = sb.toString();
        return s.isEmpty() ? "0 segundos" : s;
    }
}
