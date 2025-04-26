package cz.zcu.kiv.dinh.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import cz.zcu.kiv.dinh.ui.components.TopBarWithMenu

@Composable
fun HelpScreen(navController: NavController) {
    Scaffold(
        topBar = { TopBarWithMenu(navController, title = "Help") }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "Available Models for Image Analysis",
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "This application provides several models for analyzing images:",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(16.dp))

            // --- Text Recognition ---
            Text(
                text = "1. Text Recognition",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text =  "• Recognizes printed text from images.\n" +
                        "• Segments the detected text based on user selection (Block, Line, Word, or Symbol).\n" +
                        "• Highlighted rectangles represent the detected text segments.\n",
                style = MaterialTheme.typography.bodySmall
            )

            Spacer(modifier = Modifier.height(16.dp))

            // --- Image Labeling ---
            Text(
                text = "2. Image Labeling",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text =  "• Automatically labels the content of an image with predefined categories and their corresponding confidence scores.\n" +
                        "• Labels with confidence score lower than the minimal confidence score threshold will not be displayed.\n",
                style = MaterialTheme.typography.bodySmall
            )

            Spacer(modifier = Modifier.height(16.dp))

            // --- Object Detection ---
            Text(
                text = "3. Object Detection",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text =  "• Detects and locates multiple objects within an image.\n" +
                        "• Returns bounding boxes for each detected object along with a table of coordinates of each object.\n",
                style = MaterialTheme.typography.bodySmall
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Tip: \n" +
                        "• For most everyday tasks, we recommend using the On-Device models for faster and offline performance.\n" +
                        "• Switch to Cloud models when you need higher accuracy or advanced language support.\n" +
                        "• In Text Recognition and Object Detection, tapping a row in the coordinates table will highlight the corresponding bounding box in red.\n" +
                        "• The origin of coordinate axis [0;0] is the top-left corner of the image.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}