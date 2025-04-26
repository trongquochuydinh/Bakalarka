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

@Composable
fun CoordTable(
    boundingBoxes: List<ComposeRect>,
    selectedIndex: Int?,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(16.dp)) {
        Text("Detected Coordinates", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))

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

        HorizontalDivider()

        LazyColumn(
            modifier = Modifier.heightIn(max = 150.dp) // přidáno omezení výšky na cca 5 řádků
        ) {
            itemsIndexed(boundingBoxes) { index, box ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable { onSelect(index) },
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("${index + 1}", modifier = Modifier.weight(0.2f),
                        color = if (index == selectedIndex) MaterialTheme.colorScheme.primary else LocalContentColor.current)
                    Text("%.1f".format(box.left), modifier = Modifier.weight(0.2f))
                    Text("%.1f".format(box.top), modifier = Modifier.weight(0.2f))
                    Text("%.1f".format(box.right), modifier = Modifier.weight(0.2f))
                    Text("%.1f".format(box.bottom), modifier = Modifier.weight(0.2f))
                }
            }
        }
    }
}
