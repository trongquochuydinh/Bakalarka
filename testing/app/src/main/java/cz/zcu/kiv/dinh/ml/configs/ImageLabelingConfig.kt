// ImageLabelingConfig.kt
package cz.zcu.kiv.dinh.ml.configs

/**
 * Konfigurační objekt pro model označování obrázků (Image Labeling).
 * Umožňuje přepínat mezi cloudovým a offline modelem a nastavit minimální důvěru pro zobrazené štítky.
 */
object ImageLabelingConfig {
    /** Určuje, zda se má použít cloudový model (true), nebo on-device model (false) */
    var useCloudModel: Boolean = false

    /** Minimální důvěra v procentech, pod kterou se detekované štítky nezobrazují */
    var minConfidencePercentage: Int = 70
}