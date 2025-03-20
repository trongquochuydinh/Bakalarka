package com.example.testing.ui.results

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.testing.ui.components.TopBarWithMenu

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageLabelingResultScreen(
    navController: NavController,
    detectedLabels: List<String>
) {
    Scaffold(
        topBar = { TopBarWithMenu(navController, title = "Image Labeling Results") },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFF625A5A)),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Display only labels with confidence > 70%
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(detectedLabels) { label ->
                    val regex = Regex("\\((\\d+)%\\)")
                    val matchResult = regex.find(label)
                    val confidence = matchResult?.groupValues?.get(1)?.toIntOrNull() ?: 0

                    if (confidence > 70) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(16.dp),
                                color = Color.Black
                            )
                        }
                    }
                }
            }
        }
    }
}
