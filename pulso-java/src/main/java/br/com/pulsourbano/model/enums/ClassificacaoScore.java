package br.com.pulsourbano.model.enums;

public enum ClassificacaoScore {
    BOM, MODERADO, RUIM, CRITICO;

    public static ClassificacaoScore from(double score) {
        if (score >= 80) return BOM;
        if (score >= 60) return MODERADO;
        if (score >= 40) return RUIM;
        return CRITICO;
    }
}
