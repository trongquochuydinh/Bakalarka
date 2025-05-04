// TextRecognitionConfig.kt
package cz.zcu.kiv.dinh.ml.configs

/**
 * Konfigurační objekt pro model rozpoznávání textu (Text Recognition).
 * Umožňuje přepnout model, zvolit typ segmentace a definovat podmínky zvýraznění.
 */
object TextRecognitionConfig {
    /** Určuje, zda se má použít cloudový model (true), nebo on-device model (false) */
    var useCloudModel: Boolean = false

    /**
     * Režim segmentace textu. Povolené hodnoty:
     * - "block": rozdělení na bloky
     * - "line": rozdělení na řádky
     * - "word": rozdělení na slova
     * - "symbol": rozdělení na jednotlivé znaky
     */
    var segmentationMode: String = "block"

    /**
     * Zvýrazněný znak v režimu "symbol". Pokud není null, bude zvýrazněn pouze tento znak.
     */
    var highlightSymbol: Char? = null

    /**
     * Zvýrazněné slovo v režimu "word". Pokud není null, budou zvýrazněna pouze slova obsahující tento řetězec (nerozlišuje velikost písmen).
     */
    var highlightWord: String? = null
}