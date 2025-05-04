package cz.zcu.kiv.dinh.ml.configs

/**
 * Konfigurační objekt pro model detekce objektů (Object Detection).
 * Definuje, zda se má použít cloudová varianta modelu a jaký je minimální práh důvěry.
 */
object ObjectDetectionConfig {
    /** Určuje, zda se má použít cloudový model (true), nebo on-device model (false) */
    var useCloudModel: Boolean = false

    /** Minimální důvěra v procentech, pod kterou se detekované objekty ignorují */
    var minConfidencePercentage: Int = 30
}