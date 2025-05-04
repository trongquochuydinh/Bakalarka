package cz.zcu.kiv.dinh.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect as ComposeRect
import androidx.compose.ui.unit.dp

/**
 * Komponenta pro zobrazení tabulky detekovaných souřadnic (bounding boxů).
 *
 * @param boundingBoxes Seznam detekovaných oblastí ve formátu Rect.
 * @param selectedIndex Index právě vybrané oblasti – použitý pro zvýraznění.
 * @param onSelect Callback volaný při kliknutí na řádek, vrací index zvoleného boxu.
 * @param modifier Volitelný modifikátor pro úpravu vzhledu komponenty.
 */
@Composable
fun CoordTable(
    boundingBoxes: List<ComposeRect>,
    selectedIndex: Int?,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(16.dp)) {
        // Nadpis tabulky
        Text("Detected Coordinates", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))

        // Hlavička tabulky – názvy sloupců
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("ID", modifier = Modifier.weight(0.2f))
            Text("Left", modifier = Modifier.weight(0.2f))
            Text("Top", modifier = Modifier.weight(0.2f))
            Text("Right", modifier = Modifier.weight(0.2f))
            Text("Bottom", modifier = Modifier.weight(0.2f))
        }

        // Oddělovač mezi hlavičkou a tělem tabulky
        HorizontalDivider()

        // Tělo tabulky – seznam boxů
        LazyColumn(
            modifier = Modifier.heightIn(max = 150.dp) // omezení výšky tabulky na cca 5 řádků
        ) {
            itemsIndexed(boundingBoxes) { index, box ->
                // Jeden řádek tabulky
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable { onSelect(index) }, // umožňuje kliknutí na řádek
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Každý sloupec se zobrazuje s příslušnou vahou
                    Text(
                        "${index + 1}",
                        modifier = Modifier.weight(0.2f),
                        color = if (index == selectedIndex)
                            MaterialTheme.colorScheme.primary // zvýraznění vybraného řádku
                        else LocalContentColor.current
                    )
                    Text("%.1f".format(box.left), modifier = Modifier.weight(0.2f))
                    Text("%.1f".format(box.top), modifier = Modifier.weight(0.2f))
                    Text("%.1f".format(box.right), modifier = Modifier.weight(0.2f))
                    Text("%.1f".format(box.bottom), modifier = Modifier.weight(0.2f))
                }
            }
        }
    }
}