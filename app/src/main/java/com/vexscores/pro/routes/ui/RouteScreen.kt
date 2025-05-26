package com.vexscores.pro.routes.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RouteScreen(controller: NavController, item: String) {
	if (item == "Events List") {
		Scaffold(
			modifier = Modifier.fillMaxSize(),
			topBar = {
				CenterAlignedTopAppBar(
					title = { Text("Events List") },
					navigationIcon = {
						IconButton(onClick = { controller.popBackStack() }) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back") }
					}
				)
			}
		) { innerPadding ->
			Column(modifier = Modifier.padding(innerPadding)) {  }
		}
	}
}