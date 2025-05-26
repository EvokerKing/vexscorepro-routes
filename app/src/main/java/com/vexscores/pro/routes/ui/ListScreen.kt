package com.vexscores.pro.routes.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.vexscores.pro.routes.MainActivity

val routes = mapOf(
	"Events List" to "List of robotics events",
	"Event Info" to "Info about specific event",
	"Event Teams" to "Get teams in an event",
	"Event Skills" to "Skills runs in an event",
	"Event Awards" to "Awarded teams and info about the award",
	"Event Division Rankings" to "Rankings of teams within a division",
	"Event Division Matches" to "Matches within a division",
	"Teams List" to "List of teams",
	"Team Info" to "Info about a team",
	"Team Matches" to "Matches a team is enrolled in",
	"Team Awards" to "Awards a team has achieved",
	"Team Skills" to "Skills runs a team has performed",
	"Team Rankings" to "Rankings a team has ended with per event",
	"Programs" to "List of supported programs",
	"Seasons" to "List of seasons/games"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListScreen(controller: NavController, context: MainActivity) {
	val scroll = rememberScrollState()

	Scaffold(
		modifier = Modifier.fillMaxSize(),
		topBar = {
			CenterAlignedTopAppBar(
				title = { Text("VEX Score Pro - Routes") },
				colors = if (scroll.value > 0) TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = context.scheme.surfaceContainer) else TopAppBarDefaults.centerAlignedTopAppBarColors()
			)
		}
	) { innerPadding ->
		Column(modifier = Modifier.padding(innerPadding).verticalScroll(scroll)) {
			routes.onEachIndexed { index, i ->
				ListItem(
					headlineContent = { Text(i.key) },
					supportingContent = { Text(i.value) },
					modifier = Modifier.clickable {
						controller.navigate(i.key)
					}
				)
				if (index != routes.size) {
					HorizontalDivider()
				}
			}
		}
	}
}