package com.kanhaji.upastithi.screen.home

import androidx.compose.material3.DatePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable

@Composable
fun HomeComponent2(
    screenModel: HomeScreenModel
) {
    DatePicker(state = rememberDatePickerState())
}