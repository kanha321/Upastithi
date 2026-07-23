package com.kanhaji.upastithi.screen.edit.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kanhaji.basics.composables.KTextField

@Composable
fun EditVenueFacultyCard(
    location: String,
    onLocationChanged: (String) -> Unit,
    facultyName: String,
    onFacultyNameChanged: (String) -> Unit,
    group: String,
    onGroupChanged: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Venue & Faculty",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            KTextField(
                value = location,
                onValueChange = onLocationChanged,
                label = "Room / Location",
                placeholder = "e.g. GS8, NLH2",
                leadingIcon = Icons.Default.Place
            )

            KTextField(
                value = facultyName,
                onValueChange = onFacultyNameChanged,
                label = "Faculty / Instructor Name",
                placeholder = "e.g. Dr. Himanshu Nandanwar",
                leadingIcon = Icons.Default.Person
            )

            KTextField(
                value = group,
                onValueChange = onGroupChanged,
                label = "Group / Batch (Optional)",
                placeholder = "e.g. Group B, Batch 1",
                leadingIcon = Icons.Default.Group
            )
        }
    }
}
